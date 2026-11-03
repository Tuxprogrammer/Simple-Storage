# System: Screen widget kit

| # | Field | Value |
|---|---|---|
| 1 | **Identity** | `com.tom.storagemod.screen.widget.IconButton` (`SH/screen/widget/IconButton.java`) — base 16×16 icon button; `ToggleButton` (same package) — on/off icon toggle with dual tooltips; `EnumCycleButton<T extends Enum<T>>` (same package) — enum state cycler; `TerminalSearchModeButton` (same package) — the terminal search-mode bitfield button; `ListWidget<T>` (same package) — scrollable entry list with custom scrollbar; `CraftingTerminalRecipeBookWidget` (same package) — vanilla recipe-book component adapted to the crafting terminal; `com.tom.storagemod.util.PopupMenuManager` (`SH/util/PopupMenuManager.java`) — the tooltip-anchored popup menu mechanism (util package, but a pure UI mechanism); `com.tom.storagemod.screen.RecipeBookButton` (`SH/screen/RecipeBookButton.java`) — the recipe-book open/close button used by the crafting terminal screen |
| 2 | **Inputs** | Mouse (position, click, wheel), keyboard (arrows, ESC, enter, text characters for `TextFieldElement`), the owning `Screen` (for `PopupMenuManager` and the recipe book), the enum value arrays and state-update callbacks supplied by the screens (area `03-terminal`/`02-storage`), `IAutoFillTerminal.getHandlerNameOr(...)` for the search-mode labels |
| 3 | **Numbers** | Icon buttons: 16×16 px, drawn from the `gui` atlas sprites `widget/slot_button`, `widget/slot_button_disabled`, `widget/slot_button_hovered` (nine-slice, 16×16, 2 px border) + a 16×16 icon blit. List widget: entry height passed by the subclass (16 px in `InventoryLinkScreen.ListHandler`, list 114×100 px → 6 visible lines), list-button sprites `widget/small_button` (nine-slice 106×16, 2 px border) / `widget/small_button_selected` / `widget/small_scroll` (5×9 px thumb). Scrollbar grab rect: hard-coded at `x+56`, width 14 px, `y−1` to `y+57` (`UI-05`). Popup menu: entries 100×8 px each, click-tested at `(x, y+i*10)`; rendered as a tooltip anchored at `(x−12, y+12)` (`UI-06`). Search-mode icon overlays: 14×14 px at (x+1, y+1), base `icons/search_mode` + overlays `icons/search_mode_auto` (bit 1), `icons/search_mode_keep` (bit 2), `icons/search_mode_sync` (bit 4). `CraftingTerminalRecipeBookWidget`: grid limit 3×3 (shaped: `width() <= 3 && height() <= 3`; shapeless: `ingredients().size() <= 9`), crafting slots = `slot.index < 10`; uses vanilla `recipe_book/filter_*` textures and the 5 vanilla crafting tabs |
| 4 | **Behavior** | See Behavior detail |
| 5 | **Determinism** | All client-side, per-frame. `EnumCycleButton`/`ToggleButton` state changes fire the screen's state-update callback, which sends the server a sync packet (the menu/screen does the networking — area `03`/`02`). `ListWidget.scroll` step = `dir / (listSize − visibleLines)` (a full list view needs `listSize − visibleLines` wheel ticks to traverse). `PopupMenuManager` state lives only in the field: open → click/ESC/arrow-navigate → close |
| 6 | **Data formats** | No NBT. `EnumCycleButton` icon id: `toms_storage:icons/<prefix>_<enum name lower-cased>` (prefixes in use: `sort`, `control`, `priority`, `side` — matching the 45 icon sprites in `textures/gui/sprites/icons/`). Search-mode bitfield: `searchType` int with bits 1 (auto), 2 (keep), 4 (sync), 8 (smart *off*). Popup `TextFieldElement` renders the typed value with a `_` cursor inserted at the edit-box cursor position (the `_` literal is hard-coded) |
| 7 | **Output** | (client) icon buttons with hover/press feedback; cycled enum/toggle states (icon + tooltip swap); a scrollable list with a 5×9 px thumb; the popup menu rendered as a styled tooltip (AQUA on hover/selected, GRAY otherwise); the recipe book attached to the crafting terminal with ghost-recipe placement into the terminal's own grid slots |
| 8 | **Relationships** | The screens that instantiate the widgets (area `03-terminal`: sort type, sort reverse, control mode, ghost mode, tall mode, search mode; area `02-storage`: configurator priority/side/skip/add/remove buttons, the inventory-link list) — see [client-package.md](client-package.md) for the client entry point and [lang.md](lang.md) for the tooltip/narrator keys. `CraftingTerminalRecipeBookWidget` depends on `CraftingTerminalMenu.getInputGridSlots()`/`getCraftingResultSlot()` (area `03-terminal`). `TerminalSearchModeButton` depends on `IAutoFillTerminal` (area `05-integration` seam) and `PopupMenuManager`. `RecipeBookButton` depends on the menu's recipe-book state (area `03-terminal`) |
| 9 | **NeoForge dependencies** | Vanilla (platform-neutral) GUI APIs only: `Button`/`AbstractWidget`, `WidgetSprites` + `blitSprite(RenderPipelines.GUI_TEXTURED, ...)` (1.7.10: `GuiDrawHelper.blit` + custom nine-slice scaling, pending S2), `Tooltip`, `EditBox`, `RecipeBookComponent`/`GhostSlots`/`PlaceRecipeHelper` (1.7.10: vanilla crafting book equivalent), `GLFW` (mouse-button state in `ListWidget.preRender`), `ChatFormatting`. `WidgetSprites`/nine-slice mcmeta has no 1.7.10 counterpart (custom sprite scaling, pending S2) |
| 10 | **Bugs** | `UI-02`, `UI-04`, `UI-05`, `UI-06`, `UI-07` (see [index](index.md)) |

## Behavior detail

All steps are **(client)**.

### `IconButton`

1. Constructor `IconButton(x, y, name, icon, pressable)` (or `Runnable` overload): stores the name/icon/handler in its own fields and passes `Component.empty()` + `Button.DEFAULT_NARRATION` to the `Button` super — the base button is therefore nameless for narration purposes (`UI-07`).
2. `extractContents` (render): if visible, update `isHovered` from the mouse, blit the nine-slice background sprite chosen by `(active, isHoveredOrFocused)` from `SPRITES` (normal / disabled / hovered variants), then `drawIcon` blits the icon over the full 16×16.
3. `onPress`: delegates to the `ButtonPressHandler.onPress(button, input)` with the full modifier state.

### `ToggleButton`

1. Builder: `ToggleButton.builder(x, y).name(c).iconOn(id).iconOff(id).build(consumer)`.
2. `onPress`: toggles `state` and passes the new value to the `BooleanConsumer` callback (the screen sends the sync packet).
3. `setTooltip(ttFalse, ttTrue)` stores both; the active tooltip follows `state`. The single-arg `setTooltip` overload maps to `(p, p)`.
4. `getIcon`: `iconOn` when on, else the base icon (off).

### `EnumCycleButton<T>`

1. Constructor `EnumCycleButton(x, y, name, prefix, values, stateUpdate)`: icon function `e -> toms_storage:icons/<prefix>_<e.name().toLowerCase()>`; initial `state = null` until `setState` is called (`UI-04`).
2. Press: `stateUpdate.accept(values[(state.ordinal() + values.length + (hasShiftDown ? −1 : 1)) % values.length])` — forward cycle, shift = reverse.
3. `setState(v)`: on change, swaps the tooltip via `tooltipFactory` (if set) and stores the value.
4. Narration: `name + " " + Component.translatable("narrator.toms_storage.button_state." + state.name().toLowerCase())` — the `button_state.*` keys do not exist in any lang file (`UI-02`).

### `TerminalSearchModeButton`

1. Built with `Component.translatable("")` as its name (empty key), base icon `icons/search_mode`; `searchType` starts at −1 (no bits set).
2. `drawIcon`: base icon at (x, y, 16, 16), then 14×14 overlays at (x+1, y+1) for each set bit: auto (1), keep (2), sync (4).
3. Click: opens a `PopupMenuManager` at (x+16, y+16) with four `ButtonElement`s:
   - "auto select search" — `tooltip.toms_storage.opt.search_auto` + yes/no (bit 1); toggles bit 1 and runs `sendUpdate`.
   - "keep search" — `tooltip.toms_storage.opt.search_keep` (bit 2).
   - "sync search with <viewer>" — `tooltip.toms_storage.opt.search_sync` with the viewer name from `IAutoFillTerminal.getHandlerNameOr(I18n.get("tooltip.toms_storage.recipe_book"))`; shown dark-grayed with NO when `canSync` is false, but still toggles bit 4.
   - "smart features with <viewer> (May lag)" — `tooltip.toms_storage.opt.search_smart`; YES when bit 8 is **clear** (bit 8 = smart off), same graying rule.
4. `setSearchType(v)`: on change, rebuilds the tooltip key `tooltip.toms_storage.search` + `_auto` (bit 1) + `_keep` (bit 2) + `_sync` (bit 4) — all four keys exist in the lang file — with the viewer-name argument.

### `ListWidget<T>`

1. Constructor `(x, y, w, h, elemH, narrator)`: creates `h / elemH` `ListEntry` widgets at `y + i*elemH`.
2. `preRender` (called by the screen before render): polls the GLFW left mouse button; if the button goes down inside the hard-coded grab rect (`x+56 .. x+70`, `y−1 .. y+57`) and scrolling is needed, `isScrolling = true`; while scrolling, `currentScroll = clamp((mouseY − l − 4.5) / (58 − 9), 0, 1)` — note the denominator is the fixed 49 px, not the list's actual height (`UI-05`).
3. `extractWidgetRenderState`: draws the 5×9 px scroll thumb at `x + width − 6`, `y − 1 + (height − 9) * currentScroll`, with hovered/normal/disabled `small_scroll` sprites.
4. `mouseScrolled`: when scrolling is needed, `currentScroll = clamp(currentScroll − dir / (listSize − visibleLines), 0, 1)` and consumes the event.
5. Entries render via the subclass's `renderEntry`; selection via the generated `small_button`/`small_button_selected` row buttons; `tooltip` renders the hovered entry's `renderTooltip` content.

### `CraftingTerminalRecipeBookWidget`

1. Extends vanilla `RecipeBookComponent<CraftingTerminalMenu>` with the 5 vanilla crafting tabs (crafting, equipment, building blocks, misc, redstone).
2. `canDisplay`: shaped displays with `width() <= 3 && height() <= 3`; shapeless displays with at most 9 ingredients; anything else is filtered out.
3. `isCraftingSlot`: `slot.index < 10` (the 3×3 grid + result slot in `CraftingTerminalMenu`).
4. `fillGhostRecipe`: places the display's result on the menu's crafting-result slot, then fills the grid via `menu.getInputGridSlots()` — shaped: `PlaceRecipeHelper.placeRecipe(3, 3, width, height, ingredients, (slotDisplay, ix, jx, k) -> ghostSlots.setInput(list.get(ix), ...))` (row-major sequential grid slots); shapeless: the first `min(ingredients, gridSlots)` ingredients in order.
5. Filter button uses the vanilla `recipe_book/filter_*` textures; filter tooltip = `gui.recipebook.toggleRecipes.craftable` (vanilla key).

### `PopupMenuManager` (util package)

1. `open(x, y, elements...)`: stores the anchor and elements, `selected = −1`; `replace` swaps elements keeping the anchor.
2. `extract` (per frame): renders the menu as a tooltip via `setTooltipForNextFrame` with each element's `getHoveredText(mouseX − x, mouseY − y − i*10, selected == i)` — hover text is AQUA when hovered/selected, GRAY otherwise; anchored at `(x−12, y+12)` (`UI-06` offset vs the click rects).
3. `mouseClick`: entry i is clickable in `(x, y+i*10, 100×8)` (±1 px tolerance); on hit: `onClick` (default `activate`), `selected = i`, consume; any other click closes the menu (also consumes).
4. `keyPressed`: up/down arrows cycle `selected` (wrapping); ESC (`GLFW_KEY_ESCAPE` = 256) closes; otherwise the selected element receives the key (enter/enter-pad activates by default).
5. `charTyped`: routed to the selected element (used by `TextFieldElement`).
6. Elements: `ButtonElement` (text supplier + action runnable); `TextFieldElement` (an internal `EditBox` with focus kept; renders `tooltip.toms_trading_network.textfield` with the label and the typed value, inserting a `_` at the cursor position while activated — key/namespace missing, `UI-03`).

### `RecipeBookButton` (`SH/screen/RecipeBookButton.java`)

1. A 20×18 vanilla `ImageButton` using `RecipeBookComponent.RECIPE_BUTTON_SPRITES`; placed by `CraftingTerminalScreen` (area `03-terminal`); its press handler toggles the recipe book open/closed via the screen. (Documented here as a widget; the screen-side wiring is `03-terminal`.)

## Deviations in the new mod

None (original spec only).
