# core · key facts

Part of [core](index.md).

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

