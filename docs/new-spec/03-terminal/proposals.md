# 03-terminal · contradictions and proposals

Part of [03-terminal](index.md).

## Contradictions with other docs

| Where | Quote | Conflicts with |
|---|---|---|
| [original-spec/03-terminal/index.md:171](../../original-spec/03-terminal/index.md) | "None yet (port notes go to the new-spec 03-terminal area)." | not the literal "None yet." body; left as is (see rule note in the report) |

<a id="proposed-not-decided"></a>
## Accepted proposals

All eight were settled on 2026-11-03.

| # | Question | Outcome | Decision |
|---|---|---|---|
| P03-1 | How does the terminal GUI base handle display slots, scroll and the data packet? | One `GuiContainer` subclass (`TSContainerScreen`) with a `SlotAction` packet; the server validates every action | D29 |
| P03-2 | Keep any in-GUI recipe book in the crafting terminal? | Dropped; the NEI overlay covers it (D11); per-cell auto-fill reservation (TERM-05) kept | D21 |
| P03-3 | The tag filter and item tags: what replaces them in 1.7.10? | Ore-dictionary-name filter (list of `OreDictionary` names plus allow/deny) with the same add/remove list UI; key `tags` stays a string list; "match components" becomes "match NBT" | D20 |
| P03-4 | Fix the unsatisfiable beacon scan gate (TERM-04/WIRE-04) in the terminal or the wireless lane? | Fixed once in shared code in M03; M04 reuses it | D29 |
| P03-5 | Fix the small UI bugs (TERM-01, 03, 06, 08, 11, 13) as written? | Yes, each with a `doesNotReproduce<ID>_` test. TERM-07 (keep-last narrator text) cannot occur: the narrator layer is not ported; TERM-10 was checked against the code and is not a bug | D29, D21 |
| P03-6 | Source of the TYPE sort order in 1.7.10 | TYPE sort mode dropped: five visible modes (amount, name, mod, space efficiency, ID); ordinal 5 reserved and inert so old saves load; TERM-09 cannot occur | D27 |
| P03-7 | Hash and `CraftingMatrix` on 1.7.10 stacks | NBT-based hash; `CraftingMatrix` over `InventoryCrafting` | D29 |
| P03-8 | Keep multithreaded change tracking (TERM-12)? | Server thread only until a profile shows a need; the stale array is fixed either way | D29 |
