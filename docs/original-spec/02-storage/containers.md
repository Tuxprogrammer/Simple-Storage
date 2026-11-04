# Utility Containers

## Identity
`com.tom.storagemod.util.BasicContainer` (SH) + `LimitedContainer` (SH). (`FilingCabinetContainer` is documented in `filing-cabinet.md`.)

Sources (upstream 26.2@95bb61d): `NeoForge/src/platform-shared/java/com/tom/storagemod/util/{BasicContainer,LimitedContainer}.java`; short names below are `Class.java:line` in those files (class declarations: BasicContainer.java:13, LimitedContainer.java:12).

## Inputs
- `BasicContainer(int size)` / `BasicContainer(ItemStack...)` — same constructors as `SimpleContainer` plus listener support.
- `LimitedContainer(delegate, sizeLimit)` — a view over another container with an optional `startOffset` and `sizeLimit` (LimitedContainer.java:17).

## Numbers
- `LimitedContainer.getContainerSize() = min(sizeLimit, delegate.getContainerSize() - startOffset)` — can go **negative** if `startOffset > delegate size` (observed, no clamp) (LimitedContainer.java:28-29).

## Behavior
1. `BasicContainer` (extends `SimpleContainer`):
   - `addListener` / `removeListener` (lazy list) (BasicContainer.java:24, 32).
   - `setChanged()` → notifies all listeners via `ContainerListener.containerChanged(this)` (BasicContainer.java:39).
   - `loadItems(ValueInput.TypedInputList<ItemStackWithSlot>)`: clears all slots, then sets each entry whose `slot()` is valid in this container (BasicContainer.java:47-56).
   - `storeItems(ValueOutput.TypedOutputList<ItemStackWithSlot>)`: appends `(slot, stack)` for every non-empty slot (BasicContainer.java:59).
2. `LimitedContainer` (implements `Container`, delegates everything to `delegate` with `startOffset` added to slot indices):
   - `getContainerSize` as above; `isEmpty`/`countItem`/`hasAnyOf`/`hasAnyMatching` delegate **without** offset/limit semantics (they reflect the whole delegate, observed) (LimitedContainer.java:33-34, 88-89, 93-94, 98-99).
   - `setSizeLimit` / `setStartOffset` (LimitedContainer.java:102, 106) mutators (used by windowed GUIs, e.g. the filing cabinet menu — 03-terminal `filing-cabinet-gui.md`).

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

## Deviations in the new mod

See [new-spec 02-storage](../../new-spec/02-storage/index.md) (row: containers).
