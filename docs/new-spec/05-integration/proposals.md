# 05-integration · contradictions and proposals

Part of [05-integration](index.md).

## Contradictions with other docs

| Where | Quote | Conflicts with |
|---|---|---|
| Original 05 sheets (emi, jei, rei, jade, other-compat) | "None (original spec only; ...)" | not the literal "None yet."; left untouched |

## Proposed (not decided)

| Id | Question | Recommendation | Settled by |
|---|---|---|---|
| P05-1 | Ship NEI ghost-drag into filter slots and NEI search-box sync? | Defer to beta (D11 already marks ghost drag a beta candidate); alpha accepts direct click/drag | M06 |
| P05-2 | One Waila provider per tile entity or one shared provider with per-class payloads? | Per-class providers registered individually; keep the upstream NBT keys | M06 |
| P05-3 | Add TOP or Sophisticated Storage compat on 1.7.10? | No (not in the pack); record as "none" and reopen only if the pack adds them | Owner, M06 |
| P05-4 | Event hooks for filter-drop on break and left-click listeners | Forge event bus handlers behind `modules.*` flags; verify event class names at Forge 10.13.4.1614 first | M03 / M06 |
| P05-5 | The M06 Waila list has six providers against upstream Jade's three. Keep all six? | Keep the six; they are cheap Waila providers. Owner to confirm | Owner, M06 |
