# 02 Storage · contradictions and proposals

Part of [02 Storage](index.md).

## Contradictions with other docs

| Where | Says | Conflicts with |
|---|---|---|
| [inventory-connector.md](../../original-spec/02-storage/inventory-connector.md), [index.md](../../original-spec/02-storage/index.md) | config names `invConnectorScanRange`, `onlyTrims` | [PACK-CHANGES.md](../../PACK-CHANGES.md):46-47 `network.inventoryConnectorRange`, `network.onlyTrimsConnect` |

<a id="proposed-not-decided"></a>
## Accepted proposals

All six were settled on 2026-11-03.

| # | Question | Outcome | Decision |
|---|---|---|---|
| P02-1 | Entity scan cadence for the open crate in 1.7.10? | `EntityItem` AABB query every 5 ticks with a non-negative phase (`hash & 0x7fffffff`) | D29 |
| P02-2 | Paint/facade storage and rendering in 1.7.10? | TE NBT `paintBlock` / `paintMeta` style keys (names picked at M02), custom ISBRH renderer | D29 |
| P02-3 | Drop waterlogging on cables? | Dropped; cables are not waterloggable | D21 |
| P02-4 | Ship the mid/fast hopper variants? | Closed, premise wrong: upstream has ONE hopper block (`BasicInventoryHopperBlock`); "mid" and "fast" are internal cooldown stages (`midCd` 4, `fastCd` 1 ticks by default), not variants | D23 |
| P02-5 | Tag filter source in 1.7.10? | Ore-dictionary-name filter (list of `OreDictionary` names plus allow/deny); key `tags` stays a string list; "match components" becomes "match NBT" | D20 |
| P02-6 | Channel-name fallback and `RemoteConnections` saved-data name? | Counter-based fallback, saved-data name `simplestorage_rc`, readers tolerate missing keys | D29 |
