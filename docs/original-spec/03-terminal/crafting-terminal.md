# Crafting terminal (menu + screen + auto-fill)

| # | Field | Contents |
|---|---|---|
| 1 | **Identity** | `CraftingTerminalMenu` (SH `menu/CraftingTerminalMenu.java`), `CraftingTerminalScreen` (SH `screen/CraftingTerminalScreen.java`, texture `toms_storage:textures/gui/crafting_terminal.png`, 194×256, 5 storage rows, slot origin (7,17)), `TerminalCraftingFiller` (SH `menu/TerminalCraftingFiller.java`), `RecipeBookButton` (SH `screen/RecipeBookButton.java`, vanilla recipe-book sprite 20×18). Menu type `Content.craftingTerminalMenu`. |
| 2 | **Inputs** | Everything the plain storage terminal has (03-terminal `storage-terminal.md`), plus: the 3×3 crafting grid, recipe book, Clear Grid button, pull-from-player-inventory toggle (mode bit 0x20), `S` key over a ghost recipe slot, the recipe "fill" message (`fill` = recipe id). |
| 3 | **Numbers** | GUI 194×256 px. Crafting grid at x = −4+30+j×18, y = 94+17+i×18 (relative to leftPos/topPos); result slot at (120, 129) relative (x+124, y+35 with x=−4, y=94 → (120,129)); storage slots 5 rows from (8,18). Player slots at (8,174). Recipe book visible when screen width ≥ 379 px (narrower → toggleable overlay). Clear button 11×11 px at (leftPos+80, topPos+20+rowCount×18). 7th button (pull-from-inv) at (leftPos−18, topPos+5+108); button column wraps to a second column (x − 18) when y+18 would exceed the available space (recipe-book search-box y − 16, else imageHeight). Amount buttons and recipe book: vanilla recipe book widget (`CraftingTerminalRecipeBookWidget`, 06-client-ui). |
| 4 | **Behavior** | See Behavior detail. |
| 5 | **Determinism** | Recipe lookup deterministic (vanilla recipe manager / polymorph). Auto-fill order: grid slots in `PlaceRecipeHelper` placement order; item sources tried in this order — terminal (per matching stored item), then player inventory. |
| 6 | **Data formats** | All storage-terminal tags plus: menu buttons — id 0 = Clear Grid (`te.clear(player)`), id 1 = polymorph update (`te.polymorphUpdate(player)`). `receive` (server) reads `fill` = recipe id string (`Identifier.tryParse`); if it resolves in the player's recipe manager, a `TerminalCraftingFiller` places the recipe. Crafting grid NBT: `CraftingTable` (list of items) — see 03-terminal `terminal-blocks.md`. |
| 7 | **Output** | Crafted result in the result slot; auto-filled grid (items pulled from network and/or player inventory); recipe book state; grid cleared back into the network. |
| 8 | **Relationships** | Extends `StorageTerminalMenu` / `AbstractStorageTerminalScreen` (03-terminal `storage-terminal.md`). Uses `CraftingTerminalBlockEntity` (03-terminal `terminal-blocks.md`), `CraftingMatrix` (03-terminal `terminal-utils.md`), `IAutoFillTerminal` (03-terminal `terminal-utils.md`), `TerminalCraftingFiller` (this sheet), `PolymorphHelper` (05-integration), widgets `ToggleButton`, `CraftingTerminalRecipeBookWidget` (06-client-ui). |
| 9 | **NeoForge dependencies** | `RecipeUpdateListener` (recipe book refresh), `RecipeBookComponent.RECIPE_BUTTON_SPRITES`, `PlaceRecipeHelper.placeRecipe(3, 3, recipe, slotsToIngredientIndex, callback)` (vanilla recipe-placement math; 1.7.10 has no direct counterpart — must be re-implemented), `RecipeDisplay`, `StackedItemContents`, `TransientCraftingContainer` (client menu), `ResultSlot`/`CraftingContainer`, `RecipeHolder` + `Registries.RECIPE` key lookup. |
| 10 | **Bugs** | `TERM-05` (auto-fill accounts player-inventory stacks that are then pulled one-by-one with no reservation — double-claim is possible across multiple grid cells in one fill). |

## Behavior detail

### Menu

1. Constructor (server): `super(craftingTerminalMenu, id, inv, te)` → storage slots (5×9 `SlotStorage`, not real slots); then 10 real slots: index 0 = result `Result` (x=120, y=129 relative), indices 1..9 = `SlotCrafting` grid (row-major, j + i×3). `addPlayerSlots(inv, 8, 174)` (playerSlotsStart = 9). `te.registerCrafting(this)`; `removed` → `te.unregisterCrafting`.
2. `Result.onTake`: `checkTakeAchievements(stack)`; server side → `te.craft(player)` (consumes inputs, refills the grid from the network — see terminal-blocks step 16).
3. `addStorageSlots` override: 5 rows at (8,18).
4. `shiftClickItems` (server):
   - index 0 (result): if `!te.canCraft()` or `moveItemStackTo(stack, 10, 46, true)` (player slots, excluding offhand at 46) fails → nothing happens. Else `onQuickCraft`, update the slot; if the whole stack moved, done; otherwise `slot.onTake` and **drop the remainder at the player** (`playerIn.drop(itemstack1, false)`) — a shift-click that only partially fits drops the rest.
   - index 1..9 (grid): push the stack into the terminal (`te.pushStack(itemstack)`), remainder back in the grid slot, `broadcastChanges` (server).
   - `slot.onTake` runs for the grid case too (plain `Slot.onTake` — no extra effect).
5. `onCraftMatrixChanged` (server, per menu): sends `ClientboundContainerSetSlotPacket` for slots 0..9 (and the result) to every `ServerPlayer` listener.
6. `clickMenuButton`: id 0 → `te.clear(player)`; id 1 → `te.polymorphUpdate(player)`; else super (data-slot buttons). Always returns false.
7. `receive`: super (storage tags) then `fill` = recipe id → `TerminalCraftingFiller(te, player, sync).placeRecipe(recipe.value())`.
8. `handlePlacement` (recipe-book "crafting" placement): `TerminalCraftingFiller.placeRecipe(...)`; returns NOTHING (the filler already mutated the grid).
9. `getStoredItems` = `itemList`; `isSmartSearch` = (searchType & 8) == 0; `getInputGridSlots` = slots 1..9.

### TerminalCraftingFiller (server)

10. `placeRecipe(recipe)`: `te.clear(player)` (grid → network → player → drop); `sync.fillCraftingFiller(this)` (integration hook, e.g. AE-style auto-fill providers register here — see `IAutoFillTerminal`); account the player's non-equipment inventory stacks into `allItems` keyed by item holder (**skips stacks with a custom name component**); then `PlaceRecipeHelper.placeRecipe(3, 3, recipe, placementInfo.slotsToIngredientIndex, (idx, slotId, ...) -> ...)`: for each grid cell, iterate the ingredient's allowed items in order; for each, first try the terminal — for each stored stack of that item, if the ingredient test passes, `te.pullStack(stack, 1)` and place; else the player inventory — `findSlotMatchingItem(item)`, remove 1. **`TERM-05`: accounting is per-item, not reserved per cell — two cells needing the same single item can both claim it before either pulls (player-inventory path: `findSlotMatchingItem` re-scans live inventory, so the player side is mostly safe; the terminal side pulls lazily per cell, but `allItems` lists are never decremented, so a stored quantity of 1 shared by two cells is handled because the pull itself fails the second time — the visible risk is the player-inventory `findSlotMatchingItem` matching a slot whose item was just consumed by the same fill; observed: each cell pulls exactly 1 and checks success, so this is a suspected edge, not a proven loss)**.
11. `accountStack`: skips stacks with `DataComponents.CUSTOM_NAME`; groups by `typeHolder()`.

### Screen

12. `init`: super; `widthTooNarrow = width < 379`; recipe book hidden if currently visible and the screen is narrow (auto-toggled); recipe book initialized, screen re-positioned (`leftPos = recipeBookGui.updateScreenPosition(width, imageWidth − 16)`); initial focus = recipe book; Clear Grid button; pull-from-inv toggle (icons `icons/refill_off`/`icons/refill_on`) added as the 7th button in the left column; recipe book toggle button at (leftPos+4, topPos+38+rowCount×18); `setButtonsPos` lays out all 7 buttons (order: sort, direction, search type, control mode, ghost, pull-from-inv, tall mode) at 18 px spacing, wrapping to a second column at x−18 when the next button would exceed `space` (recipe-book search-box y − 16, else imageHeight); `onPacket` to sync states.
13. `onPacket`: super + `buttonPullFromInv.setState((menu.modes & 0x20) != 0)`. `writeModes`: super | (on ? 0x20 : 0).
14. `onUpdateSearch` (called from the base on every search change): when `IAutoFillTerminal.hasSync()` or search-type bit 4 — set the recipe-book search box text to the search string and refresh recipes.
15. `keyPressed`: `S` while hovering a slot that has a ghost-recipe ingredient → search field = that ingredient's hover name (unfocused) — quick search for a recipe ingredient; ESC when the recipe book is open in narrow mode → close the book.
16. `mouseClicked`: recipe book consumes its clicks (and takes focus); in narrow mode with the book open, a click outside the GUI toggles the book closed (returns false so the slot doesn't get the click).
17. `extractRenderState`: narrow mode with the book open → base render with (−1,−1) mouse (no hover) + book render; else both with the real mouse. `slotClicked` also notifies the recipe book.
18. `fillGhostRecipe`: delegates to the recipe book widget.
19. `ButtonClear`: 11×11 sprite button (`widget/clear_button`/`_disabled`/`_hovered`), presses → `handleInventoryButtonClick(containerId, 0)`.

## Lang keys used by this feature

- `menu.toms_storage.crafting_terminal` = "C. Terminal"
- `tooltip.toms_storage.crafting_pull_on` = "Pull missing items from players inventory when crafting"; `crafting_pull_off` = "Don't pull from players inventory when crafting"
- `tooltip.toms_storage.crafting_terminal` = "Gives access to your items.\Place it on an inventory.\Has a builtin crafting table."
- `tooltip.toms_storage.recipe_book` = "Recipe Book"
- `tooltip.toms_storage.items_missing` = "Some items may not be inserted into the crafting grid"
- Plus all plain-terminal keys (03-terminal `storage-terminal.md`).

## Deviations in the new mod

None yet.
