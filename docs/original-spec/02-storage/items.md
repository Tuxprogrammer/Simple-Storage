# Items

## Identity
`PaintKitItem` (SH), `PaintedBlockItem` (SH), `BlockItemTSS` (SH), `ILeftClickListener` (SH).

## Inputs
- Paint Kit: right-click a block (apply paint), shift-right-click (collect facade from a full opaque block).

## Numbers
- Paint Kit durability cost: 1 per successful paint; on break the hand receives an `Items.BUCKET` (paint kit is a bucket-shaped item).
- Paint sound: `BUCKET_EMPTY` (volume 1.0, pitch 1.0).

## Behavior
1. `PaintKitItem.useOn` (server):
   - Shift (secondary use): if the clicked block has no BE, is `canOcclude()`, and has a full-block shape → store its `BlockState` into the `paint` data component on the held stack. Always SUCCESS.
   - Plain: if the target block is `IPaintable` and the held stack has a non-null `paint` component → `paint(level, pos, storedState)`; on success play the bucket sound, damage the kit by 1 (in the hand's equipment slot), and on break hand over an empty bucket.
   - (client) returns SUCCESS for `IPaintable` targets (or shift) to swing the arm.
2. `PaintKitItem.getName`: appends " (<block name in green>)" when the paint component is set.
3. `PaintedBlockItem extends BlockItemTSS`: same name suffix for painted trim items.
4. `BlockItemTSS extends BlockItem`: `appendHoverText` delegates to the block's `BlockWithTooltip.appendHoverText` — every TS block item shows its block's tooltip lines.
5. `ILeftClickListener`: `onLeftClick(ItemStack, BlockPos, Player)` — hook for items that act on left-click (implemented by configurator-related items, 06-client-ui / 01-core).

## Determinism
Deterministic.

## Data formats
`paint` data component: a `BlockState` (codec-registered, 01-core). `configurator`, `simple_item_filter`, `tag_filter` components on filter items — see `components.md`.

## Output
Painted blocks; tooltip lines on all TS block items.

## Relationships
- `trims-paint.md` (`IPaintable`, `BlockWithTooltip`).
- 01-core: `Content.paintComponent`, `Platform.canScrapeWithItem`.

## NeoForge dependencies
Data components (`DataComponentPatch`, component registry), `SoundEvents.BUCKET_EMPTY`, `Items.BUCKET`.

## Bugs
None.

## Deviations in the new mod

See [new-spec 02-storage](../../new-spec/02-storage/index.md) (row: items).
