# 01 · Core

## Overview

The mod's lifecycle and everything shared by all other areas of Tom's Simple Storage 2.11.3 (upstream branch `26.2`, commit `95bb61d`, NeoForge 2.26.2 baseline): mod entry and registration (`StorageMod`, `Content`), the two-file config (per-world server config + common config), the network channel (`DataPacket`/`OpenTerminalPacket`), the NeoForge/Fabric platform split (deferred registration, capabilities, attachments, the inventory-access adapter classes over the NeoForge transfer API), the terminal inventory-sync protocol (`TerminalSyncManager` — shared by wired and wireless terminals), and the shared utility classes (containers, filter bases, data slots, misc helpers, the multiblock API). Where a fact differs by loader, the sheet says which loader it came from; the Fabric side is read for loader-difference notes only.

## Contents

- [mod-lifecycle.md](mod-lifecycle.md): mod entry, client setup, `Content` registration table (all registry names), `ServerEvents`, `StorageTags`, creative tab, capabilities, IMC, keybind
- [config.md](config.md): every config key in both toml files with type, default, range, translation key and meaning; Fabric autoconfig differences
- [network.md](network.md): the `toms_storage:data` (bidirectional) and `toms_storage:open_terminal` (to server) payloads, handlers, send helpers
- [platform-layer.md](platform-layer.md): `Platform` (NF/Fabric), `GameObject`/registries, `PlatformItem`, `PlatformBlockEntity`, `PlatformItemHandler`
- [inventory-access.md](inventory-access.md): the NF inventory-access adapters (`BlockInventoryAccess`/capability cache, proxy, multi-inventory offsets, filtered access, `EmptyInventory`, `InventoryChangeTracker`, `InventorySlot`, `NeoStack`, `PolyFilter`)
- [terminal-sync.md](terminal-sync.md): `TerminalSyncManager` packet protocol (stack id table, 64000-byte split, mini-stack overflow, interaction format), `IAutoFillTerminal`, `IDataReceiver`
- [util-containers.md](util-containers.md): `BasicContainer`, `LimitedContainer`, `FilingCabinetContainer`, `CraftingMatrix`
- [util-filters.md](util-filters.md): `AbstractSimpleItemFilter` (9-slot working container, match-NBT/allow-list) and `AbstractTagFilter` bases over data components
- [util-data-slots.md](util-data-slots.md): `DataSlots` and `IntDataSlots` (two 16-bit halves) factories
- [util-misc.md](util-misc.md): `BlockFace(Reference)`, `ComponentJoiner`, `KeyUtil`, `NumberFormatUtil`, `PlayerInvUtil`, `PopupMenuManager`, `Priority`, `BeaconLevelCalc`, `TickerUtil`, `WorldStates`, `FloatSupplier`, `IValidInfo`, `InventoryConnectorConfigUtil`, `Util`
- [util-api.md](util-api.md): `EventHandler`, `MultiblockInventoryAPI`, `PolymorphHelper`, `PolymorphTerminalWidget` (dead on NF)

## Key facts

- Mod ID is `toms_storage`; all registry names are in that namespace (full table in mod-lifecycle.md).
- Registration is eager at class load: `Content.init()` is empty, everything registers in static initializers through `Platform`'s deferred registers (NF) or eager `Registry.register` (Fabric).
- Most gameplay tuning lives in the **per-world server config** (`toms_storage-server.toml`), not the common config — the common config only carries the connector block/mod blocklists (and a cosmetic notice). The server config is synced to clients by the loader.
- The single network payload that carries terminal data is `toms_storage:data`, a raw NBT compound (`TerminalSyncManager` binary blobs inside); there is no other protocol.
- The inventory-access layer is NeoForge-transfer-API shaped (`ResourceHandler<ItemResource>`); the SH abstractions (`IInventoryAccess` et al.) are 03-terminal territory, the NF adapters are documented in inventory-access.md.
- `TerminalSyncManager` is shared by the wired and wireless terminals alike (both open the same terminal menu) — it is documented here, and 04-wireless cross-links it rather than duplicating it.

## Related areas

- `02-storage`: the inventory connector/interface/proxy block entities expose `PlatformItemHandler` capabilities; `ServerEvents` drops block-filter attachments on break; the connector reads `blockedMods`/`blockedBlocks` + `inventory_connector_skip` via `InventoryConnectorConfigUtil`; `MultiblockInventoryAPI` discovers multiblock inventories during connector scans; filing cabinet BE uses `FilingCabinetContainer`.
- `03-terminal`: owns the SH inventory abstractions (`IInventoryAccess`, `BlockFilter`, `NetworkInventory`, `InventoryCableNetwork`) and the terminal BE/menus; the terminal BE's beacon scan and reach check consume `BeaconLevelCalc`, `PlayerInvUtil` and the wireless config; `StorageTerminalMenu` instantiates `TerminalSyncManager` and implements `IDataReceiver`/`IAutoFillTerminal`.
- `04-wireless`: the wireless items' reach feeds the terminal's `canInteractWith` (03-terminal); the beacon rules and keybind open flow are specified there; the terminal screen sync is this area's terminal-sync.md.
- `05-integration`: JEI/EMI/REI register `IAutoFillTerminal.ISearchHandler`s; the polymorph hooks (util-api.md) serve the crafting terminal.
- `06-client-ui`: screens bound in `StorageModClient.registerScreens`, `PopupMenuManager` usage, `ClientUtil` tooltip/outline code that consumes `KeyUtil` and the wireless config.

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

## Suspected bugs

| ID | Title | Location | What looks wrong | Severity |
|---|---|---|---|---|
| CORE-01 | `importantInfo` config entry has no label | `NeoForge/src/main/java/com/tom/storagemod/Config.java:135` | `define("importantInfo", true)` has no `.translation(...)`; no `config.toms_storage.importantInfo` lang key exists (only the tooltip key `toms_storage.configuration.importantInfo.tooltip`), so the config GUI shows the raw key name as the label | cosmetic |
| CORE-02 | `DataPacket` carries unbounded NBT | `NeoForge/src/platform-shared/java/com/tom/storagemod/network/DataPacket.java:17` | Client→server payload is read with `NbtAccounter.unlimitedHeap()` and passed unvalidated to the open menu's `receive()` — a malicious/buggy client can send an arbitrarily large compound to the server (memory); no size or field whitelist | performance |
| CORE-03 | Multi-inventory offsets compaction mis-indexes `connected` | `NeoForge/src/main/java/com/tom/storagemod/inventory/PlatformMultiInventoryAccess.java:68` | `refresh()` compacts the offsets table when a connected inventory reports size 0 (`hOff` skip), but `getHandler(i, ...)` indexes `connected.get(i)` with the *compacted* position returned by `findInventory` instead of the original inventory index — when an earlier inventory in the list is empty/missing, slot→inventory mapping is shifted for all later inventories | gameplay |
| CORE-04 | Filtered `extract` can pass a negative amount (keepLast) | `NeoForge/src/main/java/com/tom/storagemod/inventory/PlatformFilteredInventoryAccess.java:129` | With a keepLast filter, `amount = min(amount, count - 1)` on an empty slot (count 0) yields -1, which is forwarded to the underlying handler without the `checkNonEmptyNonNegative` precondition that `insert` paths check — edge-case crash/assertion on a keepLast filter over an emptied slot | gameplay |
| CORE-05 | Filing cabinet's first item must be single-stack | `NeoForge/src/platform-shared/java/com/tom/storagemod/util/FilingCabinetContainer.java:74` | `canPlaceItem` when no type is set yet accepts only stacks with `getMaxStackSize() == 1` — the first item placed (e.g. 64 ingots) is rejected; possibly intentional (unique items only) but undocumented in-world | gameplay |
| CORE-06 | Filing cabinet container max stack size is 1 | `NeoForge/src/platform-shared/java/com/tom/storagemod/util/FilingCabinetContainer.java:80` | `getMaxStackSize()` returns 1 at container level, and the BE exposes the container directly to hoppers (`VanillaContainerWrapper.of(be.getInv())`, mod-lifecycle.md) — hopper pushes are limited to 1 item per operation; possibly intentional but a performance trap | gameplay |
| CORE-07 | Missing lang key `tooltip.toms_trading_network.textfield` | `NeoForge/src/platform-shared/java/com/tom/storagemod/util/PopupMenuManager.java:163` | The search-mode popup's `TextFieldElement` renders through a Tom's Trading Network leftover key that is absent from every lang file — players see the raw key string in the terminal search popup | visual |
| CORE-08 | Polymorph integration is dead code on NeoForge | `NeoForge/src/main/java/com/tom/storagemod/StorageMod.java:69` | `StorageMod.polymorph` is set (line 56) but its only consumer — `PolymorphTerminalWidget.register()` — is commented out in `doClientStuff`; the crafting terminal never gets the polymorph recipe widget even with polymorph installed | cosmetic |
| CORE-09 | Server-config fields default to 0 before config sync | `NeoForge/src/main/java/com/tom/storagemod/Config.java:30` | `wirelessRange`, `advWirelessRange` etc. are plain fields with no initializers; on an MP client they stay 0 until the per-world server config is synced and fires a reload — e.g. `WirelessTerminalItem.use` would raytrace `pick(0)` in that window | gameplay |
| CORE-10 | `KeyUtil` only checks the left modifier keys | `NeoForge/src/platform-shared/java/com/tom/storagemod/util/KeyUtil.java:11` | `hasControlDown`/`hasShiftDown` query `GLFW_KEY_LEFT_CONTROL`/`GLFW_KEY_LEFT_SHIFT` only — right Ctrl/Shift produce no effect in shift/ctrl-gated tooltips and screens | cosmetic |
