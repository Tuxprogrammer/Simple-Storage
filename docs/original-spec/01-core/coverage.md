# core · coverage and limits

Part of [core](index.md).

## Coverage

- Source files read: 57 of 70 in scope
  - NeoForge (`NeoForge/src/main/java/com/tom/storagemod/`): 22 of 22 (root 5, network 1, platform 4, inventory 9 + filter 1, util 2)
  - Platform-shared (`NeoForge/src/platform-shared/java/com/tom/storagemod/`): 30 of 30 (Content, network 2, api 2, polymorph/PolymorphHelper, util 24)
  - Fabric (root 5 + inventory 9 + platform 4 = 18, read for loader-difference notes only): 5 of 18 (`platform/*` 4, `Config`)
  - Additionally read, outside the in-scope list: `Fabric/.../network/NetworkHandler.java`, `Fabric/.../util/TickerUtil.java`, `Fabric/.../util/WorldStates.java`, `NeoForge/.../polymorph/PolymorphTerminalWidget.java` (adjacent; documented in util-api.md)
- Textures / sounds / lang keys covered: 0 / 0 / 24 lang keys (all `config.toms_storage.*` keys incl. the missing `importantInfo`, `key.toms_storage.open_terminal`, `itemGroup.toms_storage.tab`, `tooltip.toms_storage.nbt_overflow`, `tooltip.toms_storage.item_sync_error`, the missing `tooltip.toms_trading_network.textfield`, `toms_storage.configuration.importantInfo.tooltip`, and the Fabric `text.autoconfig.toms_storage.option.*` labels)
- Corrections made to earlier statements: none (first writing)

## Known limits

- No MC instance was run: all behavior is read from source at `95bb61d`; screen/widget interactions (popup menu pixel math, keybind flow) are *inferred* from code paths.
- Fabric inventory adapters (9 files) were not read in full; the Fabric notes in inventory-access.md are *inferred* from the NF counterparts plus the Fabric `Platform`/`Config`/`NetworkHandler`/`TickerUtil`/`WorldStates` that were read.
- The `VanillaMultiblockInventories` and `SophisticatedDoubleBlocks` implementations (multiblock detectors) were not read — they are 02-storage territory; only their registration points are documented here.
- `BlockFilter`, `NetworkInventory`, `InventoryCableNetwork`, `StoredItemStack`, `TerminalItemStack`, `ItemPredicate` and the rest of the SH inventory package are 03-terminal territory; only the NF adapters over them are covered here.
- `ClientUtil` (SH client package, 06-client-ui) was read only for the tooltip/outline behavior relevant to wireless (04) and the `tooltip.toms_storage.*` key convention.

