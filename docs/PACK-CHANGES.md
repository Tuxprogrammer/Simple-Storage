# Pack-side changes

<!-- Every change this mod needs outside its own jar.
     Paths are relative to the pack's .minecraft/ folder; take them from the real pack's config/, not the other mod's defaults.
     This mod never writes another mod's config at runtime. -->

Target pack version: GTNH 2.9.0 ([0002](design/0002-target-pack.md), S3-verified). Filled by S2 (2026-11-03); questbook/loot/config rows follow [new-spec/00-pack-integration/tier-map.md](new-spec/00-pack-integration/tier-map.md) and S3's [integration-opportunities.md](new-spec/00-pack-integration/integration-opportunities.md) (BQ 3.8.92-GTNH, EnhancedLootBags 1.3.5, TooMuchLoot 4.3.0-GTNH, GTNHLib 0.11.54, Waila 1.19.34, IIGX 2.9.8).

## Questbook (BetterQuesting 3.8.92-GTNH)

Delivery decision (D12): the mod **ships its own quest line in the jar** via the BQ importer API (`betterquesting.api.client.importers.IImportRegistry#registerImporter`, S3-verified) so the content is self-contained; the pack may override it from the pack's quest repo if preferred. The pack already carries one storage-themed line ("Storing and Transferring…", S3) — our line complements it (Simple Storage is a different system from the pack's pipe/logistics storage) rather than duplicating it.

Suggested quest lines, one per tier band of the tier map:

| Quest line (jar-shipped) | Quests (items, in tier order) | Why this order |
|---|---|---|
| "Storing Things Simply" (ULV→LV) | 1. craft Open Crate → 2. craft Inventory Trim → 3. craft Filing Cabinet → 4. craft Storage Terminal → 5. craft Item Filter + Tag Item Filter → 6. craft Inventory Configurator → 7. (optional) craft Paint Kit | The LV-or-below band: first ingots → a terminal on a plain chest → filters. Teach the cabinet's single-item-type lock (quest text) before the network. |
| "The Inventory Network" (LV, plus one MV quest) | 1. Inventory Cable → 2. Framed Inventory Cable → 3. Inventory Cable Connector (+Framed) → 4. Inventory Connector → 5. Inventory Proxy → 6. Inventory Interface → 7. Basic Inventory Hopper → 8. Wireless Terminal (the one MV quest) | The LV band (D18): wire a two-chest network first (quest: "connect two inventories and open them from one terminal"), then the hopper in/out; the basic Wireless Terminal is the single MV step, a mid-game reward for the MV circuit. |
| "Advanced Storage" (HV) | 1. Crafting Terminal → 2. Advanced Wireless Terminal → 3. Level Emitter → 4. Polymorphic Item Filter | The HV top of the band: crafting terminal + auto-fill, bound remote access, redstone automation, component-matching filters. |

Quest mechanics notes: use BQ's standard item-quest type (no jar-quest needed for item crafting); the "connect two inventories" quest needs a BQ quest trigger the pack already supports (S3: BQ 3.8.92-GTNH quest JSON under `config/betterquesting/DefaultQuests/`); keep quest IDs namespaced `simplestoragenh.*` so pack overrides are a drop-in.

## Loot

| Source (S3-verified) | Suggested content | Why |
|---|---|---|
| EnhancedLootBags 1.3.5 (ULV–EV tiered bags) | ULV bags: Open Crate, Inventory Trim. LV bags: Filing Cabinet, Item Filter, Storage Terminal, Inventory Cable, Inventory Connector, Inventory Proxy, Inventory Interface, Basic Inventory Hopper. MV bags: Wireless Terminal. HV bags: Crafting Terminal, Level Emitter. | The bag tiers line up 1:1 with our tier band; a player pulling an LV bag finds a ready-to-wire network part (inferred placement, pack tunes the odds). |
| LootGames 2.2.14 (seed-determined dungeons) | Filing Cabinet and Open Crate in dungeon chests; Inventory Connector + cable in dungeon "workshop" rooms. | Dungeon storage should be the early tier, not the network (inferred). |
| TooMuchLoot 4.3.0-GTNH (16 vanilla loot categories from XML at server start) | **No default change from this mod.** Server owners who want chests to contain storage can add `filing_cabinet` to a dungeon category in TML's XML — documented here, not shipped by the mod. | Keeps the mod jar free of vanilla loot edits (house rule: pack changes live in the pack). |

## Configs (GTNHLib 0.11.54)

One config file, `config/simplestoragenh.cfg` (GTNHLib `@Config` annotation, `modid = "simplestoragenh"`, `filename = "simplestoragenh"` — GTNHLib `Config.java#L14`, S3 config conventions). Upstream's two-file layout (`toms_storage-common.toml` + `toms_storage-server.toml`, 01-core `config.md`) collapses to one file with categories in 1.7.10; per-world keys use GTNHLib `@Config.Sync` where a client must see them (deviation recorded, decision D16). The pack's automatic config screen renders it for free.

| Category / key | Default | What it does |
|---|---|---|
| `modules.storage` | `true` | storage blocks (cabinet, crate, trims) — module flag, gates behavior not registration |
| `modules.terminal` | `true` | storage + crafting terminals |
| `modules.network` | `true` | cables, cable connectors, inventory connectors, proxy, interface, hopper |
| `modules.wireless` | `true` | wireless + advanced wireless terminals |
| `modules.integration.nei` | `true` | NEI plugin (only meaningful with NEI loaded) |
| `modules.integration.waila` | `true` | Waila providers (only with Waila loaded) |
| `modules.integration.appeng` | `false` | AE2 storage compat (M05; flag registered in M01, behavior built in M05; off at alpha per [machine-model.md](new-spec/00-pack-integration/machine-model.md) D10 and D14) |
| `euSink.enabled` | `false` | the 0004 optional EU sink ([0004](design/0004-energy-and-machine-model.md) §3, decision D7) |
| `euSink.euPerTick` | `8` | sink rate (ULV rate, `GTValues.V[0]` — [gtnh-tiers-and-energy.md](new-spec/00-pack-integration/gtnh-tiers-and-energy.md)) |
| `network.inventoryConnectorRange` | `16` (4–256) | port of upstream `inventoryConnectorRange` (01-core `config.md`) |
| `network.onlyTrimsConnect` | `false` | port of upstream `onlyTrimsConnect` |
| `network.invCableConnectorMaxScanSize` | `2048` | port of upstream `invCableConnectorMaxScanSize` |
| `wireless.reach` | `16` (4–64) | port of upstream `wirelessReach` (basic wireless terminal) |
| `wireless.advRange` | `64` (16–512) | port of upstream `advWirelessRange` |
| `wireless.beaconLvl` / `beaconLvlDim` | `1` / `4` (range -1–4) | ports of upstream `wirelessTermBeaconLvl*` (-1 disables, 0 = one beacon suffices) |
| `network.linkBeaconLvl` / `linkBeaconRange` / `linkBeaconLvlSameDim` / `linkBeaconLvlCrossDim` | `0` / `4096` / `1` / `2` | ports of upstream `invLinkBeacon*` (cable-connector linking) |
| `behavior.runMultithreaded` | `true` | port of upstream `runMultithreaded` |
| `behavior.basicHopperCooldown` | `10` (1–200) | port of upstream `basicHopperCooldown` |
| `behavior.blockedMods` / `blockedBlocks` | `[]` | ports of upstream COMMON keys (connector skips) |

Pack-side notes: the pack ships pre-configured `config/` (195 files, S3) — no shipped override is needed for our mod at alpha; if the pack wants the EU sink on for its servers, that is a one-line pack config (`euSink.enabled=true`), recorded here rather than hardcoded.

## Other pack-side touches (optional, not required for alpha)

| Surface | Change | Why |
|---|---|---|
| Waila 1.19.34 | none — IMC `register` at runtime from our jar (S3-verified `IWailaRegistrar` contract) | our Waila providers need no pack file |
| InGame-Info-XML 2.9.8 | optional per-block info pages (cabinet, terminal, emitter) as pack-side XML | richer tooltips than Waila lines; nice-to-have |
| GuideNH 1.3.55 | optional mod guide page | discoverability in the in-game guide |
| NEI 2.8.171-GTNH | none required (plugin auto-discovered, [recipe-viewer.md](new-spec/00-pack-integration/recipe-viewer.md)); the pack's `config/NEI/handlerordering.csv` *may* order our handlers relative to GT's `gtneioreplugin` | avoids recipe-panel overlap only if the pack cares about ordering |
| Key bindings | none shipped: the advanced wireless terminal's open key ships unbound (D28), so no pack key file is needed | avoids silent clashes |
| BetterQuesting | none if the jar-shipped line is accepted (D12); otherwise the three quest lines above move to the pack's quest repo | delivery choice |

## Pack-wide issues found

Shipped values that break something for everyone, not just this mod. Report separately.

| File | Value | Effect |
|---|---|---|

None yet.
