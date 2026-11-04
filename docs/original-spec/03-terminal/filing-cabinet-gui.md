# Filing cabinet GUI (menu + screen)

| # | Field | Contents |
|---|---|---|
| 1 | **Identity** | `FilingCabinetMenu` (SH `menu/FilingCabinetMenu.java`), `FilingCabinetScreen` (SH `screen/FilingCabinetScreen.java`). Menu type `Content.filingCabinetMenu`; title `menu.toms_storage.filing_cabinet` = "Filing Cabinet". Background: vanilla `textures/gui/container/generic_54.png` (two blits: header rows + fixed bottom) plus mod texture `textures/gui/side_scrollbar.png` (24×115) at (leftPos+170, topPos). Sources: `SH` = `NeoForge/src/platform-shared/java/com/tom/storagemod/` on branch `26.2`; short refs are `FilingCabinetMenu.java:NN` (FM), `FilingCabinetScreen.java:NN` (FS) and `util/LimitedContainer.java:NN` (LC). (FM:16, FS:18, FS:19-21) |
| 2 | **Inputs** | The filing cabinet's 512-slot single-item container (02-storage `filing-cabinet.md`), player inventory, mouse wheel, scrollbar drag, menu button (row change). |
| 3 | **Numbers** | GUI height = 114 + rows×18 = 204 px for the standard 5 visible rows; width 176 (FS:29, FM:32). Cabinet capacity: 512 slots (client-side placeholder `SimpleContainer(512)`; real size from the block entity container) (FM:23-25). Visible rows: 5; each menu-button row step = 9 slots (one row) (FM:154-155). Player slots at y = 103 + (rows−4)×18 = 121 (3 rows) and hotbar at 161 + 18 = 179 (FM:33, 90, 95). Scrollbar: region x ∈ [leftPos+174, leftPos+188), y ∈ [topPos+18, topPos+18+5×18) (FS:39-42); scroller sprite `container/creative_inventory/scroller` 12×15 px (FS:20, 61); `currentScroll = clamp((mouseY − top − 7.5) / (height − 15), 0, 1)` (FS:54-55). Locked-slot icon: `icons/locked_slot` (FM:17). |
| 4 | **Behavior** | See Behavior detail. |
| 5 | **Determinism** | None (no randomness). Scroll is continuous (float 0..1) quantized to rows in `containerTick` (FS:77-78). |
| 6 | **Data formats** | Menu button `row` (int ≥ 0) → `setRow(row)`: `container.setStartOffset(row * 9)` on the `LimitedContainer` window (FM:149-156). No NBT of its own; the window is not persisted (each open starts at row 0 — inferred: no row state in the block entity). |
| 7 | **Output** | Windowed view over the cabinet (5 rows), items moved between cabinet and player inventory. |
| 8 | **Relationships** | `LimitedContainer` (02-storage `filing-cabinet.md`) — a 45-slot window over the cabinet container with a start offset (FM:31; LC:29, size = min(limit, size − offset)); `FilingCabinetContainer` for `canPlaceItem`. `TSContainerScreen` base (03-terminal `container-screens.md`). |
| 9 | **NeoForge dependencies** | `Generic_54`-style vanilla GUI texture (1.7.10: `textures/gui/container/generic_54.png` exists there too), vanilla `Slot` overrides (`getMaxStackSize`, `mayPlace`, `mayPickup`, `getNoItemIcon` — 1.7.10: `getSlotIcon`), `glfwGetMouseButton` for drag detection (1.7.10: `isMouseButtonDown`). |
| 10 | **Bugs** | `TERM-06` (FS:93-94; wheel scroll divides by (rows−5); a cabinet with exactly 45 items → division by zero, float Inf clamped to 0/1). |

## Behavior detail

### Menu (server unless noted)

1. Slots 0..44: cabinet slots through the `LimitedContainer(inv, 45)` window (start offset set by `setRow`) (FM:31, 35-38). Per-slot rules: max stack size 1 (both directions) (FM:40-46); `mayPlace` — only if the slot is within the window (`isValid`) and the incoming stack's max size is 1, and if the cabinet already holds an item, the incoming item must be `==` the same `Item` (first non-empty cabinet slot decides) (UNVERIFIED: the scan runs over the slot's container, which is the 45-slot window, so only the visible window decides, FM:53-57) (FM:50-59); `mayPickup` — only within the window (FM:62-65); `getItem` — EMPTY when outside the window (FM:68-71); empty slots outside the window render the locked-slot icon (`getNoItemIcon` → `icons/locked_slot`, null inside the window) (FM:74-79); `isValid` = window size > slot index (FM:81-83).
2. Slots 45..71: player main (3 rows) at y=121, slot index `j + l*9 + 9` (FM:88-91); slots 72..80: hotbar at y=179 (FM:94-96).
3. `quickMoveStack` (FM:100-137): cabinet → player (move to slots 45..end) (FM:106-107); player → cabinet: try `moveItemStackTo(0, 45, false)` (FM:110) (respects the same-item rule via `mayPlace`); if that fails and `containerParent.canPlaceItem(0, stack)` (cabinet container check — empty cabinet requires max-size-1, non-empty requires same item), insert into the first empty cabinet slot, mark changed; if no empty slot, the item stays (FM:111-127).
4. `clickMenuButton(row)` → `setRow(row)`, returns true (FM:148-152). `stillValid`: `container.stillValid(player)` (FM:140-141).
5. `getContainerSize`: the cabinet's full size (used by the screen for scroll math) (FM:158-160).

### Screen (client)

6. `extractRenderState`: drag detection via raw GLFW left-button state (`wasClicking` edge): pressing inside the scrollbar region starts `isScrolling`; release stops it (FS:36, 44-50); while scrolling, `currentScroll` tracks the mouse (formula in Numbers). Super render, then the scroller sprite at `y = regionTop + (int)((regionHeight − 17) * currentScroll)` (FS:57-61).
7. `extractBackground`: vanilla `generic_54.png` blit for the top `rows*18 + 17` px, then the bottom 96 px (offset 126 in the texture), then the mod side scrollbar texture (24×115) at (leftPos+170, topPos) (FS:70-72).
8. `containerTick` (every client tick): `i = (cabinetSize + 8) / 9 − 5` (scrollable row count); `scroll = round(currentScroll * i)`; if it changed, `menu.setRow(scroll)` and `handleInventoryButtonClick(containerId, scroll)` (FS:79-88).
9. `mouseScrolled`: `i = (cabinetSize + 8) / 9 − 5`; `currentScroll = clamp(currentScroll − delta / i, 0, 1)` (FS:93-95). **`TERM-06`: with exactly 5 full rows (45 items) i = 0 → `delta / 0.0f` = ±Inf → clamps instantly to 0/1; no crash.** Always returns true (consumes the wheel) (FS:96).
10. `getExclusionAreas`: none (FS:99-101).

## Lang keys used by this feature

- `menu.toms_storage.filing_cabinet` = "Filing Cabinet"
- `block.toms_storage.filing_cabinet` = "Filing Cabinet"
- `tooltip.toms_storage.filing_cabinet` = "Store multiple variants of the same item"

## Deviations in the new mod

See the [03-terminal deviation table](../../new-spec/03-terminal/deviations.md#deviation-table) (row `filing-cabinet-gui`).
