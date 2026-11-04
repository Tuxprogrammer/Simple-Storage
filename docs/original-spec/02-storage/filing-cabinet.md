# Filing Cabinet

## Identity
`com.tom.storagemod.block.FilingCabinetBlock` (SH) + `FilingCabinetBlockEntity` (SH) + `FilingCabinetContainer` (SH/util). The "storage item with internal inventory" of the area table: upstream it is a **block**, not an item — see the index deviation note.

Sources (upstream 26.2@95bb61d): `NeoForge/src/platform-shared/java/com/tom/storagemod/block/FilingCabinetBlock.java`, `.../block/entity/FilingCabinetBlockEntity.java`, `.../util/FilingCabinetContainer.java`; short names below are `Class.java:line` in those files (class declarations: FilingCabinetBlock.java:34, FilingCabinetBlockEntity.java:23, FilingCabinetContainer.java:14).

## Inputs
- Placement: `getStateForPlacement` — FACING = `context.getHorizontalDirection().getOpposite()`, i.e. the placer's horizontal look direction reversed, not the clicked face (default NORTH; `HORIZONTAL_FACING`) (FilingCabinetBlock.java:36, 69-70).
- Right-click (server): opens `FilingCabinetMenu` via `player.openMenu(be)`; no distance check at the block level (FilingCabinetBlock.java:74-84).
- Block item tooltip: `tooltip.toms_storage.filing_cabinet` = "Store multiple variants of the same item" (FilingCabinetBlock.java:43-45).

## Numbers
- 512 inventory slots (fixed constant in the BE) (FilingCabinetBlockEntity.java:24).
- Interaction reach: `distanceToSqr < 64.0` (8 blocks) in `canInteractWith`; used by the menu's `stillValid` via the BE (FilingCabinetBlockEntity.java:24, 60).
- `getMaxStackSize() = 1` per slot (FilingCabinetContainer.java:80-82).

## Behavior
1. Placement: there is no `onPlace` override; FACING is set in `getStateForPlacement` to the placer's horizontal direction reversed (FilingCabinetBlock.java:69-70).
2. (server) `affectNeighborsAfterRemoval`: redstone-style neighbor update after removal (NeoForge hook) (FilingCabinetBlock.java:98-101).
3. BE holds a `FilingCabinetContainer` (512 slots); `onRemove` drops contents via `dropContents` (FilingCabinetBlockEntity.java:24, 79-80).
4. BE is `Nameable` (custom name stored as component, nullable) (FilingCabinetBlockEntity.java:23, 35, 74).
5. Menu provider: `FilingCabinetMenu(id, container, container)` — title `menu.toms_storage.filing_cabinet` = "Filing Cabinet" (FilingCabinetBlockEntity.java:47, 51).
6. `FilingCabinetContainer` (wraps `SimpleContainer`):
   - Tracks an `item` field = canonical item type (FilingCabinetContainer.java:64).
   - `setItem(slot, stack)`: if stack non-empty → `item = stack.getItem()` **unconditionally** (no consistency check — writing a different type through the API re-locks the cabinet to the new type); if stack empty and the whole inventory is now empty → `item = null` (FilingCabinetContainer.java:61-66).
   - `removeItem` / `removeItemNoUpdate`: `item` is reset to null **only when the entire inventory becomes empty** — emptying a single slot keeps the lock (FilingCabinetContainer.java:47-49, 54-56).
   - `canPlaceItem(slot, stack)`: if `item != null` → only the exact same `Item`; else only items with `maxStackSize == 1` (FilingCabinetContainer.java:74-77).
7. `fromTag`: `tag.listOrEmpty("inventory", ItemStack.CODEC)` → `fromItemList`; then `item = getItem(0)` if slot 0 non-empty, else null — **slot 0 only** (see STOR-02) (FilingCabinetContainer.java:94-98).

## Determinism
Deterministic. No randomness.

## Data formats
NBT (BE): `inventory` — list of item stacks (only non-empty slots, via `storeAsItemList`); `CustomName` — optional component (FilingCabinetBlockEntity.java:34-35, 41-42; FilingCabinetContainer.java:101-102).

## Output
Contents drop as item entities on removal; GUI access via `FilingCabinetMenu`/`FilingCabinetScreen` (03-terminal `filing-cabinet-gui.md`).

## Relationships
- Menu/screen: 03-terminal `filing-cabinet-gui.md`.
- Platform container base (`SimpleContainer`) and `ItemStack.CODEC` from 01-core.

## NeoForge dependencies
`affectNeighborsAfterRemoval` (NeoForge `Block` hook), `Nameable`, codec-based NBT (`ValueInput`/`ValueOutput`), `ItemStack.CODEC`.

## Bugs
STOR-02 (lock semantics — `FilingCabinetContainer.java:61-77` + `:94-99`).

## Deviations in the new mod

See [new-spec 02-storage](../../new-spec/02-storage/index.md) (row: filing-cabinet).
