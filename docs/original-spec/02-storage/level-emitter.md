# Level Emitter

## Identity
`com.tom.storagemod.block.LevelEmitterBlock` (SH) + `LevelEmitterBlockEntity` (SH). Emits a full-strength redstone signal when the connected network's item count for a filter passes (or fails) a threshold.

## Inputs
- Placement: `getStateForPlacement` FACING = clicked face (not inverted — FACING points at the block it was placed against).
- Right-click (server): opens `LevelEmitterMenu` (title `menu.toms_storage.level_emitter` = "Level Emitter"); GUI in 03-terminal `level-emitter-gui.md`.
- Server tick: staggered; work when `getGameTime() % 10 == Math.abs(worldPosition.hashCode()) % 10` (1 in 10 ticks).
- Block state: `FACING` (default DOWN), `POWERED` (default false).

## Numbers
- Signal strength: 15 (full) or 0; direct signal only on the face where `FACING.getOpposite() == side`, indirect (`getSignal`) is 15 on all sides when powered.
- `canConnectRedstone`: true on all faces except FACING.
- Redstone cable connection: only `FACING.getOpposite()` (`canConnectFrom`); network scan: single face `BlockFace(pos.relative(FACING.getOpposite()), FACING)` — the inventory is read from the block behind the emitter.
- Particle offset jitter: ±0.1 block, redstone dust particle at -0.4375 along the emission axis (observed: `-7.0F / 16.0F`).

## Behavior
1. (server, staggered tick) `updateServer`:
   1. Read current `POWERED` from block state.
   2. If `filter != null`:
      - `topCache.getAccess(level, worldPosition).tracker()` — the `NetworkInventory` cache (03-terminal `inventory-access.md`) is loaded at `pos.relative(FACING.getOpposite())` with direction FACING (in `onLoad`).
      - If the change tracker value changed: recompute `counter = tracker.countItems(filter)`; `currState = lessThan ? counter < count : counter > count`.
      - If unchanged: return (no state update).
   3. Else `currState = false`.
   4. If `currState != POWERED`: set state (flag 3), then `Platform.notifyBlocks` + `updateNeighborsAt` at `pos.relative(FACING)` and `pos.relative(FACING).relative(FACING)` with the experimental redstone `Orientation` (01-core `Platform`).
2. Filter/count setters reset `changeTracker = 0` to force re-evaluation on next tick.
3. `stillValid(player)`: BE identity + `distanceToSqr < 64.0`.

## Determinism
Deterministic given the network's change tracker.

## Data formats
NBT (BE): `Filter` — optional item stack (the filter, stored as the actual stack), `Count` — int (load default **0**, runtime field default **1** — see STOR-07), `lessThan` — boolean (default false).

## Output
Redstone signal (15/0) on the back-facing side; block state `POWERED` drives the model.

## Relationships
- 03-terminal: `LevelEmitterMenu`/`LevelEmitterScreen` (GUI), `StoredItemStack` (filter representation), `NetworkInventory` (access cache).
- 01-core: `Platform.notifyBlocks`, `ExperimentalRedstoneUtils` orientation (NeoForge experimental redstone), `TickerUtil`, `ClientUtil.tooltip`.

## NeoForge dependencies
`BlockStateProperties.POWERED`, `ExperimentalRedstoneUtils`/`Orientation` (NeoForge experimental redstone system), `DustParticleOptions.REDSTONE`, codec, `TickerUtil` (platform ticker).

## Bugs
STOR-06 (Math.abs tick gate, `LevelEmitterBlockEntity.java:41`); STOR-07 (NBT Count load-default 0 vs runtime default 1, `LevelEmitterBlockEntity.java:91`).
