# Integration · coverage and limits

Part of [05-integration](index.md).

## Coverage

- Source files read: 21 of 21 in-scope Java files (the files listed in [COVERAGE.md](../COVERAGE.md) row `05-integration`: the three viewer packages `jei`/`rei`/`emi` + `NF/rei/REIPluginLoader`, the `jade` package (8), `SophisticatedDoubleBlocks`, `NF/top/TheOneProbeHandler`, `FB/ModMenu`), plus the `01-core` files `NF/StorageMod.java` and `FB/StorageMod.java` (for the registration call sites), and the build/metadata files: `NeoForge/build.gradle`, `Fabric/build.gradle`, `NeoForge/gradle.properties`, `Fabric/gradle.properties`, `META-INF/neoforge.mods.toml`, `Fabric/.../fabric.mod.json`, `version-check-nf.json`, `version-check.json`. Classes that are *used* here but owned by other areas: `EventHandler`/`MultiblockInventoryAPI`/`PolymorphHelper`/`PolymorphTerminalWidget` (→ `01-core/util-api.md`), `VanillaMultiblockInventories`/`IAutoFillTerminal` (→ `03-terminal/inventory-access.md`, `03-terminal/terminal-utils.md`), `ServerEvents` (→ `01-core/mod-lifecycle.md`) — all were read and are covered from the integration side in [other-compat.md](other-compat.md).
- Textures / sounds / lang keys covered: 0 textures, 0 sounds (none used directly by these classes except shared GUI icons documented in `06-client-ui`); lang keys are listed in [06-client-ui/lang.md](../06-client-ui/lang.md)
- Corrections made to earlier statements: none (first writing); the field-9 "pending S2" notes were resolved by S2 (2026-11-03, see [API-MAP](../API-MAP.md))

## Known limits

- Nothing was run; all behavior is read from source at `95bb61d`. The build-script comment-style analysis (INT-01) is *inferred* from standard Groovy comment semantics, not from a build log.
- The JEI clickable-area coordinates (INT-10) were compared against the menu slot coordinates in `SH/menu/CraftingTerminalMenu.java` (`x = -4, y = 94` origin), read from source; no runtime measurement.
- TOP and Jade behavior depends on those mods' rendering code (e.g. the zero-total progress bar, INT-07), which was not read.
- `ServerEvents` (NF) is assigned to `01-core/mod-lifecycle.md` (it registers in `StorageMod`), but its two handlers (filter drop on break, left-click item interface) are also relevant to `02-storage` and `03-terminal`; the area owners may cross-reference.
- Observations found in files assigned to other areas while reading the viewer code, for the `03-terminal` (part 2) sheet: the terminal search silently drops an unparseable search token (`SH/screen/AbstractStorageTerminalScreen.java:265-275` — `buildPattern` fallback `continue`), and `isTransferOne` for AE mode returns shift+ctrl although the inline comment says "not in AE" (`SH/screen/AbstractStorageTerminalScreen.java:616-619`).
