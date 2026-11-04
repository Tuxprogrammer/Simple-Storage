# Inventory Proxy

## Identity
`com.tom.storagemod.block.InventoryProxyBlock` (SH) + `InventoryProxyBlockEntity` (SH). Extends the "inventory face" of the block it points at, so a network can reach through it.

Sources (upstream 26.2@95bb61d): `NeoForge/src/platform-shared/java/com/tom/storagemod/block/InventoryProxyBlock.java`, `.../block/entity/InventoryProxyBlockEntity.java` and `.../block/IPaintable.java`; short names below are `Class.java:line` in those files (class declarations: InventoryProxyBlock.java:37, InventoryProxyBlockEntity.java:14).

## Inputs
- Placement: FACING = opposite of the clicked face (full-direction, default DOWN) (InventoryProxyBlock.java:39, 43, 101-102).
- Paint (scrape) with a Paint Kit on the block: `IPaintable` path.

## Numbers
- Highlight color: `0x880088` (purple) — `IConfiguratorHighlight.getHighlightColor` (InventoryProxyBlock.java:106-107).
- Highlight shape: per-direction boxes (the face it points at) (InventoryProxyBlock.java:112-124).

## Behavior
1. (both) `getStateForPlacement`: FACING = clicked face's opposite.
2. (server) `useItemOn`: `tryScrape` — with a valid scrape item (Paint Kit / debug, per 01-core `Platform.canScrapeWithItem`), paints the proxy to the Painted state; plays `AXE_STRIP` (volume 1.0, pitch 1.0) (IPaintable.java:41), consumes 1 durability (IPaintable.java:43); shield in offhand blocks scraping unless sneaking (IPaintable.java:33, 52-54; InventoryProxyBlock.java:54-56).
3. `paint(world, pos, to)`: sets the painted block state on the BE (`PaintedBlockEntity.setPaintedBlockState`) (InventoryProxyBlock.java:63-66).
4. BE (`InventoryProxyBlockEntity extends PaintedBlockEntity implements IInventory`):
   - Wraps a `BlockInventoryAccess` over the block in the FACING direction: `onLoad(level, pos.relative(facing), facing.getOpposite(), this)` (InventoryProxyBlockEntity.java:14, 29-34).
   - `onInvalid` (from the platform access): `markCapsInvalid` — the proxy stops presenting an inventory when the target changes (InventoryProxyBlockEntity.java:18-19).
5. Tooltip: `tooltip.toms_storage.inventory_proxy` = "Extends the face of the block its pointing at." (InventoryProxyBlock.java:49-50)

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

## Deviations in the new mod

See [new-spec 02-storage](../../new-spec/02-storage/index.md) (row: inventory-proxy).
