# System: JEI integration

| # | Field | Value |
|---|---|---|
| 1 | **Identity** | `com.tom.storagemod.jei.JEIHandler` (`SH/jei/JEIHandler.java`) — the `@JeiPlugin` class, plugin UID `toms_storage:jei`; `com.tom.storagemod.jei.JeiGhostIngredientHandler` (`SH/jei/JeiGhostIngredientHandler.java`) — `IGhostIngredientHandler<AbstractFilteredScreen>`; `com.tom.storagemod.jei.CraftingTerminalTransferHandler` (`SH/jei/CraftingTerminalTransferHandler.java`) — `IRecipeTransferHandler<CraftingTerminalMenu, RecipeHolder<CraftingRecipe>>` |
| 2 | **Inputs** | The JEI API: `IModPlugin` lifecycle callbacks, `IJeiRuntime` (ingredient filter for search sync), `IIngredient`/`IRecipe`/`RecipeHolder` from the recipe GUI, `IContainerSlotView` for slot roles, the open screen (`AbstractFilteredScreen` / `CraftingTerminalScreen` / any `PlatformContainerScreen`), the ghost-dragged `IIngredient` stack |
| 3 | **Numbers** | Clickable recipe area: `createBasic(83, 35 + rowCount * 18, 28, 23)` px, screen-relative (recomputed on every JEI query, so tall terminals (`rowCount` 5..10) move it down 18 px per extra row). Ghost target rects: 16×16 px at `guiLeft + slot.x`, `guiTop + slot.y`. JEI version bound: `30.7.0.41` on both loaders (NF: `compileOnly` API + `runtimeOnly` full jar in `NeoForge/build.gradle`; FB: `compileOnlyApi` + `runtimeOnly` in `Fabric/gradle.properties`). Single static `TransferWarning`/error instances in the transfer handler (never recreated) |
| 4 | **Behavior** | See Behavior detail |
| 5 | **Determinism** | All client-side. `registerGuiHandlers`/`addGuiHandlers` are called by JEI during its setup (client). Search sync runs on the main thread whenever a TSS screen calls `IAutoFillTerminal.syncSearch()` (the terminal screens call it in `tick`/on open). The auto-fill "missing" computation is a pure function of the player inventory contents + the terminal's `getStoredItems()` + the recipe's input stacks |
| 6 | **Data formats** | Auto-fill request: a `CompoundTag` with one `String` field `fill` = the recipe's registry ID, sent client→server through `IAutoFillTerminal.sendMessage(tag)` (the server-side handling lives in `CraftingTerminalMenu`, area `03-terminal`). JEI `TransferWarning` of type `COSMETIC` for missing items, message key `tooltip.toms_storage.items_missing` |
| 7 | **Output** | (client) JEI shows recipe buttons/areas on TSS screens; ghost-drag highlights valid filter slots; the recipe GUI's "auto-fill" button fills the crafting grid (or warns about missing items); JEI's ingredient-filter text mirrors the terminal search field; the crafting terminal item appears as a crafting-station catalyst in JEI |
| 8 | **Relationships** | Depends on `AbstractFilteredScreen` (ghost targets + exclusion areas), `PlatformContainerScreen` (exclusion areas on all TSS screens), `PhantomSlot`/`FilterSlot` (target rules), `IAutoFillTerminal` + `ISearchHandler` (search sync seam, `SH/util/IAutoFillTerminal.java`), `CraftingTerminalMenu` (transfer target; `Content.craftingTerminalMenu` for the menu type), `Content.craftingTerminal` (catalyst), `TerminalItemStack` (stored-item comparison), the JEI mod itself (runtime discovery) |
| 9 | **NeoForge dependencies** | JEI API (mod discovery via `@JeiPlugin` — no 1.7.10 analog by the same name; 1.7.10 JEI 4.x uses a different `IModPlugin`-family API (S2: no JEI in the pack — the target is NEI, see API-MAP)). `IGuiContainerHandler` (clickable areas, exclusion areas), `IGhostIngredientHandler`, `IRecipeTransferHandler`, `IJeiRuntime`, `RecipeTypes.CRAFTING` — all JEI-side APIs with 1.7.10 counterparts of the same intent (S2: exact classes in API-MAP). Uses the mod's own menu/screen/slot classes (area `03-terminal`) |
| 10 | **Bugs** | `INT-10` (clickable area position). See also the shared missing-slot logic note `INT-05` (REI/EMI only — JEI has no slot highlighting) |

## Behavior detail

All steps below are **(client)** unless labeled otherwise; `(server)` is used where the handler runs on the server.

### Registration (`JEIHandler`, `@JeiPlugin`)

1. `registerGuiHandlers`:
   1. For `CraftingTerminalScreen`: a clickable recipe area for `RecipeTypes.CRAFTING` at screen-relative rect `(83, 35 + rowCount * 18, 28, 23)`, where `rowCount` is the screen's current terminal row count (5 for normal terminals, up to 10 for tall terminals). Clicking the area opens the crafting recipes (JEI). The rect is recomputed on every JEI query, so it tracks tall/normal mode.
   2. For `AbstractFilteredScreen` (the item-filter and tag-filter GUIs): ghost-ingredient drag handling delegated to `JeiGhostIngredientHandler`.
   3. For `PlatformContainerScreen` (the base of every TSS screen): exclusion areas from `IScreen.getExclusionAreas` — JEI will not open recipe GUIs when clicking inside those rects (e.g. button columns).
2. `addRecipes`: none (no custom recipes).
3. `addCatalysts`: `Content.craftingTerminal` item added as a crafting-station catalyst for `RecipeTypes.CRAFTING`.
4. Static initializer: registers a `JEI` `ISearchHandler` into `IAutoFillTerminal.updateSearch`:
   - `setSearch(String)`: if `jeiRuntime` is non-null, `jeiRuntime.getIngredientFilter().setFilterText(text)` (null text clears).
   - `getSearch()`: `jeiRuntime.getIngredientFilter().getFilterText()`; returns `""` while the JEI runtime is null (JEI not started yet).
   - Name: `"JEI"`.

### Ghost drag (`JeiGhostIngredientHandler`)

1. `getTargets(screen, ingredient)`: for each slot of `screen.getMenu()`:
   - `PhantomSlot` → always a target.
   - `FilterSlot` → a target iff the dragged ingredient is **not** a TSS filter item **and** the slot does not already contain a filter item.
   - Any other slot → not a target.
   - Each target is the 16×16 px rect of the slot.
2. `accept`: the target's `accept(ItemStack)` calls `AbstractFilteredMenu.setPhantom(slot, stack)` on the specific `Slot` object — the ghost item is staged in that slot and committed to the filter on drop (the commit logic is in area `03-terminal`). `onComplete` is a no-op.

### Auto-fill (`CraftingTerminalTransferHandler`)

1. JEI invokes `transferRecipe(container, recipeHolder, slotViews, player, maxTransfer, doTransfer)` from the recipe GUI's fill button; `doTransfer` is false for a preview click.
2. `container` must implement `IAutoFillTerminal`; otherwise returns the shared `INTERNAL` error (`TransferWarning` with a hardcoded "internal" message) — this happens if the player opens the recipe GUI for a different container.
3. If `!container.isSmartSearch()` and `!doTransfer` → return `null` (no preview, no work).
4. Build the set of terminal stored items: `container.getStoredItems()` into a `HashSet<TerminalItemStack>` (equality covers item + count-independent match — see `03-terminal` for the exact semantics).
5. For each `IContainerSlotView` whose role is `INPUT` or `CRAFTING_STATION`:
   1. Take the slot's ingredient alternatives (all stacks the recipe accepts there).
   2. If the list is empty → no input requirement, skip.
   3. "Found" if **any** alternative: is present in the player inventory (`findSlotMatchingItem(alt, 1) != -1`) **or** has a matching `TerminalItemStack` in the stored set.
   4. Otherwise add that slot's index to the `missing` list.
6. If `doTransfer`: build `CompoundTag { fill: <recipe registry ID as string> }` and call `container.sendMessage(tag)` — the server-side `CraftingTerminalMenu` then fills the 3×3 grid (area `03-terminal`).
7. Return: `null` if no missing items; else the shared `TransferWarning` of type `COSMETIC` wrapping the JEI missing-slots error with message key `tooltip.toms_storage.items_missing`.

### Search sync

- The terminal screens call `IAutoFillTerminal.syncSearch()` on open/tick; the handler list is iterated in registration order (JEI, REI, EMI each register one). Only the handler whose mod is loaded does anything; the others are no-ops or guarded.

## Deviations in the new mod

JEI is not in the pack; NEI per D11. (Upstream-only wording: the original spec said the port may drop EMI/REI and keep JEI; that is superseded.)
