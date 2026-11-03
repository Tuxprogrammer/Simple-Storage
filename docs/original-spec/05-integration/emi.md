# System: EMI integration

| # | Field | Value |
|---|---|---|
| 1 | **Identity** | `com.tom.storagemod.emi.EMIPlugin` (`SH/emi/EMIPlugin.java`) — `EmiPlugin` with `@EmiEntrypoint` (NeoForge) and the Fabric `emi` entrypoint in `fabric.mod.json`; `com.tom.storagemod.emi.EmiGhostIngredientHandler` (`SH/emi/EmiGhostIngredientHandler.java`) — `EmiDragDropHandler<Screen>`; `com.tom.storagemod.emi.EmiTransferHandler` (`SH/emi/EmiTransferHandler.java`) — `StandardRecipeHandler<CraftingTerminalMenu>` |
| 2 | **Inputs** | EMI API: `EmiRegistry` (workstation, recipe handler, generic drag-drop, generic stack provider, generic exclusion area), `EmiRecipe`/`EmiStack`/`EmiIngredient`, `StandardRecipeHandler` callbacks (input sources, crafting slots, inventory, supports/canCraft/craft, render), the drag context (screen, ingredient, x/y) |
| 3 | **Numbers** | EMI version bounds: NF `1.1.7+1.21` (only with build flag `-DuseLib`); FB `1.1.13+1.21.1` (dependency line commented out). Ghost target rects: 16×16 px at `guiLeft + slot.x`, `guiTop + slot.y`. Ghost highlight fill: `0x8822BB33` (53% alpha green). Missing-slot highlight fill: `0x44FF0000` (27% alpha red). Missing-index remap: `width == 1 ? i * 3 : width == 2 ? (i % 2) + i / 2 * 3 : i` (`width` = `recipe.getDisplayWidth()`). `getInventory` reads menu slots `subList(1, size)` — slot 0 (the crafting result) is excluded |
| 4 | **Behavior** | See Behavior detail |
| 5 | **Determinism** | All client-side, run by EMI. `getInventory` is a pure read of menu slot stacks + (when smart item search is on) the terminal's stored items with their quantities. The "craft" path re-opens the terminal screen via `Minecraft.setScreen` |
| 6 | **Data formats** | Auto-fill request: `CompoundTag { fill: String }` (recipe ID location as string) — **currently dead code**: the send block in `handleRecipe` is commented out (see `INT-03`). Search sync: direct `EmiApi.setSearchText`/`getSearchText` statics, no wire format |
| 7 | **Output** | (client) Crafting terminal item as a crafting workstation; hovering the terminal focuses the hovered slot's item; ghost-drag into the filter GUI highlights valid slots green and stages ghost filters; from the EMI recipe GUI, "craft" re-opens the terminal screen (fill packet not yet sent — `INT-03`); with smart item search on, missing inputs are highlighted red in the recipe GUI |
| 8 | **Relationships** | `AbstractStorageTerminalScreen` (hover focus, smart-search flag), `IScreen.getExclusionAreas` (area `06-client-ui`), `PhantomSlot`/`FilterSlot` + `AbstractFilteredMenu.setPhantom(slot, stack)` (area `03-terminal`), `IAutoFillTerminal` (seam documented in [jei.md](jei.md)), `TerminalItemStack`/`StoredItemStack` (area `03-terminal`), `CraftingTerminalMenu` (the recipe-handler target menu), `Content.craftingTerminal`/`Content.craftingTerminalMenu` (area `01-core`), EMI itself |
| 9 | **NeoForge dependencies** | EMI API (`EmiApi`, `StandardRecipeHandler`, `EmiDragDropHandler`, `EmiRegistry`) — no 1.7.10 counterpart (pending S2; the port target is likely JEI only). `@EmiEntrypoint` / Fabric `emi` entrypoint — plugin discovery with no 1.7.10 counterpart (pending S2) |
| 10 | **Bugs** | `INT-01` (classes never compiled), `INT-02` (Fabric entrypoint for an uncompiled class), `INT-03` (craft never sends the fill packet), `INT-04` (slot 0 exclusion — verified not a bug), `INT-05` (missing-slot index mapping, shared with REI) |

## Behavior detail

All steps below are **(client)**.

### Plugin registration (`EMIPlugin`, `register(EmiRegistry)`)

1. Workstation: `registry.addWorkstation(VanillaEmiRecipeCategories.CRAFTING, EmiStack.of(Content.craftingTerminal.get()))`.
2. Recipe handler: `registry.addRecipeHandler(Content.craftingTerminalMenu.get(), new EmiTransferHandler())`.
3. Generic drag-drop handler: `EmiGhostIngredientHandler`.
4. Generic stack provider: when the focused screen is an `AbstractStorageTerminalScreen`, return an `EmiStackInteraction` for the hovered slot's item; else empty.
5. Generic exclusion area: for every `IScreen`, accept all rects from `getExclusionAreas` as `Bounds`.
6. Static initializer: registers an `EMI` `ISearchHandler` into `IAutoFillTerminal.updateSearch` (`EmiApi.setSearchText`/`getSearchText`, name `"EMI"`).

### Ghost drag (`EmiGhostIngredientHandler`)

1. `dropStack`: only for `AbstractFilteredScreen`; takes the first non-empty `ItemStack` from the ingredient; if a target rect contains (x, y), `accept` → `AbstractFilteredMenu.setPhantom(slot, stack)` and return true; else false.
2. `render`: for the same screens, fill every target rect with `0x8822BB33` while the ingredient is dragged.
3. Target rules: identical to the JEI/REI handlers — `PhantomSlot` always; `FilterSlot` only for non-filter items into slots without a filter item already.

### Recipe handler (`EmiTransferHandler`, `StandardRecipeHandler<CraftingTerminalMenu>`)

1. `getInputSources` / `getCraftingSlots`: both empty (no automatic movement of player items).
2. `getInventory(screen)`: `EmiStack` of every menu slot from index 1 upward (slot 0 = the crafting result slot is excluded — verified correct, `INT-04`), plus, when the screen is an `AbstractStorageTerminalScreen` with smart item search on, the terminal's stored items with their exact quantities (`StoredItemStack.getQuantity()`).
3. `supportsRecipe`: category `CRAFTING` and `recipe.supportsRecipeTree()`.
4. `canCraft`: the computed inventory `canCraft(recipe)` (EMI's own check against the inventory from step 2).
5. `craft(recipe, context)`: call `handleRecipe(recipe, screen, simulate=false)`, then re-open the terminal screen (`Minecraft.getInstance().setScreen(screen)`); always returns `true`.
6. `handleRecipe(recipe, screen, simulate)`:
   1. Build `ItemStack[][]` from `recipe.getInputs()` (non-empty stacks only).
   2. Missing computation: for each non-empty input alternative list, "found" if any stack is in the player inventory, else if any has a matching `TerminalItemStack` in the terminal's stored items; otherwise add the remapped index `width == 1 ? i * 3 : width == 2 ? (i % 2) + i / 2 * 3 : i` (`width = recipe.getDisplayWidth()`).
   3. If not simulating: the fill-send block (`CompoundTag { fill: <recipe ID location> }` → `term.sendMessage`) is **commented out** — `INT-03`.
7. `render(recipe, context, widgets, matrices)`: only when the open screen is an `AbstractStorageTerminalScreen` with smart item search on; `missing = handleRecipe(recipe, screen, true)`; walk widgets; for `SlotWidget`s in order (the counter increments only for `SlotWidget`s), if the widget's own recipe is `null` and its stack is non-empty and its sequential index is in `missing`, fill its bounds with `0x44FF0000`.

## Deviations in the new mod

None (original spec only; the port likely drops EMI entirely — see [API-MAP](../API-MAP.md)).
