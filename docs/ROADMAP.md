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
- The planning phase (S1 to S3 and M01) is interactive: open questions go to the owner with a recommendation. S1 to S3 landed 2026-11-03, so the milestone scopes below are refined from the specs and their exit gates are written in full.

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
| **Spec** | S1, S4 | The original spec, the bug register, coverage and the API map |
| **Core** | M01 | Registration, config, network, the static checks, the dev self-check |
| **Storage** | M02, M03, M04 | The storage item and block, the terminal with its filters and sorting, the wireless terminal |
| **Integration** | S2, S3, M05, M06 | GT integration research, pack research, the GTNH layer, pack content and compat |

## Spikes

Status: **S1 to S4 are done** (2026-11-03). S1 delivered the six original-spec areas, 51 system sheets, [BUGS.md](original-spec/BUGS.md) (50 suspected bugs plus 1 checked not-a-bug (INT-04), 51 unique IDs), `COVERAGE.md` (217/217 upstream Java files) and `API-MAP.md` (every row resolved). S2 delivered the verified GT5-Unofficial tier table (HV cap = 512V, correcting 0003's draft figure, D15), the recipe maps and material conventions, the machine model, the NEI/Waila recipe-viewer mapping, the 22-row per-item tier map, and settled Q1 to Q4, recording decisions D7 to D16. S3 delivered the verified GTNH 2.9.0 target (released 2026-10-31), the 243-row MOD-INDEX, and the integration surfaces (AE2 rv3-beta-1095-GTNH present, NEI 2.8.171-GTNH, BetterQuesting 3.8.92-GTNH, Waila 1.19.34). Open questions: all settled — Q1 to Q9 and the 36 area proposals, decisions D17 to D29 ([OPEN-QUESTIONS.md](new-spec/OPEN-QUESTIONS.md) has no open rows). S4 delivered the new-spec areas `01` to `06` (deviation tables built from decisions D1 to D16, 36 Proposed rows listed in [OPEN-QUESTIONS.md](new-spec/OPEN-QUESTIONS.md)), `BUGS-COVERAGE.md` and the testing runbook ([docs/testing/](testing/README.md)). The milestone scopes below are now refined from the specs, and their exit gates are written in full.

| Spike | Delivers | Before | Lane |
|---|---|---|---|
| **S1 · Behavior spec** | All six original-spec areas per [CONVENTIONS.md](original-spec/CONVENTIONS.md), [BUGS.md](original-spec/BUGS.md), `COVERAGE.md` (every upstream Java file and resource named) and `API-MAP.md` (every NeoForge/Fabric API and its 1.7.10 counterpart, or none) | M02, M03, M04 | Spec |
| **S2 · GT integration research** | GT's recipe maps, materials and tier progression read from source at the pack's tag; design records [0003](design/0003-gtnh-layer-and-tier-ceiling.md) and [0004](design/0004-energy-and-machine-model.md) refined; the new-spec gtnh area (the tier map, the energy answer, the recipe-viewer surface) | M05 | Integration |
| **S3 · Pack research** | The reference pack and the pack's versions; the new-spec `00-pack-integration` area and record 0002 | M05, M06 | Integration |
| **S4 · New-spec areas** | New-spec areas `01` to `06` as deviation tables per [new-spec/README.md](new-spec/README.md), every original sheet and bug ID covered; `BUGS-COVERAGE.md`; [docs/testing/](testing/README.md) | M01 to M06 | Spec |

## M01 · Foundation

**Lane:** Core. **Parallel with:** S1, S2, S3.

**Entry gate:** this roadmap, [CONTRIBUTING.md](../CONTRIBUTING.md) and design records [0001](design/0001-port-policy-and-licensing.md) to [0004](design/0004-energy-and-machine-model.md) are committed (met: all on `master`).

**Budget:** 1 agent, 60 calls in total; model plan: top model for the static checks and the packet base classes (hard code), mid-tier for the wiring and docs. Stop and re-plan at 1.5x (`mod-port-budget`).

**Scope:**
- dependencies pinned to the pack per [0002](design/0002-target-pack.md) and the [Recommended dependencies](new-spec/00-pack-integration/integration-opportunities.md#recommended-dependencies-q4) table: GT5-Unofficial 5.09.54.265, GTNHLib 0.11.54 (`api`), NEI 2.8.171-GTNH, and the optional compat set (AE2 rv3-beta-1095-GTNH, waila 1.19.34, InGame-Info-XML 2.9.8, BetterQuesting 3.8.92-GTNH, EnhancedLootBags 1.3.5, Chisel 2.17.34-GTNH) as `compileOnly`, guarded by `Loader.isModLoaded` in the `compat` package; GT5-Unofficial and GTNHLib are hard runtime dependencies, declared `required-after` in `@Mod(dependencies=…)` and `mcmod.info` (D17);
- the package skeleton (`CONTRIBUTING.md` § Architecture);
- JUnit wired into the build;
- the static checks (`ClientIsolationTest`, `Java8ApiTest`, `PortedCodeGateTest`, `LangKeysTest`, `EventHandlerVisibilityTest`);
- the network channel with validated packet base classes;
- config with module flags per `config-safe-registration` and D16 (one `config/simplestoragenh.cfg`, GTNHLib `@Config`; the key catalog is the [PACK-CHANGES.md § Configs](PACK-CHANGES.md) table — `modules.*`, `euSink.*`, `network.*`, `wireless.*`, `behavior.*`);
- the dev self-check (its tier-cap checks land in M05 per [0003](design/0003-gtnh-layer-and-tier-ceiling.md) § Consequences);
- `THIRD-PARTY-NOTICES.md` and the changelog;
- CI green.

**Exit gate:**
- [ ] `./gradlew spotlessApply build` passes, with at least one real unit test and every static check running (each proven by a fixture that makes it fail).
- [ ] `./gradlew runServer` boots with the pack's core dependencies and no errors from this mod.
- [ ] `./gradlew runClient` reaches the main menu.
- [ ] `dependencies.gradle` carries every coordinate of the Recommended dependencies table at the pack's pinned version and the build resolves them (GT5-Unofficial 5.09.54.265, GTNHLib 0.11.54, NEI 2.8.171-GTNH, AE2 rv3-beta-1095-GTNH, waila 1.19.34, InGame-Info-XML 2.9.8, BetterQuesting 3.8.92-GTNH, EnhancedLootBags 1.3.5, Chisel 2.17.34-GTNH).
- [ ] The dev self-check passes on a fresh world.
- [ ] GT5-Unofficial and GTNHLib are declared `required-after`: a server without GT fails at load with a clear message (D17).
- [ ] Module flags load, save and are documented: every key of the [PACK-CHANGES.md § Configs](PACK-CHANGES.md) table is registered with its default, and flags gate behavior only, never registration.
- [ ] Packet base classes reject out-of-range, wrong-position and unauthorized requests, with tests (CORE-02: the client→server `DataPacket` is bounded and field-whitelisted).
- [ ] Every Proposed row of [new-spec 01-core](new-spec/01-core/index.md) (P01-*) is settled — built as recommended, or changed with a decision-log row — and the area's deviation table matches what shipped.

## M02 · Storage

**Lane:** Storage. **Parallel with:** M03, M04.

**Entry gate:** M01 done; S1's `02-storage` area and S4's [new-spec 02-storage](new-spec/02-storage/index.md) written (met: [02-storage](original-spec/02-storage/index.md), 14 sheets).

**Budget:** 1 agent, 50 calls in total; model plan: top model for the block entity and its NBT (hard code), mid-tier for spec transcription and the ported code. Stop and re-plan at 1.5x (`mod-port-budget`).

**Scope:** per the [02-storage](original-spec/02-storage/index.md) sheets: the storage blocks — the filing cabinet (512 slots, one item type per cabinet) and the open crate (the virtual inventory of item entities in front of it); the inventory trim and painted trim, and the paint kit (the `IPaintable` component, the painted block state); the cable network — the inventory cable (plain and framed), the cable connector (plain and framed), the inventory connector (20-tick staggered scan, `network.inventoryConnectorRange`), the proxy, the interface, the level emitter (the 0004 EU-sink target) and the basic inventory hopper (`basicHopperCooldown`); the cable-graph walk and its per-level cache; the item components (paint state, `bound_pos`); the NBT formats as the sheets specify, server-authoritative, readers tolerant of missing keys; every `STOR` bug of the ported logic (and the filing-cabinet container's CORE-05/CORE-06) fixed with its regression test; module flags `modules.storage` and `modules.network`; the new-spec 02-storage area (index.md deviation table plus a sheet for each deviation) written as M02 builds it.

**Exit gate:**
- [ ] All 13 storage/network blocks (including `painted_trim`) and the paint kit are registered; every block entity round-trips its NBT per the 02-storage sheets (round-trip test per block entity, missing keys tolerated, writers forward-compatible).
- [ ] The cable network walks and caches the graph per level per `cable-network.md`; a network built from cables, connectors, proxies and interfaces answers the same contents from every attached face, and `hasConnectedInventories` reflects actual connections (STOR-03).
- [ ] The staggered-tick gates hold for every position hash, including `Integer.MIN_VALUE` (STOR-01, STOR-05 and STOR-06 with `doesNotReproduce` tests).
- [ ] Every `STOR` bug of the ported logic (STOR-01 to STOR-10) and CORE-05/CORE-06 have their `doesNotReproduce<ID>_…` test, and the ported files pass the [ported-code gate](design/0001-port-policy-and-licensing.md) (provenance header, Javadoc, `Avoids:` lines).
- [ ] The static checks pass on the new code (`ClientIsolationTest`, `Java8ApiTest`, `PortedCodeGateTest`, `LangKeysTest`, `EventHandlerVisibilityTest`); the dev self-check passes on a fresh world.
- [ ] `modules.storage` and `modules.network` gate behavior only, never registration: with each flag off the blocks still register and the behavior is inert, tested.
- [ ] `./gradlew spotlessApply build` and `./gradlew runServer` are clean with the pack's core dependencies, no errors from this mod.
- [ ] Every Proposed row of [new-spec 02-storage](new-spec/02-storage/index.md) (P02-*) is settled — built as recommended, or changed with a decision-log row — and the area's deviation table matches what shipped.

## M03 · Terminal

**Lane:** Storage. **Parallel with:** M02, M04.

**Entry gate:** M01 done; S1's `03-terminal` area and S4's [new-spec 03-terminal](new-spec/03-terminal/index.md) written (met: [03-terminal](original-spec/03-terminal/index.md), 14 sheets).

**Budget:** 1 agent, 70 calls in total; model plan: top model for the inventory stack logic and the sorting (hard code), mid-tier for the screens and lang. Stop and re-plan at 1.5x (`mod-port-budget`).

**Scope:** per the [03-terminal](original-spec/03-terminal/index.md) sheets: the storage terminal and crafting terminal blocks, the item, ore-dictionary (tag) and polymorphic item filter items (D20) and the inventory configurator item, registered per the 01-core `mod-lifecycle.md` registry table; the terminal block entities (the item list lives in the block entity, lazily rebuilt on change); the screens, menus and `SlotStorage` display slots with the re-implemented vanilla slot mechanics (shift-click, drag); the `modes`/`sorting` bit layouts; the filter system (item, ore-dictionary and polymorphic filters) and the sorting system (five modes: amount, name, mod, space efficiency, ID; ordinal 5 reserved inert, D27); the inventory stack logic (`MultiInventoryAccess`, the change tracker) unit-tested with fake inventories so the lane does not wait on M02's blocks; the crafting terminal's grid and auto-fill (no in-GUI recipe book, D21) behind the `IAutoFillTerminal` contract; the inventory configurator bind tool and the inventory-link (beacon) screen; the level emitter GUI; the [06-client-ui](original-spec/06-client-ui/index.md) widget kit as the screens need it (icon buttons, lists, popups; the narrator layer is not ported, D21); every `TERM` bug of the ported logic (and CORE-03/CORE-04, CORE-07, CORE-08, CORE-10) fixed with its regression test; the `modules.terminal` flag; the new-spec 03-terminal area and the terminal-facing part of 06-client-ui (index.md deviation tables plus a sheet for each deviation) written as M03 builds them.

**Exit gate:**
- [ ] The terminal screens, menus and slots behave per the sheets for every control mode and sort type in the 03-terminal key facts (AE/RS/DEF, tall mode, ghost off), with the `modes`/`sorting` bit layouts exact and the `SlotStorage` display slots re-implementing shift-click and drag per `terminal-stacks.md`.
- [ ] The inventory stack logic is unit-tested with fake inventories: pull/push iterates lowest priority first, filtered insert/extract honor keepLast (CORE-04), multi-inventory offsets stay correct through compaction (CORE-03), and the change tracker survives same-tick network growth (TERM-12), against the spec's numbers.
- [ ] Every `TERM` bug of the ported logic (TERM-01 to TERM-12, except TERM-07 and TERM-09, which cannot occur: the narrator layer and the TYPE comparator are not ported, D21, D27) and CORE-07/CORE-10 have their `doesNotReproduce<ID>_…` test; CORE-08 (the polymorphic widget) is recorded as "not registered — polymorph is not in the pack"; the ported files pass the [ported-code gate](design/0001-port-policy-and-licensing.md).
- [ ] The widget kit ships with every lang key defined (`LangKeysTest`); UI-01, UI-02 and UI-07 cannot occur (narrator layer not ported, D21) and UI-03 to UI-06 are fixed where the widget lands here, with tests where one can be written (the EnumCycleButton NPE, the scrollbar grab rect, the popup click rects, the missing key).
- [ ] The crafting terminal's auto-fill reserves item accounting per cell (TERM-05), and the `IAutoFillTerminal` contract answers `updateSearch`, `sendMessage`, `getStoredItems` and `isSmartSearch` per the 05-integration shared viewer contract.
- [ ] The static checks pass (`ClientIsolationTest`, `Java8ApiTest`, `PortedCodeGateTest`, `LangKeysTest`, `EventHandlerVisibilityTest`); the dev self-check passes on a fresh world; `modules.terminal` gates behavior only, never registration.
- [ ] `./gradlew spotlessApply build` and `./gradlew runServer` are clean with the pack's core dependencies, no errors from this mod.
- [ ] Every Proposed row of [new-spec 03-terminal](new-spec/03-terminal/index.md) (P03-*) is settled — built as recommended, or changed with a decision-log row — and the area's deviation table matches what shipped.
- [ ] Every Proposed row of [new-spec 06-client-ui](new-spec/06-client-ui/index.md) (P06-*) is settled — built as recommended, or changed with a decision-log row — and the area's deviation table matches what shipped.

## M04 · Wireless

**Lane:** Storage. **Parallel with:** M02, M03.

**Entry gate:** M01 done; S1's `04-wireless` area and S4's [new-spec 04-wireless](new-spec/04-wireless/index.md) written (met: [04-wireless](original-spec/04-wireless/index.md), 2 sheets).

**Budget:** 1 agent, 40 calls in total; model plan: top model for the range and synchronization mechanics, mid-tier for the rest. Stop and re-plan at 1.5x (`mod-port-budget`).

**Scope:** per the [04-wireless](original-spec/04-wireless/index.md) sheets: the basic wireless item (extends the terminal's interaction reach, `wireless.reach` 4–64, default 16) and the advanced wireless item (binds to one terminal, `wireless.advRange` 16–512, default 64, opens it from anywhere including another dimension via the open-terminal keybind (ships unbound, D28)); the reach check running on the terminal's block entity per the 03-terminal sheets; the beacon levels (8-block radius, 40-tick refresh with a per-position phase) granting same-dimension access at `wireless.beaconLvl` and cross-dimension at `wireless.beaconLvlDim` (−1 disables each rule); the `bound_pos` data component with a stable NBT key; every `WIRE` bug of the ported logic fixed with its regression test; the `modules.wireless` flag; the new-spec 04-wireless area (index.md deviation table plus a sheet for each deviation) written as M04 builds it.

**Exit gate:**
- [ ] The basic wireless item extends reach to exactly `wireless.reach` (clamped 4–64): carrying the item in any slot, a player opens terminals in range and fails just out of range, with the check running on the terminal's block entity (unit tests at the range boundary).
- [ ] The advanced item binds to exactly one terminal (sneak + right-click) and opens that exact terminal from anywhere, including another dimension, via the open-terminal keybind (ships unbound, D28); when the bound terminal is invalid or its chunk is unloaded, the use is consumed consistently on both sides (WIRE-03).
- [ ] The open key ships unbound and the tooltip tells the player to bind it in Controls; no default key is registered, so it cannot clash with the pack (D28), and the lookup covers the held slot and the inventory (1.7.10 has no off-hand slot) only (D26).
- [ ] Beacon levels compute per `BeaconLevelCalc` (8-block radius, 40-tick refresh) with a per-position phase that holds for every hash including `Integer.MIN_VALUE` (WIRE-04/TERM-04), granting same-dimension access at `wireless.beaconLvl` and cross-dimension at `wireless.beaconLvlDim`, with −1 disabling each rule.
- [ ] Every `WIRE` bug of the ported logic (WIRE-01 to WIRE-05) has its `doesNotReproduce<ID>_…` test, and the ported files pass the [ported-code gate](design/0001-port-policy-and-licensing.md); `bound_pos` keeps a stable NBT key (WIRE-05) so old saves load.
- [ ] The static checks pass (`ClientIsolationTest`, `Java8ApiTest`, `PortedCodeGateTest`, `LangKeysTest`, `EventHandlerVisibilityTest`); no client-only state on the server; the dev self-check passes on a fresh world.
- [ ] `modules.wireless` gates behavior only, never registration; `./gradlew spotlessApply build` and `./gradlew runServer` are clean with the pack's core dependencies, no errors from this mod.
- [ ] Every Proposed row of [new-spec 04-wireless](new-spec/04-wireless/index.md) (P04-*) is settled — built as recommended, or changed with a decision-log row — and the area's deviation table matches what shipped.

## M05 · GTNH layer

**Lane:** Integration. **Parallel with:** none (integration).

**Entry gate:** M02, M03 and M04 done; S2 done (met: [0003](design/0003-gtnh-layer-and-tier-ceiling.md) and [0004](design/0004-energy-and-machine-model.md) refined with verified numbers, the [00-pack-integration](new-spec/00-pack-integration/index.md) sheets committed, decisions D7 to D16 recorded).

**Budget:** 1 agent, 50 calls in total; model plan: mid-tier for research transcription and the audits, top model for the recipe design. Stop and re-plan at 1.5x (`mod-port-budget`).

**Scope:** per [0003](design/0003-gtnh-layer-and-tier-ceiling.md) and the [00-pack-integration](new-spec/00-pack-integration/index.md) sheets: every recipe in the GT recipe maps per [recipes-and-materials.md](new-spec/00-pack-integration/recipes-and-materials.md) — all machine recipes in `gt.recipe.assembler` at the item's tier (D8) and the ULV rows as crafting-table recipes per the pack's crafting convention; the 22-row [tier map](new-spec/00-pack-integration/tier-map.md) landed row by row (LV/MV/HV), with GT materials and parts through ore-dict and no own materials; the optional EU sink per [0004](design/0004-energy-and-machine-model.md) §3 (the level emitter as a fixed consumer at 8 EU/t while active, `euSink.enabled` off by default, D7); the config finalized per D16 (one `config/simplestoragenh.cfg`, every key of the [PACK-CHANGES.md § Configs](PACK-CHANGES.md) table); the dev self-check walks every recipe against the tier map and the cap (cap HV = 512V, no item before its tier, the input-gate rule); the optional AE2 storage access behind `modules.integration.appeng` (off at alpha, D14).

**Exit gate:**
- [ ] Every tier-map row is a recipe: the 18 machine recipes in `gt.recipe.assembler` at the row's tier (13 LV, 1 MV, 4 HV after D18; D8) and the 3 crafting-table recipes per the crafting convention (21 of the 22 rows — `painted_trim` is painted in-game); the 15 blocks and 7 items each carry exactly one tier, nothing above HV = 512V.
- [ ] The dev self-check walks every recipe against the tier map and the 0003 cap: no output above HV, no item obtainable before its tier (the circuit input-gate rule holds for every LV/MV/HV row), no own material anywhere; the check fails on a deliberately bad fixture recipe.
- [ ] The S2 risks hold against the pinned build: the GT5-Unofficial pin in `dependencies.gradle` matches the pack's manifest (risk R1: if the pack bumps GT, re-run the re-pin procedure of the gtnh-integration-research skill before the recipes land), the gating circuit's own recipe tier matches each row's tier (risk R2), and no machine tier below LV exists (risk R5).
- [ ] The EU sink is live per [0004](design/0004-energy-and-machine-model.md) §3 (D7): the level emitter consumes 8 EU/t while active, `euSink.enabled` off by default, `euSink.euPerTick` the knob; tests with the sink on and off.
- [ ] The config is final per D16: one `config/simplestoragenh.cfg`, every key of the [PACK-CHANGES.md § Configs](PACK-CHANGES.md) table registered with its default, entries labeled (CORE-01) and no zero defaults before per-world sync (CORE-09); module flags gate behavior only, never registration, with `modules.integration.appeng` off at alpha (D14).
- [ ] The crafting-table rows use GT tools in the grid and the tools are not consumed (recipes-and-materials.md, checked in a real-pack run).
- [ ] Every tier-gated recipe is visible in NEI at its tier in the real pack (GT's own recipe handlers render the assembler recipes); the AE2 compat surface, if built, sits behind `Loader.isModLoaded("appeng")` in the `compat` package and off by default.
- [ ] The static checks pass (`ClientIsolationTest`, `Java8ApiTest`, `PortedCodeGateTest`, `LangKeysTest`, `EventHandlerVisibilityTest`); `./gradlew spotlessApply build` and the dev self-check are clean on a fresh world.

## M06 · Pack content and compatibility

**Lane:** Integration. **Parallel with:** none (integration).

**Entry gate:** M05 done; S3 done (met: [00-pack-integration](new-spec/00-pack-integration/index.md) and [0002](design/0002-target-pack.md) accepted).

**Budget:** 1 agent, 45 calls in total; model plan: mid-tier for research, catalogs and audits. Stop and re-plan at 1.5x (`mod-port-budget`).

**Scope:** per [PACK-CHANGES.md](PACK-CHANGES.md) and [recipe-viewer.md](new-spec/00-pack-integration/recipe-viewer.md): the NEI plugin `NEISimpleStorageConfig implements IConfigureNEI` per its § "What M06 builds" (D11) — the network-inventory `IUsageHandler` (the terminal shows what it holds, the AE2 usage-handler precedent), `registerGuiOverlay` on the terminal/cabinet/filter GUIs, and the `INEIGuiHandler` fast transfer, with ghost-drag deferred to beta; the Waila `IWailaBlock` providers (cabinet contents, terminal stats, emitter, cable/connector network size, hopper filter, configurator bound state) registered by IMC when Waila loads; upstream's JEI/REI/EMI/Jade/TOP/Polymorph/Sophisticated-Storage integrations mapped to NEI/Waila/none per [API-MAP.md](original-spec/API-MAP.md); the pack content per PACK-CHANGES.md: the three jar-shipped BetterQuesting quest lines per tier band (D12), the EnhancedLootBags placement rows and the config table; a module flag per compat: `modules.integration.nei` and `.waila` are built here; `.appeng` is registered by M01, its behavior built in M05, per `config-safe-registration`; the new-spec 05-integration area (index.md deviation table plus a sheet for each deviation) written as M06 builds it.

**Exit gate:**
- [ ] The NEI plugin is discovered by class scan and registers per D11: in the real pack GT's own handler shows the assembler recipes and our usage handler shows the terminal's stored items, the GUI overlays mount on the terminal/cabinet/filter screens and fast transfer works; ghost-drag is recorded as a beta candidate.
- [ ] Every INT row of [BUGS.md](original-spec/BUGS.md) (INT-01 to INT-10) is resolved: mapped to the NEI/Waila surface, fixed with a `doesNotReproduce<ID>_…` test where the code lands (INT-05's highlight mapping, INT-10's clickable area), or recorded "none" with its API-MAP evidence (TOP and Sophisticated Storage are not in the pack); no upstream integration dies silently.
- [ ] The Waila providers render the 05-integration sheet's info (cabinet, terminal stats, emitter, network size, hopper, configurator) when Waila loads and nothing when it does not; their registration is the IMC `Waila`/`register` message, guarded by `Loader.isModLoaded("Waila")`.
- [ ] The jar-shipped quest line loads via the BQ importer in the real pack: the three tier-band lines of PACK-CHANGES.md exist with namespaced IDs `simplestoragenh.*` (risk R3: the pack may instead override from its quest repo per D12 — either way the line content matches PACK-CHANGES.md).
- [ ] The EnhancedLootBags placement rows (ULV to HV) and the TooMuchLoot note from PACK-CHANGES.md are delivered or recorded with a decision; the config table is complete, and every compat has its module flag (`.appeng` registered by M01, its behavior built in M05), gating behavior only, never registration.
- [ ] Every Proposed row of [new-spec 05-integration](new-spec/05-integration/index.md) (P05-*) is settled — built as recommended, or changed with a decision-log row — and the area's deviation table matches what shipped.
- [ ] The static checks pass (`ClientIsolationTest`, `Java8ApiTest`, `PortedCodeGateTest`, `LangKeysTest`, `EventHandlerVisibilityTest`); the dev self-check passes on a fresh world; `./gradlew spotlessApply build` and `./gradlew runServer` are clean with the pack's core dependencies, no errors from this mod.

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
- [ ] The owner's interactive review: play sessions with back-and-forth on the GTNH integrations and the advanced features. It ends with the owner's sign-off and a list of balance items for the polish pass. The 0003 "basic terminal" reading (D13, S2 risk R4) is flagged in the review and its outcome recorded in the [decision log](design/decision-log.md).

**Open before the gate:** M01 to M06 and the alpha gate; the owner's review is the last Beta exit item above.
