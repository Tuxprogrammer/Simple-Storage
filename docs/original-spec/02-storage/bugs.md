# storage · suspected bugs

Part of [storage](index.md).

## Suspected bugs

| ID | Title | Location | What looks wrong | Severity |
|---|---|---|---|---|
| STOR-01 | Open crate rescan gate unsatisfiable for one position hash | `NeoForge/src/main/java/com/tom/storagemod/block/entity/OpenCrateBlockEntity.java:31` | `getGameTime() % 5 == Math.abs(worldPosition.hashCode()) % 5`: if the position hash is `Integer.MIN_VALUE`, `Math.abs` stays negative and the gate never holds — the crate's item list is never rebuilt (stale/empty virtual inventory forever). | gameplay |
| STOR-02 | Filing cabinet item-type lock derived from slot 0 only / re-locks unconditionally | `NeoForge/src/platform-shared/java/com/tom/storagemod/util/FilingCabinetContainer.java:94-99` | `fromTag` sets the lock from slot 0 only: a cabinet loaded with slot 0 empty is "unlocked" and accepts a second item type while other slots are filled; `setItem` (lines 61-66) also re-locks to any written type without checking the existing lock, so API writes can mix types. | gameplay |
| STOR-03 | `hasConnectedInventories()` ignores actual connections | `NeoForge/src/platform-shared/java/com/tom/storagemod/block/entity/InventoryConnectorBlockEntity.java:188` | Returns `!isRemoved()`; an `InventoryInterfaceBlockEntity` (its `updateServer` discovery, line 46) treats any living connector as a usable network even with zero attached inventories. | gameplay |
| STOR-04 | Open crate phantom append slot | `NeoForge/src/main/java/com/tom/storagemod/block/entity/OpenCrateBlockEntity.java:40-41` | `getContainerSize()` returns `items.size() + 1` and `canPlaceItem` (lines 108-109) accepts only `index == items.size()`, but `getItem` there returns EMPTY — the append slot is only meaningful through the platform adapter's push convention; a plain `Container` consumer sees a permanently empty last slot. | gameplay |
| STOR-05 | Connector/interface rescan gate unsatisfiable for one position hash | `NeoForge/src/platform-shared/java/com/tom/storagemod/block/entity/InventoryConnectorBlockEntity.java:50` | `time % 20 == Math.abs(worldPosition.hashCode()) % 20`: with a `MIN_VALUE` hash the connector never rebuilds its inventories (dead network); same pattern at `InventoryInterfaceBlockEntity.java:31` (interface never re-links). | gameplay |
| STOR-06 | Level emitter tick gate unsatisfiable for one position hash | `NeoForge/src/platform-shared/java/com/tom/storagemod/block/entity/LevelEmitterBlockEntity.java:41` | `getGameTime() % 10 != Math.abs(worldPosition.hashCode()) % 10 → return`: with a `MIN_VALUE` hash the emitter never re-evaluates, so `POWERED` is frozen. | gameplay |
| STOR-07 | Level emitter `Count` NBT load-default differs from runtime default | `NeoForge/src/platform-shared/java/com/tom/storagemod/block/entity/LevelEmitterBlockEntity.java:91` | Field default is `count = 1` (line 30) but `loadAdditional` uses `getIntOr("Count", 0)` — a missing/legacy tag loads threshold 0, so in greater-than mode any non-zero count powers the emitter. | gameplay |
| STOR-08 | Hopper `waiting == 2` branch is dead | `NeoForge/src/platform-shared/java/com/tom/storagemod/block/entity/BasicInventoryHopperBlockEntity.java:101` | `waiting` is assigned 0, 1 and 3 but never 2, so the `if (waiting == 2) return;` after the bottom-change check never fires — an incomplete state machine (the intended "bottom just changed" pause is missing). | cosmetic |
| STOR-09 | Unfiltered network-source hopper spins every tick | `NeoForge/src/platform-shared/java/com/tom/storagemod/block/entity/BasicInventoryHopperBlockEntity.java:83` | `if (topNet && !hasFilter) return;` fires after the cooldown is already consumed but sets no cooldown — a network-source hopper without a filter re-runs the full check every tick. | performance |
| STOR-10 | Channel name fallback is time-based | `NeoForge/src/platform-shared/java/com/tom/storagemod/inventory/RemoteConnections.java:131` | `makeChannel` falls back to `"Channel " + System.currentTimeMillis()` for empty/overlong names — nondeterministic display name, and two channels created in the same millisecond collide (feeds the 03-terminal TERM-08 ambiguous-name lookup). | cosmetic |

## Deviations in the new mod

- The area table calls this "the storage item with internal inventory": upstream 26.2 has **no standalone storage item** — the filing cabinet is a block. The new spec should say so and decide whether the 1.7.10 port keeps it a block (recommended for GTNH crate parity).

See also the row in [new-spec 02-storage](../../new-spec/02-storage/index.md) (row: index).

