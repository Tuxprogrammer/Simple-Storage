# Terminal item stacks (StoredItemStack / TerminalItemStack)

| # | Field | Contents |
|---|---|---|
| 1 | **Identity** | `StoredItemStack` (SH `inventory/StoredItemStack.java`), `TerminalItemStack` (SH `inventory/TerminalItemStack.java`). |
| 2 | **Inputs** | An `ItemStack` + a `long` count (arbitrary precision quantity — networks can hold more than 2^31 of one item type), or another `StoredItemStack`. |
| 3 | **Numbers** | Count: `long` (displayed via `NumberFormatUtil`, 03-terminal `terminal-utils.md`). Hash: `31 * itemHash + componentsPatchHash` (int, lazily computed; a genuine zero hash is tracked by `hashZero` so it isn't recomputed). `usedSlotCount`: int, default 1 (terminal-side only — how many inventory slots this item occupies in the network). |
| 4 | **Behavior** | See Behavior detail. |
| 5 | **Determinism** | Pure value semantics. Hash is deterministic per (item, components). |
| 6 | **Data formats** | Not persisted directly — the terminal's binary sync format (01-core `terminal-sync`) encodes item id + components + count; `TerminalSyncManager` does the (de)serialization. `getActualStack()` materializes a real `ItemStack` with count cast to int (overflow possible if count > 2^31 — the cast is unchecked). |
| 7 | **Output** | Display data (name/id/namespace caches), quantities, slot usage; equality for grouping. |
| 8 | **Relationships** | Grouping target in `StorageTerminalBlockEntity.items` (03-terminal `terminal-blocks.md`); sort comparators (03-terminal `sorting.md`); filters test these (03-terminal `filters.md`); `IInventoryAccess` trackers stream them (03-terminal `inventory-access.md`); screen caches key on them (03-terminal `storage-terminal.md`). |
| 9 | **NeoForge dependencies** | `ItemStack.isSameItemSameComponents`, `getComponentsPatch()` (the data-component patch — 1.7.10: NBT), `getHoverName` (display name, 1.7.10: `getDisplayName`), `BuiltInRegistries.ITEM.getKey` (1.7.10: `Registry.NAME_ID`). |
| 10 | **Bugs** | None suspected (`getActualStack`'s int cast is noted in Data formats). |

## Behavior detail

1. `StoredItemStack(ItemStack, long)` stores the given stack reference (no copy); `StoredItemStack(ItemStack)` copies the stack, sets its count to 1, and records the original count as `count`.
2. `hashCode`: lazy `31 * item.hashCode() + componentsPatch.hashCode()`; result 0 is disambiguated by `hashZero`.
3. `equals(Object)`: item + components equal (count ignored). `equals(StoredItemStack)`: also requires equal count. `equalItem(StoredItemStack)`: item + components only.
4. `grow(c)`: count += c. `setCount(long)`. `getMaxStackSize` delegates to the stack.
5. `TerminalItemStack` extends with: `displayNameCache` (hover name string), `descriptionIdCache` (registry id "ns:path"), `namespaceCache`, `usedSlotCount` (default 1, copied by the `StoredItemStack` copy constructor only between `TerminalItemStack`s).
6. `merge(a, b)`: null-safe; sum of counts, sum of usedSlotCounts, hash from `a`.
7. `equalDetails(other)`: count and usedSlotCount equal (used by the sync manager for change detection — 01-core `terminal-sync`).

## Deviations in the new mod

None yet.
