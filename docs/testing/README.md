# Testing

How to run each test layer of [CONTRIBUTING § Testing](../../CONTRIBUTING.md#testing). Every test class and command that does not exist yet is marked **planned (M0x)** with its milestone. The bug-coverage generator exists today. Testing guidance lives in the `testing-gtnh-mods` skill; the rules are below.

## Verification tiers

| Tier | Proves | Command | When | Required by |
|---|---|---|---|---|
| T1 | the change builds and its logic holds | `./gradlew spotlessApply build` | every change | every change (definition of done) |
| T2 | the lane's features work in world | `./gradlew runServer` + the lane's scenarios, 2-minute smoke boot | lane landing | Core M01 exit gate (clean `runServer` boot); every lane landing |
| T3 | the whole mod works headless | full headless suite, one world type | batch of landings | integrator, once per batch |
| T4 | the real pack works | real pack, every world type, shot list, budgets | gates only (alpha, beta, release) | alpha, beta, release gates |

Lanes run T1 and T2 only; the integrator runs T3 once per batch.

## 1. Unit tests

- **Framework:** JUnit 5 in `src/test/java`, mirroring the main package structure; run with `./gradlew test` — **exists (M01)**, the registration and config it covers land in M01 (Core).
- **What they cover:**
  - every formula and decision in `filter`, `sort` and `inventory`: filter matching, sort and arrangement, slot and stack operations;
  - **inventory round-trips:** storing and retrieving a stack preserves it exactly, NBT included;
  - **filter and sort golden tests:** deterministic output for fixed inputs — the same inventory and settings give the same arrangement, in the same order, every run;
  - **NBT format tests:** the internal inventory format round-trips, and each historic format it must still read (the upgrade paths) loads;
  - config and module-flag defaults, and packet encode/decode round-trips.
- **Speed:** tests are deterministic (seeded randomness, no wall-clock time) and fast.

## 2. Static checks

Tests that read the compiled classes and resources and fail the build — **exists (M01)**:

| Check | Proves |
|---|---|
| `ClientIsolationTest` | no common class references a client class |
| `Java8ApiTest` | no API newer than Java 8 |
| `PortedCodeGateTest` | the [ported-code gate](../../CONTRIBUTING.md#ported-code) |
| `LangKeysTest` | every player-visible string is a lang key, and every lang key, texture and sound referenced in code exists in resources |
| `EventHandlerVisibilityTest` | every `@SubscribeEvent` handler and its class are public |

Each static check is proven by a fixture that makes it fail, so none can pass because it looked at nothing.

## 3. Headless server boot and the dev self-check

- **Server boot smoke test (T2):** `./gradlew runServer` starts with the pack dependencies loaded and no errors or warnings from this mod, with every module off and with every module on. **Planned (M01)**: the M01 exit gate is a clean `./gradlew runServer` boot with the pack's core dependencies and no errors from this mod, and `./gradlew runClient` reaching the main menu.
- **Dev self-check:** in the dev environment, a `postInit` check that every registered thing has a lang key and a texture, every recipe resolves, the tier map holds, and the NBT format round-trips. A failure stops the dev server. **Planned (M01)** except its tier-cap checks, which land in M05.

## 4. In-world scenarios

`/simplestoragenh test <all|list|TestClass>` (op only; development environment, or `-Dsimplestoragenh.testHooks=true`; JUnit is not shipped in the release jar, so in-game scenarios are dev-only, D32) runs the JUnit classes tagged `ingame` inside the live server with a small reflection runner and prints `PASS`/`FAIL` lines and `Scenarios: N passed, M failed`. `tools/remote/work.sh server` runs all of them unattended on the build host (`-Dsimplestoragenh.autotest=true` stops the server when done; `--flags-off` boots with every behavior module off). Tests that need real Items, Blocks or tile-entity mappings are tagged `ingame`; plain `./gradlew test` excludes them.

- **Multiplayer:** for anything synced, two clients on a dedicated server: the second client sees the same inventory contents, GUI state matches, the server wins on conflict, and nothing desyncs after a relog.

## 5. Real-pack and visual checks (T4)

- **Real pack:** the release jar on a GTNH dedicated server and client, at the alpha and beta gates.
- **Visual:** `runClient` and real-pack screenshots of the blocks and GUIs, including with shaders where rendering is involved. Visual changes keep their screenshots with the change's evidence.

## Bug regressions and coverage

Each upstream bug a feature could reproduce gets a regression test named for it: `doesNotReproduce<ID>_<what>`, for example `doesNotReproduceSTOR01_rescanGateFiresForMinValueHash`, with IDs from `docs/original-spec/BUGS.md` (register template in `docs/templates/bug-register.md`).

`docs/original-spec/BUGS-COVERAGE.md` is generated by `tools/bugs-coverage/generate.py` (standard library only, exists today):

```sh
python3 -I tools/bugs-coverage/generate.py .
```

For every ID in `BUGS.md` it lists the guarding test, or the reason recorded in `docs/original-spec/bug-reasons.json` as `{"ID": ["status", "text"]}` with statuses `tested`, `cannot occur`, `kept` or `not a bug`. IDs with neither are listed as `MISSING`. Run it when a bug gets a test or a reason, and commit the result; a dedicated test or CI step can fail the build when the table reports `MISSING`. **MISSING must be zero before an area is done.**

## Exists today vs planned

| Item | Status |
|---|---|
| `tools/bugs-coverage/generate.py` and `BUGS-COVERAGE.md` generation | exists today |
| `docs/original-spec/bug-reasons.json` convention | exists today (status records) |
| Unit tests, `./gradlew test` | exists (M01) |
| `ClientIsolationTest`, `Java8ApiTest`, `PortedCodeGateTest`, `LangKeysTest`, `EventHandlerVisibilityTest` | exists (M01) |
| `./gradlew runServer` smoke boot, dev self-check | exists (M01); self-check tier-cap checks planned (M05) |
| In-game scenario runner (`/simplestoragenh test`, autotest) | exists (M02) |
| Real-pack server and client runs, screenshots | planned (alpha/beta gates, T4) |

## Running and waiting on long jobs

Builds and Minecraft runs happen on the build host (`<build-host>`), not on a development laptop; nothing machine-specific (hostnames, usernames, local paths) goes into tracked files. Wrap them in `tools/agent/quiet-run.sh` and read the done-marker, never the whole log:

- `remote.sh build <slot>`, `devserver <slot>`, `realpack`, `client` run on `<build-host>` through `tools/remote/remote.sh`; each run writes a done marker (`run.done` with status, boot seconds, peak RSS).
- Wait with one capped wait, `tools/agent/wait-marker.sh <marker> [timeout]`, used as the command of one Monitor — block on the marker, print one line, no polling.
- Summarize with `tools/agent/run-summary.py <run-dir>`: status, PASS/FAIL/ERROR counts, first failures, budget lines folded to min/median/max.

## Definition of done

A change is done when all of these hold:

- [ ] It implements its spec sheets, with any deviations recorded.
- [ ] `./gradlew spotlessApply build` passes, and unit tests cover the new logic and the relevant BUGS.md regressions.
- [ ] Static checks pass, and ported code passes the ported-code gate.
- [ ] `runServer` boots cleanly, and the dev self-check passes.
- [ ] A scenario exists for features that touch a block or a GUI, and it passes; multiplayer is checked for synced features.
- [ ] Tier-gated recipes are visible in the recipe viewer (NEI), and no GT material is duplicated.
- [ ] Every string is a lang key, and every asset's provenance is recorded.
- [ ] Javadoc and `// Spec:` comments are in place. There is no commented-out code.
- [ ] The roadmap box is ticked, and docs are updated in the same change.
