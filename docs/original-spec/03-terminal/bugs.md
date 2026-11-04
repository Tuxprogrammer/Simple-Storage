# Terminal · suspected bugs

Part of [03-terminal](index.md).

## Suspected bugs

| ID | Title | Location | What looks wrong | Severity |
|---|---|---|---|---|
| TERM-01 | Mouse-wheel scroll step hardcodes 5 rows | `NeoForge/src/platform-shared/java/com/tom/storagemod/screen/AbstractStorageTerminalScreen.java:688` | `i = (size+8)/9 - 5` computes the scroll range as if 5 rows were visible; in tall mode (rowCount up to ~46) each wheel notch moves the wrong fraction of the list. | gameplay |
| TERM-02 | Unbounded enum index on stale sort NBT | `NeoForge/src/platform-shared/java/com/tom/storagemod/screen/AbstractStorageTerminalScreen.java:138` | `sortingType = SortingTypes.VALUES[sorting & 0xFF]` has no bounds check; a value ≥ 6 (edited/corrupted `sorting` NBT from an older version) throws `ArrayIndexOutOfBoundsException` when the GUI opens. | crash |
| TERM-03 | Stat tooltip divides by zero slot count | `NeoForge/src/platform-shared/java/com/tom/storagemod/screen/AbstractStorageTerminalScreen.java:461` | `Math.round(menu.freeCount / (float) menu.slotCount * 100)` guards only the 32767 case; with slotCount 0 (empty network) 0/0 = NaN rounds to 0, showing "0%" (Infinity if free > 0). | cosmetic |
| TERM-04 | Beacon scan gate unsatisfiable for one position hash | `NeoForge/src/platform-shared/java/com/tom/storagemod/block/entity/StorageTerminalBlockEntity.java:148` | `level.getGameTime() % 40 == Math.abs(worldPosition.hashCode()) % 40`: if the block position's hash is `Integer.MIN_VALUE`, `Math.abs` stays negative and the comparison never holds, so `beaconLevel` is never computed. | gameplay |
| TERM-05 | Auto-fill item accounting not reserved per cell | `NeoForge/src/platform-shared/java/com/tom/storagemod/menu/TerminalCraftingFiller.java:41-67` | `placeRecipe` accounts player-inventory stacks once (`allItems`) but pulls lazily per grid cell; suspected double-claim window when two cells need the same scarce item (each pull checks success, so this is an edge, not a proven loss). | gameplay |
| TERM-06 | Filing cabinet wheel scroll divides by (rows−5) | `NeoForge/src/platform-shared/java/com/tom/storagemod/screen/FilingCabinetScreen.java:93-94` | `i = (size+8)/9 - 5` is 0 when the cabinet holds exactly 45 items → `delta / 0.0f` = ±Inf, clamped to 0/1 (instant full jump instead of a no-op). | cosmetic |
| TERM-07 | Keep-last button narrator reuses "skip" | `NeoForge/src/platform-shared/java/com/tom/storagemod/screen/InventoryConfiguratorScreen.java:70-71` | `buttonKeepLast` is built with `.name(Component.translatable("narrator.toms_storage.skip"))` — screen-reader text says "skip" for the keep-last toggle. | cosmetic |
| TERM-08 | Typed channel name lookup is ambiguous | `NeoForge/src/platform-shared/java/com/tom/storagemod/screen/InventoryLinkScreen.java:130-137` | Creating channels never checks name uniqueness; the text-field responder selects the first channel whose display name equals the typed text, so duplicate names make the lookup order-dependent. | cosmetic |
| TERM-09 | ComparatorType lookup not thread-safe | `NeoForge/src/platform-shared/java/com/tom/storagemod/inventory/sorting/ComparatorType.java:30-39` | Static `LOOKUP` (SoftReference) and `lastAccess` are read/written without synchronization; parallel terminal sorting can rebuild the lookup concurrently (benign race, wasted work). | performance |
| TERM-10 | Level emitter SHIFT-step table is asymmetric | `NeoForge/src/platform-shared/java/com/tom/storagemod/screen/LevelEmitterScreen.java:83-91` | The +1000 button's SHIFT step is −64 (not +1000 doubled), and the negative buttons' SHIFT steps are not consistent doubles of their base steps; the "double with SHIFT" pattern the other buttons follow is broken. | cosmetic |
| TERM-11 | Item-filter shift-click fill order | `NeoForge/src/platform-shared/java/com/tom/storagemod/menu/ItemFilterMenu.java:95-102` | Shift-click places a copy in the first slot that is empty *or* holds a different item, but the scan breaks at the first matching item — a grid already containing the item at slot k will never fill empty slots after k (inferred edge, no repro). | gameplay |
| TERM-12 | MultiChangeTracker stale array after same-tick network growth | `NeoForge/src/platform-shared/java/com/tom/storagemod/inventory/MultiInventoryAccess.java:194-223` | `trackers`/`items` arrays are resized only when `connected.size()` differs at the top of `getChangeTracker`; if `connected` grows later in the same game tick (network `build` before first read), `trackers[i]` indexes a shorter array → `ArrayIndexOutOfBounds`. | crash |

## Deviations in the new mod

None yet (port notes go to the new-spec 03-terminal area).
