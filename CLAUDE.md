# Agent guide

Simple Storage backports [Tom's Simple Storage](https://github.com/tom5454/Toms-Storage) (MIT, upstream branch `26.2` at `95bb61d`) to **GT New Horizons 2.9.0 (Minecraft 1.7.10, Forge 10.13.4.1614)**. It is faithful to the original's functionality, not its bugs: a storage network — the storage blocks and items, the terminal with its filters and sorting, and the wireless access — rebuilt on GT materials and recipe maps, with a GTNH layer on top. This file is a map: load the skill or document you need, not everything up front.

Skills live in [.claude/skills/](.claude/skills/); agent definitions in [.claude/agents/](.claude/agents/), guard hooks in [.claude/hooks/](.claude/hooks/) (see [.claude/README.md](.claude/README.md)), brief templates in [docs/agent-setup/](docs/agent-setup/README.md), and quiet-run, wait and contact-sheet helpers in [tools/agent/](tools/agent/README.md) with the build-host runner in [tools/remote/](tools/remote/README.md). Claude Code loads one automatically when its description matches the task; you can also ask for one by name.

## Non-negotiables

1. **Ported code meets the bar before it lands.** Upstream is MIT, so its code may be ported, but a port is a rewrite in our style, with Javadoc, tests, a provenance header and every known upstream bug fixed. See [CONTRIBUTING § Ported code](CONTRIBUTING.md#ported-code) and [0001](docs/design/0001-port-policy-and-licensing.md).
2. **Faithful to functionality, not bugs.** Behavior follows [docs/original-spec/](docs/original-spec/README.md); nothing in [BUGS.md](docs/original-spec/BUGS.md) comes back.
3. **The internal inventory is server-authoritative, with a documented NBT format.** Store-and-retrieve is lossless; readers tolerate missing keys and writers stay forward-compatible, so old saves load after upgrades. → `config-safe-registration`, [CONTRIBUTING § Data and sync rules](CONTRIBUTING.md#data-and-sync-rules)
4. **GT and the pack first.** Storage content lives in the GT recipe maps on GT materials, in the tier band [0003](docs/design/0003-gtnh-layer-and-tier-ceiling.md) sets; the terminal and storage blocks are power-free utility blocks like upstream, not GT machines ([0004](docs/design/0004-energy-and-machine-model.md)); never duplicate what the pack provides. → `gt-first-design`
5. **Source before guesses.** Read another mod's source at the tag the pack ships before integrating with it; cite it. → `gtnh-integration-research`
6. **Config never changes what is registered.** Register everything; gate behavior; IDs are forever. → `config-safe-registration`
7. **Servers never load client classes, and never trust clients.** → `networking-side-safety`
8. **Java 8 API.** `enableModernJavaSyntax = jabel` allows modern syntax, but the runtime is Java 8: no records, no `List.of`, no `String.isBlank`.
9. **Tests and comments ship with the code.** → `testing-gtnh-mods`, [CONTRIBUTING § Testing](CONTRIBUTING.md#testing)
10. **Never edit `build.gradle.kts` or `settings.gradle.kts`.** They come from the GTNH buildscript. Use `dependencies.gradle`, `repositories.gradle`, `gradle.properties`.
11. **Nothing machine-specific in git:** no hostnames, usernames, local paths, IPs or secrets. Remote-host settings live in the git-ignored `.env`. → `release-and-public-repo`
12. **Spend tokens like they're scarce.** Subagents default to a mid-tier model at medium effort ([.claude/agents/](.claude/agents/) sets the model and effort); use the top model only for hard design or code, and say why. Every brief has a hard stop (report by ~120 calls or ~200k context) and at most two verification rounds; lanes never merge `master`, the orchestrator integrates once. Wait on long jobs with one capped done-marker loop. → `orchestrating-parallel-agents`, `agent-context-budget`, `mod-port-budget`

## Skills

**Load the matching skill before you start, not after something goes wrong.** Skills hold the repo's hard-won rules, and ignoring them is how the same mistakes come back. At the start of a task, scan the table below and load every skill whose "Load it when" fits (the Skill tool, or name it); when the task changes, scan again. Sessions past ~60 calls, and anything that spawns or supervises agents, always load the budget skills. Loading is cheap compared with redoing work.

| Skill | Purpose | Load it when |
|---|---|---|
| [behavior-spec](.claude/skills/behavior-spec/SKILL.md) | How to write the upstream behavior spec and keep the bug register; the sheet layout and the `doesNotReproduce<ID>` rule | Documenting upstream, adding or reviewing a spec sheet or new-spec area, logging a suspected bug, building a feature from a sheet |
| [clean-room-reimplementation](.claude/skills/clean-room-reimplementation/SKILL.md) | License triage and the port rules: what may enter the repo and how a port must be rewritten | Any code or art that does not come from upstream Tom's Storage; porting MIT code |
| [gtnh-integration-research](.claude/skills/gtnh-integration-research/SKILL.md) | Source-first research on another mod at the exact pack tag, with citations; dependency pinning | Any code touching GT, NEI, Waila, AE2, GTNHLib or another pack mod; bumping a dependency; writing a doc claim about another mod |
| [gt-first-design](.claude/skills/gt-first-design/SKILL.md) | Re-home content onto GT materials, recipe maps, tiers and EU; deconflict with the pack; key defaults | Materials, recipes, tier map, energy, machines, multiblocks, key bindings |
| [config-safe-registration](.claude/skills/config-safe-registration/SKILL.md) | Register always and gate behavior; IDs are forever; NBT tolerates the past | Module toggles, registering items/blocks/TEs, retiring content, NBT or saved-data formats, config keys |
| [testing-gtnh-mods](.claude/skills/testing-gtnh-mods/SKILL.md) | The five test layers, scenarios, real-pack runs, flaky-test handling | Writing or running tests; a test hangs, flakes or passes in dev but fails in the pack |
| [client-rendering-shaders](.claude/skills/client-rendering-shaders/SKILL.md) | 1.7.10 renderers and effects that survive shader packs and Angelica | Renderers, models, particles, HUD; anything that breaks only under shaders |
| [networking-side-safety](.claude/skills/networking-side-safety/SKILL.md) | Packets, sync and GUIs without loading client classes on the server or trusting clients | Packets, GUIs with buttons, keybind actions, synced state, a `ClassNotFound` for a client class on a server |
| [in-pack-content-delivery](.claude/skills/in-pack-content-delivery/SKILL.md) | Getting quests, loot and pack configs into the pack from the jar | Quests, loot tables, NEI groups, other mods' configs, deciding jar versus pack |
| [release-and-public-repo](.claude/skills/release-and-public-repo/SKILL.md) | Tags, changelogs, upgrade notes, and keeping machine-specific data out of git | Cutting a release, writing a changelog, going public, and before committing anything in tool or script folders |
| [port-workflow](.claude/skills/port-workflow/SKILL.md) | Roadmap gates, decision records, definition of done, landing | Planning milestones, writing or changing a decision record, deciding whether a change is done |
| [pack-contribution-policy](.claude/skills/pack-contribution-policy/SKILL.md) | Pack and mod contribution, AI and content policies | Before planning or promising any submission to a pack or another mod |
| [mod-port-budget](.claude/skills/mod-port-budget/SKILL.md) | Token and call budgets, verification tiers T1 to T4, doc depth | Planning a run by agents; a milestone is over budget; deciding how deep a doc should be |
| [orchestrating-parallel-agents](.claude/skills/orchestrating-parallel-agents/SKILL.md) | Scope, model, brief, stop rules and round limits for subagents | Spawning, briefing or supervising any agent |
| [agent-context-budget](.claude/skills/agent-context-budget/SKILL.md) | Context-size limits and handoff files | A session past ~60 calls or ~150k context; handing off or restarting |
| [integration-merge-choreography](.claude/skills/integration-merge-choreography/SKILL.md) | One integrator lands lanes on `master`; when to re-verify | Landing or merging lanes; scheduling shared test resources |
| [remote-build-runner](.claude/skills/remote-build-runner/SKILL.md) | Slots, instances and runs on the build host | Builds, servers or clients on the build host |
| [waiting-on-remote-jobs](.claude/skills/waiting-on-remote-jobs/SKILL.md) | Done markers and one capped wait instead of polling | Any job longer than about a minute: builds, servers, clients, benchmarks |
| [quiet-tool-output](.claude/skills/quiet-tool-output/SKILL.md) | Keep command and file output small | Before commands or reads that may be large: builds, logs, diffs, big docs |
| [cheap-screenshot-review](.claude/skills/cheap-screenshot-review/SKILL.md) | Review screenshots as contact sheets first | Looking at screenshots or shot-list output |

## Where to look

| You need… | Read |
|---|---|
| Where anything is, level by level (map -> area -> sheet) | [docs/INDEX.md](docs/INDEX.md), then the area `index.md` (each at most 40 lines) |
| Where something is specified, decided or implemented, here or upstream, when you don't know its name | The `semantic_search` MCP tool ([tools/semantic-search/](tools/semantic-search/README.md)); grep or `git grep <pattern> 26.2` for exact names |
| Upstream code and assets | The local `26.2` branch, pinned at `95bb61d`: `git show 26.2:<path>`. Never commit to it except to fast-forward from upstream |
| The binding rules: workflow, porting, architecture, data and sync, GTNH integration, code, tests, assets, landing | [CONTRIBUTING.md](CONTRIBUTING.md), by section (start from [CONTRIBUTING.template.md](CONTRIBUTING.template.md)) |
| What to build next, and its gates | [docs/ROADMAP.md](docs/ROADMAP.md) (template: [roadmap.md](docs/templates/roadmap.md)) |
| Accepted design decisions | [docs/design/](docs/design/README.md) and the [decision log](docs/design/decision-log.md) (template: [decision-record.md](docs/templates/decision-record.md)) |
| How upstream behaves | [docs/original-spec/](docs/original-spec/README.md) (layout: [CONVENTIONS.md](docs/original-spec/CONVENTIONS.md)) |
| Upstream bugs not to reproduce | [docs/original-spec/BUGS.md](docs/original-spec/BUGS.md) (template: [bug-register.md](docs/templates/bug-register.md)) |
| How this mod differs and fits the pack | [docs/new-spec/](docs/new-spec/README.md), and what is undecided in [OPEN-QUESTIONS.md](docs/new-spec/OPEN-QUESTIONS.md) |
| Changes the pack would need | [docs/PACK-CHANGES.md](docs/PACK-CHANGES.md) (template: [pack-changes.md](docs/templates/pack-changes.md)) |
| Briefs for spawning agents | [docs/agent-setup/briefs/](docs/agent-setup/briefs/) |
| Templates: decision record, spec sheet, area index, bug register, roadmap, pack changes, change checklist | [docs/templates/](docs/templates/) |
| Build settings, mod ID, package | [gradle.properties](gradle.properties) |
| Dependencies and repositories | [dependencies.gradle](dependencies.gradle), [repositories.gradle](repositories.gradle) |
| Buildscript FAQ, migrating or porting a build | The GTNH buildscript's own docs: [FAQ](https://github.com/GTNewHorizons/ExampleMod1.7.10/blob/master/docs/FAQ.md), [migration](https://github.com/GTNewHorizons/ExampleMod1.7.10/blob/master/docs/migration.md), [porting](https://github.com/GTNewHorizons/ExampleMod1.7.10/blob/master/docs/porting.md) |
| Tools | [tools/agent/](tools/agent/README.md), [tools/remote/](tools/remote/README.md), [tools/semantic-search/](tools/semantic-search/README.md), [tools/bugs-coverage/](tools/bugs-coverage/README.md), [tools/gtnh-reference/](tools/gtnh-reference/README.md) |

## Commands

```sh
./gradlew spotlessApply build   # format, compile, test: run before every commit
./gradlew test                  # unit and static-check tests
./gradlew runServer             # headless boot smoke test (side safety, scenarios)
./gradlew runClient             # visual checks
python3 -I tools/semantic-search/index.py --query "..."   # semantic search from the shell
```

Builds and Minecraft runs belong on the build host, not a development laptop. Drive it with [tools/remote/remote.sh](tools/remote/README.md) (`build <slot> <rev>`, `devserver`, `realpack`, `client`); host and paths come from the git-ignored `.env`. Wrap the gradle commands in `tools/agent/quiet-run.sh` and wait on `run.done` (skill `waiting-on-remote-jobs`).

## Housekeeping

- Changes that meet the definition of done may land straight on `master`; parallel work uses lanes and the single integrator.
- When behavior changes, update the spec deviation notes or design records in the same change. Docs state the current truth; git keeps the history.
- Keep reference packs, other mods' clones and decompiled output in the git-ignored `reference/` folder.
- American English in code, docs and lang (`color`, `armor`, `gray`); keep external API identifiers as they are.
