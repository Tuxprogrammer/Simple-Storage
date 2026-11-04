# 0002 · Target pack: GTNH 2.9.0

- Status: Accepted (owner accepts by default for the planning phase)
- Gate: none (foundation)
- Amends: none
- Date: 2026-11-02

## Context

Integration must be written against what the pack actually ships ([gtnh-integration-research](../../.claude/skills/gtnh-integration-research/SKILL.md)). This is a backport of Tom's Simple Storage, which targets NeoForge and integrates with three recipe viewers (JEI/REI/EMI) and Jade; the 1.7.10 pack has exactly one of each (NEI, Waila), so the pins here determine the shape of every `compat` package. The owner's Lost Cities backport (same owner, same pack line) targets 2.9.0, and our buildscript is already pinned to `minecraftVersion = 1.7.10`, `forgeVersion = 10.13.4.1614` ([gradle.properties](../../gradle.properties)).

Spike S3 verified the current stable from three sources on 2026-11-02: the GTNH download index (`versions.json` lists 2.9.0 as the "Stable release", 2026-10-31), the pack source manifest (`releases/manifests/2.9.0.json`, `last_updated 2026-10-31T12:04:06Z`, the newest stable in DreamAssemblerXXL), and the CurseForge project page (main file GT-New-Horizons-2.9.0, "Latest release"). The 2.9.0 server pack was downloaded and extracted to git-ignored `reference/gtnh-pack/`; it ships `forge-1.7.10-10.13.4.1614-1.7.10-universal.jar`, confirming the Forge pin. Full citations: [docs/new-spec/00-pack-integration/pack-version.md](../new-spec/00-pack-integration/pack-version.md).

## Decision

1. **The target is GTNH 2.9.0**, as listed in [`releases/manifests/2.9.0.json`](https://github.com/GTNewHorizons/DreamAssemblerXXL/blob/master/releases/manifests/2.9.0.json) of [GTNewHorizons/DreamAssemblerXXL](https://github.com/GTNewHorizons/DreamAssemblerXXL): Minecraft 1.7.10, Forge 10.13.4.1614, GT5-Unofficial 5.09.54.265, GTNHLib 0.11.54.
2. **Dependencies are pinned to the versions 2.9.0 ships**, using GTNH Maven coordinates with the `:dev` classifier: `api` for GTNHLib, `compileOnly` for optional integrations (NEI 2.8.171-GTNH, AE2 rv3-beta-1095-GTNH, Waila 1.19.34, InGame-Info-XML 2.9.8, BetterQuesting 3.8.92-GTNH, EnhancedLootBags 1.3.5, Chisel 2.17.34-GTNH). GT5-Unofficial 5.09.54.265 is also `compileOnly` for the compile classpath but is a hard runtime dependency (`required-after:gregtech`, D17), not optional. The full table with configurations is in [docs/new-spec/00-pack-integration/integration-opportunities.md](../new-spec/00-pack-integration/integration-opportunities.md) § Recommended dependencies.
3. **The reference pack lives in git-ignored `reference/gtnh-pack/`**: the 2.9.0 server pack zip (from `downloads.gtnewhorizons.com/ServerPacks/`), the manifest, and the extracted tree. The MOD-INDEX of all 243 manifest mods is [docs/new-spec/00-pack-integration/shipped-mods.md](../new-spec/00-pack-integration/shipped-mods.md).
4. **The real-pack check uses the same version.** Alpha and beta gates run the release jar on a GTNH 2.9.0 dedicated server (shipped `server.properties` default: classic RWG, `level-type=rwg`) and client (default world type: RWG2 Continents, whitelist `flat`/`RWG`/`RWG2_CONTINENTS` per the shipped `config/defaultworldgenerator.cfg`), covering both overworlds.

## Consequences

- M01 applies the pin table to `dependencies.gradle`; a pack bump is a deliberate change: update this record, the pins, the reference pack, and re-run the real-pack gate.
- The buildscript's blowdryer tag (`gtnh.settings.blowdryerTag = 0.2.2` in gradle.properties) is independent of the pack version and stays as-is unless the buildscript itself moves.
- Because NEI is the only recipe viewer and Waila the only block-info mod, upstream's three-way JEI/REI/EMI choice collapses to one NEI plugin (`NEI*Config implements IConfigureNEI`, auto-discovered) and upstream's Jade plugin maps to the Waila `IWailaBlock`/`IWailaRegistrar` surface (mapping table in [integration-opportunities.md](../new-spec/00-pack-integration/integration-opportunities.md)).
- The server pack's 216 jars all match manifest entries, so the MOD-INDEX is the ground truth for versions; per-mod mod IDs and licenses are indexed by the owner's Lost Cities backport (`reference/gtnh/2.9.0/MOD-INDEX.md` there, same pack/tag) and are reused, not duplicated.

## Alternatives considered

- **GTNH dev/daily branch** (`daily.json` manifest): closest to the next release, but APIs churn under the integration work; the daily's versions are not what the owner plays.
- **An older stable (2.8.x):** what some of the pack's neighbors pinned before, but 1.7.10-2.9.0 is what the owner runs and what Lost Cities already verified; reusing a 2.8.x reference pack would force a second version line.
- **Tracking the next release (2.10):** premature; 2.9.0 shipped 2026-10-31, one day before this spike.

## Still open

None — both former open items are settled:

- Jar-shipped BetterQuesting quest line: **yes**, the mod ships its own quest line in the jar (decision log D12).
- Ghost-drag in NEI: **beta candidate**, not alpha scope (decision log D11).
