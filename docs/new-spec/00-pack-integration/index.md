# 00 · Pack integration

Where Simple Storage plugs into the GT New Horizons pack: which pack version we build against, what the pack ships, and the integration surfaces a storage mod touches — the recipe viewer, the questbook, loot, block info and config conventions. The other new-spec areas build on these facts.

Status: *research notes* (spike S3). Facts about the pack are *verified* against the release manifest, the GTNH download index, the CurseForge page and the downloaded server pack (checked 2026-11-02); the design decisions that follow from them belong to the S2 lane and design records 0003/0004.

## Contents

| File | Covers |
|---|---|
| [pack-version.md](pack-version.md) | S3-Q1: the target pack version (GTNH 2.9.0), the Forge/MC pins, world types and server-pack facts, with sources |
| [shipped-mods.md](shipped-mods.md) | S3-Q2: the MOD-INDEX — every mod in the pack (243 manifest entries) with its version and why it matters to us |
| [integration-opportunities.md](integration-opportunities.md) | S3-Q3/Q4: the NEI plugin API, BetterQuesting, loot, Waila/IIGX, config conventions, upstream's JEI/REI/EMI/Jade integrations, the mapping table and recommended dependency coordinates |

## Key facts

- **Pack version: GTNH 2.9.0** (stable, released 2026-10-31), checked 2026-11-02 from the GTNH download index, the DreamAssemblerXXL manifest and CurseForge.
- **Minecraft 1.7.10, Forge 10.13.4.1614** — confirmed from the shipped server pack (`forge-1.7.10-10.13.4.1614-1.7.10-universal.jar`).
- **GregTech 5.09.54.265, GTNHLib 0.11.54** per the 2.9.0 manifest.
- **World types:** RWG2 Continents (client default), classic RWG (dedicated-server default), flat.
- **Recipe viewer is NEI** (2.8.171-GTNH, plugin classes auto-discovered as `NEI*Config implements IConfigureNEI`); **no JEI/REI/EMI, no Jade** — Waila 1.19.34 + InGame-Info-XML 2.9.8 are the block-info stack.
- The reference pack lives in git-ignored `reference/gtnh-pack/` (server pack zip + manifest).

## Related areas

- Design record [0002-target-pack](../../design/0002-target-pack.md) accepts the target version and pins.
- S2 lane: upstream behavior spec (which integrations map to what) and the `compat` package layout.
- M01 applies the dependency pins from [integration-opportunities.md](integration-opportunities.md) to `dependencies.gradle`.
