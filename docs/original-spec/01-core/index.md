# 01 · Core

## Overview

The mod's lifecycle and everything shared by all other areas of Tom's Simple Storage 2.11.3 (upstream branch `26.2`, commit `95bb61d`, NeoForge 2.26.2 baseline): mod entry and registration (`StorageMod`, `Content`), the two-file config (per-world server config + common config), the network channel (`DataPacket`/`OpenTerminalPacket`), the NeoForge/Fabric platform split (deferred registration, capabilities, attachments, the inventory-access adapter classes over the NeoForge transfer API), the terminal inventory-sync protocol (`TerminalSyncManager` — shared by wired and wireless terminals), and the shared utility classes (containers, filter bases, data slots, misc helpers, the multiblock API). Where a fact differs by loader, the sheet says which loader it came from; the Fabric side is read for loader-difference notes only.

## Contents

| Sheet | What it covers |
|---|---|
| [mod-lifecycle.md](mod-lifecycle.md) | mod entry, client setup, `Content` registration table (all registry names), `ServerEvents`, `StorageTags`,… |
| [config.md](config.md) | every config key in both toml files with type, default, range, translation key and meaning; Fabric… |
| [network.md](network.md) | the `toms_storage:data` (bidirectional) and `toms_storage:open_terminal` (to server) payloads, handlers,… |
| [platform-layer.md](platform-layer.md) | `Platform` (NF/Fabric), `GameObject`/registries, `PlatformItem`, `PlatformBlockEntity`, `PlatformItemHandler` |
| [inventory-access.md](inventory-access.md) | the NF inventory-access adapters (`BlockInventoryAccess`/capability cache, proxy, multi-inventory offsets,… |
| [terminal-sync.md](terminal-sync.md) | `TerminalSyncManager` packet protocol (stack id table, 64000-byte split, mini-stack overflow, interaction… |
| [util-containers.md](util-containers.md) | `BasicContainer`, `LimitedContainer`, `FilingCabinetContainer`, `CraftingMatrix` |
| [util-filters.md](util-filters.md) | `AbstractSimpleItemFilter` (9-slot working container, match-NBT/allow-list) and `AbstractTagFilter` bases… |
| [util-data-slots.md](util-data-slots.md) | `DataSlots` and `IntDataSlots` (two 16-bit halves) factories |
| [util-misc.md](util-misc.md) | `BlockFace(Reference)`, `ComponentJoiner`, `KeyUtil`, `NumberFormatUtil`, `PlayerInvUtil`,… |
| [util-api.md](util-api.md) | `EventHandler`, `MultiblockInventoryAPI`, `PolymorphHelper`, `PolymorphTerminalWidget` (dead on NF) |

## Also in this area

- [facts.md](facts.md): key facts, related areas.
- [coverage.md](coverage.md): what was read, and known limits.
- [bugs.md](bugs.md): suspected upstream bugs.

New mod: [../../new-spec/01-core/index.md](../../new-spec/01-core/index.md)
