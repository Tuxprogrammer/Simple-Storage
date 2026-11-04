# Inventory Cable Connector

## Identity
`InventoryCableConnectorBlock` (SH, `BaseEntityBlock`) + `FramedInventoryCableConnectorBlock` (SH, extends the former). A cable node with a FACING that attaches to a single inventory face and can open an inventory-link GUI.

Sources (upstream 26.2@95bb61d): `NeoForge/src/platform-shared/java/com/tom/storagemod/block/InventoryCableConnectorBlock.java` and `.../block/FramedInventoryCableConnectorBlock.java`; short names below are `Class.java:line` in those files (class declarations: InventoryCableConnectorBlock.java:54, FramedInventoryCableConnectorBlock.java:25).

## Inputs
- Six connection booleans (default false) + `FACING` enum (default DOWN) (InventoryCableConnectorBlock.java:61, 76, 114).
- Placement: `getStateForPlacement` sets FACING = clicked face's opposite (InventoryCableConnectorBlock.java:129-130).
- Right-click with empty hand: opens the inventory-link menu **only if the BE `hasBeacon()`** (see `cable-network.md` / 03-terminal `inventory-link.md`); otherwise PASS (InventoryCableConnectorBlock.java:283, 295).

## Numbers
- Collision apothem: `0.125f` (small cable body) (InventoryCableConnectorBlock.java:68).
- Shape table: `shapes[facingOrdinal][shapeIndex]`, 64 connection masks per facing (InventoryCableConnectorBlock.java:63, 201).
- Beacon-link config (from 01-core `Config`): `invLinkBeaconLvl` (range), `invLinkBeaconLvlSameDim`, `invLinkBeaconLvlCrossDim`; tooltip only shown when `invLinkBeaconLvl != -1` (InventoryCableConnectorBlock.java:83-90).
- Framed variant: full-block collision (`Shapes.block()`), highlight `0xFFFF00` (yellow) (FramedInventoryCableConnectorBlock.java:57, 62).

## Behavior
1. `canConnectFrom(state, dir)`: true iff `dir != FACING` (the facing face is reserved for the attached inventory, not the cable) (InventoryCableConnectorBlock.java:135-136).
2. `nextScan`: all directions except FACING whose connection boolean is set (InventoryCableConnectorBlock.java:140-144).
3. `updateShape`: for the FACING face, the boolean = `!neighbor.isAir()` (any block attaches); other faces use `canConnect` (InventoryCableConnectorBlock.java:118-125).
4. `withConnectionProperties`: same as cables, but the FACING face connects to any non-air block (InventoryCableConnectorBlock.java:149, 167-168).
5. `useWithoutItem` (server): if BE `hasBeacon()` and `stillValid(player)` → `openMenu(be)`; else overlay `chat.toms_storage.inv_link_access_denied` = "Access Denied" (InventoryCableConnectorBlock.java:280-292).
6. Server tick: `TickerUtil.createTicker(world, false, true)` — staggered server-side tick (see `cable-network.md` for the BE's link resolution) (InventoryCableConnectorBlock.java:102-104).
7. Framed variant: adds `IPaintable` + `IConfiguratorHighlight`, paint/scrape, full-block shape (FramedInventoryCableConnectorBlock.java:25, 57).

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
