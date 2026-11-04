# Terminal item stacks (StoredItemStack / TerminalItemStack)

| # | Field | Contents |
|---|---|---|
| 1 | **Identity** | `StoredItemStack` (SH `inventory/StoredItemStack.java`), `TerminalItemStack` (SH `inventory/TerminalItemStack.java`). Sources: `NeoForge/src/platform-shared/java/com/tom/storagemod/inventory/StoredItemStack.java` (`StoredItemStack.java:NN`) and `NeoForge/src/platform-shared/java/com/tom/storagemod/inventory/TerminalItemStack.java` (`TerminalItemStack.java:NN`). |
| 2 | **Inputs** | An `ItemStack` + a `long` count (arbitrary precision quantity — networks can hold more than 2^31 of one item type), or another `StoredItemStack`. |
| 3 | **Numbers** | Count: `long` (StoredItemStack.java:7) (displayed via `NumberFormatUtil`, 03-terminal `terminal-utils.md`). Hash: `31 * (31 + itemHash) + componentsPatchHash` (the computation starts from result 1, so the item hash is multiplied by 31 twice; int, lazily computed; StoredItemStack.java:51-61); a genuine zero hash is tracked by `hashZero` so it isn't recomputed, StoredItemStack.java:9,52,58). `usedSlotCount`: int, default 1 (TerminalItemStack.java:10) (terminal-side only — how many inventory slots this item occupies in the network). |
| 4 | **Behavior** | See Behavior detail. |
| 5 | **Determinism** | Pure value semantics. Hash is deterministic per (item, components). |
| 6 | **Data formats** | Not persisted directly — the terminal's binary sync format (01-core `terminal-sync`) encodes item id + components + count; `TerminalSyncManager` does the (de)serialization. `getActualStack()` materializes a real `ItemStack` with count cast to int (overflow possible if count > 2^31 — the cast is unchecked) (StoredItemStack.java:44-47). |
| 7 | **Output** | Display data (name/id/namespace caches), quantities, slot usage; equality for grouping. |
| 8 | **Relationships** | Grouping target in `StorageTerminalBlockEntity.items` (03-terminal `terminal-blocks.md`); sort comparators (03-terminal `sorting.md`); filters test these (03-terminal `filters.md`); `IInventoryAccess` trackers stream them (03-terminal `inventory-access.md`); screen caches key on them (03-terminal `storage-terminal.md`). |
| 9 | **NeoForge dependencies** | `ItemStack.isSameItemSameComponents`, `getComponentsPatch()` (the data-component patch — 1.7.10: NBT), `getHoverName` (display name, 1.7.10: `getDisplayName`), `BuiltInRegistries.ITEM.getKey` (1.7.10: `Registry.NAME_ID`). |
| 10 | **Bugs** | None suspected (`getActualStack`'s int cast is noted in Data formats). |

## Behavior detail

1. `StoredItemStack(ItemStack, long)` stores the given stack reference (no copy) (StoredItemStack.java:11-14); `StoredItemStack(ItemStack)` copies the stack, sets its count to 1, and records the original count as `count` (StoredItemStack.java:16-19).
2. `hashCode`: lazy `31 * (31 + item.hashCode()) + componentsPatch.hashCode()` (starts from result 1); result 0 is disambiguated by `hashZero` (StoredItemStack.java:52-59).
3. `equals(Object)`: item + components equal (count ignored) (StoredItemStack.java:64-75, count check commented out at :70). `equals(StoredItemStack)`: also requires equal count (StoredItemStack.java:77-85, :80). `equalItem(StoredItemStack)`: item + components only (StoredItemStack.java:87-94).
4. `grow(c)`: count += c (StoredItemStack.java:96-98). `setCount(long)`. `getMaxStackSize` delegates to the stack (StoredItemStack.java:104-105).
5. `TerminalItemStack` extends with: `displayNameCache` (hover name string, TerminalItemStack.java:38-42), `descriptionIdCache` (registry id "ns:path", TerminalItemStack.java:44-50), `namespaceCache` (TerminalItemStack.java:52-58), `usedSlotCount` (default 1, copied by the `StoredItemStack` copy constructor only between `TerminalItemStack`s) (TerminalItemStack.java:24-28).
6. `merge(a, b)`: null-safe; sum of counts, sum of usedSlotCounts, hash from `a` (TerminalItemStack.java:30-36).
7. `equalDetails(other)`: count and usedSlotCount equal (TerminalItemStack.java:68-70) (used by the sync manager for change detection — 01-core `terminal-sync`).

## Deviations in the new mod

See the [03-terminal deviation table](../../new-spec/03-terminal/deviations.md#deviation-table) (row `terminal-stacks`).
