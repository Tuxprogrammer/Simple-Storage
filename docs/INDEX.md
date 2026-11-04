# Docs index

This is level 1: the map. Every area `index.md` is level 2 (a map of at most 40 lines); individual sheets are level 3.

| Grouping | Start at | What it holds |
|---|---|---|
| **Entry** | | |
| | [../CLAUDE.md](../CLAUDE.md) | Agent guide: rules, skills table, commands (already in context) |
| | [../CONTRIBUTING.md](../CONTRIBUTING.md) | Binding rules: workflow, porting, architecture, data and sync, code, tests, landing, definition of done |
| | [ROADMAP.md](ROADMAP.md) | Milestones M01-M06, spikes S1-S4, entry and exit gates, alpha and beta gates (read one milestone by heading) |
| **Decisions** | | |
| | [design/README.md](design/README.md) | Design records 0001-0004: port policy and licensing, target pack, GT layer and tier ceiling, energy and machine model |
| | [design/decision-log.md](design/decision-log.md) | One-row-per-decision log D1-D16 (grep it, never cat it) |
| | [new-spec/OPEN-QUESTIONS.md](new-spec/OPEN-QUESTIONS.md) | Open and settled questions Q1-Q9, plus 36 area proposals with recommendation and settler |
| **How upstream behaves** (Tom's Simple Storage 2.11.3 at 26.2 @95bb61d) | | |
| | [original-spec/README.md](original-spec/README.md) | How to read the spec; CONVENTIONS.md sheet layout and bug-ID rules |
| | [01-core](original-spec/01-core/index.md), [02-storage](original-spec/02-storage/index.md), [03-terminal](original-spec/03-terminal/index.md), [04-wireless](original-spec/04-wireless/index.md), [05-integration](original-spec/05-integration/index.md), [06-client-ui](original-spec/06-client-ui/index.md) | Area indexes for core (11 sheets, CORE), storage (14, STOR), terminal (14, TERM), wireless (2, WIRE), integration (5, INT), client UI (5, UI) |
| | [original-spec/BUGS.md](original-spec/BUGS.md), [original-spec/COVERAGE.md](original-spec/COVERAGE.md) | Bug register (52 IDs) with BUGS-COVERAGE.md test mapping; every upstream Java file mapped to a sheet, API-MAP.md maps NeoForge/Fabric APIs to 1.7.10 |
| **What this mod changes** (GTNH 2.9.0) | | |
| | [new-spec/README.md](new-spec/README.md) | How to read the new spec, status words |
| | [00-pack](new-spec/00-pack-integration/index.md), [01-core](new-spec/01-core/index.md), [02-storage](new-spec/02-storage/index.md), [03-terminal](new-spec/03-terminal/index.md), [04-wireless](new-spec/04-wireless/index.md), [05-integration](new-spec/05-integration/index.md), [06-client-ui](new-spec/06-client-ui/index.md) | Deviations per area; 00-pack holds the tier map, recipes, machine model, NEI/Waila, energy; 01-core also maps upstream config keys to 1.7.10 keys |
| | [PACK-CHANGES.md](PACK-CHANGES.md) | Everything needed outside the jar: quests, loot, config keys table |
| **Process and tests** | | |
| | [testing/README.md](testing/README.md) | Test tiers T1-T4, five static checks, bug regressions, definition of done |
| | [templates/](templates/) | Templates: spec sheet, area index, decision record, bug register, roadmap, pack changes, PR |
| | [agent-setup/README.md](agent-setup/README.md) | Brief templates for spawning agents |

| If you need... | Go to |
|---|---|
| What does upstream do for X | The original-spec area, then its index.md sheet table, then the sheet |
| What does our mod do differently | new-spec area index, then deviations.md; undecided items in proposals.md |
| Why was it decided | design/decision-log.md (grep the D-id), then the numbered record |
| What is next / is it done | ROADMAP.md milestone heading |
| Which bug IDs apply | original-spec/BUGS.md, then the area's bugs.md |
| What test proves it | testing/README.md, then original-spec/BUGS-COVERAGE.md |
| What does the pack provide | new-spec/00-pack-integration/index.md |
