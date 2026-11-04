# System: EMI integration

| # | Field | Value |
|---|---|---|
| 1 | **Identity** | `com.tom.storagemod.emi.EMIPlugin` (`SH/emi/EMIPlugin.java`) — `EmiPlugin` with `@EmiEntrypoint` (NeoForge) and the Fabric `emi` entrypoint in `fabric.mod.json`; `com.tom.storagemod.emi.EmiGhostIngredientHandler` (`SH/emi/EmiGhostIngredientHandler.java`) — `EmiDragDropHandler<Screen>`; `com.tom.storagemod.emi.EmiTransferHandler` (`SH/emi/EmiTransferHandler.java`) — `StandardRecipeHandler<CraftingTerminalMenu>`. Sources (upstream `26.2`, `SH` = `NeoForge/src/platform-shared/java/com/tom/storagemod/`): `SH/emi/EMIPlugin.java:18-19`, `SH/emi/EmiGhostIngredientHandler.java:23`, `SH/emi/EmiTransferHandler.java:33`; build `NeoForge/build.gradle`, `Fabric/build.gradle`; `Fabric/src/main/resources/fabric.mod.json:34-36` (`emi` entrypoint) |
| 2 | **Inputs** | EMI API: `EmiRegistry` (workstation, recipe handler, generic drag-drop, generic stack provider, generic exclusion area), `EmiRecipe`/`EmiStack`/`EmiIngredient`, `StandardRecipeHandler` callbacks (input sources, crafting slots, inventory, supports/canCraft/craft, render), the drag context (screen, ingredient, x/y) |
| 3 | **Numbers** | EMI version bounds: NF `1.1.7+1.21` (only with build flag `-DuseLib`) (NeoForge/build.gradle:198-200); FB `1.1.13+1.21.1` (dependency line commented out) (Fabric/build.gradle:107-113). Ghost target rects: 16×16 px at `guiLeft + slot.x`, `guiTop + slot.y` (EmiGhostIngredientHandler.java:79). Ghost highlight fill: `0x8822BB33` (53% alpha green) (EmiGhostIngredientHandler.java:52). Missing-slot highlight fill: `0x44FF0000` (27% alpha red) (EmiTransferHandler.java:85). Missing-index remap: `width == 1 ? i * 3 : width == 2 ? (i % 2) + i / 2 * 3 : i` (`width` = `recipe.getDisplayWidth()`) (EmiTransferHandler.java:99, :125). `getInventory` reads menu slots `subList(1, size)` — slot 0 (the crafting result) is excluded (EmiTransferHandler.java:48) |
| 4 | **Behavior** | See Behavior detail |
| 5 | **Determinism** | All client-side, run by EMI. `getInventory` is a pure read of menu slot stacks + (when smart item search is on) the terminal's stored items with their quantities. The "craft" path re-opens the terminal screen via `Minecraft.setScreen` |
| 6 | **Data formats** | Auto-fill request: `CompoundTag { fill: String }` (recipe ID location as string) — **currently dead code**: the send block in `handleRecipe` is commented out (see `INT-03`) (EmiTransferHandler.java:132-137). Search sync: direct `EmiApi.setSearchText`/`getSearchText` statics, no wire format |
| 7 | **Output** | (client) Crafting terminal item as a crafting workstation; hovering the terminal focuses the hovered slot's item; ghost-drag into the filter GUI highlights valid slots green and stages ghost filters; from the EMI recipe GUI, "craft" re-opens the terminal screen (fill packet not yet sent — `INT-03`); with smart item search on, missing inputs are highlighted red in the recipe GUI |
| 8 | **Relationships** | `AbstractStorageTerminalScreen` (hover focus, smart-search flag), `IScreen.getExclusionAreas` (area `06-client-ui`), `PhantomSlot`/`FilterSlot` + `AbstractFilteredMenu.setPhantom(slot, stack)` (area `03-terminal`), `IAutoFillTerminal` (seam documented in [jei.md](jei.md)), `TerminalItemStack`/`StoredItemStack` (area `03-terminal`), `CraftingTerminalMenu` (the recipe-handler target menu), `Content.craftingTerminal`/`Content.craftingTerminalMenu` (area `01-core`), EMI itself |
| 9 | **NeoForge dependencies** | EMI API (`EmiApi`, `StandardRecipeHandler`, `EmiDragDropHandler`, `EmiRegistry`) — no 1.7.10 counterpart (S2: dropped — no EMI in the pack, see API-MAP). `@EmiEntrypoint` / Fabric `emi` entrypoint — plugin discovery with no 1.7.10 counterpart (S2: dropped — see API-MAP) |
| 10 | **Bugs** | `INT-01` (classes never compiled), `INT-02` (Fabric entrypoint for an uncompiled class), `INT-03` (craft never sends the fill packet), `INT-04` (slot 0 exclusion — verified not a bug), `INT-05` (missing-slot index mapping, shared with REI) |

## Behavior detail

All steps below are **(client)**.

### Plugin registration (`EMIPlugin`, `register(EmiRegistry)`)

1. Workstation: `registry.addWorkstation(VanillaEmiRecipeCategories.CRAFTING, EmiStack.of(Content.craftingTerminal.get()))` (EMIPlugin.java:23).
2. Recipe handler: `registry.addRecipeHandler(Content.craftingTerminalMenu.get(), new EmiTransferHandler())` (EMIPlugin.java:24).
3. Generic drag-drop handler: `EmiGhostIngredientHandler` (EMIPlugin.java:25).
4. Generic stack provider: when the focused screen is an `AbstractStorageTerminalScreen`, return an `EmiStackInteraction` for the hovered slot's item; else empty (EMIPlugin.java:26-32).
5. Generic exclusion area: for every `IScreen`, accept all rects from `getExclusionAreas` as `Bounds` (EMIPlugin.java:33-37).
6. Static initializer: registers an `EMI` `ISearchHandler` into `IAutoFillTerminal.updateSearch` (`EmiApi.setSearchText`/`getSearchText`, name `"EMI"`) (EMIPlugin.java:40-57).

### Ghost drag (`EmiGhostIngredientHandler`)

1. `dropStack`: only for `AbstractFilteredScreen` (EmiGhostIngredientHandler.java:30-41); takes the first non-empty `ItemStack` from the ingredient; if a target rect contains (x, y), `accept` → `AbstractFilteredMenu.setPhantom(slot, stack)` and return true; else false.
2. `render`: for the same screens, fill every target rect with `0x8822BB33` while the ingredient is dragged (EmiGhostIngredientHandler.java:45-54).
3. Target rules: identical to the JEI/REI handlers — `PhantomSlot` always; `FilterSlot` only for non-filter items into slots without a filter item already (EmiGhostIngredientHandler.java:56-69).

### Recipe handler (`EmiTransferHandler`, `StandardRecipeHandler<CraftingTerminalMenu>`)

1. `getInputSources` / `getCraftingSlots`: both empty (no automatic movement of player items) (EmiTransferHandler.java:35-43).
2. `getInventory(screen)`: `EmiStack` of every menu slot from index 1 upward (slot 0 = the crafting result slot is excluded — verified correct, `INT-04`), plus, when the screen is an `AbstractStorageTerminalScreen` with smart item search on, the terminal's stored items with their exact quantities (`StoredItemStack.getQuantity()`) (EmiTransferHandler.java:46-51).
3. `supportsRecipe`: category `CRAFTING` and `recipe.supportsRecipeTree()` (EmiTransferHandler.java:54-57).
4. `canCraft`: the computed inventory `canCraft(recipe)` (EMI's own check against the inventory from step 2) (EmiTransferHandler.java:59-62).
5. `craft(recipe, context)`: call `handleRecipe(recipe, screen, simulate=false)`, then re-open the terminal screen (`Minecraft.getInstance().setScreen(screen)`); always returns `true` (EmiTransferHandler.java:64-70).
6. `handleRecipe(recipe, screen, simulate)`:
   1. Build `ItemStack[][]` from `recipe.getInputs()` (non-empty stacks only) (EmiTransferHandler.java:95-97).
   2. Missing computation: for each non-empty input alternative list, "found" if any stack is in the player inventory, else if any has a matching `TerminalItemStack` in the terminal's stored items; otherwise add the remapped index `width == 1 ? i * 3 : width == 2 ? (i % 2) + i / 2 * 3 : i` (`width = recipe.getDisplayWidth()`) (EmiTransferHandler.java:104-129).
   3. If not simulating: the fill-send block (`CompoundTag { fill: <recipe ID location> }` → `term.sendMessage`) is **commented out** — `INT-03` (EmiTransferHandler.java:132-137).
7. `render(recipe, context, widgets, matrices)`: only when the open screen is an `AbstractStorageTerminalScreen` with smart item search on; `missing = handleRecipe(recipe, screen, true)`; walk widgets; for `SlotWidget`s in order (the counter increments only for `SlotWidget`s), if the widget's own recipe is `null` and its stack is non-empty and its sequential index is in `missing`, fill its bounds with `0x44FF0000` (EmiTransferHandler.java:72-90).

## Deviations in the new mod

None (original spec only; the port likely drops EMI entirely — see [API-MAP](../API-MAP.md)).
