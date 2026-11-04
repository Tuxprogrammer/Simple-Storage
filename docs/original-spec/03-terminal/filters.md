# Filter system (item predicates)

| # | Field | Contents |
|---|---|---|
| 1 | **Identity** | `IFilter` (SH `inventory/filter/IFilter.java`, incl. `IFilter.MultiFilter`), `ItemFilter` (SH), `ItemPredicate` (SH), `SimpleItemFilter` (SH), `TagFilter` (SH). Base classes `AbstractSimpleItemFilter` / `AbstractTagFilter` live in SH `util` (01-core `platform-layer.md`). Sources: `NeoForge/src/platform-shared/java/com/tom/storagemod/inventory/filter/` (`IFilter.java:NN`, `ItemPredicate.java:NN`, `ItemFilter.java:NN`, `SimpleItemFilter.java:NN`, `TagFilter.java:NN`) and `NeoForge/src/platform-shared/java/com/tom/storagemod/util/` (`AbstractSimpleItemFilter.java:NN`, `AbstractTagFilter.java:NN`). |
| 2 | **Inputs** | Filter definition: `SimpleItemFilter` — a 9-slot `SimpleItemFilter` container (item definitions) (AbstractSimpleItemFilter.java:25), `matchNBT` flag, `allowList` flag (AbstractSimpleItemFilter.java:15); `TagFilter` — a list of tag locations, `allowList` flag (AbstractTagFilter.java:18-19). The object to test: a `StoredItemStack`. |
| 3 | **Numbers** | `SimpleItemFilter` container: 9 slots (AbstractSimpleItemFilter.java:25). `ItemFilter.TRUE` — constant predicate always true (ItemFilter.java:11). `MultiFilter` priority: sum of member `Priority.getSorting()` values mapped back through `Priority.fromSorting` (01-core `platform-layer.md`) (IFilter.java:48-55). |
| 4 | **Behavior** | See Behavior detail. |
| 5 | **Determinism** | Pure functions of (filter state, stack). No randomness, no ordering dependence (all loops short-circuit on first match). |
| 6 | **Data formats** | Filter state is stored by the owner (item components — 02-storage `components.md`; or `BlockFilter` — 02-storage `inventory-connector.md`). `SimpleItemFilter`: container contents + `matchNBT` + `allowList` (persistence handled by `AbstractSimpleItemFilter`, 01-core `platform-layer.md`). `TagFilter`: tag list + `allowList`. |
| 7 | **Output** | Boolean test results; `configMatch(ItemStack)` — identity check against the defining item stack (used by the configurator to recognize its own item). |
| 8 | **Relationships** | Used by: `BlockFilter`/`ItemFilterSlot` consumers (02-storage `inventory-connector.md`), `ItemFilterMenu`/`TagItemFilterMenu` (03-terminal `item-filter-gui.md`), `StoredItemStack` (03-terminal `terminal-stacks.md`), `Priority` (01-core). `MultiFilter` composes `IFilter`s with AND semantics. |
| 9 | **NeoForge dependencies** | `ItemStack.isSameItem` / `isSameItemSameComponents` (1.7.10: item + NBT equality), `ItemStack.is(TagKey)` (1.7.10: `IItemHandler`/`IItemTags` contains). No direct 1.7.10 counterpart: data components (1.7.10: NBT on the stack). |
| 10 | **Bugs** | None suspected. |

## Behavior detail

1. `ItemPredicate.test(StoredItemStack)` (ItemPredicate.java:6) — the core test; `updateState()` hook (default no-op, ItemPredicate.java:8) for filters that lazily rebuild internal state.
2. `ItemFilter extends ItemPredicate` adds `configMatch(ItemStack)` (default true) and the constant `TRUE` (ItemFilter.java:5-11).
3. `SimpleItemFilter.test(stack)`: if `matchNBT` — true iff the stack matches (item **and** components) any of the 9 filter entries, XORed with `allowList` (`testNbt(stack) == allowList`); otherwise match on item identity only (`test0`) (SimpleItemFilter.java:14-35). Empty filter slots are skipped (SimpleItemFilter.java:22,31). An empty filter therefore matches nothing (in allow mode) / everything (in deny mode).
4. `TagFilter.test(stack)`: true iff the stack has any of the configured tags, XORed with `allowList` (`test0(stack) == allowList`) (TagFilter.java:19-30).
5. Both implement `configMatch` as reference identity (`stack == this.stack`) — the filter item instance itself (SimpleItemFilter.java:37-40, TagFilter.java:14-17).
6. `IFilter`: bundle of (item predicate, keep-last flag, priority). `MultiFilter`: AND-combines member filters (flattening nested `MultiFilter`s, deduplicating by identity, IFilter.java:17-33; the check is `List.contains`, i.e. `equals`, at :30); `isKeepLast` = any member keeps last (IFilter.java:40-46); `getPriority` = `Priority.fromSorting(sum of member sorting weights)`; `test` = all members' predicates pass (IFilter.java:57-64); `updateState` propagates (IFilter.java:66-71).

## Deviations in the new mod

See the [03-terminal deviation table](../../new-spec/03-terminal/deviations.md#deviation-table) (row `filters`).
