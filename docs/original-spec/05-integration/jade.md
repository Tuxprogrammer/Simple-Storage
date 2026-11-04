# System: Jade (Waila) integration

| # | Field | Value |
|---|---|---|
| 1 | **Identity** | `com.tom.storagemod.jade.JadePlugin` (`SH/jade/JadePlugin.java`) — `IWailaPlugin` with `@WailaPlugin`; component UIDs: `toms_storage:inventory_connector`, `toms_storage:inventory_config`, `toms_storage:inventory_hopper`. Providers: `InventoryConnectorDataProvider`/`InventoryConnectorComponentProvider`, `InventoryConfigDataProvider`/`InventoryConfigComponentProvider`, `InventoryHopperDataProvider`/`InventoryHopperComponentProvider` (all `SH/jade/`), shared helper `JadeUtil.addFilterInfo` (`SH/jade/JadeUtil.java`). Sources (upstream `26.2`, `SH` = `NeoForge/src/platform-shared/java/com/tom/storagemod/`): `SH/jade/JadePlugin.java:18-22` (UIDs), `SH/jade/*Provider.java`, `SH/jade/JadeUtil.java:15`; `Fabric/src/main/resources/fabric.mod.json:40-42` (`jade` entrypoint) |
| 2 | **Inputs** | Jade API: `IWailaCommonRegistration`/`IWailaClientRegistration`, `IServerDataProvider<BlockAccessor>` (server), `IBlockComponentProvider` (client), `ITooltip`/`JadeUI`/`ProgressView` (client rendering), `IPluginConfig` |
| 3 | **Numbers** | Jade versions: NF `curse.maven:jade-324717:8326243` (**bundled in the NeoForge jar**, `implementation`) (NeoForge/build.gradle:175); FB `maven.modrinth:jade:26.2.0+fabric` (bundled, `implementation`) (Fabric/build.gradle:66). Filter icon in detail mode: 18×18 px, offset (0, −1) (JadeUtil.java:19), inside a transparent `BoxStyle` with border width 1 (border color line commented out: `0xFFFF0000`×4) (JadeUtil.java:23-25). Connector progress: fraction `(all − free) / max(all, 1f)` (float division; 0 when `all == 0`) (InventoryConnectorComponentProvider.java:30) |
| 4 | **Behavior** | See Behavior detail |
| 5 | **Determinism** | Server data providers run on the server when Jade requests block data (hovered block, throttled by Jade). Client component providers run on the client per tooltip render. The config data provider runs for **every** block entity (registered for `BlockEntity.class`) — see `INT-06` (JadePlugin.java:27) |
| 6 | **Data formats** | NBT payload per component (written by the data provider, read by the component provider):<br>`inventory_connector`: `free` (int, free slots), `all` (int, total slots), `blocks` (int, number of inventories in the network) (InventoryConnectorDataProvider.java:24-26).<br>`inventory_config`: `bf` (bool, set only when a `BlockFilter` exists), `skip` (bool), `pr` (byte = `Priority` ordinal: 0=`lowest`, 1=`low`, 2=`normal`, 3=`high`, 4=`highest`), `filter` (ItemStack via `ItemStack.CODEC`, only when the filter slot is non-empty) (InventoryConfigDataProvider.java:24-30).<br>`inventory_hopper`: `filter` (ItemStack via `ItemStack.CODEC`, only when non-empty), `enabled` (bool) (InventoryHopperDataProvider.java:24-27).<br>Client read-back uses `TagValueInput` with `ProblemReporter.DISCARDING` (malformed NBT is ignored) and `Minecraft.getInstance().level.registryAccess()` as the registry source (InventoryConfigComponentProvider.java:32; InventoryHopperComponentProvider.java:26) |
| 7 | **Output** | (client) Jade tooltips: inventory connector → free/total slot count + fill progress bar + inventory count; any block with a block filter → "Inventory Configuration" line + skip/priority line + (when a filter item is set) the filter item (detail mode: icon + full item tooltip in a bordered box; otherwise a single "Filter: <item name>" line); inventory hopper → filter info + "disabled by redstone" when redstone-disabled |
| 8 | **Relationships** | `InventoryConnectorBlockEntity.getUsage()` → `UsageInfo` (`free/all/blocks`) (area `02-storage`), `BlockFilter.getFilterAt(level, pos)` (area `03-terminal`/`02-storage`), `BasicInventoryHopperBlockEntity.getFilter()/isEnabled()` (area `02-storage`), `Priority` enum (`SH/util/Priority.java`, area `02-storage`), the block/item classes for registration (areas `02`/`03`), the `config.jade.plugin_toms_storage.*` lang keys (area `06-client-ui`) |
| 9 | **NeoForge dependencies** | Jade API (`snownee.jade.api.*`, `IWailaPlugin` discovery) — the 1.7.10 counterpart is WAILA (S2: Waila 1.19.34, see API-MAP). `ItemStack.CODEC` + `TagValueInput` for the `filter` field (data components → NBT in 1.7.10). `ProgressView`/`JadeUI` (no 1.7.10 counterpart, S2: none — see API-MAP). Uses the mod's own block-entity getters |
| 10 | **Bugs** | `INT-06` (config providers registered for every block/entity) |

## Behavior detail

### Registration

1. **(server)** `register(IWailaCommonRegistration)`:
   - `InventoryConnectorDataProvider` for `InventoryConnectorBlockEntity` (JadePlugin.java:26).
   - `InventoryConfigDataProvider` for `BlockEntity.class` (every block entity) (JadePlugin.java:27).
   - `InventoryHopperDataProvider` for `BasicInventoryHopperBlockEntity` (JadePlugin.java:28).
2. **(client)** `registerClient(IWailaClientRegistration)`:
   - `InventoryConnectorComponentProvider` for `InventoryConnectorBlock` (JadePlugin.java:33).
   - `InventoryConfigComponentProvider` for `Block.class` (every block) (JadePlugin.java:34).
   - `InventoryHopperComponentProvider` for `BasicInventoryHopperBlock` (JadePlugin.java:35).

### Server data providers (run on the server)

1. **Connector** (`InventoryConnectorDataProvider.appendServerData`): reads `te.getUsage()` and writes `free`, `all`, `blocks` (ints) — always, for every hovered connector (InventoryConnectorDataProvider.java:22-26).
2. **Config** (`InventoryConfigDataProvider.appendServerData`): `BlockFilter bf = BlockFilter.getFilterAt(level, pos)`; only when `bf != null` writes `bf=true`, `skip=bf.skip()`, `pr=(byte) bf.getPriority().ordinal()`, and `filter` (ItemStack) when the filter slot is non-empty (InventoryConfigDataProvider.java:22-30). When there is no block filter, nothing is written (the component renders nothing).
3. **Hopper** (`InventoryHopperDataProvider.appendServerData`): casts the block entity to `BasicInventoryHopperBlockEntity`; writes `filter` when the hopper's filter item is non-empty, and `enabled` (bool) always (InventoryHopperDataProvider.java:22-27).

### Client component providers (run on the client, per tooltip)

1. **Connector** (`InventoryConnectorComponentProvider.appendTooltip`): only when server data contains `free` (InventoryConnectorComponentProvider.java:25). Adds (in order):
   1. `Component.translatable("tooltip.toms_storage.connector_info.size", free, all)` — "free/total" line (InventoryConnectorComponentProvider.java:29).
   2. A `JadeUI.progress` bar with fraction `(all − free) / max(all, 1f)` (i.e. 0.0 when `all == 0`, no division by zero) and `BoxStyle.nestedBox()` (InventoryConnectorComponentProvider.java:30).
   3. `Component.translatable("tooltip.toms_storage.connector_info.inv", blocks)` — inventory count line (InventoryConnectorComponentProvider.java:31).
2. **Config** (`InventoryConfigComponentProvider.appendTooltip`): only when server data has `bf == true` (InventoryConfigComponentProvider.java:30). Adds:
   1. `Component.translatable("tooltip.toms_storage.block_filter")` — the "Inventory Configuration" header (InventoryConfigComponentProvider.java:34).
   2. Priority from `pr`: `Priority.VALUES[abs(pr) % 5]` (defensive against stale/out-of-range bytes) (InventoryConfigComponentProvider.java:35), then:
      - if `skip`: `Component.translatable("tooltip.toms_storage.block_filter.skip")` (InventoryConfigComponentProvider.java:36-37).
      - else: `Component.translatable("tooltip.toms_storage.priority_" + pr.name().toLowerCase())` (e.g. `tooltip.toms_storage.priority_normal`) and, when a filter item was stored, `JadeUtil.addFilterInfo(...)` (InventoryConfigComponentProvider.java:39-42).
3. **Hopper** (`InventoryHopperComponentProvider.appendTooltip`): reads `filter` (default empty) and `enabled` (default `true`) (InventoryHopperComponentProvider.java:27, :31). If a filter item exists → `JadeUtil.addFilterInfo(...)`; if `enabled == false` → `Component.translatable("tooltip.toms_storage.disabled_by_redstone")` (InventoryHopperComponentProvider.java:28-33).
4. **Shared** (`JadeUtil.addFilterInfo`): when the accessor is in "show details" mode (Jade's detail toggle) (JadeUtil.java:16): builds a nested tooltip with an 18×18 px item icon (offset 0,−1) followed by the item's full tooltip lines (`getTooltipLines` with `TooltipContext.of(level)`, `TooltipFlag.Default.NORMAL`), wrapped in a `JadeUI.box` with a transparent `BoxStyle` and border width 1 (the red border-color line is commented out) (JadeUtil.java:17-26). Without details: a single line `tooltip.toms_storage.block_filter.item` with the item's hover name as the argument (JadeUtil.java:27-28).

## Deviations in the new mod

None (original spec only; the 1.7.10 info-mod counterpart is settled by S2 (Waila — see API-MAP)).
