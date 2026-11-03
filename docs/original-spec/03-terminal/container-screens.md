# Container screen base (TSContainerScreen, IScreen)

| # | Field | Contents |
|---|---|---|
| 1 | **Identity** | `TSContainerScreen` (SH `screen/TSContainerScreen.java`), `IScreen` (SH `screen/IScreen.java`). |
| 2 | **Inputs** | A menu (`AbstractContainerMenu` subclass), player inventory, title component; optional image size (default from the platform screen). Mouse/keyboard events; the recipe-book exclusion-area query. |
| 3 | **Numbers** | No fixed geometry of its own; subclasses supply the texture size (e.g. 176×114+rows×18 for the filing cabinet, 194×202/256 for terminals). |
| 4 | **Behavior** | See Behavior detail. |
| 5 | **Determinism** | None. |
| 6 | **Data formats** | `IScreen.getExclusionAreas(Consumer<Box>)` — `Box(x, y, width, height)` record in screen coordinates; these rectangles are reported to the recipe-book logic as "do not draw the book behind these". |
| 7 | **Output** | Rendered GUI; forwarded input events. |
| 8 | **Relationships** | Base class for: `FilingCabinetScreen`, `InventoryConfiguratorScreen`, `InventoryLinkScreen`, `ItemFilterScreen` (via `AbstractFilteredScreen`), `LevelEmitterScreen`, `TagItemFilterScreen` (03-terminal). The terminal screens extend the platform `PlatformContainerScreen` directly (01-core `platform-layer.md`) rather than `TSContainerScreen` (observed). |
| 9 | **NeoForge dependencies** | Extends the platform `PlatformContainerScreen` (01-core `platform-layer.md`), which extends the vanilla `AbstractContainerScreen`. `mouseScrolled(double, double, double, double)` — NeoForge's 4-arg signature (1.7.10: `mouseScrolled(double, double, float)`). |
| 10 | **Bugs** | None suspected. |

## Behavior detail

1. `TSContainerScreen`: thin subclass of the platform container screen; forwards `mouseScrolled` (4-arg) to the vanilla 3-arg path (drops the horizontal delta) so subclasses can override either. Constructor variants pass image width/height through.
2. `IScreen`: single method `getExclusionAreas(Consumer<Box>)` + the `Box` record; implemented by every mod screen (terminal: button column; configurator: button column; item filter: 40 px column; level emitter: 25 px column; tag filter: 25 px column; filing cabinet and link: none).
3. Screens that need button columns exclude that region so the recipe book (crafting terminal) doesn't render behind/under the buttons.

## Deviations in the new mod

None yet.
