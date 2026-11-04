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

## Not done / known gaps vs Lost Cities
1. Citation density: original sheets average ~1.6 `file:line` cites each (82 total / 51 sheets); LC cites per claim. Upstream is `git show 26.2:<path>`.
2. Area `index.md` files exceed the 40-line map rule in `mod-port-budget` (03-terminal 171 lines); decide whether to split or relax the rule.
3. Not present: `docs/config.md` (player-facing config reference), `docs/api.md`, `docs/reviews/`. Premature before code; revisit at M01/M05.
4. Decision log is D1-D16; LC is D269 because it records every settled choice. Settle P-rows and Q5-Q9 into D-rows as the owner decides.

## Next 3 steps
1. Present Q5-Q9 and the 36 proposals to the owner; record accepted ones as decision-log rows D17+ and move them to Settled in `docs/new-spec/OPEN-QUESTIONS.md` (grep, do not cat).
2. Fold in two skill findings: Q6 gets "plus a test that checks key defaults against the pack's key list"; M05 gets "recipes get free programmed-circuit numbers; test clashes against a dumped full-pack recipe set" (`gt-first-design`).
3. Optionally raise citation density with Sonnet editors per area (disjoint files), `git show 26.2:<path>` as the source.

## Open questions for the owner
Q5 (GT hard dependency), Q6-Q9, P01-P06 (see OPEN-QUESTIONS). Also: should 0001/0002 be edited for Q5 once decided (README and 0002 still say GTNHLib is the one hard dependency)?
