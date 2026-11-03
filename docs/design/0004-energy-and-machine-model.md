# 0004 · Energy and machine model

- Status: Accepted
- Gate: none (foundation)
- Amends: none
- Date: 2026-11-02

## Context

Upstream's terminal, storage blocks, cable and connector blocks and wireless items are **power-free utility blocks**: they take no energy input and do no machine processing. A terminal is a terminal, a storage block is storage. That is the mod's identity — a simple, free storage network.

The GT-first alternative would make every one of them an EU-consuming GT machine with a GT GUI. That is the pack-native shape, but it turns a free utility into a machine with a progression cost, and it changes what the mod is.

## Decision

1. **Faithful upstream: power-free.** The terminal, the storage blocks, the cable and connector blocks and the wireless items consume no energy at alpha. No EU buffer, no per-operation cost.
2. **Not GT machines.** The blocks are ordinary 1.7.10 blocks with their own simple GUIs, not GT MetaTileEntities and not GT's GUI framework. They claim no MetaTileEntity IDs.
3. **An optional EU sink in M05.** M05 may add an **optional** EU sink behind a config flag, off by default (for example, the level emitter as an EU consumer, or a small per-transfer cost on the terminal), so a server that wants an energy cost gets one without changing the default experience. S2 refines the mechanism. The flag changes behavior only, never registration (`config-safe-registration`).
4. **No multiblock at alpha.** Upstream has no multiblock; a storage network scales by adding blocks. If the beta review wants a multiblock, it gets its own record.

## Consequences

- The new-spec `02-storage` and `03-terminal` sheets say "no energy" and cite this record; the EU sink, if it ships, gets its own row.
- The config flag for the EU sink is registered with M01's config and documented there.
- [OPEN-QUESTIONS](../new-spec/OPEN-QUESTIONS.md) Q2 tracks whether the sink ships and as what.

## Alternatives considered

- **Full GT machines.** Every terminal and storage block an EU-consuming MetaTileEntity at its tier, with GT's GUI. Rejected: it loses what makes the mod recognizable, front-loads machine work, and costs players energy for what the pack expects to be free.
- **RF or IC2-style energy.** Rejected: GTNH is EU-only.

## Still open

- Q2: does the optional EU sink ship, and as what — settled by S2 and built in M05.
