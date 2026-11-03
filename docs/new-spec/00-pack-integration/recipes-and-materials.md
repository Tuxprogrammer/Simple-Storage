# GT recipe maps, materials and crafting conventions (verified, GT5-Unofficial 5.09.54.265)

Which GT recipe maps our recipes go into, the material/ore-dict conventions, and the crafting-table convention. All map identifiers and builder APIs verified 2026-11-03 from GT5-Unofficial source at tag `5.09.54.265` (git-ignored `reference/src/GT5`); line numbers refer to that checkout.

## Recipe registration API

GTNH mods add machine recipes to GT's recipe maps through the **GT recipe builder**:

- Entry point: `gregtech.api.util.GTRecipeBuilder#builder()` (GTRecipeBuilder.java#L173).
- Registration: `GTRecipeBuilder#addTo(IRecipeMap)` / `addTo(IRecipeMap...)` (GTRecipeBuilder.java#L1201-L1210) — one builder can feed several maps; `build()` (GTRecipeBuilder.java#L1071) emits without adding.
- Maps live in `gregtech.api.recipe.RecipeMaps` (RecipeMaps.java), each built by `RecipeMapBuilder.of("<id>")` (RecipeMaps.java, e.g. #L137, #L1265, #L1446). The map **identifier string** (e.g. `gt.recipe.hammer`) is the stable name used by NEI/GT's recipe viewer and by CraftTweaker.
- Machine↔map binding: single-block machines are built with `MTEBasicMachineWithRecipeBuilder.builder(<id>).setTier(<tier>).setRecipes(<map>)` (LoaderMetaTileEntities.java; e.g. the LV Forge Hammer at LoaderMetaTileEntities.java#L111-L119, `setTier(1)` + `setRecipes(hammerRecipes)`). Every basic machine exists at tiers 1–12 (LV–UMV), so a map recipe at any tier 1–12 has a machine (see [gtnh-tiers-and-energy.md](gtnh-tiers-and-energy.md)).

## The maps this mod's recipes use

Per [0003](../../design/0003-gtnh-layer-and-tier-ceiling.md) §2 ("machine processing goes through GT's recipe maps with the builder"):

| Map identifier (RecipeMaps.java) | Field | Machine (min tier) | Use in this mod |
|---|---|---|---|
| `gt.recipe.assembler` | `assemblerRecipes` (RecipeMaps.java#L1265) | Assembler, tiers 1–12 (LV–UMV) | **Primary assembly map** — the network parts, terminals and tools (9 item inputs incl. a circuit slot, maxIO 9/1/1/0) |
| `gt.recipe.circuitassembler` | `circuitAssemblerRecipes` (RecipeMaps.java#L1276) | Circuit Assembler, tiers 1–14 (LV–MAX) | Not ours directly — how the tier-gating circuits we consume are made (cites the gate, not a recipe of ours) |
| `gt.recipe.hammer` | `hammerRecipes` (RecipeMaps.java#L1446) | Forge Hammer, tiers 1–12 (LV–UMV) | GT parts (plates, frames) our crafting recipes consume are made here |
| `gt.recipe.metalbender` | `benderRecipes` (RecipeMaps.java#L1228) | Bender, tiers 1–12 (LV–UMV) | Plate bending for the parts above |
| `gt.recipe.wiremill` | `wiremillRecipes` (RecipeMaps.java#L1218) | Wire Mill, tiers 1–12 (LV–UMV) | Fibre-optic/wire inputs for cables (inferred usage) |
| `gt.recipe.extruder` | `extruderRecipes` (RecipeMaps.java#L1434) | Extruder, tiers 1–12 (LV–UMV) | Alternative for cable fabrication (inferred usage; assembler is the default choice) |

Full map inventory for reference (all verified present in RecipeMaps.java at the tag, with their identifiers): `gt.recipe.orewasher` (#L137), `gt.recipe.thermalcentrifuge` (#L163), `gt.recipe.compressor` (#L189), `gt.recipe.extractor` (#L235), `gt.recipe.recycler` (#L272), `gt.recipe.furnace` (#L285), `gt.recipe.electricblastfurnace`-family (`efrBlastingRecipes` #L303, `efrSmokingRecipes` #L321), `gt.recipe.microwave` (#L339), `gt.recipe.assembler` (#L1265), `gt.recipe.circuitassembler` (#L1276), `gt.recipe.canner` (#L1288), `gt.recipe.lathe` (#L1314), `gt.recipe.cuttingsaw` (#L1373), `gt.recipe.extruder` (#L1434), `gt.recipe.hammer` (#L1446), `gt.recipe.uuamplifier` (#L1465), `gt.recipe.alloysmelter` (#L1238), `gt.recipe.wiremill` (#L1218), `gt.recipe.metalbender` (#L1228), `gt.recipe.mixer` (#L533), `gt.recipe.autoclave` (#L542), `gt.recipe.chemicalbath` (#L610), `gt.recipe.brewer` (#L616), `gt.recipe.fluidheater` (#L626), `gt.recipe.distillery` (#L649), `gt.recipe.fermenter` (#L713), `gt.recipe.fluids` (#L725), `gt.recipe.fluidextraction` (#L734), `gt.recipe.packager` (#L747), `gt.recipe.unpackager` (#L769), `gt.recipe.fusionreactor` (#L776), `gt.recipe.centrifuge` (#L788), `gt.recipe.electrolyzer` (#L819), `gt.recipe.blastfurnace` (#L853), `gt.recipe.plasmaforge` (#L861), `gt.recipe.cokeoven` (#L894), `gt.recipe.vacuumfreezer` (#L1098), `gt.recipe.chemical` (#L1126), `gt.recipe.multiblockchemical` (#L1168), `gt.recipe.distillationtower` (#L1175), `gt.recipe.cracking` (#L1203), `gt.recipe.pyro` (#L1210), `gt.recipe.solarfactory` (#L1214), `gt.recipe.nanoforge` (#L1609), `gt.recipe.pcbfactory` (#L1625), `gt.recipe.isotope-decay` (#L1739), `gt.recipe.cable` (#L1746). (Names abbreviated to their identifier where the field name differs; the identifier string in `RecipeMapBuilder.of(...)` is what M05 must match.)

**Decision (D8):** all machine recipes in this mod go into **`gt.recipe.assembler`** at the item's tier (LV/MV/HV per [tier-map.md](tier-map.md)). One map keeps the NEI category, the dev self-check and the questbook simple; the assembler's 9-slot layout fits the part-heavy recipes, and it exists at every tier we need. No map above tier 3 (HV) is ever used — enforcing the [0003](../../design/0003-gtnh-layer-and-tier-ceiling.md) cap.

## Materials and ore-dict conventions

- **Materials:** `gregtech.api.enums.Materials` (Materials.java) — one entry per GT material (`Materials.Steel`, `Materials.Tin`, `Materials.Aluminium`, `Materials.Glass`, `Materials.Quartz`, `Materials.Fibers` …). Per [0003](../../design/0003-gtnh-layer-and-tier-ceiling.md) §1, our recipes use GT materials only; nothing mod-specific is registered.
- **Ore prefixes:** `gregtech.api.enums.OrePrefixes` (OrePrefixes.java) — `OrePrefixes.plate.get(Materials.Steel)` → `plateSteel`, `OrePrefixes.gear`, `OrePrefixes.frameGirder`, `OrePrefixes.cable` etc. Recipe ingredients are written as these ore-dict items, so other mods' parts unify in automatically (GT's unification, `gregtech.api.util.GTOreDictUnificator`, GTOreDictUnificator.java#L45).
- **Circuits are the tier gate:** `OrePrefixes.circuit.get(Materials.LV|MV|HV)` (`circuitLV`, `circuitMV`, `circuitHV`) — the same resolution GT itself uses to build machine hulls (GTModHandler.java#L662-L690). Every machine-recipe row in [tier-map.md](tier-map.md) is gated by exactly this input.
- **GT parts as ingredients:** plates, frames, gears, wires, coils — all from the maps above (`gt.recipe.hammer`, `gt.recipe.metalbender`, `gt.recipe.wiremill`), so no part in our recipes is unobtainable before the item's tier.

## The crafting-table convention (GT tools in the grid)

Verified from GT's own machine-recipe construction:

- GT crafting recipes are **ore-dict shaped/shapeless recipes** registered into the vanilla crafting table: `gregtech.api.util.GTShapedRecipe extends ShapedOreRecipe` and `GTShapelessRecipe extends ShapelessOreRecipe` (GTShapedRecipe.java#L14-L16, GTShapelessRecipe.java#L19), registered via `GTModHandler.addCraftingRecipe(result, recipe)` (GTModHandler.java#L626-L660) with `RecipeBits` flags (`REVERSIBLE`, `DELETE_ALL_OTHER_RECIPES`, `DONT_UNIFY_OUTPUT`, …).
- **Machine hulls are crafted in the 3×3 grid** with tier-resolved components: `GTModHandler.addMachineCraftingRecipe(result, recipe, machineTier)` (GTModHandler.java#L662-L690) substitutes the `MTEBasicMachineWithRecipe.X` placeholders — `CIRCUIT`, `BETTER_CIRCUIT`, `HULL`, `PISTON`, `WIRE`, `COIL_HEATING`, `ROTOR`, `MOTOR`, `SENSOR`, … (enum at MTEBasicMachineWithRecipe.java#L597-L621) — for the tier-matching ore-dict part. Example: the LV Forge Hammer hull is `WAW` hull/piston/circuit/wire + double heating coil + `craftingAnvil` (MTERecipeLoader.java#L112-L119).
- **GT tools in the grid:** the hard hammer, wrench and screwdriver are used as crafting-table ingredients (e.g. the `craftingAnvil` slot in the recipe above; GT tool items from `MetaGeneratedTool`). GT's recipe types implement `IGTCraftingRecipe` with an `isRemovable()` flag that drives GT's recipe-removal feature (GTModHandler.java#L1407, #L1465, #L1543, #L1583). Whether a tool placed in the grid is consumed is a GT5 crafting-table behavior implemented outside the GT5 recipe classes (coremod/patch territory) — **inferred: tools are not consumed (classic GT5 gameplay); verify in the T4 real-pack run.**
- **Our convention (per [0003](../../design/0003-gtnh-layer-and-tier-ceiling.md) §2):** ULV/LV items whose final recipe is the crafting table use GT parts (plates, wooden parts, dyes, redstone parts) as grid ingredients and a GT tool (hard hammer) where a shape demands one; machine-tier items use the assembler, not the crafting table. No crafting recipe may let an item be obtained before its tier (the input that gates the tier is named per row in [tier-map.md](tier-map.md)).

## Sources

- GT5-Unofficial `5.09.54.265`: `gregtech/api/recipe/RecipeMaps.java` (all identifiers), `gregtech/api/util/GTRecipeBuilder.java#L173,#L1201-L1210`, `gregtech/api/util/GTModHandler.java#L626-L690,#L1407`, `gregtech/api/util/GTShapedRecipe.java#L14-L16`, `gregtech/api/util/GTOreDictUnificator.java#L45`, `gregtech/api/enums/OrePrefixes.java`, `gregtech/api/enums/Materials.java`, `gregtech/api/metatileentity/implementations/MTEBasicMachineWithRecipe.java#L597-L621`, `gregtech/loaders/load/MTERecipeLoader.java#L112-L119`.
