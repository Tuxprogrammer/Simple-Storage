# Agent guide

<!-- TEMPLATE. Replace the placeholders, delete rules that do not apply, and keep it a short map: it is loaded in every session and every subagent. -->

<Mod Name> is a Forge 1.7.10 mod built on the GT New Horizons buildscript, meant to live inside the GTNH pack. <One sentence: a rewrite, a port of <upstream mod>, or a pack integration.> This file is a map: load the skill or document you need, not everything up front.

Skills live in [.claude/skills/](.claude/skills/). Claude Code loads one automatically when its description matches the task; you can also ask for one by name. Agent definitions, hooks and suggested settings are described in [.claude/README.md](.claude/README.md).

## Non-negotiables

1. **Never copy code or assets you have no license for.** Rebuild from a written behavior spec in a clean room, or port under the license's terms with the gate in CONTRIBUTING. → `clean-room-reimplementation`
2. **Faithful to functionality, not bugs.** Behavior follows the spec; nothing in the bug register comes back. → `behavior-spec`
3. **GT and the pack first.** Use GT materials, recipe maps, voltage tiers and EU; never duplicate what the pack provides. → `gt-first-design`
4. **Source before guesses.** Read another mod's source at the tag the pack ships before integrating with it; cite it. → `gtnh-integration-research`
5. **Config never changes what is registered.** Register everything; gate behavior; IDs are forever. → `config-safe-registration`
6. **Servers never load client classes, and never trust clients.** → `networking-side-safety`
7. **Java 8 API.** `enableModernJavaSyntax = jabel` allows modern syntax, but the runtime is Java 8: no records, no `List.of`, no `String.isBlank`.
8. **Tests and comments ship with the code.** → `testing-gtnh-mods`
9. **Never edit `build.gradle.kts` or `settings.gradle.kts`.** They come from the GTNH buildscript. Use `dependencies.gradle`, `repositories.gradle`, `gradle.properties`.
10. **Nothing machine-specific in git:** no hostnames, usernames, local paths, IPs or secrets. Remote-host settings live in the git-ignored `.env`. → `release-and-public-repo`

## Budget (binding for autonomous work)

11. **Agents are short.** One deliverable per agent; hand off at 200k context or 120 calls. Only the orchestrator spawns agents. → `agent-context-budget`, `orchestrating-parallel-agents`
12. **Model plan.** A mid-tier model at medium effort for research, catalogs, spec writing, audits, applying decisions, integration and shot review; the top model at high effort for design and hard code. Pass `model` on every spawn.
13. **Verification tiers.** Lanes run T1 and T2; the integrator runs T3 once per batch; T4 (real pack, every world type, shot list, budgets) at gates only. At most 2 rounds per brief, then report the gap with numbers. → `mod-port-budget`
14. **Lanes never merge the default branch.** One integrator lands lanes in a queue. → `integration-merge-choreography`
15. **Remote runs are quiet.** Read `run.done` and `grep`, never whole logs; wait with one Monitor on the done-marker; give each lane its own slot and instance. → `waiting-on-remote-jobs`, `quiet-tool-output`, `remote-build-runner`
16. **Docs are read by map.** CLAUDE.md is already loaded; never re-read it. Read CONTRIBUTING by section (`grep -n '^## '`). `grep` the decision log, never `cat` it. Read one roadmap milestone by anchor. Screenshots: contact sheet first. → `cheap-screenshot-review`

## Skills

| Skill | Use it when |
|---|---|
| [clean-room-reimplementation](.claude/skills/clean-room-reimplementation/SKILL.md) | Rebuilding another mod's behavior or look; license triage; ported code |
| [behavior-spec](.claude/skills/behavior-spec/SKILL.md) | Documenting the original; bug register; deviations |
| [gtnh-integration-research](.claude/skills/gtnh-integration-research/SKILL.md) | Any code touching GT or another pack mod; dependency bumps |
| [gt-first-design](.claude/skills/gt-first-design/SKILL.md) | Materials, recipes, tiers, energy, machines, multiblocks, key defaults |
| [config-safe-registration](.claude/skills/config-safe-registration/SKILL.md) | Module toggles, registration, retiring content, NBT formats |
| [testing-gtnh-mods](.claude/skills/testing-gtnh-mods/SKILL.md) | Unit, static, headless, scenario, real-pack, worldgen and client tests |
| [client-rendering-shaders](.claude/skills/client-rendering-shaders/SKILL.md) | Renderers, effects, models; anything that breaks under shaders |
| [networking-side-safety](.claude/skills/networking-side-safety/SKILL.md) | Packets, sync, GUIs, tooltips, server crashes on client classes |
| [in-pack-content-delivery](.claude/skills/in-pack-content-delivery/SKILL.md) | Quests, loot, pack configs; shipping from the jar |
| [release-and-public-repo](.claude/skills/release-and-public-repo/SKILL.md) | Tags, changelogs, upgrade notes, going public, machine-specific details |
| [pack-contribution-policy](.claude/skills/pack-contribution-policy/SKILL.md) | Before planning any submission to a pack or another mod |
| [port-workflow](.claude/skills/port-workflow/SKILL.md) | Roadmap and gates, decision records, definition of done |
| [mod-port-budget](.claude/skills/mod-port-budget/SKILL.md) | Budgets, verification tiers T1-T4, documentation depth |
| [orchestrating-parallel-agents](.claude/skills/orchestrating-parallel-agents/SKILL.md) | Spawning and briefing agents; models, stop rules, round limits |
| [agent-context-budget](.claude/skills/agent-context-budget/SKILL.md) | Long agent sessions; handoff files |
| [integration-merge-choreography](.claude/skills/integration-merge-choreography/SKILL.md) | Landing lanes; the single integrator; shared test resources |
| [remote-build-runner](.claude/skills/remote-build-runner/SKILL.md) | Builds and Minecraft runs on a build host; slots, instances |
| [waiting-on-remote-jobs](.claude/skills/waiting-on-remote-jobs/SKILL.md) | Any job longer than a minute; markers and Monitor |
| [quiet-tool-output](.claude/skills/quiet-tool-output/SKILL.md) | Before commands or reads with large output |
| [cheap-screenshot-review](.claude/skills/cheap-screenshot-review/SKILL.md) | Before opening screenshots |

## Where to look

| You need... | Read |
|---|---|
| Where something is specified or implemented, when you do not know its name | The `semantic_search` tool if configured ([tools/semantic-search/](tools/semantic-search/README.md)); otherwise grep |
| The binding rules: workflow, porting, architecture, testing, landing | CONTRIBUTING.md (start from [CONTRIBUTING.template.md](CONTRIBUTING.template.md)), by section |
| What to build next, and its gates | `docs/ROADMAP.md` (template: [roadmap.md](docs/templates/roadmap.md)) |
| Accepted design decisions | `docs/design/` (template: [decision-record.md](docs/templates/decision-record.md)) |
| How the original behaves | `docs/original-spec/` (templates: [spec-area-index.md](docs/templates/spec-area-index.md), [spec-sheet.md](docs/templates/spec-sheet.md), [bug-register.md](docs/templates/bug-register.md)) |
| Changes the pack would need | `docs/PACK-CHANGES.md` (template: [pack-changes.md](docs/templates/pack-changes.md)) |
| Briefs for spawning agents | [docs/agent-setup/briefs/](docs/agent-setup/briefs/) |
| Change checklist | [docs/templates/pull_request_template.md](docs/templates/pull_request_template.md) |
| Build settings, mod ID, package | [gradle.properties](gradle.properties) |
| Dependencies and repositories | [dependencies.gradle](dependencies.gradle), [repositories.gradle](repositories.gradle) |
| Buildscript FAQ, migrating or porting a build | [docs/FAQ.md](docs/FAQ.md), [docs/migration.md](docs/migration.md), [docs/porting.md](docs/porting.md) |
| Tools | [tools/agent/](tools/agent/README.md), [tools/remote/](tools/remote/README.md), [tools/semantic-search/](tools/semantic-search/README.md), [tools/bugs-coverage/](tools/bugs-coverage/README.md), [tools/gtnh-reference/](tools/gtnh-reference/README.md) |

## Commands

```sh
./gradlew spotlessApply build   # format, compile, test: run before every commit
./gradlew test                  # unit and static-check tests
./gradlew runServer             # headless boot smoke test (side safety, scenarios)
./gradlew runClient             # visual checks
tools/remote/remote.sh build <slot> HEAD   # the same on the build host (see tools/remote/README.md)
```

Builds and Minecraft runs belong on the build host when the project has one. Wrap them in `tools/agent/quiet-run.sh`.

## Housekeeping

- Changes that meet the definition of done may land straight on the default branch; parallel work uses lanes and the integrator.
- When behavior changes, update the spec deviation notes or design records in the same change. Docs state the current truth; git keeps the history.
- Keep reference packs, other mods' clones and decompiled output in a git-ignored `reference/` folder.
- American English in code, docs and lang (`color`, `armor`, `gray`); keep external API identifiers as they are.
