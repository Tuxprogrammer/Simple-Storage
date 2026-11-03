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
2. **Not GT machines.** The blocks are ordinary 1.7.10 blocks with their own simple GUIs, not GT MetaTileEntities and not GT's GUI framework. They claim no MetaTileEntity IDs. *Refined by S2 (decision D9): verified against the pack — `IMetaTileEntity extends ISidedInventory, IFluidTank, IFluidHandler` (GT5-Unofficial 5.09.54.265, IMetaTileEntity.java#L50) is the GT-machine shape; the pack's storage neighbors (ironchest 6.1.13, StorageDrawers 2.2.31-GTNH, EnderStorage 1.8.9 — S3 MOD-INDEX) are all ordinary tile entities without EU, so "power-free utility block with its own GUI" is the established pack shape for non-processing storage. Details in [new-spec/00-pack-integration/machine-model.md](../new-spec/00-pack-integration/machine-model.md).*
3. **An optional EU sink in M05.** M05 may add an **optional** EU sink behind a config flag, off by default, so a server that wants an energy cost gets one without changing the default experience. *Refined by S2 (decision D7): the sink is the **level emitter as a fixed consumer at the ULV rate — 8 EU/t while active** (`GTValues.V[0]`, verified in [new-spec/00-pack-integration/gtnh-tiers-and-energy.md](../new-spec/00-pack-integration/gtnh-tiers-and-energy.md)). The emitter is a redstone utility, not a machine, so it drains at the ULV rate rather than its HV item tier (512 EU/t would make the sink a machine, which §2 avoids). Config keys: `euSink.enabled` (default `false`), `euSink.euPerTick` (default `8`). The flag changes behavior only, never registration (`config-safe-registration`).*
4. **No multiblock at alpha.** Upstream has no multiblock; a storage network scales by adding blocks. If the beta review wants a multiblock, it gets its own record.

## Consequences

- The new-spec `02-storage` and `03-terminal` sheets say "no energy" and cite this record; the EU sink, if it ships, gets its own row.
- The config flag for the EU sink is registered with M01's config and documented there.
- [OPEN-QUESTIONS](../new-spec/OPEN-QUESTIONS.md) Q2 tracks whether the sink ships and as what.

## Alternatives considered

- **Full GT machines.** Every terminal and storage block an EU-consuming MetaTileEntity at its tier, with GT's GUI. Rejected: it loses what makes the mod recognizable, front-loads machine work, and costs players energy for what the pack expects to be free.
- **RF or IC2-style energy.** Rejected: GTNH is EU-only.

## Still open

- (S2, 2026-11-03: Q2 settled — the optional EU sink ships as what, at what rate: **level emitter, 8 EU/t ULV rate while active, behind `euSink.enabled` (off by default)** — decision D7. M05 builds it; if the beta review finds 8 EU/t too cheap or too expensive, the `euSink.euPerTick` knob is the escape hatch rather than a new record.)
