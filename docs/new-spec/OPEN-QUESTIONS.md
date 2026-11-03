# Open questions

What the new spec has not settled yet. Each row says what settles it: research in a roadmap spike, a milestone, or the owner, with a recommendation. A settled question leaves the open list, and its answer goes into the area sheet and a design record or decision-log row.

## Open

| # | Question | Recommendation | Settled by |
|---|---|---|---|
| Q1 | Which tier cap does the storage content occupy, and what is the per-item tier map? | Per [0003](../design/0003-gtnh-layer-and-tier-ceiling.md): early game up to **EU (HV)**; the per-item map is written when S2 refines it and M05 lands the recipes | S2, built in M05 |
| Q2 | Does the terminal consume EU? | Per [0004](../design/0004-energy-and-machine-model.md): faithful upstream, **power-free**; M05 may add an optional EU sink behind a config flag, off by default | S2, built in M05 |
| Q3 | Does GTNH ship Applied Energistics 2, and does that change this mod's positioning? | No recommendation yet: if AE2 is in the pack, this mod is the cheap, simple storage network beside it, not a replacement. S3 confirms whether AE2 ships and what it covers | S3 |
| Q4 | Which recipe viewer does GTNH use, and what plugin API applies in 1.7.10? | GTNH ships NEI (Not Enough Items); its 1.7.10 recipe-handler plugin API is what M06's recipe handlers target — confirm the exact surface from source at the pack's tag | S2, S3 |
