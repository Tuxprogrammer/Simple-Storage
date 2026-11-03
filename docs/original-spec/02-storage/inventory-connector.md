# Inventory Connector

## Identity
`com.tom.storagemod.block.InventoryConnectorBlock` (SH) + `InventoryConnectorBlockEntity` (SH). The core network endpoint: merges all touching inventories (through trims, up to a scan range) into one multi-inventory, and links to other connectors over cables.

## Inputs
- Right-click (empty hand, server): sends usage info to the player and returns SUCCESS.
- Server tick: staggered (`TickerUtil.createTicker(world, false, true)`); work happens when `getGameTime() % 20 == Math.abs(worldPosition.hashCode()) % 20` (1 in 20 ticks).
- Config: `invConnectorScanRange` (int, squared to a max distance), `onlyTrims` (boolean).

## Numbers
- Rescan period: 20 ticks, staggered by position hash.
- Scan radius: `invConnectorScanRange` squared (squared-Euclidean bound, not a box).
- `UsageInfo` record: `(blocks, all, free)` = inventory count, slot count, free slot count.

## Behavior
1. (server, staggered tick) `updateServer`:
   a. `detectTouchingInventories()`:
      - BFS/DFS from the connector position (stack + checked set), bound by `p.distSqr(worldPosition) < maxRange`.
      - TRIMS blocks (tag `StorageTags.TRIMS`) expand the search.
      - `IInventoryNode` blocks (interfaces) are recorded as `interfaces` (face DOWN) and expand the search.
      - Any other block that passes `InventoryConnectorConfigUtil.canConnect(state)` and has an inventory on that face (`BlockInventoryAccess.hasInventoryAt`) is recorded in `connected` (pos → side).
      - Multiblock detection: `MultiblockInventoryAPI.EVENT.invoker().detectMultiblocks(level, p, state, mbCheck)` — third-party hook (05-integration); `onlyTrims` config stops the search from entering multiblock parts.
      - `BlockFilter` (configurator, 01-core): a filter at the connector position wraps all accesses; filters found on connected blocks remove their `getConnectedBlocks()` from `connected`, and (if not skipped) contribute their own access at `getMainPos()`/`getSide()`; collected filters' `skip()` excludes them.
      - Accesses are reused from the previous scan when the same `BlockFace` persists; stale ones are `markInvalid`-ed.
      - If the `interfaces` set changed: mark old interface positions + self invalid in the cable network.
      - Each `InventoryInterfaceBlockEntity` in `interfaces` gets `setConnectorAccess(this)`.
   b. `detectCableNetwork()`: clears `linkedConnectors`, then adds every `IInventoryConnector` block entity found among the cable network nodes at this position (loaded chunks only, self excluded).
   c. `handler.build(this, linkedConnectors)`: rebuilds the `PlatformMultiInventoryAccess` (01-core) — the merged inventory with per-inventory `Priority` ordering.
2. `getUsage()`: `UsageInfo(inventoryCount, slotCount, freeSlotCount)` from the handler.
3. (both) `nextScan`: 6 neighbors + all recorded `interfaces` faces.
4. `useWithoutItem` overlay text: `chat.toms_storage.inventory_connector.free_slots` = "Inventory Connector: %d free/%d slots (in %d inventories)." (args: free, all, blocks).

## Determinism
Scan order: LIFO stack (DFS) — first-found inventory wins the `connected` map on duplicate faces; `linkedConnectors` is a HashSet.

## Data formats
No NBT of its own (state is re-derived from the world each scan).

## Output
- `getMergedHandler()` / `getInventoryAccess()`: the merged `MultiInventoryAccess` (01-core `inventory-access.md`).
- `getConnectedInventories()`, `getConnectedConnectors()`, `getConnectedBlocks()`, `getInterfaces()`.
- `getConnectorRef() = this` (implements `IInventoryConnectorReference`).

## Relationships
- `cables.md`, `cable-network.md`, `inventory-interface.md`, `trims-paint.md` (TRIMS tag).
- 01-core: `BlockInventoryAccess`, `PlatformMultiInventoryAccess`, `BlockFilter`, `InventoryConnectorConfigUtil`, `MultiblockInventoryAPI` (05-integration), `StorageTags`, `Config`, `TickerUtil`, `IInventoryConnector`.
- 03-terminal: `NetworkInventory` reads the merged handler.

## NeoForge dependencies
`MultiblockInventoryAPI.EVENT` (NeoForge event), codec, `BlockStateProperties`, `TickerUtil` (platform ticker), `NeoForgeBlock` marker.

## Bugs
STOR-03 (`hasConnectedInventories()` = `!isRemoved()`, `InventoryConnectorBlockEntity.java:188`); STOR-05 (Math.abs rescan gate, `InventoryConnectorBlockEntity.java:50`).
