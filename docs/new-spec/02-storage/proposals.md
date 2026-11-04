# 02 Storage · contradictions and proposals

Part of [02 Storage](index.md).

## Contradictions with other docs

| Where | Says | Conflicts with |
|---|---|---|
| [inventory-connector.md](../../original-spec/02-storage/inventory-connector.md), [index.md](../../original-spec/02-storage/index.md) | config names `invConnectorScanRange`, `onlyTrims` | [PACK-CHANGES.md](../../PACK-CHANGES.md):46-47 `network.inventoryConnectorRange`, `network.onlyTrimsConnect` |

## Proposed (not decided)

| ID | Question | Recommendation | Settled by |
|---|---|---|---|
| P02-1 | Entity scan cadence for the open crate in 1.7.10 (`EntityItem` AABB query every 5 ticks)? | Keep 5 ticks with a non-negative phase (`hash & 0x7fffffff`). | M02 |
| P02-2 | Paint/facade storage and rendering in 1.7.10 (TE with block id + meta, custom ISBRH)? | TE NBT `paintBlock` / `paintMeta` style keys; owner picks names. Pack facade support unverified. | M02 |
| P02-3 | Drop waterlogging on cables? | Drop; document as a deviation. | M02 |
| P02-4 | Ship the mid/fast hopper variants? | Defer: only the basic hopper has a tier-map row. | M04 |
| P02-5 | Tag filter source in 1.7.10 (ore dictionary replaces block/item tags)? | Map tags to ore dictionary names; key `tags` stays a string list. | M04 |
| P02-6 | Channel-name fallback and `RemoteConnections` saved-data name? | Counter-based fallback, saved-data name `simplestorage_rc`, readers tolerate missing keys. | M02 |
