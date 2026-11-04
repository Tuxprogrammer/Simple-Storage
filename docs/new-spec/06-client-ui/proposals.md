# 06-client-ui · contradictions and proposals

Part of [06-client-ui](index.md).

## Contradictions with other docs

| Where | Says | Conflicts with |
|---|---|---|
| [lang.md Bugs row](../../original-spec/06-client-ui/lang.md) | UI-02 is "missing `narrator.toms_storage.button_state.*`" | [BUGS.md](../../original-spec/BUGS.md) UI-02 is "narration key constructed dynamically, exists for no value" (same defect, two phrasings) |

<a id="proposed-not-decided"></a>
## Accepted proposals

| # | Question | Outcome | Decision |
|---|---|---|---|
| P06-1 | Narrator keys (UI-01, UI-02, UI-07): is a narration layer ported? | Not ported by decision (upstream code remains at 26.2); UI-01, UI-02 and UI-07 cannot occur. Every button keeps a human-readable tooltip. | D21 |
| P06-2 | Painted-block rendering approach | One renderer id with the facade in the TE, shader-safe (skill `client-rendering-shaders`), tested under Angelica 2.2.43. | D29 |
| P06-3 | World outlines without the no-depth pipeline | `RenderWorldLastEvent` with depth test disabled and state restored; tested under Angelica 2.2.43 with a shader pack. | D29 |
| P06-4 | Cost of the per-frame 15x15x15 painted-highlight scan | Scan only while the paint item is held, or cache per tick. | D29 |
| P06-5 | Nine-slice and sprite atlas replacement | One shared nine-blit helper, individual PNGs, no atlas. | D29 |
| P06-6 | Recipe book widget | Not ported by decision (upstream code remains at 26.2); crafting fill is covered by the NEI overlay (D11). | D21 |
| P06-7 | ViveCraft helper | Not ported by decision (upstream code remains at 26.2); no ViveCraft in the pack. | D21 |
| P06-8 | Which locales ship | English (`en_us`) only, no other locale at alpha or beta. | D25 |
