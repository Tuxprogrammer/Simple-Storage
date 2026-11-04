# storage · key facts

Part of [storage](index.md).

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

## Lang keys (in scope, exact text from `NeoForge/src/platform-shared/resources/assets/toms_storage/lang/en_us.json`)

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

