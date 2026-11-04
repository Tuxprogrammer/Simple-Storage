# GT tiers and energy (verified, GTNH 2.9.0 / GT5-Unofficial 5.09.54.265)

Answer to S2-Q1 (the verified tier table behind [0003](../../design/0003-gtnh-layer-and-tier-ceiling.md)) and S2-Q2 (the EU model behind [0004](../../design/0004-energy-and-machine-model.md)). All facts verified 2026-11-03 from GT5-Unofficial source at the pack's tag, shallow-cloned to git-ignored `reference/src/GT5` (tag `5.09.54.265`); line numbers refer to that checkout.

## The verified tier table

Tier names, voltages and the tier lookup all come from `gregtech/api/enums/GTValues.java`:

- Voltage array `GTValues.V` (GTValues.java#L92-L95): 16 entries, index 0 = 8 V through index 15 = 8 589 934 592 V.
- Short names `GTValues.VN` (GTValues.java#L134-L150) and long names `GTValues.VOLTAGE_NAMES` (GTValues.java#L155-L171) — same index order.
- Tier lookup `GTUtility.getTier(long)` (GTUtility.java#L443-L456): `ceil(log2(voltage))/2` mapping, so `getTier(8)=0 (ULV)`, `getTier(32)=1 (LV)`, `getTier(512)=3 (HV)`.

| Tier index | Name | Voltage (V) | Machine rate at 1 A (EU/t) |
|---|---|---|---|
| 0 | ULV (Ultra Low Voltage) | 8 | 8 |
| 1 | LV (Low Voltage) | 32 | 32 |
| 2 | MV (Medium Voltage) | 128 | 128 |
| 3 | HV (High Voltage) | 512 | 512 |
| 4 | EV (Extreme Voltage) | 2048 | 2048 |
| 5 | IV (Insane Voltage) | 8192 | 8192 |
| 6 | LuV (Ludicrous Voltage) | 32768 | 32768 |
| 7 | ZPM (ZPM Voltage) | 131072 | 131072 |
| 8 | UV (Ultimate Voltage) | 524288 | 524288 |
| 9 | UHV (Ultimate High Voltage) | 2097152 | 2097152 |
| 10 | UEV (Ultimate Extreme Voltage) | 8388608 | 8388608 |
| 11 | UIV (Ultimate Insane Voltage) | 33554432 | 33554432 |
| 12 | UMV (Ultimate Mega Voltage) | 134217728 | 134217728 |
| 13 | UXV (Ultimate Extended Mega Voltage) | 536870912 | 536870912 |
| 14 | MAX (Maximum Voltage) | 2147483640 (`Integer.MAX_VALUE-7`) | 2147483640 |
| 15 | MAX+ | 8589934592 | (error tier; "not really a real tier", GTValues.java#L94) |

**Correction to [0003](../../design/0003-gtnh-layer-and-tier-ceiling.md):** the original 0003 draft wrote the HV cap as **128V**, a slip — 128V is **MV** in this build. The voltages above are what classic GT5 already had (ULV=8, LV=32, MV=128, HV=512, EV=2048, IV=8192, LuV=32768, ZPM=131072, UV=524288), so the verified cap is **HV = 512V** and the tier *order* is the one [0003](../../design/0003-gtnh-layer-and-tier-ceiling.md) assumed. Do not trust GT5 numbers from memory or old snippets: the 128V slip is exactly the trap the gtnh-integration-research skill warns about.

**Practical voltage for recipes:** `GTValues.VP` (GTValues.java#L97-L108) is `V × 30/32` — the "recipe voltage you should use if you expect the recipe to use a full amp of that tier" (headroom for cable/transformer loss).

## Machine rates and voltage validation

- Machine base rate: `OverclockCalculator().setRecipeEUt(recipe.mEUt).setEUt(V[mTier] * mAmperage)` (MTEBasicMachine.java#L747-L751, `gregtech/api/metatileentity/implementations/MTEBasicMachine.java`). A tier-N machine at 1 A draws **V[N] EU/t**; overclocking multiplies rate and halves time (standard GT overclock math, same calculator).
- Recipe-voltage validation on item insertion: the recipe query is built with `.voltage(V[this.mTier])` (MTEBasicMachineWithRecipe.java#L336 and #L355) — a recipe with a higher `voltage()` requirement does not match a lower-tier machine.
- Max amperage per tier: `GTValues.AatV` (GTValues.java#L129-L130) keeps `V × amps < Integer.MAX_VALUE-7`.
- **Machine tier floor:** every single-block machine built by `MTEBasicMachineWithRecipeBuilder` (hammer, assembler, bender, lathe, circuit assembler, macerator, ore washer, mixer, etc. — 465 machine builder configurations in `gregtech/loaders/preload/LoaderMetaTileEntities.java`) is registered with `setTier(1)` through `setTier(12)` (LV→UMV); **there is no `setTier(0)` single-block EU machine** (the pre-EU tier is steam machines, e.g. `MTESteamForgeHammer*`, and the primitive blast furnace). Circuit assemblers additionally exist to tier 14. Consequence for [0003](../../design/0003-gtnh-layer-and-tier-ceiling.md): **LV (32 EU/t) is the cheapest machine a player can run**, so "reachable at LV or below" means "crafting table, or an LV machine".

## The EU model in 1.7.10 GTNH

- **EU is GT's native integer energy unit**, stored per MetaTileEntity (`BaseMetaTileEntity#addEnergy`, BaseMetaTileEntity.java#L2003, `gregtech/api/metatileentity/BaseMetaTileEntity.java`).
- **Consumers:** machines drain `V[tier] × amps` EU/t per tick while processing (MTEBasicMachine.java#L608, #L726-L727 `drainEnergyForProcess`); multiblocks, pumps, drones and other GT blocks do the same through the same base classes.
- **Producers (where EU comes from):** GT's own generators — `gregtech/common/tileentities/generators/`: `MTESteamTurbine`, `MTEGasTurbine`, `MTEDieselGenerator`, `MTEPlasmaGenerator`, `MTESolarGenerator`, `MTENaquadahReactor`, `MTEMagicEnergyConverter`, `MTEMagicalEnergyAbsorber`, plus small dynamos (e.g. the RTG basic generator, `gt.blockmachines.basicgenerator.rtg` lang). Non-GT pack generators (IC2, solar panel mods, etc. — see [shipped-mods.md](shipped-mods.md)) bridge into the same EU flow.
- **Distribution:** GT's energy-node graph (`gregtech/api/graphs/paths/PowerNodePath.java`, energy-node classes in `gregtech/api/graphs/`) routes EU from generators to consumers through cables at the cable's voltage tier; **transformers** step voltage between tiers (`MTETransformer`, MTETransformer.java#L46). A machine only accepts EU at its own tier voltage (the `.voltage(V[mTier])` recipe check above is the recipe side; the energy side is the same tier match).
- **Our mod's position ([0004](../../design/0004-energy-and-machine-model.md)):** our blocks are power-free utility blocks at alpha — no EU buffer, no per-operation cost. The optional M05 EU sink, if it lands, is the **level emitter as a fixed consumer at the ULV rate: 8 EU/t while active** (V[0], 1-A equivalent), behind a config flag, off by default. Rationale: the emitter is a redstone utility, not a machine; charging it at its HV item tier (512 EU/t) would make the sink a machine, which 0004 explicitly avoids. (Inferred design choice, recorded as decision D7.)

## What the energy decision means for this mod

| Block | Energy at alpha | Optional sink (M05, off by default) |
|---|---|---|
| All terminals, storage blocks, cables, connectors, interface, proxy, hopper, filters, wireless items, configurator | none (power-free, [0004](../../design/0004-energy-and-machine-model.md) §1) | none |
| Level Emitter | none | 8 EU/t (ULV rate) while powered |

The dev self-check for M05 (per [0003](../../design/0003-gtnh-layer-and-tier-ceiling.md) §3-4): for a machine-recipe row, the final-recipe machine tier **equals** the item's tier (assembler at that tier) and at least one input (the circuit of that tier) is unobtainable before that tier; ULV crafting-table rows have no machine and no tier-gating input. No recipe may use a machine or input above the item's tier, and no output may exceed the HV cap (512V). The optional sink must read the tier table from `GTValues.V`, never hard-coded voltages.

## Sources

- GT5-Unofficial `5.09.54.265` (pack tag per [pack-version.md](pack-version.md)): `gregtech/api/enums/GTValues.java#L92-L150` (tier arrays), `gregtech/api/util/GTUtility.java#L443-L461` (tier lookup), `gregtech/api/metatileentity/implementations/MTEBasicMachine.java#L608,#L726-L751` (rate and drain), `gregtech/api/metatileentity/implementations/MTEBasicMachineWithRecipe.java#L336,#L355` (recipe voltage check), `gregtech/loaders/preload/LoaderMetaTileEntities.java` (machine tier range; 465 machine builder configurations, none at tier 0), `gregtech/api/metatileentity/BaseMetaTileEntity.java#L2003`, `gregtech/api/metatileentity/implementations/MTETransformer.java#L46`, `gregtech/common/tileentities/generators/*` (producers).
