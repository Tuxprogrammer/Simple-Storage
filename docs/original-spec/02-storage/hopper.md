# Inventory Hopper

## Identity
`AbstractInventoryHopperBlock` (SH), `BasicInventoryHopperBlock` (SH), `AbstractInventoryHopperBlockEntity` (SH), `BasicInventoryHopperBlockEntity` (SH). Moves one item per cycle between the inventory network (or any inventory) on its top/bottom faces, with an optional item filter.

## Inputs
- Placement: `getStateForPlacement` FACING = clicked face's opposite. FACING points at the **destination** (the "bottom" inventory at `pos.relative(FACING)`); the source ("top") is on the opposite side.
- Block state: `FACING` (default DOWN), `ENABLED` (default true).
- Redstone: `updateState` on place/neighbor change — `ENABLED = !worldIn.hasNeighborSignal(pos)` (any power disables; flag 4).
- Filter: right-click with an item (server) — `useItemOn`; right-click empty hand: shift clears the filter, otherwise shows the current filter in an overlay.
- Config: `basicHopperCooldown` (int, default 10, range 1–200).

## Numbers
- Cooldowns (ticks): `baseCd = max(1, basicHopperCooldown)` (10 by default); `midCd = max(1, baseCd * 4 / 10)` (4 by default); `fastCd = max(1, baseCd / 10)` (1 by default).
- Transfer amount: 1 item per successful cycle.

## Behavior
1. (server, every tick) `BasicInventoryHopperBlockEntity.updateServer`:
   1. If filter non-empty but `filterPred == null` → rebuild the predicate via `setFilter(filter)`.
   2. `top` = access at `pos.relative(FACING.getOpposite())` (topCache), `bottom` = access at `pos.relative(FACING)` (bottomCache); caches loaded in `onLoad` (01-core `NetworkInventory` via 03-terminal `inventory-access.md`).
   3. Return if either cache invalid, or if **neither** side is a network (`topCache.isNetwork()` / `bottomCache.isNetwork()`).
   4. Decrement and consume `cooldown` (>0 → return).
   5. `topNet && !hasFilter` → return (a network source must be filtered).
   6. `!isEnabled()` → return.
   7. Top change tracker: if changed → `waiting = 0`, `topSlot = null`; else `cooldown = midCd`. If `waiting == 1` → return.
   8. Bottom change tracker: if changed → `waiting = 0`; else `cooldown = midCd`. If `waiting == 2` → return (see STOR-08: `waiting` is never set to 2).
   9. Find source slot: `topSlot = tracker.findSlotAfter(topSlot, hasFilter ? filterPred : always-true, false, true)` (re-scan only when the slot was null or `waiting == 3`).
   10. No source slot: `waiting = 1` + `cooldown = baseCd` if it was already null before, else `cooldown = midCd`; return.
   11. Source stack empty → `waiting = 3`, `cooldown = fastCd`, return.
   12. Filter mismatch → `waiting = 3`, `cooldown = fastCd`, return.
   13. Destination: `bottomSlot = bottomTracker.findSlotDest(storedStack)`; null → `waiting = 3`, `cooldown = baseCd`, return.
   14. `topSlot.transferTo(1, bottomSlot)`: success → `cooldown = baseCd`; failure → `waiting = 3`, `cooldown = baseCd`.
2. `setFilter(stack)`: empty → null predicate; `IItemFilter` item → `item.createFilter(touching face ref, stack)` (dynamic filter from 01-core/03); else → same-item+components predicate (`ItemStack.isSameItemSameComponents`). Resets `waiting = 0`, `setChanged()`.
3. `useItemOn` (server): if the current filter is an `IItemFilter` item, drop it (`popResource`); set the held item's copy as the new filter; if the held item is an `IItemFilter`, consume it; overlay `tooltip.toms_storage.filter_item` = "Filter: %s".
4. `useWithoutItem` (server): shift → give filter item back (if `IItemFilter`) and clear; else show current filter (or `tooltip.toms_storage.empty` = "Empty").
5. `preRemoveSideEffects`: if the filter is an `IItemFilter` item, drop it as an entity.
6. `clearContent()`: clears the filter.
7. Block: `canConnectFrom` = same axis as FACING; `nextScan` = empty list (the hopper's own caches do the scanning); collision shape = two boxes per facing (same as the level emitter).

## Determinism
Deterministic; `findSlotAfter` iteration order is slot order (01-core `IInventoryChangeTracker`).

## Data formats
NBT (BE): `Filter` — optional item stack (the actual held filter item, including `IItemFilter` items).

## Output
Item movement bottom→top; filter item dropped on block removal.

## Relationships
- 03-terminal: `NetworkInventory` (both caches), `IInventoryAccess.IInventoryChangeTracker` (`findSlotAfter`, `findSlotDest`, `transferTo`), `StoredItemStack`, `ItemPredicate` (filter system in 03-terminal `filters.md`), `IItemFilter` items (03-terminal `item-filter-gui.md` components).
- 01-core: `Config.basicHopperCooldown`, `TickerUtil`, `BlockFaceReference`, `IValidInfo`.

## NeoForge dependencies
Codec, `BlockStateProperties.ENABLED`, `TickerUtil` (platform ticker), `Containers.dropItemStack`, `Clearable`.

## Bugs
STOR-08 (dead `waiting == 2` branch, `BasicInventoryHopperBlockEntity.java:101`); STOR-09 (unfiltered network-top hopper re-checks every tick, `BasicInventoryHopperBlockEntity.java:83`).
