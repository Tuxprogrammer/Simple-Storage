# Inventory Cable Connector

## Identity
`InventoryCableConnectorBlock` (SH, `BaseEntityBlock`) + `FramedInventoryCableConnectorBlock` (SH, extends the former). A cable node with a FACING that attaches to a single inventory face and can open an inventory-link GUI.

## Inputs
- Six connection booleans (default false) + `FACING` enum (default DOWN).
- Placement: `getStateForPlacement` sets FACING = clicked face's opposite.
- Right-click with empty hand: opens the inventory-link menu **only if the BE `hasBeacon()`** (see `cable-network.md` / 03-terminal `inventory-link.md`); otherwise PASS.

## Numbers
- Collision apothem: `0.125f` (small cable body).
- Shape table: `shapes[facingOrdinal][shapeIndex]`, 64 connection masks per facing.
- Beacon-link config (from 01-core `Config`): `invLinkBeaconLvl` (range), `invLinkBeaconLvlSameDim`, `invLinkBeaconLvlCrossDim`; tooltip only shown when `invLinkBeaconLvl != -1`.
- Framed variant: full-block collision (`Shapes.block()`), highlight `0xFFFF00` (yellow).

## Behavior
1. `canConnectFrom(state, dir)`: true iff `dir != FACING` (the facing face is reserved for the attached inventory, not the cable).
2. `nextScan`: all directions except FACING whose connection boolean is set.
3. `updateShape`: for the FACING face, the boolean = `!neighbor.isAir()` (any block attaches); other faces use `canConnect`.
4. `withConnectionProperties`: same as cables, but the FACING face connects to any non-air block.
5. `useWithoutItem` (server): if BE `hasBeacon()` and `stillValid(player)` → `openMenu(be)`; else overlay `chat.toms_storage.inv_link_access_denied` = "Access Denied".
6. Server tick: `TickerUtil.createTicker(world, false, true)` — staggered server-side tick (see `cable-network.md` for the BE's link resolution).
7. Framed variant: adds `IPaintable` + `IConfiguratorHighlight`, paint/scrape, full-block shape.

## Determinism
Deterministic from neighbor states.

## Data formats
Block state only. The BE's link state (channel, beacon) is in `cable-network.md` / 03-terminal.

## Output
A functional network node (a cable endpoint) that binds one inventory face to the cable network; optionally a beacon-gated remote link.

## Relationships
- `cables.md` (IInventoryCable), `cable-network.md`.
- 03-terminal `inventory-link.md` (the GUI opened by right-click).
- 01-core: `Config` (beacon levels), `BlockFace`, `TickerUtil`, `ClientUtil.tooltip`.

## NeoForge dependencies
`BaseEntityBlock`, codec, `BlockStateProperties`, `NeoForgeBlock` marker, `TickerUtil` (platform ticker).

## Bugs
None.

## Deviations in the new mod

See [new-spec 02-storage](../../new-spec/02-storage/index.md) (row: cable-connector).
