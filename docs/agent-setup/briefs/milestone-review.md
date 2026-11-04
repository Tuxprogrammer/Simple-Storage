<!-- Brief template: one batched review slice at a milestone gate. Spawn with the port-reviewer agent (sonnet, medium). One agent per slice, in parallel, disjoint slices. Fill the <> fields. -->

You are reviewing **<slice, e.g. "inventory, filter and sort packages">** of milestone <Mxx> before its exit gate.

## Goal
Find defects the build and tests cannot: behavior that disagrees with the spec, bugs from `docs/original-spec/BUGS.md` that came back, missing tolerance for old saves, client classes reachable from common code, packets that trust the client, missing tests.

## Scope
- Diff to review: `git diff <milestone-base>..HEAD -- <paths>` (read it per file, never the whole diff at once).
- Spec sheets that apply: <at most 3, by path and anchor>.
- BUGS.md rows that apply: <ids>. Each needs a `doesNotReproduce<ID>` test or a reason in `bug-reasons.json`.

## Method
1. For each changed class, check its `// Spec:` target and compare the behavior, numbers and sides.
2. Check the rules in CLAUDE.md "Non-negotiables" 1 to 9 against the diff.
3. Write every finding to `handoff/review-<slice>.md` as one line each: `file:line | severity (blocker, major, minor) | what is wrong | what the spec or bug row says`. Blockers and majors need a quoted spec line or bug id.
4. Do not fix anything and do not edit code. Fixes go back to qwen through the gate.

## Limits (hard)
Context 150k or 90 calls, then write what you have and STOP. One pass; no re-verification round. Report at most 10 lines: counts per severity and the findings path.
