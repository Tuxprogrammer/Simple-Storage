# 0003 · GTNH layer and tier ceiling

- Status: Accepted
- Gate: none (foundation)
- Amends: none
- Date: 2026-11-02

## Context

The port must live in GTNH's progression, not beside it. Upstream's storage content is vanilla-only: no own materials, no energy, no machine processing. GT-first means the recipes go through GT's recipe maps and the parts come from GT materials, while the mod-specific items — the terminal, the storage blocks and items, the filters, the configurator, the cable and connector blocks, the wireless items — stay the mod's own content.

Storage is an early-game need: players want a storage network soon after their first ingots. But it is also a simple utility, and the pack may already carry a full storage solution. The content therefore needs a floor (reachable early) and a named ceiling (a tier above which nothing the mod makes can be obtained), so it does not fight the pack's heavier storage.

## Decision

1. **No own materials.** Every part is made from GT materials and parts through ore-dict unification. The only mod-specific items are this mod's own blocks and items; nothing is registered that GT already provides.
2. **Recipes in the GT recipe maps.** Machine processing goes through GT's recipe maps with the builder, at the EU/t of the item's tier. Crafting-table recipes follow pack conventions: GT tools in the grid, GT parts as ingredients. No recipe may let an item be obtained before its tier.
3. **Tier band: the early game up to HV.**
   - Early-game items (the basic storage block and item, the basic terminal, the filters, the configurator) are reachable at LV or below, right after first ingots;
   - The network parts (cables, connectors, inventory connector, proxy, interface, basic hopper) are also reachable at LV or below (D18); the basic wireless terminal sits at MV;
   - The advanced items (the crafting terminal, the advanced wireless terminal, the level emitter) sit at HV;
   - Nothing the mod outputs is above **HV** (512V — *refined by S2*: the original draft wrote 128V, a slip (128V is MV); this GT build has LV=32V, MV=128V, HV=512V; verified table in [new-spec/00-pack-integration/gtnh-tiers-and-energy.md](../new-spec/00-pack-integration/gtnh-tiers-and-energy.md)). The cap is named here so M05's dev self-check can enforce it.
4. **The per-item tier map is M05's.** This record fixes the band and the rule; the per-item map is written when S2 refines it and M05 lands the recipes. Every item gets exactly one tier: for a machine-recipe row, the final-recipe machine tier **equals** the item's tier (assembler at that tier) and at least one input (the circuit of that tier) is unobtainable before that tier; ULV crafting-table rows have no machine and no tier-gating input. No recipe may use a machine or input above the item's tier, and no output may exceed the HV cap (512V). *Written by S2: [new-spec/00-pack-integration/tier-map.md](../new-spec/00-pack-integration/tier-map.md) — 22 rows, all within the band; the "basic terminal" of §3 is the storage terminal (LV), the "main terminal" of the MV–HV band is the network-side terminal set, and the crafting terminal tops out at HV (decision D13).*

## Consequences

- The new-spec `00-pack-integration` area carries the tier map ([new-spec/00-pack-integration/tier-map.md](../new-spec/00-pack-integration/tier-map.md), written by S2), settled as Q1 in [OPEN-QUESTIONS](../new-spec/OPEN-QUESTIONS.md) (decisions D13/D15; D18 amends D13: network parts LV, only the basic wireless terminal MV).
- The dev self-check (roadmap M01, run from M05) fails on any recipe whose output is above the cap or that uses an input or machine above its output's tier. Machine tier floors are verified in [gtnh-tiers-and-energy.md](../new-spec/00-pack-integration/gtnh-tiers-and-energy.md) (LV is the cheapest machine, 32 EU/t; every basic machine exists at tiers 1–12).
- Pack-side delivery (questbook entries, loot) follows the tier map and is recorded in [PACK-CHANGES.md](../PACK-CHANGES.md) from S3.

## Alternatives considered

- **Faithful vanilla recipes.** Rejected: a parallel progression beside GT's is exactly what GT-first exists to avoid.
- **A higher ceiling (EV).** Rejected at this stage: the content would stretch to the late game, exactly where the pack's AE2 already takes over storage (D14), and the advanced items would arrive after the early/mid game when a simple network is most needed. Revisit in beta if the content feels cheap.
- **No ceiling.** Rejected: the advanced items would reach ZPM and IV and compete with the pack's full storage solution for the same job.

## Still open

- (S2, 2026-11-03: Q1 settled — the tier map is written in [new-spec/00-pack-integration/tier-map.md](../new-spec/00-pack-integration/tier-map.md), cap confirmed as HV = 512V in this GT build, decision D13/D15, amended by D18 (network parts at LV, only the basic wireless terminal at MV). Remaining: M05 lands the recipes against it and the dev self-check enforces it.)
