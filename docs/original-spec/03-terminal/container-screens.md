# Container screen base (TSContainerScreen, IScreen)

| # | Field | Contents |
|---|---|---|
| 1 | **Identity** | `TSContainerScreen` (SH `screen/TSContainerScreen.java`), `IScreen` (SH `screen/IScreen.java`). Sources: `NeoForge/src/platform-shared/java/com/tom/storagemod/screen/` (`TSContainerScreen.java:NN`, `IScreen.java:NN`, and each screen as named). |
| 2 | **Inputs** | A menu (`AbstractContainerMenu` subclass), player inventory, title component; optional image size (default from the platform screen). Mouse/keyboard events; the recipe-book exclusion-area query. |
| 3 | **Numbers** | No fixed geometry of its own; subclasses supply the texture size (e.g. 176×114+rows×18 for the filing cabinet, 194×202/256 for terminals). |
| 4 | **Behavior** | See Behavior detail. |
| 5 | **Determinism** | None. |
| 6 | **Data formats** | `IScreen.getExclusionAreas(Consumer<Box>)` — `Box(x, y, width, height)` record in screen coordinates (IScreen.java:8); these rectangles are reported to the recipe-book logic as "do not draw the book behind these". |
| 7 | **Output** | Rendered GUI; forwarded input events. |
| 8 | **Relationships** | Base class for: `FilingCabinetScreen`, `InventoryConfiguratorScreen`, `InventoryLinkScreen`, `ItemFilterScreen` (via `AbstractFilteredScreen`), `LevelEmitterScreen`, `TagItemFilterScreen` (03-terminal). The terminal screens also extend it: `AbstractStorageTerminalScreen` extends `TSContainerScreen` (AbstractStorageTerminalScreen.java:77), so `StorageTerminalScreen` and `CraftingTerminalScreen` inherit it (StorageTerminalScreen.java:9, CraftingTerminalScreen.java:30). The other base-class relations: FilingCabinetScreen.java:18, InventoryConfiguratorScreen.java:24, InventoryLinkScreen.java:40, AbstractFilteredScreen.java:8. `TSContainerScreen` itself extends the platform `PlatformContainerScreen` (01-core `platform-layer.md`) (TSContainerScreen.java:7). |
| 9 | **NeoForge dependencies** | Extends the platform `PlatformContainerScreen` (01-core `platform-layer.md`), which extends the vanilla `AbstractContainerScreen`. `mouseScrolled(double, double, double, double)` — NeoForge's 4-arg signature (1.7.10: `mouseScrolled(double, double, float)`) (TSContainerScreen.java:17-20). |
| 10 | **Bugs** | None suspected. |

## Behavior detail

1. `TSContainerScreen`: thin subclass of the platform container screen; overrides the 4-arg `mouseScrolled` to call `super.mouseScrolled` with all four arguments (the horizontal delta is kept), then, if that returns false, falls back to a private copy of `ContainerEventHandler`'s child dispatch (`getChildAt(...)` then that child's `mouseScrolled`) (TSContainerScreen.java:17-26). Constructor variants pass image width/height through (TSContainerScreen.java:9-15).
2. `IScreen`: single method `getExclusionAreas(Consumer<Box>)` + the `Box` record (IScreen.java:6-8); implemented by every mod screen (terminal: button column, 30 px wide, AbstractStorageTerminalScreen.java:777-779; configurator: button column, InventoryConfiguratorScreen.java:122-124; item filter: 40 px column, ItemFilterScreen.java:64-66; level emitter: 25 px column, LevelEmitterScreen.java:133-135; tag filter: 25 px column, TagItemFilterScreen.java:165-167; filing cabinet and link: none, FilingCabinetScreen.java:100-101, InventoryLinkScreen.java:250-251).
3. Screens that need button columns exclude that region so the recipe book (crafting terminal) doesn't render behind/under the buttons.

## Deviations in the new mod

See the [03-terminal deviation table](../../new-spec/03-terminal/deviations.md#deviation-table) (row `container-screens`).
