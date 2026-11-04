# 03-terminal · contradictions and proposals

Part of [03-terminal](index.md).

## Contradictions with other docs

| Where | Quote | Conflicts with |
|---|---|---|
| [original-spec/03-terminal/index.md:171](../../original-spec/03-terminal/index.md) | "None yet (port notes go to the new-spec 03-terminal area)." | not the literal "None yet." body; left as is (see rule note in the report) |

## Proposed (not decided)

| Id | Question | Recommendation | Settled by |
|---|---|---|---|
| P03-1 | How does the terminal GUI base handle display slots, scroll and the data packet? | One `GuiContainer` subclass (`TSContainerScreen`) with a `SlotAction` packet, server validates every action | M03 |
| P03-2 | Keep any in-GUI recipe book in the crafting terminal? | Drop it; rely on the NEI overlay (D11); keep per-cell auto-fill reservation (TERM-05) | Owner, M03 |
| P03-3 | The tag filter and item tags: what replaces them in 1.7.10? | `OreDictionary` name filter with the same add/remove list UI; "match components" becomes "match NBT" | Owner, M03 |
| P03-4 | Fix the unsatisfiable beacon scan gate (TERM-04/WIRE-04) in the terminal or the wireless lane? | Fix once in shared code in M03 and let M04 reuse it | M03 and M04 |
| P03-5 | Fix the small UI bugs (TERM-01, 03, 06, 07, 08, 10, 11) as written in the table? | Yes: fix each with a `doesNotReproduce<ID>_` test (ROADMAP M03 exit gate already requires the tests) | M03 |
| P03-6 | Source of the TYPE sort order in 1.7.10 | Creative-tab list order; names from `Item.itemRegistry`; keep the ordinal 5 for old saves | M03 |
| P03-7 | Hash and `CraftingMatrix` on 1.7.10 stacks | NBT-based hash; `CraftingMatrix` over `InventoryCrafting` | M03 |
| P03-8 | Keep multithreaded change tracking (TERM-12)? | Server thread only until a profile shows a need; fix the stale array either way | M03 |
