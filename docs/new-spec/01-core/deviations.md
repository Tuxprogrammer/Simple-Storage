# 01-core · deviations

Part of [01-core](index.md).

## Sheets

| Sheet | Contents |
|---|---|
| [config.md](config.md) | Upstream config key to `simplestoragenh.cfg` key, with category, default, range, side/sync, status |

## How to read the table

- **Original**: the original-spec sheet.
- **Here**: `same`, `changed`, `not ported` (by decision; the upstream code remains at the 26.2 branch), `deferred`, `new`.
- **Decision**: an Accepted decision-log row (D#) or design record (000N); `P01-n` points to an [Accepted proposal](proposals.md#proposed-not-decided).
- 1.7.10 facts about other mods cite a file in [00-pack-integration](../00-pack-integration/) or [API-MAP.md](../../original-spec/API-MAP.md); otherwise they say "unverified".

## Deviation table

<a id="deviation-table"></a>

| Original | Here | What changes | Decision | Bugs |
|---|---|---|---|---|
| [index](../../original-spec/01-core/index.md) | same | Area index; no behavior | D6 | all CORE-xx (see rows) |
| [config](../../original-spec/01-core/config.md) | changed | Two toml specs (`toms_storage-common/-server.toml`, ModConfigSpec) become one `config/simplestoragenh.cfg` via GTNHLib 0.11.54 `@Config`; per-world values via `@Config.Sync`; adds `modules.*` and `euSink.*` keys; auto config screen from GTNHLib `SimpleGuiFactory` (`machine-model.md` § Config). Full table in [config.md](config.md). `importantInfo` and `inventoryConnectorMaxSlots` dropped (config.md). CORE-09 (0 before sync) re-tested at M01 | D16, D22 | CORE-01, CORE-09 |
| [mod-lifecycle](../../original-spec/01-core/mod-lifecycle.md) | changed | `@Mod("toms_storage")` plus `DeferredRegister` becomes a `@Mod` class for mod id `simplestoragenh` calling `GameRegistry.registerBlock/registerItem/registerTileEntity` in pre-init (API-MAP; machine-model.md). All content registered regardless of config (non-negotiable 6). Capability registration is not ported by decision (no capabilities in 1.7.10, see platform-layer). Menu types become `IGuiHandler` ids. NeoForge key-mapping, GUI-layer and model events become client proxy calls. Creative tab id `toms_storage:tab` becomes a `CreativeTabs` instance | D9, D16 | CORE-01, CORE-08 |
| [network](../../original-spec/01-core/network.md) | changed | Two payloads (`toms_storage:data` NBT compound, bidirectional; `toms_storage:open_terminal`, empty) become packets on a 1.7.10 channel (`PacketSimple`/`PacketCustom`, API-MAP). Handler runs the server side with the player's open-container check, never trusting a client (non-negotiable 7). Library: GTNHLib channel classes; NBT bounded by a per-packet decode limit and a fixed schema per type id (confirmed in M01) | Accepted P01-1, P01-2 (D29) | CORE-02 |
| [platform-layer](../../original-spec/01-core/platform-layer.md) | not ported | The NF/Fabric `Platform` abstraction (`GameObject`, `PlatformBlockEntity`, `PlatformItemHandler`, attachments, Curios, `ResourceHandler<ItemResource>`) has no 1.7.10 need: one loader. Registration is direct `GameRegistry`; tile-entity data is plain `TileEntity` NBT | D9 | none |
| [inventory-access](../../original-spec/01-core/inventory-access.md) | changed | `BlockCapabilityCache` over `ResourceHandler<ItemResource>` becomes resolution `ISidedInventory` then `IInventory` then AE2 (optional, off at alpha) then `EMPTY`; proxy-chain and cycle guard kept. `PolyFilter` refresh every 10 ticks and `InventoryChangeTracker` kept in intent. CORE-03 (compaction index) and CORE-04 (negative extract amount with keepLast) fixed in the rewrite, each with a test | D10, D14 | CORE-03, CORE-04 |
| [terminal-sync](../../original-spec/01-core/terminal-sync.md) | changed | Stack-id tables, wire flags, and the >32000-stack batching are kept; `RegistryFriendlyByteBuf`/`ItemStack.STREAM_CODEC` become `ByteBuf` plus 1.7.10 `ItemStack` NBT write. Upstream's 64000-byte cap is split into several packets under a conservative per-packet size (confirmed in M01/M03) | Accepted P01-1 (D29) | none (CORE-02 shares the carrier) |
| [util-api](../../original-spec/01-core/util-api.md) | changed | `EventHandler<T>` and `MultiblockInventoryAPI` kept as plain Java. `PolymorphHelper` and `PolymorphTerminalWidget` not ported by decision: Polymorph is not in the pack (API-MAP lists no counterpart) | none needed | CORE-08 |
| [util-containers](../../original-spec/01-core/util-containers.md) | changed | `BasicContainer`, `LimitedContainer`, `CraftingMatrix` become `IInventory` implementations (`InventoryBasic`, `InventoryCrafting` base). `FilingCabinetContainer`: CORE-05 (first item must be single-stack) and CORE-06 (max stack 1) are bugs not to return; any first item is accepted and automation sees real stack sizes (confirmed in M02) | Accepted P01-7 (D29) | CORE-05, CORE-06 |
| [util-filters](../../original-spec/01-core/util-filters.md) | changed | 9-slot `AbstractSimpleItemFilter` and `AbstractTagFilter` keep behavior; data components become item NBT (the sheet itself names this); readers tolerate missing keys, writers forward-compatible (non-negotiable 3) | none needed (rule) | none |
| [util-data-slots](../../original-spec/01-core/util-data-slots.md) | changed | `IntDataSlots` (mask 0xFFFF, 2 slots per int) maps to `Container.detectAndSendChanges` with `ICrafting.sendProgressBarUpdate`; the 16-bit limit is the same in 1.7.10 (unverified: no doc in 00-pack-integration cites it) | none needed | none |
| [util-misc](../../original-spec/01-core/util-misc.md) | changed | `NumberFormatUtil` (base 1000, "KMGTPE", 4-char width, round down) kept as-is. `Priority` (5 levels) kept. `BeaconLevelCalc` (levels 1-4) re-based on `TileEntityBeacon` levels and 1.7.10 pyramid blocks (iron, gold, emerald, diamond), confirmed against Forge 10.13.4.1614 source in M04. `KeyUtil` uses LWJGL `Keyboard` and checks left and right Ctrl/Shift/Alt (fixes CORE-10). `TickerUtil`/`WorldStates` simplified to `TileEntity.updateEntity` on the server and a `WorldSavedData`/event keyed map. CORE-07 (missing text-field tooltip key) is fixed by adding the key; the 06 row `lang` owns it | Accepted P01-6 (D29, BeaconLevelCalc); CORE-10 fix needs no decision | CORE-07, CORE-10 |
