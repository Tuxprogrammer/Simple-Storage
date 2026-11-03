# Terminal sync (shared by wired and wireless terminals)

| # | Field | Contents |
|---|---|---|
| 1 | **Identity** | `TerminalSyncManager` (SH, `NeoForge/src/platform-shared/java/com/tom/storagemod/util/TerminalSyncManager.java`), `IAutoFillTerminal` (+ inner `ISearchHandler`) and `IDataReceiver` (SH). |
| 2 | **Inputs** | Server: the terminal's current item map `Map<StoredItemStack, TerminalItemStack>`, a change id (the BE's `changeCount`), the viewer `ServerPlayer`, an optional `Consumer<CompoundTag>` extra-sync callback. Client: the player's screen (an `IDataReceiver`), search-box text. |
| 3 | **Numbers** | `MAX_PACKET_SIZE = 64000` bytes (soft cap per `DataPacket` payload). Stacks per packet: flushed when the buffer would exceed 64000 bytes **or** more than 32000 stacks are pending (and at least 2 stacks are in the buffer) — the stack count is stored in a NBT `short`, so it must stay below 32768. Id table: ids start at 1 and grow per manager instance (per open menu). |
| 4 | **Behavior** | Ordered steps in [Behavior detail](#behavior-detail). |
| 5 | **Determinism** | Id assignment is deterministic per manager: first-appearance order of stacks. The id tables are per-menu-instance (server and client each keep their own tables, kept consistent by the protocol). No randomness. |
| 6 | **Data formats** | `DataPacket` compound fields (see network.md): `d` (byte array — binary stack list), `l` (short — stack count in `d`), `a` (byte array — one interaction). Wire format of `d`, per stack: 1 byte flags (bit 0 = quantity 0/absent; bit 1 = mini stack (NBT overflow fallback); bit 2 = id already known, stack data omitted), varint id; if not known: full `ItemStack.STREAM_CODEC` encoding (or a placeholder stack with red lore for mini stacks); if quantity != 0: varlong quantity + varint used-slot-count. Wire format of `a`: 1 byte flags (bit 0 = modifier key held; bit 1 = no stack / null target), varint stack id + varlong quantity (unless bit 1), enum `SlotAction`. `SlotAction` enum order (wire values): `PULL_OR_PUSH_STACK`(0), `PULL_ONE`(1), `SPACE_CLICK`(2), `SHIFT_PULL`(3), `GET_HALF`(4), `GET_QUARTER`(5), `CRAFT`(6). |
| 7 | **Output** | Client: `itemList` (`Map<TerminalItemStack, TerminalItemStack>` — the displayed stacks, quantity 0 = removed) and the id tables. Server: `InteractHandler.onInteract(stack, action, mod)` invoked by the menu (03-terminal). |
| 8 | **Relationships** | Instantiated by `StorageTerminalMenu` (03-terminal, one per open menu). Server-side `update` is driven by the terminal BE's change count (03-terminal `StorageTerminalBlockEntity`). `sendInteract`/`receiveInteract` are the click path from the terminal screen (06-client-ui). `IAutoFillTerminal` is implemented by `StorageTerminalMenu`/`CraftingTerminalMenu` (03-terminal) and by the JEI/EMI/REI transfer handlers (05-integration); `IAutoFillTerminal.updateSearch` is the global list of recipe-book search sync handlers. `IDataReceiver` is implemented by terminal menus (server) and terminal screens (client) and is the target of `DataPacket` (network.md). |
| 9 | **NeoForge dependencies** | `Platform.makeRegByteBuf` (RegistryFriendlyByteBuf — item stacks in packets carry registry references), `Platform.runWithPacketContext` (no-op on NF, packet-context wrapper on Fabric — the manager is called from server tick and from Fabric packet handlers), `ItemStack.STREAM_CODEC`. |
| 10 | **Bugs** | None new (the unbounded NBT carrier is `CORE-02`, network.md). |

## Behavior detail

### Server → client (inventory sync)

1. (server) `update(changeID, items, player, extraSync)`: if `changeID != lastChangeID` (the BE bumps `changeCount` whenever the tracked inventory changes): set `lastChangeID`, then under `Platform.runWithPacketContext(player, ...)` run `updateInternal`. Independently, if `extraSync != null`, build an empty `CompoundTag`, let the callback fill it, and send it to the player as a separate `DataPacket` (this is how extra fields like sort/modes/searchType ride the same channel).
2. (server) `updateInternal`: compute the changed set — for every (stack, count) in the new `items` map, remove it from the local `items` mirror; if it was absent or details differ (`equalDetails`), add the new count to `toWrite`; then every remaining entry in the mirror is written as a quantity-0 tombstone (removed item); replace the mirror with the new map. If `toWrite` is empty, nothing is sent (the `extraSync` packet in step 1 may still go out).
3. (server) For each stack in `toWrite`: `write(buf, stack)` — flags byte (bit 0 if quantity 0), varint id (assigned via `idMap.computeIfAbsent`, id = `lastId++`, also recorded in `idMap2`), full stack encoding unless the id was already known (bit 2), then varlong quantity + varint used-slot-count when quantity != 0. On `IndexOutOfBoundsException` (stack larger than the buffer headroom), rewind and `writeMiniStack` instead: a placeholder stack (same item, empty NBT) with red lore `tooltip.toms_storage.nbt_overflow` ("This item's tag is too big, the item may not show correctly in the terminal") and flag bit 1 set.
4. (server) After each write, if the buffer size would exceed 64000 bytes or more than 32000 stacks are pending (and at least 2 stacks are buffered): flush the buffer as a `DataPacket` compound (`d` = bytes, `l` = count as short) and start a new buffer; if the stack that just overflowed is itself larger than 64000 bytes even as a mini stack, it is written again as a mini stack into the new buffer (a single oversized stack can therefore produce a >64000-byte packet on its own).
5. (server) After the loop, a final non-empty buffer is flushed the same way.
6. (client) `receiveUpdate(registryAccess, tag)`: reads `d` (byte buffer codec) and `l` (short, default 0); decodes exactly `l` stacks via `read`: flags byte, varint id; if bit 2 is unset, decode the full stack (mini stacks arrive as the placeholder item + lore); if bit 2 is set, look up `idMap2` — an unknown id becomes a `BARRIER` item with red lore `tooltip.toms_storage.item_sync_error` ("Item Stack Sync error"); quantity/used-slot-count read only when bit 0 is unset (else both 0); the stack is inserted into both id tables. Stacks with quantity 0 remove the entry from `itemList`; others add/replace it. Returns whether `d` was present (so the menu knows a sync arrived).
7. (client) `getAsList()`: current displayed stacks (`itemList.values()`). `getAmount(stack)`: displayed quantity of a stack (0 if unknown) — used by the screen for counts.

### Client → server (interactions)

8. (client) `sendInteract(stack, action, mod)`: builds the `a` wire format (step, wire formats above; `mod` = a modifier key held, recorded as bit 0; a null stack sets bit 1 and writes no id/quantity) and sends it to the server as a `DataPacket`.
9. (server) `receiveInteract(tag, handler)`: reads `a`; flags byte; if bit 1 set, stack = null; else varint id → `idMap2` lookup (null id → null stack), varlong quantity; reconstructs a `StoredItemStack` copy with the client-supplied quantity (the server menu re-validates, 03-terminal); invokes `handler.onInteract(stack, SlotAction, mod)`.
10. (server) `fillStackedContents` / `fillCraftingFiller`: feed the manager's item mirror into `StackedItemContents` / `TerminalCraftingFiller` (recipe-book contents, 05-integration/03-terminal).

### IAutoFillTerminal / IDataReceiver

11. (both) `IAutoFillTerminal` (implemented by terminal menus): `sendMessage(CompoundTag)` (menu→screen extra sync), `getStoredItems()`, `isSmartSearch()`. Static `updateSearch` list of `ISearchHandler`s: JEI (`JEIHandler`), EMI (`EMIPlugin`) and REI (`REIPlugin`) each register one (05-integration). `hasSync()` = list non-empty (the terminal screen shows the search-type button only when a recipe viewer is present or smart search is active, 06-client-ui). `sync(searchString)` pushes the current search string to every registered handler (so the recipe viewer filters in sync with the terminal search box). `getHandlerName()/getHandlerNameOr(text)` joins handler names with `", "` for button tooltips (falls back to the given text when no handlers are registered).
12. (both) `IDataReceiver.receive(ValueInput)`: the single entry point for `DataPacket` payloads (network.md). Server implementations are terminal menus; client implementations are terminal screens.

## Loader differences (Fabric)

Identical SH classes; on Fabric, `update` must run inside a packet context (step 1) because the send happens from a packet handler, and `makeRegByteBuf` uses the two-arg `RegistryFriendlyByteBuf`.

## Deviations in the new mod

None yet.
