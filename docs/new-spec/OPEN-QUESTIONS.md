# Open questions

What the new spec has not settled yet. Each row says what settles it: research in a roadmap spike, a milestone, or the owner, with a recommendation. A settled question leaves the open list, and its answer goes into the area sheet and a design record or decision-log row.

## Open

| # | Question | Recommendation | Settled by |
|---|---|---|---|

(none as of 2026-11-03: Q1 to Q9 and all 36 area proposals are settled below; new questions are added here with a recommendation)

## Settled

| # | Question | Answer | Settled by |
|---|---|---|---|
| Q1 | Which tier cap does the storage content occupy, and what is the per-item tier map? | Cap: **HV** — refined to **512V** in this GT build (GT5-Unofficial 5.09.54.265's tier table is the classic one — ULV=8, LV=32, MV=128, HV=512 EU/t; 0003's first draft said HV=128V, a slip, D15). Per-item map: **written** — [00-pack-integration/tier-map.md](00-pack-integration/tier-map.md), 22 rows (15 blocks + 7 items), each exactly one tier within the 0003 band, gated by final-recipe machine tier + a tier-gating circuit input. Decisions D13 (map accepted), D15 (tier-table refinement). | S2 (2026-11-03), built in M05 |
| Q2 | Does the terminal consume EU? | Confirmed [0004](../design/0004-energy-and-machine-model.md): **power-free by default**. The optional M05 EU sink is the **level emitter as a fixed consumer at 8 EU/t (ULV rate) while active**, behind `euSink.enabled` (off by default) with an `euSink.euPerTick` knob — decision D7. Mechanism detail in [00-pack-integration/gtnh-tiers-and-energy.md](00-pack-integration/gtnh-tiers-and-energy.md) §EU model. | S2 (2026-11-03), built in M05 |
| Q3 | Does GTNH ship Applied Energistics 2, and does that change this mod's positioning? | **Yes** — AE2 rv3-beta-1095-GTNH is in the pack (S3, [shipped-mods.md](00-pack-integration/shipped-mods.md)). Positioning: **Simple Storage is the cheap, simple, power-free network *beside* AE2, not a replacement** — AE2 is the late-game ME storage solution with cells and interfaces; this mod is the early/mid-game "wire a few chests together" system that never asks for energy (decision D14). Compat surface: AE2's NEI usage-handler precedent (S3) and optional AE2 storage access in M05 behind `modules.integration.appeng` (off at alpha, D10). | S2 (2026-11-03) |
| Q4 | Which recipe viewer does GTNH use, and what plugin API applies in 1.7.10? | **NEI 2.8.171-GTNH** (no JEI/REI/EMI in the pack). The 1.7.10 plugin surface is verified from source: `NEI…Config implements codechicken.nei.api.IConfigureNEI` (auto-discovered by name scan), `codechicken.nei.api.API` registration methods, `ICraftingHandler`/`IUsageHandler`/`TemplateRecipeHandler`, `INEIGuiHandler`/`FastTransferManager` for GUI interaction; Jade maps to Waila 1.19.34 (`IWailaBlock` via IMC), TOP and Sophisticated Storage are "none". Full specifics: [00-pack-integration/recipe-viewer.md](00-pack-integration/recipe-viewer.md); scope decision D11. | S2 (2026-11-03), built in M06 |
| Q5 | Is GT5-Unofficial a hard runtime dependency? | **Yes**: `required-after:gregtech` (decision D17); README and 0002 §2 corrected. | Owner, 2026-11-03 |
| Q6 | What does the advanced wireless terminal's open key default to? | **Unbound**, with a tooltip telling the player to bind it (D28). | Owner, 2026-11-03 |
| Q7 | Does the circuit really gate each tier? | Verified in M05 from the pinned GT source; re-gate a row if a lower tier can make the circuit (D29; M05 exit gate). | Policy, M05 |
| Q8 | Is the storage terminal the 0003 "basic terminal"? Where does the network sit? | **Network parts move to LV, the basic wireless terminal stays MV** (D18, amends D13 and 0003 §3). | Owner, 2026-11-03 |
| Q9 | Does the jar ship a BetterQuesting quest line? | **Yes, namespaced `simplestoragenh.*`**, pack may override (D19, confirms D12). | Owner, 2026-11-03 |
| P01-P06 | The 36 area proposals | Settled: owner decisions D20 to D28 (tag filter, scope drops, config keys, hopper, Waila six, English only, terminal slot, TYPE sort, unbound key); the rest accepted as recommended under D29. Each area's `proposals.md` records the outcome per row. | Owner and policy, 2026-11-03 |
