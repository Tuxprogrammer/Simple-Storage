# 03 Terminal · what milestone M03 shipped

Part of [03 Terminal](index.md). Written when M03 closed; [deviations.md](deviations.md) says what differs from upstream, this page says what exists in code and what waits for a later milestone.

## Shipped

| Original sheet | 1.7.10 code (package com.tuxprogrammer.simplestorage) | Tests |
|---|---|---|
| sorting | sort.SortingTypes, sort.SortKey (five modes, ordinal 5 reserved and inert) | SortingTypesTest (TERM-02, TERM-09 cannot occur) |
| filters | filter.ItemFilters, filter.FilterKey, filter.FilterSlotLogic, item.ItemFilterItem, item.TagFilterItem (ore-dictionary names, D20) | ItemFiltersTest, FilterSlotLogicTest (TERM-11), TagFilterItemTest |
| terminal-stacks and inventory-access | inventory.StoredStack, inventory.MultiInventory (priority order, keepLast, offsets), blockentity.StackKeys, blockentity.InventorySlots, blockentity.TerminalEntry, blockentity.TerminalBackend | MultiInventoryTest (CORE-03, CORE-04, TERM-12), TerminalBackendTest |
| storage-terminal modes and search | terminal.TerminalModes (the `sorting`, `modes` and `searchType` words), terminal.SearchQuery, terminal.TerminalView (client list model), terminal.SlotAction, terminal.ScrollMath (TERM-01, TERM-06), terminal.UsageStats (TERM-03), terminal.NumberFormat | TerminalModesTest, SearchQueryTest, TerminalViewTest, ScrollMathTest, UsageStatsTest, NumberFormatTest |
| terminal-blocks | block.StorageTerminalBlock, block.CraftingTerminalBlock, blockentity.StorageTerminalTileEntity (beacon gate every 40 ticks, TERM-04), blockentity.CraftingTerminalTileEntity | StorageTerminalTileEntityTest |
| crafting-terminal and terminal-utils | menu.CraftingTerminalContainer (grid, result, `autoFill` per cell, TERM-05), terminal.AutoFillPlanner, terminal.IAutoFillTerminal, terminal.KeyMods (CORE-10) | AutoFillPlannerTest, CraftingAutoFillTest, KeyModsTest |
| menus | menu.StorageTerminalContainer, menu.InventoryView, menu.FilingCabinetContainer (TERM-13), menu.LevelEmitterContainer, menu.ItemFilterContainer, menu.TagFilterContainer | FilingCabinetContainerTest, LevelEmitterContainerTest, ItemFilterContainerTest |
| network | network.TerminalListPacket, TerminalStatePacket (client-bound), TerminalActionPacket, TerminalSettingsPacket, FilingCabinetScrollPacket, EmitterSettingsPacket, FilterFlagsPacket, TagFilterPacket (server-bound), all through the validated base classes | TerminalPacketsTest, TerminalListPacketTest |
| GUI | gui.GuiIds, gui.GuiHandler, client.ClientProxy, client.screen.StorageTerminalScreen, CraftingTerminalScreen, FilingCabinetScreen, LevelEmitterScreen, ItemFilterScreen, TagFilterScreen | compiled and booted by the dev client |
| widget math | ui.ScrollbarModel, ui.PopupRects, ui.EnumCycle | ScrollbarModelTest, PopupRectsTest, EnumCycleTest (UI-04, UI-05, UI-06) |

## Registration

15 blocks, 3 items and 10 tile entities are registered always; `modules.terminal` gates opening the GUIs and the terminal tick, never registration; GUI ids are in `gui.GuiIds` and never renumbered.

## Deferred

| What | Milestone | Why |
|---|---|---|
| the inventory configurator, `BlockFilter` and `bound_pos` | M04 | needs the connected-block data model (D34) |
| the inventory-link GUI and the `simplestorage_rc` saved data | M04 | wireless and linking |
| the beacon scan body (`rescanBeacon` sets -1 today) | M04 | wireless rules own it |
| recipe-viewer search sync and viewer-driven auto-fill | M06 | NEI plugin |
| textures, item icons and block models | M06 | art pass (the screens draw plain panels) |
| the popup menu widget users (search-mode popup) | not ported | PopupRects exists for the fix of UI-06 but no screen uses a popup yet |

## Testing notes

- Logic classes are plain-Java unit tests with spec tables.
- Menus, items, tile entities and packets that need real registries run as `ingame` scenarios (`tools/remote/work.sh server`).
- The first in-game run found real bugs: player slots indexed past the end of the inventory, an empty source stack left after shift-click, the tag filter writing a list it could not read back, a backend pull before any scan.
