# Design records

Accepted design decisions for Simple Storage. Larger decisions get a numbered record, from the [template](../templates/decision-record.md); to add one, copy the template to `NNNN-short-title.md` and link it here. Smaller ones are rows in the [decision log](decision-log.md). When a decision changes, its record is edited in place; git keeps the history.

| Record | Status | Settles |
|---|---|---|
| [0001](0001-port-policy-and-licensing.md) | Accepted | Upstream baseline `26.2` at `95bb61d` (mod 2.11.3, MIT); code ported under the ported-code gate; upstream textures and models reused; our work MIT, sole copyright Spencer Callicott 2026 |
| [0002](0002-target-pack.md) | Pending S3 | The target pack and its dependency versions (default GTNH 2.9.0); written by the pack-research lane this round |
| [0003](0003-gtnh-layer-and-tier-ceiling.md) | Accepted | Storage content lives in the GT recipe maps on GT materials, from the early game up to the named cap (EU); no own materials; the per-item tier map is M05's |
| [0004](0004-energy-and-machine-model.md) | Accepted | Faithful upstream: the terminal and storage blocks are power-free utility blocks, not GT machines; an optional EU sink may come in M05 |
| [decision-log.md](decision-log.md) | Running | Process, build, tooling and spec decisions (D1 to D6) |
