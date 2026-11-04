# Crafting terminal (menu + screen + auto-fill)

| # | Field | Contents |
|---|---|---|
| 1 | **Identity** | `CraftingTerminalMenu` (SH `menu/CraftingTerminalMenu.java`), `CraftingTerminalScreen` (SH `screen/CraftingTerminalScreen.java`, texture `toms_storage:textures/gui/crafting_terminal.png`, 194×256, 5 storage rows, slot origin (7,17)), `TerminalCraftingFiller` (SH `menu/TerminalCraftingFiller.java`), `RecipeBookButton` (SH `screen/RecipeBookButton.java`, vanilla recipe-book sprite 20×18). Menu type `Content.craftingTerminalMenu`. Sources: `SH` = `NeoForge/src/platform-shared/java/com/tom/storagemod/` on branch `26.2`; short refs are `CM` = `menu/CraftingTerminalMenu.java:NN`, `CS` = `screen/CraftingTerminalScreen.java:NN`, `TF` = `menu/TerminalCraftingFiller.java:NN`. (CM:34, CS:30-38, TF:22) |
| 2 | **Inputs** | Everything the plain storage terminal has (03-terminal `storage-terminal.md`), plus: the 3×3 crafting grid, recipe book, Clear Grid button, pull-from-player-inventory toggle (mode bit 0x20), `S` key over a ghost recipe slot, the recipe "fill" message (`fill` = recipe id) (CM:190). |
| 3 | **Numbers** | GUI 194×256 px. Crafting grid at x = −4+30+j×18, y = 94+17+i×18 (relative to leftPos/topPos) (CM:83-89); result slot at (120, 129) relative (x+124, y+35 with x=−4, y=94 → (120,129)) (CM:85); storage slots 5 rows from (8,18) (CM:110-111). Player slots at (8,174) (CM:63). Recipe book visible when screen width ≥ 379 px (narrower → toggleable overlay) (CS:58). Clear button 11×11 px at (leftPos+80, topPos+20+rowCount×18) (CS:64, 222). 7th button (pull-from-inv) at (leftPos−18, topPos+5+108) (CS:66; the six preceding buttons are laid out by `setButtonsPos`, CS:88-99); button column wraps to a second column (x − 18) when y+18 would exceed the available space (recipe-book search-box y − 16, else imageHeight) (CS:87, 93-96). Amount buttons and recipe book: vanilla recipe book widget (`CraftingTerminalRecipeBookWidget`, 06-client-ui). |
| 4 | **Behavior** | See Behavior detail. |
| 5 | **Determinism** | Recipe lookup deterministic (vanilla recipe manager / polymorph). Auto-fill order: grid slots in `PlaceRecipeHelper` placement order; item sources tried in this order — terminal (per matching stored item), then player inventory. |
| 6 | **Data formats** | All storage-terminal tags plus: menu buttons — id 0 = Clear Grid (`te.clear(player)`), id 1 = polymorph update (`te.polymorphUpdate(player)`) (CM:178-182). `receive` (server) reads `fill` = recipe id string (`Identifier.tryParse`); if it resolves in the player's recipe manager, a `TerminalCraftingFiller` places the recipe (CM:188-195). Crafting grid NBT: `CraftingTable` (list of items) — see 03-terminal `terminal-blocks.md`. |
| 7 | **Output** | Crafted result in the result slot; auto-filled grid (items pulled from network and/or player inventory); recipe book state; grid cleared back into the network. |
| 8 | **Relationships** | Extends `StorageTerminalMenu` / `AbstractStorageTerminalScreen` (03-terminal `storage-terminal.md`). Uses `CraftingTerminalBlockEntity` (03-terminal `terminal-blocks.md`), `CraftingMatrix` (03-terminal `terminal-utils.md`), `IAutoFillTerminal` (03-terminal `terminal-utils.md`), `TerminalCraftingFiller` (this sheet), `PolymorphHelper` (05-integration), widgets `ToggleButton`, `CraftingTerminalRecipeBookWidget` (06-client-ui). |
| 9 | **NeoForge dependencies** | `RecipeUpdateListener` (recipe book refresh), `RecipeBookComponent.RECIPE_BUTTON_SPRITES`, `PlaceRecipeHelper.placeRecipe(3, 3, recipe, slotsToIngredientIndex, callback)` (vanilla recipe-placement math; 1.7.10 has no direct counterpart — must be re-implemented), `RecipeDisplay`, `StackedItemContents`, `TransientCraftingContainer` (client menu), `ResultSlot`/`CraftingContainer`, `RecipeHolder` + `Registries.RECIPE` key lookup. |
| 10 | **Bugs** | `TERM-05` (TF:34-70; auto-fill accounts player-inventory stacks that are then pulled one-by-one with no reservation — double-claim is possible across multiple grid cells in one fill). |

## Behavior detail

### Menu

1. Constructor (server): `super(craftingTerminalMenu, id, inv, te)` → storage slots (5×9 `SlotStorage`, not real slots); then 10 real slots: index 0 = result `Result` (x=120, y=129 relative), indices 1..9 = `SlotCrafting` grid (row-major, j + i×3) (CM:82-91). `addPlayerSlots(inv, 8, 174)` (playerSlotsStart = 9) (CM:63). `te.registerCrafting(this)` (CM:64); `removed` → `te.unregisterCrafting` (CM:75-80).
2. `Result.onTake`: `checkTakeAchievements(stack)`; server side → `te.craft(player)` (consumes inputs, refills the grid from the network — see terminal-blocks step 16) (CM:100-106).
3. `addStorageSlots` override: 5 rows at (8,18) (CM:109-112).
4. `shiftClickItems` (server) (CM:119-160):
   - index 0 (result): if `!te.canCraft()` or `moveItemStackTo(stack, 10, 46, true)` (player slots, excluding offhand at 46) fails → nothing happens (CM:129-131). Else `onQuickCraft`, update the slot; if the whole stack moved, done; otherwise `slot.onTake` and **drop the remainder at the player** (`playerIn.drop(itemstack1, false)`, CM:145-148) — a shift-click that only partially fits drops the rest.
   - index 1..9 (grid): push the stack into the terminal (`te.pushStack(itemstack)`), remainder back in the grid slot, `broadcastChanges` (server) (CM:152-156).
   - `slot.onTake` runs for the grid case too (plain `Slot.onTake` — no extra effect) (CM:158).
5. `onCraftMatrixChanged` (server, per menu): sends `ClientboundContainerSetSlotPacket` for slots 0..9 (and the result) to every `ServerPlayer` listener (CM:163-173).
6. `clickMenuButton`: id 0 → `te.clear(player)`; id 1 → `te.polymorphUpdate(player)`; else super (data-slot buttons). Always returns false (CM:183-184).
7. `receive`: super (storage tags) then `fill` = recipe id → `TerminalCraftingFiller(te, player, sync).placeRecipe(recipe.value())` (CM:188-197).
8. `handlePlacement` (recipe-book "crafting" placement): `TerminalCraftingFiller.placeRecipe(...)`; returns NOTHING (the filler already mutated the grid) (CM:215-221).
9. `getStoredItems` = `itemList`; `isSmartSearch` = (searchType & 8) == 0; `getInputGridSlots` = slots 1..9 (CM:201-203, 210-212, 233-234).

### TerminalCraftingFiller (server)

10. `placeRecipe(recipe)` (TF:34): `te.clear(player)` (grid → network → player → drop) (TF:35); `sync.fillCraftingFiller(this)` (TF:36) (integration hook, e.g. AE-style auto-fill providers register here — see `IAutoFillTerminal`); account the player's non-equipment inventory stacks into `allItems` (TF:37-39) keyed by item holder (**skips stacks with a custom name component**); then `PlaceRecipeHelper.placeRecipe(3, 3, recipe, placementInfo.slotsToIngredientIndex, (idx, slotId, ...) -> ...)` (TF:41-69): for each grid cell, iterate the ingredient's allowed items in order; for each, first try the terminal — for each stored stack of that item, if the ingredient test passes, `te.pullStack(stack, 1)` and place; else the player inventory — `findSlotMatchingItem(item)`, remove 1. **`TERM-05`: accounting is per-item, not reserved per cell — two cells needing the same single item can both claim it before either pulls (player-inventory path: `findSlotMatchingItem` re-scans live inventory, so the player side is mostly safe; the terminal side pulls lazily per cell, but `allItems` lists are never decremented, so a stored quantity of 1 shared by two cells is handled because the pull itself fails the second time — the visible risk is the player-inventory `findSlotMatchingItem` matching a slot whose item was just consumed by the same fill; observed: each cell pulls exactly 1 and checks success, so this is a suspected edge, not a proven loss)**.
11. `accountStack`: skips stacks with `DataComponents.CUSTOM_NAME`; groups by `typeHolder()` (TF:71-73).

### Screen

12. `init`: super; `widthTooNarrow = width < 379` (CS:58); recipe book hidden if currently visible and the screen is narrow (auto-toggled) (UNVERIFIED: the visible book is toggled off regardless of narrowness, CS:59); recipe book initialized, screen re-positioned (`leftPos = recipeBookGui.updateScreenPosition(width, imageWidth − 16)`) (CS:61); initial focus = recipe book; Clear Grid button; pull-from-inv toggle (icons `icons/refill_off`/`icons/refill_on`) added as the 7th button in the left column; recipe book toggle button at (leftPos+4, topPos+38+rowCount×18) (CS:74-77); `setButtonsPos` lays out all 7 buttons (order: sort, direction, search type, control mode, ghost, pull-from-inv, tall mode) at 18 px spacing, wrapping to a second column at x−18 when the next button would exceed `space` (recipe-book search-box y − 16, else imageHeight); `onPacket` to sync states (CS:56-82, 84-101).
13. `onPacket`: super + `buttonPullFromInv.setState((menu.modes & 0x20) != 0)` (CS:103-108). `writeModes`: super | (on ? 0x20 : 0) (CS:110-115).
14. `onUpdateSearch` (called from the base on every search change): when `IAutoFillTerminal.hasSync()` or search-type bit 4 — set the recipe-book search box text to the search string and refresh recipes (CS:47-52).
15. `keyPressed`: `S` while hovering a slot that has a ghost-recipe ingredient → search field = that ingredient's hover name (unfocused) — quick search for a recipe ingredient; ESC when the recipe book is open in narrow mode → close the book (CS:190-212).
16. `mouseClicked`: recipe book consumes its clicks (and takes focus); in narrow mode with the book open, a click outside the GUI toggles the book closed (returns false so the slot doesn't get the click) (CS:152-164).
17. `extractRenderState`: narrow mode with the book open → base render with (−1,−1) mouse (no hover) + book render; else both with the real mouse. `slotClicked` also notifies the recipe book (CS:123-133, 175-179).
18. `fillGhostRecipe`: delegates to the recipe book widget (CS:236-239).
19. `ButtonClear` (CS:214-234): 11×11 sprite button (`widget/clear_button`/`_disabled`/`_hovered`), presses → `handleInventoryButtonClick(containerId, 0)` (CS:186-188).

## Lang keys used by this feature

- `menu.toms_storage.crafting_terminal` = "C. Terminal"
- `tooltip.toms_storage.crafting_pull_on` = "Pull missing items from players inventory when crafting"; `crafting_pull_off` = "Don't pull from players inventory when crafting"
- `tooltip.toms_storage.crafting_terminal` = "Gives access to your items.\Place it on an inventory.\Has a builtin crafting table."
- `tooltip.toms_storage.recipe_book` = "Recipe Book"
- `tooltip.toms_storage.items_missing` = "Some items may not be inserted into the crafting grid"
- Plus all plain-terminal keys (03-terminal `storage-terminal.md`).

## Deviations in the new mod

See the [03-terminal deviation table](../../new-spec/03-terminal/deviations.md#deviation-table) (row `crafting-terminal`).
