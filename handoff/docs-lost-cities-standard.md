# Handoff: docs at the Lost Cities standard; M01 in progress (2026-11-03)

## Goal and stop condition
Review the docs for inconsistencies and undecided items, then bring them up to the owner's reference standard (the Lost Cities repo, sibling folder `LostCities`). Done when: no known contradictions, every new-spec area exists, open items are registered with a recommendation and a settler.

## Done and committed on master (7 + 3 commits, newest 32307d8)
- Fixed the false "tier table shifted vs classic GT5" claim everywhere; one tier-gating rule (machine tier equals item tier plus a circuit input) in 0003, tier-map, energy sheet.
- New-spec areas `01-core` to `06-client-ui` written (deviation tables, 36 Proposed rows P01-P06, config key map in 01-core/config.md).
- OPEN-QUESTIONS: Q5-Q9 added; all 36 area proposals listed with recommendation and settler.
- `BUGS-COVERAGE.md` generated (46 MISSING, 4 cannot occur, 1 not a bug); `bug-reasons.json` added.
- ROADMAP: S4 spike, "settle the Proposed rows" exit gates, five static checks, M05 tool-consumption check, M06 no longer builds a duplicate assembler NEI handler (D11 amended: GT's GTNEIDefaultHandler renders assembler recipes).
- Verified against pack sources and fixed: no capability system in Forge 10.13.4.1614; `ISidedInventory` is vanilla; AE2 1.7.10 has no `IStorageChannel`; API-MAP 1.7.10 names; upstream branch list; texture counts.
- `docs/testing/README.md` runbook (qwen-written, reviewed); CLAUDE.md now says to load skills up front and has a 20-skill table.
- Privacy grep of all commits: clean.

## Division of labor (owner rule)
qwen creates new documents; Sonnet reviews and edits; Claude decides and integrates. qwen plugin v2 is async (see memory `qwen-delegation`): `qwen_start`, then Bash `run_in_background` the `wait` command. Keep qwen briefs to one file to write, <=2 reads, a step plan, max_turns ~12; a 14-file read list failed at 41 turns.

## Since the first handoff (all committed, newest 19ab3cb)
- Navigation: docs/INDEX.md (level 1, 36 lines); every area index.md is a <=40-line map (facts/coverage/bugs/lang siblings; new-spec deviations.md + proposals.md). The skill's 40-line rule wins over Lost Cities.
- Citations: 1,077 verified `file:line` cites over 51 original sheets (was 82). 41 claims the code contradicts are marked inline `(UNVERIFIED: ...)`.
- Owner decisions D17-D29 recorded and propagated (decision log, tier map, 0001-0003, PACK-CHANGES, ROADMAP, new-spec areas). Highlights: GT hard dependency; network parts LV (basic wireless MV); English only; TYPE sort, narrator, recipe book, ViveCraft, waterlogging not ported; open key ships unbound; one hopper (the "mid/fast variants" proposal was a misreading).
- OPEN-QUESTIONS has no open rows. BUGS-COVERAGE: 40 untested, 10 cannot occur, 1 not a bug.

## State after 7919a62 and the M01 start
- The 41 UNVERIFIED flags are corrected; the bug register is re-judged (52 IDs: 50 suspected, TERM-10 and INT-04 not bugs, TERM-13 new). Coverage 40 untested / 10 cannot occur / 2 not a bug.
- M01 landed in 2870e6f plus a follow-up: pinned dependencies, GTNHLib config (modules, network, wireless, behavior, euSink), ModuleState, packet base classes (adapted from the sibling Lost Cities repo), the five static checks with failing fixtures, dev self-check, `.changelogs/unreleased.md`. `./gradlew spotlessApply build` passes locally.
- M01 boxes ticked: build/tests/static checks, dependencies. Still open: `runServer` boot (needs the Minecraft EULA accepted by the owner; the tracked `run/` has no eula.txt), `runClient` main menu, self-check on a fresh world, `required-after` proof on a server without GT, config documentation test, packet-base-class tests for position and authorization rules, P01 rows settled in the shipped deviation table.
- `tools/remote/remote.sh` is not configured for this repo (`.env` has only the Lost Cities host keys); builds ran locally on JDK 21.

## Next 3 steps
1. Owner accepts the EULA (or asks me to), then run `./gradlew runServer` and `runClient`; tick the boxes.
2. Add tests: config key table against PACK-CHANGES, packet position/authorization rules, `required-after` check.
3. Settle the P01 rows against what shipped, then start M02 (Storage).

Division of labor (unchanged): qwen creates new documents and boilerplate (one file per job, small plan; two of three M01 jobs ran out of turns and I finished them), Sonnet reviews and edits, Claude decides and integrates.
