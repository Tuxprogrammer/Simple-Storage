# Mod config

| # | Field | Contents |
|---|---|---|
| 1 | **Identity** | `Config` (NF: `NeoForge/src/main/java/com/tom/storagemod/Config.java`, ModConfigSpec-based; Fabric: `Fabric/src/main/java/com/tom/storagemod/Config.java`, autoconfig-based). Singleton `Config.get()`. Two spec files: `toms_storage-common.toml` (COMMON) and `toms_storage-server.toml` (SERVER, per-world). |
| 2 | **Inputs** | ModConfigEvent.Loading / ModConfigEvent.Reloading (NF, mod event bus); the toml files themselves. |
| 3 | **Numbers** | Every key with default and allowed range, in [Behavior detail](#behavior-detail) table. |
| 4 | **Behavior** | (both) On Loading and Reloading of either spec, `load(config)` copies the spec values into plain fields: SERVER type fills `onlyTrims`, `invConnectorScanRange`, `invConnectorCableRange`, `wirelessRange`, `advWirelessRange`, `wirelessTermBeaconLvl`, `wirelessTermBeaconLvlCrossDim`, `invLinkBeaconLvl`, `invLinkBeaconRange`, `invLinkBeaconLvlSameDim`, `invLinkBeaconLvlCrossDim`, `runMultithreaded`, `basicHopperCooldown`; COMMON type fills `blockedMods` (as a `Set<String>`) and `blockedBlocks` (parsed via `Identifier.tryParse` → block registry lookup, entries that fail to parse or are air are dropped, kept as `Set<Block>`). Logs the config file name on load and a "changed on the file system!" message on reload. |
| 5 | **Determinism** | None. |
| 6 | **Data formats** | Config keys (toml), in [Behavior detail](#behavior-detail). No NBT. |
| 7 | **Output** | Plain fields read throughout the mod: `onlyTrims`, `invConnectorScanRange`, `invConnectorCableRange`, `wirelessRange`, `advWirelessRange`, `wirelessTermBeaconLvl`, `wirelessTermBeaconLvlCrossDim`, `invLinkBeaconLvl`, `invLinkBeaconRange`, `invLinkBeaconLvlSameDim`, `invLinkBeaconLvlCrossDim`, `runMultithreaded`, `basicHopperCooldown`, `getBlockedBlocks()`, `getBlockedMods()`. |
| 8 | **Relationships** | Read by 02-storage (connector scan range, cable range, blocked mods/blocks, basic hopper cooldown, onlyTrims), 03-terminal (runMultithreaded), 04-wireless (wirelessRange, advWirelessRange, wirelessTermBeaconLvl, wirelessTermBeaconLvlCrossDim), inventory cable linking (invLinkBeacon* — 02-storage). `InventoryConnectorConfigUtil.canConnect` (this area, `util-misc.md`) reads `blockedMods`/`blockedBlocks` + the `inventory_connector_skip` tag. |
| 9 | **NeoForge dependencies** | `ModConfigSpec` (typed spec builder, `define`/`defineInRange`/`defineList`), `ModContainer.registerConfig(Type.COMMON/SERVER)`, `ModConfigEvent.Loading/Reloading` on the mod bus, per-world SERVER config synced to clients by the loader. |
| 10 | **Bugs** | `CORE-01` (unlabeled `importantInfo` entry), `CORE-09` (zero field defaults before server-config sync). |

## Behavior detail

### SERVER config (`toms_storage-server.toml`) — per world

| Key | Type | Default | Range | Translation key | Meaning |
|---|---|---|---|---|---|
| `inventoryConnectorRange` | int | 16 | 4–256 | `config.toms_storage.inventory_connector_range` | Inventory Connector scan range (blocks) |
| `onlyTrimsConnect` | bool | false | — | `config.toms_storage.only_trims_connect` | Only Allow Trims to Connect Inventories |
| `invCableConnectorMaxScanSize` | int | 2048 | 16–Integer.MAX_VALUE | `config.toms_storage.inv_cable_connector_max_scan` | Inventory Cable Connector maximum number of cables scanned |
| `wirelessReach` | int | 16 | 4–64 | `config.toms_storage.wireless_reach` | Wireless terminal reach (blocks) |
| `advWirelessRange` | int | 64 | 16–512 | `config.toms_storage.adv_wireless_range` | Advanced wireless terminal range (blocks) |
| `wirelessTermBeaconLvl` | int | 1 | -1–4 | `config.toms_storage.adv_wireless_beacon_lvl` | Adv wireless terminal required beacon level for infinite range; 0 = a single beacon block nearby suffices; -1 disables |
| `wirelessTermBeaconLvlDim` | int | 4 | -1–4 | `config.toms_storage.adv_wireless_beacon_lvl_dim` | Adv wireless terminal required beacon level for cross-dimension access; 0 = one beacon nearby; -1 disables |
| `invLinkBeaconLvl` | int | 0 | -1–4 | `config.toms_storage.inv_link_beacon_lvl` | Inventory Cable Connector required beacon level for inventory linking; 0 = one beacon block; -1 disables |
| `invLinkBeaconRange` | int | 4096 | 0–Integer.MAX_VALUE | `config.toms_storage.inv_link_beacon_range` | Inventory linking range (blocks) with beacons; 0 disables |
| `invLinkBeaconLvlSameDim` | int | 1 | -1–4 | `config.toms_storage.inv_link_beacon_lvl_same_dim` | Cable connector beacon level for unlimited-range same-dimension linking; 0 = one beacon; -1 disables |
| `invLinkBeaconLvlCrossDim` | int | 2 | -1–4 | `config.toms_storage.inv_link_beacon_lvl_dim` | Cable connector beacon level for cross-dimension linking; 0 = one beacon; -1 disables |
| `runMultithreaded` | bool | true | — | `config.toms_storage.run_multithreaded` | Use multithreading to improve performance |
| `basicHopperCooldown` | int | 10 | 1–200 | `config.toms_storage.basic_hopper_cooldown` | Base tick cooldown (ticks) for Basic Inventory Hopper transfer |

Commented-out (config-only, not active): `inventoryConnectorMaxSlots` (int, default Integer.MAX_VALUE, range 1–MAX, key `config.toms_storage.inv_connector_max_slots`).

All SERVER keys are loaded into `Config` fields of the same names (camelCased). Beacon-level keys use the sentinel -1 = "feature disabled", 0 = "a single beacon block in range suffices" (no pyramid required), 1–4 = required full pyramid level.

### COMMON config (`toms_storage-common.toml`)

| Key | Type | Default | Translation key | Meaning |
|---|---|---|---|---|
| `importantInfo` | bool | true | none (see `CORE-01`) | Cosmetic notice entry; the 6-line "IMPORTANT NOTICE" comment above it explains that most settings live in the per-world server config. The notice text is provided as the entry tooltip via lang key `toms_storage.configuration.importantInfo.tooltip` (exact text in the 06-client-ui lang listing) |
| `blockedMods` | list of strings | `[]` | `config.toms_storage.inv_blocked_mods` | List of mod ids whose blocks are ignored by the inventory connector |
| `blockedBlocks` | list of strings | `[]` (NF) / `["create:belt"]` (Fabric) | `config.toms_storage.inv_blocked_blocks` | List of block ids ignored by the inventory connector |

`blockedBlocks` entries are parsed as identifiers and resolved against the block registry at load time; unparseable entries and `minecraft:air` are silently dropped. `blockedMods` entries are kept verbatim (no validation) and compared against the block's registry namespace.

### Loader differences (Fabric)

Fabric's `Config` is an autoconfig `ConfigData` class in `config/toms_storage.json` with the same fields and defaults as the NF SERVER keys: `onlyTrims=false`, `runMultithreaded=true`, `invConnectorScanRange=16`, `wirelessRange=16`, `invConnectorMaxCables=2048` (named `invConnectorMaxCables` on Fabric vs `invCableConnectorMaxScanSize` on NF), `advWirelessRange=64`, `basicHopperCooldown=10`, `wirelessTermBeaconLvl=1`, `wirelessTermBeaconLvlCrossDim=4`, `invLinkBeaconLvl=0`, `invLinkBeaconRange=4096`, `invLinkBeaconLvlSameDim=1`, `invLinkBeaconLvlCrossDim=2`. Differences: no ranges enforced (plain fields); `blockedBlocks` defaults to `["create:belt"]` (NF: empty); the resolved-block set is cached in `StorageMod.blockedBlocks` (lazy) instead of being rebuilt on reload; no `importantInfo` entry; the `text.autoconfig.toms_storage.option.*` lang keys (present in en_us.json) are the autoconfig GUI labels.

## Deviations in the new mod

See the [`config` row of the new-spec 01-core deviation table](../../new-spec/01-core/index.md#deviation-table).
