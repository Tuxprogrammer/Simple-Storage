# Data slots (container data)

| # | Field | Contents |
|---|---|---|
| 1 | **Identity** | `DataSlots` (SH), `IntDataSlots` (SH). Factories for vanilla `DataSlot`/`ContainerData` backed by lambdas. Sources: `NeoForge/src/platform-shared/java/com/tom/storagemod/util/DataSlots.java`, `util/IntDataSlots.java` (short citations below are relative to these). |
| 2 | **Inputs** | An `IntConsumer` (write target) and/or an `IntSupplier` (read source); an optional `onUpdate` runnable. |
| 3 | **Numbers** | `IntDataSlots`: `MASK = 0xFFFF` (IntDataSlots.java:9) — the supplier value is a 32-bit int split into two 16-bit halves, so one `IntDataSlots` object occupies **2** `ContainerData` slots (`getCount() = 2`) (IntDataSlots.java:31-33); slot 0 = low 16 bits, slot 1 = high 16 bits. (IntDataSlots.java:17-18, 23-24) |
| 4 | **Behavior** | Ordered steps in [Behavior detail](#behavior-detail). |
| 5 | **Determinism** | None. |
| 6 | **Data formats** | None (values ride the menu's data-slot sync, 16 bits per slot for `IntDataSlots`; the slot-1 read is signed). |
| 7 | **Output** | Data slot objects added to menus (03-terminal/06-client-ui) that sync integer state (e.g. sort order, mode flags, beacon level display) between the menu (server) and screen (client). |
| 8 | **Relationships** | Used by terminal menus/screens (03-terminal/06-client-ui). No other users found in 26.2. |
| 9 | **NeoForge dependencies** | None (vanilla `DataSlot`/`ContainerData`). |
| 10 | **Bugs** | None. |

## Behavior detail

### DataSlots

1. (both) `DataSlots.create(consumer, supplier)`: a `DataSlot` whose `get()` returns `supplier.getAsInt()` (DataSlots.java:13-16) and whose `set(v)` calls `consumer.accept(v)` then runs `onUpdate` if set. (DataSlots.java:18-22) `DataSlots.set(consumer)` — write-only (get() always 0); (DataSlots.java:29-31) `DataSlots.get(supplier)` — read-only (set() is a no-op). (DataSlots.java:33-35) `onUpdate(runnable)` chains (returns this) (DataSlots.java:41-44) — used to react to client-side data changes (e.g. re-render a screen after a synced value changes).

### IntDataSlots

2. (both) `IntDataSlots.create(consumer, supplier)` implements `ContainerData` (IntDataSlots.java:8, 35-37) with `getCount() = 2`. `get(off)`: reads `supplier.getAsInt()`, masks the 16-bit half at bit offset `off << 4`, and returns it shifted down with a signed `>>`, so slot 0 reads 0–65535 but slot 1 reads negative (-32768..-1) when bit 31 of the supplier value is set (IntDataSlots.java:16-19). `set(off, val)`: merges `val & 0xFFFF` into the cached `value` at that half (the other half is preserved from the cached `value`), pushes the merged 32-bit int through the consumer, updates the cache, and runs `onUpdate`. (IntDataSlots.java:21-28)
3. (both) Note: the cached `value` starts at 0 (IntDataSlots.java:13) and is only updated via `set` — a value read back from the supplier after a supplier-side change is not reflected in the cache until the next `set` merges it. *Inferred* acceptable because menus write through the consumer on every change; flag for porters: if a supplier changes behind the menu's back, a subsequent `set` of one half would overwrite the other half with the stale cached value.

## Deviations in the new mod

See the [`util-data-slots` row of the new-spec 01-core deviation table](../../new-spec/01-core/deviations.md#deviation-table).
