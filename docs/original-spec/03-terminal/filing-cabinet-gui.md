# Filing cabinet GUI (menu + screen)

| # | Field | Contents |
|---|---|---|
| 1 | **Identity** | `FilingCabinetMenu` (SH `menu/FilingCabinetMenu.java`), `FilingCabinetScreen` (SH `screen/FilingCabinetScreen.java`). Menu type `Content.filingCabinetMenu`; title `menu.toms_storage.filing_cabinet` = "Filing Cabinet". Background: vanilla `textures/gui/container/generic_54.png` (two blits: header rows + fixed bottom) plus mod texture `textures/gui/side_scrollbar.png` (24×115) at (leftPos+170, topPos). |
| 2 | **Inputs** | The filing cabinet's 512-slot single-item container (02-storage `filing-cabinet.md`), player inventory, mouse wheel, scrollbar drag, menu button (row change). |
| 3 | **Numbers** | GUI height = 114 + rows×18 = 204 px for the standard 5 visible rows; width 176. Cabinet capacity: 512 slots (client-side placeholder `SimpleContainer(512)`; real size from the block entity container). Visible rows: 5; each menu-button row step = 9 slots (one row). Player slots at y = 103 + (rows−4)×18 = 121 (3 rows) and hotbar at 161 + 18 = 179. Scrollbar: region x ∈ [leftPos+174, leftPos+188), y ∈ [topPos+18, topPos+18+5×18); scroller sprite `container/creative_inventory/scroller` 12×15 px; `currentScroll = clamp((mouseY − top − 7.5) / (height − 15), 0, 1)`. Locked-slot icon: `icons/locked_slot`. |
| 4 | **Behavior** | See Behavior detail. |
| 5 | **Determinism** | None (no randomness). Scroll is continuous (float 0..1) quantized to rows in `containerTick`. |
| 6 | **Data formats** | Menu button `row` (int ≥ 0) → `setRow(row)`: `container.setStartOffset(row * 9)` on the `LimitedContainer` window. No NBT of its own; the window is not persisted (each open starts at row 0 — inferred: no row state in the block entity). |
| 7 | **Output** | Windowed view over the cabinet (5 rows), items moved between cabinet and player inventory. |
| 8 | **Relationships** | `LimitedContainer` (02-storage `filing-cabinet.md`) — a 45-slot window over the cabinet container with a start offset; `FilingCabinetContainer` for `canPlaceItem`. `TSContainerScreen` base (03-terminal `container-screens.md`). |
| 9 | **NeoForge dependencies** | `Generic_54`-style vanilla GUI texture (1.7.10: `textures/gui/container/generic_54.png` exists there too), vanilla `Slot` overrides (`getMaxStackSize`, `mayPlace`, `mayPickup`, `getNoItemIcon` — 1.7.10: `getSlotIcon`), `glfwGetMouseButton` for drag detection (1.7.10: `isMouseButtonDown`). |
| 10 | **Bugs** | `TERM-06` (wheel scroll divides by (rows−5); a cabinet with exactly 45 items → division by zero, float Inf clamped to 0/1). |

## Behavior detail

### Menu (server unless noted)

1. Slots 0..44: cabinet slots through the `LimitedContainer(inv, 45)` window (start offset set by `setRow`). Per-slot rules: max stack size 1 (both directions); `mayPlace` — only if the slot is within the window (`isValid`) and the incoming stack's max size is 1, and if the cabinet already holds an item, the incoming item must be `==` the same `Item` (first non-empty cabinet slot decides); `mayPickup` — only within the window; `getItem` — EMPTY when outside the window; empty slots outside the window render the locked-slot icon (`getNoItemIcon` → `icons/locked_slot`, null inside the window).
2. Slots 45..71: player main (3 rows) at y=121, slot index `j + l*9 + 9`; slots 72..80: hotbar at y=179.
3. `quickMoveStack`: cabinet → player (move to slots 45..end); player → cabinet: try `moveItemStackTo(0, 45, false)` (respects the same-item rule via `mayPlace`); if that fails and `containerParent.canPlaceItem(0, stack)` (cabinet container check — empty cabinet requires max-size-1, non-empty requires same item), insert into the first empty cabinet slot, mark changed; if no empty slot, the item stays.
4. `clickMenuButton(row)` → `setRow(row)`, returns true. `stillValid`: `container.stillValid(player)`.
5. `getContainerSize`: the cabinet's full size (used by the screen for scroll math).

### Screen (client)

6. `extractRenderState`: drag detection via raw GLFW left-button state (`wasClicking` edge): pressing inside the scrollbar region starts `isScrolling`; release stops it; while scrolling, `currentScroll` tracks the mouse (formula in Numbers). Super render, then the scroller sprite at `y = regionTop + (int)((regionHeight − 17) * currentScroll)`.
7. `extractBackground`: vanilla `generic_54.png` blit for the top `rows*18 + 17` px, then the bottom 96 px (offset 126 in the texture), then the mod side scrollbar texture (24×115) at (leftPos+170, topPos).
8. `containerTick` (every client tick): `i = (cabinetSize + 8) / 9 − 5` (scrollable row count); `scroll = round(currentScroll * i)`; if it changed, `menu.setRow(scroll)` and `handleInventoryButtonClick(containerId, scroll)`.
9. `mouseScrolled`: `i = (cabinetSize + 8) / 9 − 5`; `currentScroll = clamp(currentScroll − delta / i, 0, 1)`. **`TERM-06`: with exactly 5 full rows (45 items) i = 0 → `delta / 0.0f` = ±Inf → clamps instantly to 0/1; no crash.** Always returns true (consumes the wheel).
10. `getExclusionAreas`: none.

## Lang keys used by this feature

- `menu.toms_storage.filing_cabinet` = "Filing Cabinet"
- `block.toms_storage.filing_cabinet` = "Filing Cabinet"
- `tooltip.toms_storage.filing_cabinet` = "Store multiple variants of the same item"

## Deviations in the new mod

See the [03-terminal deviation table](../../new-spec/03-terminal/index.md#deviation-table) (row `filing-cabinet-gui`).
