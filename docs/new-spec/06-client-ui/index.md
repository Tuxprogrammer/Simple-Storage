# 06-client-ui: new spec

Status: *draft* (new-spec pass, 2026-11-03). Compares each sheet in [original-spec 06-client-ui](../../original-spec/06-client-ui/index.md) (`toms_storage@95bb61d`) with the 1.7.10 port. Area prefix `UI` ([D6](../../design/decision-log.md)). No extra sheets: every deviation fits one row.

## Sheets

| Sheet | Contents |
|---|---|
| none | All content is in the deviation table below |

## How to read the table

- **Original**: the original-spec sheet.
- **Here**: `same`, `changed`, `dropped`, `deferred`, `new`.
- **Decision**: an Accepted decision-log row (D#) or design record (000N); `Proposed P06-n` is listed under [Proposed](#proposed-not-decided) and not decided.
- 1.7.10 facts about other mods cite [API-MAP.md](../../original-spec/API-MAP.md) or [00-pack-integration](../00-pack-integration/); otherwise "unverified". Vanilla 1.7.10 facts not in those docs are also marked unverified.

## Deviation table

<a id="deviation-table"></a>

| Original | Here | What changes | Decision | Bugs |
|---|---|---|---|---|
| [index](../../original-spec/06-client-ui/index.md) | same | Area index; no behavior | D6 | all UI-xx (see rows) |
| [widgets](../../original-spec/06-client-ui/widgets.md) | changed | 16x16 `IconButton`, `ToggleButton`, 106x16-row `ListWidget`, `EnumCycleButton` (icon `icons/<prefix>_<enum>`), `TerminalSearchModeButton` (bits 1 auto, 2 keep, 4 sync, 8 smart-off) become `GuiButton`/`Gui` subclasses drawn with `Gui.drawModalRectWithCustomSize`; nine-slice sprites have no 1.7.10 counterpart, so a shared nine-blit helper replaces `WidgetSprites` + mcmeta (API-MAP:26). Tooltip text goes through `GuiScreen.drawHoveringText` (unverified). `CraftingTerminalRecipeBookWidget` has no 1.7.10 counterpart (API-MAP:27, vanilla has no recipe book). UI-04 (NPE before `setState`), UI-05 (scrollbar grab area hard-coded) and UI-06 (popup click rects offset) are fixed in the rewrite with tests; UI-07 and UI-02 fall under narrator, below | Proposed P06-1, P06-5, P06-6 | UI-02, UI-04, UI-05, UI-06, UI-07 |
| [client-package](../../original-spec/06-client-ui/client-package.md) | changed | World outlines (terminal range from the wireless config, 2 px lines, white, alpha 0.4; configurator outline) move to Forge `RenderWorldLastEvent` (API-MAP:30). HUD overlay moves to `RenderGameOverlayEvent` (API-MAP:31). `B` keybind: `KeyBinding` plus client-tick `isPressed` (API-MAP:33), default-key clash is [OPEN-QUESTIONS Q6](../OPEN-QUESTIONS.md). Tooltip extension uses the item tooltip hook (API-MAP:34 names `Item.addTooltip`; unverified). The custom no-depth line `RenderPipeline` has no 1.7.10 counterpart (client-package sheet row 9); depth test is switched off around the draw (shader-pack behavior unverified, skill `client-rendering-shaders`). The per-frame 15x15x15 painted-highlight scan is kept only if measured cheap. `ViveCraftHelper` dropped (no ViveCraft in the pack per API-MAP) | Proposed P06-3, P06-4, P06-7 | none |
| [painted-models](../../original-spec/06-client-ui/painted-models.md) | changed | `DynamicBlockStateModel`/`BakedPaintedModel` wrapping (4 blocks: `paintedTrim`, `invCableFramed`, `invProxy`, `invCableConnectorFramed`, facade state) has no 1.7.10 baking event (API-MAP:35); the facade is held in the tile entity and drawn by a custom block renderer (API-MAP:35 "custom IBlockRenderer/tile-entity-driven rendering"). `GameRegistry`-based block and TE registration stays in 01-core | Proposed P06-2 | none |
| [lang](../../original-spec/06-client-ui/lang.md) | changed | 217 `en_us` keys carry over by name where the feature exists; `config.jade.plugin_toms_storage.*` become Waila provider names via IMC (API-MAP:16; owner 05-integration). `text.autoconfig.*` and `toms_storage.configuration.*` become the GTNHLib auto config screen's keys (exact format unverified). Missing keys fixed: the 11 narrator keys (UI-01, via P06-1) and `tooltip.toms_trading_network.textfield` (UI-03/CORE-07: key renamed into the `simplestoragenh` namespace and added). Six partial locales (fr_fr 189, ja_jp 189, ko_kr 96, pt_br 212, ru_ru 212, zh_cn 197 keys) are not carried at alpha. 1.7.10 lang files are `*.lang` key=value, not JSON (unverified) | Proposed P06-1, P06-8 | UI-01, UI-02, UI-03 |
| [textures-sounds](../../original-spec/06-client-ui/textures-sounds.md) | changed | GUI backgrounds (7 per index.md:35, 5 per this sheet; see Contradictions) are re-authored 1.7.10 GUI textures; the 45 icon sprites and 23-24 widget sprites become plain PNGs without the sprite atlas (`atlases/gui.json`) or mcmeta. No sounds upstream, none added | Proposed P06-5 | none |

## Contradictions with other docs

| Where | Says | Conflicts with |
|---|---|---|
| [textures-sounds.md:5](../../original-spec/06-client-ui/textures-sounds.md) | "5 GUI backgrounds" | [index.md:35](../../original-spec/06-client-ui/index.md) "7 GUI backgrounds" (index.md:36 says the count was corrected to 76 files) |
| [textures-sounds.md:5](../../original-spec/06-client-ui/textures-sounds.md) | "24 widget sprite files" | [index.md:35](../../original-spec/06-client-ui/index.md) "23 widget sprite files" |
| [API-MAP.md:34](../../original-spec/API-MAP.md) | tooltip hook is `Item.addTooltip` | not a name cited anywhere in this repo's pack docs; Forge's `ItemTooltipEvent` is the usual hook. Unverified |
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
