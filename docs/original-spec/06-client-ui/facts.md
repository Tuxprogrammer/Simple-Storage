# Client UI · key facts

Part of [06-client-ui](index.md).

## Key facts

- **Widget kit:** all icon buttons are 16×16 px, drawn from the `gui` atlas (nine-slice `widget/slot_button*` sprites, 2 px border) with a 16×16 icon blit on top; list entries use the 106×16 nine-slice `widget/small_button*` sprites. `EnumCycleButton` cycles enum values on press (shift = reverse) and builds its icon as `icons/<prefix>_<enum name lower-cased>`; its narration appends a *dynamically constructed* key `narrator.toms_storage.button_state.<state>` that exists for no state value (`UI-02`).
- **Search-mode bits:** `TerminalSearchModeButton.searchType` is a bitfield — 1=auto, 2=keep, 4=sync, 8=*smart-off* (the "smart features" popup option reports ON when bit 8 is clear). The tooltip key is built dynamically as `tooltip.toms_storage.search[_auto][_keep][_sync]`; all four combinations exist in the lang file.
- **Lang:** `en_us.json` has 217 keys (128 `tooltip.toms_storage.*`, 22 `text.autoconfig.*` config options, 15 `block.*`, 15 `config.toms_storage.*`, 7 `chat.*`, 7 `item.*`, 3 `config.jade.*`, 3 `narrator.*`, 1 `key.*`, 1 `overlay.*`). Six translations ship (fr_fr, ja_jp, ko_kr, pt_br, ru_ru, zh_cn); all are partial (5–133 missing keys each) — see [lang.md](lang.md).
- **Client entry points:** NF `StorageModClient.preInit` registers the 8 menu screens, the `toms_storage:pipeline/lines` render pipeline (Iris `ShaderKey.LINES` when Iris is present), the configurator GUI overlay layer (above all), the `PaintedExtension` client block extensions (for painted trim / framed cable / proxy / framed connector), the `key.toms_storage.open_terminal` keybind (key `B`), the tooltip event, and the model-baking hook for painted blocks. FB `StorageModClient.onInitializeClient` does the equivalent with Fabric APIs (MenuScreens, ModelLoadingPlugin, BlockColorRegistry, LevelRenderEvents.END_MAIN, the `toms_storage:config` login-network config sync, `KeyMappingHelper`).
- **World outlines:** the wireless terminal outline draws the occlusion shape of the looked-at block when holding a wireless terminal and the block's state is in the `toms_storage:remote_activate` tag (range = `Config.wirelessRange`); the configurator outline draws selected blocks, the mass-selection box (blue, no depth), the bound block (red), and — when not selecting — painted-block highlights within a 7-block radius (15×15×15 scan, alpha fades from distance 5 to 7).
- **No sounds:** the mod ships zero sound files and no `sound_definitions`/`sounds.json` at `95bb61d`.

## Related areas

- `03-terminal`: the terminal screens that host the widgets (search field, sort/cycle buttons, tall-mode and control-mode buttons), and the `CraftingTerminalMenu` slot indices the recipe-book widget and viewers rely on
- `02-storage`: the screens for the configurator, level emitter, inventory link, filing cabinet, and item/tag filter (assigned there); the block/item names and config options in the lang file
- `04-wireless`: the wireless terminal item whose world outline `ClientUtil.drawTerminalOutline` draws, and the keybind's `NetworkHandler.openTerminal()` packet
- `05-integration`: the recipe viewers consume the same screens, exclusion areas, and the `IAutoFillTerminal` search handlers; the `PolymorphTerminalWidget` split is noted in `05-integration/other-compat.md`
- `01-core`: `StorageModClient` is the client half of the mod lifecycle (registration, config, keybinds); `StorageTags.REMOTE_ACTIVATE` and `Config` live there
