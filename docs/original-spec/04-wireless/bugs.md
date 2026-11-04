# Wireless · suspected bugs

Part of [04-wireless](index.md).

## Suspected bugs

| ID | Title | Location | What looks wrong | Severity |
|---|---|---|---|---|
| WIRE-01 | Basic wireless item force-returns SUCCESS_SERVER | `NeoForge/src/platform-shared/java/com/tom/storagemod/item/WirelessTerminalItem.java:42` | When the raycast target is in `remote_activate`, `use()` returns `SUCCESS_SERVER` even if `state.useWithoutItem` returned `PASS` (nothing actually happened) — the client still consumes the item use, hiding a failed activation | gameplay |
| WIRE-02 | Typo in advanced terminal tooltip | `NeoForge/src/platform-shared/resources/assets/toms_storage/lang/en_us.json:82` | "The teminal's chunk has to be loaded" — misspells "terminal" | cosmetic |
| WIRE-03 | Invalid/unloaded bound terminal not consumed | `NeoForge/src/platform-shared/java/com/tom/storagemod/item/AdvWirelessTerminalItem.java:77` | When the bound block is not in `remote_activate` or its chunk is unloaded, the server only shows an action-bar message and the use falls through to `PASS` (item use not consumed), while the client side already returned `CONSUME` — inconsistent consumption between sides, and no way to distinguish "did nothing" from a real activation | gameplay |
| WIRE-04 | Beacon rescan phase can never fire | `NeoForge/src/platform-shared/java/com/tom/storagemod/block/entity/StorageTerminalBlockEntity.java:148` | The scan phase is `getGameTime() % 40 == Math.abs(worldPosition.hashCode()) % 40`; if `hashCode() == Integer.MIN_VALUE`, `Math.abs` stays negative, so the right-hand side is negative while the left is always ≥ 0 — that terminal's `beaconLevel` never updates from 0 (beacon access silently dead) | gameplay |
| WIRE-05 | `bound_pos` codec field name inconsistent | `NeoForge/src/platform-shared/java/com/tom/storagemod/components/WorldPos.java:18` | The component is registered as `bound_pos` but the codec field for the position is `bounds_pos` (extra "s") — a typo in the persisted field name; harmless within this mod but will look wrong in any external NBT inspection | cosmetic |

## Deviations in the new mod

See [new-spec 04-wireless](../../new-spec/04-wireless/index.md) (row: index).
