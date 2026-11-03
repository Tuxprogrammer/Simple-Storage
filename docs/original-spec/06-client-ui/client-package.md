# System: Client package and client entry points

| # | Field | Value |
|---|---|---|
| 1 | **Identity** | `com.tom.storagemod.client.ClientUtil` (`SH/client/ClientUtil.java`) — world-space outlines, painted highlights, tooltip extension, HUD overlay; `com.tom.storagemod.client.CustomRenderTypes` (`SH/client/CustomRenderTypes.java`) — the no-depth line render pipeline; `com.tom.storagemod.client.ViveCraftHelper` (`SH/client/ViveCraftHelper.java`) — ViveCraft VR raytrace; `com.tom.storagemod.StorageModClient` (`NF/StorageModClient.java`, NeoForge client entry point) and `com.tom.storagemod.StorageModClient` (`FB/StorageModClient.java`, Fabric `ClientModInitializer`) |
| 2 | **Inputs** | Per-frame render events (NF `SubmitCustomGeometryEvent`, FB `LevelRenderEvents.END_MAIN`), the player's held items (wireless terminal, inventory configurator with its `ConfiguratorComponent`), the hit result, `Config` values (`wirelessRange`), `StorageTags.REMOTE_ACTIVATE`, `PaintedBlockEntity` facade state, Iris shader pipeline (`ShaderKey.LINES`) |
| 3 | **Numbers** | Terminal outline: raytrace max distance = `Config.get().wirelessRange` blocks (fluids included), line width 2 px, color white (1, 1, 1), alpha 0.4. Configurator outline: selected blocks drawn as unit line boxes, white (1, 1, 1) alpha 0.4; mass-selection box in blue (1, 1, 0) alpha 0.4 (no-depth pipeline); bound block in red (1, 0, 0) alpha 1. Painted highlights: scan region = player position inflated by 7 blocks (15×15×15 = up to 3375 block states per frame), hard cutoff at `dist² > 49` (7 blocks), alpha 1.0 within `dist² ≤ 25` (5 blocks) fading linearly to 0.0 at `dist² = 49` (`a = 1 − (dist−25)/24`). Line pipeline: built from `RenderPipelines.LINES_SNIPPET` with depth compare `ALWAYS_PASS` and no depth write, registered at location `toms_storage:pipeline/lines`; `LINES_NO_DEPTH` render type uses `VIEW_OFFSET_Z_LAYERING` and the item-entity output target. Keybind: `key.toms_storage.open_terminal`, key `B`, category GAMEPLAY (both loaders) |
| 4 | **Behavior** | See Behavior detail |
| 5 | **Determinism** | All client-side, per frame (render pass). `drawPaintedHighlights` does a full 15×15×15 level scan every frame while holding the configurator not in selecting/box mode — O(3375) block-state lookups/frame (performance note). Tooltip extension is per-item, per-render. Keybind click is consumed once per client tick |
| 6 | **Data formats** | FB config sync: login-network packet `toms_storage:config` carrying the full `Config` as JSON (Gson) in a `ByteBufInputStream`; on parse failure the client logs `"Error loading server config"` and keeps its defaults; on success logs `"Received server config"`. No other formats; outlines read block entities/components in place (no NBT wire) |
| 7 | **Output** | (client) white outline around the looked-at `remote_activate` block when holding a wireless terminal; configurator HUD: selected-block outlines, blue mass-selection box, red bound block, painted-block edge highlights in their paint colors; the configurator info overlay (HUD layer above all); the `B` keybind opens the storage terminal; extended item tooltips (`ClientUtil.tooltipExt` — currently a static, empty by default) |
| 8 | **Relationships** | `WirelessTerminalItem.isPlayerHolding` (area `04-wireless`), `NetworkHandler.openTerminal()` (area `01-core`), `StorageTags.REMOTE_ACTIVATE` (area `01-core`), `Config` (area `01-core`), `ConfiguratorComponent` (area `02-storage`), `PaintedBlockEntity` (area `02-storage`), `IConfiguratorHighlight` (area `02-storage`), `Platform.vivecraft` (area `01-core`), the 8 menu screens (areas `02`/`03`), `PaintedExtension`/`BakedPaintedModel` (see [painted-models.md](painted-models.md)) |
| 9 | **NeoForge dependencies** | NF: `SubmitCustomGeometryEvent` + `SubmitNodeCollector` (1.7.10: `RenderWorldLastEvent`, S2: API-MAP), `RegisterRenderPipelinesEvent` + custom `RenderPipeline` at a mod location (1.7.10: `RenderType.create` + Iris support, S2: API-MAP; the named pipeline location has **no direct 1.7.10 counterpart**), `IrisPipelines.assignPipeline(..., ShaderKey.LINES)` (Iris 1.7.10 exists, S2: API-MAP), `RegisterGuiLayersEvent` (1.7.10: `RenderGameOverlayEvent` layer, S2: API-MAP), `RegisterMenuScreensEvent` (1.7.10: `GuiContainer.register` in pre-init), `RegisterKeyMappingsEvent` (1.7.10: `KeyBinding` + client tick), `ItemTooltipEvent` (1.7.10: `Item.addTooltip`), `ModelEvent.ModifyBakingResult` (1.7.10: `ModelBakeEvent`), `IConfigScreenFactory`/Cloth (1.7.10: direct config-screen registration, S2: API-MAP). FB: `MenuScreens.register`, `ModelLoadingPlugin.modifyBlockModelOnLoad`, `BlockColorRegistry` (1.7.10: block tint registration), `LevelRenderEvents.END_MAIN`, `ClientLoginNetworking` (1.7.10: login packet channel), `ItemTooltipCallback`, `KeyMappingHelper` |
| 10 | **Bugs** | None in-scope beyond the widget/lang rows in [index](index.md) (the 15×15×15 per-frame scan is a performance characteristic, not a suspected bug) |

## Behavior detail

All steps are **(client)**.

### `ClientUtil`

1. **Tooltip extension**: `tooltip(key, consumer, args)` / `tooltip(key, addShift, consumer, args)` build translatable components (the `addShift` variant appends the hold-shift hint) and push them into `setTooltipExt`; `collectExtraTooltips(stack, list)` appends the stored components to an item's tooltip list (wired to `ItemTooltipEvent` on NF / `ItemTooltipCallback` on FB). `multilineTooltip(text, objects)` splits a translated string on literal backslash characters and joins the parts as separate literal lines.
2. **`drawTerminalOutline`**: returns unless the player holds a wireless terminal (main or off hand, via `WirelessTerminalItem.isPlayerHolding`); raytraces up to `Config.get().wirelessRange` blocks (fluids hit; VR raytrace first when `Platform.vivecraft`); if the hit block state's block is in `StorageTags.REMOTE_ACTIVATE`, submits line geometry for the block's **occlusion shape** (not bounding box) in white alpha 0.4, line width 2.
3. **`drawConfiguratorOutline`**: returns unless the main (then off) hand holds the inventory configurator; reads its `ConfiguratorComponent`:
   - If the component is **not** selecting and not showing the inventory box → `drawPaintedHighlights` (step 4) and return.
   - Otherwise, for every position in `component.selection()`: a unit-cube line box, white alpha 0.4.
   - If `massSelect()` and the current hit result is a block: an `AABB.encapsulatingFullBlocks(boxStart, hitPos)` line box in blue (1, 1, 0) alpha 0.4, rendered with the no-depth pipeline.
   - If the component is bound: the bound block's unit line box in red (1, 0, 0) alpha 1, no-depth.
4. **`drawPaintedHighlights`**: streams `BlockPos.betweenClosedStream(AABB(player.pos).inflate(7))` (15×15×15); skips positions with `distanceToSqr > 49`; for each `IConfiguratorHighlight` block state, collects its highlight `VoxelShape` + `getHighlightColor()`; alpha = 1.0 while `dist² ≤ 25`, else `1 − (dist²−25)/24`; draws each shape's edges as lines in the paint color.
5. **`drawShape`**: per voxel-shape edge, two vertices with normalized edge direction as normal, color + alpha, line width 2.
6. **`renderLineBox`** (three overloads): emits the 12 edges of a box (from two corners or an `AABB`).
7. **`drawConfiguratorOverlay`**: the HUD overlay (registered as the GUI layer above all) — draws the configurator's current mode/selection info (see the `overlay.toms_storage.configurator_info` lang key).

### `CustomRenderTypes`

1. `LINES` (registered pipeline): `RenderPipeline.builder(RenderPipelines.LINES_SNIPPET)` + `DepthStencilState(CompareOp.ALWAYS_PASS, false)` (always passes depth, no depth write) at location `toms_storage:pipeline/lines`; registered through `StorageModClient.registerPipeline`, which also assigns it to Iris `ShaderKey.LINES` when Iris is loaded (so shaders can override it).
2. `LINES_NO_DEPTH`: a `RenderType` over `LINES` with `LayeringTransform.VIEW_OFFSET_Z_LAYERING` and `OutputTarget.ITEM_ENTITY_TARGET`; used for the configurator's mass-selection and bound-box lines so they draw regardless of world geometry occlusion.

### `ViveCraftHelper`

1. `rayTraceVR(maxDist, hitFluids)`: when `VRState.VR_RUNNING` and the VR player data exists and the player is **not seated**, returns `vrPlayer.rayTraceBlocksVR(worldRender, 0, maxDist, hitFluids)`; else `null` (caller falls back to the normal `player.pick`).

### `StorageModClient` (NeoForge)

1. `preInit(mc, bus)` (mod bus): registers the 8 menu screens (`storageTerminal`, `craftingTerminal`, `levelEmitter`, `inventoryLink`, `itemFilter`, `tagItemFilter`, `invConfig`, `filingCabinet` → their screens); registers `registerPipelines`, `registerOverlays`, `registerClientExtensions`; registers the Cloth config screen factory (`IConfigScreenFactory` → `ConfigurationScreen`).
2. Game bus (registered in common init): `getTooltip` (→ `ClientUtil.collectExtraTooltips`), `renderWorldOutline` (`SubmitCustomGeometryEvent` → `drawTerminalOutline` + `drawConfiguratorOutline`), `clientTick` (→ `openTerm.consumeClick()` → `NetworkHandler.openTerminal()`).
3. `registerScreens`/`initKeybinds` (`RegisterKeyMappingsEvent`): `openTerm = new KeyMapping("key.toms_storage.open_terminal", IN_GAME, KEY_B, Category.GAMEPLAY)`.
4. `bakeModels` (`ModelEvent.ModifyBakingResult`): for `paintedTrim`, `invCableFramed`, `invProxy`, `invCableConnectorFramed`, replaces every possible block state's baked model with `new BakedPaintedModel(block, existingModel)` (see [painted-models.md](painted-models.md)).
5. `registerOverlays` (`RegisterGuiLayersEvent`): the `CONFIGURATOR_OVERLAY_ID` layer above all → `ClientUtil.drawConfiguratorOverlay`.
6. `registerClientExtensions` (`RegisterClientExtensionsEvent`): `PaintedExtension` for the same 4 painted blocks.
7. `registerPipelines` (`RegisterRenderPipelinesEvent`): forces `CustomRenderTypes` class init, then registers the collected pipelines.

### `StorageModClient` (Fabric, `ClientModInitializer`)

1. `onInitializeClient`: `MenuScreens.register` for the same 8 menus; `ClientPlayNetworking` receiver for `DataPacket.ID` (routes to the open screen if it implements `IDataReceiver` — the screen-data sync, area `01-core`/`03-terminal`); `ModelLoadingPlugin` that wraps the 4 painted blocks' models in `UnbakedPaintedModel`; `BlockColorRegistry` for the same 4 blocks: when the block entity is a `PaintedBlockEntity` with a non-air `getPaintedBlockState()`, it queries the facade state's tint sources and adds each `colorInWorld` result; `LevelRenderEvents.END_MAIN` → `drawTerminalOutline` + `drawConfiguratorOutline`; login receiver `toms_storage:config` (see Data formats); `openTerm` keybind via `KeyMappingHelper` (same key `B`); the Polymorph widget registration call (commented out — `INT-08`, area `05-integration`).

## Deviations in the new mod

None (original spec only).
