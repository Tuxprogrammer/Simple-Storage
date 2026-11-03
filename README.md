# Simple Storage

A storage network for **GT New Horizons** (Minecraft 1.7.10): terminals, item/tag/poly filters, stack sorting and a cable network. This is a backport of [Tom's Simple Storage](https://github.com/tom5454/Toms-Storage), built for GTNH 2.9.0.

Store items in blocks, reach them from a terminal across the whole network — by exact item, by tag, or by a poly filter — and let the terminal sort the result. Cable the storage together, watch levels with an emitter, and reach a network from anywhere with a wireless terminal.

> [!NOTE]
> **In development — planning and spec phase done, no playable build yet.** Spike S1 (behavior spec) and S3 (pack research) have landed; S2 (GT integration) is in flight. What builds next, and each gate, is in [docs/ROADMAP.md](docs/ROADMAP.md).

## What it does

- **Storage blocks.** The filing cabinet and the open crate, each with its own documented internal inventory.
- **Terminals with filters and sorting.** A storage terminal and a crafting terminal, each with item, tag and poly filters and stack sorting.
- **The cable network.** Cables, connectors, interfaces and level emitters that tie storage together into a network.
- **Wireless access.** Wireless terminals (basic and advanced) with configurable range.
- **Inventory configurator.** Edit a storage block's contents and filters from the outside.
- **Paint kit.** Re-skin storage blocks.
- **Recipe viewer and info integrations.** NEI/REI/EMI recipe-viewer plugins and Waila/Jade/TOP overlays.

## Requirements

- **GT New Horizons 2.9.0**, server and client. The pack provides GTNHLib, the one hard dependency.

## Installing

The same jar goes on the server and on every client: a server running the mod turns away clients without it.

### Dedicated server

1. Stop the server and put `simplestoragenh-<version>.jar` in `mods/`.
2. Start the server with a **new world** (delete or rename the old `world/` folder, or set `level-name` to a new name).

### Client

Put the same `simplestoragenh-<version>.jar` in the instance's `mods/` folder. For single player, create a new world.

## Building from source

```sh
./gradlew build        # the jar is in build/libs/
./gradlew runClient    # development client
```

Contributors start with [CONTRIBUTING.md](CONTRIBUTING.md). The roadmap is in [docs/ROADMAP.md](docs/ROADMAP.md), the design records in [docs/design/](docs/design/README.md), the behavior spec in [docs/original-spec/](docs/original-spec/README.md) and the pack-facing changes in [docs/new-spec/](docs/new-spec/README.md).

Builds and Minecraft runs belong on the build host, not a development laptop: drive it with [tools/remote/remote.sh](tools/remote/README.md); host and paths come from the git-ignored `.env`.

## License

MIT; see [LICENSE](LICENSE). Tom's Simple Storage is © 2020 tom5454, under the MIT License; see [THIRD-PARTY-NOTICES.md](THIRD-PARTY-NOTICES.md).
