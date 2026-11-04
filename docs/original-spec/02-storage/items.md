# Items

## Identity
`PaintKitItem` (SH), `PaintedBlockItem` (SH), `BlockItemTSS` (SH), `ILeftClickListener` (SH).

Sources (upstream 26.2@95bb61d): `NeoForge/src/platform-shared/java/com/tom/storagemod/item/{PaintKitItem,PaintedBlockItem,BlockItemTSS,ILeftClickListener}.java`, registry in `NeoForge/src/platform-shared/java/com/tom/storagemod/Content.java`, dispatch in `NeoForge/src/main/java/com/tom/storagemod/ServerEvents.java`; short names below are `Class.java:line` in those files (declarations: PaintKitItem.java:29, PaintedBlockItem.java:13, BlockItemTSS.java:14, ILeftClickListener.java:7).

## Inputs
- Paint Kit: right-click a block (apply paint), shift-right-click (collect facade from a full opaque block) (PaintKitItem.java:42-44).

## Numbers
- Paint Kit durability cost: 1 per successful paint; on break the hand receives an `Items.BUCKET` (PaintKitItem.java:61-66) (paint kit is a bucket-shaped item) (PaintKitItem.java:62-64).
- Paint sound: `BUCKET_EMPTY` (volume 1.0, pitch 1.0) (PaintKitItem.java:60).

## Behavior
1. `PaintKitItem.useOn` (server):
   - Shift (secondary use): if the clicked block has no BE, is `canOcclude()`, and has a full-block shape → store its `BlockState` into the `paint` data component on the held stack. Always SUCCESS (PaintKitItem.java:44-51).
   - Plain: if the target block is `IPaintable` and the held stack has a non-null `paint` component → `paint(level, pos, storedState)`; on success play the bucket sound, damage the kit by 1 (in the hand's equipment slot), and on break hand over an empty bucket (PaintKitItem.java:55-67; the kit is only damaged when a player is present, :61).
   - (client) returns SUCCESS for `IPaintable` targets (or shift) to swing the arm (PaintKitItem.java:72-77).
2. `PaintKitItem.getName`: appends " (<block name in green>)" when the paint component is set (PaintKitItem.java:82-92).
3. `PaintedBlockItem extends BlockItemTSS`: same name suffix for painted trim items (PaintedBlockItem.java:19-29).
4. `BlockItemTSS extends BlockItem`: `appendHoverText` delegates to the block's `BlockWithTooltip.appendHoverText` — every TS block item shows its block's tooltip lines (BlockItemTSS.java:20-24).
5. `ILeftClickListener`: `onLeftClick(ItemStack, BlockPos, Player)` (returns boolean, ILeftClickListener.java:8; invoked from ServerEvents.java:31) — hook for items that act on left-click (implemented by configurator-related items, 06-client-ui / 01-core).

## Determinism
Deterministic.

## Data formats
`paint` data component: a `BlockState` (codec-registered, 01-core; the registry id is `paint_state`, persistent codec `BlockState.CODEC`, Content.java:125). `configurator`, `simple_item_filter`, `tag_filter` components on filter items — see `components.md`.

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
