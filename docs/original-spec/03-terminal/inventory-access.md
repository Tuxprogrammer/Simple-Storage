# Inventory access layer (IInventoryAccess, MultiInventoryAccess, NetworkInventory)

| # | Field | Contents |
|---|---|---|
| 1 | **Identity** | `IInventoryAccess` (SH `inventory/IInventoryAccess.java`, incl. `IInventoryChangeTracker`, `IMultiThreadedTracker`, `IChangeNotifier`, `Delegate`, `IInventory`), `MultiInventoryAccess` (SH, incl. `MultiChangeTracker`, `TrackerInfo`), `NetworkInventory` (SH), `VanillaMultiblockInventories` (SH). |
| 2 | **Inputs** | Per-inventory platform handlers (01-core `inventory-access.md` — `PlatformInventoryAccess`); the cable network topology (02-storage `cable-network.md`); the config flag `runMultithreaded` (01-core `config.md`). |
| 3 | **Numbers** | `NetworkInventory` network rescan cooldown: 10 game ticks. Change-tracker values: per-inventory opaque `long` (platform-provided) + `System.nanoTime()` as the aggregate "last change" timestamp. `MultiInventoryAccess` slot counts: summed over connected inventories. `pullMatchingStack`/`pushStack` amounts: `long` (pull) / ItemStack counts (push). |
| 4 | **Behavior** | See Behavior detail. |
| 5 | **Determinism** | Iteration order over `connected` = priority order (lowest priority first — see build). Pull/push results depend on that order and on per-inventory slot order. Multithreaded path: per-inventory prep/finish is ordered, the middle `processOffThread` runs on the parallel-stream pool (order-irrelevant by design — each inventory is processed independently). |
| 6 | **Data formats** | No NBT of its own. `IInventoryChangeTracker` contract: `getChangeTracker(level)` long; `streamWrappedStacks(parallel)` → `Stream<StoredItemStack>`; `countItems(filter)` long; `findSlot(filter, findEmpty)`, `findSlotAfter(slot, filter, findEmpty, loop)`, `findSlotDest(forStack)`, `findSlotDestAfter(slot, forStack, loop)` → `InventorySlot` (01-core `inventory-access.md`). `Delegate` wraps one tracker (used by platform adapters to forward). |
| 7 | **Output** | Pushed/pulled `ItemStack`s; free/slot totals; slot handles for extraction; change notifications. |
| 8 | **Relationships** | Used by: `StorageTerminalBlockEntity.pullStack/pushStack` (03-terminal `terminal-blocks.md`), `InventoryInterfaceBlockEntity`, hoppers, `RemoteConnections` (02-storage), `InventoryCableNetwork` merge handlers (02-storage `cable-network.md`). `getRootHandler(dejaVu)` — proxy-chain resolution with cycle guard (`IProxy`, 02-storage `cable-network.md`). `MultiInventoryAccess.build` is implemented by `InventoryCableNetwork`'s merged handler (02-storage `cable-network.md`). |
| 9 | **NeoForge dependencies** | `Level.getGameTime`, parallel streams (ForkJoinPool), `IdentityHashMap`-backed proxy set; `WorldStates.trackers` (01-core `platform-layer.md` — global map from platform handler → tracker). No 1.7.10 counterpart for the tracker abstraction itself (port concept, not API). |
| 10 | **Bugs** | `TERM-12` (`MultiChangeTracker.getChangeTracker` allocates/reallocates `trackers`/`items` arrays only when the game tick changes; if `connected` grows on the same tick after the first call, the multithread path indexes `trackers[i]` with a stale, shorter array — `ArrayIndexOutOfBounds`; observed path: `build()` on a network change landing in the same tick as a terminal refresh). |

## Behavior detail

### IInventoryAccess (server)

1. `pullMatchingStack(st, max)` (default): loop `tracker().findSlotAfter(slot, equalItemPredicate, findEmpty=false, loop=false)`; extract `max − extracted` from each found slot until the quota is met; concatenate into one stack. Returns EMPTY when nothing.
2. `pushStack(stack)` (default): loop `findSlotDestAfter(slot, stored, loop=false)`; `slot.insert(stack)` until empty; return the remainder.
3. `getFreeSlotCount`/`getSlotCount`: abstract. `getPlatformHandler()`: the platform handler object or `PlatformInventoryAccess.EMPTY`.
4. `IMultiThreadedTracker.prepForOffThread(level)` → snapshot; `processOffThread(snapshot)` → per-inventory result; `finishOffThreadProcess(level, result)` → new tracker value (runs on the main thread).

### MultiInventoryAccess (server)

5. `build(self, connectors)` (called on network rebuild): BFS the connector graph (self + all connected connectors, deduplicated); collect their connected inventories, deduplicated by platform-handler identity; group by priority (`IPriority.GETTER`); then iterate priorities **highest index first** (`Priority.VALUES.length−1 → 0`) — i.e. lowest-priority inventories first — resolving each through `getRootHandler(dejaVu)` and skipping roots that are `MultiInventoryAccess` (prevents double-counting sub-networks); add each unique root's access to `connected`; `refresh()`.
6. `pullMatchingStack`: iterate `connected` in build order, pulling `max − pulled` from each until filled; result count = total pulled.
7. `pushStack`: iterate `connected` in order, pushing until the stack is empty; return EMPTY when fully inserted, else the remainder.
8. `MultiChangeTracker.getChangeTracker(level)`: only re-evaluated when `level.getGameTime() != lastUpdate`. On first evaluation after a rescan (array size mismatch): snapshot all per-inventory tracker values, return `System.nanoTime()`. Otherwise: if `Config.runMultithreaded` — `multithreadProcessing`: for each connected inventory, if it is an `IMultiThreadedTracker` with non-null prep, queue a `TrackerInfo`; else read its tracker value; then `infos.parallelStream().unordered().peek(run).toArray()` (off-thread processing), then per info `finish` on the main thread; any change invalidates that inventory's cached item list (`items[i] = null`) and sets `ch`. Non-mt path: sequential value compare. Any change → `lastChange = System.nanoTime()`. **`TERM-12`**: the `trackers[i]` indexing assumes the array matches `connected.size()`; a same-tick growth of `connected` (build before the first read) is not caught because the size check happens before the mt path.
9. `streamWrappedStacks(parallel)`: per inventory, lazily collect `streamWrappedStacks(false)` into a cached `ItemList` (rebuilt when invalidated); flat-map; the parallel variant uses parallel streams + `unordered()` — item order is not stable in parallel mode (grouping downstream makes this fine — 03-terminal `terminal-blocks.md` step 9).
10. `countItems(filter)`: sum over connected. `findSlot*`/`findSlotDest*`: first hit across connected (build order).
11. `getRootHandler(dejaVu)` returns `this` (a multi-access is its own root).

### NetworkInventory (server)

12. Holds a `BlockInventoryAccess` for the block directly on the terminal/connector face (`onLoad(level, pos, side, v)`).
13. `getAccess(level, pos)`: if the direct block has a handler: return it (or its root if the handler is a `PlatformItemHandler`); else if more than 10 ticks since the last scan: `scanNetwork` — walk the cable-network nodes at the position, find the first loaded `IInventoryConnector` block entity with connected inventories, hold it as a weak ref; if a live network connector is found: return `net.getMergedHandler()`; else return the (empty) block access.
14. `isNetwork`: no direct block but a network is attached. `isValid`: either.

### VanillaMultiblockInventories (server)

15. `checkChest(level, pos, state, extra)`: if the state is a double chest (type ≠ SINGLE), compute the paired position from the chest type direction; if the neighbor is a chest of a *different* non-SINGLE type with the *same* facing, report the neighbor via `extra` (so a multiblock inventory is seen as one unit — used by platform inventory discovery; 01-core `inventory-access.md`).

## Deviations in the new mod

See the [03-terminal deviation table](../../new-spec/03-terminal/deviations.md#deviation-table) (row `inventory-access`).
