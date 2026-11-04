# Open Crate

## Identity
`com.tom.storagemod.block.OpenCrateBlock` (SH) + `OpenCrateBlockEntity` (NF — NeoForge-side implementation). Converts the block in front of it into a virtual inventory of its item entities.

## Inputs
- Placement: FACING = opposite of the nearest looking direction (full-direction property, default DOWN).
- Server tick (staggered): `updateServer` runs the scan only when `getGameTime() % 5 == Math.abs(worldPosition.hashCode()) % 5` (1 in 5 ticks).

## Numbers
- Rescan period: 5 ticks, staggered by position hash.
- Scanned AABB: the full 1×1×1 block in the FACING direction.
- `getContainerSize() = items.size() + 1` (the +1 is a phantom "append" slot).
- `getMaxStackSize() = 64`.

## Behavior
1. (server, staggered tick) Rescan: `items = level.getEntitiesOfClass(ItemEntity, AABB(front block))` — full replacement, order = entity iteration order.
2. `getItem(index)`: index < items.size() → the entity's item stack; else EMPTY.
3. `canPlaceItem(index, stack)`: true only for `index == items.size()` (the phantom append slot).
4. `removeItem(index, count)`: `ent.getItem().split(count)` — splits the entity's stack, returns the split-off part; the entity keeps the rest.
5. `removeItemNoUpdate(index)`: removes the entire entity from the world.
6. `setItem(index, stack)`: creates a **new ItemEntity** at the center of the front block (default pickup delay, zero delta movement), adds it to the level and to the local `items` list (list is replaced wholesale on the next rescan).
7. `stillValid(player)` = true; `clearContent()` = no-op.

## Determinism
Non-deterministic order: the item order follows `getEntitiesOfClass` iteration, which depends on entity ID order.

## Data formats
No NBT of its own (item entities are the storage).

## Output
A virtual `Container` (implemented via `IInventoryAccess` adapter in 01-core `inventory-access.md`) exposing the front block's item entities; the phantom append slot is the push path.

## Relationships
- 01-core: `PlatformInventoryAccess` adapter, `TickerUtil` (staggered ticker).
- 03-terminal: `NetworkInventory`/`MultiInventoryAccess` see it as one inventory.

## NeoForge dependencies
`TickerUtil.createTicker` (shared, in 01-core); nothing NeoForge-specific beyond platform BE base.

## Bugs
STOR-01 (Math.abs scan gate, `OpenCrateBlockEntity.java:31`); STOR-04 (phantom append slot, `OpenCrateBlockEntity.java:40-41` + `:108-109`).

## Deviations in the new mod

See [new-spec 02-storage](../../new-spec/02-storage/index.md) (row: open-crate).
