# Config key mapping (upstream to 1.7.10)

Maps every key in [original-spec config](../../original-spec/01-core/config.md) (`toms_storage@95bb61d`, `Config.java`) to the key in [PACK-CHANGES.md](../../PACK-CHANGES.md) § Configs. Layout is settled by [D16](../../design/decision-log.md): one file `config/simplestoragenh.cfg`, GTNHLib 0.11.54 `@Config` (`Config.java#L14`, `@Config.Sync` at `#L251`; [machine-model.md](../00-pack-integration/machine-model.md) § Config). PACK-CHANGES.md is not edited here; gaps are settled in [proposals.md](proposals.md#proposed-not-decided).

How to read: Status is `same` (key, default, range carried), `changed` (renamed or moved), `dropped` (no counterpart, deliberately), or `unmapped` (no PACK-CHANGES row). Side/sync: PACK-CHANGES says only that per-world keys use `@Config.Sync` "where a client must see them" and names none (PACK-CHANGES.md:33), so the column follows the accepted [P01-5](proposals.md#proposed-not-decided): only `wireless.reach` and `wireless.advRange` sync.

## Server keys (upstream `toms_storage-server.toml`, per world)

| Upstream key | 1.7.10 key | Category | Default | Range | Side/sync | Status | Bugs |
|---|---|---|---|---|---|---|---|
| `inventoryConnectorRange` | `network.inventoryConnectorRange` | network | 16 blocks | 4-256 | server; sync: no (P01-5, D29) | same | CORE-09 |
| `onlyTrimsConnect` | `network.onlyTrimsConnect` | network | false | n/a | server | same | CORE-09 |
| `invCableConnectorMaxScanSize` | `network.invCableConnectorMaxScanSize` | network | 2048 cables | PACK-CHANGES gives none; upstream 16-`Integer.MAX_VALUE` (Accepted, P01-4, D29) | server | same | CORE-09 |
| `wirelessReach` | `wireless.reach` | wireless | 16 blocks | 4-64 | client reads it for the terminal outline ([client-package](../../original-spec/06-client-ui/client-package.md)); @Config.Sync (P01-5, D29) | changed (name) | CORE-09 |
| `advWirelessRange` | `wireless.advRange` | wireless | 64 blocks | 16-512 | server; @Config.Sync (P01-5, D29) | changed (name) | CORE-09 |
| `wirelessTermBeaconLvl` | `wireless.beaconLvl` | wireless | 1 | -1 to 4 (-1 off, 0 = one beacon) | server | changed (name) | CORE-09 |
| `wirelessTermBeaconLvlDim` | `wireless.beaconLvlDim` | wireless | 4 | -1 to 4 | server | changed (name) | CORE-09 |
| `invLinkBeaconLvl` | `network.linkBeaconLvl` | network | 0 | PACK-CHANGES gives none; upstream -1 to 4 (P01-4, D29) | server | changed (name) | CORE-09 |
| `invLinkBeaconRange` | `network.linkBeaconRange` | network | 4096 blocks | PACK-CHANGES gives none; upstream 0-`Integer.MAX_VALUE` (0 off) (P01-4, D29) | server | changed (name) | CORE-09 |
| `invLinkBeaconLvlSameDim` | `network.linkBeaconLvlSameDim` | network | 1 | PACK-CHANGES gives none; upstream -1 to 4 (P01-4, D29) | server | changed (name) | CORE-09 |
| `invLinkBeaconLvlCrossDim` | `network.linkBeaconLvlCrossDim` | network | 2 | PACK-CHANGES gives none; upstream -1 to 4 (P01-4, D29) | server | changed (name) | CORE-09 |
| `runMultithreaded` | `behavior.runMultithreaded` | behavior | true | n/a | server | same | CORE-09 |
| `basicHopperCooldown` | `behavior.basicHopperCooldown` | behavior | 10 ticks | 1-200 | server | same | CORE-09 |
| `inventoryConnectorMaxSlots` (commented out upstream, inactive) | none | n/a | `Integer.MAX_VALUE` | 1-`Integer.MAX_VALUE` | n/a | dropped, Accepted (D22, P01-3) | none |

## Common keys (upstream `toms_storage-common.toml`)

| Upstream key | 1.7.10 key | Category | Default | Range | Side/sync | Status | Bugs |
|---|---|---|---|---|---|---|---|
| `blockedMods` | `behavior.blockedMods` | behavior | `[]` | list of mod ids, verbatim, unvalidated upstream | server | same | none |
| `blockedBlocks` | `behavior.blockedBlocks` | behavior | `[]` | list of block ids; unresolvable entries and air dropped upstream | server | same | none |
| `importantInfo` | none | n/a | true | n/a | n/a | dropped, Accepted (D22, P01-3); CORE-01 cannot occur, the key is not ported | CORE-01 |

Fabric default `blockedBlocks = ["create:belt"]` is not carried: PACK-CHANGES.md fixes the default at `[]`; whether Create exists in the pack is unverified.

## Keys new in 1.7.10 (no upstream counterpart)

| 1.7.10 key | Category | Default | Range | Source | Status |
|---|---|---|---|---|---|
| `modules.storage`, `modules.terminal`, `modules.network`, `modules.wireless` | modules | true | n/a | PACK-CHANGES.md:39-40, D16; gate behavior, never registration (non-negotiable 6) | new |
| `modules.integration.nei` / `.waila` | modules | true | n/a | PACK-CHANGES.md; only meaningful with the mod loaded | new |
| `modules.integration.appeng` | modules | false | n/a | PACK-CHANGES.md; flag registered in M01, behavior in M05 (D10, D14) | new |
| `euSink.enabled` | euSink | false | n/a | D7, [0004](../../design/0004-energy-and-machine-model.md) | new |
| `euSink.euPerTick` | euSink | 8 EU/t | not stated | D7 (ULV rate `GTValues.V[0]`) | new |

## Notes

- Upstream loader differences (Fabric autoconfig `config/toms_storage.json`, `invConnectorMaxCables` vs NF `invCableConnectorMaxScanSize`, original-spec config.md "Loader differences") do not apply; one 1.7.10 key set.
- CORE-09 (fields are 0 before server-config sync): the GTNHLib `@Config` fields carry declared defaults from class load and `@Config.Sync` overwrites them on join, so no zero window is expected. This is unverified until M01 tests a client joining a server; recorded in index row `config`.
