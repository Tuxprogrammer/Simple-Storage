# Client UI · coverage and limits

Part of [06-client-ui](index.md).

## Coverage

- Source files read: 13 of 13 in-scope Java files (the files listed in [COVERAGE.md](../COVERAGE.md) row `06-client-ui`: `SH/screen/widget/*` (6), `SH/client/*` (3), `NF/client/BakedPaintedModel.java`, `NF/client/PaintedExtension.java`, `FB/client/UnbakedPaintedModel.java`, `FB/client/BakedPaintedModel.java`), plus `NF/StorageModClient.java` and `FB/StorageModClient.java` (assigned to `01-core/mod-lifecycle.md` but documented in detail here in [client-package.md](client-package.md)), and the screen files cross-read for context: `AbstractStorageTerminalScreen`, `StorageTerminalScreen`, `CraftingTerminalScreen`, `ItemFilterScreen`, `TagItemFilterScreen`, `InventoryConfiguratorScreen`, `InventoryLinkScreen`, `LevelEmitterScreen`, `FilingCabinetScreen`
- Textures / sounds / lang keys covered: all 76 client resource files under `assets/toms_storage/textures/gui/**` + `atlases/gui.json` (7 GUI backgrounds, 45 icon sprites, 23 widget sprite files incl. 8 nine-slice mcmeta, 1 atlas); 0 sounds (none exist); all 217 lang keys (en_us) + locale coverage stats
- Corrections made to earlier statements: the client-resource count corrected to 76 (7 GUI backgrounds, 23 widget sprite files, verified against `git ls-tree -r 26.2`); the field-9 "pending S2" notes were resolved by S2 (2026-11-03, see [API-MAP](../API-MAP.md))

## Known limits

- Icon sprite *depictions* are inferred from their names and the code that uses them (e.g. `control_ae` = AE-style controls icon); the 45-icon contact sheet was generated but image analysis timed out twice, so no pixel-level verification.
- `PlatformContainerScreen`, `IScreen`, `TSContainerScreen` are documented here as screen infrastructure (the base classes the widgets sit on), but are assigned to `03-terminal/container-screens.md`; `RecipeBookButton` is documented here as a widget but assigned to `03-terminal/crafting-terminal.md`.
- The NF client entry point's exact event ordering (mod bus vs game bus) follows the code; the game-bus registration (`NeoForge.EVENT_BUS.register(StorageModClient.class)`) happens in `commonInit` (NF `StorageMod`), not in the client class.
- `FilingCabinetScreen` (assigned to `03-terminal/filing-cabinet-gui.md`) has a wheel-scroll divide-by-zero edge case (`int i = (size + 8) / 9 - 5` is 0 for ≤45 items, `FilingCabinetScreen.java:93`) — noted here for the `03` sheet owner.
- `AbstractStorageTerminalScreen` search/token behavior (assigned to `03-terminal`) was cross-read; observations are listed in `05-integration/index.md` Known limits.
