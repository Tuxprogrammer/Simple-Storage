# Inventory Connector

## Identity
`com.tom.storagemod.block.InventoryConnectorBlock` (SH) + `InventoryConnectorBlockEntity` (SH). The core network endpoint: merges all touching inventories (through trims, up to a scan range) into one multi-inventory, and links to other connectors over cables.

Sources (upstream 26.2@95bb61d): `NeoForge/src/platform-shared/java/com/tom/storagemod/block/InventoryConnectorBlock.java` and `.../block/entity/InventoryConnectorBlockEntity.java`, config in `NeoForge/src/main/java/com/tom/storagemod/Config.java`; short names below are `Class.java:line` in those files (class declarations: InventoryConnectorBlock.java:39, InventoryConnectorBlockEntity.java:36).

## Inputs
- Right-click (empty hand, server): sends usage info to the player and returns SUCCESS (InventoryConnectorBlock.java:69-76).
- Server tick: staggered (`TickerUtil.createTicker(world, false, true)`) (InventoryConnectorBlock.java:54); work happens when `getGameTime() % 20 == Math.abs(worldPosition.hashCode()) % 20` (1 in 20 ticks) (InventoryConnectorBlockEntity.java:48-50).
- Config: `invConnectorScanRange` (int, squared to a max distance), `onlyTrims` (boolean) (InventoryConnectorBlockEntity.java:84-86; Config.java:57, 61, 165-166).

## Numbers
- Rescan period: 20 ticks, staggered by position hash (InventoryConnectorBlockEntity.java:50).
- Scan radius: `invConnectorScanRange` squared (squared-Euclidean bound, not a box) (InventoryConnectorBlockEntity.java:84-85, 99); config default 16, range 4-256 (Config.java:57).
- `UsageInfo` record: `(blocks, all, free)` = inventory count, slot count, free slot count (InventoryConnectorBlockEntity.java:171).

## Behavior
1. (server, staggered tick) `updateServer`:
   a. `detectTouchingInventories()`:
      - BFS/DFS from the connector position (stack + checked set), bound by `p.distSqr(worldPosition) < maxRange` (InventoryConnectorBlockEntity.java:99).
      - TRIMS blocks (tag `StorageTags.TRIMS`) expand the search (InventoryConnectorBlockEntity.java:102-104).
      - `IInventoryNode` blocks (interfaces) are recorded as `interfaces` (face DOWN) and expand the search (InventoryConnectorBlockEntity.java:104-107).
      - Any other block that passes `InventoryConnectorConfigUtil.canConnect(state)` and has an inventory on that face (`BlockInventoryAccess.hasInventoryAt`) is recorded in `connected` (pos → side) (InventoryConnectorBlockEntity.java:107-111).
      - Multiblock detection: `MultiblockInventoryAPI.EVENT.invoker().detectMultiblocks(level, p, state, mbCheck)` — third-party hook (05-integration); `onlyTrims` config stops the search from entering multiblock parts (InventoryConnectorBlockEntity.java:88-91, 113).
      - `BlockFilter` (configurator, 01-core): a filter at the connector position wraps all accesses; filters found on connected blocks remove their `getConnectedBlocks()` from `connected`, and (if not skipped) contribute their own access at `getMainPos()`/`getSide()`; collected filters' `skip()` excludes them (InventoryConnectorBlockEntity.java:72-77, 108, 119, 133).
      - Accesses are reused from the previous scan when the same `BlockFace` persists; stale ones are `markInvalid`-ed (InventoryConnectorBlockEntity.java:140-145).
      - If the `interfaces` set changed: mark old interface positions + self invalid in the cable network (InventoryConnectorBlockEntity.java:147-152).
      - Each `InventoryInterfaceBlockEntity` in `interfaces` gets `setConnectorAccess(this)` (InventoryConnectorBlockEntity.java:154-157).
   b. `detectCableNetwork()`: clears `linkedConnectors`, then adds every `IInventoryConnector` block entity found among the cable network nodes at this position (loaded chunks only, self excluded) (InventoryConnectorBlockEntity.java:40, 57-67).
   c. `handler.build(this, linkedConnectors)`: rebuilds the `PlatformMultiInventoryAccess` (01-core) — the merged inventory with per-inventory `Priority` ordering (InventoryConnectorBlockEntity.java:53).
2. `getUsage()`: `UsageInfo(inventoryCount, slotCount, freeSlotCount)` from the handler (InventoryConnectorBlockEntity.java:167-168).
3. (both) `nextScan`: 6 neighbors + all recorded `interfaces` faces (InventoryConnectorBlock.java:122-123).
4. `useWithoutItem` overlay text: `chat.toms_storage.inventory_connector.free_slots` = "Inventory Connector: %d free/%d slots (in %d inventories)." (args: free, all, blocks) (InventoryConnectorBlock.java:76).

## Determinism
Scan order: LIFO stack (DFS) — first-found inventory wins the `connected` map on duplicate faces; `linkedConnectors` is a HashSet (InventoryConnectorBlockEntity.java:40).

## Data formats
No NBT of its own (state is re-derived from the world each scan).

## Output
- `getMergedHandler()` / `getInventoryAccess()`: the merged `MultiInventoryAccess` (01-core `inventory-access.md`) (InventoryConnectorBlockEntity.java:174, 193).
- `getConnectedInventories()`, `getConnectedConnectors()`, `getConnectedBlocks()`, `getInterfaces()` (InventoryConnectorBlockEntity.java:179, 183, 197, 202).
- `getConnectorRef() = this` (implements `IInventoryConnectorReference`) (InventoryConnectorBlockEntity.java:36, 207).

## Relationships
- `cables.md`, `cable-network.md`, `inventory-interface.md`, `trims-paint.md` (TRIMS tag).
- 01-core: `BlockInventoryAccess`, `PlatformMultiInventoryAccess`, `BlockFilter`, `InventoryConnectorConfigUtil`, `MultiblockInventoryAPI` (05-integration), `StorageTags`, `Config`, `TickerUtil`, `IInventoryConnector`.
- 03-terminal: `NetworkInventory` reads the merged handler.

## NeoForge dependencies
`MultiblockInventoryAPI.EVENT` (NeoForge event), codec, `BlockStateProperties`, `TickerUtil` (platform ticker), `NeoForgeBlock` marker.

## Bugs
STOR-03 (`hasConnectedInventories()` = `!isRemoved()`, `InventoryConnectorBlockEntity.java:188`); STOR-05 (Math.abs rescan gate, `InventoryConnectorBlockEntity.java:50`).

## Deviations in the new mod

See [new-spec 02-storage](../../new-spec/02-storage/index.md) (row: inventory-connector).
