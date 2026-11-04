# Terminal · key facts

Part of [03-terminal](index.md).

## Key facts

- Menu + screen pairs share one sheet per feature.
- The terminal's item list lives in the **block entity** (`StorageTerminalBlockEntity`), lazily rebuilt on `broadcastChanges` when the change tracker changes; the menu only shuttles data slots + a binary item payload (`TerminalSyncManager`, 01-core `network.md`).
- Storage "slots" in the terminal menu are `SlotStorage` display records, **not** real `Slot`s — all vanilla slot mechanics (shift-click, drag) are re-implemented by the screen + `onInteract` (`SlotAction`).
- `modes` bit layout: 0x00–0x03 control mode (AE/RS/DEF), 0x10 tall mode, 0x20 pull-from-player-inventory (crafting). `sorting` layout: 0x00–0x05 type, 0x100 reverse, 0x200 ghost off.
- `Priority` values: NORMAL, LOW, HIGH, LOWEST, HIGHEST (01-core `platform-layer.md`); multi-inventory pull/push iterates lowest priority first.
- All GUIs talk to the server through one NBT `DataPacket` channel (01-core `network.md`); menu buttons use `handleInventoryButtonClick`.

## Related areas

- `01-core`: platform inventory adapters (`PlatformInventoryAccess`, `InventorySlot`), `DataSlots`/`IntDataSlots`, `TerminalSyncManager` + `SlotAction`, `NetworkHandler`, `Config`, `BeaconLevelCalc`, `TickerUtil`, `AbstractSimpleItemFilter`/`AbstractTagFilter`, `Priority`, `WorldStates`.
- `02-storage`: the blocks the terminals attach to (filing cabinet, inventory connector, cable network, `RemoteConnections`, `BlockFilter`, `IProxy`/`IInventoryLink`/`IInventoryConnectorReference`/`IChangeTrackerAccess`, item components, level emitter block, hoppers, interface).
- `04-wireless`: `WirelessTerminal.getRange` used by the terminal reach check.
- `05-integration`: `PolymorphHelper`, `IAutoFillTerminal` search-handler registration.
- `06-client-ui`: widgets (`EnumCycleButton`, `ToggleButton`, `IconButton`, `ListWidget`, `CraftingTerminalRecipeBookWidget`, `TerminalSearchModeButton`), `ClientUtil`, renderers/models.

Lang keys: see [lang.md](lang.md).
