# Inventory Proxy

## Identity
`com.tom.storagemod.block.InventoryProxyBlock` (SH) + `InventoryProxyBlockEntity` (SH). Extends the "inventory face" of the block it points at, so a network can reach through it.

## Inputs
- Placement: FACING = opposite of the clicked face (full-direction, default DOWN).
- Paint (scrape) with a Paint Kit on the block: `IPaintable` path.

## Numbers
- Highlight color: `0x880088` (purple) — `IConfiguratorHighlight.getHighlightColor`.
- Highlight shape: per-direction boxes (the face it points at).

## Behavior
1. (both) `getStateForPlacement`: FACING = clicked face's opposite.
2. (server) `useItemOn`: `tryScrape` — with a valid scrape item (Paint Kit / debug, per 01-core `Platform.canScrapeWithItem`), paints the proxy to the Painted state; plays `AXE_STRIP` (volume 1.0, pitch 1.0), consumes 1 durability; shield in offhand blocks scraping unless sneaking.
3. `paint(world, pos, to)`: sets the painted block state on the BE (`PaintedBlockEntity.setPaintedBlockState`).
4. BE (`InventoryProxyBlockEntity extends PaintedBlockEntity implements IInventory`):
   - Wraps a `BlockInventoryAccess` over the block in the FACING direction: `onLoad(level, pos.relative(facing), facing.getOpposite(), this)`.
   - `onInvalid` (from the platform access): `markCapsInvalid` — the proxy stops presenting an inventory when the target changes.
5. Tooltip: `tooltip.toms_storage.inventory_proxy` = "Extends the face of the block its pointing at."

## Determinism
Deterministic.

## Data formats
Painted state stored by `PaintedBlockEntity` (01-core `platform-layer.md` / this area `trims-paint.md`).

## Output
Presents the proxied block's inventory to the cable network as if the network were touching that block's face directly.

## Relationships
- `trims-paint.md` (PaintedBlockEntity, IPaintable, IConfiguratorHighlight).
- 01-core: `PlatformInventoryAccess.BlockInventoryAccess`, `IInventoryAccess`.
- 03-terminal: `NetworkInventory` consumes the proxied access.

## NeoForge dependencies
`IPaintable` scrape hook (platform `Platform.canScrapeWithItem`), block entity capabilities invalidation.

## Bugs
None.
