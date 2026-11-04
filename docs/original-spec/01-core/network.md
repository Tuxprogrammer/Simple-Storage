# Network

| # | Field | Contents |
|---|---|---|
| 1 | **Identity** | `NetworkHandler` (NF: `NeoForge/src/main/java/com/tom/storagemod/network/NetworkHandler.java` — payload registration + handlers + send helpers; Fabric: `Fabric/src/main/java/com/tom/storagemod/network/NetworkHandler.java` — send helpers only, handlers live in the Fabric mod initializer), `DataPacket` (SH), `OpenTerminalPacket` (SH). |
| 2 | **Inputs** | Incoming payloads from the peer; the player's current menu/screen; the player's inventory (for the open-terminal lookup). |
| 3 | **Numbers** | Channel/protocol version string: `"1"` (NF `PayloadRegistrar`). No fixed payload sizes — `DataPacket` carries a whole `CompoundTag` (NBT). |
| 4 | **Behavior** | Ordered steps in [Behavior detail](#behavior-detail). |
| 5 | **Determinism** | None (no randomness; order = packet arrival order). |
| 6 | **Data formats** | Payload `toms_storage:data` (`DataPacket`): one NBT compound, written with `writeNbt`/`readNbt(NbtAccounter.unlimitedHeap())`. Payload `toms_storage:open_terminal` (`OpenTerminalPacket`): empty. Inner tags used by `DataPacket` payloads are produced by `TerminalSyncManager` (see `terminal-sync.md`): `d` = byte-array binary stack data, `l` = short stack count; `a` = byte-array interaction data. |
| 7 | **Output** | Server: menu data updates, item pulls/pushes via the open terminal. Client: screen data updates. |
| 8 | **Relationships** | Called by: `StorageModClient.clientTick` (keybind → `openTerminal()`), `TerminalSyncManager` (`sendTo`/`sendDataToServer`, `terminal-sync.md`), menus and screens (03-terminal/06-client-ui) via `IDataReceiver.receive`. Calls: `IDataReceiver` (SH interface, `terminal-sync.md`), `PlayerInvUtil.findItem` + `WirelessTerminal.open` (04-wireless). |
| 9 | **NeoForge dependencies** | NeoForge payload system: `RegisterPayloadHandlersEvent`, `PayloadRegistrar` (play-to-server / play-bidirectional), `CustomPacketPayload` + `StreamCodec` (SH packet records), `IPayloadContext.enqueueWork` (dispatch to main thread), `PacketDistributor.sendToPlayer`, `ClientPacketDistributor.sendToServer`. Fabric uses `ClientPlayNetworking`/`ServerPlayNetworking` with the same SH payload classes (channel names are the payload type ids). |
| 10 | **Bugs** | `CORE-02` (unbounded NBT in `DataPacket`). |

## Behavior detail

### Registration

1. (both) NF: `NetworkHandler.onPayloadRegister` (RegisterPayloadHandlersEvent) on protocol version `"1"`:
   - `toms_storage:data` — **bidirectional** (play phase): server handler `handleDataServer`, client handler `handleDataClient`.
   - `toms_storage:open_terminal` — **client → server** (play phase): handler `handleTermServer`.
2. (Fabric) The same two payloads are registered by the Fabric mod initializer (not in the Fabric `NetworkHandler`); the Fabric `NetworkHandler` only provides the three send helpers.

### Packet handling

3. (server) `handleDataServer(DataPacket, ctx)`: enqueue to main thread; cast the sender to `ServerPlayer`; if the sender's current `containerMenu` implements `IDataReceiver`, call `receive(ValueInput.create(ProblemReporter.DISCARDING, sender.registryAccess(), packet.tag()))`. Note: `ProblemReporter.DISCARDING` means malformed inner fields are dropped silently (logged), not crashing the player. No other validation of the tag contents — the menu decides which fields it reads.
4. (client) `handleDataClient(DataPacket, ctx)`: enqueue to main thread; if the currently open screen (`Minecraft.getInstance().gui.screen()`) implements `IDataReceiver`, call `receive(...)` with the client player's registry access. A `DataPacket` arriving when no such screen is open is dropped.
5. (server) `handleTermServer(OpenTerminalPacket, ctx)`: enqueue to main thread; `PlayerInvUtil.findItem(sender, stack → stack's item is a WirelessTerminal and item.canOpen(stack), def EMPTY, identity)` — searches main hand, offhand, then all inventory slots, then Curios slots (NF); if found, calls `WirelessTerminal.open(player, stack)` on the item (see 04-wireless `wireless-items.md`). No feedback to the client if no suitable item is found.

### Send helpers

6. (client) `sendDataToServer(CompoundTag)`: wraps in `DataPacket`, sends to server.
7. (server) `sendTo(ServerPlayer, CompoundTag)`: wraps in `DataPacket`, sends to that player.
8. (client) `openTerminal()`: sends an empty `OpenTerminalPacket` to the server.

### Loader differences (Fabric)

Fabric `NetworkHandler` exposes the same three helpers (`sendDataToServer`, `sendTo`, `openTerminal`) implemented with `ClientPlayNetworking.send` / `ServerPlayNetworking.send`; packet handling is wired in `Fabric/StorageMod`. No other behavior differences.

## Deviations in the new mod

See the [`network` row of the new-spec 01-core deviation table](../../new-spec/01-core/deviations.md#deviation-table).
