# 04 Wireless · contradictions and proposals

Part of [04 Wireless](index.md).

## Contradictions with other docs

| Where | Says | Conflicts with |
|---|---|---|
| [wireless-items.md](../../original-spec/04-wireless/wireless-items.md) step 1 | `Config.get().wirelessRange` | same sheet Numbers row, index and [PACK-CHANGES.md](../../PACK-CHANGES.md):46-55 use `wirelessReach` / `wireless.reach` |
| [tier-map.md](../00-pack-integration/tier-map.md) | basic reach "16 blocks" | upstream base reach 6 blocks (not a conflict with `wireless.reach`, a pack deviation to record) |

<a id="proposed-not-decided"></a>
## Accepted proposals

| # | Question | Outcome | Decision |
|---|---|---|---|
| P04-1 | Binding NBT key set and legacy `BindX/Y/Z/Dim` read support? | Write `bound_dim` + `bounds_pos` fixed to one spelling; read legacy keys. | D29 |
| P04-2 | Equipment-slot search beyond hands and inventory (Baubles)? | The open-terminal keybind finds the item in the held slot and the inventory (1.7.10 has no off-hand slot) only; Baubles slots are a beta candidate. The advanced terminal's open key ships unbound, with a tooltip telling the player to bind it in Controls (replaces the upstream default `B`). | D26, D28 |
| P04-3 | Beacon detection in 1.7.10 (`TileEntityBeacon` levels)? | Read the beacon level via the vanilla TE (confirmed against source in M04). | D29 |
