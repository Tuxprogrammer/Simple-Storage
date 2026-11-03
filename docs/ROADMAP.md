# Roadmap

<!-- Milestones in one lane run in order; lanes run in parallel once their entry gates hold. Tick a box only in the commit or pull request that completes it. -->

Simple Storage backports Tom's Simple Storage Mod to GT New Horizons: the same storage network — the storage blocks and items, the terminal with its filters and sorting, and the wireless access — rebuilt on GT materials and recipe maps. Upstream's behavior is in [docs/original-spec/](original-spec/README.md), what changes for the pack in [docs/new-spec/](new-spec/README.md), and the accepted decisions in [docs/design/](design/README.md).

Two gates matter most:

- **Alpha:** a player in a fresh GTNH real-pack world (dedicated server) can store and retrieve items through the terminal, the wireless terminal works within range, and no errors from this mod appear on the server.
- **Beta, the MVP:** the whole mod, unpolished. Every spec sheet and design record is functional and live in the mod, so only polish and balance remain. This is where the real review happens: interactive back-and-forth with the owner to iron out the GTNH integrations and the advanced features.

## How to read this roadmap

- **Spikes (S1…)** are research or spec work that must land before the milestones named in them. They run in parallel with whatever is under way.
- **Milestones (M01…)** each have an **entry gate** (true before work starts), a budget, a scope and an **exit gate** (the definition of done).
- **Lanes** are the agent teams. A lane owns its feature area's code, tests and docs, so lanes don't collide. Milestones in different lanes run **in parallel** once their entry gates hold.
- Each milestone's content ships behind a module toggle that changes behavior only, never registration (`config-safe-registration`).
- Every change meets the definition of done in `CONTRIBUTING.md`: spec sheet cited, deviations recorded, regression tests for upstream bugs it could reproduce (`doesNotReproduce<ID>_…`, IDs from [BUGS.md](original-spec/BUGS.md)), ported code through the gate, headless boot clean.
- The planning phase (S1 to S3 and M01) is interactive: open questions go to the owner with a recommendation. Milestone scopes below are refined from the specs when S1 to S3 land, and their exit gates are written in full then.

```
 S1 Behavior spec ─────────┐
 S2 GT integration ────────┼──► (new spec written) ──┐
 S3 Pack research ─────────┘                         │
 M01 Foundation ─────────────────────────────────────┤
                                                      ▼
                ┌────────────────┬───────────────────┴────────────────┐
                ▼                ▼                                    ▼
           M02 Storage      M03 Terminal                       M04 Wireless
                └────────────────┴───────────────────┬────────────────┘
                                                     ▼
                                              M05 GTNH layer
                                                     ▼
                                          M06 Pack content and compat
                                                     ▼
                                                ALPHA GATE
                                                     ▼
                                              BETA GATE (MVP) ──► Polish and balance ──► Release
```

## Lanes

| Lane | Milestones | Owns |
|---|---|---|
| **Spec** | S1 | The original spec, the bug register, coverage and the API map |
| **Core** | M01 | Registration, config, network, the static checks, the dev self-check |
| **Storage** | M02, M03, M04 | The storage item and block, the terminal with its filters and sorting, the wireless terminal |
| **Integration** | S2, S3, M05, M06 | GT integration research, pack research, the GTNH layer, pack content and compat |

## Spikes

Status: **S3 is done this round** by the parallel pack-research lane (the new-spec `00-pack-integration` area and record 0002). **S1 and S2 are not started.**

| Spike | Delivers | Before | Lane |
|---|---|---|---|
| **S1 · Behavior spec** | All six original-spec areas per [CONVENTIONS.md](original-spec/CONVENTIONS.md), [BUGS.md](original-spec/BUGS.md), `COVERAGE.md` (every upstream Java file and resource named) and `API-MAP.md` (every NeoForge/Fabric API and its 1.7.10 counterpart, or none) | M02, M03, M04 | Spec |
| **S2 · GT integration research** | GT's recipe maps, materials and tier progression read from source at the pack's tag; design records [0003](design/0003-gtnh-layer-and-tier-ceiling.md) and [0004](design/0004-energy-and-machine-model.md) refined; the new-spec gtnh area (the tier map, the energy answer, the recipe-viewer surface) | M05 | Integration |
| **S3 · Pack research** | The reference pack and the pack's versions; the new-spec `00-pack-integration` area and record 0002 | M05, M06 | Integration |

## M01 · Foundation

**Lane:** Core. **Parallel with:** S1, S2, S3.

**Entry gate:** this roadmap, `CONTRIBUTING.md` and design records 0001 to 0004 are committed.

**Budget:** 1 agent, 60 calls in total; model plan: top model for the static checks and the packet base classes (hard code), mid-tier for the wiring and docs. Stop and re-plan at 1.5x (`mod-port-budget`).

**Scope:**
- dependencies pinned to the pack per [0002](design/0002-target-pack.md);
- the package skeleton (`CONTRIBUTING.md` § Architecture);
- JUnit wired into the build;
- the static checks (`ClientIsolationTest`, `Java8ApiTest`, `PortedCodeGateTest`, `LangKeysTest`);
- the network channel with validated packet base classes;
- config with module flags per `config-safe-registration`;
- the dev self-check;
- `THIRD-PARTY-NOTICES.md` and the changelog;
- CI green.

**Exit gate:**
- [ ] `./gradlew spotlessApply build` passes, with at least one real unit test and every static check running (each proven by a fixture that makes it fail).
- [ ] `./gradlew runServer` boots with the pack's core dependencies and no errors from this mod.
- [ ] `./gradlew runClient` reaches the main menu.
- [ ] The dev self-check passes on a fresh world.
- [ ] Module flags load, save and are documented.
- [ ] Packet base classes reject out-of-range, wrong-position and unauthorized requests, with tests.

## M02 · Storage

**Lane:** Storage. **Parallel with:** M03, M04.

**Entry gate:** M01 done; S1's `02-storage` area written.

**Budget:** 1 agent, 50 calls in total; model plan: top model for the block entity and its NBT (hard code), mid-tier for spec transcription and the ported code. Stop and re-plan at 1.5x (`mod-port-budget`).

**Scope:** the storage item with its internal inventory; the storage block and its block entity; the item components that carry per-item state; NBT formats as specified; every `STOR` bug of the ported logic fixed, with its regression test; the module toggle.

**Exit gate:** written in full when S1 to S3 land.

## M03 · Terminal

**Lane:** Storage. **Parallel with:** M02, M04.

**Entry gate:** M01 done; S1's `03-terminal` area written.

**Budget:** 1 agent, 70 calls in total; model plan: top model for the inventory stack logic and the sorting (hard code), mid-tier for the screens and lang. Stop and re-plan at 1.5x (`mod-port-budget`).

**Scope:** the terminal block and entity; its screen, menu and slots; the filter system (item and tag filters) and the sorting system; the inventory stack logic that ties the network's inventories together, unit-tested with fake inventories so the lane does not wait on M02's blocks; every `TERM` bug fixed, with its regression test; the module toggle.

**Exit gate:** written in full when S1 to S3 land.

## M04 · Wireless

**Lane:** Storage. **Parallel with:** M02, M03.

**Entry gate:** M01 done; S1's `04-wireless` area written.

**Budget:** 1 agent, 40 calls in total; model plan: top model for the range and synchronization mechanics, mid-tier for the rest. Stop and re-plan at 1.5x (`mod-port-budget`).

**Scope:** the wireless terminal and its screen; range and synchronization; the advanced wireless variant; every `WIRE` bug fixed, with its regression test; the module toggle.

**Exit gate:** written in full when S1 to S3 land.

## M05 · GTNH layer

**Lane:** Integration. **Parallel with:** none (integration).

**Entry gate:** M02, M03 and M04 done; S2 done (records [0003](design/0003-gtnh-layer-and-tier-ceiling.md) and [0004](design/0004-energy-and-machine-model.md) refined, the new-spec gtnh area written).

**Budget:** 1 agent, 50 calls in total; model plan: mid-tier for research transcription and the audits, top model for the recipe design. Stop and re-plan at 1.5x (`mod-port-budget`).

**Scope:** every recipe in the GT recipe maps per [0003](design/0003-gtnh-layer-and-tier-ceiling.md), tier-gated to the named cap, with the per-item tier map; GT materials and parts through ore-dict, no own materials; the optional EU sink per [0004](design/0004-energy-and-machine-model.md) if S2 keeps it; the recipe-viewer handlers for the pack's viewer; the dev self-check walks every recipe against the tier map and the cap.

**Exit gate:** written in full when S1 to S3 land.

## M06 · Pack content and compatibility

**Lane:** Integration. **Parallel with:** none (integration).

**Entry gate:** M05 done; S3 done (new-spec `00-pack-integration` and record 0002 accepted).

**Budget:** 1 agent, 45 calls in total; model plan: mid-tier for research, catalogs and audits. Stop and re-plan at 1.5x (`mod-port-budget`).

**Scope:** the recipe-viewer plugin for the pack's viewer (Q4); the info-mod integration if S3 finds one; the remaining compat (the belt placement, multiblock inventories, the probe); pack content per [PACK-CHANGES.md](PACK-CHANGES.md) (questbook entries, loot, configs); a module flag per compat.

**Exit gate:** written in full when S1 to S3 land.

## Alpha gate

**Entry gate:** M05 and M06 done.

**Exit gate:**
- [ ] On a fresh GTNH real-pack world (dedicated server), a player can store and retrieve items through the terminal: an item pushed into a connected storage block appears in the terminal and comes back out, with the counts exact.
- [ ] The wireless terminal works within range, and does nothing out of range, with no client-only state on the server.
- [ ] No errors from this mod on the server: a boot, a full play session and a client join are all clean.

## Beta gate (MVP)

**Entry gate:** the alpha gate.

**Exit gate:**
- [ ] Every original-spec sheet is implemented or has a recorded deviation; every new-spec sheet is live.
- [ ] Every reproducible bug in [BUGS.md](original-spec/BUGS.md) has its regression test.
- [ ] The full scenario suite passes on a fresh world; the real-pack server and client run is clean on the target pack.
- [ ] The owner's interactive review: play sessions with back-and-forth on the GTNH integrations and the advanced features. It ends with the owner's sign-off and a list of balance items for the polish pass.

**Open before the gate:** the owner's playtest of the beta jar ([README](../README.md)).
