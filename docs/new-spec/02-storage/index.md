# 02 Storage: what changes in the new mod

The storage blocks and items (filing cabinet, open crate, trims and paint, inventory cables, connectors, interface, proxy, level emitter, hopper) and the cable network behind them. Original: [original-spec/02-storage](../../original-spec/02-storage/index.md).

Status: draft. Rows marked Accepted cite a decision-log row or record; rows marked Proposed are listed under "Proposed (not decided)".
Implementation: none yet (roadmap M02 for blocks, cables and connectors; M04 for emitter and hopper behavior gates).

## Sheets

| Sheet | Content |
|---|---|
| This file | Deviation table, contradictions, proposals |

No extra sheets at draft stage; the NBT key lists are in the table below.

## How to read the table

- **Original**: link to the original-spec sheet.
- **Here**: `same`, `changed`, `dropped`, `deferred` or `new`.
- **Decision**: a decision-log row (D#) or design record (000N). `Proposed P02-n` means no record settles it yet.
- **Bugs**: IDs from [BUGS.md](../../original-spec/BUGS.md) the row must not reproduce.
- Tier and recipe rows cite [tier-map.md](../00-pack-integration/tier-map.md) (D13) and do not restate recipes. All machine recipes go in `gt.recipe.assembler` unless the tier map says crafting table (D8).
- All blocks here are ordinary 1.7.10 block + TileEntity and power-free (D9, [0004](../../design/0004-energy-and-machine-model.md)), except the level emitter's optional EU sink (D7).
- NBT: readers tolerate missing keys, writers stay forward-compatible (config-safe-registration). 1.7.10 NBT key names equal the upstream key names listed in the sheet; where upstream used data components, the component field names become NBT keys.

## Deviation table

| Original | Here | What changes | Decision | Bugs |
|---|---|---|---|---|
| [index](../../original-spec/02-storage/index.md) | changed | Area table says "storage item"; upstream 26.2 has none (filing cabinet is a block). Module flags `modules.storage` / `modules.network` gate behavior only, never registration. | D16, 0003 | none |
| [filing-cabinet](../../original-spec/02-storage/filing-cabinet.md) | changed | Same 512-slot, one-item-type rules. NBT `inventory` (list of non-empty stacks) and `CustomName` stay as 1.7.10 NBT. Reach `distanceToSqr < 64.0` kept. Tier: LV assembler (tier-map `filing_cabinet`). Power-free. Lock rules rewritten, not copied. | D8, D9, D13 | CORE-05, CORE-06, STOR-02 |
| [open-crate](../../original-spec/02-storage/open-crate.md) | changed | Virtual inventory of `EntityItem`s in front, no NBT. Rescan interval and the phantom append slot per sheet; the staggered tick must not use `Math.abs` of a hash. Tier: ULV crafting table (`open_crate`). | D13, Proposed P02-1 | STOR-01, STOR-04 |
| [inventory-proxy](../../original-spec/02-storage/inventory-proxy.md) | changed | Pass-through over the front block via the D10 chain (`ISidedInventory`, `IInventory`, AE2 optional off at alpha, `EMPTY`). Paintable. Tier: MV assembler (`inventory_proxy`). Power-free. | D8, D9, D10, D13 | none |
| [trims-paint](../../original-spec/02-storage/trims-paint.md) | changed | Trim and painted trim keep behavior. Painted block stores a facade block + meta; the 1.7.10 NBT keys and renderer are not specified upstream (component `paint`) and not yet decided. Tier: ULV crafting table for `trim`; `painted_trim` has no recipe (made with the paint kit), per tier-map. | D13, Proposed P02-2 | none |
| [cables](../../original-spec/02-storage/cables.md) | changed | Cable and framed cable keep connection rules. Block state in 1.7.10 is meta or TE; waterlogging is dropped (no such mechanic in 1.7.10, unverified). Tier: MV assembler (`inventory_cable`, `inventory_cable_framed`). | D8, D9, D13, Proposed P02-3 | none |
| [cable-connector](../../original-spec/02-storage/cable-connector.md) | changed | FACING plus 6 connection flags keep. Beacon-gated inventory-link GUI kept as gated behavior (`network.linkBeacon*` keys). Tier: MV assembler (`inventory_cable_connector`, `inventory_cable_connector_framed`). | D8, D13, D16 | none |
| [inventory-connector](../../original-spec/02-storage/inventory-connector.md) | changed | Scan every 20 ticks, range from `network.inventoryConnectorRange` (16, 4-256), `network.onlyTrimsConnect`, `network.invCableConnectorMaxScanSize` (2048). Staggered phase must be non-negative. Tier: MV assembler (`inventory_connector`). | D8, D10, D13, D16 | STOR-03, STOR-05 |
| [inventory-interface](../../original-spec/02-storage/inventory-interface.md) | changed | Pass-through view of a connector's merged inventory, 20-tick scan, same phase fix. Tier: MV assembler (`inventory_interface`). | D8, D10, D13 | STOR-05 |
| [level-emitter](../../original-spec/02-storage/level-emitter.md) | changed | 10-tick gate, signal 15/0. NBT `Filter` (stack), `Count` (int; load default 0 vs runtime 1 must be unified), `lessThan` (bool). Optional EU sink 8 EU/t behind `euSink.enabled`. Tier: HV assembler (`level_emitter`). | D7, D8, D13 | STOR-06, STOR-07 |
| [hopper](../../original-spec/02-storage/hopper.md) | changed | 1 item per cycle, cooldown `behavior.basicHopperCooldown` (10, 1-200); mid/fast variants (4, 1 ticks) are not in the tier map (unverified). `ENABLED` from redstone. NBT `Filter`. Tier: MV assembler (`basic_inventory_hopper`). | D8, D13, D16, Proposed P02-4 | STOR-08, STOR-09 |
| [items](../../original-spec/02-storage/items.md) | changed | Paint kit: ULV crafting table (`paint_kit`), durability 1 per paint. The `paint` data component becomes NBT, key names per the sheet. Left-click listener becomes a 1.7.10 event hook (unverified). | D13, Proposed P02-2 | none |
| [components](../../original-spec/02-storage/components.md) | changed | Component codecs become item NBT: `configurator` (`bound`, `is_bound`, `selecting`, `show_inv_box`, `mass_select`, `box_start`, `selection`, `last_action`), `simple_item_filter` (`stacks` up to 9, `match_component`, `allow_list`), `tag_filter` (`tags`, `allow_list`), `WorldPos` (`bound_dim`, `bounds_pos`). `match_component` and tag filtering depend on 1.7.10 equivalents. Tier: LV assembler (`item_filter`, `tag_item_filter`, `inventory_configurator`), HV assembler (`polymorphic_item_filter`). | D8, D13, Proposed P02-5 | WIRE-05 |
| [containers](../../original-spec/02-storage/containers.md) | changed | `BasicContainer` / `LimitedContainer` become plain `IInventory` helpers; negative-size edge must be rejected. | none | none |
| [cable-network](../../original-spec/02-storage/cable-network.md) | changed | Cable graph with per-level cache, LIFO walk, `markNodeInvalid`. `RemoteConnections` becomes a `WorldSavedData` named `toms_storage:rc` equivalent: `{connections:[{id, owner, owner_name, name, public}]}`, channel name limit 50 chars, connector positions not persisted. Time-based channel name fallback must be replaced. | D10, Proposed P02-6 | STOR-10 |
| Terminal-side wireless gate | same | The beacon-scan gate shared with the terminal is specified in [04-wireless](../04-wireless/index.md). | none | TERM-04, WIRE-04 |

## Contradictions with other docs

| Where | Says | Conflicts with |
|---|---|---|
| [cable-network.md](../../original-spec/02-storage/cable-network.md) (Numbers) | "see STOR-06" for the time-based channel name fallback | BUGS.md lists it as STOR-10 |
| [inventory-connector.md](../../original-spec/02-storage/inventory-connector.md), [index.md](../../original-spec/02-storage/index.md) | config names `invConnectorScanRange`, `onlyTrims` | [PACK-CHANGES.md](../../PACK-CHANGES.md):46-47 `network.inventoryConnectorRange`, `network.onlyTrimsConnect` |
| [machine-model.md](../00-pack-integration/machine-model.md) (capability section), [API-MAP.md](../../original-spec/API-MAP.md):59 | 1.7.10 Forge has `Capability` / `net.minecraftforge.items.IInventory` | Forge 10.13.4.1614 jar in the pack reference: no capability classes found; D10 does not depend on them |
| [OPEN-QUESTIONS.md](../OPEN-QUESTIONS.md):16 (Q1) | tier table shifted one tier up vs classic GT5 | D15: matches classic GT5, no shift |

## Proposed (not decided)

| ID | Question | Recommendation | Settled by |
|---|---|---|---|
| P02-1 | Entity scan cadence for the open crate in 1.7.10 (`EntityItem` AABB query every 5 ticks)? | Keep 5 ticks with a non-negative phase (`hash & 0x7fffffff`). | M02 |
| P02-2 | Paint/facade storage and rendering in 1.7.10 (TE with block id + meta, custom ISBRH)? | TE NBT `paintBlock` / `paintMeta` style keys; owner picks names. Pack facade support unverified. | M02 |
| P02-3 | Drop waterlogging on cables? | Drop; document as a deviation. | M02 |
| P02-4 | Ship the mid/fast hopper variants? | Defer: only the basic hopper has a tier-map row. | M04 |
| P02-5 | Tag filter source in 1.7.10 (ore dictionary replaces block/item tags)? | Map tags to ore dictionary names; key `tags` stays a string list. | M04 |
| P02-6 | Channel-name fallback and `RemoteConnections` saved-data name? | Counter-based fallback, saved-data name `simplestorage_rc`, readers tolerate missing keys. | M02 |
