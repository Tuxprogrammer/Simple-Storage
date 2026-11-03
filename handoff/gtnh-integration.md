# Handoff: S2 GT integration spike (lane/gtnh-integration)

Done 2026-11-03. Branch `lane/gtnh-integration`, 4 commits (74ec424, 4acd874, c9d61db, 706ecd8) on top of master@20a2aec.

## What S2 delivered (all committed)

- `docs/new-spec/00-pack-integration/`: gtnh-tiers-and-energy.md, recipes-and-materials.md, machine-model.md, recipe-viewer.md, tier-map.md (22 rows); index.md Contents updated (S3's rows kept).
- `docs/design/0003…` + `0004…`: refined in place with verified numbers (HV = 512V; EU sink = level emitter 8 EU/t), "refined by S2" noted, decisions kept.
- `docs/PACK-CHANGES.md`: filled (BQ quest lines per tier band, loot bag placement, GTNHLib config layout).
- `docs/original-spec/API-MAP.md`: every "pending S2" row resolved (real 1.7.10 classes or "none" with evidence).
- `docs/new-spec/OPEN-QUESTIONS.md`: Q1–Q4 settled (moved to Settled with answers).
- `docs/design/decision-log.md`: D7–D16 added.

## Verified ground truth (cite these, don't re-derive)

- Tier table: GT5-Unofficial 5.09.54.265, `GTValues.java#L92-L150` — ULV=8, LV=32, MV=128, HV=512, EV=2048, IV=8192, LuV=32768, ZPM=131072, UV=524288 (V = EU/t at 1A). Machine rate = `V[tier]×amps` (MTEBasicMachine.java#L747-751). Cheapest machine tier = LV (no setTier(0) machines).
- Recipes: `gt.recipe.assembler` is our map (D8); builder = `GTRecipeBuilder#builder()…addTo(map)` (GTRecipeBuilder.java#L173,#L1201). 138 maps total in RecipeMaps.java.
- NEI: `NEI…Config implements IConfigureNEI` auto-discovered (ClientHandler.java#L317-L346); registration via `codechicken.nei.api.API` statics.
- Source clones (git-ignored): `reference/src/{GT5@5.09.54.265, NEI@2.8.171-GTNH, AE2@rv3-beta-1095-GTNH, GTNHLib@0.11.54}` — reusable by M01/M05/M06.

## What remains (not S2's job)

1. **M01**: apply S3's dependency coordinates (integration-opportunities.md §Recommended dependencies) to dependencies.gradle — GT5/GTNHLib/NEI/AE2/waila/IIGX/BQ/EnhancedLootBags/Chisel.
2. **M05**: land recipes per tier-map.md rows (assembler at LV/MV/HV), implement the dev self-check (cap HV=512V, no item before its tier, input-gate rule), build the EU sink (D7: `euSink.enabled`/`euSink.euPerTick`, level emitter 8 EU/t), wire config per D16 (single `config/simplestoragenh.cfg`, GTNHLib @Config).
3. **M06**: NEI plugin `NEISimpleStorageConfig` per recipe-viewer.md §"What M06 builds" (D11 scope: crafting handler + usage handler + GUI overlay + INEIGuiHandler fast transfer; ghost drag deferred to beta).
4. **AE2 compat (M05, optional)**: `modules.integration.appeng` off at alpha (D14); AE2 storage access behind `Loader.isModLoaded("appeng")` in compat package.
5. **T4 real-pack checks** (deferred, marked "inferred" in the sheets): GT crafting-tool non-consumption in the grid; Angelica/Embeddium shader pipeline (optional); exact GT progression availability of the tier-gating secondary materials (circuit is the verifiable gate).
6. **Pack submission**: PACK-CHANGES.md rows (quest line delivery choice D12, loot bags, config overrides) go to the pack via pack-contribution-policy when the lane merges.

## Open risks (≤5)

- R1: if the pack bumps GT5-Unofficial before merge, re-verify the tier table + map identifiers (skill §6 re-pin procedure).
- R2: `circuitLV/MV/HV` gating assumes circuits stay the tier gate in GT recipe changes — re-check at M05.
- R3: BQ jar-shipped quest line is a first in the pack (no precedent, S3) — pack may prefer their quest repo; D12 allows override.
- R4: 0003 "basic terminal" interpretation (storage terminal at LV) is a documented reading (D13), not an owner decision — flag at the next owner review.
- R5: UEV+ tiers (10–15) exist in this GT build but are never referenced by us; if a future machine tier below LV appears (ULV machine), the "no machine below LV" note in gtnh-tiers-and-energy.md needs revisiting.
