# Inventory Cables

## Identity
`InventoryCableBlock` (SH, extends `PipeBlock`), `FramedInventoryCableBlock` (SH, `BaseEntityBlock`), `IInventoryCable` (SH), `IInventoryNode` (SH). The network wiring that links functional nodes (connectors, emitters, interfaces, hoppers).

## Inputs
- Connection state: six boolean properties (UP/DOWN/NORTH/SOUTH/EAST/WEST), all default false.
- `InventoryCableBlock` is also waterlogged (`WATERLOGGED`, default false) and a `SimpleWaterloggedBlock`.
- Neighbor changes drive `updateShape` (connection booleans) and network invalidation.

## Numbers
- Pipe collision apothem: `4f` (cable), `4f` (framed highlight shapes via `makeShapes(4f)`).
- `InventoryCableBlock` constructor: `super(4f, pr)`.
- Water tick delay: vanilla `Fluids.WATER.getTickDelay(level)`.
- Framed cable highlight color: `0xFF0000` (red).

## Behavior
1. `IInventoryCable` (interface):
   - `canConnectFrom(state, dir)` default true.
   - `nextScan(world, state, pos)` default: all 6 neighbors as `BlockFace(pos.relative(d), d.getOpposite())`.
   - `isFunctionalNode()` default true.
   - static `canConnect(block, dir)`: true iff the neighbor block is an `IInventoryCable` and `canConnectFrom(neighborState, dir.getOpposite())`.
2. `InventoryCableBlock` (plain cable, `PipeBlock` subclass):
   - `isFunctionalNode() = false` (cables are pass-through; they are not network endpoints).
   - `nextScan`: only directions whose connection boolean is set.
   - `updateShape`: sets the connection boolean for the changed face to `canConnect(neighbor, dir)`; if waterlogged, schedules a water tick.
   - `getStateForPlacement`: `withConnectionProperties` + waterlogged flag.
   - `rotate`/`mirror`: remap the 4 horizontal booleans.
   - `destroy` / `neighborChanged` / `onNeighborChange` (server): `InventoryCableNetwork.getNetwork(level).markNodeInvalid(pos)` for the cable and its (orientation-specific or all) neighbors.
3. `FramedInventoryCableBlock` (framed variant, `BaseEntityBlock`, `IPaintable`, `IConfiguratorHighlight`):
   - Same connection semantics as the plain cable; `isFunctionalNode() = false`; `nextScan` only set-directions.
   - `updateShape`: sets the face boolean to `canConnect(neighbor, dir)` (no water handling).
   - `useItemOn`: `tryScrape` (paint). `paint`: stores facade state on a `PaintedBlockEntity`. `newBlockEntity`: `PaintedBlockEntity`.
   - Highlight: red (`0xFF0000`), per-state cable+arm shapes.
   - Same destroy/neighbor invalidation as the plain cable.

## Determinism
Connection booleans are deterministic from neighbor states. Scan order is a LIFO stack (see `cable-network.md`).

## Data formats
Block state only (booleans). No NBT.

## Output
A graph of connected cable blocks + functional nodes consumed by `InventoryCableNetwork.getNetworkNodes` (see `cable-network.md`).

## Relationships
- `cable-network.md` (`InventoryCableNetwork`, `IInventoryNode`).
- `cable-connector.md`, `inventory-connector.md`, `level-emitter.md`, `inventory-interface.md` — the functional nodes.
- 01-core: `BlockFace`, `TickerUtil`, `ClientUtil.tooltip`.

## NeoForge dependencies
`PipeBlock`, `SimpleWaterloggedBlock`, `BlockStateProperties`, codec (`MapCodec`), `NeoForgeBlock` marker.

## Bugs
None.

## Deviations in the new mod

See [new-spec 02-storage](../../new-spec/02-storage/index.md) (row: cables).
