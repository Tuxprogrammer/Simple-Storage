# 00 · Pack integration

Where Simple Storage plugs into the GT New Horizons pack: which pack version we build against, what the pack ships, and the integration surfaces a storage mod touches — the recipe viewer, the questbook, loot, block info and config conventions. The other new-spec areas build on these facts.

Status: *research notes* (spikes S3 + S2). Facts about the pack are *verified* against the release manifest, the GTNH download index, the CurseForge page and the downloaded server pack (checked 2026-11-02); S2's sheets verify GT/NEI/GTNHLib facts against source at the pack's tags (checked 2026-11-03). The design decisions that follow from them belong to the S2 lane and design records 0003/0004 (refined in place by S2).

## Contents

| File | Covers |
|---|---|
| [pack-version.md](pack-version.md) | S3-Q1: the target pack version (GTNH 2.9.0), the Forge/MC pins, world types and server-pack facts, with sources |
| [shipped-mods.md](shipped-mods.md) | S3-Q2: the MOD-INDEX — every mod in the pack (243 manifest entries) with its version and why it matters to us |
| [integration-opportunities.md](integration-opportunities.md) | S3-Q3/Q4: the NEI plugin API, BetterQuesting, loot, Waila/IIGX, config conventions, upstream's JEI/REI/EMI/Jade integrations, the mapping table and recommended dependency coordinates |
| [gtnh-tiers-and-energy.md](gtnh-tiers-and-energy.md) | S2: the verified GT tier table (all 16 tiers, voltages, EU/t), the 1.7.10 EU model, and what the energy decision means for this mod (0003/0004 refinement) |
| [recipes-and-materials.md](recipes-and-materials.md) | S2: which GT recipe maps this mod's recipes go into (verified identifiers), material/ore-dict conventions, the crafting-table convention |
| [machine-model.md](machine-model.md) | S2: what "machine" means in 1.7.10 GTNH — power-free utility blocks vs GT MetaTileEntities, block-entity conventions, the capability/inventory-access surface for the terminals |
| [recipe-viewer.md](recipe-viewer.md) | S2: NEI 2.8.171-GTNH plugin API specifics (verified classes, AE2 precedent) and the upstream JEI/REI/EMI/Jade → NEI/Waila mapping (REI/EMI dropped, Jade → Waila) |
| [tier-map.md](tier-map.md) | S2: THE per-item tier map for M05 — all 22 blocks/items, one tier each within the 0003 band, final-recipe machine + map, tier-gating input |

## Key facts

- **Pack version: GTNH 2.9.0** (stable, released 2026-10-31), checked 2026-11-02 from the GTNH download index, the DreamAssemblerXXL manifest and CurseForge.
- **Minecraft 1.7.10, Forge 10.13.4.1614** — confirmed from the shipped server pack (`forge-1.7.10-10.13.4.1614-1.7.10-universal.jar`).
- **GregTech 5.09.54.265, GTNHLib 0.11.54** per the 2.9.0 manifest.
- **World types:** RWG2 Continents (client default), classic RWG (dedicated-server default), flat.
- **Recipe viewer is NEI** (2.8.171-GTNH, plugin classes auto-discovered as `NEI*Config implements IConfigureNEI`); **no JEI/REI/EMI, no Jade** — Waila 1.19.34 + InGame-Info-XML 2.9.8 are the block-info stack.
- The reference pack lives in git-ignored `reference/gtnh-pack/` (server pack zip + manifest).

## Related areas

- Design record [0002-target-pack](../../design/0002-target-pack.md) accepts the target version and pins.
- [Upstream behavior spec](../../original-spec/README.md) (written by S1; the S2 and S3 spikes are done) says which integrations map to what; the `compat` package layout is in [recipe-viewer.md](recipe-viewer.md) §What M06 builds.
- M01 applies the dependency pins from [integration-opportunities.md](integration-opportunities.md) to `dependencies.gradle`.
