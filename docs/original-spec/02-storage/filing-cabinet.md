# Filing Cabinet

## Identity
`com.tom.storagemod.block.FilingCabinetBlock` (SH) + `FilingCabinetBlockEntity` (SH) + `FilingCabinetContainer` (SH/util). The "storage item with internal inventory" of the area table: upstream it is a **block**, not an item — see the index deviation note.

## Inputs
- Placement: `getStateForPlacement` — FACING = opposite of the clicked horizontal face (default NORTH; `HORIZONTAL_FACING`).
- Right-click (server): opens `FilingCabinetMenu` via `player.openMenu(be)`; no distance check at the block level.
- Block item tooltip: `tooltip.toms_storage.filing_cabinet` = "Store multiple variants of the same item".

## Numbers
- 512 inventory slots (fixed constant in the BE).
- Interaction reach: `distanceToSqr < 64.0` (8 blocks) in `canInteractWith`; used by the menu's `stillValid` via the BE.
- `getMaxStackSize() = 1` per slot.

## Behavior
1. (server) `onPlace`: FACING set to opposite of click direction.
2. (server) `affectNeighborsAfterRemoval`: redstone-style neighbor update after removal (NeoForge hook).
3. BE holds a `FilingCabinetContainer` (512 slots); `onRemove` drops contents via `dropContents`.
4. BE is `Nameable` (custom name stored as component, nullable).
5. Menu provider: `FilingCabinetMenu(id, container, container)` — title `menu.toms_storage.filing_cabinet` = "Filing Cabinet".
6. `FilingCabinetContainer` (wraps `SimpleContainer`):
   - Tracks an `item` field = canonical item type.
   - `setItem(slot, stack)`: if stack non-empty → `item = stack.getItem()` **unconditionally** (no consistency check — writing a different type through the API re-locks the cabinet to the new type); if stack empty and the whole inventory is now empty → `item = null`.
   - `removeItem` / `removeItemNoUpdate`: `item` is reset to null **only when the entire inventory becomes empty** — emptying a single slot keeps the lock.
   - `canPlaceItem(slot, stack)`: if `item != null` → only the exact same `Item`; else only items with `maxStackSize == 1`.
7. `fromTag`: `tag.listOrEmpty("inventory", ItemStack.CODEC)` → `fromItemList`; then `item = getItem(0)` if slot 0 non-empty, else null — **slot 0 only** (see STOR-02).

## Determinism
Deterministic. No randomness.

## Data formats
NBT (BE): `inventory` — list of item stacks (only non-empty slots, via `storeAsItemList`); `CustomName` — optional component.

## Output
Contents drop as item entities on removal; GUI access via `FilingCabinetMenu`/`FilingCabinetScreen` (03-terminal `filing-cabinet-gui.md`).

## Relationships
- Menu/screen: 03-terminal `filing-cabinet-gui.md`.
- Platform container base (`SimpleContainer`) and `ItemStack.CODEC` from 01-core.

## NeoForge dependencies
`affectNeighborsAfterRemoval` (NeoForge `Block` hook), `Nameable`, codec-based NBT (`ValueInput`/`ValueOutput`), `ItemStack.CODEC`.

## Bugs
STOR-02 (lock semantics — `FilingCabinetContainer.java:61-77` + `:94-99`).
