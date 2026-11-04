# 03 · Terminal

## Overview

The terminal family: the storage terminal and crafting terminal blocks and their block entities, the storage/crafting terminal menus and screens (search syntax, sorting, ghost mode, control modes, tall mode, recipe book), plus every GUI the storage system opens (filing cabinet, inventory configurator, inventory link, level emitter, item/tag filter GUIs), and the inventory-logic layer the terminals sit on: item stacks, filters, sorting comparators, and the multi-inventory access/tracker abstraction. Documents upstream Tom's Simple Storage 2.11.3, branch `26.2` at commit `95bb61d` (Minecraft 26.2, NeoForge 26.2.0.90). The NeoForge side is the port baseline.

## Contents

| Sheet | What it covers |
|---|---|
| [terminal-blocks.md](terminal-blocks.md) | Terminal blocks (states, shapes, reach, beacon scan) and both block entities |
| [storage-terminal.md](storage-terminal.md) | Storage terminal menu and screens: search syntax, sorting, control/ghost/tall modes, slot actions |
| [crafting-terminal.md](crafting-terminal.md) | Crafting terminal menu and screen: grid, shift-click, auto-fill, recipe book |
| [filing-cabinet-gui.md](filing-cabinet-gui.md) | Filing cabinet GUI: windowed 512-slot cabinet, scroll |
| [inventory-configurator.md](inventory-configurator.md) | Inventory configurator GUI: priority, side, skip, keep-last |
| [inventory-link.md](inventory-link.md) | Inventory link GUI: channel list (create, edit, delete, select, public/private) |
| [level-emitter-gui.md](level-emitter-gui.md) | Level emitter GUI: filter slot, count, less-than, amount buttons |
| [item-filter-gui.md](item-filter-gui.md) | Item and tag filter menus, screens and filter slots |
| [filters.md](filters.md) | `IFilter`, `MultiFilter`, `ItemFilter`, `ItemPredicate`, `SimpleItemFilter`, `TagFilter` |
| [sorting.md](sorting.md) | `SortingTypes` and the six comparators |
| [terminal-stacks.md](terminal-stacks.md) | `StoredItemStack`, `TerminalItemStack` |
| [inventory-access.md](inventory-access.md) | `IInventoryAccess`, `MultiInventoryAccess`, `NetworkInventory`, vanilla multiblock inventories |
| [container-screens.md](container-screens.md) | `TSContainerScreen`, `IScreen` (exclusion areas) |
| [terminal-utils.md](terminal-utils.md) | `IAutoFillTerminal`, `IDataReceiver`, `IValidInfo`, `CraftingMatrix`, `KeyUtil`, `NumberFormatUtil` |

## Also in this area

- [facts.md](facts.md): key facts, related areas, lang pointer.
- [coverage.md](coverage.md): what was read, known limits.
- [bugs.md](bugs.md): suspected bugs and deviations in the new mod.
- [lang.md](lang.md): exact English text of every in-scope lang key.

New mod: [../../new-spec/03-terminal/index.md](../../new-spec/03-terminal/index.md).
