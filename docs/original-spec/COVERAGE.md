# Coverage

Every upstream Java file and resource, and the sheet that covers it. Upstream = Tom's Simple Storage `2.11.3`, branch `26.2`, commit `95bb61d`. Path prefixes: `SH` = `NeoForge/src/platform-shared/java/com/tom/storagemod/`, `NF` = `NeoForge/src/main/java/com/tom/storagemod/`, `FB` = `Fabric/src/main/java/com/tom/storagemod/`, `RES` = `NeoForge/src/platform-shared/resources/`.

**Totals (verified):** `git ls-tree -r 26.2 --name-only | grep -c '\.java$'` = **217** Java files; **488** files in the tree overall (271 non-Java). The per-area table below covers all 217 Java files individually (217 rows) and all 271 non-Java files by group (counts verified against `git ls-tree`).

## Per-area summary (final; all four parallel areas committed)

| Area | Java files | Sheets | Notes |
|---|---|---|---|
| `01-core` | 57 | [index](01-core/index.md), mod-lifecycle, config, network, platform-layer, inventory-access, terminal-sync, util-containers, util-filters, util-data-slots, util-misc, util-api | lifecycle/registration (incl. `StorageModClient` NF/FB, `ServerEvents`), config keys, the two mod network channels, platform adapters + inventory access, terminal sync protocol, shared utils |
| `02-storage` | 68 | [index](02-storage/index.md), filing-cabinet, open-crate, inventory-proxy, trims-paint, cables, cable-connector, inventory-connector, inventory-interface, level-emitter, hopper, items, components, containers, cable-network | storage-network blocks, block entities, cable network, components, items, `PaintedBlockEntity` (NF) |
| `03-terminal` | 55 | [index](03-terminal/index.md), terminal-blocks, storage-terminal, crafting-terminal, filing-cabinet-gui, inventory-configurator, inventory-link, level-emitter-gui, item-filter-gui, filters, sorting, terminal-stacks, inventory-access, container-screens, terminal-utils | terminal blocks/entities, menus, screens (incl. the screen base classes `IScreen`/`TSContainerScreen`/`PlatformContainerScreen`), filter + sorting systems, terminal stacks |
| `04-wireless` | 3 | [index](04-wireless/index.md), wireless-items, range-and-sync | the two wireless terminal items + `WirelessTerminal` interface; beacon range calc |
| `05-integration` | 21 | [index](05-integration/index.md), [jei](05-integration/jei.md), [rei](05-integration/rei.md), [emi](05-integration/emi.md), [jade](05-integration/jade.md), [other-compat](05-integration/other-compat.md) | recipe viewers, Jade, TOP, Polymorph, Sophisticated, ModMenu |
| `06-client-ui` | 13 | [index](06-client-ui/index.md), [widgets](06-client-ui/widgets.md), [client-package](06-client-ui/client-package.md), [painted-models](06-client-ui/painted-models.md), [lang](06-client-ui/lang.md), [textures-sounds](06-client-ui/textures-sounds.md) | widget kit, client package, painted-block model facades, lang + GUI resources |

Assignment rules for files that sit between areas (stated per the README): the multiblock-inventory API + `EventHandler`/`PolymorphHelper`/`PolymorphTerminalWidget` → `01` (catalogued in `01-core/util-api.md`); `VanillaMultiblockInventories`/`MultiInventoryAccess`/`IAutoFillTerminal` → `03` (`03-terminal/inventory-access.md`, `03-terminal/terminal-utils.md`); the screen base classes (`IScreen`, `TSContainerScreen`, `PlatformContainerScreen` NF/FB) + `RecipeBookButton` → `03` (`container-screens.md`/`crafting-terminal.md`) though `06-client-ui/widgets.md` documents them as GUI-kit infrastructure; `StorageModClient` (NF+FB) → `01` (`mod-lifecycle.md`) though `06-client-ui/client-package.md` documents it in detail; utility containers (`BasicContainer`, `LimitedContainer`, `FilingCabinetContainer`, `CraftingMatrix`) → the area owning their behavior (`02`/`03`); `BeaconLevelCalc` → `01` (`util-misc.md`, behavior cross-referenced in `04-wireless/range-and-sync.md`). `05`'s `other-compat.md` covers the integration-side usage of the `01`/`03`-owned compat classes.

## Java files by area (217 rows — count matches `git ls-tree -r 26.2 --name-only | grep -c '\.java$'` = 217; both numbers stated here per the stop condition)

| Area | Rows |
|---|---|
| `01-core` | 57 |
| `02-storage` | 68 |
| `03-terminal` | 55 |
| `04-wireless` | 3 |
| `05-integration` | 21 |
| `06-client-ui` | 13 |

Sheet names below are the committed sheet files in each area. Where a file is documented in detail by more than one sheet, the sheet in this column is the one that owns its behavior; the others cross-reference it.

| `01` | Fabric/src/main/java/com/tom/storagemod/Config.java | 01-core/config.md |
| `05` | Fabric/src/main/java/com/tom/storagemod/ModMenu.java | 05-integration/other-compat.md |
| `01` | Fabric/src/main/java/com/tom/storagemod/StorageMod.java | 01-core/mod-lifecycle.md |
| `01` | Fabric/src/main/java/com/tom/storagemod/StorageModClient.java | 01-core/mod-lifecycle.md |
| `01` | Fabric/src/main/java/com/tom/storagemod/StorageTags.java | 01-core/mod-lifecycle.md |
| `01` | Fabric/src/main/java/com/tom/storagemod/block/NeoForgeBlock.java | 01-core/platform-layer.md |
| `02` | Fabric/src/main/java/com/tom/storagemod/block/entity/OpenCrateBlockEntity.java | 02-storage/open-crate.md |
| `02` | Fabric/src/main/java/com/tom/storagemod/block/entity/PaintedBlockEntity.java | 02-storage/trims-paint.md |
| `06` | Fabric/src/main/java/com/tom/storagemod/client/BakedPaintedModel.java | 06-client-ui/client-package.md |
| `06` | Fabric/src/main/java/com/tom/storagemod/client/UnbakedPaintedModel.java | 06-client-ui/client-package.md |
| `02` | Fabric/src/main/java/com/tom/storagemod/inventory/InventoryChangeTracker.java | 02-storage/cable-network.md |
| `02` | Fabric/src/main/java/com/tom/storagemod/inventory/InventoryImage.java | 02-storage/cable-network.md |
| `02` | Fabric/src/main/java/com/tom/storagemod/inventory/InventorySlot.java | 02-storage/cable-network.md |
| `02` | Fabric/src/main/java/com/tom/storagemod/inventory/PlatformFilteredInventoryAccess.java | 02-storage/cable-network.md |
| `02` | Fabric/src/main/java/com/tom/storagemod/inventory/PlatformInventoryAccess.java | 02-storage/cable-network.md |
| `02` | Fabric/src/main/java/com/tom/storagemod/inventory/PlatformItemHandler.java | 02-storage/cable-network.md |
| `02` | Fabric/src/main/java/com/tom/storagemod/inventory/PlatformMultiInventoryAccess.java | 02-storage/cable-network.md |
| `02` | Fabric/src/main/java/com/tom/storagemod/inventory/PlatformProxyInventoryAccess.java | 02-storage/inventory-proxy.md |
| `01` | Fabric/src/main/java/com/tom/storagemod/inventory/filter/PolyFilter.java | 01-core/util-filters.md |
| `01` | Fabric/src/main/java/com/tom/storagemod/network/NetworkHandler.java | 01-core/network.md |
| `01` | Fabric/src/main/java/com/tom/storagemod/platform/GameObject.java | 01-core/platform-layer.md |
| `01` | Fabric/src/main/java/com/tom/storagemod/platform/Platform.java | 01-core/platform-layer.md |
| `01` | Fabric/src/main/java/com/tom/storagemod/platform/PlatformBlockEntity.java | 01-core/platform-layer.md |
| `01` | Fabric/src/main/java/com/tom/storagemod/platform/PlatformItem.java | 01-core/platform-layer.md |
| `03` | Fabric/src/main/java/com/tom/storagemod/screen/PlatformContainerScreen.java | 03-terminal/container-screens.md |
| `01` | Fabric/src/main/java/com/tom/storagemod/util/TickerUtil.java | 01-core/util-misc.md |
| `01` | Fabric/src/main/java/com/tom/storagemod/util/WorldStates.java | 01-core/util-misc.md |
| `01` | NeoForge/src/main/java/com/tom/storagemod/Config.java | 01-core/config.md |
| `01` | NeoForge/src/main/java/com/tom/storagemod/ServerEvents.java | 01-core/mod-lifecycle.md |
| `01` | NeoForge/src/main/java/com/tom/storagemod/StorageMod.java | 01-core/mod-lifecycle.md |
| `01` | NeoForge/src/main/java/com/tom/storagemod/StorageModClient.java | 01-core/mod-lifecycle.md |
| `01` | NeoForge/src/main/java/com/tom/storagemod/StorageTags.java | 01-core/mod-lifecycle.md |
| `01` | NeoForge/src/main/java/com/tom/storagemod/block/NeoForgeBlock.java | 01-core/platform-layer.md |
| `02` | NeoForge/src/main/java/com/tom/storagemod/block/entity/BlockFilterAttachment.java | 02-storage/cable-network.md |
| `02` | NeoForge/src/main/java/com/tom/storagemod/block/entity/OpenCrateBlockEntity.java | 02-storage/open-crate.md |
| `02` | NeoForge/src/main/java/com/tom/storagemod/block/entity/PaintedBlockEntity.java | 02-storage/trims-paint.md |
| `06` | NeoForge/src/main/java/com/tom/storagemod/client/BakedPaintedModel.java | 06-client-ui/client-package.md |
| `06` | NeoForge/src/main/java/com/tom/storagemod/client/PaintedExtension.java | 06-client-ui/client-package.md |
| `01` | NeoForge/src/main/java/com/tom/storagemod/inventory/EmptyInventory.java | 01-core/inventory-access.md |
| `01` | NeoForge/src/main/java/com/tom/storagemod/inventory/InventoryChangeTracker.java | 01-core/inventory-access.md |
| `01` | NeoForge/src/main/java/com/tom/storagemod/inventory/InventorySlot.java | 01-core/inventory-access.md |
| `01` | NeoForge/src/main/java/com/tom/storagemod/inventory/NeoStack.java | 01-core/inventory-access.md |
| `01` | NeoForge/src/main/java/com/tom/storagemod/inventory/PlatformFilteredInventoryAccess.java | 01-core/inventory-access.md |
| `01` | NeoForge/src/main/java/com/tom/storagemod/inventory/PlatformInventoryAccess.java | 01-core/inventory-access.md |
| `01` | NeoForge/src/main/java/com/tom/storagemod/inventory/PlatformItemHandler.java | 01-core/inventory-access.md |
| `01` | NeoForge/src/main/java/com/tom/storagemod/inventory/PlatformMultiInventoryAccess.java | 01-core/inventory-access.md |
| `01` | NeoForge/src/main/java/com/tom/storagemod/inventory/PlatformProxyInventoryAccess.java | 01-core/inventory-access.md |
| `01` | NeoForge/src/main/java/com/tom/storagemod/inventory/filter/PolyFilter.java | 01-core/util-filters.md |
| `01` | NeoForge/src/main/java/com/tom/storagemod/network/NetworkHandler.java | 01-core/network.md |
| `01` | NeoForge/src/main/java/com/tom/storagemod/platform/GameObject.java | 01-core/platform-layer.md |
| `01` | NeoForge/src/main/java/com/tom/storagemod/platform/Platform.java | 01-core/platform-layer.md |
| `01` | NeoForge/src/main/java/com/tom/storagemod/platform/PlatformBlockEntity.java | 01-core/platform-layer.md |
| `01` | NeoForge/src/main/java/com/tom/storagemod/platform/PlatformItem.java | 01-core/platform-layer.md |
| `05` | NeoForge/src/main/java/com/tom/storagemod/rei/REIPluginLoader.java | 05-integration/rei.md |
| `01` | NeoForge/src/main/java/com/tom/storagemod/screen/PlatformContainerScreen.java | 01-core/util-misc.md |
| `05` | NeoForge/src/main/java/com/tom/storagemod/top/TheOneProbeHandler.java | 05-integration/other-compat.md |
| `01` | NeoForge/src/main/java/com/tom/storagemod/util/TickerUtil.java | 01-core/util-misc.md |
| `01` | NeoForge/src/main/java/com/tom/storagemod/util/WorldStates.java | 01-core/util-misc.md |
| `01` | NeoForge/src/platform-shared/java/com/tom/storagemod/Content.java | 01-core/util-misc.md |
| `01` | NeoForge/src/platform-shared/java/com/tom/storagemod/api/EventHandler.java | 01-core/util-api.md |
| `01` | NeoForge/src/platform-shared/java/com/tom/storagemod/api/MultiblockInventoryAPI.java | 01-core/util-api.md |
| `02` | NeoForge/src/platform-shared/java/com/tom/storagemod/block/AbstractInventoryHopperBlock.java | 02-storage/hopper.md |
| `03` | NeoForge/src/platform-shared/java/com/tom/storagemod/block/AbstractStorageTerminalBlock.java | 03-terminal/terminal-blocks.md |
| `02` | NeoForge/src/platform-shared/java/com/tom/storagemod/block/BasicInventoryHopperBlock.java | 02-storage/hopper.md |
| `02` | NeoForge/src/platform-shared/java/com/tom/storagemod/block/BlockWithTooltip.java | 02-storage/trims-paint.md |
| `03` | NeoForge/src/platform-shared/java/com/tom/storagemod/block/CraftingTerminalBlock.java | 03-terminal/terminal-blocks.md |
| `02` | NeoForge/src/platform-shared/java/com/tom/storagemod/block/FilingCabinetBlock.java | 02-storage/filing-cabinet.md |
| `02` | NeoForge/src/platform-shared/java/com/tom/storagemod/block/FramedInventoryCableBlock.java | 02-storage/cables.md |
| `02` | NeoForge/src/platform-shared/java/com/tom/storagemod/block/FramedInventoryCableConnectorBlock.java | 02-storage/cable-connector.md |
| `02` | NeoForge/src/platform-shared/java/com/tom/storagemod/block/IConfiguratorHighlight.java | 02-storage/trims-paint.md |
| `02` | NeoForge/src/platform-shared/java/com/tom/storagemod/block/IInventoryCable.java | 02-storage/cables.md |
| `02` | NeoForge/src/platform-shared/java/com/tom/storagemod/block/IInventoryNode.java | 02-storage/cable-network.md |
| `02` | NeoForge/src/platform-shared/java/com/tom/storagemod/block/IPaintable.java | 02-storage/trims-paint.md |
| `02` | NeoForge/src/platform-shared/java/com/tom/storagemod/block/InventoryCableBlock.java | 02-storage/cables.md |
| `02` | NeoForge/src/platform-shared/java/com/tom/storagemod/block/InventoryCableConnectorBlock.java | 02-storage/cable-connector.md |
| `02` | NeoForge/src/platform-shared/java/com/tom/storagemod/block/InventoryConnectorBlock.java | 02-storage/inventory-connector.md |
| `02` | NeoForge/src/platform-shared/java/com/tom/storagemod/block/InventoryInterfaceBlock.java | 02-storage/inventory-interface.md |
| `02` | NeoForge/src/platform-shared/java/com/tom/storagemod/block/InventoryProxyBlock.java | 02-storage/inventory-proxy.md |
| `02` | NeoForge/src/platform-shared/java/com/tom/storagemod/block/LevelEmitterBlock.java | 02-storage/level-emitter.md |
| `02` | NeoForge/src/platform-shared/java/com/tom/storagemod/block/OpenCrateBlock.java | 02-storage/open-crate.md |
| `02` | NeoForge/src/platform-shared/java/com/tom/storagemod/block/PaintedTrimBlock.java | 02-storage/trims-paint.md |
| `03` | NeoForge/src/platform-shared/java/com/tom/storagemod/block/StorageTerminalBlock.java | 03-terminal/terminal-blocks.md |
| `02` | NeoForge/src/platform-shared/java/com/tom/storagemod/block/TrimBlock.java | 02-storage/trims-paint.md |
| `02` | NeoForge/src/platform-shared/java/com/tom/storagemod/block/entity/AbstractInventoryHopperBlockEntity.java | 02-storage/hopper.md |
| `02` | NeoForge/src/platform-shared/java/com/tom/storagemod/block/entity/AbstractPainedBlockEntity.java | 02-storage/cable-network.md |
| `02` | NeoForge/src/platform-shared/java/com/tom/storagemod/block/entity/BasicInventoryHopperBlockEntity.java | 02-storage/hopper.md |
| `02` | NeoForge/src/platform-shared/java/com/tom/storagemod/block/entity/CraftingTerminalBlockEntity.java | 02-storage/cable-network.md |
| `02` | NeoForge/src/platform-shared/java/com/tom/storagemod/block/entity/FilingCabinetBlockEntity.java | 02-storage/filing-cabinet.md |
| `02` | NeoForge/src/platform-shared/java/com/tom/storagemod/block/entity/IInventoryConnector.java | 02-storage/inventory-connector.md |
| `02` | NeoForge/src/platform-shared/java/com/tom/storagemod/block/entity/InventoryCableConnectorBlockEntity.java | 02-storage/cable-connector.md |
| `02` | NeoForge/src/platform-shared/java/com/tom/storagemod/block/entity/InventoryConnectorBlockEntity.java | 02-storage/inventory-connector.md |
| `02` | NeoForge/src/platform-shared/java/com/tom/storagemod/block/entity/InventoryInterfaceBlockEntity.java | 02-storage/inventory-interface.md |
| `02` | NeoForge/src/platform-shared/java/com/tom/storagemod/block/entity/InventoryProxyBlockEntity.java | 02-storage/inventory-proxy.md |
| `02` | NeoForge/src/platform-shared/java/com/tom/storagemod/block/entity/LevelEmitterBlockEntity.java | 02-storage/level-emitter.md |
| `02` | NeoForge/src/platform-shared/java/com/tom/storagemod/block/entity/StorageTerminalBlockEntity.java | 02-storage/cable-network.md |
| `06` | NeoForge/src/platform-shared/java/com/tom/storagemod/client/ClientUtil.java | 06-client-ui/client-package.md |
| `06` | NeoForge/src/platform-shared/java/com/tom/storagemod/client/CustomRenderTypes.java | 06-client-ui/client-package.md |
| `06` | NeoForge/src/platform-shared/java/com/tom/storagemod/client/ViveCraftHelper.java | 06-client-ui/client-package.md |
| `02` | NeoForge/src/platform-shared/java/com/tom/storagemod/components/ConfiguratorComponent.java | 02-storage/components.md |
| `02` | NeoForge/src/platform-shared/java/com/tom/storagemod/components/SimpleItemFilterComponent.java | 02-storage/components.md |
| `02` | NeoForge/src/platform-shared/java/com/tom/storagemod/components/TagFilterComponent.java | 02-storage/components.md |
| `02` | NeoForge/src/platform-shared/java/com/tom/storagemod/components/WorldPos.java | 02-storage/components.md |
| `05` | NeoForge/src/platform-shared/java/com/tom/storagemod/emi/EMIPlugin.java | 05-integration/emi.md |
| `05` | NeoForge/src/platform-shared/java/com/tom/storagemod/emi/EmiGhostIngredientHandler.java | 05-integration/emi.md |
| `05` | NeoForge/src/platform-shared/java/com/tom/storagemod/emi/EmiTransferHandler.java | 05-integration/emi.md |
| `03` | NeoForge/src/platform-shared/java/com/tom/storagemod/inventory/BlockFilter.java | 03-terminal/filters.md |
| `02` | NeoForge/src/platform-shared/java/com/tom/storagemod/inventory/IChangeTrackerAccess.java | 02-storage/cable-network.md |
| `02` | NeoForge/src/platform-shared/java/com/tom/storagemod/inventory/IInventoryAccess.java | 02-storage/cable-network.md |
| `02` | NeoForge/src/platform-shared/java/com/tom/storagemod/inventory/IInventoryConnectorReference.java | 02-storage/inventory-connector.md |
| `02` | NeoForge/src/platform-shared/java/com/tom/storagemod/inventory/IInventoryLink.java | 02-storage/cable-network.md |
| `02` | NeoForge/src/platform-shared/java/com/tom/storagemod/inventory/IProxy.java | 02-storage/inventory-proxy.md |
| `02` | NeoForge/src/platform-shared/java/com/tom/storagemod/inventory/InventoryCableNetwork.java | 02-storage/cables.md |
| `03` | NeoForge/src/platform-shared/java/com/tom/storagemod/inventory/MultiInventoryAccess.java | 03-terminal/inventory-access.md |
| `02` | NeoForge/src/platform-shared/java/com/tom/storagemod/inventory/NetworkInventory.java | 02-storage/cable-network.md |
| `02` | NeoForge/src/platform-shared/java/com/tom/storagemod/inventory/RemoteConnections.java | 02-storage/cable-network.md |
| `03` | NeoForge/src/platform-shared/java/com/tom/storagemod/inventory/StoredItemStack.java | 03-terminal/terminal-stacks.md |
| `03` | NeoForge/src/platform-shared/java/com/tom/storagemod/inventory/TerminalItemStack.java | 03-terminal/terminal-stacks.md |
| `03` | NeoForge/src/platform-shared/java/com/tom/storagemod/inventory/VanillaMultiblockInventories.java | 03-terminal/inventory-access.md |
| `05` | NeoForge/src/platform-shared/java/com/tom/storagemod/inventory/compat/SophisticatedDoubleBlocks.java | 05-integration/other-compat.md |
| `03` | NeoForge/src/platform-shared/java/com/tom/storagemod/inventory/filter/IFilter.java | 03-terminal/filters.md |
| `03` | NeoForge/src/platform-shared/java/com/tom/storagemod/inventory/filter/ItemFilter.java | 03-terminal/filters.md |
| `03` | NeoForge/src/platform-shared/java/com/tom/storagemod/inventory/filter/ItemPredicate.java | 03-terminal/filters.md |
| `03` | NeoForge/src/platform-shared/java/com/tom/storagemod/inventory/filter/SimpleItemFilter.java | 03-terminal/filters.md |
| `03` | NeoForge/src/platform-shared/java/com/tom/storagemod/inventory/filter/TagFilter.java | 03-terminal/filters.md |
| `03` | NeoForge/src/platform-shared/java/com/tom/storagemod/inventory/sorting/ComparatorAmount.java | 03-terminal/sorting.md |
| `03` | NeoForge/src/platform-shared/java/com/tom/storagemod/inventory/sorting/ComparatorID.java | 03-terminal/sorting.md |
| `03` | NeoForge/src/platform-shared/java/com/tom/storagemod/inventory/sorting/ComparatorMod.java | 03-terminal/sorting.md |
| `03` | NeoForge/src/platform-shared/java/com/tom/storagemod/inventory/sorting/ComparatorName.java | 03-terminal/sorting.md |
| `03` | NeoForge/src/platform-shared/java/com/tom/storagemod/inventory/sorting/ComparatorSpaceEfficiency.java | 03-terminal/sorting.md |
| `03` | NeoForge/src/platform-shared/java/com/tom/storagemod/inventory/sorting/ComparatorType.java | 03-terminal/sorting.md |
| `03` | NeoForge/src/platform-shared/java/com/tom/storagemod/inventory/sorting/SortingTypes.java | 03-terminal/sorting.md |
| `04` | NeoForge/src/platform-shared/java/com/tom/storagemod/item/AdvWirelessTerminalItem.java | 04-wireless/wireless-items.md |
| `02` | NeoForge/src/platform-shared/java/com/tom/storagemod/item/BlockItemTSS.java | 02-storage/items.md |
| `03` | NeoForge/src/platform-shared/java/com/tom/storagemod/item/FilterItem.java | 03-terminal/filters.md |
| `03` | NeoForge/src/platform-shared/java/com/tom/storagemod/item/IItemFilter.java | 03-terminal/filters.md |
| `02` | NeoForge/src/platform-shared/java/com/tom/storagemod/item/ILeftClickListener.java | 02-storage/items.md |
| `02` | NeoForge/src/platform-shared/java/com/tom/storagemod/item/InventoryConfiguratorItem.java | 02-storage/cable-network.md |
| `02` | NeoForge/src/platform-shared/java/com/tom/storagemod/item/PaintKitItem.java | 02-storage/trims-paint.md |
| `02` | NeoForge/src/platform-shared/java/com/tom/storagemod/item/PaintedBlockItem.java | 02-storage/trims-paint.md |
| `03` | NeoForge/src/platform-shared/java/com/tom/storagemod/item/PolyFilterItem.java | 03-terminal/filters.md |
| `03` | NeoForge/src/platform-shared/java/com/tom/storagemod/item/TagFilterItem.java | 03-terminal/filters.md |
| `04` | NeoForge/src/platform-shared/java/com/tom/storagemod/item/WirelessTerminal.java | 04-wireless/wireless-items.md |
| `04` | NeoForge/src/platform-shared/java/com/tom/storagemod/item/WirelessTerminalItem.java | 04-wireless/wireless-items.md |
| `05` | NeoForge/src/platform-shared/java/com/tom/storagemod/jade/InventoryConfigComponentProvider.java | 05-integration/jade.md |
| `05` | NeoForge/src/platform-shared/java/com/tom/storagemod/jade/InventoryConfigDataProvider.java | 05-integration/jade.md |
| `05` | NeoForge/src/platform-shared/java/com/tom/storagemod/jade/InventoryConnectorComponentProvider.java | 05-integration/jade.md |
| `05` | NeoForge/src/platform-shared/java/com/tom/storagemod/jade/InventoryConnectorDataProvider.java | 05-integration/jade.md |
| `05` | NeoForge/src/platform-shared/java/com/tom/storagemod/jade/InventoryHopperComponentProvider.java | 05-integration/jade.md |
| `05` | NeoForge/src/platform-shared/java/com/tom/storagemod/jade/InventoryHopperDataProvider.java | 05-integration/jade.md |
| `05` | NeoForge/src/platform-shared/java/com/tom/storagemod/jade/JadePlugin.java | 05-integration/jade.md |
| `05` | NeoForge/src/platform-shared/java/com/tom/storagemod/jade/JadeUtil.java | 05-integration/jade.md |
| `05` | NeoForge/src/platform-shared/java/com/tom/storagemod/jei/CraftingTerminalTransferHandler.java | 05-integration/jei.md |
| `05` | NeoForge/src/platform-shared/java/com/tom/storagemod/jei/JEIHandler.java | 05-integration/jei.md |
| `05` | NeoForge/src/platform-shared/java/com/tom/storagemod/jei/JeiGhostIngredientHandler.java | 05-integration/jei.md |
| `03` | NeoForge/src/platform-shared/java/com/tom/storagemod/menu/AbstractFilteredMenu.java | 03-terminal/item-filter-gui.md |
| `03` | NeoForge/src/platform-shared/java/com/tom/storagemod/menu/CraftingTerminalMenu.java | 03-terminal/crafting-terminal.md |
| `02` | NeoForge/src/platform-shared/java/com/tom/storagemod/menu/FilingCabinetMenu.java | 02-storage/filing-cabinet.md |
| `02` | NeoForge/src/platform-shared/java/com/tom/storagemod/menu/InventoryConfiguratorMenu.java | 02-storage/cable-network.md |
| `02` | NeoForge/src/platform-shared/java/com/tom/storagemod/menu/InventoryLinkMenu.java | 02-storage/cable-network.md |
| `03` | NeoForge/src/platform-shared/java/com/tom/storagemod/menu/ItemFilterMenu.java | 03-terminal/item-filter-gui.md |
| `02` | NeoForge/src/platform-shared/java/com/tom/storagemod/menu/LevelEmitterMenu.java | 02-storage/level-emitter.md |
| `03` | NeoForge/src/platform-shared/java/com/tom/storagemod/menu/StorageTerminalMenu.java | 03-terminal/storage-terminal.md |
| `03` | NeoForge/src/platform-shared/java/com/tom/storagemod/menu/TagItemFilterMenu.java | 03-terminal/item-filter-gui.md |
| `03` | NeoForge/src/platform-shared/java/com/tom/storagemod/menu/TerminalCraftingFiller.java | 03-terminal/crafting-terminal.md |
| `03` | NeoForge/src/platform-shared/java/com/tom/storagemod/menu/slot/FilterSlot.java | 03-terminal/item-filter-gui.md |
| `03` | NeoForge/src/platform-shared/java/com/tom/storagemod/menu/slot/ItemFilterSlot.java | 03-terminal/item-filter-gui.md |
| `03` | NeoForge/src/platform-shared/java/com/tom/storagemod/menu/slot/PhantomSlot.java | 03-terminal/item-filter-gui.md |
| `01` | NeoForge/src/platform-shared/java/com/tom/storagemod/network/DataPacket.java | 01-core/network.md |
| `01` | NeoForge/src/platform-shared/java/com/tom/storagemod/network/OpenTerminalPacket.java | 01-core/network.md |
| `01` | NeoForge/src/platform-shared/java/com/tom/storagemod/polymorph/PolymorphHelper.java | 01-core/util-api.md |
| `01` | NeoForge/src/platform-shared/java/com/tom/storagemod/polymorph/PolymorphTerminalWidget.java | 01-core/util-api.md |
| `05` | NeoForge/src/platform-shared/java/com/tom/storagemod/rei/REIPlugin.java | 05-integration/rei.md |
| `05` | NeoForge/src/platform-shared/java/com/tom/storagemod/rei/ReiGhostIngredientHandler.java | 05-integration/rei.md |
| `05` | NeoForge/src/platform-shared/java/com/tom/storagemod/rei/ReiTransferHandler.java | 05-integration/rei.md |
| `03` | NeoForge/src/platform-shared/java/com/tom/storagemod/screen/AbstractFilteredScreen.java | 03-terminal/item-filter-gui.md |
| `03` | NeoForge/src/platform-shared/java/com/tom/storagemod/screen/AbstractStorageTerminalScreen.java | 03-terminal/storage-terminal.md |
| `03` | NeoForge/src/platform-shared/java/com/tom/storagemod/screen/CraftingTerminalScreen.java | 03-terminal/crafting-terminal.md |
| `03` | NeoForge/src/platform-shared/java/com/tom/storagemod/screen/FilingCabinetScreen.java | 03-terminal/filing-cabinet-gui.md |
| `03` | NeoForge/src/platform-shared/java/com/tom/storagemod/screen/IScreen.java | 03-terminal/container-screens.md |
| `03` | NeoForge/src/platform-shared/java/com/tom/storagemod/screen/InventoryConfiguratorScreen.java | 03-terminal/inventory-configurator.md |
| `03` | NeoForge/src/platform-shared/java/com/tom/storagemod/screen/InventoryLinkScreen.java | 03-terminal/inventory-link.md |
| `03` | NeoForge/src/platform-shared/java/com/tom/storagemod/screen/ItemFilterScreen.java | 03-terminal/item-filter-gui.md |
| `03` | NeoForge/src/platform-shared/java/com/tom/storagemod/screen/LevelEmitterScreen.java | 03-terminal/level-emitter-gui.md |
| `03` | NeoForge/src/platform-shared/java/com/tom/storagemod/screen/RecipeBookButton.java | 03-terminal/crafting-terminal.md |
| `03` | NeoForge/src/platform-shared/java/com/tom/storagemod/screen/StorageTerminalScreen.java | 03-terminal/storage-terminal.md |
| `03` | NeoForge/src/platform-shared/java/com/tom/storagemod/screen/TSContainerScreen.java | 03-terminal/container-screens.md |
| `03` | NeoForge/src/platform-shared/java/com/tom/storagemod/screen/TagItemFilterScreen.java | 03-terminal/item-filter-gui.md |
| `06` | NeoForge/src/platform-shared/java/com/tom/storagemod/screen/widget/CraftingTerminalRecipeBookWidget.java | 06-client-ui/widgets.md |
| `06` | NeoForge/src/platform-shared/java/com/tom/storagemod/screen/widget/EnumCycleButton.java | 06-client-ui/widgets.md |
| `06` | NeoForge/src/platform-shared/java/com/tom/storagemod/screen/widget/IconButton.java | 06-client-ui/widgets.md |
| `06` | NeoForge/src/platform-shared/java/com/tom/storagemod/screen/widget/ListWidget.java | 06-client-ui/widgets.md |
| `06` | NeoForge/src/platform-shared/java/com/tom/storagemod/screen/widget/TerminalSearchModeButton.java | 06-client-ui/widgets.md |
| `06` | NeoForge/src/platform-shared/java/com/tom/storagemod/screen/widget/ToggleButton.java | 06-client-ui/widgets.md |
| `03` | NeoForge/src/platform-shared/java/com/tom/storagemod/util/AbstractSimpleItemFilter.java | 03-terminal/filters.md |
| `03` | NeoForge/src/platform-shared/java/com/tom/storagemod/util/AbstractTagFilter.java | 03-terminal/filters.md |
| `02` | NeoForge/src/platform-shared/java/com/tom/storagemod/util/BasicContainer.java | 02-storage/containers.md |
| `01` | NeoForge/src/platform-shared/java/com/tom/storagemod/util/BeaconLevelCalc.java | 01-core/util-misc.md |
| `01` | NeoForge/src/platform-shared/java/com/tom/storagemod/util/BlockFace.java | 01-core/util-misc.md |
| `01` | NeoForge/src/platform-shared/java/com/tom/storagemod/util/BlockFaceReference.java | 01-core/util-misc.md |
| `01` | NeoForge/src/platform-shared/java/com/tom/storagemod/util/ComponentJoiner.java | 01-core/util-misc.md |
| `03` | NeoForge/src/platform-shared/java/com/tom/storagemod/util/CraftingMatrix.java | 03-terminal/terminal-utils.md |
| `01` | NeoForge/src/platform-shared/java/com/tom/storagemod/util/DataSlots.java | 01-core/util-data-slots.md |
| `02` | NeoForge/src/platform-shared/java/com/tom/storagemod/util/FilingCabinetContainer.java | 02-storage/filing-cabinet.md |
| `01` | NeoForge/src/platform-shared/java/com/tom/storagemod/util/FloatSupplier.java | 01-core/util-misc.md |
| `03` | NeoForge/src/platform-shared/java/com/tom/storagemod/util/IAutoFillTerminal.java | 03-terminal/terminal-utils.md |
| `03` | NeoForge/src/platform-shared/java/com/tom/storagemod/util/IDataReceiver.java | 03-terminal/terminal-utils.md |
| `03` | NeoForge/src/platform-shared/java/com/tom/storagemod/util/IValidInfo.java | 03-terminal/terminal-utils.md |
| `01` | NeoForge/src/platform-shared/java/com/tom/storagemod/util/IntDataSlots.java | 01-core/util-data-slots.md |
| `01` | NeoForge/src/platform-shared/java/com/tom/storagemod/util/InventoryConnectorConfigUtil.java | 01-core/util-misc.md |
| `03` | NeoForge/src/platform-shared/java/com/tom/storagemod/util/KeyUtil.java | 03-terminal/terminal-utils.md |
| `02` | NeoForge/src/platform-shared/java/com/tom/storagemod/util/LimitedContainer.java | 02-storage/containers.md |
| `03` | NeoForge/src/platform-shared/java/com/tom/storagemod/util/NumberFormatUtil.java | 03-terminal/terminal-utils.md |
| `01` | NeoForge/src/platform-shared/java/com/tom/storagemod/util/PlayerInvUtil.java | 01-core/util-misc.md |
| `01` | NeoForge/src/platform-shared/java/com/tom/storagemod/util/PopupMenuManager.java | 01-core/util-misc.md |
| `01` | NeoForge/src/platform-shared/java/com/tom/storagemod/util/Priority.java | 01-core/util-misc.md |
| `01` | NeoForge/src/platform-shared/java/com/tom/storagemod/util/TerminalSyncManager.java | 01-core/util-misc.md |
| `01` | NeoForge/src/platform-shared/java/com/tom/storagemod/util/Util.java | 01-core/util-misc.md |

## Coverage notes

- `01-core` (57): entry/lifecycle/registration (`mod-lifecycle.md`, incl. `StorageModClient` NF/FB and `ServerEvents`), config (`config.md`), network (`network.md`), platform layer + inventory-access adapters (`platform-layer.md`, `inventory-access.md`), terminal sync (`terminal-sync.md`), and the utility classes (`util-misc.md`, `util-api.md`, `util-filters.md`, `util-data-slots.md`). Utility containers (`BasicContainer`, `LimitedContainer`, `FilingCabinetContainer`) and `CraftingMatrix` are assigned to the areas that own their behavior (`02-storage/containers.md`, `02-storage/filing-cabinet.md`, `03-terminal/terminal-utils.md`); `BeaconLevelCalc` is catalogued in `01-core/util-misc.md` with its behavior in `04-wireless/range-and-sync.md`.
- `02-storage` (68): blocks, block entities, cable network, components, items, `PaintedBlockEntity` (NF).
- `03-terminal` (55): terminal blocks/entities, menus, screens, filter items + filters + sorting, terminal stacks, `MultiInventoryAccess`/`VanillaMultiblockInventories`, and the screen base classes (`IScreen`, `TSContainerScreen`, `PlatformContainerScreen` NF/FB, `RecipeBookButton`) documented in `container-screens.md`/`crafting-terminal.md`.
- `04-wireless` (3): the two wireless terminal items and the `WirelessTerminal` interface; beacon calculation behavior in `range-and-sync.md`.
- `05-integration` (21): `jei` (3), `rei` (4 incl. `REIPluginLoader` NF), `emi` (3), `jade` (8), `SophisticatedDoubleBlocks`, `ModMenu` (FB), `TheOneProbeHandler` (NF).
- `06-client-ui` (13): `screen/widget` (6), `client` (3), painted-model classes (NF 2, FB 2).
- `05-integration` files in scope: **21**, all read (see [05 index](05-integration/index.md)). The multiblock-inventory API, `PolymorphHelper`/`PolymorphTerminalWidget` and `EventHandler` sit in the upstream `api`/`polymorph` packages but are documented by `01-core/util-api.md`; `VanillaMultiblockInventories` and `IAutoFillTerminal` by `03-terminal`; `ServerEvents` by `01-core/mod-lifecycle.md`. This area's `other-compat.md` covers the integration-side usage of those classes.
- `06-client-ui` files in scope: **13**, all read (see [06 index](06-client-ui/index.md)). `StorageModClient` (NF+FB) is assigned to `01-core/mod-lifecycle.md` (client setup) but is also documented in detail in `06-client-ui/client-package.md`; the screen base classes are assigned to `03-terminal` but documented here as GUI-kit infrastructure.

## Resources (271, by group; counts verified against `git ls-tree -r 26.2`)

| Area | Count | Files | Sheet |
|---|---|---|---|
| `06-client-ui` | 83 | `RES/assets/toms_storage/lang/*.json` (7: en_us 217 keys + 6 translations), `atlases/gui.json` (1), `textures/gui/*.png` (7 GUI backgrounds), `textures/gui/sprites/icons/*.png` (45 icon sprites), `textures/gui/sprites/widget/*` (23 widget sprite files incl. 8 nine-slice mcmeta) | [lang.md](06-client-ui/lang.md), [textures-sounds.md](06-client-ui/textures-sounds.md) |
| `02-storage` | 108 | `textures/block/*` (16, non-terminal), `textures/item/{inventory_configurator,paint_kit}.png` (2), `items/*.json` item models (15), `models/item/*.json` (4: cable, framed cable, configurator, paint kit), `models/block/*.json` + `models/block/cable_connector/*` (19), `blockstates/*.json` (13), `data/toms_storage/recipe/*.json` (21), `data/toms_storage/loot_table/blocks/*.json` (13), `data/toms_storage/advancement/*.json` (5) | the 02-storage block sheets: [filing-cabinet.md](02-storage/filing-cabinet.md), [open-crate.md](02-storage/open-crate.md), [trims-paint.md](02-storage/trims-paint.md), [cables.md](02-storage/cables.md), [cable-connector.md](02-storage/cable-connector.md), [inventory-connector.md](02-storage/inventory-connector.md), [inventory-proxy.md](02-storage/inventory-proxy.md), [inventory-interface.md](02-storage/inventory-interface.md), [level-emitter.md](02-storage/level-emitter.md), [hopper.md](02-storage/hopper.md), [items.md](02-storage/items.md), [containers.md](02-storage/containers.md) |
| `03-terminal` | 22 | `textures/block/{terminal_front,terminal_side,crafting_terminal_front}.png` (3), `textures/item/{item_filter,tag_item_filter,poly_item_filter}.png` (3), `items/*.json` for storage/crafting terminal + the 3 filter items (5), `models/item/*.json` (3), `models/block/{storage_terminal,crafting_terminal}.json` (2), `blockstates/{storage_terminal,crafting_terminal}.json` (2), `data/toms_storage/recipe/{storage_terminal,crafting_terminal}.json` (2), `data/toms_storage/loot_table/blocks/{storage_terminal,crafting_terminal}.json` (2) | the 03-terminal sheets: [terminal-blocks.md](03-terminal/terminal-blocks.md), [item-filter-gui.md](03-terminal/item-filter-gui.md), [inventory-configurator.md](03-terminal/inventory-configurator.md), [level-emitter-gui.md](03-terminal/level-emitter-gui.md), [filing-cabinet-gui.md](03-terminal/filing-cabinet-gui.md) |
| `04-wireless` | 14 | `NeoForge/libs/curios_dummy.jar`, `Fabric/libs/trinkets_dummy.jar` (2), curios/trinkets belt data (NF `data/curios/tags/item/belt.json`, `data/toms_storage/curios/entities/player_belt.json`; FB `data/trinkets/entities/toms_storage.json`, `data/trinkets/tags/item/legs/belt.json` = 4), `items/{wireless_terminal,adv_wireless_terminal}.json` (2), `models/item/` same 2 (2), `textures/item/` same 2 (2), `data/toms_storage/recipe/` same 2 (2) | [wireless-items.md](04-wireless/wireless-items.md) |
| `01-core` | 41 | build & repo metadata: `NeoForge/` + `Fabric/` `{build.gradle, settings.gradle, gradle.properties, gradlew, gradlew.bat}` (10) + `gradle/wrapper/*` (4) + `.gitignore`/`project.json` per loader (4) + root `LICENSE`, `README.md`, `banner.png`, `icon.png` (`RES`), `version-check.json`, `version-check-nf.json`, `.github/FUNDING.yml` (8); mod metadata: `META-INF/{neoforge.mods.toml, accesstransformer.cfg}` (2), `tomsstorage.accesswidener` (NF main + FB = 2), `fabric.mod.json` (1), `pack.mcmeta` (1); data tags: `data/toms_storage/tags/block/*.json` (4: `inventory_configurator_skip`, `inventory_connector_skip`, `remote_activate`, `trims` — the `StorageTags` registry), `data/toms_storage/tags/item/trims.json` (1), `data/minecraft/tags/block/mineable/*.json` (NF 2 + FB 2 = 4), FB `data/c/tags/item/{ender_pearls.json, glass_blocks/colorless.json}` (2) | [mod-lifecycle.md](01-core/mod-lifecycle.md) (`StorageTags`/registration; `remote_activate` consumed by `04`) |
| `05-integration` | 3 | compat compile stubs: `NeoForge/libs/{iris.jar, polymorph_dummy.jar, vc_dummy.jar}` (Iris is consumed by the `06` client pipeline; ViveCraft by `06` VR helper; Polymorph by `05`) | [other-compat.md](05-integration/other-compat.md), [client-package.md](06-client-ui/client-package.md) |

Resource total check: 83 + 108 + 22 + 14 + 41 + 3 = 271.
