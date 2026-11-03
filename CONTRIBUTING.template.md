# Contributing to <Mod Name>

<!-- TEMPLATE. Copy to CONTRIBUTING.md, replace every <placeholder>, delete the sections and bracketed notes that do not apply, and keep the result short enough that agents can read it by section (`grep -n '^## '`). -->

<Mod Name> <rebuilds / ports / integrates> <upstream mod or pack> for **GT New Horizons (Minecraft 1.7.10)**. It is faithful to the original's functionality, not its bugs. These rules apply to every change, whether a person or an AI agent makes it.

> [!IMPORTANT]
> **must** is a hard rule; **should** is a strong default. A change that breaks a *must* does not land. To depart from a *should*, say why in the commit message or pull request.

## Contents

1. [Workflow](#workflow) 2. [Ported code](#ported-code) 3. [Toolchain and build](#toolchain-and-build) 4. [Architecture](#architecture) 5. [World generation rules](#world-generation-rules) 6. [GTNH integration](#gtnh-integration) 7. [Code and comments](#code-and-comments) 8. [Testing](#testing) 9. [Assets and localization](#assets-and-localization) 10. [Commits and landing](#commits-and-landing) 11. [Definition of done](#definition-of-done)

---

## Workflow

1. **Start from the roadmap.** Every change belongs to a milestone, spike or decision gate in `docs/ROADMAP.md` (template: [docs/templates/roadmap.md](docs/templates/roadmap.md)). Do not start a milestone whose entry gate is not met, and do not build what a decision gate blocks until its record in `docs/design/` is `Accepted`.
2. **Read the spec.** Read the feature's sheets in `docs/original-spec/` (how the original behaves; start at the area's `index.md`) and `docs/new-spec/` (what changes and how it plugs into the pack).
3. **Check the bug register.** Read the feature's rows in `docs/original-spec/BUGS.md`. Original bugs **must not** be reproduced; where practical a test proves it.
4. **Record deviations.** A deliberate difference from the original (a 1.7.10 limit, a pack block in place of a modern block, a bug fix, a GTNH integration) goes in the new-spec area and, if it is a design choice, in a design record. A small one needs only a `// Deviation:` comment citing the spec section.
5. **Keep changes focused.** One logical change per commit. Parallel work runs in lanes ([Commits and landing](#commits-and-landing)).

## Ported code

<!-- Keep this section only for a port of code under a license that allows it. For all-rights-reserved or incompatible licenses, use the clean-room route instead (skill `clean-room-reimplementation`). -->

The upstream mod is <license> (decision record `docs/design/0001-port-policy-and-licensing.md`), so its code may be ported. A port is a rewrite that starts from the upstream logic, never a copy made for speed. Ported code passes this gate before it lands (**must**):

- **Pinned source.** Port from one named baseline commit of `<upstream-branch>`; cite the exact commit.
- **A header on every ported file**, directly under the package declaration:

  ```java
  // Ported from <Upstream Mod> (<license>): https://github.com/<owner>/<repo>/blob/<commit>/<path>/Widget.java
  // Avoids: WIDGET-03, WIDGET-07
  ```

  Omit `Avoids:` only when the spec lists no bug in the ported logic. Name bugs by their row ID in `BUGS.md`.
- **The notice.** `THIRD-PARTY-NOTICES.md` keeps the upstream notice; `LICENSE` keeps the upstream copyright line.
- **Our style, not theirs.** Our package layout, naming and idioms; the Java 8 API; Javadoc on every class and non-trivial public method; spec-traceability comments. Modern-version abstractions with no 1.7.10 counterpart become plain Java.
- **Tests.** Unit tests cover the spec's numbers, and each bug on the `Avoids:` line has a regression test.
- **Every known bug is fixed.**

A static check (for example `PortedCodeGateTest`) should verify the header, notice, Javadoc, test class and bug IDs so CI fails an incomplete port. Other third-party code: prefer MIT, CC0, CC-BY or LGPL; never unlicensed, non-commercial or all-rights-reserved code.

## Toolchain and build

| Item | Rule |
|---|---|
| JDK | As in [`.java-version`](.java-version). Gradle provisions it. |
| Language level | Modern **syntax** only (`enableModernJavaSyntax = jabel`) on the **Java 8 API**. No records, sealed types, `List.of`, `Map.of`, `Optional.isEmpty`, `String.isBlank`, `Stream.toList`. |
| Formatting | Spotless with the GTNH shared config. Run `./gradlew spotlessApply` before every commit. |
| Build files | **Never edit** `build.gradle.kts` or `settings.gradle.kts` (they come from the GTNH buildscript). Use `dependencies.gradle`, `repositories.gradle` and `gradle.properties`. |
| Dependencies | GTNH Maven coordinates with the `:dev` classifier, pinned to the versions of the target pack (<pack version>, record `docs/design/0002-target-pack.md`). `compileOnly` for optional integrations; never shade a mod. |

```sh
./gradlew spotlessApply build   # format, compile, test, package
./gradlew test                  # unit and static-check tests only
./gradlew runServer             # dev dedicated server (boot smoke test, scenarios)
./gradlew runClient             # dev client
```

## Architecture

<!-- Describe your packages and the dependency rules between them, for example: -->

- `<pkg>.core`: no Minecraft types; pure logic and data, unit-tested without a world.
- `<pkg>.common`: server-safe code; **never** references `<pkg>.client`.
- `<pkg>.client`: rendering, GUI, key bindings; loaded only on the client.
- Every module has a config toggle that changes behavior only, never registration (skill `config-safe-registration`).

## World generation rules

<!-- Keep for mods that generate terrain or structures. -->

- Deterministic: the same seed, coordinates and config give the same result, regardless of generation order.
- No cascading generation: populating a chunk never loads or generates another.
- A per-chunk time budget, measured in a scenario.
- Build test fixtures out of something other than stone (the pack's ore generator replaces it).

## GTNH integration

- **Source before guesses** (**must**): read another mod's source at the tag the pack ships before integrating; cite it in a comment or record (skill `gtnh-integration-research`).
- GT and the pack first: GT materials, recipe maps, voltage tiers and EU; do not duplicate what the pack provides (skill `gt-first-design`).
- Optional integrations are guarded so the mod loads without them.
- Anything that needs the pack to change is listed in `docs/PACK-CHANGES.md` and delivered from the jar where possible (skill `in-pack-content-delivery`).

## Code and comments

- Javadoc on every class and non-trivial public method. Explain why, not what.
- A `// Spec: docs/original-spec/<area>/<sheet>.md#<anchor>` comment on code that implements a sheet; `// Deviation: <what and why> (decision NNNN)` where it differs.
- No commented-out code. No wildcard imports. American English in code, docs and lang.
- Servers never load client classes and never trust clients (skill `networking-side-safety`).

## Testing

Layers and techniques are in the `testing-gtnh-mods` skill. Rules:

1. **Unit tests (must).** JUnit 5 in `src/test/java`, mirroring the main packages. Deterministic and fast. Every formula and decision, parsing and validation (including malformed input), config defaults, NBT round-trips, and a `doesNotReproduce<ID>_<what>` test for each original bug a feature could reproduce.
2. **Static checks (must).** Tests that fail the build on: a common class referencing a client class, API newer than Java 8, a missing lang key, an invalid bundled asset. Prove each check with a fixture that makes it fail.
3. **In-world scenarios (must for anything touching the world).** A registered scenario prints `PASS`/`FAIL` lines and a `Scenarios: N passed, M failed` summary. Run new worlds, not only worlds that already hold the terrain.
4. **Real pack and visual checks (must, at gates).** The release jar on the pack's dedicated server and client; screenshots for visual features, including with shaders.

**Verification tiers.** T1 every change: build, unit and static tests. T2 lane landing: T1, the lane's scenarios and a short smoke boot. T3 batch of landings: the full headless suite on one world type. T4 gates only: real pack, every world type, shot list, budgets. Lanes run T1 and T2; the integrator runs T3 once per batch (skills `mod-port-budget`, `integration-merge-choreography`).

Builds and Minecraft runs belong on the build host when there is one: [tools/remote/](tools/remote/README.md).

## Assets and localization

- **Provenance (must).** Every asset is original, carried over under a compatible license, or third-party under CC0, CC-BY, MIT or LGPL, with source, author and license recorded in `docs/assets/CREDITS.md`. Never non-commercial, no-derivatives or all-rights-reserved material.
- **Lang keys (must)** for every player-visible string. `en_US.lang` is the source of truth. Namespaces: `tile.<modid>.<name>`, `item.<modid>.<name>`, `<modid>.<area>.<key>`.
- Mojang assets never ship in the jar, and nothing may look official.

## Commits and landing

- **Commits.** Imperative subject, a body that explains why, one logical change per commit. Agents add their `Co-Authored-By` line.
- **Landing.** A small change that meets the definition of done may go straight to the default branch. Parallel or long work happens in a lane (own branch, worktree and build slot) that **never merges the default branch**; one integrator merges finished lanes with `--no-ff`, re-verifies what the merge can break and runs the full suite once per batch (skill `integration-merge-choreography`).
- **Pushing.** The default branch is pushed at roadmap gates and when the owner asks. Release tags need the owner's go-ahead.
- **Nothing machine-specific** in tracked files or commit metadata: no hostnames, usernames, local paths, IPs or secrets (skill `release-and-public-repo`). Remote-host settings live in the git-ignored `.env`.
- **Record of a change:** the commit message or pull request carries the applicable [checklist](docs/templates/pull_request_template.md) items.

## Definition of done

- [ ] Implements its spec sheets, with deviations recorded.
- [ ] `./gradlew spotlessApply build` passes; unit tests cover the new logic and the relevant bug regressions.
- [ ] Static checks pass; ported code passes the ported-code gate.
- [ ] `runServer` boots cleanly and the dev self-check passes.
- [ ] A scenario exists and passes for world-touching features, deterministically and within budget.
- [ ] Every string is a lang key; every asset's provenance is recorded.
- [ ] Javadoc and spec comments in place; no commented-out code.
- [ ] Roadmap box ticked; docs and changelog updated in the same change.
