# Inventory Interface

## Identity
`com.tom.storagemod.block.InventoryInterfaceBlock` (SH) + `InventoryInterfaceBlockEntity` (SH). A cable node that re-exposes a connected Inventory Connector's merged inventory (used to reach a network across a gap or from the configurator).

Sources (upstream 26.2@95bb61d): `NeoForge/src/platform-shared/java/com/tom/storagemod/block/InventoryInterfaceBlock.java` and `.../block/entity/InventoryInterfaceBlockEntity.java`; short names below are `Class.java:line` in those files (class declarations: InventoryInterfaceBlock.java:33, InventoryInterfaceBlockEntity.java:19).

## Inputs
- Server tick: staggered; work when `getGameTime() % 20 == Math.abs(worldPosition.hashCode()) % 20` (1 in 20 ticks) (InventoryInterfaceBlockEntity.java:29-31; ticker InventoryInterfaceBlock.java:48).
- `setConnectorAccess(connector)`: called by the owning `InventoryConnectorBlockEntity` during its scan when the interface is in its `interfaces` set (InventoryInterfaceBlockEntity.java:73-75).

## Numbers
- Rescan period: 20 ticks, staggered by position hash (InventoryInterfaceBlockEntity.java:31).

## Behavior
1. (server, staggered tick) `updateServer`:
   - If `hasConnector` (set by a fresh `setConnectorAccess` this tick): clears the flag and keeps `connectorAccess` (InventoryInterfaceBlockEntity.java:32-33).
   - Else: nulls `connectorAccess` (InventoryInterfaceBlockEntity.java:34-35); if `connectorAccess` is still non-null (a fresh reference was just set) it becomes `networkAccess`; otherwise it re-discovers: first `IInventoryConnector` in the cable network at this position (loaded chunks) with `hasConnectedInventories()` becomes `networkAccess` (WeakReference) (InventoryInterfaceBlockEntity.java:37-48).
2. `getInventoryAccess()`: if `networkAccess` is alive and `hasConnectedInventories()` → its `getMergedHandler()`; else `PlatformInventoryAccess.EMPTY` (InventoryInterfaceBlockEntity.java:56-61).
3. `getConnectorRef()`: the live connector, or null (InventoryInterfaceBlockEntity.java:65-68).
4. The block is both `IInventoryCable` and `IInventoryNode` (a functional node that also passes the network through — default `nextScan`, default `canConnectFrom`) (InventoryInterfaceBlock.java:33).
5. Tooltip: `tooltip.toms_storage.inventory_interface` = "Access the connected Inventory Connector's inventory" (InventoryInterfaceBlock.java:52-54).

## Determinism
Connector discovery order follows the cable-network node set order (HashSet) — first match wins (see STOR-03 for why "first match" can be a connector with no inventories).

## Data formats
No NBT (references are re-derived each tick).

## Output
A pass-through `IInventoryAccess` view of the chosen connector's merged inventory, and an `IInventoryConnectorReference`.

## Relationships
- `inventory-connector.md` (owner), `cables.md`, `cable-network.md`.
- 01-core: `PlatformInventoryAccess.EMPTY`, `IInventoryAccess`, `IInventoryConnector`, `TickerUtil`.

## NeoForge dependencies
Codec, `BlockStateProperties`, `TickerUtil` (platform ticker), `NeoForgeBlock` marker.

## Bugs
STOR-05 (Math.abs rescan gate, `InventoryInterfaceBlockEntity.java:31`); affected by STOR-03.

## Deviations in the new mod

See [new-spec 02-storage](../../new-spec/02-storage/index.md) (row: inventory-interface).
