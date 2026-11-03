# System: Painted-block facade rendering

| # | Field | Value |
|---|---|---|
| 1 | **Identity** | NeoForge: `com.tom.storagemod.client.BakedPaintedModel` (`NF/client/BakedPaintedModel.java`) — `DynamicBlockStateModel` wrapper; `com.tom.storagemod.client.PaintedExtension` (`NF/client/PaintedExtension.java`) — `IClientBlockExtensions` dynamic tint values. Fabric: `com.tom.storagemod.client.UnbakedPaintedModel` (`FB/client/UnbakedPaintedModel.java`) — `BlockStateModel.UnbakedRoot` wrapper; `com.tom.storagemod.client.BakedPaintedModel` (`FB/client/BakedPaintedModel.java`) — Fabric `BlockStateModel` that delegates quads to the facade state |
| 2 | **Inputs** | The facade state: NF reads `level.getModelData(pos).get(PaintedBlockEntity.FACADE_STATE)` (model data set by the block entity renderer, a `Supplier<BlockState>`); FB reads `PaintedBlockEntity.getPaintedBlockState()` directly from the block entity (with try/catch fallback to the parent model) |
| 3 | **Numbers** | The 4 painted blocks on both loaders: `paintedTrim` (inventory trim), `invCableFramed` (framed cable), `invProxy`, `invCableConnectorFramed`. All possible block states of each block get the wrapper model (NF: at `ModelEvent.ModifyBakingResult`, per state; FB: at model load, per state). Fallback: facade state null or `AIR` → the painted block's own model (parent) |
| 4 | **Behavior** | See Behavior detail |
| 5 | **Determinism** | All client-side, per frame per visible block. NF `collectParts`/`materialFlags` and FB `emitQuads` look up the facade state's model in the model manager on every call (no caching beyond the model manager's own) |
| 6 | **Data formats** | `PaintedBlockEntity.FACADE_STATE` model-data key (NF): a `Supplier<BlockState>` holding the painted-over block state (the actual block whose appearance is shown through the "painted" shell). No NBT on the wire (the facade state comes from the block entity, synced normally, area `02-storage`) |
| 7 | **Output** | (client) painted trim / framed cable / proxy / framed connector blocks render with the **facade block's** model (and, via the tint extension, the facade's tint) instead of their own — the "painted over another block" look |
| 8 | **Relationships** | `PaintedBlockEntity` (area `02-storage`) provides the facade state; `StorageModClient` wires the model replacement (NF `bakeModels`, FB `ModelLoadingPlugin`) and the tint collection (NF `PaintedExtension` via `RegisterClientExtensionsEvent`, FB `BlockColorRegistry`) — see [client-package.md](client-package.md). The block states/models themselves are area `02-storage` |
| 9 | **NeoForge dependencies** | NF: `DynamicBlockStateModel` + `BlockStateModelSet` lookup (1.7.10: custom `IModel`/`IModelBakery` handling or a custom block renderer, pending S2 — **no direct 1.7.10 counterpart** for per-state model swapping at this granularity); `IClientBlockExtensions.collectDynamicTintValues` (1.7.10: `IBlockColor`/`BlockColorMap` registration, pending S2); `ModelData` (1.7.10: tile-entity-driven rendering). FB: `BlockStateModel.UnbakedRoot`, `ModelLoadingPlugin.modifyBlockModelOnLoad`, `BlockColorRegistry` (1.7.10: tint registration) |
| 10 | **Bugs** | None suspected |

## Behavior detail

All steps are **(client)**.

### NeoForge

1. **Model replacement** (`StorageModClient.bakeModels`): for each of the 4 painted blocks, for every possible `BlockState`, the baking result's `blockStateModels` map is replaced with `new BakedPaintedModel(block, originalModel)`.
2. **`BakedPaintedModel.collectParts`**: resolve the facade state from model data; if null/AIR → delegate to the parent model with the painted block's own state; else look up the facade state's model in the model manager; if that model is itself a `BakedPaintedModel` (a painted block painted over a painted block) → skip (no parts, avoids infinite recursion); else `model.collectParts(level, pos, facadeState, random, parts)`.
3. **`BakedPaintedModel.materialFlags`**: same resolution; the recursive case returns the parent's flags.
4. **`PaintedExtension.collectDynamicTintValues`**: resolve the facade state; if null/AIR → no tints; else query `getBlockColors().getTintSources(facadeState)` and append each source's `colorInWorld(facadeState, level, pos)` — so leaves/grass/etc. painted facades keep their world tints.

### Fabric

1. **Model replacement** (`ModelLoadingPlugin`): `modifyBlockModelOnLoad` wraps the load result for the 4 blocks' states in `UnbakedPaintedModel` (only if not already wrapped); `UnbakedPaintedModel.bake` → `new BakedPaintedModel(parent.bake(state, baker))`.
2. **`BakedPaintedModel.emitQuads`**: if the block entity at the position is a `PaintedBlockEntity` (in a try/catch; on exception fall through to the parent), resolve `getPaintedBlockState()`; null/AIR → parent model with own state; else the facade state's model from the model manager; if the facade model is not a `BakedPaintedModel`, emit its quads with the facade state (else the parent quads, avoiding recursion).
3. **Tints** (`BlockColorRegistry`, registered in `StorageModClient`): same logic as the NF `PaintedExtension` — the facade state's tint sources, `colorInWorld` per source.

## Deviations in the new mod

None (original spec only).
