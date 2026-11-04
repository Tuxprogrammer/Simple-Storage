# 04 Wireless · contradictions and proposals

Part of [04 Wireless](index.md).

## Contradictions with other docs

| Where | Says | Conflicts with |
|---|---|---|
| [wireless-items.md](../../original-spec/04-wireless/wireless-items.md) step 1 | `Config.get().wirelessRange` | same sheet Numbers row, index and [PACK-CHANGES.md](../../PACK-CHANGES.md):46-55 use `wirelessReach` / `wireless.reach` |
| [tier-map.md](../00-pack-integration/tier-map.md) | basic reach "16 blocks" | upstream base reach 6 blocks (not a conflict with `wireless.reach`, a pack deviation to record) |

## Proposed (not decided)

| ID | Question | Recommendation | Settled by |
|---|---|---|---|
| P04-1 | Binding NBT key set and legacy `BindX/Y/Z/Dim` read support? | Write `bound_dim` + `bounds_pos` fixed to one spelling; read legacy keys. | M04 |
| P04-2 | Equipment-slot search beyond hands and inventory (Baubles in the pack, unverified)? | Hands and inventory only at alpha. | M04 |
| P04-3 | Beacon detection in 1.7.10 (`TileEntityBeacon` levels, unverified)? | Read beacon level via the vanilla TE; confirm in the integration research. | M04 |
