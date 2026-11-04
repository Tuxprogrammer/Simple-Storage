# Handoff: raise Simple Storage docs to the Lost Cities standard (2026-11-03)

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

## Not done
1. **Correction pass for the 41 UNVERIFIED flags** (grep -rn UNVERIFIED docs/original-spec). Several touch the bug register: TERM-10 (the shift-step table is actually symmetric), TERM-12 (same-tick growth cannot index a stale array; residual risk is elsewhere), TERM-11 shift-click behavior, STOR/trims paint-scrape claims. Fix the sheet text, then re-judge each affected BUGS.md row (keep, rewrite or move to Not bugs) and rerun tools/bugs-coverage/generate.py.
2. Spec sheets still describe upstream's now-dropped features (narrator, recipe book, TYPE sort) as upstream facts; that is correct, the new-spec rows record the deviation.
3. Not present, premature before code: docs/config.md, docs/api.md, docs/reviews/.
4. Decision log holds D1-D29; keep adding a row whenever a choice is settled.

## Next 3 steps
1. Run the correction pass above with Sonnet editors per area (disjoint files; `git show 26.2:<path>` is the source), then review BUGS.md rows by hand.
2. Re-run the link check and index-length check (all index.md <= 40 lines) after any docs change.
3. M01 can start: entry gate is met; its scope now includes the `required-after` dependency declaration (D17) and a reminder that tests for the four+1 static checks need failing fixtures.

## Division of labor and tooling (unchanged)
qwen creates new documents (one file to write, <=2 reads, step plan, max_turns ~12); Sonnet reviews and edits; Claude decides and integrates. qwen plugin v2 is async. Questions to the owner are batched with context, options, consequences and a recommendation; decisions are recorded as decision-log rows.
