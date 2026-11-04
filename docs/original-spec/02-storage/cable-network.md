# Cable Network & Remote Connections

## Identity
`InventoryCableNetwork` (SH/inventory), `RemoteConnections` (SH/inventory, a `SavedData`), `IInventoryLink` (SH/inventory), `IInventoryConnectorReference` (SH/inventory), `IProxy` (SH/inventory), `IChangeTrackerAccess` (SH/inventory), `BlockFilterAttachment` (NF). The graph machinery that connects cable nodes and the world-level channel registry for inventory linking.

Sources (upstream 26.2@95bb61d): `NeoForge/src/platform-shared/java/com/tom/storagemod/inventory/{InventoryCableNetwork,RemoteConnections,IInventoryLink,IProxy,IChangeTrackerAccess,IInventoryConnectorReference}.java` and `NeoForge/src/main/java/com/tom/storagemod/block/entity/BlockFilterAttachment.java`; short names below are `Class.java:line` in those files.

## Inputs
- `InventoryCableNetwork`: one instance per level, held in `WorldStates.cableNetworks` (01-core `WorldStates`).
- `RemoteConnections`: per-dimension `SavedData` loaded from the **overworld** data storage; id `toms_storage:rc` (RemoteConnections.java:31, 39, 74).

## Numbers
- Channel name length limit: 50 chars (`makeChannel`); empty/overlong names fall back to `"Channel " + System.currentTimeMillis()` (see STOR-10) (RemoteConnections.java:129-131).
- `findOthers(world, pos, lvl)`: `lvl` is the beacon level; connectors are kept only while their BE is loaded and `isAccessibleFrom(world, pos, lvl)` (RemoteConnections.java:100, 110).

## Behavior
1. `InventoryCableNetwork.getNetworkNodes(from)` (server):
   - BFS/DFS (LIFO stack) from `from` over `IInventoryCable` blocks; a neighbor is traversed only if `canConnectFrom(neighborState, approach direction)` (InventoryCableNetwork.java:26, 30, 34-40).
   - Unloaded chunks stop the walk (`level.isLoaded`) (InventoryCableNetwork.java:38).
   - `isFunctionalNode()` blocks go to `attached` (the returned set — the network's endpoints); non-functional (cables) go to `cables` and keep expanding (InventoryCableNetwork.java:48-50).
   - **Cache**: after a scan, a `CableCache(cables, attached)` is stored for every *cable* position in the result; a later scan starting at a cached cable position merges all cached `cables`/`attached` sets instead of re-walking. (Functional nodes are not cached — they always re-scan, so they pick up topology changes.) (InventoryCableNetwork.java:41-47, 53-54; note: a scan whose start position is itself a cached cable returns that cache's `attached` directly, InventoryCableNetwork.java:27-28.)
   - Returns the `attached` set (the functional nodes) (InventoryCableNetwork.java:55).
2. `markNodeInvalid(pos)`: drops the cached entry for `pos` and, through it, for every cable in that cached network — i.e., the whole cached network is invalidated (InventoryCableNetwork.java:58-61). Called by all cable-family blocks on destroy/neighbor change (see `cables.md`, `cable-connector.md`).
3. `RemoteConnections` (world saved data):
   - `Channel`: `connectors` (Set of `WorldPos`), `owner` UUID, `ownerName`, `publicChannel`, `displayName` (RemoteConnections.java:77-82).
   - `makeChannel(name, isPublic, owner)`: random UUID id; name fallback (STOR-10); `setDirty()` (RemoteConnections.java:129-133).
   - `removeChannel(id, player)` / `editChannel(id, pub, player)`: owner-only mutations (RemoteConnections.java:137, 145).
   - `canAccess(player)`: public or owner. `streamChannels(player)`: accessible channels (RemoteConnections.java:120, 158-159).
   - `Channel.findOthers(world, pos, lvl)`: **registers** the caller's `WorldPos` into `connectors`, then resolves every other stored position: loaded `IInventoryLink` BEs pass `isAccessibleFrom(world, pos, lvl)` into the result set; dead positions are pruned during iteration (RemoteConnections.java:100-117).
   - `invalidateCache(id)`: clears a channel's connector cache (RemoteConnections.java:153).
   - Codec: `connections` list of `Connection(channelId, owner, ownerName, displayName, isPublic)` — fields `id`, `owner`, `owner_name`, `name`, `public`. Note: the connector positions are **not** persisted (re-registered at runtime) (RemoteConnections.java:33-35, 55-63, 95-98).
4. `IInventoryLink`: `getConnector()`, `isAccessibleFrom(world, pos, level)`, `getChannel()` (IInventoryLink.java:11-13) — implemented by `InventoryCableConnectorBlockEntity` (out of 02 scope; see index Known limits) and wireless terminal BEs (04-wireless).
5. `IInventoryConnectorReference`: `getConnectorRef()` — implemented by connectors/interfaces (this area) so caches can resolve back to a connector (IInventoryConnectorReference.java:6).
6. `IProxy`: `getRootHandler(Set<IProxy> dejaVu)` — proxy-chain root resolution (used by `InventoryProxyBlockEntity`, cycle-safe via the dejaVu set) (IProxy.java:6).
7. `IChangeTrackerAccess`: `tracker()` — single-method view over an `IInventoryChangeTracker` (01-core `IInventoryAccess`) (IChangeTrackerAccess.java:6).
8. `BlockFilterAttachment` (NF): a NeoForge **attachment type** holding a `BlockFilter` per block entity; serializes `BlockFilter.storeState()` via `BlockFilter.STATE_CODEC` (01-core `BlockFilter`). `BlockFilter.getFilterAt(level, pos)` reads it (BlockFilterAttachment.java:11, 23-29).

## Determinism
Scan order: LIFO stack; `attached`/`cables` are HashSets (order-insensitive consumers). Channel iteration order: HashMap order (nondeterministic across runs).

## Data formats
Saved data `toms_storage:rc`: `{connections: [{id, owner, owner_name, name, public}]}` (RemoteConnections.java:35, 59-63).

## Output
- `getNetworkNodes` → the set of functional-node positions for a network.
- `RemoteConnections` → channel registry powering the inventory-link GUI (03-terminal `inventory-link.md`) and beacon-gated linking.

## Relationships
- `cables.md`, `cable-connector.md`, `inventory-connector.md`, `inventory-interface.md`.
- 01-core: `WorldStates`, `BlockFilter`, `IInventoryAccess`, `Config` (beacon levels/range).
- 03-terminal: `inventory-link.md` (GUI), `NetworkInventory` (uses `getNetworkNodes` + connector references).

## NeoForge dependencies
`SavedData`/`SavedDataType` with codec, `DataFixTypes.LEVEL`, NeoForge attachment types (`IAttachmentHolder`, `ValueIOSerializable`), `WorldStates` (01-core platform state).

## Bugs
STOR-10 (time-based channel name fallback, `RemoteConnections.java:131`).

## Deviations in the new mod

See [new-spec 02-storage](../../new-spec/02-storage/index.md) (row: cable-network).
