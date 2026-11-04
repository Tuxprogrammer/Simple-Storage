# 05 · Integration

## Overview

Third-party integrations of Tom's Simple Storage Mod `2.11.3` (upstream branch `26.2`, commit `95bb61d`): the recipe-viewer plugins for JEI, REI and EMI (ghost-drag item filters into the filter screens, auto-fill/auto-craft from the recipe GUI, search-field sync), the Jade (Waila) block-info integration, The One Probe, Polymorph, Sophisticated Storage, the mod's own public multiblock-inventory API, and the small compat event handlers. All of these are optional at runtime (except what the NeoForge jar ships inside itself — see Key facts). The NeoForge side is the port baseline; Fabric differences are noted per sheet.

## Contents

| Sheet | What it covers |
|---|---|
| [jei.md](jei.md) | JEI plugin: recipe areas, ghost drag, auto-fill transfer handler, search sync |
| [rei.md](rei.md) | REI plugin: workstations, ghost drag, transfer handler, search sync |
| [emi.md](emi.md) | EMI plugin: workstation, drag-drop, recipe handler, search sync |
| [jade.md](jade.md) | Jade plugin: three block components and their server data payloads |
| [other-compat.md](other-compat.md) | The One Probe, Polymorph, Sophisticated Storage, `MultiblockInventoryAPI`, `EventHandler`, ModMenu, `ServerEvents` |

## Also in this area

- [facts.md](facts.md): key facts, related areas.
- [coverage.md](coverage.md): what was read, known limits.
- [bugs.md](bugs.md): suspected bugs.

New mod: [../../new-spec/05-integration/index.md](../../new-spec/05-integration/index.md).
