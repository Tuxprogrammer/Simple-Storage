# Handoff: M03 (Terminal) closed, M04 (Wireless) next

## State
- M01, M02 and M03 exit gates are met and pushed.
- Evidence on the build host: `tools/remote/work.sh build` green (282 unit tests); `tools/remote/work.sh server` runs 113 in-game scenarios with 0 failed and logs `Registered 15 blocks, 3 item, 10 tile entities`.
- Bug coverage in `docs/original-spec/BUGS-COVERAGE.md` (all TERM, UI, CORE-03, CORE-04, CORE-05, CORE-06, CORE-07, CORE-10 rows tested or reasoned).
- Decisions D30 to D36 record the scope cuts and the terminal architecture (packet-synced virtual list, ClientProxy in the client package).

## What M04 inherits
- The configurator, `BlockFilter` and `bound_pos`.
- The inventory-link GUI and the `simplestorage_rc` saved data for `inventory.ChannelRegistry` (pure logic exists).
- The terminal beacon scan body (`StorageTerminalTileEntity.rescanBeacon` sets -1 today; the 40-tick gate exists and is tested for TERM-04).
- Wireless config sync (CORE-09 is still MISSING in the coverage table).

## Tooling
- Never run Gradle or Minecraft on the laptop; `tools/remote/work.sh build|server|format|run` is the edit loop (`server --flags-off` boots with every module off).
- `tools/remote/remote.sh build|devserver --autotest|devclient <slot> HEAD` run committed revisions.
- In-game scenarios are JUnit classes tagged `ingame`, run by `ingame.InGameRunner`.
- The API cheat sheet for briefs is `docs/agent-setup/forge-1710-names.md`.

## qwen workflow (owner rules)
- qwen creates and edits all files; never patch a failed qwen run by hand, diagnose from the job transcript (`python3 ~/.claude/local-plugins/qwen-agents/trace.py <job>`) and re-brief.
- Briefs carry the exact signatures of every class they touch, the exact 1.7.10 API names, tables of expected values for pure logic, and the header shape (package line immediately followed by the `// Ported from Tom's Simple Storage (MIT):` line).
- Notes are in `~/.claude/local-plugins/qwen-agents/NOTES.md`.
- Common qwen mistakes: dropped `class` keywords, duplicated assignments, newer-version API names, hallucinated packages, tests and code that disagree (put a table in the brief).

## Next 3 steps
1. Start M04 per `docs/ROADMAP.md` (entry gate, scope with the items inherited from M02 and M03).
2. Write M04 briefs from `docs/original-spec/04-wireless/` and `docs/new-spec/04-wireless/`.
3. Close every gate before pausing: build, in-game scenarios, bug tests, docs, commit, push.

## Known gaps
- No textures, models or recipes yet (M06, M05).
- The screens draw plain panels.
- A client GUI is only boot-tested (the dev client reaches the menu), not driven.
