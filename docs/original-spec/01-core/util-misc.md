# Misc utilities

| # | Field | Contents |
|---|---|---|
| 1 | **Identity** | `BlockFace`, `BlockFaceReference`, `ComponentJoiner`, `KeyUtil`, `NumberFormatUtil`, `PlayerInvUtil`, `PopupMenuManager` (+ `PopupElement`, `ButtonElement`, `TextFieldElement`), `Priority` (+ `IPriority`), `BeaconLevelCalc`, `TickerUtil` (NF/Fabric variants), `WorldStates` (NF/Fabric variants), `FloatSupplier`, `IValidInfo`, `InventoryConnectorConfigUtil`, `Util` (SH, all in `NeoForge/src/platform-shared/java/com/tom/storagemod/util/` except TickerUtil/WorldStates which have NF and Fabric variants). Sources: all short `Class.java:NN` citations below are relative to `NeoForge/src/platform-shared/java/com/tom/storagemod/util/`, except TickerUtil.java/WorldStates.java (NF: `NeoForge/src/main/java/com/tom/storagemod/util/`; Fabric: `Fabric/src/main/java/com/tom/storagemod/util/`). |
| 2 | **Inputs** | Per utility, in [Behavior detail](#behavior-detail). |
| 3 | **Numbers** | `NumberFormatUtil`: division base 1000, (NumberFormatUtil.java:9) postfixes "KMGTPE", (NumberFormatUtil.java:10) display width 4 chars, (NumberFormatUtil.java:23) pattern `".#;0.#"` rounding DOWN. (NumberFormatUtil.java:16, 18) `BeaconLevelCalc`: pyramid levels 1–4 checked downward from the top. (BeaconLevelCalc.java:18-19) `Priority`: 5 levels, sorting value = ordinal - 2 (so LOWEST=-2 … HIGHEST=+2), clamped on add. (Priority.java:49-51, 53-55) `TickerUtil`: none. `InventoryConnectorConfigUtil`: none (reads config + tags). |
| 4 | **Behavior** | Ordered steps in [Behavior detail](#behavior-detail). |
| 5 | **Determinism** | `BeaconLevelCalc` result depends on the beacon pyramid below the beacon and the `BEACON_BASE_BLOCKS` block tag (BeaconLevelCalc.java:18-30) — deterministic for a given world state. No randomness anywhere. |
| 6 | **Data formats** | None (no NBT/config keys of their own). |
| 7 | **Output** | Small value objects, computed beacon levels, ticker factories, tracker/cable-network world state, formatted strings, key-press state, player item lookups, priority ordering, connector-connectivity decisions. |
| 8 | **Relationships** | `BlockFace`/`BlockFaceReference`: filter items and PolyFilter (02-storage/03-terminal, inventory-access.md). `PlayerInvUtil`: NetworkHandler open-terminal (network.md), terminal beacon reach (04-wireless). `Priority`: inventory access priorities (inventory-access.md, 03-terminal). `BeaconLevelCalc`: terminal beacon scan (04-wireless, 03-terminal BE). `TickerUtil`: every block entity with a tick (02/03). `WorldStates`: inventory-access.md and cable network (03-terminal). `IValidInfo`: capability validity (inventory-access.md). `InventoryConnectorConfigUtil`: connector (02-storage). `KeyUtil`: tooltip shift/ctrl detection (ClientUtil, 06-client-ui). `PopupMenuManager`: terminal screen popups (06-client-ui). `ComponentJoiner`: tooltip/component joining (06-client-ui). `Util`: NBT item lists + `reducingWithCopy` collector (terminal sync, util-containers.md). `FloatSupplier`: light-level suppliers (02-storage blocks). |
| 9 | **NeoForge dependencies** | `TickerUtil` (NF): `BlockEntityTicker` factories per side. `WorldStates` (NF): keys trackers by `ResourceHandler<ItemResource>` (NeoForge transfer type). `InventoryConnectorConfigUtil`: `blockState.is(TagKey)` block tags. `KeyUtil`: GLFW + `RenderSystem.isOnRenderThread` (client). (KeyUtil.java:12-13) `BeaconLevelCalc`: `BeaconBlockEntity.getBeamSections()` + `BlockTags.BEACON_BASE_BLOCKS`. (BeaconLevelCalc.java:16, 28) |
| 10 | **Bugs** | `CORE-07` (missing `tooltip.toms_trading_network.textfield` lang key), `CORE-10` (KeyUtil left-modifier only). |

## Behavior detail

### BlockFace / BlockFaceReference

1. (both) `BlockFace(pos, from)`: an immutable block position + the direction the face is viewed from (record). (BlockFace.java:6)
2. (both) `BlockFaceReference(level, pos, from)`: the same plus the level. (BlockFaceReference.java:9) `touching(level, pos, to)` constructs a reference to the block adjacent in direction `to`, with `from` set to the opposite direction (BlockFaceReference.java:15-17) (i.e. "the face of that block looking back at us"). `getBlockEntity()`, `getState()` are convenience lookups. (BlockFaceReference.java:11-13, 19-21)

### ComponentJoiner

3. (both) A stream `Collector<Component, ComponentJoiner, Component>` factory `joining(empty, delimiter)`: joins components with a delimiter component; (ComponentJoiner.java:24-26, 54-66) if nothing was accumulated, the result is the `empty` component. (ComponentJoiner.java:33-35) Used to build multi-line tooltip text from streams (06-client-ui).

### KeyUtil (client)

4. (client) `hasControlDown()` / `hasShiftDown()`: return true only on the render thread (KeyUtil.java:11-13, 17-19) (GLFW key state of the main window) and only for the **left** modifier (`GLFW_KEY_LEFT_CONTROL` / `GLFW_KEY_LEFT_SHIFT`) (KeyUtil.java:13, 19) — see `CORE-10`. Off the render thread they always return false. (KeyUtil.java:14, 20)

### NumberFormatUtil

5. (both) `formatNumber(long)`: for numbers with ≤ 4 digits, returns the plain decimal string. (NumberFormatUtil.java:25-27) Otherwise repeatedly divides by 1000 (NumberFormatUtil.java:34-42) (tracking the last divisor and the exponent, postfix K/M/G/T/P/E) until the result fits 4 characters; then prefers the one-decimal form (NumberFormatUtil.java:44-47) (`format(last/1000) + postfix`, pattern `.#;0.#`, rounding DOWN) if it fits 4 chars, else the integer form (`base + postfix`). `assert number >= 0` (debug-only) (NumberFormatUtil.java:24, 48) and `assert slimResult.length() <= width`. Examples (inferred by tracing): 1234 → "1234"; 12345 → "12K"; (NumberFormatUtil.java:27-47) 999999 → "1000K" is 5 chars so falls back to "999K"; 1500 → "1.5K". (UNVERIFIED: 1500 has 4 digits, so the early return gives "1500"; "1.5K" is not produced for it, NumberFormatUtil.java:26-27)

### PlayerInvUtil

6. (both) `findItem(player, predicate, def, map)`: checks main hand, offhand, then every inventory slot in order; (PlayerInvUtil.java:15-24) the first stack matching the predicate is mapped by `map` and returned; if nothing matches, falls back to `Platform.checkExtraSlots(player, is, def, map)` (PlayerInvUtil.java:25) (Curios/trinkets, platform-layer.md). Used for the open-terminal keybind (network.md) and terminal reach (04-wireless).

### PopupMenuManager (client)

7. (client) A context menu attached to a `Screen`: `open(x, y, elements...)` shows the menu at screen coordinates (int-cast); (PopupMenuManager.java:33-38) `replace(elements...)` swaps the entries keeping the position. (PopupMenuManager.java:40-43) `extract(g, font, mouseX, mouseY)` renders it as a tooltip at `(x-12, y+12)` (PopupMenuManager.java:45-52) — one line per element, 10 px row height, (PopupMenuManager.java:49) 100×8 px hit box per row (with a 1-px grace border); (PopupMenuManager.java:56-58, 63) returns false while the menu is open (PopupMenuManager.java:53) (so the screen defers other handling).
8. (client) `mouseClick`: if any row is hovered (100×8 box at `x, y + i*10`), fires `onClick` (default: `activate()`), sets `selected`, and consumes the click; (PopupMenuManager.java:62-68; PopupElement default onClick :100-102) a click elsewhere closes the menu and consumes the click. (PopupMenuManager.java:69-71)
9. (client) `keyPressed`: while open — if an element is selected, forwards the key to it first; (PopupMenuManager.java:77-79) Down/Up arrows move the selection (wrapping, modulo menu size); (PopupMenuManager.java:80-83) ESC (key 256) closes; (PopupMenuManager.java:84-85) all keys are consumed while the menu is open. (PopupMenuManager.java:87) `charTyped` is forwarded to the selected element while open. (PopupMenuManager.java:92-94)
10. (client) `PopupElement`: `getText()` (MutableComponent), `activate()`; default `onClick` activates; default `keyPressed` activates on Enter/Numpad-Enter; (PopupMenuManager.java:104-111) default `charTyped` returns false; (PopupMenuManager.java:112-114) `getHoveredText` colors the text AQUA when hovered or selected, GRAY otherwise. (PopupMenuManager.java:116-118) `ButtonElement(textSupplier, action)` is a simple entry. (PopupMenuManager.java:121-139) `TextFieldElement(textSupplier, action, font, value)`: first activation opens an inline `EditBox` (focused, cannot lose focus); (PopupMenuManager.java:148-156, 169-175) while active, typing goes to the box and the text shows a yellow value with a `_` cursor marker at the caret position, (PopupMenuManager.java:159-164) rendered through lang key `tooltip.toms_trading_network.textfield` (see `CORE-07`); Enter activates (commits the box value into the action and toggles the edit state off); (PopupMenuManager.java:169-175, 184) ESC while active resets the box to the last committed value and closes editing. (PopupMenuManager.java:178-183)

### Priority

11. (both) Enum `LOWEST, LOW, NORMAL, HIGH, HIGHEST`; (Priority.java:12-18) `getSorting()` = ordinal - 2; (Priority.java:49-51) `add(other)`: NORMAL is identity, otherwise sums sorting values and clamps to [0, 4] via `fromSorting`. (Priority.java:43-47, 53-55) `IPriority`: `getPriority()`; (Priority.java:28-31, nested in Priority) static `get(Object)` returns NORMAL for non-implementors; (Priority.java:37-40) `compare()` gives a `Comparator` by priority (Priority.java:33-35) (used to order inventories when multiple can receive an item, 03-terminal). `CODEC`: string-serializable (lowercase names). (Priority.java:22-26, 58-60)

### BeaconLevelCalc

12. (server) `calcBeaconLevel(world, x, y, z)`: if the block entity at (x,y,z) is a beacon: return 0 if its beam sections list is empty (BeaconLevelCalc.java:15-16) (a beacon with no active effect has no beam); otherwise for pyramid level `j` = 1..4 (top layer first): the layer at `y - j` (BeaconLevelCalc.java:18-19) must be a solid `(2j-1) x (2j-1) (UNVERIFIED: the scan covers x-j..x+j by z-j..z+j, i.e. (2j+1) x (2j+1) per layer, and breaks below the world min Y, BeaconLevelCalc.java:20-22, 26-28)` square of blocks in `BlockTags.BEACON_BASE_BLOCKS` (diamond, emerald, lapis, gold blocks, nether star per the vanilla tag); the first failing layer stops the loop; (BeaconLevelCalc.java:35-37) the last fully-valid `j` is the level (0–4). (BeaconLevelCalc.java:18, 40) Mirrors vanilla beacon pyramid rules. *Inferred*: matches vanilla `BeaconBlockEntity` semantics.

### TickerUtil (NF and Fabric, identical core)

13. (both) `createTicker(world, client, server)`: returns a `BlockEntityTicker` that calls `updateClient()` (TickerUtil.java:9-11 NF and Fabric) on client-side block entities when `client` is true, `updateServer()` on the server when `server` is true, else null. `TickableClient`/`TickableServer` are the marker interfaces (NF TickerUtil.java:13-19) block entities implement. Fabric additionally defines `OnLoadListener` (Fabric TickerUtil.java:17-19) (an `onLoad()` hook) implemented by `PlatformBlockEntity`.

### WorldStates (NF and Fabric)

14. (both) Static per-world state: `trackers` — a map from item handler → its `InventoryChangeTracker` (NF: `WeakHashMap`, handler as key, (NF WorldStates.java:17) tracker holds the handler weakly; Fabric: weak keys **and** weak values, (Fabric WorldStates.java:18) and `getTracker` re-runs `refresh(handler)` after lookup). (Fabric WorldStates.java:28-31) `cableNetworks` — `Level → InventoryCableNetwork` (WorldStates.java:18 NF, :19 Fabric) (the cable network per dimension, 03-terminal). `clearWorldStates()` empties both (NF WorldStates.java:20-23) (world unload, *inferred* call site in the cable network). `getTracker(h)`: if the handler itself is an `IChangeTrackerAccess`, use its own tracker; else create/reuse one. (NF WorldStates.java:25-28)

### FloatSupplier / IValidInfo

15. (both) `FloatSupplier`: `float getAsFloat()` (FloatSupplier.java:5) — used for block light-level suppliers (e.g. terminals emit 6, mod-lifecycle.md). `IValidInfo`: `boolean isObjectValid()` (IValidInfo.java:4) — the validity predicate for capability caches (implemented by `PlatformBlockEntity` as `!isRemoved()`).

### InventoryConnectorConfigUtil

16. (both) `canConnect(blockState)`: false if the block is in tag `toms_storage:inventory_connector_skip`; (InventoryConnectorConfigUtil.java:10) false if the block is in `Config.getBlockedBlocks()`; (InventoryConnectorConfigUtil.java:12-13) false if its registry namespace is in `Config.getBlockedMods()` (only checked when the blocked-mods set is non-empty). (InventoryConnectorConfigUtil.java:14-18) Otherwise true. (Used by the inventory connector scan, 02-storage.)

### Util

17. (both) `stream(iterator)`: a sequential stream over an iterator. (Util.java:27-31) `reducingWithCopy(identity, op, copier)`: a stateless-ish collector that folds with `op` (Util.java:33-40) and returns a **copy** via `copier` on finish (used by `TerminalItemStack` merging in the terminal BE, 03-terminal, to avoid aliasing merged results). `loadItems(container, id, nbt)` / `storeItems(container, id, nbt)`: the shared NBT item-list load/store (Util.java:62-85) (see util-containers.md step 2) — identical semantics to `BasicContainer`'s own methods (code duplication, *inferred* harmless).

## Loader differences (Fabric)

`TickerUtil` (Fabric) adds the `OnLoadListener` interface. `WorldStates` (Fabric) keys trackers by `Storage<ItemVariant>` with weak values and refreshes on lookup. Everything else is loader-agnostic SH code.

## Deviations in the new mod

See the [`util-misc` row of the new-spec 01-core deviation table](../../new-spec/01-core/deviations.md#deviation-table).
