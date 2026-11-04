# Item filter GUIs (filtered menu/screen base, item + tag filter GUIs, slots)

| # | Field | Contents |
|---|---|---|
| 1 | **Identity** | `AbstractFilteredMenu` (SH `menu/AbstractFilteredMenu.java`), `AbstractFilteredScreen` (SH `screen/AbstractFilteredScreen.java`), `ItemFilterMenu` (SH `menu/ItemFilterMenu.java`), `ItemFilterScreen` (SH `screen/ItemFilterScreen.java`, background = vanilla `textures/gui/container/dispenser.png`), `TagItemFilterMenu` (SH `menu/TagItemFilterMenu.java`), `TagItemFilterScreen` (SH `screen/TagItemFilterScreen.java`, texture `toms_storage:textures/gui/tag_filter.png`), `FilterSlot`/`ItemFilterSlot`/`PhantomSlot` (SH `menu/slot/`). Menu types: `Content.itemFilterMenu` (title from the item: `item.toms_storage.item_filter` = "Item Filter"), `Content.tagItemFilterMenu` (`item.toms_storage.tag_item_filter` = "Tag Item Filter"). |
| 2 | **Inputs** | Filter items in hand (any item for phantom/filter slots; `IItemFilter` items for filter slots), the 9-slot filter container (item filter), the tag list (tag filter), allow/deny + match-NBT toggles, tag add/remove buttons. |
| 3 | **Numbers** | Item filter: 3×3 phantom grid at (62 + j×18, 17 + i×18); player slots y=84/142; buttons at (leftPos−18, topPos+5) allow-list (`icons/deny` off / `icons/allow` on) and (leftPos−18, topPos+23) match-components (`icons/match_tag_off`/`icons/match_tag_on`). Tag filter: phantom slot at (8,15); two tag lists 61×58 px, 14 px rows, at (leftPos+28, topPos+15) (item tags) and (leftPos+109, topPos+15) (filter tags); add button (`icons/add`) at (leftPos+90, topPos+14), remove (`icons/deny`) at (leftPos+90, topPos+32); allow toggle at (leftPos−18, topPos+5). |
| 4 | **Behavior** | See Behavior detail. |
| 5 | **Determinism** | Item-tag list order = the stack's tag iteration order (deterministic per stack). No randomness. |
| 6 | **Data formats** | Item filter data slots (client menu): matchNBT (set-only, int 0/1), allowList (set-only); with a real filter: matchNBT/allowList read from the filter. Menu button id: `(target << 1) | value` — target 0 = matchNBT, 1 = allowList. Tag filter data slot: allowList (int 0/1). Tag filter server→client: `l` = list of tag location strings (sent once per open via `broadcastChanges` `synced` flag; client re-sends on every add/remove). Tag filter client→server: `l` = list of tag strings. `setPhantom` (SH, from `AbstractFilteredMenu`): tag `{id = slot index, item = ItemStack.CODEC}` → server sets a count-1 copy into the phantom/filter slot. |
| 7 | **Output** | Filter container contents (item filter), tag list + allow/deny (tag filter), allow/deny + matchNBT (item filter); nested filter GUIs opened by `IItemFilter.openGui` (02-storage `components.md`). |
| 8 | **Relationships** | `SimpleItemFilter`/`TagFilter` (03-terminal `filters.md`), `IItemFilter.openGui` (02-storage `components.md`), `PhantomSlot`/`FilterSlot`/`ItemFilterSlot` (this sheet), `AbstractTagFilter`/`AbstractSimpleItemFilter` (01-core `platform-layer.md`), `NetworkHandler` (01-core `network.md`), `ListWidget` (06-client-ui), `ToggleButton`/`IconButton` (06-client-ui). |
| 9 | **NeoForge dependencies** | `DataSlots` (container data slots — no 1.7.10 counterpart), `ItemStack.CODEC` (1.7.10: `writeToNBT`), `TagKey`/`Identifier.CODEC` (1.7.10: `IItemTags` names as strings), `EditBox`-free here (lists instead), `ProblemReporter.DISCARDING` (malformed tags dropped). |
| 10 | **Bugs** | `TERM-11` (item-filter shift-click only fills the first empty phantom slot whose item differs — a 9-slot grid with a partial duplicate layout can leave a matching item unplaced while a different empty slot exists; inferred edge, no repro). |

## Behavior detail

### AbstractFilteredMenu (server unless noted)

1. `quickMoveStack`: default EMPTY (subclasses override).
2. `clicked` override:
   - `PhantomSlot`: carried stack (or EMPTY) is set at count 1 — clicking an empty hand clears the phantom.
   - `FilterSlot`: carried and existing are each checked for `IItemFilter`. Both filters, or existing filter with empty hand → vanilla behavior (swap/pick up). Existing filter + carried non-filter → nothing. No existing filter + carried filter → clear the slot, vanilla behavior (place the filter). No existing filter + carried non-filter → set carried at count 1 (any item can sit in a filter slot as a plain filter entry).
   - else vanilla.
3. `canTakeItemForPickAll`: phantom and filter slots never participate in the pick-all (middle-click-collect) sweep.
4. `setPhantom(slot, ingredient)` (client helper): sends `{setPhantom: {id, item}}`.
5. `receive` (server): spectator guard; `setPhantom` child → phantom slot gets a count-1 copy (never EMPTY); `FilterSlot` gets a count-1 copy only when it currently does not hold an `IItemFilter` and the incoming item is not one either.

### AbstractFilteredScreen (client)

6. Trivial base for filter screens: extends `TSContainerScreen`; provides nothing beyond the class hierarchy (observed: no overrides).

### ItemFilterMenu (server)

7. 9 `PhantomSlot`s over the filter's 9-slot container (positions in Numbers); player slots standard (y=84/142); `inv.startOpen(player)` / `stopOpen` on close; with a real `SimpleItemFilter`: data slots read `isMatchNBT()`/`isAllowList()`; the plain (client) constructor uses set-only data slots and a `SimpleContainer(9)`.
8. `clickMenuButton(btn)`: `v = btn & 1`, `btn >>= 1`; 0 → `filter.setMatchNBT(v)`, 1 → `filter.setAllowList(v)`. Returns false.
9. `stillValid`: `isValid.getAsBoolean() && container.stillValid(player)` (the `isValid` supplier is `() -> true` for the standalone menu; callers pass e.g. `() -> stack.getItem() == filterItem`).
10. `quickMoveStack`: shift-click from player → count-1 copy into the first phantom slot that is empty or holds a *different* item (scan stops at the first matching item — **`TERM-11`**).
11. `removed`: `stopOpen`, `filter.flush()` (persist the filter into the item), `refresh.run()` (callers pass e.g. `markFilterDirty`).

### ItemFilterScreen (client)

12. `init`: allow-list toggle (top) → `click(1, state)`; match-NBT toggle (below) → `click(0, state)`; `click(id, val)` = `handleInventoryButtonClick(containerId, (id << 1) | (val ? 1 : 0))`.
13. `extractRenderState`: both toggle states re-read from the menu (data slots) every frame.
14. Background: vanilla dispenser GUI. `getExclusionAreas`: Box(leftPos−20, topPos, 25, 40).

### TagItemFilterMenu (server)

15. Slot 0: `PhantomSlot(SimpleContainer(1), 0, 8, 15)` — the item being tagged. Player slots standard. Data slot allowList (from `filter.isAllowList()`).
16. `clickMenuButton`: same encoding, target 0 → `filter.setAllowList(v)`.
17. `broadcastChanges`: after super, once per open (`synced` flag) sends `l` = all tag locations of the filter as strings.
18. `quickMoveStack`: shift-click → count-1 copy into slot 0 only if it is empty or holds a different item.
19. `removed`: `filter.flush()`, `refresh.run()`.
20. `receive` (server): `l` list of `Identifier` (tag locations) → `filter.setTags(...)`.

### TagItemFilterScreen (client)

21. `init`: allow toggle (same icons as the item filter) → `click(0, state)`; item-tag list (left, shows the hovered item's tags), filter-tag list (right, the current filter tags); add button: selected item tag → append to filter tags if absent, clear selection, `sync()`; remove button: selected filter tag → remove, clear selection, `sync()`.
22. `containerTick` (every client tick): read the item in slot 0, build its tag-location string list; if it changed, rebuild the item-tag list, clear selection, reset scroll to 0.
23. `extractRenderState`: allow toggle state from menu; `preRender` both lists (hover/scroll); add/remove buttons active only with a selection.
24. `receive` (client): `l` (strings, codec length 0..256) → replace filter tags, clear selection, reset scroll.
25. List rows: literal string components; hover tooltip = the literal tag name. `getExclusionAreas`: Box(leftPos−20, topPos, 25, 25).

### Slots

26. `PhantomSlot`: `mayPickup` false, max stack 1, `mayPlace` true — display-only slot that accepts placed items.
27. `FilterSlot`: `mayPickup` only when the held item is an `IItemFilter`; max stack 1; `mayPlace` true.
28. `ItemFilterSlot`: `mayPickup` only for `IItemFilter`; max stack 1; used by the inventory configurator (03-terminal `inventory-configurator.md`).

## Lang keys used by this feature

- `tooltip.toms_storage.allowList` = "Allow matching"; `denyList` = "Reject matching"
- `tooltip.toms_storage.matchNBT_on` = "Match Components"; `matchNBT_off` = "Don't match Components"
- `tooltip.toms_storage.item_filter` = "Allow filtering multiple items with components"; `item_filter.contents` = "Contents:"; `item_filter.no_items` = "No items"; `item_filter.prefix` = "- %s"
- `tooltip.toms_storage.tag_item_filter` = "Filter using tags"
- `item.toms_storage.item_filter` = "Item Filter"; `item.toms_storage.tag_item_filter` = "Tag Item Filter"
- `item.toms_storage.polymorphic_item_filter` = "Polymorphic Item Filter" (used by 02-storage `components.md`)

## Deviations in the new mod

See the [03-terminal deviation table](../../new-spec/03-terminal/index.md#deviation-table) (row `item-filter-gui`).
