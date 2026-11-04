# Handoff: M02 (Storage) closed, M03 (Terminal) next

## State
- The docs reached the Lost Cities standard, and the M01 (foundation) and M02 (storage) exit gates are met and pushed.
- Evidence: `tools/remote/work.sh build` green (120 unit tests, 0 failures).
- `tools/remote/work.sh server` runs 74 in-game scenarios, 0 failed, with the registered counts logged (`Registered 13 blocks, 1 item, 8 tile entities`), also with every behavior module off (`--flags-off`).
- Coverage in `docs/original-spec/BUGS-COVERAGE.md`: 12 tested (STOR-01 to STOR-10, CORE-05, CORE-06), 28 missing, 10 cannot occur, 2 not a bug.
- Decisions D30 to D33 record the M02 scope cut, the hopper cooldown, the in-game test runner, and the CORE-05/06 outcomes.

## Tooling you need
- Never run Gradle or Minecraft on the laptop (OOM risk).
- `tools/remote/work.sh build|test|server|format` is the edit loop (uploads the working tree, formats, and brings `src/` back).
- `tools/remote/remote.sh build|devserver|devclient <slot> HEAD` run committed revisions; devclient uses Xvnc because Xvfb breaks LWJGL 2.
- The host paths live in the git-ignored `.env`.
- JDK links under `shared/` on the host: jdk8, jdk17, jdk21, jdk25.

## qwen workflow (owner rules)
- qwen creates new files and does the edits.
- Never patch a failed qwen run by hand: diagnose from `~/.cache/qwen-agents/jobs/<id>/transcript.jsonl` (`python3 ~/.claude/local-plugins/qwen-agents/trace.py <job>`), change the brief or the plugin, and rerun.
- Notes and the brief recipe are in `~/.claude/local-plugins/qwen-agents/NOTES.md`: workers had only Edit and Read until `--bare` was dropped; put every cross-file signature in the brief; say which tools to use; one deliverable family per job; paste the compiler errors into fix briefs; put the 1.7.10 API cheat sheet `docs/agent-setup/forge-1710-names.md` facts in briefs.
- Max 4 parallel workers.

## Next 3 steps
1. Start M03 (Terminal) per `docs/ROADMAP.md` with its entry gate; its scope now includes the filing cabinet GUI and the filter components deferred from M02 (D30).
2. Write M03 briefs for qwen from the `docs/original-spec/03-terminal/` sheets and the new-spec 03 area; ported files carry the header `// Ported from Tom's Simple Storage (MIT):` directly under the package line.
3. Keep every gate closed before pausing: build, in-game scenarios, bug tests, docs, commit, push.

## Known gaps
- No textures, models, or recipes yet (M06, M05).
- `tools/remote/remote.sh install-tools` leaves new scripts non-executable on the host until `chmod +x` (rsync keeps the old mode when content is unchanged).
- The INDEX and area maps must stay at most 40 lines.
