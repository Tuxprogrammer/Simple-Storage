# 06-client-ui · contradictions and proposals

Part of [06-client-ui](index.md).

## Contradictions with other docs

| Where | Says | Conflicts with |
|---|---|---|
| [lang.md Bugs row](../../original-spec/06-client-ui/lang.md) | UI-02 is "missing `narrator.toms_storage.button_state.*`" | [BUGS.md](../../original-spec/BUGS.md) UI-02 is "narration key constructed dynamically, exists for no value" (same defect, two phrasings) |

## Proposed (not decided)

| Id | Question | Recommendation | Settled by |
|---|---|---|---|
| P06-1 | Narrator keys (UI-01, UI-02, UI-07): is a narration layer ported at all? | Drop narrator keys and `createNarrationMessage`; the 1.7.10 GUI stack has no narrator hook (unverified). Keep human-readable tooltips on every button instead | M03 (terminal UI) |
| P06-2 | Painted-block rendering: `ISimpleBlockRenderingHandler` with TE-held facade, or per-block renderer? | One renderer id with the facade in the TE, shader-safe (skill `client-rendering-shaders`) and tested under Angelica 2.2.43 | M02 (storage blocks) |
| P06-3 | World outlines without the no-depth pipeline | `RenderWorldLastEvent` with depth test disabled and state restored; test under Angelica 2.2.43 with a shader pack | M03 (terminal outline), M04 (wireless) |
| P06-4 | Cost of the per-frame 15x15x15 painted-highlight scan (3375 positions) | Throttle to a scan only while the paint item is held, or cache per tick | M03 |
| P06-5 | Nine-slice and sprite atlas replacement | One shared nine-blit helper, individual PNGs, no atlas | M03 |
| P06-6 | Recipe book widget: rebuild or drop | Drop; crafting fill is covered by the NEI overlay (D11) | M03 |
| P06-7 | ViveCraft helper | Drop (no ViveCraft in the pack, API-MAP) | owner, at M03 review |
| P06-8 | Which locales ship | `en_US` only at alpha; keep the six upstream translations as source for later import under the ported-code rules (D2) | owner, before first release (M06) |
