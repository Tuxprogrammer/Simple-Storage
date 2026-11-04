# 01-core · contradictions and proposals

Part of [01-core](index.md).

## Contradictions with other docs

| Where | Says | Conflicts with |
|---|---|---|
| [PACK-CHANGES.md:33](../../PACK-CHANGES.md) | per-world keys use `@Config.Sync` "where a client must see them" | names no key as client-visible, while [client-package](../../original-spec/06-client-ui/client-package.md) reads the wireless range on the client; see P01-5 |

## Proposed (not decided)

| Id | Question | Recommendation | Settled by |
|---|---|---|---|
| P01-1 | Which packet library carries `data` and `open_terminal`, and what is the 1.7.10 payload size limit that the 64000-byte terminal sync cap must respect? | Use the GTNHLib channel base classes ([shipped-mods.md:119](../00-pack-integration/shipped-mods.md)) over `PacketCustom`/`PacketSimple`; split terminal sync into several packets under a conservative per-packet size (limit unverified, measure at M01) | M01 (foundation), M03 for the terminal sync batching |
| P01-2 | How is `DataPacket` NBT bounded (CORE-02)? | Per-packet decode limit plus a type id that selects a fixed schema; reject and log on overrun, close nothing | M01 |
| P01-3 | Config: drop `importantInfo` (CORE-01) and `inventoryConnectorMaxSlots`? | Drop both: `importantInfo` only held a notice about the split toml files, which no longer exist; `inventoryConnectorMaxSlots` is inactive upstream | owner, at M01 config review |
| P01-4 | Which ranges apply to the five keys PACK-CHANGES lists without range (`network.invCableConnectorMaxScanSize`, `network.linkBeaconLvl`, `linkBeaconRange`, `linkBeaconLvlSameDim`, `linkBeaconLvlCrossDim`)? | Carry upstream's ranges: 16-`Integer.MAX_VALUE`; -1 to 4; 0-`Integer.MAX_VALUE`; -1 to 4; -1 to 4. Update PACK-CHANGES.md in M01 | M01 |
| P01-7 | Filing cabinet rules (CORE-05, CORE-06): keep any stack-size limit at all? | Do not reproduce. Any first item accepted; automation sees real stack sizes; document the final rule in 02-storage | M02 |
| P01-5 | Which config keys are `@Config.Sync`? | Sync `wireless.reach` and `wireless.advRange` (client outline), nothing else; all other keys server-only | M01 |
| P01-6 | `BeaconLevelCalc` on 1.7.10: how is the pyramid level read, and are the same base blocks valid? | Compute from the four-level pyramid under the beacon using the vanilla base blocks (iron, gold, emerald, diamond); verify against Forge 10.13.4.1614 source before coding | M04 (wireless uses it) |
