# Inventory Cables

## Identity
`InventoryCableBlock` (SH, extends `PipeBlock`), `FramedInventoryCableBlock` (SH, `BaseEntityBlock`), `IInventoryCable` (SH), `IInventoryNode` (SH). The network wiring that links functional nodes (connectors, emitters, interfaces, hoppers).

Sources (upstream 26.2@95bb61d): `NeoForge/src/platform-shared/java/com/tom/storagemod/block/{InventoryCableBlock,FramedInventoryCableBlock,IInventoryCable,IInventoryNode}.java`; short names below are `Class.java:line` in those files (class declarations: InventoryCableBlock.java:41, FramedInventoryCableBlock.java:52, IInventoryNode.java:3).

## Inputs
- Connection state: six boolean properties (UP/DOWN/NORTH/SOUTH/EAST/WEST), all default false (InventoryCableBlock.java:72).
- `InventoryCableBlock` is also waterlogged (`WATERLOGGED`, default false) and a `SimpleWaterloggedBlock` (InventoryCableBlock.java:41, 48, 61).
- Neighbor changes drive `updateShape` (connection booleans) and network invalidation.

## Numbers
- Pipe collision apothem: `4f` (cable), `4f` (framed highlight shapes via `makeShapes(4f)`) (FramedInventoryCableBlock.java:71).
- `InventoryCableBlock` constructor: `super(4f, pr)` (InventoryCableBlock.java:53).
- Water tick delay: vanilla `Fluids.WATER.getTickDelay(level)` (InventoryCableBlock.java:85).
- Framed cable highlight color: `0xFF0000` (red) (FramedInventoryCableBlock.java:227).

## Behavior
1. `IInventoryCable` (interface):
   - `canConnectFrom(state, dir)` default true (IInventoryCable.java:14).
   - `nextScan(world, state, pos)` default: all 6 neighbors as `BlockFace(pos.relative(d), d.getOpposite())` (IInventoryCable.java:21).
   - `isFunctionalNode()` default true (IInventoryCable.java:29).
   - static `canConnect(block, dir)`: true iff the neighbor block is an `IInventoryCable` and `canConnectFrom(neighborState, dir.getOpposite())` (IInventoryCable.java:33-34).
2. `InventoryCableBlock` (plain cable, `PipeBlock` subclass):
   - `isFunctionalNode() = false` (cables are pass-through; they are not network endpoints) (InventoryCableBlock.java:108-110).
   - `nextScan`: only directions whose connection boolean is set (InventoryCableBlock.java:99).
   - `updateShape`: sets the connection boolean for the changed face to `canConnect(neighbor, dir)`; if waterlogged, schedules a water tick (InventoryCableBlock.java:81-88).
   - `getStateForPlacement`: `withConnectionProperties` + waterlogged flag (InventoryCableBlock.java:92-95).
   - `rotate`/`mirror`: remap the 4 horizontal booleans (InventoryCableBlock.java:130, 148).
   - `destroy` / `neighborChanged` / `onNeighborChange` (server): `InventoryCableNetwork.getNetwork(level).markNodeInvalid(pos)` for the cable and its (orientation-specific or all) neighbors (InventoryCableBlock.java:168-170, 174-186, 193-197).
3. `FramedInventoryCableBlock` (framed variant, `BaseEntityBlock`, `IPaintable`, `IConfiguratorHighlight`):
   - Same connection semantics as the plain cable; `isFunctionalNode() = false`; `nextScan` only set-directions (FramedInventoryCableBlock.java:87, 96).
   - `updateShape`: sets the face boolean to `canConnect(neighbor, dir)` (no water handling) (FramedInventoryCableBlock.java:101-106).
   - `useItemOn`: `tryScrape` (paint). `paint`: stores facade state on a `PaintedBlockEntity`. `newBlockEntity`: `PaintedBlockEntity` (FramedInventoryCableBlock.java:160-177).
   - Highlight: red (`0xFF0000`), per-state cable+arm shapes (FramedInventoryCableBlock.java:227).
   - Same destroy/neighbor invalidation as the plain cable (FramedInventoryCableBlock.java:192-221).

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
