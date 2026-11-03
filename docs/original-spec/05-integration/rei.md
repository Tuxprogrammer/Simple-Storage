# System: REI integration

| # | Field | Value |
|---|---|---|
| 1 | **Identity** | `com.tom.storagemod.rei.REIPlugin` (`SH/rei/REIPlugin.java`) — `REIClientPlugin` (architectury-based REI API); loader shims: `com.tom.storagemod.rei.REIPluginLoader` (`NF/rei/REIPluginLoader.java`, `@REIPluginLoaderClient`, NeoForge only) and the Fabric entrypoint `rei_client` → `REIPlugin` in `Fabric/.../fabric.mod.json`; `com.tom.storagemod.rei.ReiGhostIngredientHandler` (`SH/rei/ReiGhostIngredientHandler.java`) — `DraggableStackVisitor<AbstractFilteredScreen>`; `com.tom.storagemod.rei.ReiTransferHandler` (`SH/rei/ReiTransferHandler.java`) — `TransferHandler` |
| 2 | **Inputs** | REI API: `TransferHandlerRegistry`, `CategoryRegistry`, `ScreenRegistry`, `Display`/`SimpleGridMenuDisplay`/`EntryStack` from the recipe GUI, the drag context (screen + dragged `EntryStack` + mouse position), `REIRuntime` (search text field) |
| 3 | **Numbers** | REI version bound: `26.2.820` on both loaders (NF: `implementation` — **bundled in the NeoForge jar**; FB: `compileOnlyApi` + `runtimeOnly`). Ghost target rects: 16×16 px at `guiLeft + slot.x`, `guiTop + slot.y`. Missing-slot highlight fill: `0x40ff0000` (25% alpha red) over the widget's inner bounds. Result color on missing: `0x67aaaa00`. Missing-index remap: `width == 1 ? i * 3 : width == 2 ? (i % 2) + i / 2 * 3 : i` (`width` = display grid width for `SimpleGridMenuDisplay`, else `Integer.MAX_VALUE` → identity) |
| 4 | **Behavior** | See Behavior detail |
| 5 | **Determinism** | All client-side, run by REI when it renders/queries. Focused-stack lookup is a direct `getHoveredSlot()` read (no server round trip). The transfer "missing" computation is a pure function of player inventory + `IAutoFillTerminal.getStoredItems()` + the display's input entries |
| 6 | **Data formats** | Auto-fill request: `CompoundTag { fill: String }` where the string is the display location (`recipe.getDisplayLocation()` → `ResourceLocation` → `toString()`), sent through `IAutoFillTerminal.sendMessage(tag)` (server-side handling in `CraftingTerminalMenu`, area `03-terminal`). Search sync has no wire format (direct `REIRuntime` text field access) |
| 7 | **Output** | (client) Crafting terminal item shown as a crafting workstation in REI; hovering the terminal screen focuses the hovered slot's item; dragging an item into the filter GUI highlights valid `PhantomSlot`/`FilterSlot` targets and stages a ghost filter; from the REI recipe GUI: "craft" fills the terminal crafting grid (server), a colored result with red-highlighted missing slots when smart item search is on and items are missing |
| 8 | **Relationships** | `AbstractStorageTerminalScreen.getHoveredSlot()` and `isSmartItemSearchOn()` (area `03-terminal`), `IScreen.getExclusionAreas` (area `06-client-ui`), `PhantomSlot`/`FilterSlot` + `AbstractFilteredMenu.setPhantom(slot, stack)` (area `03-terminal`), `IAutoFillTerminal` + `ISearchHandler` (seam documented in [jei.md](jei.md)), `TerminalItemStack`/`StoredItemStack` (area `03-terminal`), `Content.craftingTerminal` (area `01-core`), REI itself (runtime) |
| 9 | **NeoForge dependencies** | REI API (modrinth/curse REI for modern MC — no 1.7.10 counterpart; the 1.7.10 port target is likely JEI only (S2: REI dropped — no REI in the pack, see API-MAP)). `REIPluginProvider`/`REIPluginLoaderClient` (NeoForge REI loader) / Fabric `rei_client` entrypoint — plugin discovery with no 1.7.10 counterpart (S2: dropped — see API-MAP). `SimpleGridMenuDisplay`, `CategoryIdentifier.of("minecraft", "plugins/crafting")`, `ProgressView`-free (uses custom renderer lambda). Uses the mod's own screen/menu/slot classes |
| 10 | **Bugs** | `INT-05` (missing-slot index mapping heuristic). No other REI-specific bugs |

## Behavior detail

All steps below are **(client)**.

### Plugin registration (`REIPlugin`, `REIClientPlugin`)

1. `registerTransferHandlers`: registers `ReiTransferHandler` as the transfer handler.
2. `registerCategories`: `registry.addWorkstations(BuiltinPlugin.CRAFTING, EntryStacks.of(Content.craftingTerminal.get()))` — the crafting terminal item marks crafting recipes as craftable in this mod's terminal.
3. `registerScreens`:
   1. **Focused stack**: when the focused screen is an `AbstractStorageTerminalScreen`, return the `EntryStack` of the hovered slot's item (`getHoveredSlot().getItem()`) — REI's "focused item" feature follows the slot the cursor is over on the terminal; otherwise pass.
   2. **Draggable stack visitor**: `ReiGhostIngredientHandler` (below).
   3. **Exclusion zones**: for every `IScreen` (all TSS screens), `getExclusionAreas` is collected as REI rectangles — REI does not open recipe GUIs on clicks inside them.
4. Static initializer: registers a `REI` `ISearchHandler` into `IAutoFillTerminal.updateSearch`:
   - `setSearch(text)`: `REIRuntime.getInstance().getSearchTextField().setText(text)` (no null guard — NPE if REI runtime absent, i.e. REI not loaded; the class is only compiled when REI is on the classpath, but the runtime can still be null pre-init).
   - `getSearch()`: returns the search field text (no null guard).
   - Name: `"REI"`.

### Ghost drag (`ReiGhostIngredientHandler`)

1. Handles screens that are `AbstractFilteredScreen`.
2. `getTargets(gui, ingredient)`: for an `ITEM`-typed entry, for each menu slot: `PhantomSlot` → always a target; `FilterSlot` → target iff the dragged item is **not** an `IItemFilter` and the slot does not already hold a filter item; other slots → none. Target rect is the 16×16 slot rect.
3. `acceptDraggedStack`: if the current mouse position is inside a target rect, `accept` → `AbstractFilteredMenu.setPhantom(slot, stack)`; else pass.

### Transfer (`ReiTransferHandler`)

1. `context.getMenu()` must implement `IAutoFillTerminal`, else `notApplicable`.
2. Not applicable unless the display category is `minecraft:plugins/crafting` **and** the currently open screen is *not* the container's own screen (i.e. the player is looking at the recipe GUI, not the terminal).
3. If the container screen is an `AbstractStorageTerminalScreen` and the display is not "actually crafting" and smart item search is **off** → return a successful no-op that blocks further handlers (nothing to do, and no warning shown).
4. Build `ItemStack[][]` from the display's input entries (only `ItemStack`-typed values, nulls filtered).
5. Missing computation (shared shape with EMI, see [emi.md](emi.md)): for each non-empty input alternative list, "found" if any stack is in the player inventory (`findSlotMatchingItem`), else if any stack has a matching `TerminalItemStack` in the terminal's stored items; otherwise add the remapped index `width == 1 ? i * 3 : width == 2 ? (i % 2) + i / 2 * 3 : i`.
6. If `context.isActuallyCrafting()` (the REI "craft" button): if the display has a location, send `CompoundTag { fill: <location string> }` via `sendMessage` and return a successful blocking result (the terminal grid is filled server-side).
7. Else if `missing` is non-empty: return a successful result with color `0x67aaaa00`, non-blocking, plus a renderer that walks the recipe widgets in order; for widgets that are `Slot` with notice mark `INPUT` only (the counter increments once per INPUT slot), fill the slot's inner bounds with `0x40ff0000` when the sequential INPUT-slot index is in `missing`.
8. Otherwise: successful blocking result (nothing missing).

## Deviations in the new mod

None (original spec only; port likely drops REI — see [API-MAP](../API-MAP.md)).
