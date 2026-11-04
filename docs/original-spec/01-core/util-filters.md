# Filter bases

| # | Field | Contents |
|---|---|---|
| 1 | **Identity** | `AbstractSimpleItemFilter` (SH), `AbstractTagFilter` (SH). Base classes for the filter items (Sources: `NeoForge/src/platform-shared/java/com/tom/storagemod/util/AbstractSimpleItemFilter.java`, `util/AbstractTagFilter.java`; short citations below are relative to these) (02-storage: `FilterItem`, `TagFilterItem`) and their screens (06-client-ui: `ItemFilterScreen`, `TagItemFilterScreen`). |
| 2 | **Inputs** | The filter `ItemStack` itself (state is stored in data components, not NBT). |
| 3 | **Numbers** | `AbstractSimpleItemFilter` working container: 9 slots (`SimpleContainer(9)`). (AbstractSimpleItemFilter.java:25) |
| 4 | **Behavior** | Ordered steps in [Behavior detail](#behavior-detail). |
| 5 | **Determinism** | None. |
| 6 | **Data formats** | Data component `toms_storage:simple_item_filter` (`SimpleItemFilterComponent`, codec-defined, 02-storage territory) (Content.java:127; components/SimpleItemFilterComponent.java:10, fields stacks, matchComp, allowList): list of up to 9 item stacks, `matchComp` (match NBT/components) boolean, `allowList` (allow vs deny) boolean. Data component `toms_storage:tag_filter` (`TagFilterComponent`) (Content.java:128; components/TagFilterComponent.java:12, fields tags, allowList): list of item tag identifiers + `allowList` boolean. Written via `DataComponentPatch`. (AbstractSimpleItemFilter.java:29-30; AbstractTagFilter.java:40-41) |
| 7 | **Output** | Mutated filter items (after `flush()`); the working container/tag list used by the filter screens. |
| 8 | **Relationships** | Subclasses: `FilterItem` (simple filter, 02-storage), `TagFilterItem` (tag filter, 02-storage); the NF `PolyFilter` (inventory-access.md) is the platform implementation of the SH `ItemFilter` interface — the interface itself (`IFilter`/`ItemFilter`/`ItemPredicate`) is 03-terminal territory; the screens are 06-client-ui. |
| 9 | **NeoForge dependencies** | Data components (`DataComponentPatch`, `ItemStack.get/applyComponents`) — no NBT involved; the 1.7.10 counterpart is item NBT. |
| 10 | **Bugs** | None. |

## Behavior detail

### AbstractSimpleItemFilter

1. (both) Constructor: reads `Content.simpleItemFilterComponent` from the item stack. (AbstractSimpleItemFilter.java:17-19) If present, the working `SimpleContainer` is seeded with the stored stacks (up to 9), and `matchNBT`/`allowList` are restored; (AbstractSimpleItemFilter.java:20-23) otherwise an empty 9-slot container is created (AbstractSimpleItemFilter.java:24-25) (a fresh filter item).
2. (both) Getters/setters: `getContainer()` (the working container the screen edits), (AbstractSimpleItemFilter.java:33-51) `isMatchNBT()`/`setMatchNBT(boolean)`, `isAllowList()`/`setAllowList(boolean)`.
3. (both) `flush()`: serializes the current container contents (in slot order, as a list) plus `matchNBT` and `allowList` into a `SimpleItemFilterComponent` and applies it to the stack via a component patch. The screen calls `flush()` on close/apply. (AbstractSimpleItemFilter.java:28-31)

### AbstractTagFilter

4. (both) Constructor: reads `Content.tagFilterComponent`; (AbstractTagFilter.java:21-23) if present, restores the tag list and `allowList`; otherwise an empty tag list. (AbstractTagFilter.java:24-28)
5. (both) `isAllowList()`/`setAllowList(boolean)`, `getTags()` (live list) (AbstractTagFilter.java:44-46), `setTags(List<Identifier>)` — replaces the list, mapping each identifier to a `TagKey` in the ITEM registry (AbstractTagFilter.java:48-50) (note: the identifiers are parsed, not validated against existing tags — an unknown tag simply matches nothing at test time, *inferred*).
6. (both) `flush()`: writes a `TagFilterComponent` (tag list copy + allowList) onto the stack. (AbstractTagFilter.java:39-42)

## Deviations in the new mod

See the [`util-filters` row of the new-spec 01-core deviation table](../../new-spec/01-core/deviations.md#deviation-table).
