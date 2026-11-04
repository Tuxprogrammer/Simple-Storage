# Inventory Hopper

## Identity
`AbstractInventoryHopperBlock` (SH), `BasicInventoryHopperBlock` (SH), `AbstractInventoryHopperBlockEntity` (SH), `BasicInventoryHopperBlockEntity` (SH). Moves one item per cycle between the inventory network (or any inventory) on its top/bottom faces, with an optional item filter.

Sources (upstream 26.2@95bb61d): `NeoForge/src/platform-shared/java/com/tom/storagemod/block/{AbstractInventoryHopperBlock,BasicInventoryHopperBlock}.java` and `.../block/entity/{AbstractInventoryHopperBlockEntity,BasicInventoryHopperBlockEntity}.java`, config in `NeoForge/src/main/java/com/tom/storagemod/Config.java`; short names below are `Class.java:line` in those files (class declarations: AbstractInventoryHopperBlock.java:38, BasicInventoryHopperBlock.java:21, AbstractInventoryHopperBlockEntity.java:14, BasicInventoryHopperBlockEntity.java:23).

## Inputs
- Placement: `getStateForPlacement` FACING = clicked face's opposite. FACING points at the **destination** (the "bottom" inventory at `pos.relative(FACING)`); the source ("top") is on the opposite side (AbstractInventoryHopperBlock.java:70-71; BasicInventoryHopperBlockEntity.java:69-71).
- Block state: `FACING` (default DOWN), `ENABLED` (default true) (AbstractInventoryHopperBlock.java:39-40, 44).
- Redstone: `updateState` on place/neighbor change — `ENABLED = !worldIn.hasNeighborSignal(pos)` (any power disables; flag 4) (AbstractInventoryHopperBlock.java:128-131).
- Filter: right-click with an item (server) — `useItemOn`; right-click empty hand: shift clears the filter, otherwise shows the current filter in an overlay (BasicInventoryHopperBlock.java:34, 55-70).
- Config: `basicHopperCooldown` (int, default 10, range 1–200) (Config.java:116).

## Numbers
- Cooldowns (ticks): `baseCd = max(1, basicHopperCooldown)` (10 by default); `midCd = max(1, baseCd * 4 / 10)` (4 by default); `fastCd = max(1, baseCd / 10)` (1 by default) (BasicInventoryHopperBlockEntity.java:75-77).
- Transfer amount: 1 item per successful cycle (BasicInventoryHopperBlockEntity.java:138).

## Behavior
1. (server, every tick) `BasicInventoryHopperBlockEntity.updateServer`:
   1. If filter non-empty but `filterPred == null` → rebuild the predicate via `setFilter(filter)` (BasicInventoryHopperBlockEntity.java:67).
   2. `top` = access at `pos.relative(FACING.getOpposite())` (topCache), `bottom` = access at `pos.relative(FACING)` (bottomCache); caches loaded in `onLoad` (AbstractInventoryHopperBlockEntity.java:23-29; BasicInventoryHopperBlockEntity.java:70-71) (01-core `NetworkInventory` via 03-terminal `inventory-access.md`).
   3. Return if either cache invalid, or if **neither** side is a network (`topCache.isNetwork()` / `bottomCache.isNetwork()`) (BasicInventoryHopperBlockEntity.java:72-74).
   4. Decrement and consume `cooldown` (>0 → return) (BasicInventoryHopperBlockEntity.java:78-79).
   5. `topNet && !hasFilter` → return (a network source must be filtered) (BasicInventoryHopperBlockEntity.java:82-83; STOR-09).
   6. `!isEnabled()` → return (BasicInventoryHopperBlockEntity.java:84).
   7. Top change tracker: if changed → `waiting = 0`, `topSlot = null`; else `cooldown = midCd`. If `waiting == 1` → return (BasicInventoryHopperBlockEntity.java:86-93).
   8. Bottom change tracker: if changed → `waiting = 0`; else `cooldown = midCd`. If `waiting == 2` → return (see STOR-08: `waiting` is never set to 2) (BasicInventoryHopperBlockEntity.java:95-101).
   9. Find source slot: `topSlot = tracker.findSlotAfter(topSlot, hasFilter ? filterPred : always-true, false, true)` (re-scan only when the slot was null or `waiting == 3`) (BasicInventoryHopperBlockEntity.java:103-106).
   10. No source slot: `waiting = 1` + `cooldown = baseCd` if it was already null before, else `cooldown = midCd`; return (BasicInventoryHopperBlockEntity.java:108-115).
   11. Source stack empty → `waiting = 3`, `cooldown = fastCd`, return (BasicInventoryHopperBlockEntity.java:118-123).
   12. Filter mismatch → `waiting = 3`, `cooldown = fastCd`, return (BasicInventoryHopperBlockEntity.java:124-129).
   13. Destination: `bottomSlot = bottomTracker.findSlotDest(storedStack)`; null → `waiting = 3`, `cooldown = baseCd`, return (BasicInventoryHopperBlockEntity.java:131-136).
   14. `topSlot.transferTo(1, bottomSlot)`: success → `cooldown = baseCd`; failure → `waiting = 3`, `cooldown = baseCd` (BasicInventoryHopperBlockEntity.java:138-143).
2. `setFilter(stack)`: empty → null predicate; `IItemFilter` item → `item.createFilter(touching face ref, stack)` (dynamic filter from 01-core/03); else → same-item+components predicate (`ItemStack.isSameItemSameComponents`). Resets `waiting = 0`, `setChanged()` (BasicInventoryHopperBlockEntity.java:49-57).
3. `useItemOn` (server): if the current filter is an `IItemFilter` item, drop it (`popResource`); set the held item's copy as the new filter; if the held item is an `IItemFilter`, consume it; overlay `tooltip.toms_storage.filter_item` = "Filter: %s" (BasicInventoryHopperBlock.java:34-52).
4. `useWithoutItem` (server): shift → give filter item back (if `IItemFilter`) and clear; else show current filter (or `tooltip.toms_storage.empty` = "Empty") (BasicInventoryHopperBlock.java:55-70).
5. `preRemoveSideEffects`: if the filter is an `IItemFilter` item, drop it as an entity (BasicInventoryHopperBlockEntity.java:147-149).
6. `clearContent()`: clears the filter (BasicInventoryHopperBlockEntity.java:153-154).
7. Block: `canConnectFrom` = same axis as FACING (AbstractInventoryHopperBlock.java:90-91); `nextScan` = empty list (the hopper's own caches do the scanning) (AbstractInventoryHopperBlock.java:85-86); collision shape = two boxes per facing (same as the level emitter) (AbstractInventoryHopperBlock.java:96-108).

## Determinism
Deterministic; `findSlotAfter` iteration order is slot order (01-core `IInventoryChangeTracker`).

## Data formats
NBT (BE): `Filter` — optional item stack (the actual held filter item, including `IItemFilter` items) (BasicInventoryHopperBlockEntity.java:38-46).

## Output
Item movement bottom→top; filter item dropped on block removal.

## Relationships
- 03-terminal: `NetworkInventory` (both caches), `IInventoryAccess.IInventoryChangeTracker` (`findSlotAfter`, `findSlotDest`, `transferTo`), `StoredItemStack`, `ItemPredicate` (filter system in 03-terminal `filters.md`), `IItemFilter` items (03-terminal `item-filter-gui.md` components).
- 01-core: `Config.basicHopperCooldown`, `TickerUtil`, `BlockFaceReference`, `IValidInfo`.

## NeoForge dependencies
Codec, `BlockStateProperties.ENABLED`, `TickerUtil` (platform ticker), `Containers.dropItemStack`, `Clearable`.

## Bugs
STOR-08 (dead `waiting == 2` branch, `BasicInventoryHopperBlockEntity.java:101`); STOR-09 (unfiltered network-top hopper re-checks every tick, `BasicInventoryHopperBlockEntity.java:83`).

## Deviations in the new mod

See [new-spec 02-storage](../../new-spec/02-storage/index.md) (row: hopper).
