# Terminal utilities (auto-fill, data receiver, validity, crafting matrix, keys, number format)

| # | Field | Contents |
|---|---|---|
| 1 | **Identity** | `IAutoFillTerminal` (SH `util/IAutoFillTerminal.java`, incl. `ISearchHandler`), `IDataReceiver` (SH `util/IDataReceiver.java`), `IValidInfo` (SH `util/IValidInfo.java`), `CraftingMatrix` (SH `util/CraftingMatrix.java`), `KeyUtil` (SH `util/KeyUtil.java`), `NumberFormatUtil` (SH `util/NumberFormatUtil.java`). |
| 2 | **Inputs** | `IAutoFillTerminal`: search strings, registered search handlers. `IDataReceiver`: `ValueInput` NBT tags. `IValidInfo`: object validity state. `CraftingMatrix`: item sets + change callback. `KeyUtil`: GLFW key state. `NumberFormatUtil`: non-negative longs. |
| 3 | **Numbers** | `NumberFormatUtil`: division base 1000, postfixes "KMGTPE", display width 4 chars, decimal format `.#;0.#` with RoundingMode.DOWN, decimal separator ".". |
| 4 | **Behavior** | See Behavior detail. |
| 5 | **Determinism** | `NumberFormatUtil` is deterministic. `KeyUtil` reads live key state (nondeterministic by nature). |
| 6 | **Data formats** | `IAutoFillTerminal`: no own format — it receives search strings and exposes stored items (`List<TerminalItemStack>`). `IDataReceiver.receive(ValueInput)` — the mod's generic NBT callback (01-core `network.md`). `CraftingMatrix` implements `CraftingContainer` (vanilla interface). |
| 7 | **Output** | Auto-fill search propagation; data callbacks; the 3×3 crafting matrix used by the crafting terminal; formatted number strings. |
| 8 | **Relationships** | `IAutoFillTerminal` — implemented by `CraftingTerminalMenu` (03-terminal `crafting-terminal.md`); search handlers are registered by integrations (05-integration) and 06-client-ui; `sync(searchString)` is called by `AbstractStorageTerminalScreen.updateSearch` when search-type bit 4 is set. `IDataReceiver` — implemented by all data-driven menus/screens (03-terminal, 01-core `network.md` dispatch). `IValidInfo` — implemented by `StorageTerminalBlockEntity` and passed to `NetworkInventory.onLoad` (03-terminal `terminal-blocks.md`, `inventory-access.md`). `CraftingMatrix` — used by `CraftingTerminalBlockEntity` (03-terminal `terminal-blocks.md`). `KeyUtil` — used by `LevelEmitterScreen` (03-terminal `level-emitter-gui.md`) and terminal screens. `NumberFormatUtil` — used by the terminal screen for quantities and stat tooltips. |
| 9 | **NeoForge dependencies** | `ValueInput` (1.7.10: `NBTTagCompound`), `CraftingContainer` interface (1.7.10: `IRecipeHandler`-era crafting inventory — no direct counterpart, vanilla `SlotCrafting`-style container), `NonNullList`, `ContainerHelper`, `GLFW` key queries guarded by `RenderSystem.isOnRenderThread()` (client-only; returns false off the render thread), `RenderPipelines` not used. |
| 10 | **Bugs** | None suspected. |

## Behavior detail

### IAutoFillTerminal

1. Static registry `updateSearch: List<ISearchHandler>` — integrations add handlers; `hasSync()` = non-empty; `sync(text)` pushes the search string to all handlers; `getHandlerName()` = comma-joined handler names (used in tooltips, e.g. `tooltip.toms_storage.opt.search_sync` = "Sync search with %s: %s"); `getHandlerNameOr(text)` falls back when no handlers.
2. Menu side: `sendMessage(CompoundTag)`, `getStoredItems()` (the terminal's item list), `isSmartSearch()` ((searchType & 8) == 0).

### IDataReceiver / IValidInfo

3. `receive(ValueInput tag)` — the single callback for NBT payloads (server or client side, decided by the caller — 01-core `network.md`).
4. `IValidInfo.isObjectValid()` — validity probe used by `NetworkInventory` (03-terminal `inventory-access.md`).

### CraftingMatrix

5. `CraftingContainer` over a `NonNullList<ItemStack>` of size width×height; `setChanged()` runs the `onChanged` callback (the crafting terminal wires it to recipe recompute + `setChanged`); `removeItem(i, j)` marks changed only when something was removed; `stillValid` always true; `getItems()` returns a copy.

### KeyUtil

6. `hasControlDown()` / `hasShiftDown()`: true only when `RenderSystem.isOnRenderThread()` **and** the left control/shift GLFW key is down (client-only helpers; false elsewhere — callers must be on the render thread).

### NumberFormatUtil

7. `formatNumber(long)` (asserts ≥ 0): if the decimal string is ≤ 4 chars, return as-is. Otherwise repeatedly divide by 1000 while the (base/1000)-derived size exceeds 4, tracking the last pre-division value and the postfix exponent (K, M, G, T, P, E). Then two candidates: with one decimal place (`format(last/1000) + postfix`, RoundingMode.DOWN) and without (`Long.toString(base) + postfix`); return the with-precision form if it fits in 4 chars, else the plain form (asserts ≤ 4). Examples: 1234 → "1.2K"; 9999 → "9999"; 15000 → "15K" (15.0K is 5 chars → plain "15K"); 1234567 → "1.2M".

## Deviations in the new mod

See the [03-terminal deviation table](../../new-spec/03-terminal/index.md#deviation-table) (row `terminal-utils`).
