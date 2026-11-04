package com.tuxprogrammer.simplestorage.inventory;
// Ported from Tom's Simple Storage (MIT):
// https://github.com/tom5454/Toms-Storage/blob/95bb61d/NeoForge/src/platform-shared/java/com/tom/storagemod/inventory/MultiInventoryAccess.java
// Avoids: CORE-03, CORE-04, TERM-12

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

/**
 * A priority-ordered set of inventories that behaves like one inventory.
 *
 * <p>
 * This is the plain-Java port of the upstream
 * {@code MultiInventoryAccess}: a network of connected inventories, each
 * carrying a priority, that the terminal reads and writes as a single unit.
 * Every operation walks the entries in priority order, lowest priority value
 * first (upstream's build order: its {@code build} iterates the priority
 * enum highest index first, which is the lowest-priority inventories first),
 * and equal priorities keep their insertion order (stable).
 *
 * <p>
 * The inventories themselves are abstracted behind {@link Slots}: a raw
 * read/write view of one inventory, so the logic is plain Java and the
 * tests can use fake inventories.
 *
 * <p>
 * {@link #changeCounter()} counts every successful {@link #insert},
 * {@link #extract} and {@link #remove}: any other consumer can compare the
 * value before and after a batch of operations to see whether the network
 * changed, the plain-Java counterpart of upstream's change trackers.
 *
 * <p>
 * Side: both (plain Java).
 */
public final class MultiInventory {

    /**
     * The raw view of one inventory.
     *
     * <p>
     * Slot indices are local to the inventory, from 0 to
     * {@link #size()}-1; {@link MultiInventory} maps them to global indices
     * with {@link MultiInventory#slotOffset(int)}.
     */
    public interface Slots {

        /** Returns how many slots the inventory has (0 when it is empty). */
        int size();

        /** Returns the key of the stack in the slot, or null if the slot is empty. */
        String keyAt(int slot);

        /** Returns the count in the slot (0 when the slot is empty). */
        int countAt(int slot);

        /** Returns the maximum stack size of the slot (0 for an unusable slot). */
        int limitAt(int slot);

        /**
         * Inserts up to {@code amount} items of {@code key} into the slot,
         * respecting the slot's limit and any stack of the same key already
         * there.
         *
         * @param slot       the local slot index
         * @param key        the item type key
         * @param amount     how many items to insert
         * @param stackLimit the network-wide stack limit for this item type
         * @return how many items were NOT inserted
         */
        int insert(int slot, String key, int amount, int stackLimit);

        /**
         * Removes up to {@code amount} items from the slot.
         *
         * @param slot   the local slot index
         * @param amount how many items to remove
         * @return how many items were removed
         */
        int extract(int slot, int amount);
    }

    /**
     * One connected inventory: its slot view, its priority, and the options
     * that change how the multi-inventory reads from and writes to it.
     */
    public static final class Entry {

        /** The slot view of the inventory. */
        public final Slots slots;

        /**
         * The priority of the inventory. Lower values are processed first:
         * extraction takes from the lowest-priority inventories first, and
         * insertion fills them first.
         */
        public final int priority;

        /**
         * The filter of the inventory, or null to accept every item type.
         * A null filter accepts all; a non-null filter rejects any key it
         * does not accept.
         */
        public final Predicate<String> filter;

        /**
         * Whether extraction from this inventory keeps the last item of each
         * type: when true, an extraction never removes the final item of a
         * key from this inventory.
         */
        public final boolean keepLast;

        /**
         * Creates an entry.
         *
         * @param slots    the slot view of the inventory
         * @param priority the priority; lower is processed first
         * @param filter   the item filter, or null to accept all
         * @param keepLast whether extraction keeps the last of each type
         */
        public Entry(Slots slots, int priority, Predicate<String> filter, boolean keepLast) {
            this.slots = slots;
            this.priority = priority;
            this.filter = filter;
            this.keepLast = keepLast;
        }
    }

    private final List<Entry> entries = new ArrayList<>();
    private long changeCounter;

    /** Creates an empty multi-inventory with no entries. */
    public MultiInventory() {}

    /**
     * Creates a multi-inventory from the given entries.
     *
     * @param entries the entries, in the order to keep when priorities tie
     */
    public MultiInventory(Entry... entries) {
        for (Entry entry : entries) {
            add(entry);
        }
    }

    /**
     * Adds an entry, keeping the list sorted by priority (stable: equal
     * priorities keep their relative order).
     *
     * @param entry the entry to add
     */
    public void add(Entry entry) {
        int index = entries.size();
        for (int i = 0; i < entries.size(); i++) {
            if (entry.priority < entries.get(i).priority) {
                index = i;
                break;
            }
        }
        entries.add(index, entry);
    }

    /**
     * Returns the number of entries in the multi-inventory.
     */
    public int entryCount() {
        return entries.size();
    }

    /**
     * Returns the merged contents: one {@link StoredStack} per item key,
     * with the count summed over every entry.
     *
     * <p>
     * The order is deterministic: first appearance while walking the
     * entries in priority order, so the same contents give the same list
     * every time.
     *
     * @return the merged stacks, in first-appearance order
     */
    public List<StoredStack> contents() {
        Map<String, Long> totals = new LinkedHashMap<>();
        for (Entry entry : entries) {
            Slots slots = entry.slots;
            for (int i = 0; i < slots.size(); i++) {
                String key = slots.keyAt(i);
                if (key == null) {
                    continue;
                }
                // A non-null filter hides the keys it does not accept: the
                // entry contributes nothing to the merged contents for them.
                if (!accepts(entry, key)) {
                    continue;
                }
                Long count = totals.get(key);
                if (count == null) {
                    totals.put(key, (long) slots.countAt(i));
                } else {
                    totals.put(key, count + (long) slots.countAt(i));
                }
            }
        }
        List<StoredStack> out = new ArrayList<>(totals.size());
        for (Map.Entry<String, Long> e : totals.entrySet()) {
            out.add(new StoredStack(e.getKey(), e.getValue()));
        }
        return out;
    }

    /**
     * Inserts items into the multi-inventory, filling entries in priority
     * order (lowest priority first).
     *
     * <p>
     * Each entry's filter decides whether it accepts the key: an entry with
     * a filter that rejects the key is skipped. Entries with a size of 0 are
     * skipped too, so an empty inventory in the list costs nothing.
     *
     * @param key        the item type key
     * @param amount     how many items to insert
     * @param stackLimit the per-stack limit used by the underlying inventories
     * @return how many items were NOT inserted
     */
    public int insert(String key, int amount, int stackLimit) {
        if (amount <= 0) {
            return amount;
        }
        int remaining = amount;
        boolean changed = false;
        for (Entry entry : entries) {
            if (remaining == 0) {
                break;
            }
            if (!accepts(entry, key)) {
                continue;
            }
            Slots slots = entry.slots;
            if (slots.size() == 0) {
                continue;
            }
            for (int i = 0; i < slots.size() && remaining > 0; i++) {
                int before = remaining;
                remaining = slots.insert(i, key, remaining, stackLimit);
                if (remaining < before) {
                    changed = true;
                }
            }
        }
        if (changed) {
            changeCounter++;
        }
        return remaining;
    }

    /**
     * Extracts items of a key from the multi-inventory, taking from entries
     * in priority order (lowest priority first).
     *
     * <p>
     * An entry's filter rejects a key whose items are not taken from it.
     * When the entry's keepLast is set, extraction never removes the final
     * item of a key from that entry: the cap per slot is
     * {@code min(amount, count - 1)}.
     *
     * @param key    the item type key
     * @param amount how many items to extract
     * @return how many items were extracted
     */
    public int extract(String key, int amount) {
        if (amount <= 0) {
            return 0;
        }
        int extracted = 0;
        boolean changed = false;
        for (Entry entry : entries) {
            if (extracted == amount) {
                break;
            }
            if (!accepts(entry, key)) {
                continue;
            }
            Slots slots = entry.slots;
            if (slots.size() == 0) {
                continue;
            }
            int before = extracted;
            // keepLast caps the whole entry at count - 1 of the key in total
            // (never the last item), across every slot holding the key.
            int keepLastCap = entry.keepLast ? totalOf(slots, key) - 1 : Integer.MAX_VALUE;
            for (int i = 0; i < slots.size() && extracted < amount; i++) {
                if (!key.equals(slots.keyAt(i))) {
                    continue;
                }
                int count = slots.countAt(i);
                if (count <= 0) {
                    continue;
                }
                // CORE-04: the cap is only passed on when it is positive, so a
                // keepLast entry never forwards a negative amount.
                int cap = Math.min(amount - extracted, count);
                if (entry.keepLast) {
                    cap = Math.min(cap, keepLastCap);
                }
                if (cap <= 0) {
                    continue;
                }
                extracted += slots.extract(i, cap);
            }
            if (extracted > before) {
                changed = true;
            }
        }
        if (changed) {
            changeCounter++;
        }
        return extracted;
    }

    /**
     * Returns the first global slot index of the entry at the given index in
     * the priority-ordered list.
     *
     * <p>
     * The offset is recomputed from the current list on every call, so it
     * stays correct when an entry is removed or when an entry's size is 0
     * (its slots simply do not advance the offset).
     *
     * @param entryIndex the index into the priority-ordered entry list
     * @return the first global slot index of that entry
     */
    public int slotOffset(int entryIndex) {
        int offset = 0;
        for (int i = 0; i < entryIndex && i < entries.size(); i++) {
            offset += entries.get(i).slots.size();
        }
        return offset;
    }

    /**
     * Removes the entry at the given index in the priority-ordered list and
     * recomputes the offsets.
     *
     * <p>
     * The offsets are derived from the current list, so the removal shifts
     * every later offset by the removed entry's slot count, keeping the
     * global slot numbering contiguous.
     *
     * @param entryIndex the index to remove
     */
    public void remove(int entryIndex) {
        entries.remove(entryIndex);
        changeCounter++;
    }

    /**
     * Returns the total number of slots across all entries.
     *
     * @return the summed slot count
     */
    public int totalSlots() {
        int total = 0;
        for (Entry entry : entries) {
            total += entry.slots.size();
        }
        return total;
    }

    /**
     * Returns the change counter.
     *
     * <p>
     * It is incremented once for every successful {@link #insert} (an entry
     * actually took items), every {@link #extract} (an entry actually gave
     * up items) and every {@link #remove}. Comparing the value before and
     * after a batch of operations tells whether the network changed.
     *
     * @return the change counter
     */
    public long changeCounter() {
        return changeCounter;
    }

    /** Whether the entry accepts the key (a null filter accepts all). */
    private static boolean accepts(Entry entry, String key) {
        return entry.filter == null || entry.filter.test(key);
    }

    /**
     * Total count of a key across every slot of the inventory.
     *
     * @param slots the slot view
     * @param key   the item type key
     * @return the summed count
     */
    private static int totalOf(Slots slots, String key) {
        int total = 0;
        for (int i = 0; i < slots.size(); i++) {
            if (key.equals(slots.keyAt(i))) {
                total += slots.countAt(i);
            }
        }
        return total;
    }
}
