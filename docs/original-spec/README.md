# Tom's Simple Storage behavior spec

A description of **Tom's Simple Storage Mod 2.11.3 for Minecraft 26.2** (upstream branch `26.2`, commit `95bb61d`, NeoForge 26.2.0.90, [tom5454/Toms-Storage](https://github.com/tom5454/Toms-Storage)): how it behaves, every number and tunable, its data formats, and its suspected bugs. Upstream is MIT, so code may be ported ([0001](../design/0001-port-policy-and-licensing.md)). This spec is not a clean-room substitute for the code. It is the **behavior contract** that unit tests and the ported-code gate check against, and the register of upstream bugs that must not come back. How this mod differs from upstream, and how it fits GTNH, is in [docs/new-spec/](../new-spec/README.md).

Status: **written** — S1 landed 2026-11-03: 6 areas, 51 system sheets, COVERAGE.md 217/217 upstream Java files, 54 rows reported, 51 unique IDs after cross-area dedup: 50 suspected bugs plus INT-04, checked and found not a bug (register in [BUGS.md](BUGS.md)).

| Area | Covers | Bug prefix |
|---|---|---|
| `01-core` | Mod lifecycle and registration, the mod config, network, platform layer (NeoForge/Fabric split), shared utilities | `CORE` |
| `02-storage` | The storage item with its internal inventory, the storage block and its block entity, item components | `STOR` |
| `03-terminal` | The terminal block and entity, its screen, menu and slots, the filter and sorting systems, the inventory stack logic | `TERM` |
| `04-wireless` | The basic and advanced wireless terminal items and the `WirelessTerminal` interface, with range and synchronization mechanics | `WIRE` |
| `05-integration` | JEI/REI/EMI recipe-viewer plugins, Jade integration, other compat | `INT` |
| `06-client-ui` | Screen widgets, client package, lang keys, textures, sounds | `UI` |

Reference files, created with the areas:

| File | What it holds |
|---|---|
| [CONVENTIONS.md](CONVENTIONS.md) | The standard every area meets |
| [BUGS.md](BUGS.md) | Every suspected upstream bug, merged across areas |
| [COVERAGE.md](COVERAGE.md) | Every upstream Java file and resource, and the sheet that covers it |
| [API-MAP.md](API-MAP.md) | Each NeoForge/Fabric API upstream relies on and its 1.7.10 counterpart, or "none" |

## Sources

- The upstream code and resources at `95bb61d`: `git show 26.2:<path>`, or `semantic_search` with `source: upstream` ([tools/semantic-search/](../../tools/semantic-search/README.md)). The shared code lives in `NeoForge/src/platform-shared/` (the port baseline); the loader-specific code in `NeoForge/src/main/java/` and `Fabric/src/main/java/`.
- The upstream README at the branch root (the feature list) and the `version-check*.json` files, which record the supported versions.
- The older upstream branches (`1.15`, `1.16`, and the rest) where their APIs are closer to 1.7.10's: cited with pinned permalinks, per [CONVENTIONS.md](CONVENTIONS.md).
