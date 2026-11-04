# 01-core · contradictions and proposals

Part of [01-core](index.md).

## Contradictions with other docs

| Where | Says | Conflicts with |
|---|---|---|
| [PACK-CHANGES.md:33](../../PACK-CHANGES.md) | per-world keys use `@Config.Sync` "where a client must see them" | names no key as client-visible, while [client-package](../../original-spec/06-client-ui/client-package.md) reads the wireless range on the client; see P01-5 |

<a id="proposed-not-decided"></a>
## Accepted proposals

All seven were settled on 2026-11-03: P01-3 by [D22](../../design/decision-log.md), the rest as recommended by [D29](../../design/decision-log.md).

| # | Question | Outcome | Decision |
|---|---|---|---|
| P01-1 | Which packet library carries `data` and `open_terminal`, and what payload size limit must the 64000-byte terminal sync cap respect? | GTNHLib channel base classes ([shipped-mods.md:119](../00-pack-integration/shipped-mods.md)) over `PacketCustom`/`PacketSimple`; terminal sync is split into several packets under a conservative per-packet size. Limit confirmed against source in M01; batching in M03 | D29 |
| P01-2 | How is `DataPacket` NBT bounded (CORE-02)? | Per-packet decode limit plus a type id that selects a fixed schema; reject and log on overrun, close nothing | D29 |
| P01-3 | Config: drop `importantInfo` (CORE-01) and `inventoryConnectorMaxSlots`? | Both dropped. CORE-01 cannot occur because the key is not ported | D22 |
| P01-4 | Which ranges apply to the five keys PACK-CHANGES lists without range? | Upstream's ranges: `invCableConnectorMaxScanSize` 16-`Integer.MAX_VALUE`; `linkBeaconLvl` -1 to 4; `linkBeaconRange` 0-`Integer.MAX_VALUE`; `linkBeaconLvlSameDim` -1 to 4; `linkBeaconLvlCrossDim` -1 to 4. PACK-CHANGES.md updated in M01 | D29 |
| P01-7 | Filing cabinet rules (CORE-05, CORE-06): keep any stack-size limit? | No. Any first item accepted; automation sees real stack sizes; final rule documented in 02-storage (confirmed against source in M02) | D29 |
| P01-5 | Which config keys are `@Config.Sync`? | `wireless.reach` and `wireless.advRange` (client outline); all other keys server-only | D29 |
| P01-6 | `BeaconLevelCalc` on 1.7.10: how is the pyramid level read? | Computed from the four-level pyramid under the beacon with the vanilla base blocks (iron, gold, emerald, diamond); confirmed against Forge 10.13.4.1614 source in M04 | D29 |
