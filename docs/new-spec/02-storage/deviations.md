# 02 Storage · deviations

Part of [02 Storage](index.md).

## Sheets

| Sheet | Content |
|---|---|
| This file | Deviation table, contradictions, proposals |

No extra sheets at draft stage; the NBT key lists are in the table below.

## How to read the table

- **Original**: link to the original-spec sheet.
- **Here**: `same`, `changed`, `not ported` (by decision; the upstream code remains at the 26.2 branch), `deferred` or `new`.
- **Decision**: a decision-log row (D#) or design record (000N). `P02-n` points to an [Accepted proposal](proposals.md#proposed-not-decided).
- **Bugs**: IDs from [BUGS.md](../../original-spec/BUGS.md) the row must not reproduce.
- Tier and recipe rows cite [tier-map.md](../00-pack-integration/tier-map.md) (D13) and do not restate recipes. All machine recipes go in `gt.recipe.assembler` unless the tier map says crafting table (D8).
- All blocks here are ordinary 1.7.10 block + TileEntity and power-free (D9, [0004](../../design/0004-energy-and-machine-model.md)), except the level emitter's optional EU sink (D7).
- NBT: readers tolerate missing keys, writers stay forward-compatible (config-safe-registration). 1.7.10 NBT key names equal the upstream key names listed in the sheet; where upstream used data components, the component field names become NBT keys.

## Deviation table

| Original | Here | What changes | Decision | Bugs |
|---|---|---|---|---|
| [index](../../original-spec/02-storage/index.md) | changed | Area table says "storage item"; upstream 26.2 has none (filing cabinet is a block). Module flags `modules.storage` / `modules.network` gate behavior only, never registration. | D16, 0003 | none |
| [filing-cabinet](../../original-spec/02-storage/filing-cabinet.md) | changed | Same 512-slot, one-item-type rules. NBT `inventory` (list of non-empty stacks) and `CustomName` stay as 1.7.10 NBT. Reach `distanceToSqr < 64.0` kept. Tier: LV assembler (tier-map `filing_cabinet`). Power-free. Lock rules rewritten, not copied. | D8, D9, D13 | CORE-05, CORE-06, STOR-02 |
| [open-crate](../../original-spec/02-storage/open-crate.md) | changed | Virtual inventory of `EntityItem`s in front, no NBT. Rescan interval and the phantom append slot per sheet; the staggered tick must not use `Math.abs` of a hash. Entity scan every 5 ticks with a non-negative phase (`hash & 0x7fffffff`). Tier: ULV crafting table (`open_crate`). | D13, Accepted P02-1 (D29) | STOR-01, STOR-04 |
| [inventory-proxy](../../original-spec/02-storage/inventory-proxy.md) | changed | Pass-through over the front block via the D10 chain (`ISidedInventory`, `IInventory`, AE2 optional off at alpha, `EMPTY`). Paintable. Tier: LV assembler, circuitLV (`inventory_proxy`). Power-free. | D8, D9, D10, D13, D18 | none |
| [trims-paint](../../original-spec/02-storage/trims-paint.md) | changed | Trim and painted trim keep behavior. Painted block stores a facade block + meta; the 1.7.10 NBT keys and renderer are not specified upstream (component `paint`) and settled: TE NBT keys in the `paintBlock` / `paintMeta` style, names picked at M02, custom ISBRH renderer. Tier: ULV crafting table for `trim`; `painted_trim` has no recipe (made with the paint kit), per tier-map. | D13, Accepted P02-2 (D29) | none |
| [cables](../../original-spec/02-storage/cables.md) | changed | Cable and framed cable keep connection rules. Block state in 1.7.10 is meta or TE; waterlogging is not ported (no such mechanic in 1.7.10). Tier: LV assembler, circuitLV (`inventory_cable`, `inventory_cable_framed`). | D8, D9, D13, D18, D21 (P02-3) | none |
| [cable-connector](../../original-spec/02-storage/cable-connector.md) | changed | FACING plus 6 connection flags keep. Beacon-gated inventory-link GUI kept as gated behavior (`network.linkBeacon*` keys). Tier: LV assembler, circuitLV (`inventory_cable_connector`, `inventory_cable_connector_framed`). | D8, D13, D16, D18 | none |
| [inventory-connector](../../original-spec/02-storage/inventory-connector.md) | changed | Scan every 20 ticks, range from `network.inventoryConnectorRange` (16, 4-256), `network.onlyTrimsConnect`, `network.invCableConnectorMaxScanSize` (2048). Staggered phase must be non-negative. Tier: LV assembler, circuitLV (`inventory_connector`). | D8, D10, D13, D16, D18 | STOR-03, STOR-05 |
| [inventory-interface](../../original-spec/02-storage/inventory-interface.md) | changed | Pass-through view of a connector's merged inventory, 20-tick scan, same phase fix. Tier: LV assembler, circuitLV (`inventory_interface`). | D8, D10, D13, D18 | STOR-05 |
| [level-emitter](../../original-spec/02-storage/level-emitter.md) | changed | 10-tick gate, signal 15/0. NBT `Filter` (stack), `Count` (int; load default 0 vs runtime 1 must be unified), `lessThan` (bool). Optional EU sink 8 EU/t behind `euSink.enabled`. Tier: HV assembler (`level_emitter`). | D7, D8, D13 | STOR-06, STOR-07 |
| [hopper](../../original-spec/02-storage/hopper.md) | changed | 1 item per cycle, cooldown `behavior.basicHopperCooldown` (10, 1-200); one hopper block only, as upstream (`BasicInventoryHopperBlock`); "mid" (4) and "fast" (1) are internal cooldown stages (`midCd`, `fastCd`), not variants. `ENABLED` from redstone. NBT `Filter`. Tier: LV assembler, circuitLV (`basic_inventory_hopper`). | D8, D13, D16, D18, D23 (P02-4 closed) | STOR-08, STOR-09 |
| [items](../../original-spec/02-storage/items.md) | changed | Paint kit: ULV crafting table (`paint_kit`), durability 1 per paint. The `paint` data component becomes NBT, key names per the sheet. Left-click listener becomes a 1.7.10 event hook (unverified). | D13, Accepted P02-2 (D29) | none |
| [components](../../original-spec/02-storage/components.md) | changed | Component codecs become item NBT: `configurator` (`bound`, `is_bound`, `selecting`, `show_inv_box`, `mass_select`, `box_start`, `selection`, `last_action`), `simple_item_filter` (`stacks` up to 9, `match_component`, `allow_list`), `tag_filter` (`tags`, `allow_list`), `WorldPos` (`bound_dim`, `bounds_pos`). `match_component` becomes "match NBT"; `tag_filter` becomes an ore-dictionary-name filter (list of `OreDictionary` names plus allow/deny), key `tags` stays a string list. Tier: LV assembler (`item_filter`, `tag_item_filter`, `inventory_configurator`), HV assembler (`polymorphic_item_filter`). | D8, D13, D20 (P02-5) | WIRE-05 |
| [containers](../../original-spec/02-storage/containers.md) | changed | `BasicContainer` / `LimitedContainer` become plain `IInventory` helpers; negative-size edge must be rejected. | none | none |
| [cable-network](../../original-spec/02-storage/cable-network.md) | changed | Cable graph with per-level cache, LIFO walk, `markNodeInvalid`. `RemoteConnections` becomes a `WorldSavedData` named `toms_storage:rc` equivalent: `{connections:[{id, owner, owner_name, name, public}]}`, channel name limit 50 chars, connector positions not persisted. Time-based channel name fallback is replaced by a counter-based one; saved-data name `simplestorage_rc`; readers tolerate missing keys. | D10, Accepted P02-6 (D29) | STOR-10 |
| Terminal-side wireless gate | same | The beacon-scan gate shared with the terminal is specified in [04-wireless](../04-wireless/index.md). | none | TERM-04, WIRE-04 |
