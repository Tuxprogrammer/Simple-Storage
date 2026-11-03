# Contributing to Simple Storage

Simple Storage backports Tom's Simple Storage to **GT New Horizons (Minecraft 1.7.10)**. It is faithful to the original's functionality, not to its bugs. These rules apply to every change, whether a person or an AI agent makes it. They exist so that the mod feels like part of GTNH rather than an add-on bolted to it, and so that the code stays readable, tested and maintainable.

> [!IMPORTANT]
> These rules use **must** for hard rules and **should** for strong defaults. A change that breaks a *must* does not land. To depart from a *should*, say why in the commit message or pull request.

## Contents

1. [Workflow](#workflow)
2. [Ported code](#ported-code)
3. [Toolchain and build](#toolchain-and-build)
4. [Architecture](#architecture)
5. [Data and sync rules](#data-and-sync-rules)
6. [GTNH integration](#gtnh-integration)
7. [Code standards](#code-standards)
8. [Comments and documentation](#comments-and-documentation)
9. [Testing](#testing)
10. [Assets and localization](#assets-and-localization)
11. [Commits and landing](#commits-and-landing)
12. [Definition of done](#definition-of-done)

---

## Workflow

1. **Start from the roadmap.** Every change belongs to a milestone, spike or decision gate in [docs/ROADMAP.md](docs/ROADMAP.md).
   - Don't start a milestone whose entry gate isn't met.
   - Don't build anything a decision gate blocks until its record in [docs/design/](docs/design/) is `Accepted`.
2. **Read the spec.** Before building something, read its sheets in two places:
   - [docs/original-spec/](docs/original-spec/) for how upstream behaves. Start from the area's `index.md`.
   - [docs/new-spec/](docs/new-spec/) for what changes and how it plugs into the pack.
3. **Check the bug register.** Read the feature's rows in [BUGS.md](docs/original-spec/BUGS.md). Upstream bugs **must not** be reproduced, and where it's practical, a test proves that.
4. **Record deviations.** When this mod deliberately behaves differently from upstream (a 1.7.10 limitation, a GT recipe in place of a modern machine, a bug fix, a GTNH integration), record it in the new-spec area and, if it is a design choice, in a design record. For a small deviation, a `// Deviation:` comment that cites the spec section is enough (see [Comments](#comments-and-documentation)).
5. **Keep changes focused.** One logical change per commit. Parallel work happens on branches in worktrees ([Commits and landing](#commits-and-landing)).

## Ported code

Upstream Tom's Simple Storage is MIT ([0001](docs/design/0001-port-policy-and-licensing.md)), so its code may be ported. A port is a rewrite that starts from Tom's logic, never a copy made for speed. Ported code goes through this gate before it lands (**must**):

- **Pinned source.** Port from the baseline branch `26.2` at `95bb61d` (the local read-only mirror of the upstream repository: read it with `git show 26.2:<path>`, never commit to it), or from an older upstream branch where its era fits 1.7.10 better (the `1.15`–`1.21.x` series exists on GitHub; the local mirror currently tracks only `26.2`). Cite the exact commit either way.
- **A header on every ported file**, directly under the package declaration:

  ```java
  // Ported from Tom's Simple Storage (MIT): https://github.com/tom5454/Toms-Storage/blob/95bb61d/NeoForge/src/platform-shared/java/com/tom/storagemod/inventory/sorting/SortingTypes.java
  // Avoids: SORT-03, STOR-01
  ```

  Omit the `Avoids:` line only when the spec lists no bug in the ported logic. Name each bug by its row ID in [BUGS.md](docs/original-spec/BUGS.md) (the first area's ID, not an alias from its **Also** column). Spotless wraps comments at 120 columns, so the link usually ends up alone on the next `//` line, and a long `Avoids:` list continues on following `//` lines; the gate reads both forms.
- **The notice.** [THIRD-PARTY-NOTICES.md](THIRD-PARTY-NOTICES.md) keeps the upstream MIT notice, and the `credits` field in `mcmod.info` names the upstream mod. This mod's own license stays MIT in `LICENSE`.
- **Our style, not theirs.**
  - Our package layout (`com.tuxprogrammer.simplestorage`), naming, idioms and the Java 8 API.
  - Javadoc on every class and every non-trivial public method, and spec-traceability comments, as for native code ([Comments](#comments-and-documentation)).
  - NeoForge abstractions with no 1.7.10 counterpart (data components, registries, codecs, events, the `platform` layer) become plain Java. They are not emulated.
- **Tests.** Unit tests cover the spec's numbers, and each bug on the `Avoids:` line has a regression test.
- **Every known bug is fixed.** No bug-register entry in the ported logic survives.

Other third-party code: MIT is preferred, and CC0, CC-BY and LGPL are acceptable. LGPL logic is preferably described in the spec in our own words and rebuilt; the result is ours and MIT. LGPL code adapted from its source, rather than rebuilt from a description, stays LGPL. It lives in `com.tuxprogrammer.simplestorage.thirdparty.<project>` with its license text and a notices entry, and passes the same gate. Never unlicensed, NC or All Rights Reserved code. GPL code is not copied in while the mod is MIT. Calling a GPL mod's API or mixing into it is integration and is fine. If an integration can only be done by porting GPL code, the owner has pre-approved relicensing the mod to GPL-3.0, through a design record first.

`PortedCodeGateTest` checks the header, the notice, the Javadoc, the test class and the bug IDs automatically, so CI fails a port that misses any of them. Review checks the rest.

## Toolchain and build

| Item | Rule |
|---|---|
| JDK | 25, as in [`.java-version`](.java-version). Gradle provisions it. |
| Language level | Modern **syntax** only. `enableModernJavaSyntax = jabel` compiles to **Java 8 bytecode, running on the Java 8 API**. Allowed: `var`, switch expressions, text blocks, `instanceof` patterns. **Not allowed:** records, sealed types, and any API newer than Java 8 (`List.of`, `Map.of`, `Optional.isEmpty`, `String.isBlank`, `Stream.toList`…). If unsure, check the Java 8 Javadoc. |
| Formatting | Spotless, using the GTNH shared Eclipse config (blowdryer tag `gtnh.settings.blowdryerTag`, currently `0.2.2`). Run `./gradlew spotlessApply` before every commit; CI rejects unformatted code. |
| Static checks | Checkstyle: no wildcard imports. The static-check tests ([Testing](#testing)) run with the unit tests. |
| Build files | **Never edit** `build.gradle.kts` or `settings.gradle.kts`, which come from the GTNH buildscript. Put dependencies in [`dependencies.gradle`](dependencies.gradle), repositories in [`repositories.gradle`](repositories.gradle), and mod settings in [`gradle.properties`](gradle.properties). |
| Dependencies | GTNH Maven coordinates with the `:dev` classifier, pinned to the versions the target pack ships (design record 0002 in [docs/design/](docs/design/)). Use `compileOnly` for optional integrations, and `devOnlyNonPublishable` for things only needed at runtime in development. Never shade a mod. |

Commands:

```sh
./gradlew spotlessApply build   # format, compile, test, package
./gradlew test                  # unit and static-check tests only
./gradlew runServer             # dev dedicated server (boot smoke test, scenarios)
./gradlew runClient             # dev client
python3 -I tools/semantic-search/index.py --query "..."   # semantic search over this repo and upstream 26.2 (tools/semantic-search/README.md)
```

Builds and Minecraft runs belong on the build host, not on a development laptop: `tools/remote/remote.sh` drives that host, with host and paths coming from the git-ignored `.env` ([tools/remote/](tools/remote/README.md)). Wrap long runs in `tools/agent/quiet-run.sh` and wait on the done-marker rather than tailing logs.

## Architecture

Root package: `com.tuxprogrammer.simplestorage`. Target layout:

| Package | Contents |
|---|---|
| `simplestorage` | `@Mod` class (`SimpleStorage`), `CommonProxy`/`ClientProxy`, `Config`, `Tags` (generated) |
| `simplestorage.block` | Blocks this mod owns: storage and crafting terminals, filing cabinets, open crates, inventory hoppers, connectors and cables |
| `simplestorage.blockentity` | Block containers: each storage's internal inventory, filter and paint state |
| `simplestorage.item` | Items this mod owns |
| `simplestorage.menu` | The 1.7.10 containers (inventories) and slots for the terminal GUIs |
| `simplestorage.screen` | GUI screens and their widgets; client-only |
| `simplestorage.inventory` | The inventory-stack model: slots, stack operations, change tracking. Pure Java, unit-testable without Minecraft |
| `simplestorage.filter` | The filter logic: matching items against filter entries. Pure Java |
| `simplestorage.sort` | The sort and arrangement logic: how a storage orders its contents. Pure Java, deterministic |
| `simplestorage.network` | Channel, packet base classes, packet types |
| `simplestorage.compat` | Optional integrations, one package per mod, each guarded |
| `simplestorage.client` | **All** client-only code, `ClientProxy` included: renderers, painted-block models, screen assets |
| `simplestorage.util` | Small shared helpers |

The layout is a starting point. Milestones refine it in design records, and they never move code across the client, pure-Java and glue boundaries.

Rules:

- **Side safety (must).** Client-only classes live under `client`, and common code never references them, even through imports in code paths that load on the server. Reach the client through the proxy or `@SideOnly(CLIENT)` methods. `ClientIsolationTest` enforces this.
- **Pure logic and Minecraft glue are separate (must).** The filter and sorting algorithms and the inventory-stack logic live in `filter`, `sort` and `inventory`. Those classes take plain data and return plain results; no `World`, `Block`, `ItemStack`, `NBTTagCompound` or `EntityPlayer` appears in their signatures. `blockentity`, `menu`, `screen` and `network` stay thin: they translate Minecraft objects to and from the pure model. A static check enforces the package rule.
- **Modules.** Each feature area (terminals, cabinets, open crates, hoppers, connectors and cables, sorting) has a config flag. A flag never changes what is registered: content always exists, so client/server flag differences and flipped flags cannot break joining or delete blocks. A flag switches behavior and visibility off. Ask one runtime authority (`ModuleState.isActive(id)`) for behavior; never read raw config flags at use sites.
- **No mutable statics** except registries, config and bounded caches that the world lifecycle owns. Per-world state lives in world saved data or block-entity NBT, never in static maps.
- **Registration order** follows the FML lifecycle:
  - `preInit`: config, blocks and items, block entities, network channels;
  - `init`: event handlers, client-side container and screen setup;
  - `postInit`: recipes against the GT recipe maps, cross-mod hooks, the dev self-check.

## Data and sync rules

This mod is a storage mod, so these are the rules that matter most (**must** unless marked).

- **The internal inventory is a documented NBT format.** A storage block's internal inventory is stored in the NBT format documented in its spec area. Readers tolerate missing keys with sensible defaults, and writers stay forward-compatible, so old saves load after upgrades.
- **Store and retrieve are lossless.** A stack that is stored and then retrieved comes back with exactly the item, count and NBT it went in with. No NBT leaks between stacks or between the internal inventory and the item stack itself.
- **Server authoritative.** The server owns the internal inventory and its filter; the client renders from sync. GUI state (selected slot, filter text, sort order) lives server-side: the client sends actions, never state. Sync uses 1.7.10 mechanisms (block-entity description packets, `DataWatcher`, container sync), and per-player or per-entity state lives in NBT or extended properties, never static maps.
- **Determinism where it matters.** Filter and sort output is a pure function of the input inventory and the active filter and sort settings: the same input gives the same arrangement, in the same order, every time.
  - No wall-clock time and no unseeded `Random`.
  - Never iterate a `HashMap` or `HashSet` where the order affects the arrangement.
- **Network.** Every packet is validated before it acts: the target block exists and is the expected type before casting, the player is in the same dimension and within the server's reach, every slot index, enum ordinal and array length is within bounds, and strings have a maximum length. Reject silently or with a debug log; never crash or kick on bad input. A GUI-open request opens only the GUIs valid for that context, never "any ID the client sends".
- **Keys and IDs are forever.** NBT keys, registry names and metadata values are constants, never renamed or reused once a release ships (skill `config-safe-registration`).

## GTNH integration

The mod must feel native to GTNH. These are the coupling rules; [docs/new-spec/](docs/new-spec/) decides what each one means for each feature.

> [!NOTE]
> GT5-Unofficial renamed its API classes in 2024 (for example `GT_Values` became `GTValues`). **Always check names against the GT5-Unofficial version pinned in `dependencies.gradle`.**

**Researching external APIs (must)**
- These rules cover code that touches GT, NEI or any other pack mod. The extracted pack and its mod index live in the git-ignored `reference/` folder (design record 0002 in [docs/design/](docs/design/)).
- **Source first.** Read the mod's source at the tag the pack ships.
- **Decompile only as a fallback**, for mods or internals with no available source. Never paste decompiled code.
- **Cite it.**
  - Put a `// Source:` permalink, pinned to the tag (or the commit), where code relies on another mod's behavior.
  - Put a `// Decompiled:` hedge comment wherever decompiled bytecode was the evidence.

**Materials and parts**
- Never register an ore, ingot, plate, rod, screw, wire, circuit or fluid that GT provides. Get them by ore dictionary (`GTOreDictUnificator.get(OrePrefixes.plate, Materials.Steel, 1)`), using GT Materials where the pack has them.
- The storage's own components are our own items, but they are *made from* GT parts.

**Recipes and tiers**
- Machine processing goes through GT recipe maps with the builder, at the EU/t of the item's tier.
- Give each recipe a free circuit number, and test against the **pack's full recipe set** (dump recipes from a real-pack boot), not just GT's defaults.
- No recipe may let an item be obtained before its tier. Every item has exactly one GT voltage tier, set by the tier map (design record 0004 in [docs/design/](docs/design/)).
- Crafting-table recipes follow GTNH conventions: GT tools in the grid, GT parts as ingredients.

**Energy**
- EU only. Electric blocks use the IC2 electric API the way GT's do, so GT chargers and battery buffers work with them; per-tier charging rules are settled in the design records.

**The recipe viewer**
- NEI shows GT recipe maps automatically; add handlers only for non-GT logic, such as the storage's internal-inventory view if the spec calls for one. Hide unobtainable and technical items.
- NEI's default sort follows numeric item IDs, which vary per world in 1.7.10; if the items' order matters, supply an explicit order.

**Optional mods**
- Guard with `Loader.isModLoaded` or `@Optional`, and keep the code under `compat`.
- Hard dependencies are declared in `@Mod(dependencies=…)` and `mcmod.info`.

**Pack fit**
- Names, tooltips and textures follow GTNH style.
- Before adding a mechanic, check that the pack doesn't already provide it.

## Code standards

- **Readable first.** Use clear names: no abbreviations beyond common Minecraft ones (`te`, `nbt`, `pos`). Keep methods small, prefer early returns, and use one class per concept. `final` where sensible: fields, parameters and locals that are never reassigned.
- **No magic numbers.** Use named constants, or config for anything a server owner might tune. Spec numbers become constants named after what they mean, with the spec section in their Javadoc.
- **No star imports.** Checkstyle enforces it.
- **Javadoc** on every class and every non-trivial public method ([Comments](#comments-and-documentation)).
- **No commented-out code** in commits. Delete it; git keeps history.
- **Validate everything the client sends (must).** Packet handlers check that the player may act (same dimension, within the server's reach, GUI actually open for GUI actions), and check every index and enum against its bounds.
- **Server authority.** The server decides inventory contents, filter and sort state, and item consumption. The client never mutates them.
- **No swallowed exceptions.** Never `catch (Exception e) {}`. Catch the narrowest type, then handle it or log it with the mod logger. Never use `System.out`.
- **Null safety.** Check stacks, NBT and registry lookups. Use one helper rather than repeated checks.
- **NBT and saved data.** Keys are constants. Readers tolerate missing keys with sensible defaults.
- **Thread discipline.** Don't touch world or inventory state from network threads.
- **American English** in code and docs (`color`, `armor`, `gray`). External API identifiers keep their own spelling.
- **A lang key for every user-visible string:** names, tooltips, chat, GUI text and command output; no hard-coded English ([Assets and localization](#assets-and-localization)).

## Comments and documentation

- **Javadoc (must)** on every public class and every public method that isn't trivial. Say what it does, what each parameter means, its units (ticks, slots, items) and its side (client, server or both).
- **Comments explain why**, not what. Explain constraints, 1.7.10 limitations, pack conventions and non-obvious math. Don't narrate the obvious.
- **Spec traceability.** A class that implements a spec sheet cites it at the top: `// Spec: docs/original-spec/<area>/<sheet>.md`.
- **Deviations.** When behavior intentionally differs from upstream, say so where it happens: `// Deviation: connectors route through GT's cable logic (new-spec 03-connectors).` or `// Avoids STOR-01: stored stacks no longer drop their NBT.`
- **Ported code** carries the `Ported from` header and the `Avoids:` lines from [Ported code](#ported-code).
- **External behavior.** Where code depends on how another mod behaves, cite it with `// Source: <pinned permalink>`, or `// Decompiled: <jar, pack version, member>; inferred from decompiled bytecode and may be wrong`.
- **Docs state the current truth.** Design records, specs and the roadmap say what is true now. When a decision changes, rewrite the text in place; git keeps the history. Player-facing text (README, guides, lang, tooltips, config comments) never tells players what changed between versions; that belongs only in the changelog.
- **Fix stale docs on sight.** A broken link, stale statement or wrong fact found in any doc is fixed in the change that found it, not left for later.

## Testing

Each layer catches different bugs, and features need every layer that applies to them. Testing guidance lives in the `testing-gtnh-mods` skill; these are the rules.

**Verification tiers.**

| Tier | When | What |
|---|---|---|
| T1 | every change | build, unit and static tests |
| T2 | lane landing | T1 + that lane's scenarios + a 2-minute smoke boot |
| T3 | batch of landings | full headless suite, one world type |
| T4 | gates only (alpha, beta, release) | real pack, every world type, shot list, budgets |

Lanes run T1 and T2 only; the integrator runs T3 once per batch.

### 1. Unit tests: must

- **Framework:** JUnit 5 in `src/test/java`, mirroring the main package structure; run with `./gradlew test`.
- **What they cover:**
  - every formula and decision in `filter`, `sort` and `inventory`: filter matching, sort and arrangement, slot and stack operations;
  - **inventory round-trips:** storing and retrieving a stack preserves it exactly, NBT included;
  - **filter and sort golden tests:** deterministic output for fixed inputs — the same inventory and settings give the same arrangement, in the same order, every run;
  - **NBT format tests:** the internal inventory format round-trips, and each historic format it must still read (the upgrade paths) loads;
  - config and module-flag defaults, and packet encode/decode round-trips.
- **Bug regressions:** each upstream bug a feature could reproduce gets a regression test named for it, for example `doesNotReproduceSTOR01_storedStackLosesNBT`.
- **Speed:** tests are deterministic (seeded randomness, no wall-clock time) and fast.

### 2. Static checks: must

Tests that read the compiled classes and resources and fail the build:
- `ClientIsolationTest`: no common class references a client class;
- `Java8ApiTest`: no API newer than Java 8;
- `PortedCodeGateTest`: the [ported-code gate](#ported-code);
- `LangKeysTest`: every player-visible string is a lang key, and every lang key, texture and sound referenced in code exists in resources.

Each static check is proven by a fixture that makes it fail, so none can pass because it looked at nothing. `EventHandlerVisibilityTest` (every `@SubscribeEvent` handler and its class are public) runs with them.

### 3. In-world tests: must, for anything that touches a block, a GUI or another mod

- **Server boot smoke test:** `./gradlew runServer` starts with the pack dependencies loaded and no errors or warnings from this mod, with every module off and with every module on.
- **Dev self-check:** in the dev environment, a `postInit` check that every registered thing has a lang key and a texture, every recipe resolves, the tier map holds, and the NBT format round-trips. A failure stops the dev server.
- **Scenarios:** `/simplestoragenh test <name|all|list>` (op only; dev only, or `-Dsimplestoragenh.testHooks=true` in a release jar) runs registered scenarios on the server and reports `PASS`/`FAIL` lines and a `Scenarios: N passed, M failed` summary to the log and the sender's chat. Storage scenarios exercise store/retrieve through a terminal, filter behavior, sort arrangements and connector routing between two storages. Each feature adds its scenario, listed in `docs/testing/`.
- **Multiplayer:** for anything synced, two clients on a dedicated server: the second client sees the same inventory contents, GUI state matches, the server wins on conflict, and nothing desyncs after a relog.

### 4. Real-pack and visual checks: must, at the gates

- **Real pack:** the release jar on a GTNH dedicated server and client, at the alpha and beta gates.
- **Visual:** `runClient` and real-pack screenshots of the blocks and GUIs, including with shaders where rendering is involved. Visual changes keep their screenshots with the change's evidence.

Builds and Minecraft runs happen on the build host, not on a development laptop ([tools/remote/](tools/remote/README.md)); wrap them in `tools/agent/quiet-run.sh` and read the done-marker, never the whole log.

## Assets and localization

- **Provenance (must).** Every asset is our own work, upstream MIT material carried over ([0001](docs/design/0001-port-policy-and-licensing.md)), or third-party material under CC0, CC-BY, MIT or LGPL. Its source, author and license are recorded in `docs/assets/CREDITS.md`. Never anything NC, ND, share-alike-only or All Rights Reserved (skill `clean-room-reimplementation`).
- **MIT for our work (must).** By contributing, you release your contribution under the MIT License.
- **Converted upstream assets** keep their upstream file name and structure where the format allows, so a later upstream re-pin can be diffed against them.
- **Minecraft content.** The rule is to never break [Mojang's usage guidelines](https://www.minecraft.net/en-us/usage-guidelines), not to avoid Mojang content. Mojang assets never ship in the jar, and nothing may look official.
- **Lang keys (must) for every player-visible string:** names, tooltips, chat, GUI text and command output.
  - `en_US.lang` is the source of truth.
  - No hard-coded English.
  - Use the key namespace `tile.simplestoragenh.<name>`, `item.simplestoragenh.<name>`, `simplestoragenh.<area>.<key>`.
- **American English** in code, docs and lang (`color`, `armor`, `gray`). External identifiers keep their own spelling.
- **No machine-specific files** in git: no hostnames, usernames or local paths in tracked assets or their metadata.

## Commits and landing

- **Commits.** Use an imperative subject (`Port the storage terminal inventory`), a body explaining why, and one logical change per commit. Agents add their `Co-Authored-By` line.
- **Landing.** A change that meets the [definition of done](#definition-of-done) may be committed straight to `master`. Parallel or long work happens in a lane: its own branch in a git worktree and its own build slot, named for the milestone. A lane **never merges master**; it verifies its own change (T1 and T2). One integrator merges finished lanes in a queue with `--no-ff`, re-verifies what the merge can break, and runs T3 once per batch (skills `port-workflow`, `integration-merge-choreography`).
- **Pushing.** `master` is pushed at roadmap gates and when the owner asks. Release tags need the owner's go-ahead.
- **Nothing machine-specific** in tracked files or commit metadata: no hostnames, usernames, local paths, IPs or secrets (skill `release-and-public-repo`). Remote-host settings live in the git-ignored `.env`.
- **Record of a change:** the commit message, or the pull request when one is used, carries the [checklist](docs/templates/pull_request_template.md) items that apply: roadmap item, spec sheets, deviations, inherited bugs avoided, test evidence.

## Definition of done

A change is done when all of these hold:

- [ ] It implements its spec sheets, with any deviations recorded.
- [ ] `./gradlew spotlessApply build` passes, and unit tests cover the new logic and the relevant BUGS.md regressions.
- [ ] Static checks pass, and ported code passes the [ported-code gate](#ported-code).
- [ ] `runServer` boots cleanly, and the dev self-check passes.
- [ ] A scenario exists for features that touch a block or a GUI, and it passes; multiplayer is checked for synced features.
- [ ] Tier-gated recipes are visible in the recipe viewer (NEI), and no GT material is duplicated.
- [ ] Every string is a lang key, and every asset's provenance is recorded.
- [ ] Javadoc and `// Spec:` comments are in place. There is no commented-out code.
- [ ] The roadmap box is ticked, and docs are updated in the same change.
