# 06-client-ui · deviations

Part of [06-client-ui](index.md).

## Sheets

| Sheet | Contents |
|---|---|
| none | All content is in the deviation table below |

## How to read the table

- **Original**: the original-spec sheet.
- **Here**: `same`, `changed`, `not ported` (by decision; the upstream code remains at the 26.2 branch), `deferred`, `new`.
- **Decision**: an Accepted decision-log row (D#) or design record (000N); `Proposed P06-n` is listed under [Proposed](proposals.md#proposed-not-decided) and not decided.
- 1.7.10 facts about other mods cite [API-MAP.md](../../original-spec/API-MAP.md) or [00-pack-integration](../00-pack-integration/); otherwise "unverified". Vanilla 1.7.10 facts not in those docs are also marked unverified.

## Deviation table

<a id="deviation-table"></a>

| Original | Here | What changes | Decision | Bugs |
|---|---|---|---|---|
| [index](../../original-spec/06-client-ui/index.md) | same | Area index; no behavior | D6 | all UI-xx (see rows) |
| [widgets](../../original-spec/06-client-ui/widgets.md) | changed | 16x16 `IconButton`, `ToggleButton`, 106x16-row `ListWidget`, `EnumCycleButton` (icon `icons/<prefix>_<enum>`), `TerminalSearchModeButton` (bits 1 auto, 2 keep, 4 sync, 8 smart-off) become `GuiButton`/`Gui` subclasses drawn with `Gui.drawModalRectWithCustomSize`; nine-slice sprites have no 1.7.10 counterpart, so a shared nine-blit helper replaces `WidgetSprites` + mcmeta (API-MAP:26). Tooltip text goes through `GuiScreen.drawHoveringText` (unverified). `CraftingTerminalRecipeBookWidget` has no 1.7.10 counterpart (API-MAP:27, vanilla has no recipe book). UI-04 (NPE before `setState`), UI-05 (scrollbar grab area hard-coded) and UI-06 (popup click rects offset) are fixed in the rewrite with tests; UI-07 and UI-02 fall under narrator, below | Proposed P06-1, P06-5, P06-6 | UI-02, UI-04, UI-05, UI-06, UI-07 |
| [client-package](../../original-spec/06-client-ui/client-package.md) | changed | World outlines (terminal range from the wireless config, 2 px lines, white, alpha 0.4; configurator outline) move to Forge `RenderWorldLastEvent` (API-MAP:30). HUD overlay moves to `RenderGameOverlayEvent` (API-MAP:31). `B` keybind: `KeyBinding` plus client-tick `isPressed` (API-MAP:33), default-key clash is [OPEN-QUESTIONS Q6](../OPEN-QUESTIONS.md). Tooltip extension uses the item tooltip hook (API-MAP:34 names `Item.addTooltip`; unverified). The custom no-depth line `RenderPipeline` has no 1.7.10 counterpart (client-package sheet row 9); depth test is switched off around the draw (shader-pack behavior unverified, skill `client-rendering-shaders`). The per-frame 15x15x15 painted-highlight scan is kept only if measured cheap. `ViveCraftHelper` not ported by decision (no ViveCraft in the pack per API-MAP) | Proposed P06-3, P06-4, P06-7 | none |
| [painted-models](../../original-spec/06-client-ui/painted-models.md) | changed | `DynamicBlockStateModel`/`BakedPaintedModel` wrapping (4 blocks: `paintedTrim`, `invCableFramed`, `invProxy`, `invCableConnectorFramed`, facade state) has no 1.7.10 baking event (API-MAP:35); the facade is held in the tile entity and drawn by a custom block renderer (API-MAP:35 "custom IBlockRenderer/tile-entity-driven rendering"). `GameRegistry`-based block and TE registration stays in 01-core | Proposed P06-2 | none |
| [lang](../../original-spec/06-client-ui/lang.md) | changed | 217 `en_us` keys carry over by name where the feature exists; `config.jade.plugin_toms_storage.*` become Waila provider names via IMC (API-MAP:16; owner 05-integration). `text.autoconfig.*` and `toms_storage.configuration.*` become the GTNHLib auto config screen's keys (exact format unverified). Missing keys fixed: the 11 narrator keys (UI-01, via P06-1) and `tooltip.toms_trading_network.textfield` (UI-03/CORE-07: key renamed into the `simplestoragenh` namespace and added). Six partial locales (fr_fr 189, ja_jp 189, ko_kr 96, pt_br 212, ru_ru 212, zh_cn 197 keys) are not carried at alpha. 1.7.10 lang files are `*.lang` key=value, not JSON (unverified) | Proposed P06-1, P06-8 | UI-01, UI-02, UI-03 |
| [textures-sounds](../../original-spec/06-client-ui/textures-sounds.md) | changed | GUI backgrounds (7 per index.md:35, 5 per this sheet; see Contradictions) are re-authored 1.7.10 GUI textures; the 45 icon sprites and 23-24 widget sprites become plain PNGs without the sprite atlas (`atlases/gui.json`) or mcmeta. No sounds upstream, none added | Proposed P06-5 | none |
