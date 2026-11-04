# 06-client-ui: new spec

Status: *draft* (new-spec pass, 2026-11-03). Compares each sheet in [original-spec 06-client-ui](../../original-spec/06-client-ui/index.md) (`toms_storage@95bb61d`) with the 1.7.10 port. Area prefix `UI` ([D6](../../design/decision-log.md)). No extra sheets: every deviation fits one row.

| File | What it holds |
|---|---|
| [deviations.md](deviations.md) | Sheets, how to read the table, the deviation table (6 rows) |
| [proposals.md](proposals.md) | Contradictions with other docs; accepted proposals (8 rows) |

Implementation: the widget math (scrollbar, popup rects, enum cycle) and the terminal-facing screens shipped in M03 (see [03-terminal/shipped.md](../03-terminal/shipped.md)); textures and models wait for M06 (D36).
