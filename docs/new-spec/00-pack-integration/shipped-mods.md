# GTNH 2.9.0 shipped mods (MOD-INDEX)

Every mod in the GTNH 2.9.0 pack, with the version the pack ships and why it matters to Simple Storage. "Why it matters" is `-` for mods with no known storage, compat, quest, loot or recipe-viewer relevance.

**Sources (checked 2026-11-02):**
- Mod list and versions: the release manifest `releases/manifests/2.9.0.json` of [GTNewHorizons/DreamAssemblerXXL](https://github.com/GTNewHorizons/DreamAssemblerXXL/blob/master/releases/manifests/2.9.0.json) (`version: 2.9.0`, `last_updated: 2026-10-31T12:04:06Z`): 212 GitHub-sourced + 31 external mods = 243 entries, copied to `reference/gtnh-pack/manifest-2.9.0.json` (git-ignored).
- Ground truth for what a player's server runs: the official GTNH 2.9.0 server pack zip (`https://downloads.gtnewhorizons.com/ServerPacks/GT_New_Horizons_2.9.0_Server_Java_17-26.zip`, downloaded 2026-11-02, extracted to git-ignored `reference/gtnh-pack/`). It ships `forge-1.7.10-10.13.4.1614-1.7.10-universal.jar` and 216 jars in `mods/` (217 entries in the folder, one of which is the OpenSecurity directory), and every one of those 216 matches a manifest entry by name or version. The 18 manifest entries whose version string appears in no jar name are accounted for: 15 are `side=CLIENT` mods the server pack does not ship, and the other 3 are present under a jar name the manifest version string does not literally match, verified by hand: `AdvancedSolarPanel-1.7.10-3.5.1.jar` (manifest "1.7.10 Edition"), `HungerOverhaul-1.7.10-1.0.0.jenkins104.jar`, `industrialcraft-2-2.2.828-experimental.jar`.
- Per-mod mod IDs, licenses and source tags: the owner's Lost Cities backport indexed the same pack at the same tag, `reference/gtnh/2.9.0/MOD-INDEX.md` in `Documents/work/LostCities` (regenerated from the same manifest); reuse it for mod IDs and licenses, it is not part of this repo.

## Mod index

| Mod | Version | Why it matters to us |
|---|---|---|
| Advanced Solar Panel For 1.7.10 (Unofficial) | 1.7.10 Edition | - |
| AdventureBackpack2 | 1.4.28-GTNH | Backpacks (adjacent storage) |
| AE2FluidCraft-Rework | 1.5.116-gtnh | AE2 fluid extension (minor) |
| AFSU | 1.3.2-GTNH | - |
| AkashicTome | 1.2.7 | - |
| AlchemyGrate | 1.3.2-GTNH | - |
| Amazing-Trophies | 1.4.10 | Achievement system (pack achievement pages) |
| amunra | 0.8.14 | - |
| Angelica | 2.2.43 | Client renderer (Embeddium); client-only |
| AngerMod | 1.0.7 | - |
| AppleCore | 3.3.13 | - |
| Applied-Energistics-2-Unofficial | rv3-beta-1095-GTNH | Direct storage competitor (ME storage); the main compat surface: ME interfaces, part transfer |
| Archaicfix | 0.8.2 | Backports including the Phosphor lighting option (`config/archaicfix.cfg`) |
| ArchitectureCraft | 1.12.19 | - |
| AsieLib | 0.7.2 | - |
| AspectRecipeIndex | 1.1.6 | - |
| Automagy-GTNH | 0.29.8-GTNH | - |
| Avaritia | 1.100 | - |
| Avaritiaddons | 1.9.6-GTNH | - |
| Backhand | 1.8.18 | - |
| Battlegear2-for-Backhand | 1.6.8-backhand | - |
| Baubles-Expanded | 2.2.27-GTNH | - |
| bdlib | 1.11.1-GTNH | - |
| BeeBetterAtBees-GTNH | 0.4.8-GTNH | - |
| BetterAchievements | 0.3.3 | - |
| BetterBuildersWands | 0.13.12-GTNH | - |
| BetterCrashes | 1.4.7-GTNH | - |
| BetterLoadingScreen | 1.7.19-GTNH | - |
| BetterP2P | 1.4.8 | - |
| BetterQuesting | 3.8.92-GTNH | The questbook: pack quests are BetterQuesting files; a jar-shipped quest line is an option |
| BiblioCraft | 1.11.7 | - |
| BiblioCraft: BiblioWoods Biomes O'Plenty Edition | 1.9 | - |
| BiblioCraft: BiblioWoods Forestry Edition | 1.7 | - |
| BiblioCraft: BiblioWoods Natura Edition | 1.5 | - |
| Binnie | 2.6.51 | - |
| Biomes O' Plenty | 2.1.0.2308 | Replaces the overworld biome provider for every world type |
| BlockLimiter | 0.7.0 | - |
| BlockRenderer6343 | 1.4.25 | - |
| BloodArsenal | 1.5.12 | - |
| BloodMagic | 1.9.16 | - |
| Botania | 1.13.42-GTNH | Magic content; block sources |
| Botanic-horizons | 1.12.10-GTNH | - |
| Bug-Torch | 1.3.1 | - |
| BuildCraft | 7.1.65 | Pipes and logistics; storage neighbors (silo, tank, transport) |
| BuildCraftCompat | 7.1.22 | - |
| BuildCraftOilTweak | 1.1.3 | - |
| CarpentersBlocks | 3.7.3-GTNH | - |
| Catwalks-2 | 2.4.2-GTNH | - |
| Chisel | 2.17.34-GTNH | Decorative blocks for terminal/filing-cabinet looks |
| ChiselTones | 1.2.0-GTNH | Chisel color tones |
| ChromaticTooltips | 1.0.36-GTNH | - |
| ChromaticTooltipsCompat | 1.0.40-GTNH | - |
| Chunk API | 0.8.4 | - |
| CodeChickenCore | 1.4.22 | - |
| CoFH Core | 3.1.4-329 | - |
| Compact Kinetic Generators | 1.0 | - |
| Computronics | 1.9.13-GTNH | - |
| Controlling | 2.1.10 | - |
| CookingForBlockheads | 1.4.14-GTNH | - |
| CoreTweaks | 0.3.4.7-GTNH | - |
| CosmeticArmorReworked | 1.0.7-GTNH | - |
| Craft-Presence | 2.7.1 | - |
| CraftTweaker | 3.4.8 | - |
| CropsNH | 2.0.140 | - |
| Custom-Main-Menu | 1.14.1 | - |
| Darkerer | 1.1.0 | - |
| Default-Configs | 1.3.4 | - |
| DefaultServerList | 1.7.4 | - |
| DefaultWorldGenerator | 0.5.0 | World-type selection: default `RWG2_CONTINENTS`, whitelist `flat`, `RWG`, `RWG2_CONTINENTS` (`config/defaultworldgenerator.cfg`) |
| Draconic-Evolution | 1.5.35-GTNH | - |
| DummyCore | 1.21.0 | - |
| DuraDisplay | 1.4.7 | - |
| Electro-Magic-Tools | 1.7.25 | - |
| EnderCore | 0.5.15 | - |
| EnderIO | 2.10.51 | IO conduits; power and item transport |
| EnderStorage | 1.8.9 | Ender chests (storage neighbor) |
| EnderZoo | 1.3.6 | - |
| EndlessIDs | 1.7.4 | - |
| EnhancedLootBags | 1.3.5 | Tiered loot bags (ULV-EV groups) for storage-themed loot |
| Et-Futurum-Requiem | 2.6.65-GTNH | - |
| Eternal-Singularity | 1.4.3 | - |
| Extra Utilities | 1.2.12 | - |
| Extreme Sound Muffler | 1.1.1 | - |
| FalsepatternLib | 1.12.2 | - |
| Fether | 2.0.9 | - |
| FindIt | 1.4.8 | - |
| FloodLights | 1.5.6 | - |
| ForbiddenMagic | 0.9.18-GTNH | - |
| ForestryMC | 4.11.40 | Forestry content |
| Forgelin | 2.0.3-GTNH | - |
| ForgeMultipart | 1.7.18 | - |
| ForgeRelocation | 0.3.7 | - |
| ForgeRelocationFMP | 0.2.0 | - |
| Freecam | 1.0.12 | - |
| Gadomancy | 1.5.17 | - |
| Galacticraft | 3.4.37-GTNH | - |
| Galaxy-Space-GTNH | 1.1.144-GTNH | - |
| gendustry | 1.9.13-GTNH | - |
| Gravitation-Suite-Neo | 1.3.16 | - |
| Gravitation-Suite-old | 2.0.3 | - |
| GregoryTweaksForCrafting | 1.0.3 | - |
| GT5-Unofficial | 5.09.54.265 | Pack core: materials, ore dictionary, voltages and EU; machine neighbors to our terminals; the `gtneioreplugin` sub-mod is GT's NEI plugin (`config/gtneioreplugin.cfg`) |
| GTNH-Credits | 1.4.0 | - |
| GTNH-TC-Wands | 1.4.14 | - |
| GTNHExtLib | 1.0.5 | - |
| GTNHLib | 0.11.54 | Config convention: annotation-driven config plus the automatic config screen; also the network channel base classes |
| GuideNH | 1.3.55 | In-game guide (a mod guide page is an option) |
| Hardcore-Ender-Expansion | 1.12.29-GTNH | - |
| harvestcraft | 1.3.15-GTNH | Pam's HarvestCraft: food for low-tier loot |
| Healer | 1.2.1 | - |
| HelpFixer | 1.3.2 | - |
| Hodgepodge | 2.7.221 | Behavior and performance patches; the cascading-worldgen warning headless tests fail on |
| HoloInventory | 2.5.18-GTNH | - |
| Hunger-Overhaul | 1.0.0.104 | Food/balance tweaks |
| HydroEnergy | 1.4.26 | - |
| IFU | 1.12.4 | - |
| IguanaTweaksTConstruct | 2.7.15 | - |
| Industrial Craft 2 | 2.2.2.828 | Energy/tech content; a second storage-like ecosystem (chests, machines) |
| Infernal-Mobs | 1.10.7-GTNH | - |
| InGame-Info-XML | 2.9.8 | Waila info framework: XML-defined info pages |
| InventoryBogoSorter | 1.3.55-GTNH | - |
| ironchest | 6.1.13 | Iron chests (storage neighbor) |
| IronChestMinecarts | 1.2.1 | - |
| IronTankMinecarts | 1.0.12 | - |
| Irontanks | 1.4.7 | - |
| Jabba | 1.5.24 | Jabba bags (storage neighbor) |
| JarJar | 0.3.7-beta | - |
| JourneyMap | 5.2.23-fairplay | Maps (client-only) |
| JourneyMap Server | 1.0.5 | Server side of JourneyMap |
| LittleTiles | 1.6.63 | - |
| LogisticsPipes | 1.5.39-GTNH | - |
| LootGames | 2.2.14 | Seed-determined dungeons; loot source and a worldgen neighbor |
| LunatriusCore | 1.2.1-GTNH | - |
| lwjgl3ify | 3.0.42 | - |
| MagicBees | 2.10.13-GTNH | - |
| MalisisDoors | 1.19.13-GTNH | - |
| Mantle | 0.5.4 | - |
| MatterManipulator | 0.1.75-GTNH | - |
| Minecraft-Backpack-Mod | 2.6.18-GTNH | Backpacks (adjacent storage) |
| Minetweaker-Gregtech-5-Addon | 2.3.4 | - |
| Mobs Info | 0.6.2 | - |
| ModernMarkings | 0.3.13-1.7.10 | - |
| ModTweaker | 0.14.0 | - |
| ModularUI | 1.3.4 | - |
| ModularUI2 | 2.3.94-1.7.10 | UI framework used by pack mods |
| MouseTweaks | 2.5.4-GTNH | - |
| MrTJPCore | 1.3.7 | - |
| Natura | 2.8.24 | - |
| NaturesCompass | 1.5.2-GTNH | - |
| Navigator | 1.1.10 | JourneyMap navigation layer (city outlines after beta) |
| nei-custom-diagram | 1.8.36 | NEI custom diagrams: worldgen-loot and custom inventory diagrams |
| NEI-Integration | 1.5.3 | Bridges NEI with other mods' recipe systems |
| neiaddons | 1.18.8 | NEI addon framework (info, recipes, not-enough-time); plugin surface |
| NetherPortalFix | 1.4.0 | - |
| NewHorizonsCoreMod | 2.9.99 | Pack core mod: recipe and worldgen tweaks |
| Nodal-Mechanics | 1.3.4-GTNH | - |
| NoHotbarNeeded | 0.0.6 | - |
| NotEnoughEnergistics | 1.7.50 | - |
| NotEnoughItems | 2.8.171-GTNH | The 1.7.10 recipe viewer (upstream's JEI/REI/EMI map onto its plugin API) |
| Nuclear-Control | 2.7.17 | - |
| Nutrition | 1.0.1 | - |
| oauth | 1.3.4-GTNH | - |
| OCGlasses | 1.6.9-GTNH | - |
| OpenBlocks | 1.12.22-GTNH | - |
| OpenComputers | 1.12.68-GTNH | - |
| OpenModsLib | 0.10.15 | - |
| OpenModularTurrets | 2.4.8 | - |
| OpenPrinter | 0.3.1-GTNH | Printed pages: lore notes in chests |
| OpenSecurity | 1.2.2-GTNH | - |
| Opis | 1.4.12-mapless | - |
| OverloadedArmorBar | 1.1.1 | - |
| PersonalSpace | 1.0.41 | - |
| Player-API | 1.5.0 | - |
| Postea | 1.2.7 | - |
| ProjectBlue | 1.2.11-GTNH | - |
| ProjectRed | 4.12.50-GTNH | - |
| Railcraft | 9.17.36 | Rail content (loot, dressing) |
| Random-Things | 2.7.13 | - |
| RandomBoubles | 1.1.7 | - |
| Realistic-World-Gen | alpha-1.5.3 | Classic RWG overworld; the server pack default (`level-type=rwg` in `server.properties`) |
| Realistic-World-Gen-2 | 2.0.6 | RWG2 Continents; the client default world type |
| RemoteIO | 2.7.11 | - |
| Roguelike-Dungeons | 1.6.6-GTNH | - |
| Salis Arcana | 2.7.0 | - |
| SC2 | 2.3.15 | - |
| Schematica | 1.12.6-GTNH | - |
| ServerUtilities | 2.4.22 | - |
| SGCraft | 1.4.14-GTNH | - |
| Share-Where-I-am | 2.1.11 | - |
| SimpleSkinBackport | 1.0.5-GTNH | - |
| SleepingBags | 0.3.1 | - |
| SpecialMobs | 3.7.10 | - |
| SpiceOfLife | 2.2.12-carrot | - |
| Steve-s-Factory-Manager | 1.3.13-GTNH | - |
| StevesAddons | 0.15.5 | - |
| StorageDrawers | 2.2.31-GTNH | Storage drawers (storage neighbor) |
| StructureCompat | 0.7.5 | - |
| StructureLib | 1.4.47 | - |
| Super-TiC | 1.5.6 | - |
| supersolarpanels | 1.1.5 | - |
| Tainted-Magic | 7.7.8-GTNH | - |
| TC-4-Tweaks | 1.5.48 | - |
| TCNodeTracker | 1.4.6 | - |
| Thaumcraft | 4.2.3.5 | Magic content; nodes as worldgen neighbors |
| thaumcraft-research-tweaks | 1.4.1 | - |
| ThaumcraftMobAspects | 1.2.1-GTNH | - |
| Thaumic_Exploration | 1.5.29-GTNH | - |
| ThaumicBases | 1.9.22 | - |
| ThaumicBoots | 1.5.13 | - |
| ThaumicEnergistics | 1.7.68-GTNH | - |
| ThaumicHorizons | 1.8.28 | - |
| thaumicinsurgence | 0.4.1 | - |
| ThaumicMachina | 0.2.6-GTNH | - |
| ThaumicTinkerer | 2.12.34 | - |
| TiC-Tooltips | 1.4.2 | - |
| Tinkers-Defense | 1.3.4 | - |
| Tinkers-Gregworks | 1.0.34 | - |
| TinkersConstruct | 1.14.123-GTNH | Tools and smelting content |
| TinkersMechworks | 0.4.2 | - |
| TooMuchLoot | 4.3.0-GTNH | Rewrites 16 vanilla loot categories from XML at server start |
| ToroHealth | 1.2.4 | - |
| Translocators | 1.4.5 | - |
| twilightforest | 2.7.45 | Dimension content |
| TX-Loader | 1.9.1 | - |
| UniLib | 1.2.1 | - |
| UniMixins | 0.3.1 | - |
| Universal-Singularities | 8.15.4 | - |
| VendingMachine | 0.4.103 | - |
| VillageNames | 4.5.21-GTNH | - |
| VisualProspecting | 1.5.42 | - |
| waila | 1.19.34 | Jade's predecessor: the Waila API (`IWailaRegistrar`/`IWailaBlock`) is where our Jade integrations land |
| WailaHarvestability | 1.3.6-GTNH | Waila harvest-tool info |
| WAILAPlugins | 0.7.2 | Extra Waila plugins |
| WanionLib | 1.10.1 | - |
| WarpTheory | 1.5.8-GTNH | - |
| WAWLA | 1.3.4-GTNH | - |
| WirelessCraftingTerminal | 1.12.17 | - |
| WirelessRedstone-CBE | 1.7.12 | - |
| Witchery | 0.24.1 | Magic content; surface structures |
| WitcheryExtras | 1.4.20 | - |
| WitchingGadgets | 1.8.53-GTNH | - |
| Yamcl | 0.7.5 | - |
| Ztones | 2.2.2 | Light sources for the pack's dark nights |
