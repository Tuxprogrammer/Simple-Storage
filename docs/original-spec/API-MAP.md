# API map

Each NeoForge/Fabric API upstream relies on and its 1.7.10 counterpart, or "none". Filled in as S1 writes each area; the 1.7.10 answer is designed in the new spec, not here. Harvested from the `NeoForge dependencies` fields of the committed sheets (areas `05-integration` and `06-client-ui` at write time; `01`–`04` pending S1 parts 1–2) plus the cross-loader mechanisms named in this spec.

## Recipe viewers and info mods (area `05-integration`)

| API | Used for | 1.7.10 counterpart |
|---|---|---|
| JEI `IModPlugin`/`@JeiPlugin` (JEI 30.7.0.41) | plugin discovery | JEI 1.7.10 (4.x) — different API version, pending S2 for the exact 1.7.10 classes |
| JEI `IGuiContainerHandler` (clickable areas, exclusion areas) | recipe buttons on TSS screens | JEI 1.7.10 `IGuiHandler` family (pending S2) |
| JEI `IGhostIngredientHandler` | ghost-drag filters into filter GUIs | JEI 1.7.10 ghost-handling (pending S2) |
| JEI `IRecipeTransferHandler` + `IRecipeTransferError` | auto-fill from the recipe GUI | JEI 1.7.10 recipe transfer (pending S2) |
| REI `REIClientPlugin` / `REIPluginLoaderClient` (NF) / `rei_client` entrypoint (FB) (REI 26.2.820) | plugin discovery | **none** in 1.7.10 (REI post-dates it); likely dropped in the port |
| REI `SimpleGridMenuDisplay`/`CategoryIdentifier`/`TransferHandler`/`DraggableStackVisitor` | auto-fill + highlighting, ghost drag | **none** in 1.7.10 |
| EMI `EmiPlugin`/`@EmiEntrypoint`/`StandardRecipeHandler`/`EmiDragDropHandler` (EMI 1.1.7+1.21 / 1.1.13+1.21.1) | auto-craft + ghost drag | **none** in 1.7.10 |
| Jade `IWailaPlugin`/`IServerDataProvider`/`IBlockComponentProvider` (Jade 8326243 / 26.2.0+fabric) | block info tooltips | WAILA (1.7.10) or the GTNH-pack info mod — pending S2 |
| TOP `IProbeInfoProvider`/`ITheOneProbe` (curse build 8887302) | probe readouts | TOP 1.7.10 (mcjty) `IProbeInfoProvider` — pending S2 for exact class |
| Polymorph `PolymorphApi`/`PolymorphWidgets`/`PlayerRecipesWidget` | player recipe state + terminal widget | **none** known for 1.7.10 (widget is dead code upstream, `INT-08`) |
| Sophisticated Storage `StoragePositionGroups` | double/triple storage detection | Sophisticated Storage 1.7.10 (pending S2); dead on Fabric upstream (`INT-09`) |
| NeoForge `InterModComms` (IMC to `theoneprobe`) | TOP wiring | `FMLInterModComms` (direct counterpart) |

## Client rendering and GUI (area `06-client-ui`)

| API | Used for | 1.7.10 counterpart |
|---|---|---|
| `WidgetSprites` + `blitSprite(RenderPipelines.GUI_TEXTURED, …)` + nine-slice mcmeta (`gui.scaling`) | icon buttons, list rows, scroll thumb | **no direct counterpart** — 1.7.10: `GuiDrawHelper.blit`/`SpriteCollection` + custom nine-slice scaling (pending S2) |
| `RecipeBookComponent`/`GhostSlots`/`PlaceRecipeHelper` | crafting terminal recipe book | vanilla 1.7.10 crafting book (same lineage; exact 1.7.10 hooks pending S2) |
| `RenderPipeline` + `RegisterRenderPipelinesEvent` + named pipeline location `toms_storage:pipeline/lines` | no-depth line pipeline | **no direct 1.7.10 counterpart** for named pipeline locations — 1.7.10: `RenderType.create` (Forge) |
| `IrisPipelines.assignPipeline(…, ShaderKey.LINES)` | Iris shader override of the line pipeline | Iris 1.7.10 exists (pending S2 for the assignment API) |
| `SubmitCustomGeometryEvent`/`SubmitNodeCollector` (NF) / `LevelRenderEvents.END_MAIN` (FB) | world-space outlines (terminal, configurator) | Forge `RenderWorldLastEvent` (1.7.10) |
| `RegisterGuiLayersEvent` (NF) / `HudElementRegistry`-style overlay (FB) | configurator HUD overlay | Forge `RenderGameOverlayEvent` (1.7.10) |
| `RegisterMenuScreensEvent` (NF) / `MenuScreens.register` (FB) | the 8 menu screens | 1.7.10: container/screen classes + `GuiContainer.register` in pre-init (naming only) |
| `RegisterKeyMappingsEvent` (NF) / `KeyMappingHelper` (FB) | `B` keybind open-terminal | 1.7.10: `KeyBinding` + client-tick `isPressed` |
| `ItemTooltipEvent` (NF) / `ItemTooltipCallback` (FB) | tooltip extension | 1.7.10: `Item.addTooltip` |
| `ModelEvent.ModifyBakingResult` (NF) / `ModelLoadingPlugin.modifyBlockModelOnLoad` (FB) | painted-block model wrapping | 1.7.10: `ModelBakeEvent` / model bakery hooks (pending S2) |
| `DynamicBlockStateModel`/`BlockStateModelSet` (NF) / `BlockStateModel.UnbakedRoot`/`emitQuads` (FB) | per-state model swap to the facade state | **no direct 1.7.10 counterpart** at this granularity — 1.7.10: custom block renderer / tile-entity-driven rendering (pending S2) |
| `IClientBlockExtensions.collectDynamicTintValues` (NF) / `BlockColorRegistry` (FB) | facade-state tints | 1.7.10: `IBlockColor`/`BlockColorMap` registration |
| `ModelData` (`FACADE_STATE`) | NF facade state transport | 1.7.10: tile-entity-driven rendering (the FB path reads the tile entity directly — closer to the 1.7.10 model) |
| `ModMenuApi` (FB) | config screen | **none** in 1.7.10 (config screens registered directly) |
| `IConfigScreenFactory`/Cloth (NF) | config screen | 1.7.10: direct config-screen registration (pending S2) |
| `ClientLoginNetworking` (FB, `toms_storage:config` JSON sync) | server→client config sync | 1.7.10: login packet channel / capability handshake (pending S2) |
| `GLFW` mouse-button polling (`ListWidget.preRender`) | scrollbar dragging | same (LWJGL/GLFW present in 1.7.10) |
| `ViveCraft` `ClientDataHolderVR`/`VRState` | VR raytrace for the terminal outline | ViveCraft 1.7.10 (pending S2) |

## Events and platform (areas `05`/`06` + shared)

| API | Used for | 1.7.10 counterpart |
|---|---|---|
| `BreakBlockEvent` / `LeftClickBlock` (NF `ServerEvents`) | filter drop on break; left-click item interface | Forge `BlockEvent.BreakEvent` / `PlayerInteractEvent.LeftClickBlock` |
| `ItemStack.CODEC` + `TagValueInput` (Jade/TOP filter payloads) | filter ItemStack in NBT | NBT on `ItemStack` (data components → NBT) |
| `Component.translatable`/`I18n` | lang lookup | `I18n.translate` (same lineage) |
| `RenderPipelines.GUI_TEXTURED` + `GuiGraphicsExtractor` | GUI drawing | 1.7.10: `Gui`/`GuiDrawHelper` (naming only) |

## Pending from areas `01`–`04` (sheets not committed at write time)

| API family | Used for | 1.7.10 counterpart |
|---|---|---|
| Deferred registration (`GameObject`), config (`Config`, AutoConfig), the mod network API (`DataPacket`) | `01-core` | pre-init registration; Forge config; `PacketSimple`/`PacketCustom` channels — pending S1 part 1 to confirm the exact API surface |
| Item handlers/capabilities (inventory access classes) | `01-core`/`02-storage` | Forge capability system (`ICapabilityProvider`) |
| Block entities (all `*BlockEntity`) | `02`/`03`/`04` | tile entities |
| Data-driven recipes | all | **GT recipe maps** (target, per S2) |
| Menu/screen classes (`*Menu`/`*Screen`) | `02`/`03`/`04` | container/screen (naming only) |
