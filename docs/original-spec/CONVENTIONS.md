# Spec conventions

The standard every area of this spec meets, so the terminal's menu, a filter, a cable and a network packet are each documented to the same depth. The underlying rules are in the `behavior-spec` skill. This file adapts them to Tom's Simple Storage, which is MIT and may be ported ([0001](../design/0001-port-policy-and-licensing.md), decision log D2).

## What is documented

- **Version:** Tom's Simple Storage Mod `2.11.3` on upstream branch `26.2` at `95bb61d` (Minecraft 26.2, NeoForge 26.2.0.90). The NeoForge side (the `NeoForge/src/platform-shared/` code plus the `NeoForge/` sources) is the port baseline; the Fabric side (`Fabric/`) exists, and where a fact differs by loader the sheet says which loader it came from. Source paths are relative to the repository root on that commit.
- **Older branches:** upstream has older branches (from the local `upstream` remote: `1.15`, `1.16`, `1.17`, `1.18`, `1.19`, `1.19.x`, `1.20.x`, `1.21.1`, `1.21.4`, `1.21.5`, `1.21.8`, `1.21.10`, `1.21.11`, `26.1`, `master`, plus `fabric-1.16` to `fabric-1.19.3`, `hopper-patch-2` and `snapshot`). Where an older branch — `1.15` and `1.16` above all, whose Forge APIs are closest to 1.7.10's — solves the same problem with an API closer to 1.7.10, the sheet adds a short **"On <branch>"** note: what exists there, file paths, and whether it behaves differently.

## Ground rules

- **Behavior, numbers, formats and bugs; not a code transcript.** The code is portable, so the spec doesn't restate it line by line. It records what the code must achieve, so tests can check a port:
  - the algorithm as ordered steps;
  - every constant and default;
  - every random choice and what seeds it;
  - every data field (NBT key, config key, recipe field);
  - every edge case.
- **Units always:** blocks, chunks (16 blocks), block-Y levels, ticks (20 per second), EU, percentages and probabilities stated as the code uses them (0–1 or 0–100).
- **Label sides.** Every step says (client), (server) or (both).
- **Determinism is part of the behavior.** For every random choice, say which `Random` it uses and how that is seeded (world seed, a salt), and whether results depend on tick order or shared state.
- **NeoForge dependencies are called out.** Where behavior rests on a NeoForge/Fabric mechanism with no direct 1.7.10 counterpart (item handlers and capabilities, data components, block entities vs tile entities, deferred registration, the network API, data-driven recipes and models, the screen and menu classes), say so and add the row to [API-MAP.md](API-MAP.md). Don't design the 1.7.10 answer here; that belongs in the new spec.
- **Say "none" explicitly** for fields that do not apply.
- **Unreachable content is documented** and marked *unused*, *dead code* or *config-only*.
- **Inferred is not observed.** Where a sheet describes how something looks or behaves in the world without having run it, it says *inferred*.
- **Only add.** Expanding one area never trims another. Correct only what is wrong and note it in the area's coverage section.
- **Cross-references** use area folder names (`03-terminal`) or relative links. Write class names, config keys, NBT keys and bug IDs in backticks.

## System sheet

Every documented system gets a sheet with these fields in this order, for example the terminal's menu, the filter system, the sorting comparators, the cable network, or the wireless range check. Template: [docs/templates/spec-sheet.md](../templates/spec-sheet.md), with the fields below in place of the entity fields.

| # | Field | Contents |
|---|---|---|
| 1 | **Identity** | Classes and their roles; the blocks, items, config keys and NBT keys involved |
| 2 | **Inputs** | What it reads: config, NBT, inventory access, network state, player input |
| 3 | **Numbers** | Every constant, default and range, with units and the config key that changes it |
| 4 | **Behavior** | The algorithm as ordered steps, each labeled with its side, including every edge case and fallback |
| 5 | **Determinism** | Each random source, its seed, and any dependence on tick order, caches or shared state |
| 6 | **Data formats** | Every NBT key and data component it writes (name, type, meaning), and every config key it reads |
| 7 | **Output** | What ends up in the world or the player's inventory, or what it hands to the next stage |
| 8 | **Relationships** | What calls it and what it calls, linked across areas |
| 9 | **NeoForge dependencies** | Mechanisms with no direct 1.7.10 counterpart, each also listed in `API-MAP.md` |
| 10 | **Bugs** | Inline bug IDs (`TERM-12`) for everything suspicious |

Small related systems may share a sheet.

## Lang

Each area lists every lang key in its scope with its exact English text, strings built in code with the condition that produces them, and any missing keys or hard-coded English.

## Area index

Every `index.md` follows [docs/templates/spec-area-index.md](../templates/spec-area-index.md): Overview, Contents, Key facts, Related areas, Coverage (files read out of files in scope; assets and lang keys covered out of those in scope), Known limits, Suspected bugs.

## Bug IDs

| Area | Prefix |
|---|---|
| 01-core | `CORE` |
| 02-storage | `STOR` |
| 03-terminal | `TERM` |
| 04-wireless | `WIRE` |
| 05-integration | `INT` |
| 06-client-ui | `UI` |

Columns: `ID | Title | Location | What looks wrong | Severity` (crash, gameplay, performance, visual, cosmetic — there is no worldgen in this mod). Locations are `path/Class.java:line` on `95bb61d`, or the resource path. Suspected, not confirmed; no fixes designed in the spec. Upstream GitHub issues that confirm a bug are linked in its row. A finding later checked and found correct keeps its ID and row: its severity becomes "not a bug" and its row gives the evidence.
