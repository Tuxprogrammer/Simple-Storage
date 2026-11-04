# Containers and crafting matrix

| # | Field | Contents |
|---|---|---|
| 1 | **Identity** | `BasicContainer` (SH), `LimitedContainer` (SH), `FilingCabinetContainer` (SH), `CraftingMatrix` (SH). |
| 2 | **Inputs** | `BasicContainer`: constructor size or item varargs, `ValueInput.TypedInputList<ItemStackWithSlot>` (NBT load). `LimitedContainer`: a delegate `Container`, a size limit, an optional start offset. `FilingCabinetContainer`: size, `onChange` callback, `isValidFor` player predicate, NBT list id. `CraftingMatrix`: width, height, `onChanged` callback, optional item list. |
| 3 | **Numbers** | `FilingCabinetContainer.getMaxStackSize()` = 1 (container-level). `LimitedContainer.getContainerSize()` = min(sizeLimit, delegateSize - startOffset). No other constants. |
| 4 | **Behavior** | Ordered steps in [Behavior detail](#behavior-detail). |
| 5 | **Determinism** | None. |
| 6 | **Data formats** | `BasicContainer.loadItems/storeItems` and `Util.loadItems/storeItems` (util-misc.md): an NBT list under a caller-chosen key of `ItemStackWithSlot` entries (slot + stack codec); empty slots are skipped on save, all slots are cleared before load. `FilingCabinetContainer.fromTag/storeTag`: an NBT list under the caller-chosen key of plain `ItemStack` entries (no slot numbers — slots are positional). |
| 7 | **Output** | Container objects used by menus (03-terminal/06-client-ui): the filing cabinet BE's inventory (02-storage), limited views of larger containers, and the crafting terminal's matrix (03-terminal). |
| 8 | **Relationships** | `FilingCabinetContainer` is the BE inventory behind `filing_cabinet` (02-storage) and is exposed to hoppers via `VanillaContainerWrapper.of(be.getInv())` (mod-lifecycle.md). `CraftingMatrix` implements `CraftingContainer` for the crafting terminal menu (03-terminal). `Util` (util-misc.md) holds the shared load/store helpers that mirror `BasicContainer`'s methods. |
| 9 | **NeoForge dependencies** | None directly (vanilla `Container`/`SimpleContainer`/`CraftingContainer` interfaces; `ItemStackWithSlot` + `ValueInput`/`ValueOutput` codecs are vanilla data layers). |
| 10 | **Bugs** | `CORE-05` (first item of a filing cabinet must be single-stack), `CORE-06` (filing cabinet max stack size 1 for automation). |

## Behavior detail

### BasicContainer

1. (both) Extends `SimpleContainer`; adds an optional listener list. `addListener`/`removeListener` manage it lazily. `setChanged()` (called by `SimpleContainer` on any mutation) fires `containerChanged(this)` on every listener (used by menus to mark the BE dirty).
2. (both) `loadItems(list)`: clears all slots, then for each entry with `isValidInContainer(size)` (slot in range), places the stack at that slot. `storeItems(output)`: writes every non-empty slot as an `ItemStackWithSlot`.

### LimitedContainer

3. (both) A window over a delegate `Container`: `getContainerSize()` = `min(sizeLimit, delegate.getContainerSize() - startOffset)`; all slot operations (`getItem`, `removeItem`, `removeItemNoUpdate`, `setItem`, `canPlaceItem`) are offset by `startOffset` into the delegate. `isEmpty()`, `countItem`, `hasAnyOf`, `hasAnyMatching`, `startOpen`, `stopOpen`, `maxStackSize` delegate without offset (i.e. they reflect the whole delegate, not just the window — *inferred* acceptable since they are aggregate queries). `setSizeLimit`/`setStartOffset` allow resizing the window at runtime (menus use this to show a subset of a large inventory).

### FilingCabinetContainer

4. (both) A single-item-type cabinet: an internal `SimpleContainer` of the given size plus a remembered `item`. `setItem(slot, stack)`: stores the stack; if non-empty, `item = stack.getItem()`; if the container becomes empty, `item = null`. `removeItem`/`removeItemNoUpdate`: remove and clear `item` when empty.
5. (both) `canPlaceItem(slot, stack)`: if a type is set, only the same `Item` is accepted; if no type is set yet, only stacks with `getMaxStackSize() == 1` (single-stack items such as tools and unique items) may start the collection — see `CORE-05`.
6. (both) `getMaxStackSize()` returns 1 (container level) — see `CORE-06` (hopper pushes are limited accordingly).
7. (both) `stillValid(player)` delegates to the `isValidFor` predicate (usually "player is within reach of the BE"). `setChanged()` fires `onChange`. `fromTag(tag, id)`: loads the positional list and re-derives `item` from slot 0 (if empty, null — note: it only ever inspects slot 0, *inferred* fine since only one item type can exist). `storeTag(tag, id)`: writes the positional list.

### CraftingMatrix

8. (both) A `CraftingContainer` of width×height slots backed by a `NonNullList` (a fresh empty one, or a caller-supplied one). `setChanged()` runs the `onChanged` callback (the menu uses it to re-resolve recipes). `getItem(i)` returns EMPTY for out-of-range indices. `removeItem(i, j)` fires `setChanged` only if something was removed. `stillValid` is always true (the menu itself gates closure). `getItems()` returns a copy. `fillStackedContents` accounts every slot.

## Deviations in the new mod

See the [`util-containers` row of the new-spec 01-core deviation table](../../new-spec/01-core/index.md#deviation-table).
