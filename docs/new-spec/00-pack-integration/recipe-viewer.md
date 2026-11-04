# Recipe viewer: NEI 2.8.171-GTNH plugin API and the upstream → 1.7.10 mapping

Verified 2026-11-03 from source at the pack's tags (git-ignored `reference/src/NEI` at `2.8.171-GTNH`, `reference/src/AE2` at `rv3-beta-1095-GTNH`); S3's [integration-opportunities.md](integration-opportunities.md) is the research base. This sheet settles S2-Q4 ([OPEN-QUESTIONS](../OPEN-QUESTIONS.md)).

## The NEI plugin surface (1.7.10, tag 2.8.171-GTNH)

**Plugin discovery is by ASM class scan, not mcmodinfo** (`codechicken/nei/ClientHandler.java#L317-L346`, `loadPluginsList`):

- Every class that **implements `codechicken.nei.api.IConfigureNEI`** *and* whose simple name **starts with `NEI` and ends with `Config`** is loaded (ClientHandler.java#L322-L323 — the scan predicate is literally `test.startsWith("NEI") && test.endsWith("Config.class")`; with jarjar loaded, any `IConfigureNEI` implementation is loaded).
- So our plugin is a class `com.tuxprogrammer.simplestorage.compat.nei.NEISimpleStorageConfig implements IConfigureNEI` (the `IConfigureNEI` interface: `loadConfig()`, `getName()`, `getVersion()` — `codechicken/nei/api/IConfigureNEI.java`). **No manifest entry, no service loader.**

**Registration surface — static methods on `codechicken.nei.api.API`** (`codechicken/nei/api/API.java`, tag 2.8.171-GTNH):

| Method (API.java line) | Purpose |
|---|---|
| `registerRecipeHandler(ICraftingHandler)` (#L61) | recipe ("crafting" side) handler |
| `registerUsageHandler(IUsageHandler)` (#L70) | usage ("where is it used") handler |
| `registerGuiOverlay(Class, String[, x, y])` (#L80-L103) | mount NEI's recipe button over a mod GUI |
| `registerGuiOverlayHandler(Class, IOverlayHandler[, …])` (#L113) | custom recipe-panel rendering over a GUI |
| `registerNEIGuiHandler(INEIGuiHandler)` (#L138) | GUI mouse handling (click-to-move, drag) |
| `addOption(Option)` (#L236) | NEI config options |
| `addLayoutStyle(int, LayoutStyle)` (#L246) | custom recipe layouts |
| `addInfiniteItemHandler(IInfiniteItemHandler)` (#L255) | "infinite" items (catalysts) |
| `registerHighlightHandler(IHighlightHandler, ItemInfo.Layout…)` (#L285) | recipe highlighting |
| `setAliases(ItemStack, String…)` (#L186) | search aliases |
| `hideItem(ItemStack)` | hide items from the NEI list |

**Handler interfaces** (`codechicken/nei/recipe/`): `ICraftingHandler` (ICraftingHandler.java#L8) and `IUsageHandler` (IUsageHandler.java#L8) both extend `IRecipeHandler`; `TemplateRecipeHandler` (TemplateRecipeHandler.java#L52) implements both and is the base class for machine-style recipe panels built on `codechicken.nei.PositionedStack`. GUI-side interfaces: `codechicken.nei.api.INEIGuiHandler`, `INEIGuiAdapter` (the abstract adapter AE2-style handlers extend), `IOverlayHandler`, `IBookmarkContainerHandler`, `IInfiniteItemHandler`, `IHighlightHandler` (all in `codechicken/nei/api/`). Fast transfer: `codechicken.nei.FastTransferManager` (shift-click transfer over a GUI).

**Pack precedents (the intended usage patterns):**

- **GregTech:** `gregtech.nei.NEIGTConfig implements IConfigureNEI` (GT5-Unofficial 5.09.54.265, `src/main/java/gregtech/nei/NEIGTConfig.java#L57`) registers GT's machine recipe handlers, usage handlers, a custom search parser and NEI options. This is the pattern our assembler recipes will sit next to in NEI (GT's `gtneioreplugin` sub-mod renders the ore-sheet categories).
- **Applied Energistics 2 (the storage-mod precedent):** `appeng.integration.modules.NEI` (AE2 rv3-beta-1095-GTNH, `src/main/java/appeng/integration/modules/NEI.java`) wires, in `NEIHelpers/`:
  - `NEICellViewHandler implements IUsageHandler` (NEICellViewHandler.java#L35) — renders **ME cell contents into NEI's usage panel** as `PositionedStack`s of the cell's `IAEStack`s;
  - `NEIPatternViewHandler implements IUsageHandler` (NEIPatternViewHandler.java#L36) — same for pattern providers;
  - `NEIGuiHandler extends INEIGuiAdapter` (NEIGuiHandler.java) — drag-and-drop into the ME terminal's text field, recipe handling over the ME GUIs;
  - `NEIInputHandler implements IContainerInputHandler` (NEIInputHandler.java#L31);
  - `NEIAETerminalBookmarkContainerHandler implements IBookmarkContainerHandler` (NEIAETerminalBookmarkContainerHandler.java#L14) — plugs the ME terminal into NEI bookmarks.

  This is the closest in-pack analogue of upstream's "show what the terminal holds" categories.

## Mapping table: upstream (area `05-integration`) → 1.7.10 GTNH

Per S3's mapping in [integration-opportunities.md](integration-opportunities.md), refined here with the verified classes. **REI and EMI are dropped** (no 1.7.10 builds in the pack — S3: "no JEI, REI or EMI in the pack"); **Jade maps to Waila** (S3-verified Waila 1.19.34 API).

| Upstream surface | 1.7.10 GTNH target | Evidence |
|---|---|---|
| JEI `IModPlugin`/`@JeiPlugin` plugin discovery | `NEISimpleStorageConfig implements codechicken.nei.api.IConfigureNEI` (auto-discovered by name scan; must start `NEI`, end `Config`) | NEI ClientHandler.java#L317-L346, IConfigureNEI.java; GT `NEIGTConfig` precedent |
| JEI `IGuiContainerHandler` (clickable/exclusion areas on TSS screens) | `API.registerGuiOverlay(...)` + `API.registerGuiOverlayHandler(...)` over our `GuiContainer`s; mouse handling via `API.registerNEIGuiHandler(INEIGuiHandler)` | NEI API.java#L80-L138; AE2 `NEIGuiHandler` precedent |
| JEI `IRecipeTransferHandler`/`IRecipeTransferError` (auto-fill from recipe GUI) | NEI fast transfer over the terminal GUI: `INEIGuiHandler` + `codechicken.nei.FastTransferManager` (shift-click fills the grid). No dedicated 1.7.10 transfer-handler interface exists — the port implements fill inside the `INEIGuiHandler` (inferred scope) | NEI API.java#L138; AE2 `NEIInputHandler` precedent |
| JEI/REI/EMI ghost-ingredient drag (filters into filter GUIs) | **No built-in NEI ghost-drag concept.** Scope decision (D11): at alpha, filter GUIs accept items by direct click/drag (vanilla container slots); a ghost-drag handler via `INEIGuiHandler.handleDragNDrop` is a beta candidate | NEI `INEIGuiHandler` (api/INEIGuiHandler.java); AE2 `NEIGuiHandler#handleDragNDrop` precedent |
| "Terminal shows stored items" view (upstream's REI/EMI-derived category; here a NEI usage handler, D11) | `IUsageHandler` rendering the terminal's network inventory as `PositionedStack`s (one `NEISimpleStorageUsageHandler`) | AE2 `NEICellViewHandler implements IUsageHandler` (NEICellViewHandler.java#L35) |
| REI/EMI workstation registration (crafting terminal) | `API.registerGuiOverlay` on the crafting-terminal GUI; no custom recipe category (assembler recipes are rendered by GT's own handler, see next row) | NEI API.java#L80 |
| Recipe handlers for our machine recipes (assembler) | None to write: GT builds an NEI handler per `RecipeMapFrontend`, so `gt.recipe.assembler` recipes already render in NEI | GT5 `gregtech/nei/GTNEIDefaultHandler.java` (reference/src/GT5) |
| Jade `IWailaPlugin`/`IServerDataProvider`/`IBlockComponentProvider` (block info for storage blocks/item) | Waila `mcp.mobius.waila.api.IWailaBlock` (head/name/tail lines) registered via `IWailaRegistrar` using IMC `FMLInterModComms.sendMessage("Waila", "register", "<fqcn>")`; optionally an InGame-Info-XML page per block | S3-verified Waila 1.19.34 (`IWailaRegistrar` javadoc); GT precedent `gregtech.crossmod.waila.Waila` (GT5-Unofficial) |
| TOP probe readouts (`IProbeInfoProvider`) | **none** — no TOP in the pack (S3 MOD-INDEX); Waila covers the readout need | S3 [shipped-mods.md](shipped-mods.md) (no TOP entry) |
| NEI bookmarks on the storage terminal | optional: `IBookmarkContainerHandler` (AE2 `NEIAETerminalBookmarkContainerHandler` precedent) — beta candidate | AE2 NEIAETerminalBookmarkContainerHandler.java#L14 |
| Mod options screen (upstream config) | GTNHLib `@Config` + auto config screen (see [machine-model.md](machine-model.md) §block-entity conventions) | GTNHLib 0.11.54 Config.java#L14 |

## What M06 builds (the concrete plugin)

1. `compat/nei/NEISimpleStorageConfig implements IConfigureNEI` — registers: the network-inventory `IUsageHandler`, `registerGuiOverlay` on the storage/crafting terminal + filing cabinet + filter GUIs, and the `INEIGuiHandler` for click/shift-click transfer (fast transfer). It does **not** register an assembler recipe handler: GT's own `GTNEIDefaultHandler` (GT5 `gregtech/nei/GTNEIDefaultHandler.java`) already renders `gt.recipe.assembler` recipes.
2. Waila `IWailaBlock` providers for: filing cabinet (contents summary), terminal (slot stats — upstream's `terminal_stat` keys, 03-terminal `index.md`), level emitter (filter + mode), cable/connector (network size), hopper (filter), configurator (bound state). Registered by IMC in the Waila-loaded case.
3. Guard: all NEI/Waila code behind `Loader.isModLoaded("NotEnoughItems")` / `Loader.isModLoaded("Waila")` in the `compat` package (S3 dependency table, [integration-opportunities.md](integration-opportunities.md) §Recommended dependencies).

## Sources

- NEI `2.8.171-GTNH` (`reference/src/NEI`): `ClientHandler.java#L317-L346`, `api/IConfigureNEI.java`, `api/API.java#L61-L285`, `recipe/ICraftingHandler.java#L8`, `recipe/IUsageHandler.java#L8`, `recipe/TemplateRecipeHandler.java#L52`, `api/INEIGuiHandler.java`, `api/INEIGuiAdapter.java`, `api/IBookmarkContainerHandler.java`.
- AE2 `rv3-beta-1095-GTNH` (`reference/src/AE2`): `appeng/integration/modules/NEI.java` and `NEIHelpers/` classes as cited above.
- GT5-Unofficial `5.09.54.265`: `gregtech/nei/NEIGTConfig.java#L57`.
- S3: [integration-opportunities.md](integration-opportunities.md) (Waila 1.19.34, IIGX 2.9.8, NEIAddons 1.18.8, NEI-Integration 1.5.3, pack pre-configs `config/NEI/` and `config/gtneioreplugin.cfg`).
