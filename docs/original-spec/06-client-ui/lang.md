# System: Lang keys (client + common resources)

| # | Field | Value |
|---|---|---|
| 1 | **Identity** | `NeoForge/src/platform-shared/resources/assets/toms_storage/lang/*.json` — `en_us.json` (217 keys, the reference) + 6 shipped translations: `fr_fr` (189 keys), `ja_jp` (189), `ko_kr` (96), `pt_br` (212), `ru_ru` (212), `zh_cn` (197). No lang files exist elsewhere (no NF/FB-specific lang dirs at `95bb61d`) |
| 1a | **Sources** | Upstream `26.2`, file `NeoForge/src/platform-shared/resources/assets/toms_storage/lang/en_us.json` (232 lines, 217 key/value pairs); short citations below are `en_us.json:<line>`. |
| 2 | **Inputs** | `I18n.get`/`Component.translatable` call sites across the mod (areas `01`–`05`); AutoConfig (Cloth on NF, Fabric AutoConfig on FB) reads the `text.autoconfig.*` and `toms_storage.configuration.*` keys; Jade reads `config.jade.plugin_toms_storage.*` |
| 3 | **Numbers** | 217 total keys in `en_us`: 128 `tooltip.toms_storage.*`, 22 `text.autoconfig.toms_storage.*`, 15 `block.toms_storage.*`, 15 `config.toms_storage.*`, 7 `chat.toms_storage.*`, 7 `item.toms_storage.*`, 6 `toms_storage.configuration.*`, 6 `menu.toms_storage.*`, 3 `config.jade.plugin_toms_storage.*`, 3 `narrator.toms_storage.*`, 1 each `key.*`, `itemGroup.*`, `label.*`, `overlay.*`, `tag.*`. Translations are all partial (see per-locale gaps below) |
| 4 | **Behavior** | See Behavior detail |
| 5 | **Determinism** | Static resource lookup at render/tooltip time; the 3 multi-line values use `\\n` line breaks (rendered as separate tooltip lines by the `multilineTooltip`-style consumers). Format args: `%d`, `%s`, `%%%` (a literal percent in `terminal_stat.free`) |
| 6 | **Data formats** | Standard MC lang JSON (flat key → string). The `toms_storage.configuration.importantInfo.tooltip` and the `config.toms_storage.*`/`text.autoconfig.*...@Tooltip` values are the only multi-line strings |
| 7 | **Output** | Block/item/menu names, item tooltips (including the dynamic beacon-level lines), the terminal HUD lines (`terminal_stat.*`), chat messages, config-screen text, narrator labels, the keybind name |
| 8 | **Relationships** | Every area references its own keys here: block/item names (area `02`/`03`/`04`), terminal UI (area `03`), viewer names (area `05`), config options (area `01`); the search-mode and priority/side/ctrlMode keys are constructed by the widget kit (see [widgets.md](widgets.md)) |
| 9 | **NeoForge dependencies** | None beyond the lang-file convention; AutoConfig `@Tooltip` keys (`text.autoconfig.toms_storage.option.*.@Tooltip`) are the AutoConfig tooltip mechanism (NF Cloth / FB AutoConfig — 1.7.10: config GUI text, S2: API-MAP) |
| 10 | **Bugs** | `UI-01` (11 missing narrator keys), `UI-02` (missing `narrator.toms_storage.button_state.*`), `UI-03` (missing `tooltip.toms_trading_network.textfield`, wrong namespace) |

## Behavior detail

### Complete key list (`en_us`, exact text, 217 keys)

| `block.toms_storage.basic_inventory_hopper` | Basic Inventory Hopper |
| `block.toms_storage.crafting_terminal` | Crafting Terminal |
| `block.toms_storage.filing_cabinet` | Filing Cabinet |
| `block.toms_storage.inventory_cable` | Inventory Cable |
| `block.toms_storage.inventory_cable_connector` | Inventory Cable Connector |
| `block.toms_storage.inventory_cable_connector_framed` | Inventory Cable Connector (Framed) |
| `block.toms_storage.inventory_cable_framed` | Framed Inventory Cable |
| `block.toms_storage.inventory_connector` | Inventory Connector |
| `block.toms_storage.inventory_interface` | Inventory Interface |
| `block.toms_storage.inventory_proxy` | Inventory Proxy |
| `block.toms_storage.level_emitter` | Level Emitter |
| `block.toms_storage.open_crate` | Open Crate |
| `block.toms_storage.painted_trim` | Inventory Trim |
| `block.toms_storage.storage_terminal` | Storage Terminal |
| `block.toms_storage.trim` | Inventory Trim |
| `chat.toms_storage.area_too_big` | Selected Area Too Big |
| `chat.toms_storage.inv_link_access_denied` | Access Denied |
| `chat.toms_storage.inventory_connector.free_slots` | Inventory Connector: %d free/%d slots (in %d inventories). |
| `chat.toms_storage.terminal_bound` | Bound Advanced Wireless Terminal |
| `chat.toms_storage.terminal_invalid_block` | Invalid block |
| `chat.toms_storage.terminal_not_loaded` | Terminal out of range (Not Loaded) |
| `chat.toms_storage.terminal_out_of_range` | Terminal out of range |
| `config.jade.plugin_toms_storage.inventory_config` | Inventory Configurator Info |
| `config.jade.plugin_toms_storage.inventory_connector` | Inventory Connector Info |
| `config.jade.plugin_toms_storage.inventory_hopper` | Inventory Hopper Info |
| `config.toms_storage.adv_wireless_beacon_lvl` | Advanced wireless terminal requied beacon level for infinite range\nValue of 0 only requires a single beacon block nearby\nValue of -1 disables this feature entirely |
| `config.toms_storage.adv_wireless_beacon_lvl_dim` | Adv Wireless terminal requied beacon level for cross dimensional access\nValue of 0 only requires a single beacon block nearby\nValue of -1 disables this feature entirely |
| `config.toms_storage.adv_wireless_range` | Advanced wireless terminal range |
| `config.toms_storage.basic_hopper_cooldown` | Basic Inventory Hopper transfer cooldown (ticks) |
| `config.toms_storage.inv_blocked_blocks` | List of block ids ignored by the inventory connector |
| `config.toms_storage.inv_blocked_mods` | List of mod ids whose blocks is ignored by the inventory connector |
| `config.toms_storage.inv_cable_connector_max_scan` | Inventory Cable Connector Maximum number of cables |
| `config.toms_storage.inv_link_beacon_lvl` | Inventory Cable Connector requied beacon level for inventory linking\nValue of 0 only requires a single beacon block\nValue of -1 disables this feature entirely |
| `config.toms_storage.inv_link_beacon_lvl_dim` | Inventory Cable Connector requied beacon level for cross dimensional access\nValue of 0 only requires a single beacon block nearby\nValue of -1 disables this feature entirely |
| `config.toms_storage.inv_link_beacon_lvl_same_dim` | Inventory Cable Connector requied beacon level for same dimension access with unlimited range\nValue of 0 only requires a single beacon block nearby\nValue of -1 disables this feature entirely |
| `config.toms_storage.inv_link_beacon_range` | Inventory Linking range with beacons\nValue of 0 disables this feature entirely |
| `config.toms_storage.inventory_connector_range` | Inventory Connector Range |
| `config.toms_storage.only_trims_connect` | Only Allow Trims to Connect Inventories |
| `config.toms_storage.run_multithreaded` | Use multithreading to improve performance |
| `config.toms_storage.wireless_reach` | Wireless terminal reach |
| `item.toms_storage.adv_wireless_terminal` | Advanced Wireless Terminal |
| `item.toms_storage.inventory_configurator` | Inventory Configurator |
| `item.toms_storage.item_filter` | Item Filter |
| `item.toms_storage.paint_kit` | Paint Kit |
| `item.toms_storage.polymorphic_item_filter` | Polymorphic Item Filter |
| `item.toms_storage.tag_item_filter` | Tag Item Filter |
| `item.toms_storage.wireless_terminal` | Wireless Terminal |
| `itemGroup.toms_storage.tab` | Tom's Simple Storage (en_us.json:26) |
| `key.toms_storage.open_terminal` | [Tom's Simple Storage] Open Terminal (en_us.json:224) |
| `label.toms_storage.inventory_connector.beacon_level` | Beacon Level: %d |
| `menu.toms_storage.crafting_terminal` | C. Terminal |
| `menu.toms_storage.filing_cabinet` | Filing Cabinet |
| `menu.toms_storage.inventory_configurator` | Inventory Configurator (%s) |
| `menu.toms_storage.inventory_connector` | Inventory Link |
| `menu.toms_storage.level_emitter` | Level Emitter |
| `menu.toms_storage.storage_terminal` | S. Terminal |
| `narrator.toms_storage.inventory_link_channel` | Inventory Link Channel Name Field |
| `narrator.toms_storage.level_emitter_amount` | Level Emitter Item Count |
| `narrator.toms_storage.terminal_search` | Terminal Search Field |
| `overlay.toms_storage.configurator_info` | Tom's Simple Storage: Configurator Overlay |
| `tag.toms_storage.trims` | Inventory Trims |
| `text.autoconfig.toms_storage.option.advWirelessRange` | Advanced wireless terminal range |
| `text.autoconfig.toms_storage.option.basicHopperCooldown` | Basic Inventory Hopper transfer cooldown (ticks) |
| `text.autoconfig.toms_storage.option.blockedBlocks` | List of block ids ignored by the inventory connector |
| `text.autoconfig.toms_storage.option.blockedMods` | List of mod ids whose blocks is ignored by the inventory connector |
| `text.autoconfig.toms_storage.option.invConnectorMaxCables` | Inventory Cable Connector Maximum number of cables |
| `text.autoconfig.toms_storage.option.invConnectorScanRange` | Inventory Connector Range |
| `text.autoconfig.toms_storage.option.invLinkBeaconLvl` | Inventory Link Beacon Level |
| `text.autoconfig.toms_storage.option.invLinkBeaconLvl.@Tooltip` | Inventory Cable Connector requied beacon level for inventory linking\nValue of 0 only requires a single beacon block nearby\nValue of -1 disables this feature entirely |
| `text.autoconfig.toms_storage.option.invLinkBeaconLvlCrossDim` | Inventory Link Beacon Level Cross Dimension |
| `text.autoconfig.toms_storage.option.invLinkBeaconLvlCrossDim.@Tooltip` | Inventory Cable Connector requied beacon level for cross dimensional access\nValue of 0 only requires a single beacon block nearby\nValue of -1 disables this feature entirely |
| `text.autoconfig.toms_storage.option.invLinkBeaconLvlSameDim` | Inventory Link Beacon Level Same Dimension |
| `text.autoconfig.toms_storage.option.invLinkBeaconLvlSameDim.@Tooltip` | Inventory Cable Connector requied beacon level for same dimension access with unlimited range\nValue of 0 only requires a single beacon block nearby\nValue of -1 disables this feature entirely |
| `text.autoconfig.toms_storage.option.invLinkBeaconRange` | Inventory Linking range with beacons |
| `text.autoconfig.toms_storage.option.invLinkBeaconRange.@Tooltip` | Value of 0 disables this feature entirely |
| `text.autoconfig.toms_storage.option.onlyTrims` | Only Allow Trims to Connect Inventories |
| `text.autoconfig.toms_storage.option.runMultithreaded` | Use multithreading to improve performance |
| `text.autoconfig.toms_storage.option.wirelessRange` | Wireless terminal reach |
| `text.autoconfig.toms_storage.option.wirelessTermBeaconLvl` | Adv. Wireless Terminal beacon level |
| `text.autoconfig.toms_storage.option.wirelessTermBeaconLvl.@Tooltip` | Advanced wireless terminal requied beacon level for infinite range\nValue of 0 only requires a single beacon block nearby\nValue of -1 disables this feature entirely |
| `text.autoconfig.toms_storage.option.wirelessTermBeaconLvlCrossDim` | Adv. Wireless Terminal cross dimension level |
| `text.autoconfig.toms_storage.option.wirelessTermBeaconLvlCrossDim.@Tooltip` | Advanced Wireless terminal requied beacon level for cross dimensional access\nValue of 0 only requires a single beacon block nearby\nValue of -1 disables this feature entirely |
| `text.autoconfig.toms_storage.title` | Tom's Simple Storage Configuration |
| `toms_storage.configuration.importantInfo.tooltip` | IMPORTANT NOTICE:\nTHIS IS ONLY THE COMMON CONFIG. It does not contain all the values adjustable for Tom's Simple Storage\nThe settings have been moved to toms_storage-server.toml\nThat file is PER WORLD, meaning you have to go into 'saves/<world name>/serverconfig' to adjust it. Those changes will then only apply for THAT WORLD.\nYou can then take that config file and put it in the 'defaultconfigs' folder to make it apply automatically to all NEW worlds you generate FROM THERE ON.\nThis may appear confusing to many of you, but it is a new sensible way to handle configuration, because the server configuration is synced when playing multiplayer. |
| `toms_storage.configuration.section.toms.storage.common.toml` | Tom's Simple Storage Common Configuration |
| `toms_storage.configuration.section.toms.storage.common.toml.title` | Tom's Simple Storage Common Configuration |
| `toms_storage.configuration.section.toms.storage.server.toml` | Tom's Simple Storage Server Configuration |
| `toms_storage.configuration.section.toms.storage.server.toml.title` | Tom's Simple Storage Server Configuration |
| `toms_storage.configuration.title` | Tom's Simple Storage Configuration |
| `tooltip.toms_storage.adv_wireless_terminal` | Sneak + Right Click on a terminal to bind it.\\Range: %d blocks\\The teminal's chunk has to be loaded |
| `tooltip.toms_storage.adv_wireless_terminal.beacon` | A nearby beacon in an 8 block radius of the terminal\\extends the range of the terminal.\\The beacon's level determines the range: |
| `tooltip.toms_storage.adv_wireless_terminal.beacon1` | %d: anywhere in the same dimension. |
| `tooltip.toms_storage.adv_wireless_terminal.beacon2` | %d: anywhere across dimensions. |
| `tooltip.toms_storage.adv_wireless_terminal.bound` | Bound to (%s, %s, %s) in %s |
| `tooltip.toms_storage.adv_wireless_terminal.keybind` | Press the %s key to open |
| `tooltip.toms_storage.adv_wireless_terminal.keybind.outline` | [%s] |
| `tooltip.toms_storage.allowList` | Allow matching |
| `tooltip.toms_storage.amount` | Item Amount: %d |
| `tooltip.toms_storage.block_filter` | Inventory Configuration |
| `tooltip.toms_storage.block_filter.item` | Filter: %s |
| `tooltip.toms_storage.block_filter.skip` | Skipped by Inventory Connector |
| `tooltip.toms_storage.config_overlay.block` | Configuring Block: %d, %d, %d |
| `tooltip.toms_storage.config_overlay.block_alt` | Configuring Block: %s |
| `tooltip.toms_storage.config_overlay.configure` | %s to configure block |
| `tooltip.toms_storage.config_overlay.mass_select` | Multiblock Area Mass Selecting |
| `tooltip.toms_storage.config_overlay.mass_select.abort` | %s + %s to abort current selection |
| `tooltip.toms_storage.config_overlay.mass_select.finish` | %s to finish area select |
| `tooltip.toms_storage.config_overlay.select` | Multiblock Area Single Selecting |
| `tooltip.toms_storage.config_overlay.select.finish` | %s in the air to finish selection |
| `tooltip.toms_storage.config_overlay.select.single` | %s to add/remove a single inventory |
| `tooltip.toms_storage.config_overlay.select.start_mass` | %s to start area select |
| `tooltip.toms_storage.config_overlay.show` | %s to see connected blocks |
| `tooltip.toms_storage.config_overlay.too_far` | Configuring block too far |
| `tooltip.toms_storage.connector_info.inv` | %d inventories |
| `tooltip.toms_storage.connector_info.size` | %d free/%d slots |
| `tooltip.toms_storage.content_stored` | Contains items |
| `tooltip.toms_storage.crafting_pull_off` | Don't pull from players inventory when crafting |
| `tooltip.toms_storage.crafting_pull_on` | Pull missing items from players inventory when crafting |
| `tooltip.toms_storage.crafting_terminal` | Gives access to your items.\\Place it on an inventory.\\Has a builtin crafting table. |
| `tooltip.toms_storage.ctrlMode_ae` | AE Like Controls\\Pull one item: Shift+Right Click\\Transfer one item: Ctrl+Shift+Right Click |
| `tooltip.toms_storage.ctrlMode_def` | Vanilla Like Controls\\Pull one item: Right Click\\Transfer one item: Shift+Right Click |
| `tooltip.toms_storage.ctrlMode_rs` | Refined Storage Like Controls\\Pull one item: Middle Click\\Transfer one item: Shift+Middle Click |
| `tooltip.toms_storage.denyList` | Reject matching |
| `tooltip.toms_storage.disabled_by_redstone` | Disabled by Redstone |
| `tooltip.toms_storage.empty` | Empty |
| `tooltip.toms_storage.filing_cabinet` | Store multiple variants of the same item |
| `tooltip.toms_storage.filter_item` | Filter: %s |
| `tooltip.toms_storage.filtered` | Can be filtered |
| `tooltip.toms_storage.ghostMode_off` | Disable sorting while holding SHIFT |
| `tooltip.toms_storage.ghostMode_on` | Enable sorting while holding SHIFT |
| `tooltip.toms_storage.hold_control_for_details` | Hold %s for configuration details. |
| `tooltip.toms_storage.hold_shift_for_info` | Hold SHIFT for more info. |
| `tooltip.toms_storage.inv_config.add_connected` | Add Connected Blocks |
| `tooltip.toms_storage.inv_config.include` | Include in Inventory Connectors |
| `tooltip.toms_storage.inv_config.remove_all_connected` | Remove All Connected Blocks |
| `tooltip.toms_storage.inv_config.remove_filter` | Clear Configuration |
| `tooltip.toms_storage.inv_config.skip` | Exclude from Inventory Connectors |
| `tooltip.toms_storage.inv_link.private` | Private Channel |
| `tooltip.toms_storage.inv_link.public` | Public Channel |
| `tooltip.toms_storage.inventory_cable` | Connects inventories across\\long distances. Inventory Cable Connectors\\have to be used to attach inventories\\One Inventory Connector must be\\present in the network. |
| `tooltip.toms_storage.inventory_cable_connector` | Connects the inventory to the Inventory Network.\\Use the Inventory Configurator to filter. |
| `tooltip.toms_storage.inventory_cable_connector_link` | The both sides' chunk has to be loaded |
| `tooltip.toms_storage.inventory_cable_connector_link.beacon` | The beacon's level determines the range: |
| `tooltip.toms_storage.inventory_cable_connector_link.beacon0` | %d: %d block range. |
| `tooltip.toms_storage.inventory_cable_connector_link.beacon1` | %d: anywhere in the same dimension. |
| `tooltip.toms_storage.inventory_cable_connector_link.beacon2` | %d: anywhere across dimensions. |
| `tooltip.toms_storage.inventory_configurator` | Configure how each inventory\\interacts with the Inventory Connector.\\Highlights painted blocks. |
| `tooltip.toms_storage.inventory_connector` | Connects all of the touching inventories into one.\\Use the Storage Terminal to access\\all of your items. Trims can connect the gaps. |
| `tooltip.toms_storage.inventory_connector.channel.owner` | Owner: %s |
| `tooltip.toms_storage.inventory_connector.channel.owner.self` | Owner: %s (You) |
| `tooltip.toms_storage.inventory_connector.channel.owner.unknown` | Owner: Unknown |
| `tooltip.toms_storage.inventory_hopper` | Import or export from your Inventory Network\\Right click an item to set the filter.\\Sneak click with an empty hand to clear filter. |
| `tooltip.toms_storage.inventory_interface` | Access the connected Inventory Connector's inventory |
| `tooltip.toms_storage.inventory_proxy` | Extends the face of the block its pointing at. |
| `tooltip.toms_storage.item_filter` | Allow filtering multiple items with components |
| `tooltip.toms_storage.item_filter.contents` | Contents: |
| `tooltip.toms_storage.item_filter.no_items` | No items |
| `tooltip.toms_storage.item_filter.prefix` | - %s |
| `tooltip.toms_storage.item_sync_error` | Item Stack Sync error |
| `tooltip.toms_storage.items_missing` | Some items may not be inserted into the crafting grid |
| `tooltip.toms_storage.keepLast_off` | Don't keep last item in slot |
| `tooltip.toms_storage.keepLast_on` | Keep last item in slot |
| `tooltip.toms_storage.level_emitter` | Emits a redstone signal based on Inventory System contents. |
| `tooltip.toms_storage.level_emitter.greater_than` | Emit when more than specified in storage |
| `tooltip.toms_storage.level_emitter.less_than` | Emit when less than specified in storage |
| `tooltip.toms_storage.loop` | Loop detected in your storage system |
| `tooltip.toms_storage.matchNBT_off` | Don't match Components |
| `tooltip.toms_storage.matchNBT_on` | Match Components |
| `tooltip.toms_storage.nbt_overflow` | This item's tag is too big, the item may not show correctly in the terminal |
| `tooltip.toms_storage.open_crate` | Converts the block in front of it\\into an inventory.\\Beware: Creates Item Entities |
| `tooltip.toms_storage.opt.search_auto` | Auto Select search: %s (en_us.json:114) |
| `tooltip.toms_storage.opt.search_keep` | Keep search: %s (en_us.json:115) |
| `tooltip.toms_storage.opt.search_smart` | Smart features with %s: %s (May lag) (en_us.json:117) |
| `tooltip.toms_storage.opt.search_sync` | Sync search with %s: %s (en_us.json:116) |
| `tooltip.toms_storage.paint_kit` | Paint blocks to match\\their surroundings! |
| `tooltip.toms_storage.paintable` | Can be painted |
| `tooltip.toms_storage.poly_item_filter` | Only allow inserting items that already exists in this inventory |
| `tooltip.toms_storage.priority_high` | Priority: High |
| `tooltip.toms_storage.priority_highest` | Priority: Highest |
| `tooltip.toms_storage.priority_low` | Priority: Low |
| `tooltip.toms_storage.priority_lowest` | Priority: Lowest |
| `tooltip.toms_storage.priority_normal` | Priority: Normal |
| `tooltip.toms_storage.recipe_book` | Recipe Book (en_us.json:118) |
| `tooltip.toms_storage.search` | Standard Search |
| `tooltip.toms_storage.search_auto` | Auto Search |
| `tooltip.toms_storage.search_auto_keep` | Auto Search Keep |
| `tooltip.toms_storage.search_auto_keep_sync` | %s Synced Auto Search Keep (en_us.json:71) |
| `tooltip.toms_storage.search_auto_sync` | %s Synced Auto Search |
| `tooltip.toms_storage.search_keep` | Standard Search Keep |
| `tooltip.toms_storage.search_keep_sync` | %s Synced Standard Search Keep |
| `tooltip.toms_storage.search_sync` | %s Synced Standard Search |
| `tooltip.toms_storage.side_down` | Side: Down |
| `tooltip.toms_storage.side_east` | Side: East |
| `tooltip.toms_storage.side_north` | Side: North |
| `tooltip.toms_storage.side_south` | Side: South |
| `tooltip.toms_storage.side_up` | Side: Up |
| `tooltip.toms_storage.side_west` | Side: West |
| `tooltip.toms_storage.slots_used` | Used Slots: %d |
| `tooltip.toms_storage.sorting_amount` | Sort by Item Amount |
| `tooltip.toms_storage.sorting_id` | Sort by Item ID |
| `tooltip.toms_storage.sorting_mod` | Sort by Mod |
| `tooltip.toms_storage.sorting_name` | Sort by Item Name |
| `tooltip.toms_storage.sorting_space_efficiency` | Sort by Space Efficiency |
| `tooltip.toms_storage.sorting_type` | Sort by Item Type |
| `tooltip.toms_storage.storage_terminal` | Gives access to your items.\\Place it on an inventory. |
| `tooltip.toms_storage.tag_item_filter` | Filter using tags |
| `tooltip.toms_storage.tallMode_off` | Terminal Size: Default |
| `tooltip.toms_storage.tallMode_on` | Terminal Size: Auto |
| `tooltip.toms_storage.terminal_beacon` | Beacon level: %d%s |
| `tooltip.toms_storage.terminal_beacon.anywhere` | This terminal is accessible anywhere\\with the Advanced Wireless Terminal |
| `tooltip.toms_storage.terminal_beacon.sameDim` | This terminal is accessible anywhere\\in this dimension\\with the Advanced Wireless Terminal |
| `tooltip.toms_storage.terminal_stat.alot` | Alot (>32k) |
| `tooltip.toms_storage.terminal_stat.free` | Free slots: %s (%s%%) |
| `tooltip.toms_storage.terminal_stat.slots` | All slots: %s |
| `tooltip.toms_storage.terminal_stat.unique` | Unique items: %s |
| `tooltip.toms_storage.trim` | Connects inventories with the Inventory Connector. |
| `tooltip.toms_storage.wireless_terminal` | Extends your reach for the terminal.\\Reach: %d blocks |

### Keys constructed dynamically in code (not literals in the source)

| Pattern | Builder | Values that exist |
|---|---|---|
| `tooltip.toms_storage.search[_auto][_keep][_sync]` | `TerminalSearchModeButton.setSearchType` (`SH/screen/widget/TerminalSearchModeButton.java:88-97`) | all 4 present: `search`, `search_auto`, `search_keep`, `search_sync` |
| `tooltip.toms_storage.priority_<name>` | Jade config component (`SH/jade/InventoryConfigComponentProvider.java:39`) and TOP handler (`NF/top/TheOneProbeHandler.java:68`) | all 5 present: `priority_lowest` … `priority_highest` |
| `narrator.toms_storage.button_state.<state>` | `EnumCycleButton.createNarrationMessage` (`SH/screen/widget/EnumCycleButton.java:26`) | **none present** for any state (sort types, control modes, priorities, directions) — `UI-02` |
| `tooltip.toms_storage.inventory_connector.channel.owner[.self/.unknown]` | `InventoryLinkScreen.ListHandler.renderTooltip` (area `02-storage`) | all 3 present |
| `tooltip.toms_storage.inventory_connector_link.beacon<lvl>` | inventory-link beacon lines (area `02`/`04`) | `beacon0`, `beacon1`, `beacon2` present |
| `tooltip.toms_storage.adv_wireless_terminal.beacon<lvl>` | advanced wireless terminal tooltip (area `04-wireless`) | `beacon1`, `beacon2` present (no `beacon0`) |
| `tooltip.toms_storage.terminal_beacon[.sameDim/.anywhere]` | terminal beacon state lines (area `04-wireless`) | all 3 present |

### Hard-coded English in code (not lang-ified)

| String | Location | Notes |
|---|---|---|
| `Component.translatable("")` (empty key) | `SH/screen/widget/TerminalSearchModeButton.java:25` | button name is an empty-key translatable |
| `"_"` cursor character | `SH/util/PopupMenuManager.java:156` | inserted into the `TextFieldElement` text |
| `"Failed to init TheOneProbeHandler"`, `"Error loading server config"`, `"Received server config"` (log messages) | `NF/StorageMod.java:86`, `FB/StorageModClient.java:127,131` | log-only, not user-facing UI |
| `"Tried to register a null listener!"` | `SH/api/EventHandler.java` (area `05-integration`) | assertion message |
| `CommonComponents.GUI_YES`/`GUI_NO` | `SH/screen/widget/TerminalSearchModeButton.java` | vanilla keys (`gui.yes`/`gui.no`) |
| `gui.recipebook.toggleRecipes.craftable`, `recipe_book/filter_*` textures | `SH/screen/widget/CraftingTerminalRecipeBookWidget.java` | vanilla recipe-book keys/textures |

### Per-locale gaps (verified against `en_us` at `95bb61d`)

| Locale | Keys | Missing | Extra | Notes |
|---|---|---|---|---|
| `fr_fr` | 189 | 30 | 2 | missing incl. `config.toms_storage.basic_hopper_cooldown`, `overlay.toms_storage.configurator_info`, several `config.jade.*`; extra `tooltip.toms_storage.inventory_configurator.mass_selection`, `…selection` |
| `ja_jp` | 189 | 31 | 3 | missing incl. `block.toms_storage.basic_inventory_hopper`, `config.jade.plugin_toms_storage.inventory_hopper`; extra includes the two `inventory_configurator.*` keys plus a stray `block.toms_storage.basic_inventory_hopper_basic` |
| `ko_kr` | 96 | 133 | 12 | by far the most incomplete; 12 extra keys are `_text.autoconfig.*` (renamed with an underscore prefix — dead keys from an older AutoConfig naming) |
| `pt_br` | 212 | 5 | 0 | missing `tooltip.toms_storage.item_sync_error`, `slots_used`, `sorting_id`, `sorting_space_efficiency`, `sorting_type` |
| `ru_ru` | 212 | 5 | 0 | same 5 missing as pt_br |
| `zh_cn` | 197 | 22 | 2 | missing incl. `config.jade.plugin_toms_storage.inventory_hopper`, `overlay.toms_storage.configurator_info`, `tooltip.toms_storage.config_overlay.block`/`_alt` |

## Deviations in the new mod

None (original spec only; the port keeps `en_us` and may drop the partial translations — S2 decision).
