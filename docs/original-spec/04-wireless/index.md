# 04 · Wireless

## Overview

The wireless terminal feature of Tom's Simple Storage 2.11.3 (upstream branch `26.2`, commit `95bb61d`, NeoForge baseline): the two wireless items (basic `wireless_terminal`, advanced `adv_wireless_terminal`), how they extend terminal reach, the advanced item's block binding (the `bound_pos` data component), the beacon-level mechanic that grants same-dimension or cross-dimension access, and the keybind open flow. There is **no wireless block, block entity, menu or screen of its own in 26.2** — the items remotely activate the normal terminal blocks (`storage_terminal`, `crafting_terminal`, via the `toms_storage:remote_activate` tag), and the terminal's menu/screen/sync are the wired terminal's (03-terminal, 06-client-ui, and 01-core terminal-sync.md). Where a fact differs by loader or version, the sheet says which loader/branch it came from.

## Contents

| Sheet | What it covers |
|---|---|
| [wireless-items.md](wireless-items.md) | `WirelessTerminal`, basic and advanced items, binding, tooltips, keybind open, recipes |
| [range-and-sync.md](range-and-sync.md) | Reach check (`canInteractWith`), 40-tick beacon scan, keybind open flow, link to 01-core terminal sync |

## Also in this area

- [facts.md](facts.md): key facts, related areas, lang.
- [coverage.md](coverage.md): what was read, known limits.
- [bugs.md](bugs.md): suspected bugs and deviations in the new mod.

New mod: [../../new-spec/04-wireless/index.md](../../new-spec/04-wireless/index.md).
