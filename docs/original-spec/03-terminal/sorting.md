# Terminal sorting

| # | Field | Contents |
|---|---|---|
| 1 | **Identity** | `SortingTypes` (SH `inventory/sorting/SortingTypes.java`) + comparators: `ComparatorAmount`, `ComparatorName`, `ComparatorMod`, `ComparatorSpaceEfficiency`, `ComparatorID`, `ComparatorType` (SH `inventory/sorting/`). Sources: `NeoForge/src/platform-shared/java/com/tom/storagemod/inventory/sorting/` (short `Class.java:NN` below are in that folder; `AbstractStorageTerminalScreen.java` is in `NeoForge/src/platform-shared/java/com/tom/storagemod/screen/`, `StorageTerminalBlockEntity.java` in `NeoForge/src/platform-shared/java/com/tom/storagemod/block/entity/`). |
| 2 | **Inputs** | Two `TerminalItemStack`s; the reverse flag; (for TYPE) the creative "search tab" item list of the running game. |
| 3 | **Numbers** | `SortingTypes.VALUES` order (NBT ordinals): 0 AMOUNT, 1 NAME, 2 MOD, 3 SPACE_EFFICIENCY, 4 ID, 5 TYPE (SortingTypes.java:9-14, VALUES at :17). `ComparatorType` lookup: soft-referenced map rebuilt after 5 minutes (300000 ms) of non-access or GC (ComparatorType.java:27-36, 5 * 60 * 1000 at :33) (UNVERIFIED: `lastAccess` is assigned only when the lookup is rebuilt, so the 5 minutes run from the last rebuild, not from the last access, ComparatorType.java:33-36); items absent from the lookup sort as `Integer.MAX_VALUE` (ComparatorType.java:44-45). |
| 4 | **Behavior** | See Behavior detail. |
| 5 | **Determinism** | AMOUNT/NAME/MOD/SPACE_EFFICIENCY/ID: fully deterministic given the (locale-dependent) display names. TYPE: deterministic for a fixed item registry (creative search-tab order, ComparatorType.java:16-24), but the lookup is cached for 5 min — registry changes mid-session are reflected only after cache expiry or GC. |
| 6 | **Data formats** | NBT: `sorting` int on the terminal block entity — low 8 bits = ordinal, 0x100 = reversed, 0x200 = ghost mode off (03-terminal `storage-terminal.md`) (client packing AbstractStorageTerminalScreen.java:168-170, unpacking :137-141; block entity key StorageTerminalBlockEntity.java:204, read :212). |
| 7 | **Output** | A `Comparator<TerminalItemStack>` used by the screen to order `itemListClientSorted` (AbstractStorageTerminalScreen.java:352). |
| 8 | **Relationships** | `TerminalItemStack` accessors: `getQuantity` (long), `getDisplayName` (cached locale string), `getDescriptionId` (cached registry id string), `getNamespace` (cached), `getUsedSlotCount` (int). Used by `AbstractStorageTerminalScreen.updateSearch` (03-terminal `storage-terminal.md`). |
| 9 | **NeoForge dependencies** | `CreativeModeTabs.searchTab()` + `getSearchTabDisplayItems()` (TYPE order source — 1.7.10: creative tab list via `CreativeTabs`), `BuiltInRegistries.ITEM` key (1.7.10: `Registry.NAME_ID`). `SoftReference` cache. |
| 10 | **Bugs** | `TERM-09` (`ComparatorType` static `LOOKUP`/`lastAccess` are not synchronized — two threads sorting simultaneously can both rebuild the lookup; benign but wasteful, and `lastAccess` is written on every `compare` call's lookup path). |

## Behavior detail

1. `SortingTypes.create(reversed)` returns a fresh comparator instance, reversed when requested (`factory.get().reversed()`) (SortingTypes.java:24-25).
2. AMOUNT: quantity descending (long compare, negated), tie → display name ascending (ComparatorAmount.java:10-13).
3. NAME: display name ascending only (ComparatorName.java:10).
4. MOD: namespace ascending, tie → display name ascending (ComparatorMod.java:10-13).
5. SPACE_EFFICIENCY: used-slot count descending (negated long compare), tie → quantity descending, tie → display name ascending (ComparatorSpaceEfficiency.java:10-15).
6. ID: full registry id string (namespace:path) ascending (ComparatorID.java:10).
7. TYPE: creative search-tab index ascending (items not in the tab → MAX_VALUE, i.e. after all known items), tie → display name ascending (ComparatorType.java:44-48).
8. Reverse flag wraps the whole comparator (including the name tie-breaks — a reversed comparator reverses every level).

## Deviations in the new mod

See the [03-terminal deviation table](../../new-spec/03-terminal/deviations.md#deviation-table) (row `sorting`).
