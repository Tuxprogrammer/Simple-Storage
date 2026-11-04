# Level emitter GUI (menu + screen)

| # | Field | Contents |
|---|---|---|
| 1 | **Identity** | `LevelEmitterMenu` (SH `menu/LevelEmitterMenu.java`), `LevelEmitterScreen` (SH `screen/LevelEmitterScreen.java`, texture `toms_storage:textures/gui/level_emitter.png`). Menu type `Content.levelEmitterMenu`; title `menu.toms_storage.level_emitter` = "Level Emitter". Opened from the Level Emitter block (02-storage `level-emitter.md`). |
| 2 | **Inputs** | Filter item (single-slot phantom), count text field (max 100 chars, integer ≥ 0), less-than toggle, 8 amount buttons (+1/+10/+100/+1000, −1/−10/−100/−1000; with SHIFT held they become the "sv" step −1/−10/−100/−1000 — i.e. SHIFT doubles the step magnitude per the `sv` table), player inventory. |
| 3 | **Numbers** | Phantom filter slot at (43, 38). Text field at (leftPos+70, topPos+41), width 89. Less-than toggle at (leftPos−18, topPos+5) with icons `icons/greater_than` (off) / `icons/less_than` (on). Amount buttons (text, 20 px tall): +1 at (leftPos+20, topPos+16) w20; +10 at +45 w25; +100 at +75 w30; +1000 at +110 w35; negative row at topPos+56: −1 (x20 w20), −10 (x45 w25), −100 (x75 w30), −1000 (x110 w35). Player slots: 3 rows at y=84, hotbar at y=142. Count default 1, min 0. |
| 4 | **Behavior** | See Behavior detail. |
| 5 | **Determinism** | None (no randomness). |
| 6 | **Data formats** | Data slots: `count` int (→ `te.setCount` read via `IntDataSlots`), `lessThan` int 0/1 (→ `te.isLessThan()`). Both fire `updateGui` → the screen's `onPacket` runnable (re-syncs the toggle and text field). Client→server tag: `count` int, `lessThan` bool → `te.setCount` / `te.setLessThan` (spectators ignored). |
| 7 | **Output** | Filter item and count/threshold written to the Level Emitter block entity; redstone output computed by the block (02-storage `level-emitter.md`). |
| 8 | **Relationships** | `LevelEmitterBlockEntity` (02-storage `level-emitter.md`), `PhantomSlot` (03-terminal `item-filter-gui.md`), `AbstractFilteredMenu`/`AbstractFilteredScreen` (03-terminal `item-filter-gui.md`), `KeyUtil.hasShiftDown` (03-terminal `terminal-utils.md`), `ClientUtil.multilineTooltip` (06-client-ui), `ToggleButton` (06-client-ui). |
| 9 | **NeoForge dependencies** | `DataSlots`/`IntDataSlots` with `onUpdate` hooks (NeoForge container data slots — no 1.7.10 counterpart), `EditBox`, `Button` with dynamic messages, `RenderPipelines` blit. |
| 10 | **Bugs** | `TERM-10` (the SHIFT "double step" table is asymmetric: the +1000 button's sv is −64, so SHIFT+1000 button decreases the count by 64, and the negative buttons' sv magnitudes (−1/−10/−100/−1000) are not the doubles of their v values). |

## Behavior detail

### Menu (server)

1. Filter "container": when a block entity is present, an anonymous 1-slot `Container` whose `setItem` writes `te.setFilter(stack)`, `getItem` returns `te.getFilter()` (as a plain stack), and `stillValid` is always false (the menu's `stillValid` uses the block entity's instead); `SimpleContainer(1)` placeholder on the client. `startOpen`/`stopOpen` on open/close.
2. Slot 0: `PhantomSlot(inv, 0, 43, 38)` — not pickable, max stack 1, mayPlace always true (clicking with any item places a count-1 copy — see `AbstractFilteredMenu.clicked`, 03-terminal `item-filter-gui.md`).
3. Slots 1..36: player main + hotbar (y=84/142).
4. `quickMoveStack`: shift-click from the player places a count-1 copy into the phantom slot only if it is empty or holds a different item (`isSameItemSameComponents` check); the phantom slot itself does nothing. Always returns EMPTY (the player stack is not consumed by shift-click — the item stays where it was).
5. `stillValid`: block entity's `stillValid` (or true for the client menu).
6. `receive` (server): spectator/no-te guard, super (`setPhantom` handling), `count` → `te.setCount`, `lessThan` → `te.setLessThan`.

### Screen (client)

7. `init`: text field initialized to the current count; on each change, parse as int ≥ 0 → `menu.count` + `send()`; invalid text is ignored (field keeps the text). Less-than toggle: icons as listed, tooltip `tooltip.toms_storage.level_emitter.greater_than` = "Emit when more than specified in storage" (off) / `less_than` = "Emit when less than specified in storage" (on), state initialized from `menu.lessThan`. Amount buttons: label = "+v" or "v"; on press: `menu.count += hasShiftDown() ? sv : v`, clamped at 0, update field, `send()`. Button labels re-render every frame to show the SHIFT variant.
8. `extractRenderState`: update all amount button labels (SHIFT state).
9. `send()`: tag {`count`, `lessThan`} → server.
10. `getExclusionAreas`: Box(leftPos−20, topPos, 25, 25).

## Lang keys used by this feature

- `menu.toms_storage.level_emitter` = "Level Emitter"
- `narrator.toms_storage.level_emitter_amount` = "Level Emitter Item Count"
- `tooltip.toms_storage.level_emitter` = "Emits a redstone signal based on Inventory System contents."
- `tooltip.toms_storage.level_emitter.greater_than` = "Emit when more than specified in storage"
- `tooltip.toms_storage.level_emitter.less_than` = "Emit when less than specified in storage"

## Deviations in the new mod

See the [03-terminal deviation table](../../new-spec/03-terminal/deviations.md#deviation-table) (row `level-emitter-gui`).
