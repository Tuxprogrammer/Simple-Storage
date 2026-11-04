# Terminal · coverage and limits

Part of [03-terminal](index.md).

## Coverage

- Source files read: 55 of 55 in scope (all files listed in the area assignment, verified against `git ls-tree 26.2` — see batches 03-b8…03-b17).
- Textures referenced (not inspected): `gui/storage_terminal.png` (194×202), `gui/crafting_terminal.png` (194×256), `gui/inventory_configurator.png`, `gui/inventory_link.png`, `gui/level_emitter.png`, `gui/tag_filter.png`, `gui/side_scrollbar.png` (24×115), `widget/clear_button`(+`_disabled`/`_hovered`), `widget/floating_slot` (32×32), `icons/locked_slot`, `icons/refill_on|off`, `icons/add`, `icons/deny`, `icons/allow`, `icons/lock_on|off`, `icons/include_inv|skip_inv`, `icons/keep_last_off|keep_last_1`, `icons/match_tag_on|off`, `icons/greater_than|less_than`; vanilla: `container/generic_54.png`, `container/dispenser.png`, `container/creative_inventory/scroller`.
- Lang keys covered: all keys used by in-scope classes are listed with exact text in the per-sheet "Lang keys" sections and in the table below.
- Corrections made to earlier statements: none.

## Known limits

- Not run in a real pack: GUI pixel positions were read from code, not verified against the textures (texture files referenced but not inspected).
- `TerminalSyncManager`'s binary item payload format is documented in 01-core (`terminal-sync`); only its usage is covered here.
- `CraftingTerminalRecipeBookWidget` and the button widgets (06-client-ui) are used but not documented here — their internals (ghost recipe rendering, `fillGhostRecipe`, search box) are only described at the call-site level.
- The `opt.*` search tooltips (`tooltip.toms_storage.opt.search_*`) are produced by 01-core `Config` option descriptors, not by in-scope classes.
- `Priority` ordering and `BeaconLevelCalc` behavior are assumed from 01-core.
