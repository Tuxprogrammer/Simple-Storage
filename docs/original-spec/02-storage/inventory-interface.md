# Inventory Interface

## Identity
`com.tom.storagemod.block.InventoryInterfaceBlock` (SH) + `InventoryInterfaceBlockEntity` (SH). A cable node that re-exposes a connected Inventory Connector's merged inventory (used to reach a network across a gap or from the configurator).

## Inputs
- Server tick: staggered; work when `getGameTime() % 20 == Math.abs(worldPosition.hashCode()) % 20` (1 in 20 ticks).
- `setConnectorAccess(connector)`: called by the owning `InventoryConnectorBlockEntity` during its scan when the interface is in its `interfaces` set.

## Numbers
- Rescan period: 20 ticks, staggered by position hash.

## Behavior
1. (server, staggered tick) `updateServer`:
   - If `hasConnector` (set by a fresh `setConnectorAccess` this tick): clears the flag and keeps `connectorAccess`.
   - Else: nulls `connectorAccess`; if `connectorAccess` is still non-null (a fresh reference was just set) it becomes `networkAccess`; otherwise it re-discovers: first `IInventoryConnector` in the cable network at this position (loaded chunks) with `hasConnectedInventories()` becomes `networkAccess` (WeakReference).
2. `getInventoryAccess()`: if `networkAccess` is alive and `hasConnectedInventories()` → its `getMergedHandler()`; else `PlatformInventoryAccess.EMPTY`.
3. `getConnectorRef()`: the live connector, or null.
4. The block is both `IInventoryCable` and `IInventoryNode` (a functional node that also passes the network through — default `nextScan`, default `canConnectFrom`).
5. Tooltip: `tooltip.toms_storage.inventory_interface` = "Access the connected Inventory Connector's inventory".

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
