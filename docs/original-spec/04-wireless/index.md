# 04 · Wireless

## Overview

The wireless terminal feature of Tom's Simple Storage 2.11.3 (upstream branch `26.2`, commit `95bb61d`, NeoForge baseline): the two wireless items (basic `wireless_terminal`, advanced `adv_wireless_terminal`), how they extend terminal reach, the advanced item's block binding (the `bound_pos` data component), the beacon-level mechanic that grants same-dimension or cross-dimension access, and the keybind open flow. There is **no wireless block, block entity, menu or screen of its own in 26.2** — the items remotely activate the normal terminal blocks (`storage_terminal`, `crafting_terminal`, via the `toms_storage:remote_activate` tag), and the terminal's menu/screen/sync are the wired terminal's (03-terminal, 06-client-ui, and 01-core terminal-sync.md). Where a fact differs by loader or version, the sheet says which loader/branch it came from.

## Contents

- [wireless-items.md](wireless-items.md): `WirelessTerminal` interface, `WirelessTerminalItem` (raycast remote activation, reach), `AdvWirelessTerminalItem` (bind/useOn/activate, tooltips, keybind open), recipes; On 1.15 / On 1.16 notes
- [range-and-sync.md](range-and-sync.md): the terminal-side reach check (`canInteractWith`: 6-block base, +25% for menu open, beacon shortcut rules), the 40-tick phased beacon scan over an 8-block-inflated region, the keybind open flow, and the deliberate cross-link to 01-core terminal-sync.md (shared sync, not duplicated here)

## Key facts

- "Wireless" in 26.2 is **item-based**, not block-based: the basic item extends the terminal's interaction reach (config `wirelessReach`, default 16 blocks, range 4–64); the advanced item (config `advWirelessRange`, default 64, range 16–512) can also be **bound** to one terminal (sneak + right-click on it) and opens that exact terminal from anywhere, even in another dimension, via the `B` keybind.
- The reach check runs on the **terminal's** block entity (`canInteractWith`, 03-terminal) using the range of the first wireless item found anywhere in the player's hands/inventory/Curios — carrying an item in a backpack slot is enough.
- Beacon levels (0–4, computed by `BeaconLevelCalc` over an 8-block radius, refreshed every 40 ticks with a per-position phase) grant same-dimension access at `wirelessTermBeaconLvl` (default 1) and cross-dimension access at `wirelessTermBeaconLvlCrossDim` (default 4); `-1` disables each rule. The beacon shortcut also requires `termReach > 0` (a wireless item in the inventory).
- **Sync split (deliberate):** `TerminalSyncManager` (the `toms_storage:data` stack protocol) is used by wired and wireless terminals alike, so it is documented once in 01-core [terminal-sync.md](../01-core/terminal-sync.md); the terminal menu (03-terminal) drives it. This area cross-links it instead of duplicating it.

## Lang

Every lang key in this area's scope, with exact English text (en_us.json at `95bb61d`; the `\\` in tooltip values is the code's line-separator convention, rendered as a newline by `ClientUtil.tooltip`):

| Key | Text |
|---|---|
| `item.toms_storage.wireless_terminal` | Wireless Terminal |
| `item.toms_storage.adv_wireless_terminal` | Advanced Wireless Terminal |
| `key.toms_storage.open_terminal` | [Tom's Simple Storage] Open Terminal |
| `tooltip.toms_storage.hold_shift_for_info` | Hold SHIFT for more info. |
| `tooltip.toms_storage.wireless_terminal` | Extends your reach for the terminal.\<newline>Reach: %d blocks |
| `tooltip.toms_storage.adv_wireless_terminal` | Sneak + Right Click on a terminal to bind it.\<newline>Range: %d blocks\<newline>The teminal's chunk has to be loaded (typo: "teminal", WIRE-02) |
| `tooltip.toms_storage.adv_wireless_terminal.beacon` | A nearby beacon in an 8 block radius of the terminal\<newline>extends the range of the terminal.\<newline>The beacon's level determines the range: |
| `tooltip.toms_storage.adv_wireless_terminal.beacon1` | %d: anywhere in the same dimension. |
| `tooltip.toms_storage.adv_wireless_terminal.beacon2` | %d: anywhere across dimensions. |
| `tooltip.toms_storage.adv_wireless_terminal.bound` | Bound to (%s, %s, %s) in %s |
| `tooltip.toms_storage.adv_wireless_terminal.keybind` | Press the %s key to open |
| `tooltip.toms_storage.adv_wireless_terminal.keybind.outline` | [%s] |
| `chat.toms_storage.terminal_bound` | Bound Advanced Wireless Terminal |
| `chat.toms_storage.terminal_invalid_block` | Invalid block |
| `chat.toms_storage.terminal_not_loaded` | Terminal out of range (Not Loaded) |
| `tooltip.toms_storage.terminal_beacon` | Beacon level: %d%s |
| `tooltip.toms_storage.terminal_beacon.sameDim` | This terminal is accessible anywhere\<newline>in this dimension\<newline>with the Advanced Wireless Terminal |
| `tooltip.toms_storage.terminal_beacon.anywhere` | This terminal is accessible anywhere\<newline>with the Advanced Wireless Terminal |

Strings built in code: the bound-tooltip argument is the dimension's registry identifier string (e.g. `minecraft:overworld`); the keybind argument is the rendered keybind name wrapped in green `[...]`. Missing keys: none in this area (the two missing keys found during the sweep — `config.toms_storage.importantInfo`, `tooltip.toms_trading_network.textfield` — belong to 01-core, CORE-01/CORE-07). Hard-coded English: none.

## Related areas

- `01-core`: `TerminalSyncManager`/`IAutoFillTerminal`/`IDataReceiver` (terminal-sync.md — the sync protocol cross-linked from range-and-sync.md), `NetworkHandler` + `OpenTerminalPacket` (network.md — the keybind open flow), `BeaconLevelCalc`, `PlayerInvUtil` (util-misc.md), the wireless config keys (config.md), `Platform.checkUse` and the `B` keybind (mod-lifecycle.md, platform-layer.md), `bound_pos` component registration (Content, mod-lifecycle.md).
- `02-storage`: the `bound_pos` `WorldPos` codec (components package) and the terminal blocks' `BlockWithTooltip` base are there; the `remote_activate` tag values are the two terminal blocks (03-terminal).
- `03-terminal`: `StorageTerminalBlockEntity.canInteractWith` and its beacon scan (the implementation this area's range-and-sync.md specifies), `StorageTerminalMenu` (instantiates `TerminalSyncManager`, implements `IDataReceiver`/`IAutoFillTerminal`), terminal NBT.
- `06-client-ui`: `ClientUtil.tooltip` (shift-gated tooltip rendering) and `ClientUtil.drawTerminalOutline` (client outline of the targeted block while holding a basic wireless terminal), the terminal screen's beacon tooltip lines, item models/textures (`assets/toms_storage/models/item/wireless_terminal.json`, `adv_wireless_terminal.json`, textures `item/wireless_terminal.png`, `item/adv_wireless_terminal.png`).

## Coverage

- Source files read: 3 of 3 in scope (`SH/item/WirelessTerminalItem.java`, `SH/item/WirelessTerminal.java`, `SH/item/AdvWirelessTerminalItem.java`); everything else the feature touches is owned by other areas and was read out-of-scope for cross-links (below)
- Textures / sounds / lang keys covered: 2 textures confirmed present (contents not inspected — 06-client-ui territory), 0 sounds / 18 lang keys (table above)
- Corrections made to earlier statements: none (first writing)

### Files read out of scope (for cross-links)

- `SH/block/entity/StorageTerminalBlockEntity.java` (beacon scan, `canInteractWith`, NBT)
- `SH/components/WorldPos.java` (binding codec fields)
- `SH/client/ClientUtil.java` (tooltip helper, terminal outline raytrace)
- `SH/resources/data/toms_storage/tags/block/remote_activate.json`, `recipe/wireless_terminal.json`, `recipe/adv_wireless_terminal.json`
- `SH/resources/assets/toms_storage/lang/en_us.json` (in-scope keys)
- On 1.15: `src/main/java/com/tom/storagemod/item/ItemWirelessTerminal.java`; On 1.16: `src/main/java/com/tom/storagemod/item/ItemAdvWirelessTerminal.java`

## Known limits

- No MC instance was run: reach math, beacon behavior and the keybind flow are *inferred* from source at `95bb61d`.
- The terminal screen's beacon tooltip rendering (step 9 in range-and-sync.md) was read from `AbstractStorageTerminalScreen` lines 443–444 (06-client-ui territory) — the full screen behavior is not covered here.
- The `remote_activate` tag file was checked (2 values); third-party mods may add blocks to it — the behavior contract is "any block in the tag".
- Fabric item behavior was not read separately: the items are SH classes, so both loaders share the logic; only `Platform.checkUse` and the Curios/trinkets fallback differ (01-core platform-layer.md).

## Suspected bugs

| ID | Title | Location | What looks wrong | Severity |
|---|---|---|---|---|
| WIRE-01 | Basic wireless item force-returns SUCCESS_SERVER | `NeoForge/src/platform-shared/java/com/tom/storagemod/item/WirelessTerminalItem.java:42` | When the raycast target is in `remote_activate`, `use()` returns `SUCCESS_SERVER` even if `state.useWithoutItem` returned `PASS` (nothing actually happened) — the client still consumes the item use, hiding a failed activation | gameplay |
| WIRE-02 | Typo in advanced terminal tooltip | `NeoForge/src/platform-shared/resources/assets/toms_storage/lang/en_us.json:82` | "The teminal's chunk has to be loaded" — misspells "terminal" | cosmetic |
| WIRE-03 | Invalid/unloaded bound terminal not consumed | `NeoForge/src/platform-shared/java/com/tom/storagemod/item/AdvWirelessTerminalItem.java:77` | When the bound block is not in `remote_activate` or its chunk is unloaded, the server only shows an action-bar message and the use falls through to `PASS` (item use not consumed), while the client side already returned `CONSUME` — inconsistent consumption between sides, and no way to distinguish "did nothing" from a real activation | gameplay |
| WIRE-04 | Beacon rescan phase can never fire | `NeoForge/src/platform-shared/java/com/tom/storagemod/block/entity/StorageTerminalBlockEntity.java:148` | The scan phase is `getGameTime() % 40 == Math.abs(worldPosition.hashCode()) % 40`; if `hashCode() == Integer.MIN_VALUE`, `Math.abs` stays negative, so the right-hand side is negative while the left is always ≥ 0 — that terminal's `beaconLevel` never updates from 0 (beacon access silently dead) | gameplay |
| WIRE-05 | `bound_pos` codec field name inconsistent | `NeoForge/src/platform-shared/java/com/tom/storagemod/components/WorldPos.java:18` | The component is registered as `bound_pos` but the codec field for the position is `bounds_pos` (extra "s") — a typo in the persisted field name; harmless within this mod but will look wrong in any external NBT inspection | cosmetic |
