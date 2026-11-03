# Coverage

Every upstream Java file and resource, and the sheet that covers it. Upstream = Tom's Simple Storage `2.11.3`, branch `26.2`, commit `95bb61d`. Path prefixes: `SH` = `NeoForge/src/platform-shared/java/com/tom/storagemod/`, `NF` = `NeoForge/src/main/java/com/tom/storagemod/`, `FB` = `Fabric/src/main/java/com/tom/storagemod/`, `RES` = `NeoForge/src/platform-shared/resources/`.

**Totals (verified):** `git ls-tree -r 26.2 --name-only | grep -c '\.java$'` = **217** Java files; **488** files in the tree overall (271 non-Java). The per-area table below covers all 217 Java files individually (217 rows) and all 271 non-Java files by group (counts verified against `git ls-tree`).

## Per-area summary

| Area | Java files in scope | Sheets | Notes |
|---|---|---|---|
| `01-core` | 49 | pending S1 part 1 | lifecycle, registration, config, network, platform layer, shared utils; mod metadata/build files |
| `02-storage` | 70 | pending S1 part 1 | storage network blocks (cables, connectors, proxies, interfaces, hoppers, level emitter, filing cabinet, open crate, trim/painted trim), their block entities/menus/screens, item components, inventory network |
| `03-terminal` | 44 | pending S1 part 2 | terminal block/entity/menu, terminal + filter screens, filter and sorting systems, inventory stack logic |
| `04-wireless` | 4 | pending S1 part 2 | wireless terminal items + beacon range calc; curios/trinkets belt data |
| `05-integration` | 28 | [index](05-integration/index.md), [jei](05-integration/jei.md), [rei](05-integration/rei.md), [emi](05-integration/emi.md), [jade](05-integration/jade.md), [other-compat](05-integration/other-compat.md) | recipe viewers, Jade, TOP, Polymorph, Sophisticated, multiblock API, misc compat events |
| `06-client-ui` | 22 | [index](06-client-ui/index.md), [widgets](06-client-ui/widgets.md), [client-package](06-client-ui/client-package.md), [painted-models](06-client-ui/painted-models.md), [lang](06-client-ui/lang.md), [textures-sounds](06-client-ui/textures-sounds.md) | widget kit, screen base classes, client package, painted models, `StorageModClient` (both loaders — the `01-core` lane had not committed them when this was written), lang + GUI resources |

Assignment rules for files that sit between areas (stated per the README): viewer plugin packages and the shared `IAutoFillTerminal` seam → `05` (they document the integration contract); `Screen`/`ScreenBase` infrastructure + `PopupMenuManager` + `RecipeBookButton` → `06` (GUI kit), while the concrete terminal/filter screens → `03` (they present the terminal system) and the configurator/link/emitter/cabinet screens → `02` (they present storage-network systems); `ServerEvents` (NF) → `05` (compat event surface, cross-referenced from `02`/`03`); `StorageModClient` (NF+FB) → `06` (client entry point documented there, cross-linkable from `01` later).

## Java files (217)

| Area | File | Sheet |
|---|---|---|
| `01` | Fabric/src/main/java/com/tom/storagemod/Config.java | pending S1 part 1 |
| `05` | Fabric/src/main/java/com/tom/storagemod/ModMenu.java | 05-integration/other-compat.md |
| `01` | Fabric/src/main/java/com/tom/storagemod/StorageMod.java | pending S1 part 1 |
| `06` | Fabric/src/main/java/com/tom/storagemod/StorageModClient.java | 06-client-ui/client-package.md |
| `01` | Fabric/src/main/java/com/tom/storagemod/StorageTags.java | pending S1 part 1 |
| `01` | Fabric/src/main/java/com/tom/storagemod/block/NeoForgeBlock.java | pending S1 part 1 |
| `02` | Fabric/src/main/java/com/tom/storagemod/block/entity/OpenCrateBlockEntity.java | pending S1 part 1 |
| `02` | Fabric/src/main/java/com/tom/storagemod/block/entity/PaintedBlockEntity.java | pending S1 part 1 |
| `06` | Fabric/src/main/java/com/tom/storagemod/client/BakedPaintedModel.java | 06-client-ui/painted-models.md |
| `06` | Fabric/src/main/java/com/tom/storagemod/client/UnbakedPaintedModel.java | 06-client-ui/painted-models.md |
| `02` | Fabric/src/main/java/com/tom/storagemod/inventory/InventoryChangeTracker.java | pending S1 part 1 |
| `02` | Fabric/src/main/java/com/tom/storagemod/inventory/InventoryImage.java | pending S1 part 1 |
| `02` | Fabric/src/main/java/com/tom/storagemod/inventory/InventorySlot.java | pending S1 part 1 |
| `02` | Fabric/src/main/java/com/tom/storagemod/inventory/PlatformFilteredInventoryAccess.java | pending S1 part 1 |
| `02` | Fabric/src/main/java/com/tom/storagemod/inventory/PlatformInventoryAccess.java | pending S1 part 1 |
| `02` | Fabric/src/main/java/com/tom/storagemod/inventory/PlatformItemHandler.java | pending S1 part 1 |
| `02` | Fabric/src/main/java/com/tom/storagemod/inventory/PlatformMultiInventoryAccess.java | pending S1 part 1 |
| `02` | Fabric/src/main/java/com/tom/storagemod/inventory/PlatformProxyInventoryAccess.java | pending S1 part 1 |
| `03` | Fabric/src/main/java/com/tom/storagemod/inventory/filter/PolyFilter.java | pending S1 part 2 |
| `01` | Fabric/src/main/java/com/tom/storagemod/network/NetworkHandler.java | pending S1 part 1 |
| `01` | Fabric/src/main/java/com/tom/storagemod/platform/GameObject.java | pending S1 part 1 |
| `01` | Fabric/src/main/java/com/tom/storagemod/platform/Platform.java | pending S1 part 1 |
| `01` | Fabric/src/main/java/com/tom/storagemod/platform/PlatformBlockEntity.java | pending S1 part 1 |
| `01` | Fabric/src/main/java/com/tom/storagemod/platform/PlatformItem.java | pending S1 part 1 |
| `06` | Fabric/src/main/java/com/tom/storagemod/screen/PlatformContainerScreen.java | 06-client-ui/widgets.md |
| `01` | Fabric/src/main/java/com/tom/storagemod/util/TickerUtil.java | pending S1 part 1 |
| `01` | Fabric/src/main/java/com/tom/storagemod/util/WorldStates.java | pending S1 part 1 |
| `01` | NeoForge/src/main/java/com/tom/storagemod/Config.java | pending S1 part 1 |
| `05` | NeoForge/src/main/java/com/tom/storagemod/ServerEvents.java | 05-integration/other-compat.md |
| `01` | NeoForge/src/main/java/com/tom/storagemod/StorageMod.java | pending S1 part 1 |
| `06` | NeoForge/src/main/java/com/tom/storagemod/StorageModClient.java | 06-client-ui/client-package.md |
| `01` | NeoForge/src/main/java/com/tom/storagemod/StorageTags.java | pending S1 part 1 |
| `01` | NeoForge/src/main/java/com/tom/storagemod/block/NeoForgeBlock.java | pending S1 part 1 |
| `02` | NeoForge/src/main/java/com/tom/storagemod/block/entity/BlockFilterAttachment.java | pending S1 part 1 |
| `02` | NeoForge/src/main/java/com/tom/storagemod/block/entity/OpenCrateBlockEntity.java | pending S1 part 1 |
| `02` | NeoForge/src/main/java/com/tom/storagemod/block/entity/PaintedBlockEntity.java | pending S1 part 1 |
| `06` | NeoForge/src/main/java/com/tom/storagemod/client/BakedPaintedModel.java | 06-client-ui/painted-models.md |
| `06` | NeoForge/src/main/java/com/tom/storagemod/client/PaintedExtension.java | 06-client-ui/painted-models.md |
| `01` | NeoForge/src/main/java/com/tom/storagemod/inventory/EmptyInventory.java | pending S1 part 1 |
| `01` | NeoForge/src/main/java/com/tom/storagemod/inventory/InventoryChangeTracker.java | pending S1 part 1 |
| `01` | NeoForge/src/main/java/com/tom/storagemod/inventory/InventorySlot.java | pending S1 part 1 |
| `01` | NeoForge/src/main/java/com/tom/storagemod/inventory/NeoStack.java | pending S1 part 1 |
| `01` | NeoForge/src/main/java/com/tom/storagemod/inventory/PlatformFilteredInventoryAccess.java | pending S1 part 1 |
| `01` | NeoForge/src/main/java/com/tom/storagemod/inventory/PlatformInventoryAccess.java | pending S1 part 1 |
| `01` | NeoForge/src/main/java/com/tom/storagemod/inventory/PlatformItemHandler.java | pending S1 part 1 |
| `01` | NeoForge/src/main/java/com/tom/storagemod/inventory/PlatformMultiInventoryAccess.java | pending S1 part 1 |
| `01` | NeoForge/src/main/java/com/tom/storagemod/inventory/PlatformProxyInventoryAccess.java | pending S1 part 1 |
| `01` | NeoForge/src/main/java/com/tom/storagemod/inventory/filter/PolyFilter.java | pending S1 part 1 |
| `01` | NeoForge/src/main/java/com/tom/storagemod/network/NetworkHandler.java | pending S1 part 1 |
| `01` | NeoForge/src/main/java/com/tom/storagemod/platform/GameObject.java | pending S1 part 1 |
| `01` | NeoForge/src/main/java/com/tom/storagemod/platform/Platform.java | pending S1 part 1 |
| `01` | NeoForge/src/main/java/com/tom/storagemod/platform/PlatformBlockEntity.java | pending S1 part 1 |
| `01` | NeoForge/src/main/java/com/tom/storagemod/platform/PlatformItem.java | pending S1 part 1 |
| `05` | NeoForge/src/main/java/com/tom/storagemod/rei/REIPluginLoader.java | 05-integration/rei.md |
| `06` | NeoForge/src/main/java/com/tom/storagemod/screen/PlatformContainerScreen.java | 06-client-ui/widgets.md |
| `05` | NeoForge/src/main/java/com/tom/storagemod/top/TheOneProbeHandler.java | 05-integration/other-compat.md |
| `01` | NeoForge/src/main/java/com/tom/storagemod/util/TickerUtil.java | pending S1 part 1 |
| `01` | NeoForge/src/main/java/com/tom/storagemod/util/WorldStates.java | pending S1 part 1 |
| `01` | NeoForge/src/platform-shared/java/com/tom/storagemod/Content.java | pending S1 part 1 |
| `05` | NeoForge/src/platform-shared/java/com/tom/storagemod/api/EventHandler.java | 05-integration/other-compat.md |
| `05` | NeoForge/src/platform-shared/java/com/tom/storagemod/api/MultiblockInventoryAPI.java | 05-integration/other-compat.md |
| `02` | NeoForge/src/platform-shared/java/com/tom/storagemod/block/AbstractInventoryHopperBlock.java | pending S1 part 1 |
| `03` | NeoForge/src/platform-shared/java/com/tom/storagemod/block/AbstractStorageTerminalBlock.java | pending S1 part 2 |
| `02` | NeoForge/src/platform-shared/java/com/tom/storagemod/block/BasicInventoryHopperBlock.java | pending S1 part 1 |
| `02` | NeoForge/src/platform-shared/java/com/tom/storagemod/block/BlockWithTooltip.java | pending S1 part 1 |
| `03` | NeoForge/src/platform-shared/java/com/tom/storagemod/block/CraftingTerminalBlock.java | pending S1 part 2 |
| `02` | NeoForge/src/platform-shared/java/com/tom/storagemod/block/FilingCabinetBlock.java | pending S1 part 1 |
| `02` | NeoForge/src/platform-shared/java/com/tom/storagemod/block/FramedInventoryCableBlock.java | pending S1 part 1 |
| `02` | NeoForge/src/platform-shared/java/com/tom/storagemod/block/FramedInventoryCableConnectorBlock.java | pending S1 part 1 |
| `02` | NeoForge/src/platform-shared/java/com/tom/storagemod/block/IConfiguratorHighlight.java | pending S1 part 1 |
| `02` | NeoForge/src/platform-shared/java/com/tom/storagemod/block/IInventoryCable.java | pending S1 part 1 |
| `02` | NeoForge/src/platform-shared/java/com/tom/storagemod/block/IInventoryNode.java | pending S1 part 1 |
| `02` | NeoForge/src/platform-shared/java/com/tom/storagemod/block/IPaintable.java | pending S1 part 1 |
| `02` | NeoForge/src/platform-shared/java/com/tom/storagemod/block/InventoryCableBlock.java | pending S1 part 1 |
| `02` | NeoForge/src/platform-shared/java/com/tom/storagemod/block/InventoryCableConnectorBlock.java | pending S1 part 1 |
| `02` | NeoForge/src/platform-shared/java/com/tom/storagemod/block/InventoryConnectorBlock.java | pending S1 part 1 |
| `02` | NeoForge/src/platform-shared/java/com/tom/storagemod/block/InventoryInterfaceBlock.java | pending S1 part 1 |
| `02` | NeoForge/src/platform-shared/java/com/tom/storagemod/block/InventoryProxyBlock.java | pending S1 part 1 |
| `02` | NeoForge/src/platform-shared/java/com/tom/storagemod/block/LevelEmitterBlock.java | pending S1 part 1 |
| `02` | NeoForge/src/platform-shared/java/com/tom/storagemod/block/OpenCrateBlock.java | pending S1 part 1 |
| `02` | NeoForge/src/platform-shared/java/com/tom/storagemod/block/PaintedTrimBlock.java | pending S1 part 1 |
| `03` | NeoForge/src/platform-shared/java/com/tom/storagemod/block/StorageTerminalBlock.java | pending S1 part 2 |
| `02` | NeoForge/src/platform-shared/java/com/tom/storagemod/block/TrimBlock.java | pending S1 part 1 |
| `02` | NeoForge/src/platform-shared/java/com/tom/storagemod/block/entity/AbstractInventoryHopperBlockEntity.java | pending S1 part 1 |
| `02` | NeoForge/src/platform-shared/java/com/tom/storagemod/block/entity/AbstractPainedBlockEntity.java | pending S1 part 1 |
| `02` | NeoForge/src/platform-shared/java/com/tom/storagemod/block/entity/BasicInventoryHopperBlockEntity.java | pending S1 part 1 |
| `02` | NeoForge/src/platform-shared/java/com/tom/storagemod/block/entity/CraftingTerminalBlockEntity.java | pending S1 part 1 |
| `02` | NeoForge/src/platform-shared/java/com/tom/storagemod/block/entity/FilingCabinetBlockEntity.java | pending S1 part 1 |
| `02` | NeoForge/src/platform-shared/java/com/tom/storagemod/block/entity/IInventoryConnector.java | pending S1 part 1 |
| `02` | NeoForge/src/platform-shared/java/com/tom/storagemod/block/entity/InventoryCableConnectorBlockEntity.java | pending S1 part 1 |
| `02` | NeoForge/src/platform-shared/java/com/tom/storagemod/block/entity/InventoryConnectorBlockEntity.java | pending S1 part 1 |
| `02` | NeoForge/src/platform-shared/java/com/tom/storagemod/block/entity/InventoryInterfaceBlockEntity.java | pending S1 part 1 |
| `02` | NeoForge/src/platform-shared/java/com/tom/storagemod/block/entity/InventoryProxyBlockEntity.java | pending S1 part 1 |
| `02` | NeoForge/src/platform-shared/java/com/tom/storagemod/block/entity/LevelEmitterBlockEntity.java | pending S1 part 1 |
| `02` | NeoForge/src/platform-shared/java/com/tom/storagemod/block/entity/StorageTerminalBlockEntity.java | pending S1 part 1 |
| `06` | NeoForge/src/platform-shared/java/com/tom/storagemod/client/ClientUtil.java | 06-client-ui/client-package.md |
| `06` | NeoForge/src/platform-shared/java/com/tom/storagemod/client/CustomRenderTypes.java | 06-client-ui/client-package.md |
| `06` | NeoForge/src/platform-shared/java/com/tom/storagemod/client/ViveCraftHelper.java | 06-client-ui/client-package.md |
| `02` | NeoForge/src/platform-shared/java/com/tom/storagemod/components/ConfiguratorComponent.java | pending S1 part 1 |
| `02` | NeoForge/src/platform-shared/java/com/tom/storagemod/components/SimpleItemFilterComponent.java | pending S1 part 1 |
| `02` | NeoForge/src/platform-shared/java/com/tom/storagemod/components/TagFilterComponent.java | pending S1 part 1 |
| `02` | NeoForge/src/platform-shared/java/com/tom/storagemod/components/WorldPos.java | pending S1 part 1 |
| `05` | NeoForge/src/platform-shared/java/com/tom/storagemod/emi/EMIPlugin.java | 05-integration/emi.md |
| `05` | NeoForge/src/platform-shared/java/com/tom/storagemod/emi/EmiGhostIngredientHandler.java | 05-integration/emi.md |
| `05` | NeoForge/src/platform-shared/java/com/tom/storagemod/emi/EmiTransferHandler.java | 05-integration/emi.md |
| `03` | NeoForge/src/platform-shared/java/com/tom/storagemod/inventory/BlockFilter.java | pending S1 part 2 |
| `02` | NeoForge/src/platform-shared/java/com/tom/storagemod/inventory/IChangeTrackerAccess.java | pending S1 part 1 |
| `02` | NeoForge/src/platform-shared/java/com/tom/storagemod/inventory/IInventoryAccess.java | pending S1 part 1 |
| `02` | NeoForge/src/platform-shared/java/com/tom/storagemod/inventory/IInventoryConnectorReference.java | pending S1 part 1 |
| `02` | NeoForge/src/platform-shared/java/com/tom/storagemod/inventory/IInventoryLink.java | pending S1 part 1 |
| `02` | NeoForge/src/platform-shared/java/com/tom/storagemod/inventory/IProxy.java | pending S1 part 1 |
| `02` | NeoForge/src/platform-shared/java/com/tom/storagemod/inventory/InventoryCableNetwork.java | pending S1 part 1 |
| `02` | NeoForge/src/platform-shared/java/com/tom/storagemod/inventory/MultiInventoryAccess.java | pending S1 part 1 |
| `02` | NeoForge/src/platform-shared/java/com/tom/storagemod/inventory/NetworkInventory.java | pending S1 part 1 |
| `02` | NeoForge/src/platform-shared/java/com/tom/storagemod/inventory/RemoteConnections.java | pending S1 part 1 |
| `03` | NeoForge/src/platform-shared/java/com/tom/storagemod/inventory/StoredItemStack.java | pending S1 part 2 |
| `03` | NeoForge/src/platform-shared/java/com/tom/storagemod/inventory/TerminalItemStack.java | pending S1 part 2 |
| `05` | NeoForge/src/platform-shared/java/com/tom/storagemod/inventory/VanillaMultiblockInventories.java | 05-integration/other-compat.md |
| `05` | NeoForge/src/platform-shared/java/com/tom/storagemod/inventory/compat/SophisticatedDoubleBlocks.java | 05-integration/other-compat.md |
| `03` | NeoForge/src/platform-shared/java/com/tom/storagemod/inventory/filter/IFilter.java | pending S1 part 2 |
| `03` | NeoForge/src/platform-shared/java/com/tom/storagemod/inventory/filter/ItemFilter.java | pending S1 part 2 |
| `03` | NeoForge/src/platform-shared/java/com/tom/storagemod/inventory/filter/ItemPredicate.java | pending S1 part 2 |
| `03` | NeoForge/src/platform-shared/java/com/tom/storagemod/inventory/filter/SimpleItemFilter.java | pending S1 part 2 |
| `03` | NeoForge/src/platform-shared/java/com/tom/storagemod/inventory/filter/TagFilter.java | pending S1 part 2 |
| `03` | NeoForge/src/platform-shared/java/com/tom/storagemod/inventory/sorting/ComparatorAmount.java | pending S1 part 2 |
| `03` | NeoForge/src/platform-shared/java/com/tom/storagemod/inventory/sorting/ComparatorID.java | pending S1 part 2 |
| `03` | NeoForge/src/platform-shared/java/com/tom/storagemod/inventory/sorting/ComparatorMod.java | pending S1 part 2 |
| `03` | NeoForge/src/platform-shared/java/com/tom/storagemod/inventory/sorting/ComparatorName.java | pending S1 part 2 |
| `03` | NeoForge/src/platform-shared/java/com/tom/storagemod/inventory/sorting/ComparatorSpaceEfficiency.java | pending S1 part 2 |
| `03` | NeoForge/src/platform-shared/java/com/tom/storagemod/inventory/sorting/ComparatorType.java | pending S1 part 2 |
| `03` | NeoForge/src/platform-shared/java/com/tom/storagemod/inventory/sorting/SortingTypes.java | pending S1 part 2 |
| `04` | NeoForge/src/platform-shared/java/com/tom/storagemod/item/AdvWirelessTerminalItem.java | pending S1 part 2 |
| `02` | NeoForge/src/platform-shared/java/com/tom/storagemod/item/BlockItemTSS.java | pending S1 part 1 |
| `03` | NeoForge/src/platform-shared/java/com/tom/storagemod/item/FilterItem.java | pending S1 part 2 |
| `03` | NeoForge/src/platform-shared/java/com/tom/storagemod/item/IItemFilter.java | pending S1 part 2 |
| `02` | NeoForge/src/platform-shared/java/com/tom/storagemod/item/ILeftClickListener.java | pending S1 part 1 |
| `02` | NeoForge/src/platform-shared/java/com/tom/storagemod/item/InventoryConfiguratorItem.java | pending S1 part 1 |
| `02` | NeoForge/src/platform-shared/java/com/tom/storagemod/item/PaintKitItem.java | pending S1 part 1 |
| `02` | NeoForge/src/platform-shared/java/com/tom/storagemod/item/PaintedBlockItem.java | pending S1 part 1 |
| `03` | NeoForge/src/platform-shared/java/com/tom/storagemod/item/PolyFilterItem.java | pending S1 part 2 |
| `03` | NeoForge/src/platform-shared/java/com/tom/storagemod/item/TagFilterItem.java | pending S1 part 2 |
| `04` | NeoForge/src/platform-shared/java/com/tom/storagemod/item/WirelessTerminal.java | pending S1 part 2 |
| `04` | NeoForge/src/platform-shared/java/com/tom/storagemod/item/WirelessTerminalItem.java | pending S1 part 2 |
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
| `03` | NeoForge/src/platform-shared/java/com/tom/storagemod/menu/AbstractFilteredMenu.java | pending S1 part 2 |
| `03` | NeoForge/src/platform-shared/java/com/tom/storagemod/menu/CraftingTerminalMenu.java | pending S1 part 2 |
| `02` | NeoForge/src/platform-shared/java/com/tom/storagemod/menu/FilingCabinetMenu.java | pending S1 part 1 |
| `02` | NeoForge/src/platform-shared/java/com/tom/storagemod/menu/InventoryConfiguratorMenu.java | pending S1 part 1 |
| `02` | NeoForge/src/platform-shared/java/com/tom/storagemod/menu/InventoryLinkMenu.java | pending S1 part 1 |
| `03` | NeoForge/src/platform-shared/java/com/tom/storagemod/menu/ItemFilterMenu.java | pending S1 part 2 |
| `02` | NeoForge/src/platform-shared/java/com/tom/storagemod/menu/LevelEmitterMenu.java | pending S1 part 1 |
| `03` | NeoForge/src/platform-shared/java/com/tom/storagemod/menu/StorageTerminalMenu.java | pending S1 part 2 |
| `03` | NeoForge/src/platform-shared/java/com/tom/storagemod/menu/TagItemFilterMenu.java | pending S1 part 2 |
| `03` | NeoForge/src/platform-shared/java/com/tom/storagemod/menu/TerminalCraftingFiller.java | pending S1 part 2 |
| `03` | NeoForge/src/platform-shared/java/com/tom/storagemod/menu/slot/FilterSlot.java | pending S1 part 2 |
| `02` | NeoForge/src/platform-shared/java/com/tom/storagemod/menu/slot/ItemFilterSlot.java | pending S1 part 1 |
| `03` | NeoForge/src/platform-shared/java/com/tom/storagemod/menu/slot/PhantomSlot.java | pending S1 part 2 |
| `01` | NeoForge/src/platform-shared/java/com/tom/storagemod/network/DataPacket.java | pending S1 part 1 |
| `01` | NeoForge/src/platform-shared/java/com/tom/storagemod/network/OpenTerminalPacket.java | pending S1 part 1 |
| `05` | NeoForge/src/platform-shared/java/com/tom/storagemod/polymorph/PolymorphHelper.java | 05-integration/other-compat.md |
| `05` | NeoForge/src/platform-shared/java/com/tom/storagemod/polymorph/PolymorphTerminalWidget.java | 05-integration/other-compat.md |
| `05` | NeoForge/src/platform-shared/java/com/tom/storagemod/rei/REIPlugin.java | 05-integration/rei.md |
| `05` | NeoForge/src/platform-shared/java/com/tom/storagemod/rei/ReiGhostIngredientHandler.java | 05-integration/rei.md |
| `05` | NeoForge/src/platform-shared/java/com/tom/storagemod/rei/ReiTransferHandler.java | 05-integration/rei.md |
| `06` | NeoForge/src/platform-shared/java/com/tom/storagemod/screen/AbstractFilteredScreen.java | 06-client-ui/widgets.md |
| `03` | NeoForge/src/platform-shared/java/com/tom/storagemod/screen/AbstractStorageTerminalScreen.java | pending S1 part 2 |
| `03` | NeoForge/src/platform-shared/java/com/tom/storagemod/screen/CraftingTerminalScreen.java | pending S1 part 2 |
| `03` | NeoForge/src/platform-shared/java/com/tom/storagemod/screen/FilingCabinetScreen.java | pending S1 part 2 |
| `06` | NeoForge/src/platform-shared/java/com/tom/storagemod/screen/IScreen.java | 06-client-ui/widgets.md |
| `03` | NeoForge/src/platform-shared/java/com/tom/storagemod/screen/InventoryConfiguratorScreen.java | pending S1 part 2 |
| `03` | NeoForge/src/platform-shared/java/com/tom/storagemod/screen/InventoryLinkScreen.java | pending S1 part 2 |
| `03` | NeoForge/src/platform-shared/java/com/tom/storagemod/screen/ItemFilterScreen.java | pending S1 part 2 |
| `03` | NeoForge/src/platform-shared/java/com/tom/storagemod/screen/LevelEmitterScreen.java | pending S1 part 2 |
| `06` | NeoForge/src/platform-shared/java/com/tom/storagemod/screen/RecipeBookButton.java | 06-client-ui/widgets.md |
| `03` | NeoForge/src/platform-shared/java/com/tom/storagemod/screen/StorageTerminalScreen.java | pending S1 part 2 |
| `06` | NeoForge/src/platform-shared/java/com/tom/storagemod/screen/TSContainerScreen.java | 06-client-ui/widgets.md |
| `03` | NeoForge/src/platform-shared/java/com/tom/storagemod/screen/TagItemFilterScreen.java | pending S1 part 2 |
| `06` | NeoForge/src/platform-shared/java/com/tom/storagemod/screen/widget/CraftingTerminalRecipeBookWidget.java | 06-client-ui/widgets.md |
| `06` | NeoForge/src/platform-shared/java/com/tom/storagemod/screen/widget/EnumCycleButton.java | 06-client-ui/widgets.md |
| `06` | NeoForge/src/platform-shared/java/com/tom/storagemod/screen/widget/IconButton.java | 06-client-ui/widgets.md |
| `06` | NeoForge/src/platform-shared/java/com/tom/storagemod/screen/widget/ListWidget.java | 06-client-ui/widgets.md |
| `06` | NeoForge/src/platform-shared/java/com/tom/storagemod/screen/widget/TerminalSearchModeButton.java | 06-client-ui/widgets.md |
| `06` | NeoForge/src/platform-shared/java/com/tom/storagemod/screen/widget/ToggleButton.java | 06-client-ui/widgets.md |
| `03` | NeoForge/src/platform-shared/java/com/tom/storagemod/util/AbstractSimpleItemFilter.java | pending S1 part 2 |
| `03` | NeoForge/src/platform-shared/java/com/tom/storagemod/util/AbstractTagFilter.java | pending S1 part 2 |
| `01` | NeoForge/src/platform-shared/java/com/tom/storagemod/util/BasicContainer.java | pending S1 part 1 |
| `04` | NeoForge/src/platform-shared/java/com/tom/storagemod/util/BeaconLevelCalc.java | pending S1 part 2 |
| `01` | NeoForge/src/platform-shared/java/com/tom/storagemod/util/BlockFace.java | pending S1 part 1 |
| `01` | NeoForge/src/platform-shared/java/com/tom/storagemod/util/BlockFaceReference.java | pending S1 part 1 |
| `01` | NeoForge/src/platform-shared/java/com/tom/storagemod/util/ComponentJoiner.java | pending S1 part 1 |
| `03` | NeoForge/src/platform-shared/java/com/tom/storagemod/util/CraftingMatrix.java | pending S1 part 2 |
| `01` | NeoForge/src/platform-shared/java/com/tom/storagemod/util/DataSlots.java | pending S1 part 1 |
| `02` | NeoForge/src/platform-shared/java/com/tom/storagemod/util/FilingCabinetContainer.java | pending S1 part 1 |
| `01` | NeoForge/src/platform-shared/java/com/tom/storagemod/util/FloatSupplier.java | pending S1 part 1 |
| `05` | NeoForge/src/platform-shared/java/com/tom/storagemod/util/IAutoFillTerminal.java | 05-integration/jei.md (shared seam) |
| `01` | NeoForge/src/platform-shared/java/com/tom/storagemod/util/IDataReceiver.java | pending S1 part 1 |
| `01` | NeoForge/src/platform-shared/java/com/tom/storagemod/util/IValidInfo.java | pending S1 part 1 |
| `01` | NeoForge/src/platform-shared/java/com/tom/storagemod/util/IntDataSlots.java | pending S1 part 1 |
| `02` | NeoForge/src/platform-shared/java/com/tom/storagemod/util/InventoryConnectorConfigUtil.java | pending S1 part 1 |
| `01` | NeoForge/src/platform-shared/java/com/tom/storagemod/util/KeyUtil.java | pending S1 part 1 |
| `01` | NeoForge/src/platform-shared/java/com/tom/storagemod/util/LimitedContainer.java | pending S1 part 1 |
| `01` | NeoForge/src/platform-shared/java/com/tom/storagemod/util/NumberFormatUtil.java | pending S1 part 1 |
| `01` | NeoForge/src/platform-shared/java/com/tom/storagemod/util/PlayerInvUtil.java | pending S1 part 1 |
| `06` | NeoForge/src/platform-shared/java/com/tom/storagemod/util/PopupMenuManager.java | 06-client-ui/widgets.md |
| `02` | NeoForge/src/platform-shared/java/com/tom/storagemod/util/Priority.java | pending S1 part 1 |
| `03` | NeoForge/src/platform-shared/java/com/tom/storagemod/util/TerminalSyncManager.java | pending S1 part 2 |
| `01` | NeoForge/src/platform-shared/java/com/tom/storagemod/util/Util.java | pending S1 part 1 |
## Resources (271, by group; counts verified against `git ls-tree -r 26.2`)

| Area | Count | Files | Sheet |
|---|---|---|---|
| `06-client-ui` | 83 | `RES/assets/toms_storage/lang/*.json` (7: en_us 217 keys + 6 translations), `atlases/gui.json` (1), `textures/gui/*.png` (7 GUI backgrounds), `textures/gui/sprites/icons/*.png` (45 icon sprites), `textures/gui/sprites/widget/*` (23 widget sprite files incl. 8 nine-slice mcmeta) | [lang.md](06-client-ui/lang.md), [textures-sounds.md](06-client-ui/textures-sounds.md) |
| `02-storage` | 108 | `textures/block/*` (16, non-terminal), `textures/item/{inventory_configurator,paint_kit}.png` (2), `items/*.json` item models (15), `models/item/*.json` (4: cable, framed cable, configurator, paint kit), `models/block/*.json` + `models/block/cable_connector/*` (19), `blockstates/*.json` (13), `data/toms_storage/recipe/*.json` (21), `data/toms_storage/loot_table/blocks/*.json` (13), `data/toms_storage/advancement/*.json` (5) | pending S1 part 1 (block/item sheets) |
| `03-terminal` | 22 | `textures/block/{terminal_front,terminal_side,crafting_terminal_front}.png` (3), `textures/item/{item_filter,tag_item_filter,poly_item_filter}.png` (3), `items/*.json` for storage/crafting terminal + the 3 filter items (5), `models/item/*.json` (3), `models/block/{storage_terminal,crafting_terminal}.json` (2), `blockstates/{storage_terminal,crafting_terminal}.json` (2), `data/toms_storage/recipe/{storage_terminal,crafting_terminal}.json` (2), `data/toms_storage/loot_table/blocks/{storage_terminal,crafting_terminal}.json` (2) | pending S1 part 2 (terminal + filter sheets) |
| `04-wireless` | 14 | `NeoForge/libs/curios_dummy.jar`, `Fabric/libs/trinkets_dummy.jar` (2), curios/trinkets belt data (NF `data/curios/tags/item/belt.json`, `data/toms_storage/curios/entities/player_belt.json`; FB `data/trinkets/entities/toms_storage.json`, `data/trinkets/tags/item/legs/belt.json` = 4), `items/{wireless_terminal,adv_wireless_terminal}.json` (2), `models/item/` same 2 (2), `textures/item/` same 2 (2), `data/toms_storage/recipe/` same 2 (2) | pending S1 part 2 (wireless terminal sheet) |
| `01-core` | 41 | build & repo metadata: `NeoForge/` + `Fabric/` `{build.gradle, settings.gradle, gradle.properties, gradlew, gradlew.bat}` (10) + `gradle/wrapper/*` (4) + `.gitignore`/`project.json` per loader (4) + root `LICENSE`, `README.md`, `banner.png`, `icon.png` (`RES`), `version-check.json`, `version-check-nf.json`, `.github/FUNDING.yml` (8); mod metadata: `META-INF/{neoforge.mods.toml, accesstransformer.cfg}` (2), `tomsstorage.accesswidener` (NF main + FB = 2), `fabric.mod.json` (1), `pack.mcmeta` (1); data tags: `data/toms_storage/tags/block/*.json` (4: `inventory_configurator_skip`, `inventory_connector_skip`, `remote_activate`, `trims` — the `StorageTags` registry), `data/toms_storage/tags/item/trims.json` (1), `data/minecraft/tags/block/mineable/*.json` (NF 2 + FB 2 = 4), FB `data/c/tags/item/{ender_pearls.json, glass_blocks/colorless.json}` (2) | pending S1 part 1 (`StorageTags`/registration sheet; `remote_activate` consumed by `04`) |
| `05-integration` | 3 | compat compile stubs: `NeoForge/libs/{iris.jar, polymorph_dummy.jar, vc_dummy.jar}` (Iris is consumed by the `06` client pipeline; ViveCraft by `06` VR helper; Polymorph by `05`) | [other-compat.md](05-integration/other-compat.md), [client-package.md](06-client-ui/client-package.md) |

Resource total check: 83 + 108 + 22 + 14 + 41 + 3 = 271.
