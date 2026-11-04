# Wireless · key facts

Part of [04-wireless](index.md).

## Key facts

- "Wireless" in 26.2 is **item-based**, not block-based: the basic item extends the terminal's interaction reach (config `wirelessReach`, default 16 blocks, range 4–64); the advanced item (config `advWirelessRange`, default 64, range 16–512) can also be **bound** to one terminal (sneak + right-click on it) and opens that exact terminal from anywhere, even in another dimension, via the `B` keybind.
- The reach check runs on the **terminal's** block entity (`canInteractWith`, 03-terminal) using the range of the first wireless item found anywhere in the player's hands/inventory/Curios — carrying an item in a backpack slot is enough.
- Beacon levels (0–4, computed by `BeaconLevelCalc` over an 8-block radius, refreshed every 40 ticks with a per-position phase) grant same-dimension access at `wirelessTermBeaconLvl` (default 1) and cross-dimension access at `wirelessTermBeaconLvlCrossDim` (default 4); `-1` disables each rule. The beacon shortcut also requires `termReach > 0` (a wireless item in the inventory).
- **Sync split (deliberate):** `TerminalSyncManager` (the `toms_storage:data` stack protocol) is used by wired and wireless terminals alike, so it is documented once in 01-core [terminal-sync.md](../01-core/terminal-sync.md); the terminal menu (03-terminal) drives it. This area cross-links it instead of duplicating it.

## Related areas

- `01-core`: `TerminalSyncManager`/`IAutoFillTerminal`/`IDataReceiver` (terminal-sync.md — the sync protocol cross-linked from range-and-sync.md), `NetworkHandler` + `OpenTerminalPacket` (network.md — the keybind open flow), `BeaconLevelCalc`, `PlayerInvUtil` (util-misc.md), the wireless config keys (config.md), `Platform.checkUse` and the `B` keybind (mod-lifecycle.md, platform-layer.md), `bound_pos` component registration (Content, mod-lifecycle.md).
- `02-storage`: the `bound_pos` `WorldPos` codec (components package) and the terminal blocks' `BlockWithTooltip` base are there; the `remote_activate` tag values are the two terminal blocks (03-terminal).
- `03-terminal`: `StorageTerminalBlockEntity.canInteractWith` and its beacon scan (the implementation this area's range-and-sync.md specifies), `StorageTerminalMenu` (instantiates `TerminalSyncManager`, implements `IDataReceiver`/`IAutoFillTerminal`), terminal NBT.
- `06-client-ui`: `ClientUtil.tooltip` (shift-gated tooltip rendering) and `ClientUtil.drawTerminalOutline` (client outline of the targeted block while holding a basic wireless terminal), the terminal screen's beacon tooltip lines, item models/textures (`assets/toms_storage/models/item/wireless_terminal.json`, `adv_wireless_terminal.json`, textures `item/wireless_terminal.png`, `item/adv_wireless_terminal.png`).

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
