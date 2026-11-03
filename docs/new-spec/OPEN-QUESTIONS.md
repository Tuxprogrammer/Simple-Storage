# Open questions

What the new spec has not settled yet. Each row says what settles it: research in a roadmap spike, a milestone, or the owner, with a recommendation. A settled question leaves the open list, and its answer goes into the area sheet and a design record or decision-log row.

## Open

| # | Question | Recommendation | Settled by |
|---|---|---|---|

(none as of S2, 2026-11-03 — Q1–Q4 settled below)

## Settled

| # | Question | Answer | Settled by |
|---|---|---|---|
| Q1 | Which tier cap does the storage content occupy, and what is the per-item tier map? | Cap: **HV** — refined to **512V** in this GT build (GT5-Unofficial 5.09.54.265's tier table is shifted one tier up vs classic GT5; LV=32, MV=128, HV=512 EU/t). Per-item map: **written** — [00-pack-integration/tier-map.md](00-pack-integration/tier-map.md), 22 rows (15 blocks + 7 items), each exactly one tier within the 0003 band, gated by final-recipe machine tier + a tier-gating circuit input. Decisions D13 (map accepted), D15 (tier-table refinement). | S2 (2026-11-03), built in M05 |
| Q2 | Does the terminal consume EU? | Confirmed [0004](../design/0004-energy-and-machine-model.md): **power-free by default**. The optional M05 EU sink is the **level emitter as a fixed consumer at 8 EU/t (ULV rate) while active**, behind `euSink.enabled` (off by default) with an `euSink.euPerTick` knob — decision D7. Mechanism detail in [00-pack-integration/gtnh-tiers-and-energy.md](00-pack-integration/gtnh-tiers-and-energy.md) §EU model. | S2 (2026-11-03), built in M05 |
| Q3 | Does GTNH ship Applied Energistics 2, and does that change this mod's positioning? | **Yes** — AE2 rv3-beta-1095-GTNH is in the pack (S3, [shipped-mods.md](00-pack-integration/shipped-mods.md)). Positioning: **Simple Storage is the cheap, simple, power-free network *beside* AE2, not a replacement** — AE2 is the late-game ME storage solution with cells and interfaces; this mod is the early/mid-game "wire a few chests together" system that never asks for energy (decision D14). Compat surface: AE2's NEI usage-handler precedent (S3) and optional AE2 storage access in M05 behind `modules.integration.appeng` (off at alpha, D10). | S2 (2026-11-03) |
| Q4 | Which recipe viewer does GTNH use, and what plugin API applies in 1.7.10? | **NEI 2.8.171-GTNH** (no JEI/REI/EMI in the pack). The 1.7.10 plugin surface is verified from source: `NEI…Config implements codechicken.nei.api.IConfigureNEI` (auto-discovered by name scan), `codechicken.nei.api.API` registration methods, `ICraftingHandler`/`IUsageHandler`/`TemplateRecipeHandler`, `INEIGuiHandler`/`FastTransferManager` for GUI interaction; Jade maps to Waila 1.19.34 (`IWailaBlock` via IMC), TOP and Sophisticated Storage are "none". Full specifics: [00-pack-integration/recipe-viewer.md](00-pack-integration/recipe-viewer.md); scope decision D11. | S2 (2026-11-03), built in M06 |
