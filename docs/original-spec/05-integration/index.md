# 05 · Integration

## Overview

Third-party integrations of Tom's Simple Storage Mod `2.11.3` (upstream branch `26.2`, commit `95bb61d`): the recipe-viewer plugins for JEI, REI and EMI (ghost-drag item filters into the filter screens, auto-fill/auto-craft from the recipe GUI, search-field sync), the Jade (Waila) block-info integration, The One Probe, Polymorph, Sophisticated Storage, the mod's own public multiblock-inventory API, and the small compat event handlers. All of these are optional at runtime (except what the NeoForge jar ships inside itself — see Key facts). The NeoForge side is the port baseline; Fabric differences are noted per sheet.

## Contents

- [jei.md](jei.md): the JEI plugin — clickable recipe areas, ghost-ingredient drag targets, the crafting-terminal auto-fill transfer handler, recipe catalyst, search sync
- [rei.md](rei.md): the REI plugin — workstations, focused/hovered stacks, ghost drag, the transfer handler with missing-slot highlighting, search sync
- [emi.md](emi.md): the EMI plugin — workstation, drag-drop, the standard recipe handler (craft from EMI), search sync
- [jade.md](jade.md): the Jade plugin — three block components (inventory connector usage, block-filter configuration, inventory hopper filter) with their server data payloads
- [other-compat.md](other-compat.md): The One Probe handler, Polymorph (helper + terminal widget), Sophisticated Storage double-blocks, the `MultiblockInventoryAPI` and vanilla double-chest detector, the mod's generic `EventHandler`, Fabric ModMenu, and the NeoForge `ServerEvents` compat handlers

## Key facts

- **What is bundled vs optional (NeoForge jar at `95bb61d`):** REI `26.2.820`, Jade (curse.maven build `8326243`), The One Probe (curse.maven build `8887302`) are `implementation` dependencies — shipped inside the NeoForge jar. JEI `30.7.0.41` is compile-only API + `runtimeOnly` full jar (optional, not bundled). EMI `1.1.7+1.21` is only declared when the build runs with `-DuseLib`, and the EMI classes are additionally excluded from compilation (see INT-01) — EMI is effectively dead in the shipped NeoForge build. Polymorph is a dummy-compile stub by default (`curse.maven polymorph-388800:5995380` with `-DuseLib`).
- **Fabric:** JEI and REI are compile-only + runtime-only (optional); Jade (modrinth `26.2.0+fabric`) is `implementation` (bundled); the EMI dependency and the `emi` entrypoint are inconsistent (see INT-01/INT-02); Sophisticated Storage and Polymorph are dead on Fabric (see INT-08/INT-09).
- **Plugin discovery:** JEI (`@JeiPlugin`), REI (NeoForge `@REIPluginLoaderClient` loader class / Fabric `rei_client` entrypoint), EMI (`@EmiEntrypoint` and the `emi` entrypoint), Jade (`@WailaPlugin` and the `jade` entrypoint) are discovered by the viewer mod itself; TOP is wired through NeoForge inter-mod comms (`InterModComms.sendTo("theoneprobe", "getTheOneProbe", ...)`); ModMenu (`ModMenuApi`) and the Polymorph widget registry are wired explicitly.
- **Shared viewer contract:** all three viewers plug into `com.tom.storagemod.util.IAutoFillTerminal` (`SH/util/IAutoFillTerminal.java`, documented here because it is the integration seam): `updateSearch` is a static list of `ISearchHandler` (search-field sync), `sendMessage(CompoundTag)` carries the auto-fill request (`fill` key), `getStoredItems()` feeds the "missing" computation, `isSmartSearch()` gates the preview. The matching semantics (what counts as "missing") are identical in all three handlers: an input is satisfied if any alternative stack is in the player's inventory or in the terminal's stored items.
- **Build-script comment style matters:** `//*if(!useLib)*/exclude …` in the Gradle files is a *line* comment (the exclude never applies); `/*if(!useLib)*/exclude …` is a block comment (the exclude *always* applies). See INT-01.

## Related areas

- `03-terminal`: the viewers operate on the terminal screens/menus (`AbstractFilteredScreen`, `StorageTerminalMenu`, `CraftingTerminalMenu`, `PhantomSlot`, `FilterSlot`) and on the terminal's stored items (`TerminalItemStack`, `StoredItemStack`); the crafting result slot layout the JEI clickable area targets is documented there
- `06-client-ui`: the screens and widgets the viewers attach exclusion areas and ghost targets to; the lang keys the viewers use
- `02-storage`: the block-filter attachment (`BlockFilterAttachment`, `BlockFilter`) whose contents Jade and TOP read; the inventory hopper and inventory connector block entities
- `04-wireless`: the wireless terminal item is what the Jade/TOP hopper/connector info is usually about; the curios/trinkets belt data (wireless terminal in a belt) is that area's
- `01-core`: registration, config, network and the platform layer that hosts `ServerEvents` and the IMC

## Coverage

- Source files read: 28 of 28 in-scope Java files (the files listed in [COVERAGE.md](../COVERAGE.md) row `05-integration`: the three viewer packages `jei`/`rei`/`emi` + `NF/rei/REIPluginLoader`, the `jade` package (8), `polymorph` (2), `api` (2), `SophisticatedDoubleBlocks`, `VanillaMultiblockInventories`, `NF/top/TheOneProbeHandler`, `FB/ModMenu`, `SH/util/IAutoFillTerminal`, `NF/ServerEvents`), plus the `01-core` files `NF/StorageMod.java` and `FB/StorageMod.java` (for the registration call sites), and the build/metadata files: `NeoForge/build.gradle`, `Fabric/build.gradle`, `NeoForge/gradle.properties`, `Fabric/gradle.properties`, `META-INF/neoforge.mods.toml`, `Fabric/.../fabric.mod.json`, `version-check-nf.json`, `version-check.json`
- Textures / sounds / lang keys covered: 0 textures, 0 sounds (none used directly by these classes except shared GUI icons documented in `06-client-ui`); lang keys are listed in [06-client-ui/lang.md](../06-client-ui/lang.md)
- Corrections made to earlier statements: none

## Known limits

- Nothing was run; all behavior is read from source at `95bb61d`. The build-script comment-style analysis (INT-01) is *inferred* from standard Groovy comment semantics, not from a build log.
- The JEI clickable-area coordinates (INT-10) were compared against the menu slot coordinates in `SH/menu/CraftingTerminalMenu.java` (`x = -4, y = 94` origin), read from source; no runtime measurement.
- TOP and Jade behavior depends on those mods' rendering code (e.g. the zero-total progress bar, INT-07), which was not read.
- `ServerEvents` (NF) is assigned here as a compat surface, but its two handlers (filter drop on break, left-click item interface) are also relevant to `02-storage` and `03-terminal`; the area owners may cross-reference.
- Observations found in files assigned to other areas while reading the viewer code, for the `03-terminal` (part 2) sheet: the terminal search silently drops an unparseable search token (`SH/screen/AbstractStorageTerminalScreen.java:265-275` — `buildPattern` fallback `continue`), and `isTransferOne` for AE mode returns shift+ctrl although the inline comment says "not in AE" (`SH/screen/AbstractStorageTerminalScreen.java:616-619`).

## Suspected bugs

| ID | Title | Location | What looks wrong | Severity |
|---|---|---|---|---|
| `INT-01` | EMI plugin classes never compiled on either loader | `NeoForge/build.gradle:134`, `Fabric/build.gradle:127` | `/*if(!useLib)*/exclude "com/tom/storagemod/emi/**"` is a block comment, so the exclude always applies; the EMI dependency is NF-only with `-DuseLib` and commented out on Fabric. The whole EMI integration is dead code at `95bb61d` even though the plugin is fully written | gameplay |
| `INT-02` | Fabric declares an `emi` entrypoint whose class is not compiled | `Fabric/src/main/resources/fabric.mod.json` (entrypoints) + `Fabric/build.gradle:127` | If EMI is installed on Fabric, entrypoint resolution loads a class that was excluded from the build (suspected load error) | crash |
| `INT-03` | EMI "craft" never sends the fill packet | `NeoForge/src/platform-shared/java/com/tom/storagemod/emi/EmiTransferHandler.java:132-137` | In `handleRecipe` the non-simulated branch has the `fill` NBT send commented out, so `craft()` only re-opens the terminal screen without filling the grid | gameplay |
| `INT-04` | EMI inventory skips slot 0 | `NeoForge/src/platform-shared/java/com/tom/storagemod/emi/EmiTransferHandler.java:48` | `slots.subList(1, size)` drops the first menu slot — checked: slot 0 is the crafting result slot in `CraftingTerminalMenu`, so excluding it is correct | not a bug |
| `INT-05` | REI/EMI missing-slot highlight uses a guessed index mapping | `NeoForge/src/platform-shared/java/com/tom/storagemod/rei/ReiTransferHandler.java:67`, `NeoForge/src/platform-shared/java/com/tom/storagemod/emi/EmiTransferHandler.java:125` | `width == 1 ? i * 3 : width == 2 ? (i % 2) + i / 2 * 3 : i` remaps recipe-input order to recipe-widget order; if the viewer's widget order does not match the assumption, the wrong slot is highlighted red | visual |
| `INT-06` | Jade config providers registered for every block and block entity | `NeoForge/src/platform-shared/java/com/tom/storagemod/jade/JadePlugin.java:27,34` | `InventoryConfigDataProvider` is registered for `BlockEntity.class` and `InventoryConfigComponentProvider` for `Block.class`; every Jade hover on any block in the world runs `BlockFilter.getFilterAt` (a level lookup) even for unrelated mods' blocks | performance |
| `INT-07` | TOP progress with zero total | `NeoForge/src/main/java/com/tom/storagemod/top/TheOneProbeHandler.java:50` | `probeInfo.progress(usage.all() - usage.free(), usage.all(), ...)` is called with total `0` when a connector has no slots (empty network); suspected divide-by-zero inside TOP's progress renderer | crash |
| `INT-08` | Polymorph terminal widget never registered | `NeoForge/src/main/java/com/tom/storagemod/StorageMod.java:69`, `Fabric/src/main/java/com/tom/storagemod/StorageModClient.java:154` | The only `PolymorphTerminalWidget.register()` calls are commented out on both loaders, so the recipe-widget is dead code; `StorageMod.polymorph` is still set and `PolymorphHelper` still runs server-side, so only the client widget is missing | gameplay |
| `INT-09` | Sophisticated Storage compat dead on Fabric | `Fabric/src/main/java/com/tom/storagemod/StorageMod.java:179-181`, `Fabric/build.gradle:131` | The registration is commented out ("See build.gradle") and `SophisticatedDoubleBlocks.java` is unconditionally excluded from the Fabric build, while older Fabric versions shipped the compat (changelog 1.21.x) | gameplay |
| `INT-10` | JEI clickable recipe area misses the result slot | `NeoForge/src/platform-shared/java/com/tom/storagemod/jei/JEIHandler.java:45` | The area is `(83, 35 + rowCount * 18, 28, 23)` screen-relative; the crafting result slot sits at menu coordinates `(120, 129)` (`CraftingTerminalMenu` origin `x=-4, y=94`), so the clickable area (x 83..111) does not overlap the slot (x 120..136), and in tall mode its y grows with `rowCount` while the slot stays fixed | gameplay |
