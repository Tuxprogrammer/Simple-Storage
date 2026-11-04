# Trims and Paint

## Identity
`TrimBlock` (SH), `PaintedTrimBlock` (SH), `IPaintable` (SH), `PaintedBlockEntity` (NF), `IConfiguratorHighlight` (SH), `BlockWithTooltip` (SH). The trim blocks fill gaps so inventories stay "touching" through the network; paint recolors them.

## Inputs
- `TrimBlock`: plain block, `IPaintable` + `BlockWithTooltip`.
- `PaintedTrimBlock`: `BaseEntityBlock`, `IPaintable` + `IConfiguratorHighlight`.
- Paint source: Paint Kit (see `items.md`) or scrape tool.

## Numbers
- `PaintedTrimBlock` highlight color: `0x888888` (gray), full-block highlight shape.
- `PaintedBlockEntity`: model property `FACADE_STATE`; client dirty broadcast flag 3 (`sendBlockUpdated(..., 3)`).

## Behavior
1. `IPaintable.paint(world, pos, to)`: per-block hook that stores the facade state.
2. `IPaintable.tryScrape(item, state, world, pos, player, hand)` (static): returns true and applies the paint when the held item is a scrape item (Paint Kit or debug item, per 01-core `Platform.canScrapeWithItem`); shield in offhand (non-sneaking) skips scraping; on success: advancement trigger `ITEM_USED_ON_BLOCK`, `AXE_STRIP` sound (1.0/1.0), 1 durability damage.
3. `TrimBlock.paint`: replaces the block with the painted trim's default state, then calls `paintedTrim.paint(world, pos, to)` to store the facade (i.e., trim → painted trim on paint).
4. `PaintedTrimBlock`:
   - `getCloneItemStack`: drops the **plain `inventory_trim`** item (paint is lost on drop).
   - `useItemOn`: `tryScrape` (paint another block's facade onto it).
5. `PaintedBlockEntity` (NF, extends `AbstractPainedBlockEntity` — 01-core): stores the painted `BlockState`; `getModelData` exposes it via `FACADE_STATE`; `markDirtyClient` = setChanged + request model data update + block update packet (flag 3).
6. `IConfiguratorHighlight`: `getHighlightColor()` + `getHighlightShape(state, level, pos)` — used by the inventory configurator's block highlight (06-client-ui).
7. `BlockWithTooltip`: interface `appendHoverText(...)` that block items delegate to (`BlockItemTSS`, see `items.md`).

## Determinism
Deterministic.

## Data formats
Painted state: a `BlockState` stored via `PaintedBlockEntity` (NBT through the platform base, 01-core).

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
