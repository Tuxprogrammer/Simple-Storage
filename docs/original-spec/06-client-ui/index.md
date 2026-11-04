# 06 · Client UI

## Overview

The client-side presentation layer of Tom's Simple Storage Mod `2.11.3` (upstream branch `26.2`, commit `95bb61d`): the reusable GUI widget kit (`IconButton` and its subclasses, `ListWidget`, the recipe-book widget, the popup-menu mechanism), the client package (world-space outlines for the wireless terminal and inventory configurator, the custom line render pipeline, ViveCraft VR support, and the loader client entry points), the painted-block model facades, and the complete client resource inventory (every lang key with its English text, GUI textures and icon sprites, the GUI sprite atlas, and the sound list — which is empty). The concrete screens themselves (`AbstractStorageTerminalScreen`, `StorageTerminalScreen`, `CraftingTerminalScreen`, the filter/config/hopper/link/cabinet screens) are documented in areas `02`/`03` (the screens are assigned to the areas that own the systems they present); this area documents the widget kit, the client plumbing, and the resources they all consume. The NeoForge side is the port baseline; Fabric differences are noted per sheet.

## Contents

| Sheet | What it covers |
|---|---|
| [widgets.md](widgets.md) | Widget kit: `IconButton` family, `ListWidget`, recipe-book widget, `PopupMenuManager` |
| [client-package.md](client-package.md) | `ClientUtil`, `CustomRenderTypes`, `ViveCraftHelper`, client entry points |
| [painted-models.md](painted-models.md) | Painted-block facade rendering (NF and FB) |
| [lang.md](lang.md) | All 217 `en_us` lang keys, locale coverage, dynamic keys, hard-coded strings |
| [textures-sounds.md](textures-sounds.md) | GUI textures, icon and widget sprites, GUI atlas, sounds |

## Also in this area

- [facts.md](facts.md): key facts, related areas.
- [coverage.md](coverage.md): what was read, known limits.
- [bugs.md](bugs.md): suspected bugs.

New mod: [../../new-spec/06-client-ui/index.md](../../new-spec/06-client-ui/index.md).
