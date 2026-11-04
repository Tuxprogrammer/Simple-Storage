# 04 Wireless · deviations

Part of [04 Wireless](index.md).

## Sheets

| Sheet | Content |
|---|---|
| This file | Deviation table, contradictions, proposals |

## How to read the table

- **Original**: link to the original-spec sheet. **Here**: `same`, `changed`, `not ported` (by decision; the upstream code remains at the 26.2 branch), `deferred` or `new`.
- **Decision**: D# or 000N; Every P04-n proposal is accepted (see [proposals](proposals.md#proposed-not-decided)).
- **Bugs**: IDs from [BUGS.md](../../original-spec/BUGS.md).
- Tiers cite [tier-map.md](../00-pack-integration/tier-map.md) (D13); recipes are assembler recipes (D8). The items are power-free (D9, [0004](../../design/0004-energy-and-machine-model.md)).
- NBT: 1.7.10 item NBT replaces the `bound_pos` component. Key names from the sheet: `bound_pos` = WorldPos with `bound_dim` and `bounds_pos`. Upstream 1.16 used `BindX`, `BindY`, `BindZ`, `BindDim`. Readers accept missing keys and may read the legacy names; writers emit one format.

## Deviation table

| Original | Here | What changes | Decision | Bugs |
|---|---|---|---|---|
| [index](../../original-spec/04-wireless/index.md) | changed | Module flag `modules.wireless` gates behavior only. Config: `wireless.reach` (16, 4-64), `wireless.advRange` (64, 16-512), `wireless.beaconLvl` / `wireless.beaconLvlDim` (1 / 4, -1 to 4). Upstream base reach is 6 blocks; the 16 default is a pack choice recorded in PACK-CHANGES. | D16 | WIRE-02 |
| [wireless-items](../../original-spec/04-wireless/wireless-items.md) | changed | Basic item: MV assembler (D18) (`wireless_terminal`). Advanced item: HV assembler (`adv_wireless_terminal`); upstream smithing-on-netherite recipe does not exist in 1.7.10 and the tier map replaces it. Use result returns the correct action result. Invalid/unloaded bound terminal is consumed or reported, not silently ignored. `bound_pos` field name unified. Block tag `remote_activate` (storage and crafting terminal) becomes a block list. Tooltip typo fixed. | D8, D13, D18, D29 (P04-1) | WIRE-01, WIRE-02, WIRE-03, WIRE-05 |
| [range-and-sync](../../original-spec/04-wireless/range-and-sync.md) | changed | Reach +25% on menu open, beacon scan over an 8-block-inflated 17x17x17 region every 40 ticks with a per-position phase; phase must never be unsatisfiable. First wireless item found (main hand, off-hand, then inventory; Baubles slots are a beta candidate) sets reach. The open-terminal keybind sends `open_terminal`; only the advanced item opens. The key ships unbound and the advanced terminal tooltip tells the player to bind it in Controls (replaces upstream default `B`). Packets via S2 `PacketSimple`/`PacketCustom` (API-MAP). | D26, D28, D29 (P04-3) | WIRE-03, WIRE-04, TERM-04 |
