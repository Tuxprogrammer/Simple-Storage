# 02 · Storage

## Overview

The storage side of Tom's Simple Storage: the blocks that hold or expose inventories (filing cabinet, open crate, inventory proxy, trims), the network wiring (inventory cables, cable connectors, inventory connector, inventory interface), the redstone/hopper interfaces (level emitter, basic inventory hopper), the item/component layer (paint kit, painted items, filter/configurator data components), and the network/registry machinery those blocks form (cable network, remote connections, proxy/link interfaces). Documents upstream Tom's Simple Storage 2.11.3, branch `26.2` at commit `95bb61d` (Minecraft 26.2, NeoForge 26.2.0.90). The NeoForge side is the port baseline; `OpenCrateBlockEntity` and `PaintedBlockEntity` live in `NeoForge/src/main` (NF) — noted per sheet.

## Contents

| Sheet | What it covers |
|---|---|
| [filing-cabinet.md](filing-cabinet.md) | `FilingCabinetBlock` + `FilingCabinetBlockEntity` + `FilingCabinetContainer` — the 512-slot… |
| [open-crate.md](open-crate.md) | `OpenCrateBlock` + `OpenCrateBlockEntity` (NF) — virtual inventory of the front block's item entities. |
| [inventory-proxy.md](inventory-proxy.md) | `InventoryProxyBlock` + `InventoryProxyBlockEntity` — extends an inventory face for the network. |
| [trims-paint.md](trims-paint.md) | `TrimBlock`, `PaintedTrimBlock`, `IPaintable`, `PaintedBlockEntity` (NF), `IConfiguratorHighlight`,… |
| [cables.md](cables.md) | `InventoryCableBlock`, `FramedInventoryCableBlock`, `IInventoryCable`, `IInventoryNode`. |
| [cable-connector.md](cable-connector.md) | `InventoryCableConnectorBlock` + `FramedInventoryCableConnectorBlock` — attaching a face to the network,… |
| [inventory-connector.md](inventory-connector.md) | `InventoryConnectorBlock` + `InventoryConnectorBlockEntity` — touching-inventory scan, cable linking,… |
| [inventory-interface.md](inventory-interface.md) | `InventoryInterfaceBlock` + `InventoryInterfaceBlockEntity` — pass-through view of a connector's network. |
| [level-emitter.md](level-emitter.md) | `LevelEmitterBlock` + `LevelEmitterBlockEntity` — threshold redstone signal from network contents. |
| [hopper.md](hopper.md) | `AbstractInventoryHopperBlock`, `BasicInventoryHopperBlock`, `AbstractInventoryHopperBlockEntity`,… |
| [items.md](items.md) | `PaintKitItem`, `PaintedBlockItem`, `BlockItemTSS`, `ILeftClickListener`. |
| [components.md](components.md) | `ConfiguratorComponent`, `SimpleItemFilterComponent`, `TagFilterComponent`, `WorldPos`. |
| [containers.md](containers.md) | `BasicContainer`, `LimitedContainer` (utility containers). |
| [cable-network.md](cable-network.md) | `InventoryCableNetwork`, `RemoteConnections`, `IInventoryLink`, `IInventoryConnectorReference`, `IProxy`,… |

## Also in this area

- [facts.md](facts.md): key facts, related areas, lang keys.
- [coverage.md](coverage.md): what was read, and known limits.
- [bugs.md](bugs.md): suspected upstream bugs and deviations in the new mod.

New mod: [../../new-spec/02-storage/index.md](../../new-spec/02-storage/index.md)
