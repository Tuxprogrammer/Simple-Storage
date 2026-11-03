# Pack version: GTNH 2.9.0

Answer to S3-Q1: which GTNH pack version to target. Verified 2026-11-02 from three independent GTNH sources, plus the downloaded pack itself.

## Decision: target GTNH 2.9.0

- **Pack version:** GTNH **2.9.0** (released 2026-10-31).
- **Minecraft:** 1.7.10 · **Forge:** 10.13.4.1614 (confirmed from the shipped server pack, below).
- **GregTech:** GT5-Unofficial **5.09.54.265** (manifest `2.9.0.json`, key `GT5-Unofficial`).
- **GTNHLib:** **0.11.54** (manifest `2.9.0.json`, key `GTNHLib`).

## Evidence

1. **Official download index** [`https://downloads.gtnewhorizons.com/versions.json`](https://downloads.gtnewhorizons.com/versions.json) (fetched 2026-11-02): entry `"2.9.0"` is titled `"Stable release"` with `"releaseDate": "2026/10/31"` and download URLs for the MultiMC client (Java 8 and Java 17–26), the server pack and the client pack; the older entries (`2.9.0-RC-2`, `2.9.0-RC-1`, `2.9.0-beta-3`, …) are titled `"Beta release"`.
2. **Pack source manifest** [`releases/manifests/2.9.0.json`](https://github.com/GTNewHorizons/DreamAssemblerXXL/blob/master/releases/manifests/2.9.0.json) in [GTNewHorizons/DreamAssemblerXXL](https://github.com/GTNewHorizons/DreamAssemblerXXL): `"version": "2.9.0"`, `"last_updated": "2026-10-31T12:04:06Z"`. The manifests folder holds one manifest per release from 2.5.1 through 2.9.0 plus `daily.json`/`experimental.json`; 2.9.0 is the newest stable. Copied to git-ignored `reference/gtnh-pack/manifest-2.9.0.json`.
3. **CurseForge:** GT New Horizons (project ID 252507) lists the main file **GT-New-Horizons-2.9.0** as *"Latest release"* (Oct 31, 2026, 1.7.10, Forge) on [its project page](https://www.curseforge.com/minecraft/modpacks/gt-new-horizons). Confirmed 2026-11-02 via a search-engine cache of the live page: a direct fetch was bot-walled (Cloudflare "Just a moment…") and the CurseForge API `find` endpoint returned HTTP 403 to the public client IDs tried, so this is the one source not fetched directly.

## The pack as shipped (ground truth)

The official 2.9.0 server pack was downloaded 2026-11-02 and extracted to git-ignored `reference/gtnh-pack/`:

- URL: `https://downloads.gtnewhorizons.com/ServerPacks/GT_New_Horizons_2.9.0_Server_Java_17-26.zip` (from versions.json; 568,348,402 bytes).
- **Forge confirmed:** the pack ships `forge-1.7.10-10.13.4.1614-1.7.10-universal.jar` — Forge 10.13.4.1614, matching the buildscript pin.
- 216 mod jars in `mods/`; every one matches a manifest 2.9.0 entry by name or version (reconciliation details in [shipped-mods.md](shipped-mods.md)).
- The Java 17–26 server build starts via `startserver-java9.sh` + `java9args.txt`.
- Shipped `server.properties` defaults: `motd=GT:New Horizons 2.9.0`, `level-type=rwg`, `white-list=true`, `op-permission-level=2`, `allow-nether=true`, `online-mode=true`, `pvp=true`, `difficulty=3`, `gamemode=0`, `spawn-protection=1`, `max-players=20`, `view-distance=8`, `enable-command-block=true`, `spawn-npcs=true`, `generate-structures=true`.
- The zip also ships `changelog from 2.8.4 to 2.9.0.md`.

## World types

The pack offers three world types, per the shipped `config/defaultworldgenerator.cfg` (reference/gtnh-pack, DefaultWorldGenerator 0.5.0, client-side per manifest):

| World type | Where it appears |
|---|---|
| `RWG2_CONTINENTS` (RWG2 Continents) | Single-player default (`S:"World Generator"=RWG2_CONTINENTS`); whitelisted |
| `RWG` (classic RWG) | Whitelisted; dedicated-server default (`level-type=rwg` in the shipped `server.properties`) |
| `flat` | Whitelisted; a flat preset (layer list) is configured in the same cfg |

- `S:"World Generator Whitelist"` in the same cfg is exactly `flat`, `RWG`, `RWG2_CONTINENTS`.
- Classic RWG configs ship as `config/RWG.cfg` (biome IDs) and `config/RTG/` (`rtg.cfg` + 19 per-mod biome files; RTG is classic RWG's former name).
- Consequence for server setup notes: a real-pack dedicated server boots classic RWG unless `level-type` is set to `RWG2_CONTINENTS`; the client's Create World screen defaults to RWG2 Continents and offers only the three types above.

## What this means for the build

- The buildscript already pins `minecraftVersion = 1.7.10` and `forgeVersion = 10.13.4.1614` ([gradle.properties](../../../gradle.properties)) — no change needed.
- Dependency pins for M01: [integration-opportunities.md](integration-opportunities.md) § Recommended dependencies.
- The real-pack alpha/beta check runs the release jar against a GTNH 2.9.0 dedicated server and client, on both offered overworlds (RWG2 Continents and classic RWG).
