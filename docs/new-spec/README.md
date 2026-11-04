# Simple Storage new spec

How this mod differs from Tom's Simple Storage 2.11.3 and how it fits into GT New Horizons. Upstream's behavior is in [docs/original-spec/](../original-spec/README.md). Builders read both: the original-spec sheet for what a system does, and the matching new-spec area for what changes and how it connects to the pack.

Target pack: **GTNH 2.9.0** ([0002](../design/0002-target-pack.md), S3-verified).

Status: `00-pack-integration` is written (spikes S2 and S3, 2026-11-03); the area specs `01` to `06` are written as their milestones build (M02 to M06, [ROADMAP](../ROADMAP.md)). Areas 01 to 06 are written by the milestones that build them (M01 writes 01-core, M02 02-storage, M03 03-terminal and 06-client-ui, M04 04-wireless, M06 05-integration; see ROADMAP).

## What this mod is for

Give a GTNH player a storage network that feels native: the upstream terminal, storage blocks, filters, sorting and wireless access, rebuilt on GT materials and recipe maps ([0003](../design/0003-gtnh-layer-and-tier-ceiling.md)) and power-free like upstream ([0004](../design/0004-energy-and-machine-model.md)).

Every change here serves that, and every system uses what the pack already has before adding anything of its own (`gt-first-design`).

## How to read

- Each area mirrors an original-spec area, plus `00-pack-integration` for cross-cutting pack topics. Its `index.md` has a **deviation table**: every original sheet, what this mod does instead, and the decision behind it.
- **Status words.**
  - *Accepted* means a decision record in [docs/design/](../design/README.md) settles it; the record is linked.
  - *Proposed* means this spec suggests it and the owner has not decided. Every proposal is listed in [OPEN-QUESTIONS.md](OPEN-QUESTIONS.md) with its recommendation and what settles it: the milestone that builds it, or the owner.
  - *Verified* facts about other mods cite the file and line at the pack's tag; *unverified* ones say so and must be checked from source before code (`gtnh-integration-research`).
- Numbers carry units: blocks, chunks, block-Y levels, ticks, EU.

## Areas

| Area | What changes |
|---|---|
| `00-pack-integration` | The target pack and its versions, GT recipe maps and materials, the recipe viewer, every pack mod this mod touches (S3 + S2, done 2026-11-03) |
| `01-core` | 1.7.10 lifecycle and registration, Forge config with module flags, network, the platform layer collapsed into one |
| `02-storage` | The storage item and block on GT parts, NBT formats, the item components |
| `03-terminal` | The terminal, its screens, menus and slots, filters and sorting, the inventory stack logic |
| `04-wireless` | The wireless terminal, range and synchronization |
| `05-integration` | The pack's recipe viewer and the pack's info-mod equivalents of upstream's JEI/REI/EMI and Jade, other compat |
| `06-client-ui` | Widgets, the client package, lang keys, textures |
| [OPEN-QUESTIONS.md](OPEN-QUESTIONS.md) | What is still undecided, who or what settles it, and the recommendation |
