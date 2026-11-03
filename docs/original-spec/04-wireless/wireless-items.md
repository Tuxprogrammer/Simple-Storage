# Wireless terminal items

| # | Field | Contents |
|---|---|---|
| 1 | **Identity** | `WirelessTerminal` (SH interface, `NeoForge/src/platform-shared/java/com/tom/storagemod/item/WirelessTerminal.java`), `WirelessTerminalItem` (SH, `toms_storage:wireless_terminal`), `AdvWirelessTerminalItem` (SH, `toms_storage:adv_wireless_terminal`). Both items stack to 1, are in the creative tab, and are registered in `Content` (01-core mod-lifecycle.md). |
| 2 | **Inputs** | Player raycast (basic item: `pick(range, 0f, true)` — hits fluids), the `toms_storage:remote_activate` block tag (contains `storage_terminal` and `crafting_terminal`), config keys `wirelessReach` / `advWirelessRange` (01-core config.md), the `toms_storage:bound_pos` data component (advanced item), sneak state (binding), the `B` keybind (open flow, range-and-sync.md). |
| 3 | **Numbers** | Ranges (blocks): basic = `wirelessReach` (default 16, range 4–64); advanced = `advWirelessRange` (default 64, range 16–512). Beacon radius: 8 blocks around the terminal (see range-and-sync.md). Raycast: full reach, 0 partial-tick, fluids hit. Both items: max stack size 1. |
| 4 | **Behavior** | Ordered steps in [Behavior detail](#behavior-detail). |
| 5 | **Determinism** | None (no randomness; raycast is deterministic per world state). |
| 6 | **Data formats** | Advanced item binding: data component `toms_storage:bound_pos` (`WorldPos` record: `ResourceKey<Level>` dimension + `BlockPos`), codec fields `bound_dim` (dimension key) and `bounds_pos` (pos — note the extra "s", see WIRE-05). Written via `DataComponentPatch` on bind. |
| 7 | **Output** | Basic: a remote activation of the looked-at terminal (its normal right-click behavior, e.g. opening the menu). Advanced: remote activation of its bound terminal; the binding component on the item; action-bar messages `chat.toms_storage.terminal_bound` / `terminal_invalid_block` / `terminal_not_loaded`. |
| 8 | **Relationships** | `ClientUtil.tooltip` (06-client-ui) renders the tooltips (shift-gated). `Platform.checkUse` (01-core platform-layer.md) posts the synthetic right-click event for the basic item. `PlayerInvUtil.findItem` + `WirelessTerminal.open`/`canOpen` are the keybind open path (01-core network.md step 5). The terminal-side reach check (`canInteractWith`) and beacon scan are 03-terminal (`StorageTerminalBlockEntity`) consuming these items' `getRange` — see range-and-sync.md. Client outline while holding the basic item: `ClientUtil.drawTerminalOutline` (06-client-ui). |
| 9 | **NeoForge dependencies** | Data components (`bound_pos` — the 1.7.10 counterpart is item NBT; on 1.16 the binding was NBT keys `BindX`/`BindY`/`BindZ`/`BindDim`, see [On 1.16](#on-116)); `InteractionResult.SUCCESS_SERVER`; block tags; `PlayerInteractEvent.RightClickBlock` via `Platform.checkUse`. |
| 10 | **Bugs** | `WIRE-01` (unconditional SUCCESS_SERVER), `WIRE-02` (tooltip typo), `WIRE-03` (invalid/unloaded bound target not consumed), `WIRE-05` (codec field name). |

## Behavior detail

### WirelessTerminalItem (basic)

1. (both) `use(world, player, hand)`: raytrace from the player with reach `Config.get().wirelessRange` (blocks), partial tick 0, **fluids included**. If the hit block state is in tag `toms_storage:remote_activate`:
   1. (both) `Platform.checkUse(world, hit, player, hand)` — posts a synthetic `RightClickBlock` event (NF) / invokes the `UseBlockCallback` (Fabric); if canceled, its cancellation result is used.
   2. (both) Otherwise (not canceled) `state.useWithoutItem(world, player, hit)` runs the block's normal right-click (e.g. the terminal opens its menu — which re-checks reach via `canInteractWith`, range-and-sync.md).
   3. (both) In all cases of a `remote_activate` target the item use returns `InteractionResult.SUCCESS_SERVER` — see `WIRE-01`.
   If the target is not in the tag, the vanilla item use runs (typically nothing).
2. (server) `getRange(player, stack)` = `Config.get().wirelessRange` (this is what the terminal's reach check uses when the player carries the item — anywhere in the inventory, not only in hand).
3. (server) `open(sender, stack)` = no-op; `canOpen(stack)` = false — the keybind open flow (range-and-sync.md) therefore skips basic terminals.
4. (client) `isPlayerHolding(player)`: true if the main-hand **or** off-hand item is a basic wireless terminal — gates the client-side target outline (06-client-ui).
5. (client) Tooltip (shift-gated, via `ClientUtil.tooltip`): without SHIFT one italic gray line "Hold SHIFT for more info."; with SHIFT the lines of `tooltip.toms_storage.wireless_terminal` ("Extends your reach for the terminal." + "Reach: %d blocks", `%d` = `wirelessReach`).

### AdvWirelessTerminalItem (advanced)

6. (both) `use(world, player, hand)` → `activateTerminal(world, stack, player, hand)` with the stack from the used hand:
   1. (both) No `bound_pos` component → `PASS` (an unbound advanced item does nothing on right-click — binding is done on a block, step 8).
   2. (client) Bound → `CONSUME` (prevents double processing; the server decides).
   3. (server) Bound: resolve the bound dimension (`world.getServer().getLevel(pos.dim())`). If the chunk at the bound pos is **loaded**: read the block state there; if it is in `remote_activate`, run `state.useWithoutItem(termWorld, player, hit)` with a synthetic `BlockHitResult` (pos, UP, pos, inside=true) and return its result; otherwise send action-bar message `chat.toms_storage.terminal_invalid_block` ("Invalid block"). If the chunk is **not loaded**, send `chat.toms_storage.terminal_not_loaded` ("Terminal out of range (Not Loaded)"). In both message cases the use falls through to `PASS` — see `WIRE-03`. Note: no `Platform.checkUse` synthetic-event step here (unlike the basic item).
7. (server) `useOn(context)` (right-click a block while holding the item): if **sneaking** (`isSecondaryUseActive`):
   1. (client) → `CONSUME`.
   2. (server) if the clicked block is in `remote_activate`: set the `bound_pos` component to `WorldPos(level.dimension(), clickedPos)`, send action-bar `chat.toms_storage.terminal_bound` ("Bound Advanced Wireless Terminal"), return `SUCCESS`.
   3. Otherwise `PASS`. (Rebinding simply overwrites the component; there is no unbind.)
8. (server) `getRange(player, stack)` = `Config.get().advWirelessRange`. `open(sender, stack)` = `activateTerminal(sender.level(), stack, sender, MAIN_HAND)` (the keybind path, range-and-sync.md). `canOpen(stack)` = true.
9. (client) Tooltip: the base line comes from `ClientUtil.tooltip("adv_wireless_terminal", ...)` with `addShift=true` — without SHIFT only the italic gray "Hold SHIFT for more info." placeholder shows; with SHIFT the lines of `tooltip.toms_storage.adv_wireless_terminal` appear ("Sneak + Right Click on a terminal to bind it." / "Range: %d blocks" (`%d` = `advWirelessRange`) / "The teminal's chunk has to be loaded" — typo, `WIRE-02`).
   - Beacon lines, also shift-gated (passed with `addShift=false`, so they appear only while SHIFT is held): if `wirelessTermBeaconLvl != -1`: `tooltip.toms_storage.adv_wireless_terminal.beacon` ("A nearby beacon in an 8 block radius of the terminal" / "extends the range of the terminal." / "The beacon's level determines the range:") then `...beacon1` ("%d: anywhere in the same dimension.", `%d` = `wirelessTermBeaconLvl`); if `wirelessTermBeaconLvlCrossDim != -1` also `...beacon2` ("%d: anywhere across dimensions.", `%d` = `wirelessTermBeaconLvlCrossDim`).
   - If the item is bound: `tooltip.toms_storage.adv_wireless_terminal.bound` — "Bound to (%s, %s, %s) in %s" with x, y, z and the dimension identifier string — appended directly, so it shows regardless of SHIFT.
   - Always: `tooltip.toms_storage.adv_wireless_terminal.keybind` — "Press the %s key to open", where the argument is `...keybind.outline` ("[%s]", GREEN) wrapping the keybind display name of `key.toms_storage.open_terminal`.
10. (both) Crafting (data-driven recipes, 01-core API-MAP territory): `wireless_terminal` — shaped `PCP / aGg / PEP` (P = `#minecraft:planks`, a = `minecraft:spyglass`, C = `minecraft:comparator`, E = `#c:ender_pearls`, G = `#c:dusts/glowstone`, g = `#c:glass_blocks/colorless`). `adv_wireless_terminal` — `minecraft:smithing_transform` upgrade: base `wireless_terminal` + `minecraft:netherite_ingot` with `minecraft:netherite_upgrade_smithing_template`.

## On 1.15 / On 1.16

- **On 1.15** (`src/main/java/com/tom/storagemod/item/ItemWirelessTerminal.java`): only the basic wireless terminal exists (no advanced item, no binding, no beacon mechanics). Activation is the same raycast + `REMOTE_ACTIVATE` tag check, but directly calls `state.onBlockActivated` (no synthetic-event step) and returns the raw result (no forced `SUCCESS_SERVER`). The item id was `ts.wireless_terminal`.
- **On 1.16** (`src/main/java/com/tom/storagemod/item/ItemAdvWirelessTerminal.java`): the advanced terminal exists with identical behavior, but the binding is stored in **item NBT**: `BindX`/`BindY`/`BindZ` (int) + `BindDim` (string, dimension id) instead of the `bound_pos` data component; the tooltip shows the bound line when the NBT contains `BindX`.

## Deviations in the new mod

None yet.
