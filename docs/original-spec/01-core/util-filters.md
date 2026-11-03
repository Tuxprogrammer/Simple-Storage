# Filter bases

| # | Field | Contents |
|---|---|---|
| 1 | **Identity** | `AbstractSimpleItemFilter` (SH), `AbstractTagFilter` (SH). Base classes for the filter items (02-storage: `FilterItem`, `TagFilterItem`) and their screens (06-client-ui: `ItemFilterScreen`, `TagItemFilterScreen`). |
| 2 | **Inputs** | The filter `ItemStack` itself (state is stored in data components, not NBT). |
| 3 | **Numbers** | `AbstractSimpleItemFilter` working container: 9 slots (`SimpleContainer(9)`). |
| 4 | **Behavior** | Ordered steps in [Behavior detail](#behavior-detail). |
| 5 | **Determinism** | None. |
| 6 | **Data formats** | Data component `toms_storage:simple_item_filter` (`SimpleItemFilterComponent`, codec-defined, 02-storage territory): list of up to 9 item stacks, `matchComp` (match NBT/components) boolean, `allowList` (allow vs deny) boolean. Data component `toms_storage:tag_filter` (`TagFilterComponent`): list of item tag identifiers + `allowList` boolean. Written via `DataComponentPatch`. |
| 7 | **Output** | Mutated filter items (after `flush()`); the working container/tag list used by the filter screens. |
| 8 | **Relationships** | Subclasses: `FilterItem` (simple filter, 02-storage), `TagFilterItem` (tag filter, 02-storage); the NF `PolyFilter` (inventory-access.md) is the platform implementation of the SH `ItemFilter` interface — the interface itself (`IFilter`/`ItemFilter`/`ItemPredicate`) is 03-terminal territory; the screens are 06-client-ui. |
| 9 | **NeoForge dependencies** | Data components (`DataComponentPatch`, `ItemStack.get/applyComponents`) — no NBT involved; the 1.7.10 counterpart is item NBT. |
| 10 | **Bugs** | None. |

## Behavior detail

### AbstractSimpleItemFilter

1. (both) Constructor: reads `Content.simpleItemFilterComponent` from the item stack. If present, the working `SimpleContainer` is seeded with the stored stacks (up to 9), and `matchNBT`/`allowList` are restored; otherwise an empty 9-slot container is created (a fresh filter item).
2. (both) Getters/setters: `getContainer()` (the working container the screen edits), `isMatchNBT()`/`setMatchNBT(boolean)`, `isAllowList()`/`setAllowList(boolean)`.
3. (both) `flush()`: serializes the current container contents (in slot order, as a list) plus `matchNBT` and `allowList` into a `SimpleItemFilterComponent` and applies it to the stack via a component patch. The screen calls `flush()` on close/apply.

### AbstractTagFilter

4. (both) Constructor: reads `Content.tagFilterComponent`; if present, restores the tag list and `allowList`; otherwise an empty tag list.
5. (both) `isAllowList()`/`setAllowList(boolean)`, `getTags()` (live list), `setTags(List<Identifier>)` — replaces the list, mapping each identifier to a `TagKey` in the ITEM registry (note: the identifiers are parsed, not validated against existing tags — an unknown tag simply matches nothing at test time, *inferred*).
6. (both) `flush()`: writes a `TagFilterComponent` (tag list copy + allowList) onto the stack.

## Deviations in the new mod

None yet.
