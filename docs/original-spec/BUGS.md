# Suspected bugs in upstream Tom's Simple Storage

<!-- Bugs in UPSTREAM Tom's Simple Storage 2.11.3 at 95bb61d, listed so this backport does not reproduce them.
     Suspected, not confirmed at runtime; no fixes designed here. Merged from every area index. -->

S1 landed 2026-11-03: 54 rows reported, 51 unique IDs after cross-area dedup: 50 suspected bugs plus INT-04, checked and found not a bug (3 pairs reported by two areas).

The ID is the first area's ID; **Also** lists the same bug as reported by other areas. Severity is one of crash, gameplay, performance, visual or cosmetic.

## Crash

| ID | Also | Area(s) | Title | Location |
|---|---|---|---|---|
| TERM-02 | — | `03-terminal` | Unbounded enum index on stale sort NBT | `NeoForge/src/platform-shared/java/com/tom/storagemod/screen/AbstractStorageTerminalScreen.java:138` |
| TERM-12 | — | `03-terminal` | MultiChangeTracker stale array after same-tick network growth | `NeoForge/src/platform-shared/java/com/tom/storagemod/inventory/MultiInventoryAccess.java:194-223` |
| INT-02 | — | `05-integration` | Fabric declares an `emi` entrypoint whose class is not compiled | `Fabric/src/main/resources/fabric.mod.json` (entrypoints), `Fabric/build.gradle:127` |
| INT-07 | — | `05-integration` | TOP progress with zero total | `NeoForge/src/main/java/com/tom/storagemod/top/TheOneProbeHandler.java:50` |
| UI-04 | — | `06-client-ui` | EnumCycleButton NPE if pressed before first state set | `NeoForge/src/platform-shared/java/com/tom/storagemod/screen/widget/EnumCycleButton.java:45` |

## Gameplay

| ID | Also | Area(s) | Title | Location |
|---|---|---|---|---|
| CORE-03 | — | `01-core` | Multi-inventory offsets compaction mis-indexes `connected` | `NeoForge/src/main/java/com/tom/storagemod/inventory/PlatformMultiInventoryAccess.java:68` |
| CORE-04 | — | `01-core` | Filtered `extract` can pass a negative amount (keepLast) | `NeoForge/src/main/java/com/tom/storagemod/inventory/PlatformFilteredInventoryAccess.java:129` |
| CORE-05 | — | `01-core` | Filing cabinet's first item must be single-stack | `NeoForge/src/platform-shared/java/com/tom/storagemod/util/FilingCabinetContainer.java:74` |
| CORE-06 | — | `01-core` | Filing cabinet container max stack size is 1 | `NeoForge/src/platform-shared/java/com/tom/storagemod/util/FilingCabinetContainer.java:80` |
| CORE-08 | INT-08 | `01-core`, `05-integration` | Polymorph terminal widget never registered | `NeoForge/src/main/java/com/tom/storagemod/StorageMod.java:69`, `Fabric/src/main/java/com/tom/storagemod/StorageModClient.java:154` |
| CORE-09 | — | `01-core` | Server-config fields default to 0 before config sync | `NeoForge/src/main/java/com/tom/storagemod/Config.java:30` |
| STOR-01 | — | `02-storage` | Open crate rescan gate unsatisfiable for one position hash | `NeoForge/src/main/java/com/tom/storagemod/block/entity/OpenCrateBlockEntity.java:31` |
| STOR-02 | — | `02-storage` | Filing cabinet item-type lock derived from slot 0 only / re-locks unconditionally | `NeoForge/src/platform-shared/java/com/tom/storagemod/util/FilingCabinetContainer.java:94-99` |
| STOR-03 | — | `02-storage` | `hasConnectedInventories()` ignores actual connections | `NeoForge/src/platform-shared/java/com/tom/storagemod/block/entity/InventoryConnectorBlockEntity.java:188` |
| STOR-04 | — | `02-storage` | Open crate phantom append slot | `NeoForge/src/main/java/com/tom/storagemod/block/entity/OpenCrateBlockEntity.java:40-41` |
| STOR-05 | — | `02-storage` | Connector/interface rescan gate unsatisfiable for one position hash | `NeoForge/src/platform-shared/java/com/tom/storagemod/block/entity/InventoryConnectorBlockEntity.java:50` |
| STOR-06 | — | `02-storage` | Level emitter tick gate unsatisfiable for one position hash | `NeoForge/src/platform-shared/java/com/tom/storagemod/block/entity/LevelEmitterBlockEntity.java:41` |
| STOR-07 | — | `02-storage` | Level emitter `Count` NBT load-default differs from runtime default | `NeoForge/src/platform-shared/java/com/tom/storagemod/block/entity/LevelEmitterBlockEntity.java:91` |
| TERM-01 | — | `03-terminal` | Mouse-wheel scroll step hardcodes 5 rows | `NeoForge/src/platform-shared/java/com/tom/storagemod/screen/AbstractStorageTerminalScreen.java:688` |
| TERM-04 | WIRE-04 | `03-terminal`, `04-wireless` | Beacon scan gate unsatisfiable for one position hash | `NeoForge/src/platform-shared/java/com/tom/storagemod/block/entity/StorageTerminalBlockEntity.java:148` |
| TERM-05 | — | `03-terminal` | Auto-fill item accounting not reserved per cell | `NeoForge/src/platform-shared/java/com/tom/storagemod/menu/TerminalCraftingFiller.java:41-67` |
| TERM-11 | — | `03-terminal` | Item-filter shift-click fill order | `NeoForge/src/platform-shared/java/com/tom/storagemod/menu/ItemFilterMenu.java:95-102` |
| WIRE-01 | — | `04-wireless` | Basic wireless item force-returns SUCCESS_SERVER | `NeoForge/src/platform-shared/java/com/tom/storagemod/item/WirelessTerminalItem.java:42` |
| WIRE-03 | — | `04-wireless` | Invalid/unloaded bound terminal not consumed | `NeoForge/src/platform-shared/java/com/tom/storagemod/item/AdvWirelessTerminalItem.java:77` |
| INT-01 | — | `05-integration` | EMI plugin classes never compiled on either loader | `NeoForge/build.gradle:134`, `Fabric/build.gradle:127` |
| INT-03 | — | `05-integration` | EMI "craft" never sends the fill packet | `NeoForge/src/platform-shared/java/com/tom/storagemod/emi/EmiTransferHandler.java:132-137` |
| INT-09 | — | `05-integration` | Sophisticated Storage compat dead on Fabric | `Fabric/src/main/java/com/tom/storagemod/StorageMod.java:179-181`, `Fabric/build.gradle:131` |
| INT-10 | — | `05-integration` | JEI clickable recipe area misses the result slot | `NeoForge/src/platform-shared/java/com/tom/storagemod/jei/JEIHandler.java:45` |
| UI-05 | — | `06-client-ui` | ListWidget scrollbar grab area is hard-coded and misses the drawn indicator | `NeoForge/src/platform-shared/java/com/tom/storagemod/screen/widget/ListWidget.java:62-65,84-87` |

## Performance

| ID | Also | Area(s) | Title | Location |
|---|---|---|---|---|
| CORE-02 | — | `01-core` | `DataPacket` carries unbounded NBT | `NeoForge/src/platform-shared/java/com/tom/storagemod/network/DataPacket.java:17` |
| STOR-09 | — | `02-storage` | Unfiltered network-source hopper spins every tick | `NeoForge/src/platform-shared/java/com/tom/storagemod/block/entity/BasicInventoryHopperBlockEntity.java:83` |
| TERM-09 | — | `03-terminal` | ComparatorType lookup not thread-safe | `NeoForge/src/platform-shared/java/com/tom/storagemod/inventory/sorting/ComparatorType.java:30-39` |
| INT-06 | — | `05-integration` | Jade config providers registered for every block and block entity | `NeoForge/src/platform-shared/java/com/tom/storagemod/jade/JadePlugin.java:27,34` |

## Visual

| ID | Also | Area(s) | Title | Location |
|---|---|---|---|---|
| INT-05 | — | `05-integration` | REI/EMI missing-slot highlight uses a guessed index mapping | `NeoForge/src/platform-shared/java/com/tom/storagemod/rei/ReiTransferHandler.java:67`, `NeoForge/src/platform-shared/java/com/tom/storagemod/emi/EmiTransferHandler.java:125` |
| UI-06 | — | `06-client-ui` | PopupMenu click rects are offset from the rendered tooltip position | `NeoForge/src/platform-shared/java/com/tom/storagemod/util/PopupMenuManager.java:45-73` |

## Cosmetic

| ID | Also | Area(s) | Title | Location |
|---|---|---|---|---|
| CORE-01 | — | `01-core` | `importantInfo` config entry has no label | `NeoForge/src/main/java/com/tom/storagemod/Config.java:135` |
| CORE-07 | UI-03 | `01-core`, `06-client-ui` | Missing lang key `tooltip.toms_trading_network.textfield` | `NeoForge/src/platform-shared/java/com/tom/storagemod/util/PopupMenuManager.java:163` |
| CORE-10 | — | `01-core` | `KeyUtil` only checks the left modifier keys | `NeoForge/src/platform-shared/java/com/tom/storagemod/util/KeyUtil.java:11` |
| STOR-08 | — | `02-storage` | Hopper `waiting == 2` branch is dead | `NeoForge/src/platform-shared/java/com/tom/storagemod/block/entity/BasicInventoryHopperBlockEntity.java:101` |
| STOR-10 | — | `02-storage` | Channel name fallback is time-based | `NeoForge/src/platform-shared/java/com/tom/storagemod/inventory/RemoteConnections.java:131` |
| TERM-03 | — | `03-terminal` | Stat tooltip divides by zero slot count | `NeoForge/src/platform-shared/java/com/tom/storagemod/screen/AbstractStorageTerminalScreen.java:461` |
| TERM-06 | — | `03-terminal` | Filing cabinet wheel scroll divides by (rows−5) | `NeoForge/src/platform-shared/java/com/tom/storagemod/screen/FilingCabinetScreen.java:93-94` |
| TERM-07 | — | `03-terminal` | Keep-last button narrator reuses "skip" | `NeoForge/src/platform-shared/java/com/tom/storagemod/screen/InventoryConfiguratorScreen.java:70-71` |
| TERM-08 | — | `03-terminal` | Typed channel name lookup is ambiguous | `NeoForge/src/platform-shared/java/com/tom/storagemod/screen/InventoryLinkScreen.java:130-137` |
| TERM-10 | — | `03-terminal` | Level emitter SHIFT-step table is asymmetric | `NeoForge/src/platform-shared/java/com/tom/storagemod/screen/LevelEmitterScreen.java:83-91` |
| WIRE-02 | — | `04-wireless` | Typo in advanced terminal tooltip | `NeoForge/src/platform-shared/resources/assets/toms_storage/lang/en_us.json:82` |
| WIRE-05 | — | `04-wireless` | `bound_pos` codec field name inconsistent | `NeoForge/src/platform-shared/java/com/tom/storagemod/components/WorldPos.java:18` |
| UI-01 | — | `06-client-ui` | 11 narrator keys referenced in code are missing from all lang files | `NeoForge/src/platform-shared/resources/assets/toms_storage/lang/en_us.json` (missing `narrator.toms_storage.*` keys) |
| UI-02 | — | `06-client-ui` | EnumCycleButton narration key constructed dynamically, exists for no value | `NeoForge/src/platform-shared/java/com/tom/storagemod/screen/widget/EnumCycleButton.java:26` |
| UI-07 | — | `06-client-ui` | IconButton base narration is empty | `NeoForge/src/platform-shared/java/com/tom/storagemod/screen/widget/IconButton.java:25` |

## Not bugs

<!-- Per CONVENTIONS.md: a finding later checked and found correct keeps its ID and row; its severity becomes "not a bug" and the row gives the evidence. -->

| ID | Also | Area(s) | Title | Location |
|---|---|---|---|---|
| INT-04 | — | `05-integration` | EMI inventory skips slot 0 — checked: slot 0 is the crafting result slot in `CraftingTerminalMenu`, so excluding it is correct | `NeoForge/src/platform-shared/java/com/tom/storagemod/emi/EmiTransferHandler.java:48` |

<!-- In this mod: a regression test per reproducible bug, named doesNotReproduce<ID>_<what>,
     and the change's "Inherited bugs avoided" list names the IDs. Ported files list them on their Avoids: line. -->
