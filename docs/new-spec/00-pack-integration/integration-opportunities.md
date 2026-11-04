# Integration opportunities for a storage mod in GTNH 2.9.0

Facts for S3-Q3 (what exists, versions, API names) and S3-Q4 (recommended dependency coordinates). Design decisions — which integrations to build and how — belong to the S2 lane and design records 0003/0004. All versions are from the [manifest 2.9.0](https://github.com/GTNewHorizons/DreamAssemblerXXL/blob/master/releases/manifests/2.9.0.json) (checked 2026-11-02); all API facts are from the mod's source at the tag the pack ships, or from the shipped server pack in git-ignored `reference/gtnh-pack/`.

## Recipe viewer: NotEnoughItems (NEI)

GTNH ships **NotEnoughItems 2.8.171-GTNH** — GTNH's maintained fork of the 1.7.10 recipe viewer ([GTNewHorizons/NotEnoughItems](https://github.com/GTNewHorizons/NotEnoughItems), tag `2.8.171-GTNH`). There is no JEI, REI or EMI in the pack (no such entries in manifest 2.9.0). Around it: **NEIAddons 1.18.8** (`GTNewHorizons/neiaddons`), **NEI-Integration 1.5.3**, **nei-custom-diagram 1.8.36** (custom diagrams, including worldgen loot).

The pack pre-configures NEI: `config/NEI/` (client.cfg, handlerordering.csv, hiddenitems.cfg, subsets, …) and `config/gtneioreplugin.cfg` (GT's ore-sheet NEI plugin, part of the GT5-Unofficial jar).

**Plugin API (1.7.10 NEI, at tag 2.8.171-GTNH):**

- **Plugin discovery is by class scan, not mcmodinfo.** `codechicken.nei.ClientHandler#loadPluginsList()` (ClientHandler.java#L317-L346) discovers every class that implements `codechicken.nei.api.IConfigureNEI` **and** whose name starts with `NEI` and ends with `Config` (if jarjar is loaded, any `IConfigureNEI` implementation is loaded). So a plugin is a class like `NEISimpleStorageConfig implements IConfigureNEI` — no manifest entry needed.
- **Registration surface:** static methods on `codechicken.nei.api.API` (API.java#L54-L294 at the tag): `registerRecipeHandler(ICraftingHandler)`, `registerUsageHandler(IUsageHandler)`, `registerGuiOverlay(Class, String)` / `registerGuiOverlayHandler(Class, IOverlayHandler, …)` (mount NEI's recipe panel over a mod GUI), `registerNEIGuiHandler(INEIGuiHandler)` (GUI click handling, e.g. click-to-move), `addOption(Option)`, `addInfiniteItemHandler`, `registerHighlightHandler`, `hideItem`, `setAliases`, `addLayoutStyle`. Machine recipes are built on `codechicken.nei.recipe.TemplateRecipeHandler` and `PositionedStack`.
- **Pack precedent — GregTech:** `gregtech.nei.NEIGTConfig implements IConfigureNEI` (GT5-Unofficial, tag 5.09.54.265, src/main/java/gregtech/nei/NEIGTConfig.java) registers GT machine recipe handlers, usage handlers, a custom search parser and NEI options this way.
- **Pack precedent — a storage mod showing its contents in NEI:** Applied Energistics 2 (tag rv3-beta-1095-GTNH) ships `appeng.integration.modules.NEI` plus helpers: `NEICellViewHandler implements IUsageHandler` renders ME cell contents into NEI's usage panel (PositionedStacks of the cell's `IAEStack`s); `NEIPatternViewHandler` does the same for pattern providers; `NEIGuiHandler` + `NEIInputHandler` wire mouse interaction over the ME terminal GUI; `NEIAETerminalBookmarkContainerHandler` plugs the terminal into NEI bookmarks. This is the closest in-pack analogue of upstream's REI/EMI "show what the terminal holds" categories.

## Questbook: BetterQuesting

GTNH ships **BetterQuesting 3.8.92-GTNH** ([GTNewHorizons/BetterQuesting](https://github.com/GTNewHorizons/BetterQuesting), tag 3.8.92-GTNH).

- **Pack-side quests:** the server pack ships the quest content under `config/betterquesting/` — `DefaultQuests/Quests/` with **48 quest-line folders** (base64-named directories of quest JSON, plus `QuestLinesOrder.txt` and `QuestSettings.json`), `DefaultQuests/QuestLines/`, `questbook.cfg` and a `Readme.md` of quest-dev guidelines. One line is storage-themed ("Storing and Transferring…", folder `StoringandTransf-…`). Pack quests are maintained in the pack's quest repo, not by individual mods.
- **Mod-side API:** `betterquesting.api.api.QuestingAPI` is the entry point. A mod can ship its own quest line in its jar via the importer API: `betterquesting.api.client.importers.IImportRegistry#registerImporter(IImporter)`; `IImporter` (tag 3.8.92-GTNH) provides `getUnlocalisedName()`, `getUnlocalisedDescription()`, `getFileFilter()` and `loadFiles(IQuestDatabase, IQuestLineDatabase, File[])`, i.e. it loads BQ quest JSON files from anywhere — including jar resources. (The owner's Lost Cities backport calls this "a jar-shipped quest line"; the pack has no precedent for one yet.)

## Loot

- **EnhancedLootBags 1.3.5** ([GTNewHorizons/EnhancedLootBags](https://github.com/GTNewHorizons/EnhancedLootBags)): tiered loot bags with ULV–EV groups — the pack's loot-band mechanism.
- **LootGames 2.2.14** ([GTNewHorizons/LootGames](https://github.com/GTNewHorizons/LootGames)): seed-determined puzzle dungeons, a worldgen neighbor and loot source.
- **TooMuchLoot 4.3.0-GTNH** ([GTNewHorizons/TooMuchLoot](https://github.com/GTNewHorizons/TooMuchLoot), mod ID `TML`): replaces 16 vanilla loot categories from XML at server start — server owners can retune our chest categories the same way (cross-reference: Lost Cities 00-pack-integration `loot-and-mobs.md`, same pack/tag).

## Block/item info: the Jade equivalent

The pack has no Jade (no manifest entry). The Waila stack instead:

- **Waila 1.19.34** ([GTNewHorizons/waila](https://github.com/GTNewHorizons/waila), tag 1.19.34) — API package `mcp.mobius.waila.api`: `IWailaBlock` (head/name/tail tooltip lines for a block/TE), `IWailaBlockDecorator`, `IWailaEntityProvider`, `IWailaConfigHandler`, and `IWailaRegistrar` — the registration entry point. Per `IWailaRegistrar`'s javadoc (tag 1.19.34), a mod registers by sending an IMC: `FMLInterModComms.sendMessage("Waila", "register", "<fqcn>")` where `<fqcn>` has `public static void callbackRegister(IWailaRegistrar registrar)`. Pack precedent: GT registers its Waila info in `gregtech.crossmod.waila.Waila` (GT5-Unofficial).
- **InGame-Info-XML 2.9.8** ([GTNewHorizons/InGame-Info-XML](https://github.com/GTNewHorizons/InGame-Info-XML)): XML-defined info pages on top of Waila; a page can ship in our jar or be a pack-side XML line.
- **WAILAPlugins 0.7.2**, **WailaHarvestability 1.3.6-GTNH**: extras (plugins, harvest-tool info).
- Config: `config/Waila.cfg` ships pre-configured.

Upstream's Jade plugin (`JadePlugin implements IWailaPlugin`, NeoForge) maps to the Waila `IWailaBlock`/`IWailaRegistrar` surface; Jade is a later fork of this same Waila API, so the shapes correspond one-to-one (data provider → `IWailaBlock`, component → tooltip line, config section → `IWailaConfigHandler`).

## Config conventions

- The pack ships a pre-configured `config/` folder (195 entries in the server pack); per-mod `.cfg` files there are the pack's overrides of mod defaults.
- **GTNHLib 0.11.54** ([GTNewHorizons/GTNHLib](https://github.com/GTNewHorizons/GTNHLib), tag 0.11.54): the pack's config convention — annotation-driven config (GTNHLib `@Config`, D16) plus the automatic config screen its mod-list mixin gives every registered config (cross-reference: Lost Cities 00-pack-integration index, same pack/tag). Its config code uses fastutil, which the game gets from GTNHLib's dependency loader at launch (see Lost Cities `dependencies.gradle` comment, same pin).

## What upstream integrates (source side)

Upstream Tom's Storage (branch 26.2, local branch `upstream-src`) ships recipe-viewer and block-info integrations on its NeoForge platform under `NeoForge/src/platform-shared/java/com/tom/storagemod/`:

| Upstream integration | Classes (upstream-src:NeoForge/src/platform-shared/java/com/tom/storagemod/) | What it does |
|---|---|---|
| JEI | `jei/JEIHandler.java` (`@JeiPlugin IModPlugin`), `jei/CraftingTerminalTransferHandler.java`, `jei/JeiGhostIngredientHandler.java` | GUI container handler on the crafting terminal (clickable crafting recipes), recipe catalyst registration, recipe transfer into the terminal, ghost-ingredient drag |
| REI | `rei/REIPlugin.java` (`REIClientPlugin`), `rei/ReiTransferHandler.java`, `rei/ReiGhostIngredientHandler.java`; loader `NeoForge/src/main/java/…/rei/REIPluginLoader.java` | Transfer handlers, workstation registration (crafting terminal), storage categories, ghost ingredients |
| EMI | `emi/EMIPlugin.java` (`@EmiEntrypoint EmiPlugin`), `emi/EmiTransferHandler.java`, `emi/EmiGhostIngredientHandler.java` | Workstation registration, transfer handler, generic drag-drop, hovered-slot stack provider |
| Jade | `jade/JadePlugin.java` (`@WailaPlugin IWailaPlugin`), `jade/JadeUtil.java`, `jade/InventoryConnector*Provider.java`, `jade/InventoryConfig*Provider.java`, `jade/InventoryHopper*Provider.java` | Block data providers + tooltip components for the inventory connector, the storage item/config block and the hopper; filter-item tooltip in `JadeUtil` |

## Mapping table: upstream → 1.7.10 GTNH

| Upstream surface | 1.7.10 GTNH target | Evidence |
|---|---|---|
| JEI / REI / EMI plugin (one of three, selected per loader) | One NEI plugin class `NEI…Config implements IConfigureNEI` (auto-discovered; name must start `NEI`, end `Config`) | NEI ClientHandler.java#L317-L346, tag 2.8.171-GTNH |
| JEI `IGuiContainerHandler` (click-to-craft in the terminal) | `API.registerGuiOverlayHandler` + `INEIGuiHandler` / `API.registerGuiOverlay` over the terminal GUI | NEI API.java#L54-L294, tag 2.8.171-GTNH; AE2 `NEIGuiHandler` precedent |
| REI/EMI "terminal shows stored items" category | `IUsageHandler` rendering the terminal's inventory as PositionedStacks | AE2 `NEICellViewHandler implements IUsageHandler`, tag rv3-beta-1095-GTNH |
| Recipe transfer / ghost drag (JEI/REI/EMI) | NEI fast-transfer over the terminal GUI (`INEIGuiHandler`, `codechicken.nei.FastTransferManager`); ghost drag is not a built-in NEI concept — settled by D11 (ghost-drag is a beta candidate) | NEI API.java, tag 2.8.171-GTNH |
| Jade block info for storage blocks/item | Waila `IWailaBlock` via `IWailaRegistrar` (IMC `Waila`/`register`), optionally an InGame-Info-XML page | waila `IWailaRegistrar` javadoc, tag 1.19.34; GT precedent `gregtech.crossmod.waila.Waila` |
| Mod options (upstream config screen) | GTNHLib annotation config + auto config screen | GTNHLib 0.11.54 (manifest); Lost Cities index cross-ref |
| Questbook line | BetterQuesting importer (`IImportRegistry.registerImporter`) or a pack-side quest line | BQ tag 3.8.92-GTNH, `IImporter`/`IImportRegistry` |

## Recommended dependencies (Q4)

To be applied in M01 (not this lane). Pattern per [dependencies.gradle](../../../dependencies.gradle) and the owner's established GTNH pinning practice (Lost Cities `dependencies.gradle`, same pack): GTNH Maven coordinates with the `:dev` classifier, `compileOnly` for optional integrations, `api` when the type appears in public API. Coordinates use the GTNH repo name as the artifact:

| Dependency | Configuration | Coordinate | Why |
|---|---|---|---|
| GT5-Unofficial | `compileOnly` | `com.github.GTNewHorizons:GT5-Unofficial:5.09.54.265:dev` | Materials, ore dictionary, EU/voltage tiers; guarded by `Loader.isModLoaded("gregtech")` in a `compat` package |
| GTNHLib | `api` | `com.github.GTNewHorizons:GTNHLib:0.11.54:dev` | Annotation config and the auto config screen (public API surface) |
| NotEnoughItems | `compileOnly` | `com.github.GTNewHorizons:NotEnoughItems:2.8.171-GTNH:dev` | The recipe viewer; the NEI plugin is a hard part of this mod's feature set |
| Applied-Energistics-2-Unofficial | `compileOnly { transitive = false }` | `com.github.GTNewHorizons:Applied-Energistics-2-Unofficial:rv3-beta-1095-GTNH:dev` | Main storage-compat surface (ME interfaces/part transfer) |
| waila | `compileOnly` | `com.github.GTNewHorizons:waila:1.19.34:dev` | Jade-equivalent block info |
| InGame-Info-XML | `compileOnly` | `com.github.GTNewHorizons:InGame-Info-XML:2.9.8:dev` | XML info pages |
| BetterQuesting | `compileOnly` | `com.github.GTNewHorizons:BetterQuesting:3.8.92-GTNH:dev` | **Required (D12):** questbook line ships; pin added in M01 |
| EnhancedLootBags | `compileOnly` | `com.github.GTNewHorizons:EnhancedLootBags:1.3.5:dev` | Optional, M06 |
| Chisel | `compileOnly` | `com.github.GTNewHorizons:Chisel:2.17.34-GTNH:dev` | Optional, M06 |

Guarded by `Loader.isModLoaded("<modid>")` or `@Optional`, with the code kept in a `compat` package (gtnh-integration-research skill, §5).
