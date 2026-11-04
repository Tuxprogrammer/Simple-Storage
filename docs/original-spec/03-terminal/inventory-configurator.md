# Inventory configurator (menu + screen)

| # | Field | Contents |
|---|---|---|
| 1 | **Identity** | `InventoryConfiguratorMenu` (SH `menu/InventoryConfiguratorMenu.java`), `InventoryConfiguratorScreen` (SH `screen/InventoryConfiguratorScreen.java`, texture `toms_storage:textures/gui/inventory_configurator.png`). Menu type `Content.invConfigMenu`; title `menu.toms_storage.inventory_configurator` = "Inventory Configurator (%s)". Opened by right-clicking an inventory-connector-managed block with the Inventory Configurator item (02-storage `inventory-connector.md`). Sources: branch `26.2`, `NeoForge/src/platform-shared/java/com/tom/storagemod/`; `menu/InventoryConfiguratorMenu.java:NN` (M), `screen/InventoryConfiguratorScreen.java:NN` (S). (S:24-25, M:23) |
| 2 | **Inputs** | The target `BlockFilter` (02-storage `inventory-connector.md` — priority, side, skip, keep-last, connected blocks, item filter); the item held in the hand used to open the GUI; the filter item in the single `ItemFilterSlot`. |
| 3 | **Numbers** | Filter slot at (80, 32) (M:44). Player slots: 3 rows at y=84 (M:46-50), hotbar at y=142 (M:52-54). Buttons in the left column at (leftPos−18, topPos+5+18n): 0 priority, 1 add-connected, 2 remove-all-connected, 3 side, 4 skip toggle, 5 keep-last toggle (S:39, 44, 49, 54, 59, 70). Remove-filter (clear configuration) button 20×18 at (leftPos+imageWidth−22, topPos−22) — above the GUI (S:84; the 20×18 size is not in this file, UNVERIFIED here). Menu stays valid while the player is within 16 blocks squared (distance² < 256) (M:69). |
| 4 | **Behavior** | See Behavior detail. |
| 5 | **Determinism** | None (no randomness). |
| 6 | **Data formats** | Data slots (server→client): priority (int → `Priority.VALUES[v % len]`) (M:56), side (int → `Direction.from3DDataValue`) (M:57), skip (int 0/1) (M:58), keep-last (int 0/1) (M:59). Menu button id encoding (M:74-75; client side S:41, 56, 64, 75, 85, 115): `(arg << 3) | mode` — mode 0 = set priority (arg = priority index), 1 = add connected blocks, 2 = remove all connected (reset to main block only), 3 = set side (arg = direction ordinal), 4 = set skip (arg 0/1), 5 = set keep-last (arg 0/1), 6 = open the item filter's own GUI (arg = slot index, used by the middle-click path), 7 = remove filter (delete the whole BlockFilter at the target position) (M:77-131). |
| 7 | **Output** | Mutations of the target `BlockFilter` (priority/side/skip/keep-last/connected blocks/item filter), removal of the block filter, opening of nested filter GUIs, block-entity re-sync (`blockEntityChanged`) after every button press and on menu close (server). |
| 8 | **Relationships** | `BlockFilter` (02-storage `inventory-connector.md`), `ConfiguratorComponent` (02-storage `components.md` — `startSelection`/`setSelection` write the connected-block list into the held configurator item), `Priority` (01-core `platform-layer.md`), `ItemFilterSlot` + `IItemFilter` (03-terminal `item-filter-gui.md`, 02-storage), `PlatformInventoryAccess.removeBlockFilterAt` (01-core), `TSContainerScreen` (03-terminal `container-screens.md`), widgets `EnumCycleButton`, `IconButton`, `ToggleButton` (06-client-ui). |
| 9 | **NeoForge dependencies** | `DataSlots`, `Direction.from3DDataValue` (1.7.10: `EnumFacing` ordinal), `blockEntityChanged` (1.7.10: `markForUpdate`), `ItemStack.CODEC` not used here. No direct 1.7.10 counterpart: container data slots. |
| 10 | **Bugs** | `TERM-07` (buttonKeepLast is built with the wrong narrator name — reuses "narrator.toms_storage.skip" instead of a keep-last name, so screen-reader text is wrong). |

## Behavior detail

### Menu (server)

1. Slots: 0 = `ItemFilterSlot(inv, 0, 80, 32)` over `f.filter` (the filter's own 1-slot container; a `SimpleContainer(1)` when opened without a filter); 1..36 = player main + hotbar (standard layout, y=84/142) (M:42-54).
2. `quickMoveStack`: always EMPTY (no shift-click movement) (M:62-65).
3. `stillValid`: `pos == null || player.distanceToSqr(pos) < 256 && !isClosed` (M:68-70).
4. `clickMenuButton(player, id)` (M:73): decode `mode = id & 7`, `arg = id >> 3` (M:74-75):
   - 0: `filter.setPriority(Priority.VALUES[arg % len])` (M:78).
   - 1 (add connected): if the item in `hand` is the Inventory Configurator — `new ConfiguratorComponent.Configurator(item, gameTime).startSelection(pos, filter.getConnectedBlocks())` and `isClosed = true` (M:83-88; closes the menu; the configurator overlay takes over, 02-storage `components.md`/`inventory-connector.md`).
   - 2 (remove all): connected blocks = {main block only}; if the held item is the configurator, `setSelection(filter.getConnectedBlocks())` (M:94-99).
   - 3: `filter.setSide(Direction.from3DDataValue(arg))` (M:105).
   - 4: `filter.setSkip(arg != 0)` (M:109).
   - 5: `filter.setKeepLast(arg != 0)` (M:113).
   - 6: slot `arg` (if an `ItemFilterSlot` holding an `IItemFilter`) → `f.openGui(stack, player, () -> s.getItem().getItem() == f, filter::markFilterDirty)` — opens the item filter's own GUI (03-terminal `item-filter-gui.md`) (M:118-122; slot index bounds-checked, M:118).
   - 7 (remove filter): `isClosed = true`; `PlatformInventoryAccess.removeBlockFilterAt(level, pos)` (M:128-129).
   - After any mode: `player.level().blockEntityChanged(pos)` (re-sync the connector BE) (M:137). Returns true (M:139).
5. `removed`: server → `blockEntityChanged(pos)` (M:143-146).

### Screen (client)

6. `init` (S:36): priority `EnumCycleButton` (narrator "priority", icon prefix "priority", cycles `Priority.VALUES`, tooltip `tooltip.toms_storage.priority_<name>`), side `EnumCycleButton` (narrator "side", `Direction.values()`, tooltip `tooltip.toms_storage.side_<name>` — note: `Direction.values()` order is the enum order, not the 6-face display order), add-connected icon button (`icons/add`, tooltip `tooltip.toms_storage.inv_config.add_connected` = "Add Connected Blocks"), remove-all icon button (`icons/deny`, tooltip `inv_config.remove_all_connected` = "Remove All Connected Blocks"), skip toggle (`icons/include_inv`/`icons/skip_inv`, tooltip `inv_config.include` = "Include in Inventory Connectors" / `inv_config.skip` = "Exclude from Inventory Connectors"), keep-last toggle (`icons/keep_last_off`/`icons/keep_last_1`, tooltip `keepLast_off` = "Don't keep last item in slot" / `keepLast_on` = "Keep last item in slot") — **`TERM-07`: its builder `.name(...)` passes `narrator.toms_storage.skip` (copy-paste; should be a keep-last narrator key)** (S:71). Remove-filter button (`icons/deny`, tooltip `inv_config.remove_filter` = "Clear Configuration") above the GUI (S:84-87).
7. `extractRenderState`: button states are re-read from the menu every frame (priority/side/skip/keep-last) — server data slots drive the visuals (S:91-97).
8. `mouseClicked`: middle click (button 1) over the `ItemFilterSlot` holding an `IItemFilter` → `click((slot.index << 3) | 6)` (open the filter GUI) (S:113-117; UNVERIFIED: code tests `button() == 1`, which is the right mouse button in GLFW numbering, not middle, S:114).
9. `getExclusionAreas`: Box(leftPos−20, topPos, 25, 110) (S:122-123).

## Lang keys used by this feature

- `menu.toms_storage.inventory_configurator` = "Inventory Configurator (%s)"
- `tooltip.toms_storage.inv_config.add_connected` = "Add Connected Blocks"; `inv_config.remove_all_connected` = "Remove All Connected Blocks"; `inv_config.remove_filter` = "Clear Configuration"; `inv_config.include` = "Include in Inventory Connectors"; `inv_config.skip` = "Exclude from Inventory Connectors"
- `tooltip.toms_storage.keepLast_off` = "Don't keep last item in slot"; `keepLast_on` = "Keep last item in slot"
- `tooltip.toms_storage.priority_normal` = "Priority: Normal"; `priority_low` = "Priority: Low"; `priority_high` = "Priority: High"; `priority_lowest` = "Priority: Lowest"; `priority_highest` = "Priority: Highest"
- `tooltip.toms_storage.side_down` = "Side: Down"; `side_east` = "Side: East"; `side_north` = "Side: North"; `side_south` = "Side: South"; `side_up` = "Side: Up"; `side_west` = "Side: West"
- `narrator.toms_storage.priority` / `narrator.toms_storage.side` / `narrator.toms_storage.skip` — narrator keys (exact text not in the shipped en_us file; narrator keys used by widgets are declared here for completeness)
- `narrator.toms_storage.add` = narrator for the add-connected button; `narrator.toms_storage.removeAll`; `narrator.toms_storage.removeFilter` (same note)

## Deviations in the new mod

See the [03-terminal deviation table](../../new-spec/03-terminal/deviations.md#deviation-table) (row `inventory-configurator`).
