# Inventory access layer (IInventoryAccess, MultiInventoryAccess, NetworkInventory)

| # | Field | Contents |
|---|---|---|
| 1 | **Identity** | `IInventoryAccess` (SH `inventory/IInventoryAccess.java`, incl. `IInventoryChangeTracker`, `IMultiThreadedTracker`, `IChangeNotifier`, `Delegate`, `IInventory`), `MultiInventoryAccess` (SH, incl. `MultiChangeTracker`, `TrackerInfo`), `NetworkInventory` (SH), `VanillaMultiblockInventories` (SH). Sources: `SH` = `NeoForge/src/platform-shared/java/com/tom/storagemod/` on branch `26.2`; short refs are `inventory/IInventoryAccess.java:NN` (IA), `inventory/MultiInventoryAccess.java:NN` (MA), `inventory/NetworkInventory.java:NN` (NI). (IA:11, MA:27, NI:18) |
| 2 | **Inputs** | Per-inventory platform handlers (01-core `inventory-access.md` — `PlatformInventoryAccess`); the cable network topology (02-storage `cable-network.md`); the config flag `runMultithreaded` (01-core `config.md`) (MA:206). |
| 3 | **Numbers** | `NetworkInventory` network rescan cooldown: 10 game ticks (NI:50; strictly more than 10 ticks since the last scan). Change-tracker values: per-inventory opaque `long` (platform-provided) + `System.nanoTime()` as the aggregate "last change" timestamp (MA:202, 219). `MultiInventoryAccess` slot counts: summed over connected inventories. `pullMatchingStack`/`pushStack` amounts: `long` (pull) / ItemStack counts (push) (IA:13, 29). |
| 4 | **Behavior** | See Behavior detail. |
| 5 | **Determinism** | Iteration order over `connected` = priority order (lowest priority first — see build) (MA:53). Pull/push results depend on that order and on per-inventory slot order. Multithreaded path: per-inventory prep/finish is ordered, the middle `processOffThread` runs on the parallel-stream pool (order-irrelevant by design — each inventory is processed independently). |
| 6 | **Data formats** | No NBT of its own. `IInventoryChangeTracker` contract: `getChangeTracker(level)` long; `streamWrappedStacks(parallel)` → `Stream<StoredItemStack>`; `countItems(filter)` long; `findSlot(filter, findEmpty)`, `findSlotAfter(slot, filter, findEmpty, loop)`, `findSlotDest(forStack)`, `findSlotDestAfter(slot, forStack, loop)` → `InventorySlot` (01-core `inventory-access.md`). `Delegate` wraps one tracker (used by platform adapters to forward) (IA:77-138). Method signatures: IA:67-75. |
| 7 | **Output** | Pushed/pulled `ItemStack`s; free/slot totals; slot handles for extraction; change notifications. |
| 8 | **Relationships** | Used by: `StorageTerminalBlockEntity.pullStack/pushStack` (03-terminal `terminal-blocks.md`), `InventoryInterfaceBlockEntity`, hoppers, `RemoteConnections` (02-storage), `InventoryCableNetwork` merge handlers (02-storage `cable-network.md`). `getRootHandler(dejaVu)` — proxy-chain resolution with cycle guard (`IProxy`, 02-storage `cable-network.md`). `MultiInventoryAccess.build` is implemented by `InventoryCableNetwork`'s merged handler (02-storage `cable-network.md`). |
| 9 | **NeoForge dependencies** | `Level.getGameTime`, parallel streams (ForkJoinPool), `IdentityHashMap`-backed proxy set; `WorldStates.trackers` (01-core `platform-layer.md` — global map from platform handler → tracker). No 1.7.10 counterpart for the tracker abstraction itself (port concept, not API). |
| 10 | **Bugs** | `TERM-12` (`MultiChangeTracker.getChangeTracker` allocates/reallocates `trackers`/`items` arrays only when the game tick changes; if `connected` grows on the same tick after the first call, the multithread path indexes `trackers[i]` with a stale, shorter array — `ArrayIndexOutOfBounds` (UNVERIFIED: the size check at MA:195 runs inside the same `lastUpdate != gameTime` branch before the mt path, so same-tick growth is not re-read at all; the stale-array risk in code is `streamWrappedStacks` indexing `connected.get(i)` over `items.length` after a same-tick shrink, MA:227-229); observed path: `build()` on a network change landing in the same tick as a terminal refresh). |

## Behavior detail

### IInventoryAccess (server)

1. `pullMatchingStack(st, max)` (default): loop `tracker().findSlotAfter(slot, equalItemPredicate, findEmpty=false, loop=false)` (IA:20); extract `max − extracted` from each found slot until the quota is met (IA:21-24); concatenate into one stack (IA:22-23). Returns EMPTY when nothing (IA:26).
2. `pushStack(stack)` (default): loop `findSlotDestAfter(slot, stored, loop=false)` (IA:33); `slot.insert(stack)` until empty (IA:34); return the remainder (IA:37).
3. `getFreeSlotCount`/`getSlotCount`: abstract. `getPlatformHandler()`: the platform handler object or `PlatformInventoryAccess.EMPTY` (IA:40-41, 50).
4. `IMultiThreadedTracker.prepForOffThread(level)` → snapshot; `processOffThread(snapshot)` → per-inventory result; `finishOffThreadProcess(level, result)` → new tracker value (runs on the main thread) (IA:57-61; run order MA:177-180).

### MultiInventoryAccess (server)

5. `build(self, connectors)` (MA:35; called on network rebuild): BFS the connector graph (self + all connected connectors, deduplicated) (MA:37-46); collect their connected inventories, deduplicated by platform-handler identity (MA:47-48); group by priority (`IPriority.GETTER`) (MA:49); then iterate priorities **highest index first** (`Priority.VALUES.length−1 → 0`) (MA:53) — i.e. lowest-priority inventories first — resolving each through `getRootHandler(dejaVu)` and skipping roots that are `MultiInventoryAccess` (prevents double-counting sub-networks) (MA:55-56); add each unique root's access to `connected` (MA:57-58); `refresh()` (MA:61).
6. `pullMatchingStack`: iterate `connected` in build order, pulling `max − pulled` from each until filled; result count = total pulled (MA:65-77).
7. `pushStack`: iterate `connected` in order, pushing until the stack is empty; return EMPTY when fully inserted, else the remainder (MA:80-86).
8. `MultiChangeTracker.getChangeTracker(level)`: only re-evaluated when `level.getGameTime() != lastUpdate` (MA:194). On first evaluation after a rescan (array size mismatch): snapshot all per-inventory tracker values, return `System.nanoTime()` (MA:195-203). Otherwise: if `Config.runMultithreaded` (MA:206) — `multithreadProcessing` (MA:157-190): for each connected inventory, if it is an `IMultiThreadedTracker` with non-null prep, queue a `TrackerInfo`; else read its tracker value; then `infos.parallelStream().unordered().peek(run).toArray()` (off-thread processing) (MA:177), then per info `finish` on the main thread; any change invalidates that inventory's cached item list (`items[i] = null`) and sets `ch` (MA:171-174, 183-186). Non-mt path: sequential value compare (MA:209-217). Any change → `lastChange = System.nanoTime()` (MA:219). **`TERM-12`**: the `trackers[i]` indexing assumes the array matches `connected.size()`; a same-tick growth of `connected` (build before the first read) is not caught because the size check happens before the mt path.
9. `streamWrappedStacks(parallel)`: per inventory, lazily collect `streamWrappedStacks(false)` into a cached `ItemList` (rebuilt when invalidated) (MA:227-231); flat-map; the parallel variant uses parallel streams + `unordered()` (MA:233) — item order is not stable in parallel mode (grouping downstream makes this fine — 03-terminal `terminal-blocks.md` step 9).
10. `countItems(filter)`: sum over connected. `findSlot*`/`findSlotDest*`: first hit across connected (build order) (MA:248-285).
11. `getRootHandler(dejaVu)` returns `this` (a multi-access is its own root) (MA:123-125).

### NetworkInventory (server)

12. Holds a `BlockInventoryAccess` for the block directly on the terminal/connector face (`onLoad(level, pos, side, v)`) (NI:23-25).
13. `getAccess(level, pos)`: if the direct block has a handler: return it (or its root if the handler is a `PlatformItemHandler`) (NI:43-48); else if more than 10 ticks since the last scan (NI:50-53): `scanNetwork` (NI:27) — walk the cable-network nodes at the position (NI:29), find the first loaded `IInventoryConnector` block entity with connected inventories, hold it as a weak ref (NI:35-37); if a live network connector is found: return `net.getMergedHandler()` (NI:54-57); else return the (empty) block access (NI:58).
14. `isNetwork`: no direct block but a network is attached (NI:61-63). `isValid`: either (NI:65-67).

### VanillaMultiblockInventories (server)

15. `checkChest(level, pos, state, extra)`: if the state is a double chest (type ≠ SINGLE), compute the paired position from the chest type direction; if the neighbor is a chest of a *different* non-SINGLE type with the *same* facing, report the neighbor via `extra` (so a multiblock inventory is seen as one unit — used by platform inventory discovery; 01-core `inventory-access.md`).

## Deviations in the new mod

See the [03-terminal deviation table](../../new-spec/03-terminal/deviations.md#deviation-table) (row `inventory-access`).
