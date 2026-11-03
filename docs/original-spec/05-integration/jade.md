# System: Jade (Waila) integration

| # | Field | Value |
|---|---|---|
| 1 | **Identity** | `com.tom.storagemod.jade.JadePlugin` (`SH/jade/JadePlugin.java`) — `IWailaPlugin` with `@WailaPlugin`; component UIDs: `toms_storage:inventory_connector`, `toms_storage:inventory_config`, `toms_storage:inventory_hopper`. Providers: `InventoryConnectorDataProvider`/`InventoryConnectorComponentProvider`, `InventoryConfigDataProvider`/`InventoryConfigComponentProvider`, `InventoryHopperDataProvider`/`InventoryHopperComponentProvider` (all `SH/jade/`), shared helper `JadeUtil.addFilterInfo` (`SH/jade/JadeUtil.java`) |
| 2 | **Inputs** | Jade API: `IWailaCommonRegistration`/`IWailaClientRegistration`, `IServerDataProvider<BlockAccessor>` (server), `IBlockComponentProvider` (client), `ITooltip`/`JadeUI`/`ProgressView` (client rendering), `IPluginConfig` |
| 3 | **Numbers** | Jade versions: NF `curse.maven:jade-324717:8326243` (**bundled in the NeoForge jar**, `implementation`); FB `maven.modrinth:jade:26.2.0+fabric` (bundled, `implementation`). Filter icon in detail mode: 18×18 px, offset (0, −1), inside a transparent `BoxStyle` with border width 1 (border color line commented out: `0xFFFF0000`×4). Connector progress: fraction `(all − free) / max(all, 1f)` (float division; 0 when `all == 0`) |
| 4 | **Behavior** | See Behavior detail |
| 5 | **Determinism** | Server data providers run on the server when Jade requests block data (hovered block, throttled by Jade). Client component providers run on the client per tooltip render. The config data provider runs for **every** block entity (registered for `BlockEntity.class`) — see `INT-06` |
| 6 | **Data formats** | NBT payload per component (written by the data provider, read by the component provider):<br>`inventory_connector`: `free` (int, free slots), `all` (int, total slots), `blocks` (int, number of inventories in the network).<br>`inventory_config`: `bf` (bool, set only when a `BlockFilter` exists), `skip` (bool), `pr` (byte = `Priority` ordinal: 0=`lowest`, 1=`low`, 2=`normal`, 3=`high`, 4=`highest`), `filter` (ItemStack via `ItemStack.CODEC`, only when the filter slot is non-empty).<br>`inventory_hopper`: `filter` (ItemStack via `ItemStack.CODEC`, only when non-empty), `enabled` (bool).<br>Client read-back uses `TagValueInput` with `ProblemReporter.DISCARDING` (malformed NBT is ignored) and `Minecraft.getInstance().level.registryAccess()` as the registry source |
| 7 | **Output** | (client) Jade tooltips: inventory connector → free/total slot count + fill progress bar + inventory count; any block with a block filter → "Inventory Configuration" line + skip/priority line + (when a filter item is set) the filter item (detail mode: icon + full item tooltip in a bordered box; otherwise a single "Filter: <item name>" line); inventory hopper → filter info + "disabled by redstone" when redstone-disabled |
| 8 | **Relationships** | `InventoryConnectorBlockEntity.getUsage()` → `UsageInfo` (`free/all/blocks`) (area `02-storage`), `BlockFilter.getFilterAt(level, pos)` (area `03-terminal`/`02-storage`), `BasicInventoryHopperBlockEntity.getFilter()/isEnabled()` (area `02-storage`), `Priority` enum (`SH/util/Priority.java`, area `02-storage`), the block/item classes for registration (areas `02`/`03`), the `config.jade.plugin_toms_storage.*` lang keys (area `06-client-ui`) |
| 9 | **NeoForge dependencies** | Jade API (`snownee.jade.api.*`, `IWailaPlugin` discovery) — the 1.7.10 counterpart is WAILA (or the GTNH-pack info mod; pending S2). `ItemStack.CODEC` + `TagValueInput` for the `filter` field (data components → NBT in 1.7.10). `ProgressView`/`JadeUI` (no 1.7.10 counterpart, pending S2). Uses the mod's own block-entity getters |
| 10 | **Bugs** | `INT-06` (config providers registered for every block/entity) |

## Behavior detail

### Registration

1. **(server)** `register(IWailaCommonRegistration)`:
   - `InventoryConnectorDataProvider` for `InventoryConnectorBlockEntity`.
   - `InventoryConfigDataProvider` for `BlockEntity.class` (every block entity).
   - `InventoryHopperDataProvider` for `BasicInventoryHopperBlockEntity`.
2. **(client)** `registerClient(IWailaClientRegistration)`:
   - `InventoryConnectorComponentProvider` for `InventoryConnectorBlock`.
   - `InventoryConfigComponentProvider` for `Block.class` (every block).
   - `InventoryHopperComponentProvider` for `BasicInventoryHopperBlock`.

### Server data providers (run on the server)

1. **Connector** (`InventoryConnectorDataProvider.appendServerData`): reads `te.getUsage()` and writes `free`, `all`, `blocks` (ints) — always, for every hovered connector.
2. **Config** (`InventoryConfigDataProvider.appendServerData`): `BlockFilter bf = BlockFilter.getFilterAt(level, pos)`; only when `bf != null` writes `bf=true`, `skip=bf.skip()`, `pr=(byte) bf.getPriority().ordinal()`, and `filter` (ItemStack) when the filter slot is non-empty. When there is no block filter, nothing is written (the component renders nothing).
3. **Hopper** (`InventoryHopperDataProvider.appendServerData`): casts the block entity to `BasicInventoryHopperBlockEntity`; writes `filter` when the hopper's filter item is non-empty, and `enabled` (bool) always.

### Client component providers (run on the client, per tooltip)

1. **Connector** (`InventoryConnectorComponentProvider.appendTooltip`): only when server data contains `free`. Adds (in order):
   1. `Component.translatable("tooltip.toms_storage.connector_info.size", free, all)` — "free/total" line.
   2. A `JadeUI.progress` bar with fraction `(all − free) / max(all, 1f)` (i.e. 0.0 when `all == 0`, no division by zero) and `BoxStyle.nestedBox()`.
   3. `Component.translatable("tooltip.toms_storage.connector_info.inv", blocks)` — inventory count line.
2. **Config** (`InventoryConfigComponentProvider.appendTooltip`): only when server data has `bf == true`. Adds:
   1. `Component.translatable("tooltip.toms_storage.block_filter")` — the "Inventory Configuration" header.
   2. Priority from `pr`: `Priority.VALUES[abs(pr) % 5]` (defensive against stale/out-of-range bytes), then:
      - if `skip`: `Component.translatable("tooltip.toms_storage.block_filter.skip")`.
      - else: `Component.translatable("tooltip.toms_storage.priority_" + pr.name().toLowerCase())` (e.g. `tooltip.toms_storage.priority_normal`) and, when a filter item was stored, `JadeUtil.addFilterInfo(...)`.
3. **Hopper** (`InventoryHopperComponentProvider.appendTooltip`): reads `filter` (default empty) and `enabled` (default `true`). If a filter item exists → `JadeUtil.addFilterInfo(...)`; if `enabled == false` → `Component.translatable("tooltip.toms_storage.disabled_by_redstone")`.
4. **Shared** (`JadeUtil.addFilterInfo`): when the accessor is in "show details" mode (Jade's detail toggle): builds a nested tooltip with an 18×18 px item icon (offset 0,−1) followed by the item's full tooltip lines (`getTooltipLines` with `TooltipContext.of(level)`, `TooltipFlag.Default.NORMAL`), wrapped in a `JadeUI.box` with a transparent `BoxStyle` and border width 1 (the red border-color line is commented out). Without details: a single line `tooltip.toms_storage.block_filter.item` with the item's hover name as the argument.

## Deviations in the new mod

None (original spec only; the 1.7.10 info-mod counterpart is a pending S2 decision).
