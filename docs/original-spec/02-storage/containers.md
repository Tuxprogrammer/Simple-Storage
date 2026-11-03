# Utility Containers

## Identity
`com.tom.storagemod.util.BasicContainer` (SH) + `LimitedContainer` (SH). (`FilingCabinetContainer` is documented in `filing-cabinet.md`.)

## Inputs
- `BasicContainer(int size)` / `BasicContainer(ItemStack...)` — same constructors as `SimpleContainer` plus listener support.
- `LimitedContainer(delegate, sizeLimit)` — a view over another container with an optional `startOffset` and `sizeLimit`.

## Numbers
- `LimitedContainer.getContainerSize() = min(sizeLimit, delegate.getContainerSize() - startOffset)` — can go **negative** if `startOffset > delegate size` (observed, no clamp).

## Behavior
1. `BasicContainer` (extends `SimpleContainer`):
   - `addListener` / `removeListener` (lazy list).
   - `setChanged()` → notifies all listeners via `ContainerListener.containerChanged(this)`.
   - `loadItems(ValueInput.TypedInputList<ItemStackWithSlot>)`: clears all slots, then sets each entry whose `slot()` is valid in this container.
   - `storeItems(ValueOutput.TypedOutputList<ItemStackWithSlot>)`: appends `(slot, stack)` for every non-empty slot.
2. `LimitedContainer` (implements `Container`, delegates everything to `delegate` with `startOffset` added to slot indices):
   - `getContainerSize` as above; `isEmpty`/`countItem`/`hasAnyOf`/`hasAnyMatching` delegate **without** offset/limit semantics (they reflect the whole delegate, observed).
   - `setSizeLimit` / `setStartOffset` mutators (used by windowed GUIs, e.g. the filing cabinet menu — 03-terminal `filing-cabinet-gui.md`).

## Determinism
Deterministic.

## Data formats
`ItemStackWithSlot` typed NBT lists (vanilla container codec format).

## Output
Listener notification on change; windowed/limited views of a container.

## Relationships
- 03-terminal: `FilingCabinetMenu` (windowed view), `AbstractFilteredMenu` (filter slot access).
- 01-core: `SimpleContainer` base.

## NeoForge dependencies
`ValueInput`/`ValueOutput` typed list IO, `ItemStackWithSlot`.

## Bugs
None (the negative-size edge is recorded as observed behavior above).
