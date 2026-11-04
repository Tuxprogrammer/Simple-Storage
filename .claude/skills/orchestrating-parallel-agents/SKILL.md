---
name: orchestrating-parallel-agents
description: Use when spawning, briefing or supervising subagents or worktree agents - choosing their scope, model and effort, writing their brief, setting stop rules and round limits, and handling their notifications. Not for doing the work inside one agent (see agent-context-budget).
---

# Orchestrating parallel agents on a budget

The cost of an agent grows with the square of its length, and with its model. Short, well-scoped agents on the cheapest adequate model are the main lever: many short mid-tier agents cost far less per commit than a few milestone-sized top-tier ones.

## Scope

- **One deliverable per agent:** one feature, one oracle, one spec area or one bug. A milestone is several agents run in sequence, not one.
- **Size the brief.** If the task needs more than about 120 calls, split it before spawning.
- **Agents do not spawn their own forks.** Only the orchestrator spawns, so cost stays visible and the depth stays at 1.
- Give parallel agents disjoint file ownership (code, assets, docs) and their own worktree and build slot.

## Model and effort

Pass `model` on every Agent call; never inherit by accident. Ready-made definitions are in `.claude/agents/`.

| Work | Model | Effort | Agent |
|---|---|---|---|
| Research, cataloging, spec transcription, applying settled decisions to docs, audits against a checklist | sonnet | medium | `port-researcher` |
| Verify and land, conflict fixes, shot runs, log triage | sonnet | medium | `port-integrator` |
| Routine implementation | sonnet | medium | `port-builder` with a model override |
| New design, tricky worldgen or concurrency code, decisions | opus | high | `port-builder` |

## Every brief contains

Use the templates in `docs/agent-setup/briefs/`.

- **Goal.** One sentence.
- **Stop condition.** A testable "done", plus "report and stop" when it is met.
- **Round limit.** At most **2 verification rounds**. If round 2 fails, report the failure with evidence and stop. Do not iterate on a budget or perf target past the limit; report the measured gap.
- **Context limit.** Hand off at 200k or 120 calls (`agent-context-budget`).
- **Read list.** At most 3 named sections, by `grep` anchor or line range. Never "read CLAUDE.md": it is already loaded. Never "read every spec area it depends on".
- **Merges.** "Do not merge the integration branch into your lane" (`integration-merge-choreography`).
- **Report.** At most 15 lines: commits, what is live, the evidence path, open gaps. Long evidence goes in a file.
- **Banned phrases:** "never stop at a first pass", "verify everything", "make sure it all still holds". They remove the stop condition.

## Supervising

- **Notifications.** "Agent X is waiting on its own background work" needs no reply. If the harness makes you take a turn, answer with nothing or a single word and make no tool call. Batch the real follow-ups.
- **Agents that stall or overrun.** Do not message an agent that has passed its limits to "keep going". Stop it, read its handoff and start a fresh agent.
- **Final reports.** Keep them in files. Paste only the short summary into your own context.
- **Unattended runs must not block on approval prompts.** Write temp files only to a dedicated scratch directory by absolute path, never `../` out of the worktree, no broad `rm -rf` (even inside an `ssh` string), no edits to `.git/` internals or global config. List leftovers for a human instead.
- Cap parallel memory-heavy jobs (Minecraft JVMs, multiprocessing pools) to what the machine can hold.

## Qwen changes and batched review

Code and test changes are written by the local qwen worker through the check gate; Claude agents review in batches. Set by the owner, 2026-11-04.

- **A change is a qwen job behind the gate.** Write the brief to a file from `docs/agent-setup/briefs/qwen-gated-change.md`, run `python3 tools/qwen/gate.py --brief FILE --check '<check>' --max-rounds 3` with `run_in_background`, and read only its verdict line. Compiler output never enters your context. On `STUCK`, read `result.json`, change the brief and rerun; never patch the worker's files by hand.
- **The check is cheap first.** Chain `python3 tools/qwen/namecheck.py` (known-bad names from `docs/agent-setup/slips.tsv`, runs in a second) before the build host call, so a wrong 1.7.10 name costs one round on the laptop instead of a build.
- **No review inside a milestone.** The gate (names, compile, unit and static tests) is the only control between landings. Do not spawn review agents per change.
- **One batched review before the exit gate.** When the milestone's boxes are built, split `git diff <milestone-base>..HEAD` into slices by package (parallel, disjoint), and spawn one `port-reviewer` per slice with `docs/agent-setup/briefs/milestone-review.md`. Findings go to `handoff/review-<slice>.md`; the gate does not close with an open blocker or major. Fixes go back to qwen through the gate, then a single recheck of the fixed items only.
- **Exception: a seam review.** When a finished package is the base that many later pieces build on (for example the inventory model before the terminal), run one cheap review of that package alone when it lands, so a wrong shape is not built on for the rest of the milestone.
- **Learn from the slips.** When a gate round-1 error repeats a mistake already in the cheat sheet, add a row to `slips.tsv`. When a new mistake appears twice across jobs, add it to `slips.tsv` and `forge-1710-names.md` in the same commit.
