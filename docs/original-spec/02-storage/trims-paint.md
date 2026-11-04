# Trims and Paint

## Identity
`TrimBlock` (SH), `PaintedTrimBlock` (SH), `IPaintable` (SH), `PaintedBlockEntity` (NF), `IConfiguratorHighlight` (SH), `BlockWithTooltip` (SH). The trim blocks fill gaps so inventories stay "touching" through the network; paint recolors them.

Sources (upstream 26.2@95bb61d): `NeoForge/src/platform-shared/java/com/tom/storagemod/block/{TrimBlock,PaintedTrimBlock,IPaintable,IConfiguratorHighlight,BlockWithTooltip}.java`, `.../block/entity/AbstractPainedBlockEntity.java` and `NeoForge/src/main/java/com/tom/storagemod/block/entity/PaintedBlockEntity.java`; short names below are `Class.java:line` in those files (declarations: TrimBlock.java:19, PaintedTrimBlock.java:31, PaintedBlockEntity.java:13, AbstractPainedBlockEntity.java:18).

## Inputs
- `TrimBlock`: plain block, `IPaintable` + `BlockWithTooltip` (TrimBlock.java:19).
- `PaintedTrimBlock`: `BaseEntityBlock`, `IPaintable` + `IConfiguratorHighlight` (PaintedTrimBlock.java:31; it also implements `BlockWithTooltip`).
- Paint source: Paint Kit (see `items.md`) or scrape tool.

## Numbers
- `PaintedTrimBlock` highlight color: `0x888888` (gray), full-block highlight shape (PaintedTrimBlock.java:83-91).
- `PaintedBlockEntity`: model property `FACADE_STATE` (PaintedBlockEntity.java:14); client dirty broadcast flag 3 (`sendBlockUpdated(..., 3)`) (PaintedBlockEntity.java:35).

## Behavior
1. `IPaintable.paint(world, pos, to)`: per-block hook that stores the facade state (PaintedTrimBlock.java:54-60).
2. `IPaintable.tryScrape(item, state, world, pos, player, hand)` (static): returns true and clears the facade (calls `paint(world, pos, null)`, IPaintable.java:37) when the held item is a scrape item (Paint Kit or debug item, per 01-core `Platform.canScrapeWithItem`); shield in offhand (non-sneaking) skips scraping (IPaintable.java:33, 52-54); on success: advancement trigger `ITEM_USED_ON_BLOCK`, `AXE_STRIP` sound (1.0/1.0), 1 durability damage (IPaintable.java:31-50; trigger :39, sound :41, damage :43).
3. `TrimBlock.paint`: replaces the block with the painted trim's default state, then calls `paintedTrim.paint(world, pos, to)` to store the facade (i.e., trim → painted trim on paint) (TrimBlock.java:33-37).
4. `PaintedTrimBlock`:
   - `getCloneItemStack`: the pick-block result is the **plain `inventory_trim`** item (paint is not carried; this is not a drop table) (PaintedTrimBlock.java:73-75).
   - `useItemOn`: `tryScrape` (scrapes the facade off: `paint(world, pos, null)` clears the stored state, IPaintable.java:37) (PaintedTrimBlock.java:45-52).
5. `PaintedBlockEntity` (NF, extends `AbstractPainedBlockEntity` — 01-core): stores the painted `BlockState` (AbstractPainedBlockEntity.java:19, 25-31); `getModelData` exposes it via `FACADE_STATE` (PaintedBlockEntity.java:24-27); `markDirtyClient` = setChanged + request model data update + block update packet (flag 3) (PaintedBlockEntity.java:29-37).
6. `IConfiguratorHighlight`: `getHighlightColor()` + `getHighlightShape(state, level, pos)` (IConfiguratorHighlight.java:9-10) — used by the inventory configurator's block highlight (06-client-ui).
7. `BlockWithTooltip`: interface `appendHoverText(...)` that block items delegate to (`BlockItemTSS`, see `items.md`) (BlockWithTooltip.java:10-12).

## Determinism
Deterministic.

## Data formats
Painted state: a `BlockState` stored via `PaintedBlockEntity` (NBT through the platform base, 01-core): key `block` with `BlockState.CODEC`, absent when null (AbstractPainedBlockEntity.java:33-44); item component `paint_state` mirrors it (AbstractPainedBlockEntity.java:62-74; Content.java:125).

## Output
Trims render as the facade block they were painted with; unpainted trim renders as itself.

## Relationships
- `items.md` (PaintKitItem, PaintedBlockItem, BlockItemTSS).
- 01-core: `AbstractPainedBlockEntity`, `Platform.canScrapeWithItem`, model data.
- 06-client-ui: highlight rendering.

## NeoForge dependencies
Model property registration (`FACADE_STATE`), client model data update, attachment-based paint storage (NF).

## Bugs
None.

## Deviations in the new mod

See [new-spec 02-storage](../../new-spec/02-storage/index.md) (row: trims-paint).
