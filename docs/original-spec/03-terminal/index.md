# 03 · Terminal

## Overview

The terminal family: the storage terminal and crafting terminal blocks and their block entities, the storage/crafting terminal menus and screens (search syntax, sorting, ghost mode, control modes, tall mode, recipe book), plus every GUI the storage system opens (filing cabinet, inventory configurator, inventory link, level emitter, item/tag filter GUIs), and the inventory-logic layer the terminals sit on: item stacks, filters, sorting comparators, and the multi-inventory access/tracker abstraction. Documents upstream Tom's Simple Storage 2.11.3, branch `26.2` at commit `95bb61d` (Minecraft 26.2, NeoForge 26.2.0.90). The NeoForge side is the port baseline.

## Contents

- [terminal-blocks.md](terminal-blocks.md): terminal blocks (states, shapes, reach, beacon scan) and both block entities (NBT, crafting, crafting cooldown, beacon level).
- [storage-terminal.md](storage-terminal.md): `StorageTerminalMenu` + `AbstractStorageTerminalScreen` + `StorageTerminalScreen` — search syntax, sorting, control modes, ghost mode, tall mode, scrollbar, stat/beacon icons, slot actions.
- [crafting-terminal.md](crafting-terminal.md): `CraftingTerminalMenu` + `CraftingTerminalScreen` + `TerminalCraftingFiller` + `RecipeBookButton` — grid, shift-click, auto-fill, recipe book.
- [filing-cabinet-gui.md](filing-cabinet-gui.md): `FilingCabinetMenu` + `FilingCabinetScreen` — windowed 512-slot cabinet GUI, scroll.
- [inventory-configurator.md](inventory-configurator.md): `InventoryConfiguratorMenu` + `InventoryConfiguratorScreen` — priority/side/skip/keep-last, connected-block editing.
- [inventory-link.md](inventory-link.md): `InventoryLinkMenu` + `InventoryLinkScreen` — channel list GUI (create/edit/delete/select, public/private).
- [level-emitter-gui.md](level-emitter-gui.md): `LevelEmitterMenu` + `LevelEmitterScreen` — filter slot, count, less-than, amount buttons.
- [item-filter-gui.md](item-filter-gui.md): `AbstractFilteredMenu`/`AbstractFilteredScreen`, `ItemFilterMenu`/`ItemFilterScreen`, `TagItemFilterMenu`/`TagItemFilterScreen`, `FilterSlot`/`ItemFilterSlot`/`PhantomSlot`.
- [filters.md](filters.md): `IFilter`/`MultiFilter`, `ItemFilter`, `ItemPredicate`, `SimpleItemFilter`, `TagFilter`.
- [sorting.md](sorting.md): `SortingTypes` + the six comparators.
- [terminal-stacks.md](terminal-stacks.md): `StoredItemStack`, `TerminalItemStack`.
- [inventory-access.md](inventory-access.md): `IInventoryAccess`, `MultiInventoryAccess` (+ `MultiChangeTracker`), `NetworkInventory`, `VanillaMultiblockInventories`.
- [container-screens.md](container-screens.md): `TSContainerScreen`, `IScreen` (exclusion areas).
- [terminal-utils.md](terminal-utils.md): `IAutoFillTerminal`, `IDataReceiver`, `IValidInfo`, `CraftingMatrix`, `KeyUtil`, `NumberFormatUtil`.

## Key facts

- Menu + screen pairs share one sheet per feature.
- The terminal's item list lives in the **block entity** (`StorageTerminalBlockEntity`), lazily rebuilt on `broadcastChanges` when the change tracker changes; the menu only shuttles data slots + a binary item payload (`TerminalSyncManager`, 01-core `network.md`).
- Storage "slots" in the terminal menu are `SlotStorage` display records, **not** real `Slot`s — all vanilla slot mechanics (shift-click, drag) are re-implemented by the screen + `onInteract` (`SlotAction`).
- `modes` bit layout: 0x00–0x03 control mode (AE/RS/DEF), 0x10 tall mode, 0x20 pull-from-player-inventory (crafting). `sorting` layout: 0x00–0x05 type, 0x100 reverse, 0x200 ghost off.
- `Priority` values: NORMAL, LOW, HIGH, LOWEST, HIGHEST (01-core `platform-layer.md`); multi-inventory pull/push iterates lowest priority first.
- All GUIs talk to the server through one NBT `DataPacket` channel (01-core `network.md`); menu buttons use `handleInventoryButtonClick`.

## Related areas

- `01-core`: platform inventory adapters (`PlatformInventoryAccess`, `InventorySlot`), `DataSlots`/`IntDataSlots`, `TerminalSyncManager` + `SlotAction`, `NetworkHandler`, `Config`, `BeaconLevelCalc`, `TickerUtil`, `AbstractSimpleItemFilter`/`AbstractTagFilter`, `Priority`, `WorldStates`.
- `02-storage`: the blocks the terminals attach to (filing cabinet, inventory connector, cable network, `RemoteConnections`, `BlockFilter`, `IProxy`/`IInventoryLink`/`IInventoryConnectorReference`/`IChangeTrackerAccess`, item components, level emitter block, hoppers, interface).
- `04-wireless`: `WirelessTerminal.getRange` used by the terminal reach check.
- `05-integration`: `PolymorphHelper`, `IAutoFillTerminal` search-handler registration.
- `06-client-ui`: widgets (`EnumCycleButton`, `ToggleButton`, `IconButton`, `ListWidget`, `CraftingTerminalRecipeBookWidget`, `TerminalSearchModeButton`), `ClientUtil`, renderers/models.

## Coverage

- Source files read: 55 of 55 in scope (all files listed in the area assignment, verified against `git ls-tree 26.2` — see batches 03-b8…03-b17).
- Textures referenced (not inspected): `gui/storage_terminal.png` (194×202), `gui/crafting_terminal.png` (194×256), `gui/inventory_configurator.png`, `gui/inventory_link.png`, `gui/level_emitter.png`, `gui/tag_filter.png`, `gui/side_scrollbar.png` (24×115), `widget/clear_button`(+`_disabled`/`_hovered`), `widget/floating_slot` (32×32), `icons/locked_slot`, `icons/refill_on|off`, `icons/add`, `icons/deny`, `icons/allow`, `icons/lock_on|off`, `icons/include_inv|skip_inv`, `icons/keep_last_off|keep_last_1`, `icons/match_tag_on|off`, `icons/greater_than|less_than`; vanilla: `container/generic_54.png`, `container/dispenser.png`, `container/creative_inventory/scroller`.
- Lang keys covered: all keys used by in-scope classes are listed with exact text in the per-sheet "Lang keys" sections and in the table below.
- Corrections made to earlier statements: none.

## Lang keys (in scope, exact text from `NeoForge/src/main/resources/lang/en_us.json`)

| Key | Text |
|---|---|
| `block.toms_storage.storage_terminal` | Storage Terminal |
| `block.toms_storage.crafting_terminal` | Crafting Terminal |
| `block.toms_storage.filing_cabinet` | Filing Cabinet |
| `menu.toms_storage.storage_terminal` | S. Terminal |
| `menu.toms_storage.crafting_terminal` | C. Terminal |
| `menu.toms_storage.filing_cabinet` | Filing Cabinet |
| `menu.toms_storage.inventory_configurator` | Inventory Configurator (%s) |
| `menu.toms_storage.inventory_connector` | Inventory Link |
| `menu.toms_storage.level_emitter` | Level Emitter |
| `chat.toms_storage.terminal_out_of_range` | Terminal out of range |
| `label.toms_storage.inventory_connector.beacon_level` | Beacon Level: %d |
| `narrator.toms_storage.terminal_search` | Terminal Search Field |
| `narrator.toms_storage.inventory_link_channel` | Inventory Link Channel Name Field |
| `narrator.toms_storage.level_emitter_amount` | Level Emitter Item Count |
| `tooltip.toms_storage.storage_terminal` | Gives access to your items.\Place it on an inventory. |
| `tooltip.toms_storage.crafting_terminal` | Gives access to your items.\Place it on an inventory.\Has a builtin crafting table. |
| `tooltip.toms_storage.filing_cabinet` | Store multiple variants of the same item |
| `tooltip.toms_storage.sorting_amount` | Sort by Item Amount |
| `tooltip.toms_storage.sorting_name` | Sort by Item Name |
| `tooltip.toms_storage.sorting_mod` | Sort by Mod |
| `tooltip.toms_storage.sorting_space_efficiency` | Sort by Space Efficiency |
| `tooltip.toms_storage.sorting_id` | Sort by Item ID |
| `tooltip.toms_storage.sorting_type` | Sort by Item Type |
| `tooltip.toms_storage.ctrlMode_ae` | AE Like Controls\Pull one item: Shift+Right Click\Transfer one item: Ctrl+Shift+Right Click |
| `tooltip.toms_storage.ctrlMode_rs` | Refined Storage Like Controls\Pull one item: Middle Click\Transfer one item: Shift+Middle Click |
| `tooltip.toms_storage.ctrlMode_def` | Vanilla Like Controls\Pull one item: Right Click\Transfer one item: Shift+Right Click |
| `tooltip.toms_storage.ghostMode_off` | Disable sorting while holding SHIFT |
| `tooltip.toms_storage.ghostMode_on` | Enable sorting while holding SHIFT |
| `tooltip.toms_storage.tallMode_off` | Terminal Size: Default |
| `tooltip.toms_storage.tallMode_on` | Terminal Size: Auto |
| `tooltip.toms_storage.search` | Standard Search |
| `tooltip.toms_storage.search_keep` | Standard Search Keep |
| `tooltip.toms_storage.search_auto` | Auto Search |
| `tooltip.toms_storage.search_auto_keep` | Auto Search Keep |
| `tooltip.toms_storage.search_sync` | %s Synced Standard Search |
| `tooltip.toms_storage.search_keep_sync` | %s Synced Standard Search Keep |
| `tooltip.toms_storage.search_auto_sync` | %s Synced Auto Search |
| `tooltip.toms_storage.search_auto_keep_sync` | %s Synced Auto Search Keep |
| `tooltip.toms_storage.crafting_pull_on` | Pull missing items from players inventory when crafting |
| `tooltip.toms_storage.crafting_pull_off` | Don't pull from players inventory when crafting |
| `tooltip.toms_storage.recipe_book` | Recipe Book |
| `tooltip.toms_storage.items_missing` | Some items may not be inserted into the crafting grid |
| `tooltip.toms_storage.amount` | Item Amount: %d |
| `tooltip.toms_storage.slots_used` | Used Slots: %d |
| `tooltip.toms_storage.terminal_beacon` | Beacon level: %d%s |
| `tooltip.toms_storage.terminal_beacon.anywhere` | This terminal is accessible anywhere\nwith the Advanced Wireless Terminal |
| `tooltip.toms_storage.terminal_beacon.sameDim` | This terminal is accessible anywhere\nin this dimension\nwith the Advanced Wireless Terminal |
| `tooltip.toms_storage.terminal_stat.slots` | All slots: %s |
| `tooltip.toms_storage.terminal_stat.free` | Free slots: %s (%s%%) |
| `tooltip.toms_storage.terminal_stat.unique` | Unique items: %s |
| `tooltip.toms_storage.terminal_stat.alot` | Alot (>32k) |
| `tooltip.toms_storage.inv_config.add_connected` | Add Connected Blocks |
| `tooltip.toms_storage.inv_config.remove_all_connected` | Remove All Connected Blocks |
| `tooltip.toms_storage.inv_config.remove_filter` | Clear Configuration |
| `tooltip.toms_storage.inv_config.include` | Include in Inventory Connectors |
| `tooltip.toms_storage.inv_config.skip` | Exclude from Inventory Connectors |
| `tooltip.toms_storage.keepLast_off` | Don't keep last item in slot |
| `tooltip.toms_storage.keepLast_on` | Keep last item in slot |
| `tooltip.toms_storage.priority_normal` | Priority: Normal |
| `tooltip.toms_storage.priority_low` | Priority: Low |
| `tooltip.toms_storage.priority_high` | Priority: High |
| `tooltip.toms_storage.priority_lowest` | Priority: Lowest |
| `tooltip.toms_storage.priority_highest` | Priority: Highest |
| `tooltip.toms_storage.side_down` | Side: Down |
| `tooltip.toms_storage.side_east` | Side: East |
| `tooltip.toms_storage.side_north` | Side: North |
| `tooltip.toms_storage.side_south` | Side: South |
| `tooltip.toms_storage.side_up` | Side: Up |
| `tooltip.toms_storage.side_west` | Side: West |
| `tooltip.toms_storage.inv_link.private` | Private Channel |
| `tooltip.toms_storage.inv_link.public` | Public Channel |
| `tooltip.toms_storage.inventory_connector.channel.owner` | Owner: %s |
| `tooltip.toms_storage.inventory_connector.channel.owner.self` | Owner: %s (You) |
| `tooltip.toms_storage.inventory_connector.channel.owner.unknown` | Owner: Unknown |
| `tooltip.toms_storage.level_emitter` | Emits a redstone signal based on Inventory System contents. |
| `tooltip.toms_storage.level_emitter.greater_than` | Emit when more than specified in storage |
| `tooltip.toms_storage.level_emitter.less_than` | Emit when less than specified in storage |
| `tooltip.toms_storage.allowList` | Allow matching |
| `tooltip.toms_storage.denyList` | Reject matching |
| `tooltip.toms_storage.matchNBT_on` | Match Components |
| `tooltip.toms_storage.matchNBT_off` | Don't match Components |
| `tooltip.toms_storage.item_filter` | Allow filtering multiple items with components |
| `tooltip.toms_storage.item_filter.contents` | Contents: |
| `tooltip.toms_storage.item_filter.no_items` | No items |
| `tooltip.toms_storage.item_filter.prefix` | - %s |
| `tooltip.toms_storage.tag_item_filter` | Filter using tags |
| `item.toms_storage.item_filter` | Item Filter |
| `item.toms_storage.tag_item_filter` | Tag Item Filter |
| `item.toms_storage.polymorphic_item_filter` | Polymorphic Item Filter |

Narrator keys referenced in code but **absent** from the shipped `en_us.json` (fall back to the key string): `narrator.toms_storage.terminal_sort`, `narrator.toms_storage.direction`, `narrator.toms_storage.search_type`, `narrator.toms_storage.ctrl_mode`, `narrator.toms_storage.ghost_mode`, `narrator.toms_storage.tall_mode`, `narrator.toms_storage.priority`, `narrator.toms_storage.side`, `narrator.toms_storage.skip`, `narrator.toms_storage.add`, `narrator.toms_storage.removeAll`, `narrator.toms_storage.removeFilter`.

## Known limits

- Not run in a real pack: GUI pixel positions were read from code, not verified against the textures (texture files referenced but not inspected).
- `TerminalSyncManager`'s binary item payload format is documented in 01-core (`terminal-sync`); only its usage is covered here.
- `CraftingTerminalRecipeBookWidget` and the button widgets (06-client-ui) are used but not documented here — their internals (ghost recipe rendering, `fillGhostRecipe`, search box) are only described at the call-site level.
- The `opt.*` search tooltips (`tooltip.toms_storage.opt.search_*`) are produced by 01-core `Config` option descriptors, not by in-scope classes.
- `Priority` ordering and `BeaconLevelCalc` behavior are assumed from 01-core.

## Suspected bugs

| ID | Title | Location | What looks wrong | Severity |
|---|---|---|---|---|
| TERM-01 | Mouse-wheel scroll step hardcodes 5 rows | `NeoForge/src/platform-shared/java/com/tom/storagemod/screen/AbstractStorageTerminalScreen.java:688` | `i = (size+8)/9 - 5` computes the scroll range as if 5 rows were visible; in tall mode (rowCount up to ~46) each wheel notch moves the wrong fraction of the list. | gameplay |
| TERM-02 | Unbounded enum index on stale sort NBT | `NeoForge/src/platform-shared/java/com/tom/storagemod/screen/AbstractStorageTerminalScreen.java:138` | `sortingType = SortingTypes.VALUES[sorting & 0xFF]` has no bounds check; a value ≥ 6 (edited/corrupted `sorting` NBT from an older version) throws `ArrayIndexOutOfBoundsException` when the GUI opens. | crash |
| TERM-03 | Stat tooltip divides by zero slot count | `NeoForge/src/platform-shared/java/com/tom/storagemod/screen/AbstractStorageTerminalScreen.java:461` | `Math.round(menu.freeCount / (float) menu.slotCount * 100)` guards only the 32767 case; with slotCount 0 (empty network) 0/0 = NaN rounds to 0, showing "0%" (Infinity if free > 0). | cosmetic |
| TERM-04 | Beacon scan gate unsatisfiable for one position hash | `NeoForge/src/platform-shared/java/com/tom/storagemod/block/entity/StorageTerminalBlockEntity.java:148` | `level.getGameTime() % 40 == Math.abs(worldPosition.hashCode()) % 40`: if the block position's hash is `Integer.MIN_VALUE`, `Math.abs` stays negative and the comparison never holds, so `beaconLevel` is never computed. | gameplay |
| TERM-05 | Auto-fill item accounting not reserved per cell | `NeoForge/src/platform-shared/java/com/tom/storagemod/menu/TerminalCraftingFiller.java:41-67` | `placeRecipe` accounts player-inventory stacks once (`allItems`) but pulls lazily per grid cell; suspected double-claim window when two cells need the same scarce item (each pull checks success, so this is an edge, not a proven loss). | gameplay |
| TERM-06 | Filing cabinet wheel scroll divides by (rows−5) | `NeoForge/src/platform-shared/java/com/tom/storagemod/screen/FilingCabinetScreen.java:93-94` | `i = (size+8)/9 - 5` is 0 when the cabinet holds exactly 45 items → `delta / 0.0f` = ±Inf, clamped to 0/1 (instant full jump instead of a no-op). | cosmetic |
| TERM-07 | Keep-last button narrator reuses "skip" | `NeoForge/src/platform-shared/java/com/tom/storagemod/screen/InventoryConfiguratorScreen.java:70-71` | `buttonKeepLast` is built with `.name(Component.translatable("narrator.toms_storage.skip"))` — screen-reader text says "skip" for the keep-last toggle. | cosmetic |
| TERM-08 | Typed channel name lookup is ambiguous | `NeoForge/src/platform-shared/java/com/tom/storagemod/screen/InventoryLinkScreen.java:130-137` | Creating channels never checks name uniqueness; the text-field responder selects the first channel whose display name equals the typed text, so duplicate names make the lookup order-dependent. | cosmetic |
| TERM-09 | ComparatorType lookup not thread-safe | `NeoForge/src/platform-shared/java/com/tom/storagemod/inventory/sorting/ComparatorType.java:30-39` | Static `LOOKUP` (SoftReference) and `lastAccess` are read/written without synchronization; parallel terminal sorting can rebuild the lookup concurrently (benign race, wasted work). | performance |
| TERM-10 | Level emitter SHIFT-step table is asymmetric | `NeoForge/src/platform-shared/java/com/tom/storagemod/screen/LevelEmitterScreen.java:83-91` | The +1000 button's SHIFT step is −64 (not +1000 doubled), and the negative buttons' SHIFT steps are not consistent doubles of their base steps; the "double with SHIFT" pattern the other buttons follow is broken. | cosmetic |
| TERM-11 | Item-filter shift-click fill order | `NeoForge/src/platform-shared/java/com/tom/storagemod/menu/ItemFilterMenu.java:95-102` | Shift-click places a copy in the first slot that is empty *or* holds a different item, but the scan breaks at the first matching item — a grid already containing the item at slot k will never fill empty slots after k (inferred edge, no repro). | gameplay |
| TERM-12 | MultiChangeTracker stale array after same-tick network growth | `NeoForge/src/platform-shared/java/com/tom/storagemod/inventory/MultiInventoryAccess.java:194-223` | `trackers`/`items` arrays are resized only when `connected.size()` differs at the top of `getChangeTracker`; if `connected` grows later in the same game tick (network `build` before first read), `trackers[i]` indexes a shorter array → `ArrayIndexOutOfBounds`. | crash |

## Deviations in the new mod

None yet (port notes go to the new-spec 03-terminal area).
