# Range, beacon and synchronization mechanics

| # | Field | Contents |
|---|---|---|
| 1 | **Identity** | The wireless *mechanics* shared by the terminal and the wireless items: the terminal-side reach check (`StorageTerminalBlockEntity.canInteractWith`, 03-terminal), the terminal-side beacon scan (`StorageTerminalBlockEntity.updateServer`), `BeaconLevelCalc` (01-core util-misc.md), `PlayerInvUtil.findItem` (01-core util-misc.md), the keybind open flow (01-core network.md), and the terminal inventory sync (`TerminalSyncManager`, 01-core terminal-sync.md — documented there because it is shared by wired and wireless terminals alike; the split is deliberate and this sheet only cross-links it). |
| 2 | **Inputs** | Config: `wirelessReach`, `advWirelessRange`, `wirelessTermBeaconLvl`, `wirelessTermBeaconLvlCrossDim` (01-core config.md). World state: beacon blocks and their pyramids within 8 blocks of the terminal; the player's carried wireless items (any hand or inventory slot, plus Curios/trinkets). The player's keybind press. The terminal BE's `changeCount` (sync trigger). |
| 3 | **Numbers** | Base terminal reach: **6 blocks** (squared-distance test). Menu-opening bonus: reach is increased by **25%** (`d += d/4`, integer division) when the check is for opening the menu. Beacon scan region: the terminal's block AABB inflated by **8 blocks** in every direction (17×17×17 block volume). Beacon scan period: once every **40 ticks** (2 seconds), phase-shifted per terminal by `|pos.hashCode()| % 40`. Beacon level: 0–4 (pyramid level; 0 when the beacon has no active beam). Range units: blocks, Euclidean squared-distance test against the terminal center (x+0.5, y+0.5, z+0.5). |
| 4 | **Behavior** | Ordered steps in [Behavior detail](#behavior-detail). |
| 5 | **Determinism** | The beacon scan is time-phased (`gameTime % 40 == |pos.hashCode() % 40`): all terminals scan once every 40 ticks, staggered; a newly placed beacon is therefore seen by a given terminal within 2 seconds (game time). The `|hashCode|` phase can degenerate — see `WIRE-04`. Beacon level is a pure function of the pyramid below the beacon (`BeaconLevelCalc`, 01-core). Sync is deterministic per the protocol in 01-core terminal-sync.md. |
| 6 | **Data formats** | Terminal BE NBT (03-terminal): `sorting` (int), `modes` (int), `searchType` (int) — the beacon level is **not** saved (recomputed by the periodic scan; before the first scan `beaconLevel` is 0). Binding component: see wireless-items.md step 6. Sync wire format: 01-core terminal-sync.md. |
| 7 | **Output** | A boolean reach decision (menu open / GUI still valid), the cached `beaconLevel` on the terminal BE (also shown in the terminal tooltip via `tooltip.toms_storage.terminal_beacon`, 06-client-ui), and the synced terminal item list on the client. |
| 8 | **Relationships** | `canInteractWith` is called when a player opens the terminal menu and while the menu is open (still-valid check) — 03-terminal. The beacon tooltip lines in the terminal screen (`tooltip.toms_storage.terminal_beacon.anywhere` / `.sameDim`, gated on the same two config keys) are 06-client-ui. The keybind flow ends in `AdvWirelessTerminalItem.open` (wireless-items.md). `TerminalSyncManager` is instantiated by `StorageTerminalMenu` (03-terminal). |
| 9 | **NeoForge dependencies** | None beyond what the linked areas list (block entity ticking, data components for the binding, the network payload). |
| 10 | **Bugs** | `WIRE-03` (invalid/unloaded bound target), `WIRE-04` (degenerate beacon scan phase). |

## Behavior detail

### Terminal-side reach check (`canInteractWith(player, menuCheck)`, server)

1. (server) The terminal BE must be the BE at its own position, else false.
2. (server) `d = 6` (blocks). `termReach` = the range reported by the first `WirelessTerminal` item found via `PlayerInvUtil.findItem` — searched in order: main hand, offhand, every inventory slot, then Curios (NF) / trinkets (Fabric) slots. Note: a wireless item **anywhere in the inventory** (not only held) extends reach; if both items are carried, whichever is found first in that search order wins (main-hand item first).
3. (server) Beacon shortcut: if `wirelessTermBeaconLvl != -1` and the cached `beaconLevel >= wirelessTermBeaconLvl` and `termReach > 0` (the player carries some wireless item):
   - if `wirelessTermBeaconLvlCrossDim != -1` and `beaconLevel >= wirelessTermBeaconLvlCrossDim` → **true** (reachable from anywhere, any dimension);
   - else → `player.level() == level` (reachable from anywhere in the same dimension only).
   (So the beacon bypass requires both a high enough beacon AND a wireless item in the inventory — the item's `getRange` is only used to establish `termReach > 0` here.)
4. (server) Otherwise: `d = max(d, termReach)`; if `menuCheck` (opening the menu, not just validating an open one) then `d += d/4` (25%, integer division). Return true iff the player is in the terminal's dimension and the squared distance from the player to the terminal center is ≤ `d²`.

### Beacon scan (terminal BE `updateServer`, server tick)

5. (server) When `level.getGameTime() % 40 == Math.abs(worldPosition.hashCode()) % 40` (once per 40-tick cycle, phase per position — see `WIRE-04`): scan every position in `AABB(worldPosition).inflate(8)` (17×17×17 around the terminal); for each **loaded** position holding a beacon block, compute `BeaconLevelCalc.calcBeaconLevel` (0–4; a beacon with no active beam scores 0, 01-core util-misc.md); `beaconLevel` = the maximum found, or -1 if no beacons in range. The scan is a full 4913-block iteration each cycle (performance note: *inferred* acceptable at 2-second period, one terminal at a time).

### Keybind open flow (both)

6. (client) Player presses the `B` keybind (`key.toms_storage.open_terminal`, default `B`, registered in 01-core mod-lifecycle.md) → `StorageModClient.clientTick` → `NetworkHandler.openTerminal()` sends an empty `toms_storage:open_terminal` payload.
7. (server) The handler runs `PlayerInvUtil.findItem(player, stack → item is a WirelessTerminal && item.canOpen(stack), EMPTY, identity)` and, if found, calls `item.open(player, stack)`. Only the **advanced** item has `canOpen == true` (wireless-items.md) — with only a basic terminal in the inventory the keybind is a silent no-op (no message). `open` runs `activateTerminal` (wireless-items.md step 6), which either opens the bound terminal (through `useWithoutItem`, which leads to the menu — whose creation then runs `canInteractWith` again, step 1) or shows the invalid/not-loaded action-bar messages.
8. (both) Once the menu is open, the terminal's item list is kept in sync over `toms_storage:data` by `TerminalSyncManager` (01-core terminal-sync.md): the BE bumps `changeCount` when the tracked inventory changes (03-terminal), the menu's `update(changeID, ...)` sends changed stacks (id-table compressed, 64000-byte split), and clicks travel back as `a`-field interactions. This machinery is identical for a player standing in front of the terminal and for a player using the advanced wireless terminal remotely — there is no wireless-specific sync.

### Screen-side beacon display (cross-link)

9. (client) The terminal screen appends beacon info to the terminal tooltip: `tooltip.toms_storage.terminal_beacon` = "Beacon level: %d%s", where `%s` is `tooltip.toms_storage.terminal_beacon.anywhere` ("This terminal is accessible anywhere" / "with the Advanced Wireless Terminal") when `beaconLvl >= wirelessTermBeaconLvlCrossDim` (and that key != -1), else `tooltip.toms_storage.terminal_beacon.sameDim` ("This terminal is accessible anywhere" / "in this dimension" / "with the Advanced Wireless Terminal") when `beaconLvl >= wirelessTermBeaconLvl` (and that key != -1), else empty (06-client-ui, `AbstractStorageTerminalScreen`).

## Deviations in the new mod

None yet.
