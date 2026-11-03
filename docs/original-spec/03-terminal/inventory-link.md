# Inventory link (connector channel GUI, menu + screen)

| # | Field | Contents |
|---|---|---|
| 1 | **Identity** | `InventoryLinkMenu` (SH `menu/InventoryLinkMenu.java`), `InventoryLinkScreen` (SH `screen/InventoryLinkScreen.java`, texture `toms_storage:textures/gui/inventory_link.png`). Menu type `Content.inventoryLink`; title `menu.toms_storage.inventory_connector` = "Inventory Link". Opened from the Inventory Cable Connector (02-storage `inventory-connector.md`); `InventoryLinkMenu.LinkChannel` record carries one channel to the client. |
| 2 | **Inputs** | Channel name text field (max 50 chars), create/delete buttons, public/private toggle, channel list selection, data slot `beaconLvl`. |
| 3 | **Numbers** | Name field at (leftPos+13, topPos+28), width 105, no border, white text. Create button (`icons/add`) at (leftPos+121, topPos+24); delete (`icons/deny`) at (leftPos+138, topPos+24); public toggle at (leftPos+155, topPos+24) with icons `icons/lock_on` (private) / `icons/lock_off` (public). Channel list at (leftPos+12, topPos+42), 114×100 px, 16 px rows. Beacon level label under the title at (titleLabelX, titleLabelY+10), color 0xFF404040. |
| 4 | **Behavior** | See Behavior detail. |
| 5 | **Determinism** | List order: sorted by (publicChannel ascending — private first —, then display name). No randomness. |
| 6 | **Data formats** | Channel record NBT (client list entry): `id` = UUID (optional), `d` = display name (string), `p` = public (bool), `o` = owner UUID (optional), `on` = owner name (string, "" when null). Client→server edit tag: `id` (UUID, present for edits/deletes) + `p` + `d` (from `saveToServer`), or no `id` for create. Client→server select tag: `id` + `select=true`. Server→client list payload (from `broadcastChanges`, once per open — `sentList` flag): `selected` = UUID (optional) + `list` = list of channel compounds. Data slot: `beaconLvl` int. |
| 7 | **Output** | Channel created/edited/deleted in `RemoteConnections` (02-storage `cable-network.md`); the connector's channel set (`te.setChannel(id)`). |
| 8 | **Relationships** | `RemoteConnections` + `RemoteConnections.Channel` (02-storage `cable-network.md`), `InventoryCableConnectorBlockEntity.getBeaconLevel` (02-storage `inventory-connector.md`), `NetworkHandler` (01-core `network.md`), `ListWidget` (06-client-ui), `IconButton`/`ToggleButton` (06-client-ui), `TSContainerScreen` (03-terminal `container-screens.md`). |
| 9 | **NeoForge dependencies** | `UUIDUtil.CODEC`, `MapCodec`/`RecordCodecBuilder` (channel codec), `EditBox`, `ListWidget` (vanilla), data slot. No 1.7.10 counterpart: `MapCodec`, data slots, `ValueInput`/`ValueOutput`. |
| 10 | **Bugs** | `TERM-08` (typing a name that matches an existing channel selects the first match — duplicate channel names are allowed by the create path, so two channels can share a name and the typed-name lookup is ambiguous). |

## Behavior detail

### Menu (server)

1. No slots of its own; `quickMoveStack` always EMPTY. `stillValid` = the connector's `stillValid` (or true for the client placeholder menu).
2. `receive(tag)` (server; ignored for spectators or when no connector):
   - with `id`: if `select` → channel must exist and `canAccess(player)`, then `te.setChannel(id)`; else if `p` present → `RemoteConnections.editChannel(id, public, playerUUID)`, else `removeChannel(id, playerUUID)`.
   - without `id`: `makeChannel(name = tag.getStringOr("d", "No Name"), public = tag.getBooleanOr("p", false), player)` → `te.setChannel(newChannel)`.
   - `sentList = false` (next broadcast re-sends the list).
3. `broadcastChanges` (server): first broadcast after open (or after any edit) sends `{selected?, list}` via `NetworkHandler.sendTo` over the visible channels (`streamChannels(player)` — public + own private).

### Screen (client)

4. `receive` (client): rebuilds the `connections` map from the `list` array; selects the channel named by `selected` (or null); `update()` re-sorts the list by (public asc, name) and refreshes the widget.
5. Buttons:
   - Create: enabled only when no channel is selected and the text field is non-empty (trimmed); press → `sendEdit(null, new LinkChannel(publicBtn state, name))`.
   - Delete: enabled only with a selection; press → `sendEdit(selected, null)`.
   - Public toggle: enabled only when the selected channel's owner is the local player; press → `sendEdit(sel, new LinkChannel(state, sel.displayName()))`.
   - Text field: on change, clear the selection, then try to select a channel whose display name exactly equals the text (first match — **`TERM-08`**).
6. List: `ListHandler extends ListWidget<LinkChannel>` (114×100, 16 px rows); row render draws the lock icon (public/private) right-aligned (x+width−16); row tooltip: owner line — `tooltip.toms_storage.inventory_connector.channel.owner` = "Owner: %s", `.owner.self` = "Owner: %s (You)" (when the local player owns it), or `.owner.unknown` = "Owner: Unknown" when the owner name is empty — then `tooltip.toms_storage.inv_link.public` = "Public Channel" or `.private` = "Private Channel". Row selection → `sendSelect(id)` + `update()`.
7. `extractLabels`: title + `label.toms_storage.inventory_connector.beacon_level` = "Beacon Level: %d" under it.
8. Keyboard: ESC closes (returns true, so ESC does not also close via super); TAB passes through; everything else goes to the text field while it can consume input.
9. `getExclusionAreas`: none.

## Lang keys used by this feature

- `menu.toms_storage.inventory_connector` = "Inventory Link"
- `label.toms_storage.inventory_connector.beacon_level` = "Beacon Level: %d"
- `narrator.toms_storage.inventory_link_channel` = "Inventory Link Channel Name Field"
- `tooltip.toms_storage.inv_link.private` = "Private Channel"; `inv_link.public` = "Public Channel"
- `tooltip.toms_storage.inventory_connector.channel.owner` = "Owner: %s"; `.owner.self` = "Owner: %s (You)"; `.owner.unknown` = "Owner: Unknown"

## Deviations in the new mod

None yet.
