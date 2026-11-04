# Level Emitter

## Identity
`com.tom.storagemod.block.LevelEmitterBlock` (SH) + `LevelEmitterBlockEntity` (SH). Emits a full-strength redstone signal when the connected network's item count for a filter passes (or fails) a threshold.

Sources (upstream 26.2@95bb61d): `NeoForge/src/platform-shared/java/com/tom/storagemod/block/LevelEmitterBlock.java` and `.../block/entity/LevelEmitterBlockEntity.java`; short names below are `Class.java:line` in those files (class declarations: LevelEmitterBlock.java:52, LevelEmitterBlockEntity.java:28).

## Inputs
- Placement: `getStateForPlacement` FACING = clicked face (not inverted — FACING points at the block it was placed against) (LevelEmitterBlock.java:119-121).
- Right-click (server): opens `LevelEmitterMenu` (title `menu.toms_storage.level_emitter` = "Level Emitter") (LevelEmitterBlockEntity.java:127-130; LevelEmitterBlock.java:181-193); GUI in 03-terminal `level-emitter-gui.md`.
- Server tick: staggered; work when `getGameTime() % 10 == Math.abs(worldPosition.hashCode()) % 10` (1 in 10 ticks) (LevelEmitterBlockEntity.java:41; ticker LevelEmitterBlock.java:77).
- Block state: `FACING` (default DOWN), `POWERED` (default false) (LevelEmitterBlock.java:53-54, 59-60).

## Numbers
- Signal strength: 15 (full) or 0; direct signal only on the face where `FACING.getOpposite() == side`, indirect (`getSignal`) is 15 on all sides when powered (LevelEmitterBlock.java:86-92, 95-100).
- `canConnectRedstone`: true on all faces except FACING (LevelEmitterBlock.java:104-106).
- Redstone cable connection: only `FACING.getOpposite()` (`canConnectFrom`); network scan: single face `BlockFace(pos.relative(FACING.getOpposite()), FACING)` — the inventory is read from the block behind the emitter (LevelEmitterBlock.java:134-142).
- Particle offset jitter: ±0.1 block, redstone dust particle at -0.4375 along the emission axis (observed: `-7.0F / 16.0F`) (LevelEmitterBlock.java:169-177).

## Behavior
1. (server, staggered tick) `updateServer`:
   1. Read current `POWERED` from block state (LevelEmitterBlockEntity.java:42-43).
   2. If `filter != null`:
      - `topCache.getAccess(level, worldPosition).tracker()` — the `NetworkInventory` cache (03-terminal `inventory-access.md`) is loaded at `pos.relative(FACING.getOpposite())` with direction FACING (in `onLoad`) (LevelEmitterBlockEntity.java:141-147).
      - If the change tracker value changed: recompute `counter = tracker.countItems(filter)`; `currState = lessThan ? counter < count : counter > count` (LevelEmitterBlockEntity.java:48-55).
      - If unchanged: return (no state update) (LevelEmitterBlockEntity.java:56).
   3. Else `currState = false` (LevelEmitterBlockEntity.java:57-59).
   4. If `currState != POWERED`: set state (flag 3), then `Platform.notifyBlocks` + `updateNeighborsAt` at `pos.relative(FACING)` and `pos.relative(FACING).relative(FACING)` with the experimental redstone `Orientation` (01-core `Platform`) (LevelEmitterBlockEntity.java:60-70).
2. Filter/count setters reset `changeTracker = 0` to force re-evaluation on next tick (LevelEmitterBlockEntity.java:95-98, 104-107, 113-116).
3. `stillValid(player)`: BE identity + `distanceToSqr < 64.0` (LevelEmitterBlockEntity.java:132-138).

## Determinism
Deterministic given the network's change tracker.

## Data formats
NBT (BE): `Filter` — optional item stack (the filter, stored as the actual stack), `Count` — int (load default **0**, runtime field default **1** (LevelEmitterBlockEntity.java:30; load default 0 at :91) — see STOR-07), `lessThan` — boolean (default false) (LevelEmitterBlockEntity.java:74-93).

## Output
Redstone signal (15/0) on the back-facing side; block state `POWERED` drives the model.

## Relationships
- 03-terminal: `LevelEmitterMenu`/`LevelEmitterScreen` (GUI), `StoredItemStack` (filter representation), `NetworkInventory` (access cache).
- 01-core: `Platform.notifyBlocks`, `ExperimentalRedstoneUtils` orientation (NeoForge experimental redstone), `TickerUtil`, `ClientUtil.tooltip`.

## NeoForge dependencies
`BlockStateProperties.POWERED`, `ExperimentalRedstoneUtils`/`Orientation` (NeoForge experimental redstone system), `DustParticleOptions.REDSTONE`, codec, `TickerUtil` (platform ticker).

## Bugs
STOR-06 (Math.abs tick gate, `LevelEmitterBlockEntity.java:41`); STOR-07 (NBT Count load-default 0 vs runtime default 1, `LevelEmitterBlockEntity.java:91`).

## Deviations in the new mod

See [new-spec 02-storage](../../new-spec/02-storage/index.md) (row: level-emitter).
