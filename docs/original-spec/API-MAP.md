# API map

Each NeoForge/Fabric API upstream relies on and its 1.7.10 counterpart, or "none". Filled in as S1 wrote each area; the 1.7.10 answer is designed in the new spec, not here. Harvested from the `NeoForge dependencies` fields of the committed sheets (areas `05-integration` and `06-client-ui` at write time; `01`–`04` in S1 parts 1–2) plus the cross-loader mechanisms named in this spec. All "pending S2" rows resolved by spike S2 (2026-11-03, verified against source at the pack's tags).

## Recipe viewers and info mods (area `05-integration`)

| API | Used for | 1.7.10 counterpart |
|---|---|---|
| JEI `IModPlugin`/`@JeiPlugin` (JEI 30.7.0.41) | plugin discovery | NEI 2.8.171-GTNH `codechicken.nei.api.IConfigureNEI` — a class `NEI…Config` auto-discovered by name scan (NEI ClientHandler.java#L317-L346; S2) |
| JEI `IGuiContainerHandler` (clickable areas, exclusion areas) | recipe buttons on TSS screens | NEI `API.registerGuiOverlay`/`registerGuiOverlayHandler` + `codechicken.nei.api.INEIGuiHandler` (NEI API.java#L80-L138; AE2 `NEIGuiHandler` precedent; S2) |
| JEI `IGhostIngredientHandler` | ghost-drag filters into filter GUIs | **no direct counterpart** — `INEIGuiHandler#handleDragNDrop`; ghost drag is a beta candidate (D11), alpha uses direct click/drag (S2) |
| JEI `IRecipeTransferHandler` + `IRecipeTransferError` | auto-fill from the recipe GUI | `codechicken.nei.FastTransferManager` + `INEIGuiHandler` (no dedicated 1.7.10 transfer-handler interface; fill implemented inside the gui handler, S2) |
| REI `REIClientPlugin` / `REIPluginLoaderClient` (NF) / `rei_client` entrypoint (FB) (REI 26.2.820) | plugin discovery | **none** in 1.7.10 (REI post-dates it); **dropped in the port** (no REI in the pack, S3/S2) |
| REI `SimpleGridMenuDisplay`/`CategoryIdentifier`/`TransferHandler`/`DraggableStackVisitor` | auto-fill + highlighting, ghost drag | **none** in 1.7.10 |
| EMI `EmiPlugin`/`@EmiEntrypoint`/`StandardRecipeHandler`/`EmiDragDropHandler` (EMI 1.1.7+1.21 / 1.1.13+1.21.1) | auto-craft + ghost drag | **none** in 1.7.10 |
| Jade `IWailaPlugin`/`IServerDataProvider`/`IBlockComponentProvider` (Jade 8326243 / 26.2.0+fabric) | block info tooltips | WAILA 1.7.10 (GTNH 1.19.34): `mcp.mobius.waila.api.IWailaBlock` + `IWailaRegistrar` via IMC `FMLInterModComms.sendMessage("Waila", "register", …)`; optionally IIGX 2.9.8 pages (S3-verified; S2 mapping in 00-pack-integration recipe-viewer.md) |
| TOP `IProbeInfoProvider`/`ITheOneProbe` (curse build 8887302) | probe readouts | **none** — no TOP in the GTNH 2.9.0 pack (S3 MOD-INDEX); Waila covers the readout need (S2) |
| Polymorph `PolymorphApi`/`PolymorphWidgets`/`PlayerRecipesWidget` | player recipe state + terminal widget | **none** known for 1.7.10 (widget is dead code upstream, `INT-08`) |
| Sophisticated Storage `StoragePositionGroups` | double/triple storage detection | **none** in the pack (no Sophisticated Storage in S3 MOD-INDEX); dead on Fabric upstream (`INT-09`) anyway (S2) |
| NeoForge `InterModComms` (IMC to `theoneprobe`) | TOP wiring | `FMLInterModComms` (direct counterpart) |

## Client rendering and GUI (area `06-client-ui`)

| API | Used for | 1.7.10 counterpart |
|---|---|---|
| `WidgetSprites` + `blitSprite(RenderPipelines.GUI_TEXTURED, …)` + nine-slice mcmeta (`gui.scaling`) | icon buttons, list rows, scroll thumb | **no direct counterpart** — 1.7.10: `Gui.drawModalRectWithCustomSize` (nine-slice via nine blits) + `TextureManager` binding; no `SpriteCollection` (1.9+) (inferred from 1.7.10 API, S2) |
| `RecipeBookComponent`/`GhostSlots`/`PlaceRecipeHelper` | crafting terminal recipe book | **no counterpart** — vanilla 1.7.10 has no recipe book (added 1.14); the terminal's book is a mod feature (S2) |
| `RenderPipeline` + `RegisterRenderPipelinesEvent` + named pipeline location `toms_storage:pipeline/lines` | no-depth line pipeline | **no direct 1.7.10 counterpart** for named pipeline locations — 1.7.10: `RenderType.create` (Forge) |
| `IrisPipelines.assignPipeline(…, ShaderKey.LINES)` | Iris shader override of the line pipeline | the pack's 1.7.10 shader mod is **Angelica 2.2.43 (Embeddium)**, not Iris (S3 MOD-INDEX); pipeline-assignment API unverified — treat as optional (inferred, S2) |
| `SubmitCustomGeometryEvent`/`SubmitNodeCollector` (NF) / `LevelRenderEvents.END_MAIN` (FB) | world-space outlines (terminal, configurator) | Forge `RenderWorldLastEvent` (1.7.10) |
| `RegisterGuiLayersEvent` (NF) / `HudElementRegistry`-style overlay (FB) | configurator HUD overlay | Forge `RenderGameOverlayEvent` (1.7.10) |
| `RegisterMenuScreensEvent` (NF) / `MenuScreens.register` (FB) | the 8 menu screens | 1.7.10: container/screen classes + `GuiContainer.register` in pre-init (naming only) |
| `RegisterKeyMappingsEvent` (NF) / `KeyMappingHelper` (FB) | `B` keybind open-terminal | 1.7.10: `KeyBinding` + client-tick `isPressed` |
| `ItemTooltipEvent` (NF) / `ItemTooltipCallback` (FB) | tooltip extension | 1.7.10: `Item.addTooltip` |
| `ModelEvent.ModifyBakingResult` (NF) / `ModelLoadingPlugin.modifyBlockModelOnLoad` (FB) | painted-block model wrapping | **no baking event in 1.7.10** (model baking is 1.13+); painted blocks use a custom `IBlockRenderer`/tile-entity-driven rendering (inferred, S2) |
| `DynamicBlockStateModel`/`BlockStateModelSet` (NF) / `BlockStateModel.UnbakedRoot`/`emitQuads` (FB) | per-state model swap to the facade state | **no direct 1.7.10 counterpart** — 1.7.10: custom block renderer / tile-entity-driven rendering reading the facade state from the TE (the FB path reads the tile entity directly — closer to the 1.7.10 model) (S2) |
| `IClientBlockExtensions.collectDynamicTintValues` (NF) / `BlockColorRegistry` (FB) | facade-state tints | 1.7.10: `IBlockColor`/`BlockColorMap` registration |
| `ModelData` (`FACADE_STATE`) | NF facade state transport | 1.7.10: tile-entity-driven rendering (the FB path reads the tile entity directly — closer to the 1.7.10 model) |
| `ModMenuApi` (FB) | config screen | **none** in 1.7.10 (config screens registered directly) |
| `IConfigScreenFactory`/Cloth (NF) | config screen | GTNHLib 0.11.54 `SimpleGuiFactory`/automatic config screen for `@Config` classes (S3; S2) |
| `ClientLoginNetworking` (FB, `toms_storage:config` JSON sync) | server→client config sync | 1.7.10: per-world config sync via GTNHLib `@Config.Sync`, or a custom `PacketSimple` login handshake (D16, S2) |
| `GLFW` mouse-button polling (`ListWidget.preRender`) | scrollbar dragging | same (LWJGL/GLFW present in 1.7.10) |
| `ViveCraft` `ClientDataHolderVR`/`VRState` | VR raytrace for the terminal outline | **none** — no ViveCraft in the GTNH 2.9.0 pack (S3 MOD-INDEX); VR out of scope (S2) |

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
| Deferred registration (`GameObject`), config (`Config`, AutoConfig), the mod network API (`DataPacket`) | `01-core` | pre-init registration (`GameRegistry` + GTNHLib blockstate registry for property blocks); config via GTNHLib `@Config` (single file, D16); `PacketSimple`/`PacketCustom` channels (S2) |
| Item handlers/capabilities (inventory access classes) | `01-core`/`02-storage` | Forge capability system (`ICapabilityProvider`/`hasCapability`); inventory access resolves `ISidedInventory` → `IInventory` → AE2 (optional) (S2, D10 — 00-pack-integration machine-model.md) |
| Block entities (all `*BlockEntity`) | `02`/`03`/`04` | tile entities (ordinary 1.7.10 TEs per D9, not GT MetaTileEntities) (S2) |
| Data-driven recipes | all | **GT recipe maps** — `gt.recipe.assembler` via `GTRecipeBuilder#addTo` (S2 resolved, D8 — 00-pack-integration recipes-and-materials.md) |
| Menu/screen classes (`*Menu`/`*Screen`) | `02`/`03`/`04` | container/screen (naming only) |
