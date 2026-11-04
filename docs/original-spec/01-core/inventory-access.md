# Inventory access layer (NF adapters)

| # | Field | Contents |
|---|---|---|
| 1 | **Identity** | NF adapter classes over the SH inventory-access abstractions (the SH interfaces `IInventoryAccess`, `IInventoryAccess.IInventoryChangeTracker`, `IProxy`, `MultiInventoryAccess`, `StoredItemStack`, `TerminalItemStack`, `ItemPredicate`, `BlockFilter`, `InventoryCableNetwork` live in `NeoForge/src/platform-shared/java/com/tom/storagemod/inventory/` — 03-terminal territory, cross-linked here): `PlatformInventoryAccess` (+ inner `BlockInventoryAccess`, `EMPTY`), `PlatformItemHandler`, `PlatformProxyInventoryAccess`, `PlatformMultiInventoryAccess`, `PlatformFilteredInventoryAccess`, `EmptyInventory`, `InventoryChangeTracker`, `InventorySlot`, `NeoStack`, `PolyFilter` (NF implementation of the SH `ItemFilter` interface). |
| 2 | **Inputs** | Block capabilities at a position/side (`Capabilities.Item.BLOCK`), `BlockCapabilityCache` validity, a filter (`IFilter`), connected inventory list (multi access), world game time (change tracking, PolyFilter refresh). |
| 3 | **Numbers** | `PolyFilter` state refresh interval: 10 ticks (game time). (NF PolyFilter.java:27-31) `InventoryChangeTracker` change stamp: `System.nanoTime()` (monotonic, not tick-based). (NF InventoryChangeTracker.java:41,137,152) `PlatformMultiInventoryAccess` uses binary search over an offsets table. (NF PlatformMultiInventoryAccess.java:20-26) |
| 4 | **Behavior** | Ordered steps in [Behavior detail](#behavior-detail). |
| 5 | **Determinism** | Change tracking depends on world game time (tick order) and `System.nanoTime()` for the change stamp value (the stamp only needs to change, its value is compared, not interpreted). `PlatformMultiInventoryAccess.refresh()` order = order of the connected list. `PolyFilter` cache refresh is time-gated (every 10 ticks of game time). |
| 6 | **Data formats** | None of its own (no NBT). The NF transfer API types: `ResourceHandler<ItemResource>`, `ItemResource`, `TransactionContext`, `Transaction`. |
| 7 | **Output** | Inventory access objects used by the cable network, terminal (03-terminal), connector (02-storage); change-tracker streams of `StoredItemStack`; `InventorySlot` handles for item moves. |
| 8 | **Relationships** | Used by `NetworkInventory`/cable network (03-terminal), `InventoryConnectorBlockEntity` (02-storage), `StorageTerminalBlockEntity` (03-terminal), `PlatformFilteredInventoryAccess` is wrapped by `BlockFilter` (03-terminal). `WorldStates.getTracker` (util-misc.md) creates `InventoryChangeTracker` instances. |
| 9 | **NeoForge dependencies** | `BlockCapabilityCache` (capability caching with validity predicate + invalidation callback); `Capabilities.Item.BLOCK`; `ResourceHandler<ItemResource>`/`ItemResource`/`Transaction`/`TransactionContext`/`DelegatingResourceHandler` (NeoForge transfer API — replaces 1.7.10 item handlers); `IProxy`/`getRootHandler` proxy-chain resolution for capability cycles. |
| 10 | **Bugs** | `CORE-03` (multi-access compaction index mismatch), `CORE-04` (filtered extract negative amount). |

## Behavior detail

### PlatformInventoryAccess

1. (both) `PlatformInventoryAccess.EMPTY`: a shared empty access (NF PlatformInventoryAccess.java:25-56) — `tracker()` returns `InventoryChangeTracker.NULL`, `get()` returns `EmptyInventory.INSTANCE`, `pullMatchingStack` returns empty, `pushStack` returns the input unchanged (i.e. "nothing was pushed"), `getFreeSlotCount` = 0, `getRootHandler` returns itself.
2. (server) `BlockInventoryAccess.onLoad(level, pos, side, isValid)`: (NF PlatformInventoryAccess.java:63-66) sets `valid=true` and creates a `BlockCapabilityCache` for `Capabilities.Item.BLOCK` at `(pos, side)` on the server level, with validity predicate `valid && isValid.isObjectValid()` (the BE must still exist and be in the same state) and invalidation callback `onInvalid()` (empty hook). All capability reads go through the cache, so other mods' capability invalidation is respected.
3. (server) `BlockInventoryAccess.tracker()`: (NF PlatformInventoryAccess.java:71-79) if the cached capability is non-null, delegates to `WorldStates.getTracker(handler)` (shared tracker per handler, util-misc.md); else returns the NULL tracker. The returned `IInventoryChangeTracker.Delegate` wraps the delegate so a capability change swaps the underlying tracker.
4. (both) `BlockInventoryAccess.get()`: returns the cached capability, or null if never loaded/invalidated. `exists()`: valid and capability non-null. (NF PlatformInventoryAccess.java:81-97) `markInvalid()`: sets `valid=false`. (NF PlatformInventoryAccess.java:90-93)
5. (both) `BlockInventoryAccess.hasInventoryAt(level, pos, state, direction)` (static): (NF PlatformInventoryAccess.java:86-88) true if `level.getCapability(Capabilities.Item.BLOCK, pos, state, null, direction)` is non-null (direct, uncached lookup).
6. (both) `getRootHandler(dejaVu)`: if the underlying handler is an `IProxy` (NF PlatformInventoryAccess.java:99-109), add it to `dejaVu`; if added, recurse into `p.getRootHandler(dejaVu)` (follows the proxy chain to the real root); if already in the set (cycle), return `this`. Non-proxy: return `this`.
7. (both) `PlatformInventoryAccess.getBlockFilterAt(level, pos, make)` (static): (NF PlatformInventoryAccess.java:138-142; removeBlockFilterAt 144-150) returns the `BlockFilter` from the `block_filter` attachment on the BE at pos, or null (if `make` is false and no attachment); `removeBlockFilterAt(level, pos)` (static): drops the filter's contents at pos and removes the attachment.
8. (both) Default methods `getFreeSlotCount`/`getSlotCount`: (NF PlatformInventoryAccess.java:120-136) iterate the platform handler, count empty slots / report size (0 when no handler).

### PlatformProxyInventoryAccess

9. (both) Wraps an `IInventoryAccess` and also *is* a `ResourceHandler<ItemResource>`: every handler method (`isValid`, `insert` x2, `extract` x2, `getResource`, `getAmountAsLong`, `size`, `getCapacityAsLong`, `tracker`) re-enters the delegate through the wrapped access with a re-entrancy guard (`calling` flag): if the delegate calls back into the proxy mid-operation, the proxy answers conservatively (false / 0 / empty / NULL tracker) instead of recursing. `get()` returns the proxy itself; `getRootHandler(dejaVu)` adds the *wrapped access* to the set and delegates root resolution to it.

### PlatformMultiInventoryAccess

10. (both) Extends the SH `MultiInventoryAccess` (holds `connected`, a list of `IInventoryAccess`) and is itself a `ResourceHandler<ItemResource>`. Layout: a flat slot space; `offsets[i]` (NF PlatformMultiInventoryAccess.java:11-12,67-83) = flat slot offset of connected inventory i; `invSize` = total. `refresh()`: reallocates `offsets` if `connected` size changed, then walks inventories in order; inventories whose handler reports size 0 are skipped and the remaining offsets are compacted toward index 0 (`hOff` = number skipped so far); `offsetsSize = offsets.length - hOff`.
11. (both) `findInventory(slot)`: binary search over `offsets[0..offsetsSize)`; (NF PlatformMultiInventoryAccess.java:20-26) on a miss `Arrays.binarySearch` returns `-insertionPoint-1` and the code maps it to `-arrayIndex-2`, the floor entry (the previous offset, i.e. the inventory whose range contains `slot`) (PlatformMultiInventoryAccess.java:22-24). All slot operations (`isValid`, `getResource`, `getAmountAsLong`, `insert`, `extract`, `getCapacityAsLong`) resolve the owning inventory via `findInventory` and forward to `getHandler(arrayIndex, invSlot)`; out-of-range slots return false/empty/0. `insert`/`extract` validate `TransferPreconditions.checkNonEmptyNonNegative(resource, amount)` first. Re-entrancy guard like the proxy (`calling`).
12. (both) `getHandler(i, invSlot)`: returns the platform handler of connected inventory i (NF PlatformMultiInventoryAccess.java:85-91), or `EmptyInventory.INSTANCE` if the handler is null or `invSlot` is beyond its size. `clear()` also resets `invSize` to 0. (NF PlatformMultiInventoryAccess.java:61-65)

### PlatformFilteredInventoryAccess

13. (both) Wraps an access `acc` with an `IFilter` (NF PlatformFilteredInventoryAccess.java:15-23,60-69) (SH, 03-terminal): implements `IInventoryAccess` and `ResourceHandler<ItemResource>`. `pushStack`/`pullMatchingStack` test the stack against the filter first (a non-matching stack is returned unchanged / not pulled). `getResource`/`getAmountAsLong`/`insert`/`extract`/`isValid` all test through `filter.getItemPred().test(new StoredItemStack(stack, 1))`. **keepLast filters** (`filter.isKeepLast()`): the last item of a matching stack is protected — `getAmountAsLong` reports count-1, `extract` caps the amount at count-1, `getCapacityAsLong` reports capacity-1. `getPriority()` = the wrapped priority + the filter's priority (`Priority.add`, util-misc.md). `getFreeSlotCount`/`getSlotCount` pass through to `acc` (unfiltered). `getRootHandler` delegates to `acc`.
14. (server) Its `InventoryChangeTracker` subclass (step 19) overrides `checkFilter` (filter predicate), `getCount` (count-1 for keepLast), `getSlotHandler` (returns the filtered handler itself, so slot operations respect the filter), and calls `filter.getItemPred().updateState()` before off-thread prep and change computation (filter state must be current on the server thread before the stack snapshot is taken).

### EmptyInventory

15. (both) Singleton `EmptyInventory.INSTANCE`: size 0 (NF EmptyInventory.java:8-43), all reads empty/0, `isValid` false, insert/extract return 0.

### InventoryChangeTracker

16. (server) One tracker per `ResourceHandler` (created by `WorldStates.getTracker`, util-misc.md); holds a `WeakReference` to the handler (NF InventoryChangeTracker.java:20-22), a `StoredItemStack[] lastItems` snapshot, `lastUpdate` (last world game time checked) and `lastChange` (nanotime stamp of the last detected change).
17. (server) `getChangeTracker(level)`: only when `lastUpdate != level.getGameTime()` (NF InventoryChangeTracker.java:28-45,63-79) (at most once per tick): for each slot, read the current stack via `NeoStack` and `updateChange(i, stack)`: a slot counts as changed if the item identity (same item + same components, `ItemStack.isSameItemSameComponents`) changed or the counted quantity changed (for filtered trackers, the filter-excluded last item is excluded from the quantity). Any change sets `lastChange = System.nanoTime()`. Returns `lastChange`.
18. (server) `prepForOffThread(level)` (multithreaded mode (NF InventoryChangeTracker.java:115-147), `runMultithreaded` config): returns null if already current this tick; otherwise returns a `NeoStack[]` snapshot of every slot (taken on the server thread) for off-thread processing. `processOffThread(array)` (any thread): runs `updateChange` over the snapshot and returns the new nanotime stamp if anything changed, else the old `lastChange`. `finishOffThreadProcess(level, ct)`: commits the stamp and `lastUpdate`. This is the 3-phase protocol the cable network uses when `runMultithreaded` is true.
19. (server) Query methods (operate on the last snapshot, no live reads): `streamWrappedStacks(parallel)` (NF InventoryChangeTracker.java:81-98) — stream of non-null `StoredItemStack` (used by the terminal to build its item list); `countItems(filter)` — total quantity of stacks equal to the given item (identity match); `findSlot(filter, findEmpty)` — first slot whose stack passes the `ItemPredicate`, or the first empty slot if `findEmpty`; `findSlotDest(forStack)` — first slot that can accept the stack: checks the filter, then for each slot either empty-and-`isValid` or same-item, and probes capacity by trying a transactional insert of the full quantity (rolled back) — returns the first slot where `rem > 0`; `findSlotAfter(slot, filter, findEmpty, loop)` / `findSlotDestAfter(slot, forStack, loop)` — same searches continuing after a given slot (wrapping if `loop` and the slot is the last).
20. (both) `onSlotChanged(slot)` (IChangeNotifier): (NF InventoryChangeTracker.java:149-154) called by `InventorySlot` after a mutation; if the slot id is in range and `updateChange` detects a difference, bumps `lastChange`.

### InventorySlot

21. (both) A handle to (handler, slot id) plus a change notifier. `getStack()`: `handler.getResource(id).toStack(handler.getAmountAsInt(id))`. (NF InventorySlot.java:21-23) `extract(amount)`: transactional extract (NF InventorySlot.java:29-37), notify on non-zero, return the extracted stack. `insert(stack)`: transactional insert of the full stack; returns the remainder (EMPTY if fully inserted). `transferTo(amount, to)`: single transaction — extract `amount` from source, insert into target; if fully inserted, commit and notify both sides; if partially inserted, a second transaction extracts only the inserted amount and retries; otherwise no change (both transactions rolled back).

### NeoStack

22. (both) `record NeoStack(ItemResource resource, long count)` (NF NeoStack.java:6-10); convenience constructor `(handler, slot)` reads the slot once. A one-slot snapshot used by the tracker's off-thread protocol.

### PolyFilter (NF `ItemFilter` implementation)

23. (server) `PolyFilter(face)`: filters an inventory (NF PolyFilter.java:16-24,44-49) by "whatever is currently in the inventory on the given block face" (the polymorphic filter item, 03-terminal/02-storage). `updateState()`: at most every 10 ticks of `face.level().getGameTime()`; clears the cached set and rebuilds it from the capability at `face.pos()` on side `face.from()` — each non-empty slot becomes a `StoredItemStack`, `distinct()` by stack identity, then the stacks are added to the set. `test(stack)`: true if the stack is `isSameItemSameComponents`-equal to any cached stack. `configMatch(stack)`: the stack's item is `Content.polyItemFilter` (the `polymorphic_item_filter` item).

## Loader differences (Fabric)

Fabric has the same class names in `Fabric/src/main/java/com/tom/storagemod/inventory/` over Fabric API types: `ResourceHandler<ItemResource>` is replaced by `Storage<ItemVariant>` (Fabric transfer API), `ItemResource` by `ItemVariant`; `WorldStates` (Fabric) uses a `MapMaker` weak-keys+weak-values map for trackers and calls `InventoryChangeTracker.refresh(handler)` after lookup (Fabric trackers re-read their handler; NF trackers hold the reference weakly); `PlatformInventoryAccess` (Fabric) caches the `Storage<ItemVariant>` capability via Fabric's `ItemStorage.SIDE` lookup. The re-entrancy guards, offsets table, keepLast rules and 10-tick PolyFilter refresh are identical.

## Deviations in the new mod

See the [`inventory-access` row of the new-spec 01-core deviation table](../../new-spec/01-core/deviations.md#deviation-table).
