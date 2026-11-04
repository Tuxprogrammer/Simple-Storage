# 02 · Storage

## Overview

The storage side of Tom's Simple Storage: the blocks that hold or expose inventories (filing cabinet, open crate, inventory proxy, trims), the network wiring (inventory cables, cable connectors, inventory connector, inventory interface), the redstone/hopper interfaces (level emitter, basic inventory hopper), the item/component layer (paint kit, painted items, filter/configurator data components), and the network/registry machinery those blocks form (cable network, remote connections, proxy/link interfaces). Documents upstream Tom's Simple Storage 2.11.3, branch `26.2` at commit `95bb61d` (Minecraft 26.2, NeoForge 26.2.0.90). The NeoForge side is the port baseline; `OpenCrateBlockEntity` and `PaintedBlockEntity` live in `NeoForge/src/main` (NF) — noted per sheet.

## Contents

- [filing-cabinet.md](filing-cabinet.md): `FilingCabinetBlock` + `FilingCabinetBlockEntity` + `FilingCabinetContainer` — the 512-slot single-item-type cabinet.
- [open-crate.md](open-crate.md): `OpenCrateBlock` + `OpenCrateBlockEntity` (NF) — virtual inventory of the front block's item entities.
- [inventory-proxy.md](inventory-proxy.md): `InventoryProxyBlock` + `InventoryProxyBlockEntity` — extends an inventory face for the network.
- [trims-paint.md](trims-paint.md): `TrimBlock`, `PaintedTrimBlock`, `IPaintable`, `PaintedBlockEntity` (NF), `IConfiguratorHighlight`, `BlockWithTooltip`.
- [cables.md](cables.md): `InventoryCableBlock`, `FramedInventoryCableBlock`, `IInventoryCable`, `IInventoryNode`.
- [cable-connector.md](cable-connector.md): `InventoryCableConnectorBlock` + `FramedInventoryCableConnectorBlock` — attaching a face to the network, beacon link.
- [inventory-connector.md](inventory-connector.md): `InventoryConnectorBlock` + `InventoryConnectorBlockEntity` — touching-inventory scan, cable linking, merged handler.
- [inventory-interface.md](inventory-interface.md): `InventoryInterfaceBlock` + `InventoryInterfaceBlockEntity` — pass-through view of a connector's network.
- [level-emitter.md](level-emitter.md): `LevelEmitterBlock` + `LevelEmitterBlockEntity` — threshold redstone signal from network contents.
- [hopper.md](hopper.md): `AbstractInventoryHopperBlock`, `BasicInventoryHopperBlock`, `AbstractInventoryHopperBlockEntity`, `BasicInventoryHopperBlockEntity` — filtered one-item-per-cycle transfer.
- [items.md](items.md): `PaintKitItem`, `PaintedBlockItem`, `BlockItemTSS`, `ILeftClickListener`.
- [components.md](components.md): `ConfiguratorComponent`, `SimpleItemFilterComponent`, `TagFilterComponent`, `WorldPos`.
- [containers.md](containers.md): `BasicContainer`, `LimitedContainer` (utility containers).
- [cable-network.md](cable-network.md): `InventoryCableNetwork`, `RemoteConnections`, `IInventoryLink`, `IInventoryConnectorReference`, `IProxy`, `IChangeTrackerAccess`, `BlockFilterAttachment`.

## Key facts

- The area table's "storage item with internal inventory" is, upstream, a **block**: the Filing Cabinet (512 slots, one item type per cabinet). There is no standalone storage item in 26.2 (deviation noted below).
- Network topology: cables (`isFunctionalNode() == false`) connect functional nodes — cable connectors, inventory connectors, interfaces, emitters, hoppers — via `IInventoryCable.nextScan`/`canConnectFrom`; `InventoryCableNetwork` walks + caches the graph per level.
- An `InventoryConnectorBlockEntity` scans touching inventories (through `StorageTags.TRIMS` blocks, bounded by `invConnectorScanRange`²) every 20 ticks (staggered by position hash), merges them into a `PlatformMultiInventoryAccess`, and links to other connectors found in its cable network.
- All staggered-tick gates use the pattern `getGameTime() % N == Math.abs(worldPosition.hashCode()) % N` — see STOR-01/05/06.
- Config keys (01-core `config.md`): `invConnectorScanRange` (default 16), `onlyTrims` (false), `basicHopperCooldown` (10), `invLinkBeaconLvl`/`invLinkBeaconRange`/`invLinkBeaconLvlSameDim`/`invLinkBeaconLvlCrossDim`.
- Paint: `IPaintable` + Paint Kit data component; `PaintedBlockEntity` stores a facade `BlockState` (NF).

## Related areas

- `01-core`: `PlatformInventoryAccess` (`BlockInventoryAccess`, `EMPTY`), `PlatformMultiInventoryAccess`, `IInventoryAccess`, `BlockFilter` (+ attachment), `InventoryConnectorConfigUtil`, `MultiblockInventoryAPI`, `StorageTags`, `Config`, `WorldStates`, `TickerUtil`, `AbstractPainedBlockEntity`, `BeaconLevelCalc`/`BeaconLevel`, `ClientUtil`, `BlockFace`, `BlockFaceReference`, `IItemFilter`, `IInventoryConnector`.
- `03-terminal`: the GUIs (filing cabinet, inventory link, level emitter, configurator, filters) and the inventory logic layer (`NetworkInventory`, `StoredItemStack`, filters/sorting) that consumes these blocks.
- `04-wireless`: `IInventoryLink` wireless implementations (wireless terminal BEs).
- `05-integration`: `MultiblockInventoryAPI` multiblock detection hook.
- `06-client-ui`: configurator highlight rendering, scrape/advancement visuals.

## Coverage

- Source files read: 46 of 46 in scope (verified against the area file list — batches 02-b1…02-b7, re-read in full during the writing phase).
- Lang keys covered: every key used by in-scope classes is in the table below (exact text from `NeoForge/src/main/resources/lang/en_us.json`).
- Textures/models referenced but not inspected: trim/cable/connector/emitter/hopper/crate/cabinet models and their client renderers (06-client-ui).
- Corrections made to earlier statements: `FilingCabinetContainer` lock semantics corrected after re-reading (lock cleared only when the whole cabinet is empty; `setItem` re-locks unconditionally — see `filing-cabinet.md`).

## Lang keys (in scope, exact text from `NeoForge/src/main/resources/lang/en_us.json`)

| Key | Text |
|---|---|
| `block.toms_storage.filing_cabinet` | Filing Cabinet |
| `block.toms_storage.open_crate` | Open Crate |
| `block.toms_storage.inventory_proxy` | Inventory Proxy |
| `block.toms_storage.trim` | Inventory Trim |
| `block.toms_storage.painted_trim` | Inventory Trim |
| `block.toms_storage.inventory_cable` | Inventory Cable |
| `block.toms_storage.inventory_cable_framed` | Framed Inventory Cable |
| `block.toms_storage.inventory_cable_connector` | Inventory Cable Connector |
| `block.toms_storage.inventory_cable_connector_framed` | Inventory Cable Connector (Framed) |
| `block.toms_storage.inventory_connector` | Inventory Connector |
| `block.toms_storage.inventory_interface` | Inventory Interface |
| `block.toms_storage.level_emitter` | Level Emitter |
| `block.toms_storage.basic_inventory_hopper` | Basic Inventory Hopper |
| `item.toms_storage.paint_kit` | Paint Kit |
| `tooltip.toms_storage.filing_cabinet` | Store multiple variants of the same item |
| `tooltip.toms_storage.open_crate` | Converts the block in front of it\into an inventory.\Beware: Creates Item Entities |
| `tooltip.toms_storage.inventory_proxy` | Extends the face of the block its pointing at. |
| `tooltip.toms_storage.inventory_cable` | Connects inventories across\long distances. Inventory Cable Connectors\have to be used to attach inventories\One Inventory Connector must be\present in the network. |
| `tooltip.toms_storage.inventory_cable_connector` | Connects the inventory to the Inventory Network.\Use the Inventory Configurator to filter. |
| `tooltip.toms_storage.inventory_cable_connector_link` | The both sides' chunk has to be loaded |
| `tooltip.toms_storage.inventory_cable_connector_link.beacon` | The beacon's level determines the range: |
| `tooltip.toms_storage.inventory_cable_connector_link.beacon0` | %d: %d block range. |
| `tooltip.toms_storage.inventory_cable_connector_link.beacon1` | %d: anywhere in the same dimension. |
| `tooltip.toms_storage.inventory_cable_connector_link.beacon2` | %d: anywhere across dimensions. |
| `tooltip.toms_storage.inventory_connector` | Connects all of the touching inventories into one.\Use the Storage Terminal to access\all of your items. Trims can connect the gaps. |
| `tooltip.toms_storage.inventory_interface` | Access the connected Inventory Connector's inventory |
| `tooltip.toms_storage.level_emitter` | Emits a redstone signal based on Inventory System contents. |
| `tooltip.toms_storage.inventory_hopper` | Import or export from your Inventory Network\Right click an item to set the filter.\Sneak click with an empty hand to clear filter. |
| `tooltip.toms_storage.paintable` | Can be painted |
| `tooltip.toms_storage.paint_kit` | Paint blocks to match\their surroundings! |
| `tooltip.toms_storage.filter_item` | Filter: %s |
| `tooltip.toms_storage.empty` | Empty |
| `chat.toms_storage.inventory_connector.free_slots` | Inventory Connector: %d free/%d slots (in %d inventories). |
| `chat.toms_storage.inv_link_access_denied` | Access Denied |

Note: trim block items have no `item.*` keys — `BlockItem` falls back to the block name (both trims display as "Inventory Trim").

## Known limits

- `InventoryCableConnectorBlockEntity` (SH/block/entity/) is created by `InventoryCableConnectorBlock.newBlockEntity` but is **not** in the 46-file scope list; its beacon/link/NBT behavior is documented only indirectly (via `cable-connector.md`, 03-terminal `inventory-link.md`, 01-core `config.md`).
- `IInventoryConnector` (SH/block/entity/IInventoryConnector.java) — the interface implemented by connector/interface BEs — is out of scope; treated as a 01-core platform interface here.
- `BlockFilter`, `AbstractPainedBlockEntity`, `PlatformInventoryAccess`, `MultiblockInventoryAPI` are 01-core/05-integration — used, not documented here.
- Not run in a real pack: block shapes, model selection and render shapes read from code only.
- `MultiblockInventoryAPI` third-party implementations are unbounded (05-integration).

## Suspected bugs

| ID | Title | Location | What looks wrong | Severity |
|---|---|---|---|---|
| STOR-01 | Open crate rescan gate unsatisfiable for one position hash | `NeoForge/src/main/java/com/tom/storagemod/block/entity/OpenCrateBlockEntity.java:31` | `getGameTime() % 5 == Math.abs(worldPosition.hashCode()) % 5`: if the position hash is `Integer.MIN_VALUE`, `Math.abs` stays negative and the gate never holds — the crate's item list is never rebuilt (stale/empty virtual inventory forever). | gameplay |
| STOR-02 | Filing cabinet item-type lock derived from slot 0 only / re-locks unconditionally | `NeoForge/src/platform-shared/java/com/tom/storagemod/util/FilingCabinetContainer.java:94-99` | `fromTag` sets the lock from slot 0 only: a cabinet loaded with slot 0 empty is "unlocked" and accepts a second item type while other slots are filled; `setItem` (lines 61-66) also re-locks to any written type without checking the existing lock, so API writes can mix types. | gameplay |
| STOR-03 | `hasConnectedInventories()` ignores actual connections | `NeoForge/src/platform-shared/java/com/tom/storagemod/block/entity/InventoryConnectorBlockEntity.java:188` | Returns `!isRemoved()`; an `InventoryInterfaceBlockEntity` (its `updateServer` discovery, line 46) treats any living connector as a usable network even with zero attached inventories. | gameplay |
| STOR-04 | Open crate phantom append slot | `NeoForge/src/main/java/com/tom/storagemod/block/entity/OpenCrateBlockEntity.java:40-41` | `getContainerSize()` returns `items.size() + 1` and `canPlaceItem` (lines 108-109) accepts only `index == items.size()`, but `getItem` there returns EMPTY — the append slot is only meaningful through the platform adapter's push convention; a plain `Container` consumer sees a permanently empty last slot. | gameplay |
| STOR-05 | Connector/interface rescan gate unsatisfiable for one position hash | `NeoForge/src/platform-shared/java/com/tom/storagemod/block/entity/InventoryConnectorBlockEntity.java:50` | `time % 20 == Math.abs(worldPosition.hashCode()) % 20`: with a `MIN_VALUE` hash the connector never rebuilds its inventories (dead network); same pattern at `InventoryInterfaceBlockEntity.java:31` (interface never re-links). | gameplay |
| STOR-06 | Level emitter tick gate unsatisfiable for one position hash | `NeoForge/src/platform-shared/java/com/tom/storagemod/block/entity/LevelEmitterBlockEntity.java:41` | `getGameTime() % 10 != Math.abs(worldPosition.hashCode()) % 10 → return`: with a `MIN_VALUE` hash the emitter never re-evaluates, so `POWERED` is frozen. | gameplay |
| STOR-07 | Level emitter `Count` NBT load-default differs from runtime default | `NeoForge/src/platform-shared/java/com/tom/storagemod/block/entity/LevelEmitterBlockEntity.java:91` | Field default is `count = 1` (line 30) but `loadAdditional` uses `getIntOr("Count", 0)` — a missing/legacy tag loads threshold 0, so in greater-than mode any non-zero count powers the emitter. | gameplay |
| STOR-08 | Hopper `waiting == 2` branch is dead | `NeoForge/src/platform-shared/java/com/tom/storagemod/block/entity/BasicInventoryHopperBlockEntity.java:101` | `waiting` is assigned 0, 1 and 3 but never 2, so the `if (waiting == 2) return;` after the bottom-change check never fires — an incomplete state machine (the intended "bottom just changed" pause is missing). | cosmetic |
| STOR-09 | Unfiltered network-source hopper spins every tick | `NeoForge/src/platform-shared/java/com/tom/storagemod/block/entity/BasicInventoryHopperBlockEntity.java:83` | `if (topNet && !hasFilter) return;` fires after the cooldown is already consumed but sets no cooldown — a network-source hopper without a filter re-runs the full check every tick. | performance |
| STOR-10 | Channel name fallback is time-based | `NeoForge/src/platform-shared/java/com/tom/storagemod/inventory/RemoteConnections.java:131` | `makeChannel` falls back to `"Channel " + System.currentTimeMillis()` for empty/overlong names — nondeterministic display name, and two channels created in the same millisecond collide (feeds the 03-terminal TERM-08 ambiguous-name lookup). | cosmetic |

## Deviations in the new mod

- The area table calls this "the storage item with internal inventory": upstream 26.2 has **no standalone storage item** — the filing cabinet is a block. The new spec should say so and decide whether the 1.7.10 port keeps it a block (recommended for GTNH crate parity).

See also the row in [new-spec 02-storage](../../new-spec/02-storage/index.md) (row: index).
