# 05-integration · contradictions and proposals

Part of [05-integration](index.md).

## Contradictions with other docs

| Where | Quote | Conflicts with |
|---|---|---|
| Original 05 sheets (emi, jei, rei, jade, other-compat) | "None (original spec only; ...)" | not the literal "None yet."; left untouched |

<a id="proposed-not-decided"></a>
## Accepted proposals

| # | Question | Outcome | Decision |
|---|---|---|---|
| P05-1 | Ship NEI ghost-drag into filter slots and NEI search-box sync? | Stays a beta candidate; alpha accepts direct click/drag. | D11 |
| P05-2 | One Waila provider per tile entity or one shared provider? | Per-class providers registered individually; keep the upstream NBT keys. | D29 |
| P05-3 | Add TOP or Sophisticated Storage compat on 1.7.10? | No: neither is in the pack. Recorded as "none"; reopen only if the pack adds them. | D29 |
| P05-4 | Event hooks for filter-drop on break and left-click listeners | Forge event bus handlers behind `modules.*` flags; event class names verified at Forge 10.13.4.1614 first. | D29 |
| P05-5 | Six Waila providers against upstream Jade's three: keep all six? | Keep the six (cabinet contents, terminal stats, emitter, network size, hopper filter, configurator bound state), each gated by `modules.integration.waila`. A deviation from upstream Jade. | D24 |
