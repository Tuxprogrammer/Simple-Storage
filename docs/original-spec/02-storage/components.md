# Item Data Components

## Identity
`ConfiguratorComponent` (SH), `SimpleItemFilterComponent` (SH), `TagFilterComponent` (SH), `WorldPos` (SH). Codec-serialized data components carried on items (01-core `Content` registry).

## Inputs
None — pure data records with codecs.

## Numbers
- `ConfiguratorComponent` debounce window: `actionTime - lastAction < 3` (ticks) — `Configurator.debounce()`.

## Behavior
1. `ConfiguratorComponent(bound, isBound, selecting, showInvBox, massSelect, boxStart, selection, lastAction)` — the inventory configurator item's state: bound connector position, bind flag, selection mode, show-inventory-box flag, mass-select flag + box start corner, selected positions, last action time.
   - Codec fields: `bound` (BlockPos), `is_bound`, `selecting`, `show_inv_box`, `mass_select` (bools), `box_start` (BlockPos), `selection` (BlockPos list), `last_action` (long).
   - `EMPTY` constant: all-zero/false.
   - Mutators return new records: `hiddenItem`, `showInv`, `massSelectStart`, `massSelectEnd` (×2), `setSelection` — all stamp `lastAction` with the caller's `actionTime`.
   - `Configurator` helper class: wraps the item stack + component; `debounce()` rejects actions within 3 ticks of the last; `startSelection(bind, sel)` sets bound+selecting; `clear()` resets to EMPTY; `set` writes the component back onto the stack.
2. `SimpleItemFilterComponent(stacks, matchComp, allowList)` — the item filter's payload: up to 9 item stacks, match-components flag, allow/deny flag. Codec fields: `stacks` (list of optional item stacks), `match_component`, `allow_list`.
3. `TagFilterComponent(tags, allowList)` — the tag filter's payload: item tag keys, allow/deny flag. Codec fields: `tags` (list of item `TagKey`), `allow_list`.
4. `WorldPos(dim, pos)` — a dimension-keyed position (used by `RemoteConnections` channel connectors and configurator bound references). Codec fields: `bound_dim` (dimension `ResourceKey`), `bounds_pos` (BlockPos). `getBlockEntity(ServerLevel)` resolves the level, checks `isLoaded(pos)`, returns the BE (or null).

## Determinism
Deterministic (pure data).

## Data formats
Codec NBT as listed above; all component codecs are registered in 01-core (`Content.configuratorComponent`, `simpleItemFilterComponent`, `tagFilterComponent`, and the paint component).

## Output
Consumed by: inventory configurator GUI (03-terminal `inventory-configurator.md`), item/tag filter GUIs (03-terminal `item-filter-gui.md`), `RemoteConnections` (this area `cable-network.md`).

## Relationships
- 01-core: `Content` component registry, codec registration.
- 03-terminal: the filter GUIs and configurator that read/write these.

## NeoForge dependencies
Data component system (NeoForge item components), `RecordCodecBuilder`, `TagKey.codec(Registries.ITEM)`.

## Bugs
None.
