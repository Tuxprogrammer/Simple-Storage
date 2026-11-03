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
3. **Tier band: the early game up to EU (HV).**
   - Early-game items (the basic storage block and item, the basic terminal, the filters, the configurator) are reachable at LV or below, right after first ingots;
   - The network parts (cables, connectors, interface) and the main terminal sit at MV to HV;
   - The advanced items (the crafting terminal, the advanced wireless terminal, the level emitter) sit at HV;
   - Nothing the mod outputs is above **EU (HV)**. The cap is named here so M05's dev self-check can enforce it.
4. **The per-item tier map is M05's.** This record fixes the band and the rule; the per-item map is written when S2 refines it and M05 lands the recipes. Every item gets exactly one tier, gated by both the minimum machine voltage of its final recipe and at least one input material unobtainable before that tier.

## Consequences

- The new-spec `00-pack-integration` area carries the tier map, and [OPEN-QUESTIONS](../new-spec/OPEN-QUESTIONS.md) Q1 tracks the confirmation.
- The dev self-check (roadmap M01, run from M05) fails on any recipe whose output is above the cap or that uses an input or machine above its output's tier.
- Pack-side delivery (questbook entries, loot) follows the tier map and is recorded in [PACK-CHANGES.md](../PACK-CHANGES.md) from S3.

## Alternatives considered

- **Faithful vanilla recipes.** Rejected: a parallel progression beside GT's is exactly what GT-first exists to avoid.
- **A lower ceiling (EV).** Rejected at this stage: the advanced items would stop being useful in the late game, right where a storage network is most needed. Revisit in beta if the content feels cheap.
- **No ceiling.** Rejected: the advanced items would reach ZPM and IV and compete with the pack's full storage solution for the same job.

## Still open

- Q1: the exact per-item tier map, settled by S2 and built in M05.
