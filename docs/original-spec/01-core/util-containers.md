# Containers and crafting matrix

| # | Field | Contents |
|---|---|---|
| 1 | **Identity** | `BasicContainer` (SH), `LimitedContainer` (SH), `FilingCabinetContainer` (SH), `CraftingMatrix` (SH). Sources: `NeoForge/src/platform-shared/java/com/tom/storagemod/util/` `BasicContainer.java`, `LimitedContainer.java`, `FilingCabinetContainer.java`, `CraftingMatrix.java` (short citations below are relative to these). |
| 2 | **Inputs** | `BasicContainer`: constructor size or item varargs, `ValueInput.TypedInputList<ItemStackWithSlot>` (NBT load). `LimitedContainer`: a delegate `Container`, a size limit, an optional start offset. `FilingCabinetContainer`: size, `onChange` callback, `isValidFor` player predicate, NBT list id. `CraftingMatrix`: width, height, `onChanged` callback, optional item list. |
| 3 | **Numbers** | `FilingCabinetContainer.getMaxStackSize()` = 1 (container-level). (FilingCabinetContainer.java:80-82) `LimitedContainer.getContainerSize()` = min(sizeLimit, delegateSize - startOffset). (LimitedContainer.java:28-30) No other constants. |
| 4 | **Behavior** | Ordered steps in [Behavior detail](#behavior-detail). |
| 5 | **Determinism** | None. |
| 6 | **Data formats** | `BasicContainer.loadItems/storeItems` and `Util.loadItems/storeItems` (util-misc.md): an NBT list under a caller-chosen key of `ItemStackWithSlot` entries (slot + stack codec); empty slots are skipped on save, all slots are cleared before load. (BasicContainer.java:47-56; skip-empty on save: BasicContainer.java:59-66) `FilingCabinetContainer.fromTag/storeTag`: an NBT list under the caller-chosen key of plain `ItemStack` entries (no slot numbers — slots are positional). (FilingCabinetContainer.java:94-103) |
| 7 | **Output** | Container objects used by menus (03-terminal/06-client-ui): the filing cabinet BE's inventory (02-storage), limited views of larger containers, and the crafting terminal's matrix (03-terminal). |
| 8 | **Relationships** | `FilingCabinetContainer` is the BE inventory behind `filing_cabinet` (02-storage) and is exposed to hoppers via `VanillaContainerWrapper.of(be.getInv())` (mod-lifecycle.md). `CraftingMatrix` implements `CraftingContainer` for the crafting terminal menu (03-terminal). `Util` (util-misc.md) holds the shared load/store helpers that mirror `BasicContainer`'s methods. |
| 9 | **NeoForge dependencies** | None directly (vanilla `Container`/`SimpleContainer`/`CraftingContainer` interfaces; `ItemStackWithSlot` + `ValueInput`/`ValueOutput` codecs are vanilla data layers). |
| 10 | **Bugs** | `CORE-05` (first item of a filing cabinet must be single-stack), `CORE-06` (filing cabinet max stack size 1 for automation). |

## Behavior detail

### BasicContainer

1. (both) Extends `SimpleContainer`; adds an optional listener list. (BasicContainer.java:13-14) `addListener`/`removeListener` manage it lazily. (BasicContainer.java:24-36) `setChanged()` (called by `SimpleContainer` on any mutation) fires `containerChanged(this)` on every listener (used by menus to mark the BE dirty). (BasicContainer.java:38-45)
2. (both) `loadItems(list)`: clears all slots, then for each entry with `isValidInContainer(size)` (slot in range), places the stack at that slot. (BasicContainer.java:47-56) `storeItems(output)`: writes every non-empty slot as an `ItemStackWithSlot`. (BasicContainer.java:59-66)

### LimitedContainer

3. (both) A window over a delegate `Container`: `getContainerSize()` = `min(sizeLimit, delegate.getContainerSize() - startOffset)`; (LimitedContainer.java:28-30) all slot operations (`getItem`, `removeItem`, `removeItemNoUpdate`, `setItem`, `canPlaceItem`) are offset by `startOffset` into the delegate. (LimitedContainer.java:38-54, 83-85) `isEmpty()`, `countItem`, `hasAnyOf`, `hasAnyMatching`, `startOpen`, `stopOpen`, `maxStackSize` delegate without offset (LimitedContainer.java:33-35, 58-60, 73-80, 88-100) (i.e. they reflect the whole delegate, not just the window — *inferred* acceptable since they are aggregate queries). `setSizeLimit`/`setStartOffset` allow resizing the window at runtime (LimitedContainer.java:102-108) (menus use this to show a subset of a large inventory).

### FilingCabinetContainer

4. (both) A single-item-type cabinet: an internal `SimpleContainer` of the given size plus a remembered `item`. (FilingCabinetContainer.java:15-23) `setItem(slot, stack)`: stores the stack; if non-empty, `item = stack.getItem()`; if the container becomes empty, `item = null`. (FilingCabinetContainer.java:61-66) `removeItem`/`removeItemNoUpdate`: remove and clear `item` when empty. (FilingCabinetContainer.java:47-58)
5. (both) `canPlaceItem(slot, stack)`: if a type is set, only the same `Item` is accepted; (FilingCabinetContainer.java:74-77) if no type is set yet, only stacks with `getMaxStackSize() == 1` (FilingCabinetContainer.java:76) (single-stack items such as tools and unique items) may start the collection — see `CORE-05`.
6. (both) `getMaxStackSize()` returns 1 (container level) (FilingCabinetContainer.java:80-82) — see `CORE-06` (hopper pushes are limited accordingly).
7. (both) `stillValid(player)` delegates to the `isValidFor` predicate (FilingCabinetContainer.java:69-71) (usually "player is within reach of the BE"). `setChanged()` fires `onChange`. (FilingCabinetContainer.java:85-88) `fromTag(tag, id)`: loads the positional list and re-derives `item` from slot 0 (FilingCabinetContainer.java:94-99) (if empty, null — note: it only ever inspects slot 0, *inferred* fine since only one item type can exist). `storeTag(tag, id)`: writes the positional list. (FilingCabinetContainer.java:101-103)

### CraftingMatrix

8. (both) A `CraftingContainer` of width×height slots backed by a `NonNullList` (a fresh empty one, or a caller-supplied one). (CraftingMatrix.java:19-28) `setChanged()` runs the `onChanged` callback (CraftingMatrix.java:78-80) (the menu uses it to re-resolve recipes). `getItem(i)` returns EMPTY for out-of-range indices. (CraftingMatrix.java:52-54) `removeItem(i, j)` fires `setChanged` only if something was removed. (CraftingMatrix.java:62-69) `stillValid` is always true (CraftingMatrix.java:83-85) (the menu itself gates closure). `getItems()` returns a copy. (CraftingMatrix.java:103-105) `fillStackedContents` accounts every slot. (CraftingMatrix.java:108-115)

## Deviations in the new mod

See the [`util-containers` row of the new-spec 01-core deviation table](../../new-spec/01-core/deviations.md#deviation-table).
