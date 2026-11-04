# 04 Wireless: what changes in the new mod

The basic and advanced wireless terminals, their reach, the beacon extension and the bound-terminal binding. Original: [original-spec/04-wireless](../../original-spec/04-wireless/index.md).

Status: draft. Rows marked Accepted cite a decision-log row or record; the rest are Proposed.
Implementation: none yet (roadmap M04).

## Sheets

| Sheet | Content |
|---|---|
| This file | Deviation table, contradictions, proposals |

## How to read the table

- **Original**: link to the original-spec sheet. **Here**: `same`, `changed`, `dropped`, `deferred` or `new`.
- **Decision**: D# or 000N; `Proposed P04-n` means not decided.
- **Bugs**: IDs from [BUGS.md](../../original-spec/BUGS.md).
- Tiers cite [tier-map.md](../00-pack-integration/tier-map.md) (D13); recipes are assembler recipes (D8). The items are power-free (D9, [0004](../../design/0004-energy-and-machine-model.md)).
- NBT: 1.7.10 item NBT replaces the `bound_pos` component. Key names from the sheet: `bound_pos` = WorldPos with `bound_dim` and `bounds_pos`. Upstream 1.16 used `BindX`, `BindY`, `BindZ`, `BindDim`. Readers accept missing keys and may read the legacy names; writers emit one format.

## Deviation table

| Original | Here | What changes | Decision | Bugs |
|---|---|---|---|---|
| [index](../../original-spec/04-wireless/index.md) | changed | Module flag `modules.wireless` gates behavior only. Config: `wireless.reach` (16, 4-64), `wireless.advRange` (64, 16-512), `wireless.beaconLvl` / `wireless.beaconLvlDim` (1 / 4, -1 to 4). Upstream base reach is 6 blocks; the 16 default is a pack choice recorded in PACK-CHANGES. | D16 | WIRE-02 |
| [wireless-items](../../original-spec/04-wireless/wireless-items.md) | changed | Basic item: MV assembler (`wireless_terminal`). Advanced item: HV assembler (`adv_wireless_terminal`); upstream smithing-on-netherite recipe does not exist in 1.7.10 and the tier map replaces it. Use result returns the correct action result. Invalid/unloaded bound terminal is consumed or reported, not silently ignored. `bound_pos` field name unified. Block tag `remote_activate` (storage and crafting terminal) becomes a block list. Tooltip typo fixed. | D8, D13, Proposed P04-1 | WIRE-01, WIRE-02, WIRE-03, WIRE-05 |
| [range-and-sync](../../original-spec/04-wireless/range-and-sync.md) | changed | Reach +25% on menu open, beacon scan over an 8-block-inflated 17x17x17 region every 40 ticks with a per-position phase; phase must never be unsatisfiable. First wireless item found (hands, inventory, then a baubles-style slot) sets reach. `B` keybind sends `open_terminal`; only the advanced item opens. Packets via S2 `PacketSimple`/`PacketCustom` (API-MAP). | Proposed P04-2, P04-3 | WIRE-03, WIRE-04, TERM-04 |

## Contradictions with other docs

| Where | Says | Conflicts with |
|---|---|---|
| [wireless-items.md](../../original-spec/04-wireless/wireless-items.md) step 1 | `Config.get().wirelessRange` | same sheet Numbers row, index and [PACK-CHANGES.md](../../PACK-CHANGES.md):46-55 use `wirelessReach` / `wireless.reach` |
| [tier-map.md](../00-pack-integration/tier-map.md) | basic reach "16 blocks" | upstream base reach 6 blocks (not a conflict with `wireless.reach`, a pack deviation to record) |
| [OPEN-QUESTIONS.md](../OPEN-QUESTIONS.md):16 | tier table shifted | D15 matches classic GT5 |

## Proposed (not decided)

| ID | Question | Recommendation | Settled by |
|---|---|---|---|
| P04-1 | Binding NBT key set and legacy `BindX/Y/Z/Dim` read support? | Write `bound_dim` + `bounds_pos` fixed to one spelling; read legacy keys. | M04 |
| P04-2 | Equipment-slot search beyond hands and inventory (Baubles in the pack, unverified)? | Hands and inventory only at alpha. | M04 |
| P04-3 | Beacon detection in 1.7.10 (`TileEntityBeacon` levels, unverified)? | Read beacon level via the vanilla TE; confirm in the integration research. | M04 |
