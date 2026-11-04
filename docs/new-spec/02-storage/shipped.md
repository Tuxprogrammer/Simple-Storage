# 02 Storage · what milestone M02 shipped

Part of [02 Storage](index.md). Written when M02 closed; the deviation table in [deviations.md](deviations.md) says what differs from upstream, this page says what exists in code and what waits for a later milestone.

## Shipped

| Original sheet | 1.7.10 code (package com.tuxprogrammer.simplestorage) | Tests |
|---|---|---|
| filing-cabinet | block.FilingCabinetBlock, blockentity.FilingCabinetTileEntity (ISidedInventory, 512 slots, NBT `inventory`/`Slot` short/`CustomName`), inventory.CabinetModel | CabinetModelTest, FilingCabinetTileEntityTest (STOR-02, CORE-05, CORE-06) |
| open-crate | block.OpenCrateBlock, blockentity.OpenCrateTileEntity (virtual inventory of EntityItem in front, rescan every 5 ticks, append slot, no NBT) | OpenCrateTileEntityTest (STOR-01, STOR-04) |
| inventory-proxy | block.InventoryProxyBlock, blockentity.FaceNodeTileEntity (front-face inventory node, paintable) | FaceNodeTileEntityTest |
| trims-paint | block.TrimBlock, block.PaintedTrimBlock, block.IPaintable, blockentity.PaintedTileEntity (NBT `paintBlock` registry name, `paintMeta` int 0-15, tolerant) | PaintedTileEntityTest |
| cables | block.InventoryCableBlock, block.FramedInventoryCableBlock, block.IInventoryCable | CableGraphTest |
| cable-connector | block.InventoryCableConnectorBlock, block.FramedInventoryCableConnectorBlock (FaceNodeTileEntity) | FaceNodeTileEntityTest |
| inventory-connector | block.InventoryConnectorBlock, blockentity.InventoryConnectorTileEntity (20-tick staggered flood scan, `network.inventoryConnectorRange`), inventory.ConnectorScan | ConnectorScanTest, InventoryConnectorTileEntityTest (STOR-03, STOR-05) |
| inventory-interface | block.InventoryInterfaceBlock, blockentity.InventoryInterfaceTileEntity | InventoryInterfaceTileEntityTest (STOR-03, STOR-05) |
| level-emitter | block.LevelEmitterBlock, blockentity.LevelEmitterTileEntity (10-tick gate, signal 15 or 0, NBT `Filter`/`Count`/`lessThan`), inventory.EmitterLogic | EmitterLogicTest, LevelEmitterTileEntityTest (STOR-06, STOR-07) |
| hopper | block.InventoryHopperBlock, blockentity.InventoryHopperTileEntity, inventory.HopperLogic | HopperLogicTest, InventoryHopperTileEntityTest (STOR-08, STOR-09) |
| items | item.PaintKitItem (64 uses, paint stored in item NBT) | PaintKitItemTest |
| cable-network | inventory.CableGraph, inventory.CableNetworks, blockentity.WorldTopology, blockentity.NetworkQuery | CableGraphTest (STOR-03), CableNetworksTest |
| channels (RemoteConnections logic) | inventory.ChannelRegistry (pure; deterministic fallback name `Channel <n>`) | ChannelRegistryTest (STOR-10) |
| staggered gates | inventory.StaggerGate | StaggerGateTest (STOR-01, STOR-05, STOR-06) |

## Registration

- 13 blocks, 1 item and 8 tile entities are registered always.
- module flags `modules.storage` and `modules.network` only gate ticking and node exposure (`blockentity.GatedTileEntity`).
- tile entity ids (`simplestoragenh:<id>`) and registry names are never renamed.

## Deferred to later milestones

| What | Milestone | Why |
|---|---|---|
| filing cabinet GUI (container and screen) | M03 | terminal GUI work |
| inventory-link GUI and `RemoteConnections` saved data wiring (`simplestorage_rc`) for ChannelRegistry | M04 | wireless and linking |
| filter components (item filter, ore-dictionary tag filter) and the configurator (`bound_pos`, `BlockFilter`) | M03 and M04 | used by the terminal and wireless items |
| block textures, item icons, models and the facade renderer for painted blocks | M06 | client visual pass |
| recipes (assembler and crafting table) | M05 | tier map work |
| multiblock inventory hook and AE2 access | M05 | integration |
| hopper acceleration stages `midCd`/`fastCd` | not ported | M02 ships the base cooldown with an idle backoff (D31) |

## Testing notes

- unit tests cover the pure logic.
- tests that need real Items, Blocks or tile-entity mappings are tagged `ingame` and run inside the dev server (D32): `tools/remote/work.sh server` prints their PASS/FAIL lines.
- the first in-game run found two real bugs (cabinet slots all loaded into slot 0 because the NBT list was mutated while reading, and the open crate double-decremented a split stack).
