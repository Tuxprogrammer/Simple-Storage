# Inventory link (connector channel GUI, menu + screen)

| # | Field | Contents |
|---|---|---|
| 1 | **Identity** | `InventoryLinkMenu` (SH `menu/InventoryLinkMenu.java`), `InventoryLinkScreen` (SH `screen/InventoryLinkScreen.java`, texture `toms_storage:textures/gui/inventory_link.png`). Menu type `Content.inventoryLink`; title `menu.toms_storage.inventory_connector` = "Inventory Link". Opened from the Inventory Cable Connector (02-storage `inventory-connector.md`); `InventoryLinkMenu.LinkChannel` record carries one channel to the client. Sources: branch `26.2`, `NeoForge/src/platform-shared/java/com/tom/storagemod/`; `menu/InventoryLinkMenu.java:NN` (M), `screen/InventoryLinkScreen.java:NN` (S). (M:28, M:100, S:40) |
| 2 | **Inputs** | Channel name text field (max 50 chars) (S:125), create/delete buttons, public/private toggle, channel list selection, data slot `beaconLvl` (M:42). |
| 3 | **Numbers** | Name field at (leftPos+13, topPos+28), width 105, no border, white text (S:124-128). Create button (`icons/add`) at (leftPos+121, topPos+24) (S:100); delete (`icons/deny`) at (leftPos+138, topPos+24) (S:106); public toggle at (leftPos+155, topPos+24) with icons `icons/lock_on` (private) / `icons/lock_off` (public) (S:42-43, 109-111). Channel list at (leftPos+12, topPos+42), 114×100 px, 16 px rows (S:120, 204). Beacon level label under the title at (titleLabelX, titleLabelY+10), color 0xFF404040 (S:93). |
| 4 | **Behavior** | See Behavior detail. |
| 5 | **Determinism** | List order: sorted by (publicChannel ascending — private first —, then display name) (S:75-77). No randomness. |
| 6 | **Data formats** | Channel record NBT (client list entry) (M:102-111, 122-128): `id` = UUID (optional), `d` = display name (string), `p` = public (bool), `o` = owner UUID (optional), `on` = owner name (string, "" when null) (M:127). Client→server edit tag: `id` (UUID, present for edits/deletes) + `p` + `d` (from `saveToServer`), or no `id` for create (M:130-133, S:144-149). Client→server select tag: `id` + `select=true` (S:151-156). Server→client list payload (from `broadcastChanges`, once per open — `sentList` flag) (M:73-91): `selected` = UUID (optional) (M:78-79) + `list` = list of channel compounds (M:81-87). Data slot: `beaconLvl` int (M:42). |
| 7 | **Output** | Channel created/edited/deleted in `RemoteConnections` (02-storage `cable-network.md`); the connector's channel set (`te.setChannel(id)`). |
| 8 | **Relationships** | `RemoteConnections` + `RemoteConnections.Channel` (02-storage `cable-network.md`), `InventoryCableConnectorBlockEntity.getBeaconLevel` (02-storage `inventory-connector.md`), `NetworkHandler` (01-core `network.md`), `ListWidget` (06-client-ui), `IconButton`/`ToggleButton` (06-client-ui), `TSContainerScreen` (03-terminal `container-screens.md`). |
| 9 | **NeoForge dependencies** | `UUIDUtil.CODEC`, `MapCodec`/`RecordCodecBuilder` (channel codec), `EditBox`, `ListWidget` (vanilla), data slot. No 1.7.10 counterpart: `MapCodec`, data slots, `ValueInput`/`ValueOutput`. |
| 10 | **Bugs** | `TERM-08` (typing a name that matches an existing channel selects the first match — duplicate channel names are allowed by the create path, so two channels can share a name and the typed-name lookup is ambiguous). |

## Behavior detail

### Menu (server)

1. No slots of its own; `quickMoveStack` always EMPTY (M:95-98). `stillValid` = the connector's `stillValid` (or true for the client placeholder menu) (M:68-70).
2. `receive(tag)` (M:46; server; ignored for spectators or when no connector, M:47):
   - with `id`: if `select` → channel must exist and `canAccess(player)`, then `te.setChannel(id)` (M:49-52); else if `p` present → `RemoteConnections.editChannel(id, public, playerUUID)` (M:54-55), else `removeChannel(id, playerUUID)` (M:56-57).
   - without `id`: `makeChannel(name = tag.getStringOr("d", "No Name"), public = tag.getBooleanOr("p", false), player)` → `te.setChannel(newChannel)` (M:61-62).
   - `sentList = false` (next broadcast re-sends the list) (M:64).
3. `broadcastChanges` (server): first broadcast after open (or after any edit) sends `{selected?, list}` via `NetworkHandler.sendTo` over the visible channels (`streamChannels(player)` — public + own private) (M:82).

### Screen (client)

4. `receive` (client) (S:59): rebuilds the `connections` map from the `list` array (S:60-63); selects the channel named by `selected` (or null) (S:64-74); `update()` re-sorts the list by (public asc, name) and refreshes the widget (S:75-78).
5. Buttons:
   - Create: enabled only when no channel is selected and the text field is non-empty (trimmed) (S:165, 169); press → `sendEdit(null, new LinkChannel(publicBtn state, name))` (S:101-103).
   - Delete: enabled only with a selection; press → `sendEdit(selected, null)` (S:107; enabled S:161).
   - Public toggle: enabled only when the selected channel's owner is the local player; press → `sendEdit(sel, new LinkChannel(state, sel.displayName()))` (S:113-116; enabled S:163-164).
   - Text field: on change, clear the selection, then try to select a channel whose display name exactly equals the text (first match — **`TERM-08`**) (S:130-139).
6. List: `ListHandler extends ListWidget<LinkChannel>` (114×100, 16 px rows) (S:201, 204); row render draws the lock icon (public/private) right-aligned (x+width−16) (S:239); row tooltip: owner line — `tooltip.toms_storage.inventory_connector.channel.owner` = "Owner: %s", `.owner.self` = "Owner: %s (You)" (when the local player owns it), or `.owner.unknown` = "Owner: Unknown" when the owner name is empty (S:225-230) — then `tooltip.toms_storage.inv_link.public` = "Public Channel" or `.private` = "Private Channel" (S:231). Row selection → `sendSelect(id)` + `update()` (S:243-245).
7. `extractLabels` (S:91-94): title + `label.toms_storage.inventory_connector.beacon_level` = "Beacon Level: %d" under it.
8. Keyboard (S:186-199): ESC closes (returns true, so ESC does not also close via super); TAB passes through; everything else goes to the text field while it can consume input.
9. `getExclusionAreas`: none (S:250-251).

## Lang keys used by this feature

- `menu.toms_storage.inventory_connector` = "Inventory Link"
- `label.toms_storage.inventory_connector.beacon_level` = "Beacon Level: %d"
- `narrator.toms_storage.inventory_link_channel` = "Inventory Link Channel Name Field"
- `tooltip.toms_storage.inv_link.private` = "Private Channel"; `inv_link.public` = "Public Channel"
- `tooltip.toms_storage.inventory_connector.channel.owner` = "Owner: %s"; `.owner.self` = "Owner: %s (You)"; `.owner.unknown` = "Owner: Unknown"

## Deviations in the new mod

See the [03-terminal deviation table](../../new-spec/03-terminal/deviations.md#deviation-table) (row `inventory-link`).
