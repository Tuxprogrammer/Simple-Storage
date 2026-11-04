# Item filter GUIs (filtered menu/screen base, item + tag filter GUIs, slots)

| # | Field | Contents |
|---|---|---|
| 1 | **Identity** | `AbstractFilteredMenu` (SH `menu/AbstractFilteredMenu.java`), `AbstractFilteredScreen` (SH `screen/AbstractFilteredScreen.java`), `ItemFilterMenu` (SH `menu/ItemFilterMenu.java`), `ItemFilterScreen` (SH `screen/ItemFilterScreen.java`, background = vanilla `textures/gui/container/dispenser.png`), `TagItemFilterMenu` (SH `menu/TagItemFilterMenu.java`), `TagItemFilterScreen` (SH `screen/TagItemFilterScreen.java`, texture `toms_storage:textures/gui/tag_filter.png`), `FilterSlot`/`ItemFilterSlot`/`PhantomSlot` (SH `menu/slot/`). Menu types: `Content.itemFilterMenu` (title from the item: `item.toms_storage.item_filter` = "Item Filter"), `Content.tagItemFilterMenu` (`item.toms_storage.tag_item_filter` = "Tag Item Filter"). Sources: branch `26.2`, `NeoForge/src/platform-shared/java/com/tom/storagemod/`; `menu/AbstractFilteredMenu.java:NN` (AM), `menu/ItemFilterMenu.java:NN` (IM), `menu/TagItemFilterMenu.java:NN` (TM), `menu/slot/{Phantom,Filter,ItemFilter}Slot.java:NN` (PS, FS, IS), `screen/AbstractFilteredScreen.java:NN` (AS), `screen/ItemFilterScreen.java:NN` (IC), `screen/TagItemFilterScreen.java:NN` (TC). (AM:21, IM:17, TM:23, AS:8, IC:16, TC:31) |
| 2 | **Inputs** | Filter items in hand (any item for phantom/filter slots; `IItemFilter` items for filter slots), the 9-slot filter container (item filter), the tag list (tag filter), allow/deny + match-NBT toggles, tag add/remove buttons. |
| 3 | **Numbers** | Item filter: 3×3 phantom grid at (62 + j×18, 17 + i×18) (IM:46-50); player slots y=84/142 (IM:52-60); buttons at (leftPos−18, topPos+5) allow-list (`icons/deny` off / `icons/allow` on) and (leftPos−18, topPos+23) match-components (`icons/match_tag_off`/`icons/match_tag_on`) (IC:30-33, 36-39). Tag filter: phantom slot at (8,15) (TM:39); two tag lists 61×58 px, 14 px rows (TC:140), at (leftPos+28, topPos+15) (item tags) and (leftPos+109, topPos+15) (filter tags) (TC:54, 56); add button (`icons/add`) at (leftPos+90, topPos+14) (TC:59), remove (`icons/deny`) at (leftPos+90, topPos+32) (TC:69); allow toggle at (leftPos−18, topPos+5) (TC:48). |
| 4 | **Behavior** | See Behavior detail. |
| 5 | **Determinism** | Item-tag list order = the stack's tag iteration order (deterministic per stack). No randomness. |
| 6 | **Data formats** | Item filter data slots (client menu): matchNBT (set-only, int 0/1), allowList (set-only) (IM:26-27); with a real filter: matchNBT/allowList read from the filter (IM:36-37). Menu button id: `(target << 1) | value` — target 0 = matchNBT, 1 = allowList (IM:65-71; client IC:44). Tag filter data slot: allowList (int 0/1) (TM:51). Tag filter server→client: `l` = list of tag location strings (sent once per open via `broadcastChanges` `synced` flag) (TM:73-83); client re-sends on every add/remove (TC:79-85). Tag filter client→server: `l` = list of tag strings (TC:80-83; read TM:123-125). `setPhantom` (SH, from `AbstractFilteredMenu`): tag `{id = slot index, item = ItemStack.CODEC}` (AM:73-77) → server sets a count-1 copy into the phantom/filter slot (AM:83-100). |
| 7 | **Output** | Filter container contents (item filter), tag list + allow/deny (tag filter), allow/deny + matchNBT (item filter); nested filter GUIs opened by `IItemFilter.openGui` (02-storage `components.md`). |
| 8 | **Relationships** | `SimpleItemFilter`/`TagFilter` (03-terminal `filters.md`), `IItemFilter.openGui` (02-storage `components.md`), `PhantomSlot`/`FilterSlot`/`ItemFilterSlot` (this sheet), `AbstractTagFilter`/`AbstractSimpleItemFilter` (01-core `platform-layer.md`), `NetworkHandler` (01-core `network.md`), `ListWidget` (06-client-ui), `ToggleButton`/`IconButton` (06-client-ui). |
| 9 | **NeoForge dependencies** | `DataSlots` (container data slots — no 1.7.10 counterpart), `ItemStack.CODEC` (1.7.10: `writeToNBT`), `TagKey`/`Identifier.CODEC` (1.7.10: `IItemTags` names as strings), `EditBox`-free here (lists instead), `ProblemReporter.DISCARDING` (malformed tags dropped). |
| 10 | **Bugs** | `TERM-11` (item-filter shift-click only fills the first empty phantom slot whose item differs — a 9-slot grid with a partial duplicate layout can leave a matching item unplaced while a different empty slot exists; inferred edge, no repro). |

## Behavior detail

### AbstractFilteredMenu (server unless noted)

1. `quickMoveStack`: default EMPTY (subclasses override) (AM:30-32).
2. `clicked` override (AM:35-65):
   - `PhantomSlot`: carried stack (or EMPTY) is set at count 1 — clicking an empty hand clears the phantom (AM:37-42).
   - `FilterSlot`: carried and existing are each checked for `IItemFilter`. Both filters, or existing filter with empty hand → vanilla behavior (swap/pick up) (AM:46-51). Existing filter + carried non-filter → nothing (AM:52). No existing filter + carried filter → clear the slot, vanilla behavior (place the filter) (AM:53-56). No existing filter + carried non-filter → set carried at count 1 (any item can sit in a filter slot as a plain filter entry) (AM:57-61).
   - else vanilla (AM:64).
3. `canTakeItemForPickAll`: phantom and filter slots never participate in the pick-all (middle-click-collect) sweep (AM:67-70).
4. `setPhantom(slot, ingredient)` (client helper): sends `{setPhantom: {id, item}}` (AM:72-77).
5. `receive` (server) (AM:81): spectator guard (AM:82); `setPhantom` child → phantom slot gets a count-1 copy (never EMPTY) (AM:88-92); `FilterSlot` gets a count-1 copy only when it currently does not hold an `IItemFilter` and the incoming item is not one either (AM:93-99).

### AbstractFilteredScreen (client)

6. Trivial base for filter screens: extends `TSContainerScreen`; provides nothing beyond the class hierarchy (observed: no overrides) (AS:8-14).

### ItemFilterMenu (server)

7. 9 `PhantomSlot`s over the filter's 9-slot container (positions in Numbers); player slots standard (y=84/142); `inv.startOpen(player)` (IM:44) / `stopOpen` on close (IM:115); with a real `SimpleItemFilter`: data slots read `isMatchNBT()`/`isAllowList()`; the plain (client) constructor uses set-only data slots and a `SimpleContainer(9)` (IM:24-29).
8. `clickMenuButton(btn)`: `v = btn & 1`, `btn >>= 1`; 0 → `filter.setMatchNBT(v)`, 1 → `filter.setAllowList(v)`. Returns false (IM:64-72).
9. `stillValid`: `isValid.getAsBoolean() && container.stillValid(player)` (IM:79-81) (the `isValid` supplier is `() -> true` for the standalone menu; callers pass e.g. `() -> stack.getItem() == filterItem`).
10. `quickMoveStack`: shift-click from player → count-1 copy into the first phantom slot that is empty or holds a *different* item (scan stops at the first matching item — **`TERM-11`**) (IM:88-107; UNVERIFIED: the scan only ever writes into an EMPTY slot, a slot holding a different item is skipped, never replaced, IM:95-101).
11. `removed`: `stopOpen`, `filter.flush()` (persist the filter into the item), `refresh.run()` (callers pass e.g. `markFilterDirty`) (IM:113-118).

### ItemFilterScreen (client)

12. `init` (IC:26): allow-list toggle (top) → `click(1, state)` (IC:33); match-NBT toggle (below) → `click(0, state)` (IC:39); `click(id, val)` = `handleInventoryButtonClick(containerId, (id << 1) | (val ? 1 : 0))` (IC:43-45).
13. `extractRenderState`: both toggle states re-read from the menu (data slots) every frame (IC:48-51).
14. Background: vanilla dispenser GUI (IC:17, 60). `getExclusionAreas`: Box(leftPos−20, topPos, 25, 40) (IC:65).

### TagItemFilterMenu (server)

15. Slot 0: `PhantomSlot(SimpleContainer(1), 0, 8, 15)` — the item being tagged (TM:39). Player slots standard (TM:42-50). Data slot allowList (from `filter.isAllowList()`) (TM:51).
16. `clickMenuButton`: same encoding, target 0 → `filter.setAllowList(v)` (TM:55-62).
17. `broadcastChanges`: after super, once per open (`synced` flag) sends `l` = all tag locations of the filter as strings (TM:73-83).
18. `quickMoveStack`: shift-click → count-1 copy into slot 0 only if it is empty or holds a different item (TM:91-108; UNVERIFIED: code writes only when slot 0 is empty, a different item there is left untouched, TM:99-103).
19. `removed`: `filter.flush()`, `refresh.run()` (TM:114-118).
20. `receive` (server): `l` list of `Identifier` (tag locations) → `filter.setTags(...)` (TM:121-125).

### TagItemFilterScreen (client)

21. `init` (TC:44): allow toggle (same icons as the item filter) → `click(0, state)` (TC:51); item-tag list (left, shows the hovered item's tags), filter-tag list (right, the current filter tags); add button: selected item tag → append to filter tags if absent, clear selection, `sync()` (TC:60-66); remove button: selected filter tag → remove, clear selection, `sync()` (TC:70-75).
22. `containerTick` (TC:88; every client tick): read the item in slot 0, build its tag-location string list; if it changed, rebuild the item-tag list, clear selection, reset scroll to 0 (TC:89-96).
23. `extractRenderState`: allow toggle state from menu; `preRender` both lists (hover/scroll); add/remove buttons active only with a selection (TC:104-110).
24. `receive` (client): `l` (strings, codec length 0..256) → replace filter tags, clear selection, reset scroll (TC:130-134).
25. List rows: literal string components; hover tooltip = the literal tag name (TC:154-160). `getExclusionAreas`: Box(leftPos−20, topPos, 25, 25) (TC:166).

### Slots

26. `PhantomSlot` (PS:8-28): `mayPickup` false, max stack 1, `mayPlace` true — display-only slot that accepts placed items.
27. `FilterSlot` (FS:16-29): `mayPickup` only when the held item is an `IItemFilter`; max stack 1; `mayPlace` true.
28. `ItemFilterSlot` (IS:15-23): `mayPickup` only for `IItemFilter`; max stack 1 (no `mayPlace` override); used by the inventory configurator (03-terminal `inventory-configurator.md`).

## Lang keys used by this feature

- `tooltip.toms_storage.allowList` = "Allow matching"; `denyList` = "Reject matching"
- `tooltip.toms_storage.matchNBT_on` = "Match Components"; `matchNBT_off` = "Don't match Components"
- `tooltip.toms_storage.item_filter` = "Allow filtering multiple items with components"; `item_filter.contents` = "Contents:"; `item_filter.no_items` = "No items"; `item_filter.prefix` = "- %s"
- `tooltip.toms_storage.tag_item_filter` = "Filter using tags"
- `item.toms_storage.item_filter` = "Item Filter"; `item.toms_storage.tag_item_filter` = "Tag Item Filter"
- `item.toms_storage.polymorphic_item_filter` = "Polymorphic Item Filter" (used by 02-storage `components.md`)

## Deviations in the new mod

See the [03-terminal deviation table](../../new-spec/03-terminal/deviations.md#deviation-table) (row `item-filter-gui`).
