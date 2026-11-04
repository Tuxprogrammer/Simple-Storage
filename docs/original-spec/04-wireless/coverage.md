# Wireless · coverage and limits

Part of [04-wireless](index.md).

## Coverage

- Source files read: 3 of 3 in scope (`SH/item/WirelessTerminalItem.java`, `SH/item/WirelessTerminal.java`, `SH/item/AdvWirelessTerminalItem.java`); everything else the feature touches is owned by other areas and was read out-of-scope for cross-links (below)
- Textures / sounds / lang keys covered: 2 textures confirmed present (contents not inspected — 06-client-ui territory), 0 sounds / 18 lang keys (table above)
- Corrections made to earlier statements: none (first writing)

### Files read out of scope (for cross-links)

- `SH/block/entity/StorageTerminalBlockEntity.java` (beacon scan, `canInteractWith`, NBT)
- `SH/components/WorldPos.java` (binding codec fields)
- `SH/client/ClientUtil.java` (tooltip helper, terminal outline raytrace)
- `SH/resources/data/toms_storage/tags/block/remote_activate.json`, `recipe/wireless_terminal.json`, `recipe/adv_wireless_terminal.json`
- `SH/resources/assets/toms_storage/lang/en_us.json` (in-scope keys)
- On 1.15: `src/main/java/com/tom/storagemod/item/ItemWirelessTerminal.java`; On 1.16: `src/main/java/com/tom/storagemod/item/ItemAdvWirelessTerminal.java`

## Known limits

- No MC instance was run: reach math, beacon behavior and the keybind flow are *inferred* from source at `95bb61d`.
- The terminal screen's beacon tooltip rendering (step 9 in range-and-sync.md) was read from `AbstractStorageTerminalScreen` lines 443–444 (06-client-ui territory) — the full screen behavior is not covered here.
- The `remote_activate` tag file was checked (2 values); third-party mods may add blocks to it — the behavior contract is "any block in the tag".
- Fabric item behavior was not read separately: the items are SH classes, so both loaders share the logic; only `Platform.checkUse` and the Curios/trinkets fallback differ (01-core platform-layer.md).
