package com.tuxprogrammer.simplestorage.inventory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

import org.junit.jupiter.api.Test;

import com.tuxprogrammer.simplestorage.inventory.MultiInventory.Entry;
import com.tuxprogrammer.simplestorage.inventory.MultiInventory.Slots;

/**
 * Tests for {@link MultiInventory} and {@link StoredStack}: priority order,
 * merged contents, filters, keepLast, offsets through compaction, and the
 * change counter.
 */
public class MultiInventoryTest {

    /**
     * A small fake {@link Slots} backed by parallel arrays, for the tests.
     */
    static final class FakeSlots implements Slots {

        final String[] keys;
        final int[] counts;
        final int[] limits;
        final int size;

        /**
         * Records every amount passed to extract, so a test can assert a
         * negative amount never reached the underlying inventory.
         */
        final List<Integer> extractedAmounts = new ArrayList<>();

        /**
         * Records every amount passed to insert, so a test can assert a
         * negative amount never reached the underlying inventory.
         */
        final List<Integer> insertedAmounts = new ArrayList<>();

        /**
         * Creates a fake inventory.
         *
         * @param size   the slot count
         * @param limits the per-slot stack limits
         */
        FakeSlots(int size, int... limits) {
            this.size = size;
            this.keys = new String[size];
            this.counts = new int[size];
            this.limits = limits.length == 0 ? new int[size] : limits;
        }

        /**
         * Fills the fake with one stack per slot.
         *
         * @param itemKeys   the per-slot item strings, null for empty slots
         * @param itemCounts the per-slot counts
         */
        FakeSlots with(String[] itemKeys, int[] itemCounts) {
            for (int i = 0; i < size; i++) {
                keys[i] = itemKeys[i];
                counts[i] = itemCounts[i];
            }
            return this;
        }

        @Override
        public int size() {
            return size;
        }

        @Override
        public String keyAt(int slot) {
            return keys[slot];
        }

        @Override
        public int countAt(int slot) {
            return counts[slot];
        }

        @Override
        public int limitAt(int slot) {
            return limits[slot];
        }

        @Override
        public int insert(int slot, String key, int amount, int stackLimit) {
            insertedAmounts.add(amount);
            if (amount < 0) {
                throw new AssertionError("negative insert amount: " + amount);
            }
            int space = limits[slot];
            if (key != null && key.equals(keys[slot])) {
                space -= counts[slot];
            }
            int placed = Math.max(0, Math.min(amount, Math.min(space, stackLimit)));
            keys[slot] = key;
            counts[slot] += placed;
            return amount - placed;
        }

        @Override
        public int extract(int slot, int amount) {
            extractedAmounts.add(amount);
            if (amount < 0) {
                throw new AssertionError("negative extract amount: " + amount);
            }
            int taken = Math.max(0, Math.min(amount, counts[slot]));
            counts[slot] -= taken;
            if (counts[slot] == 0) {
                keys[slot] = null;
            }
            return taken;
        }
    }

    /** T5 (CORE-03): the offsets track the priority-ordered entries through removals. */
    @Test
    public void doesNotReproduceCORE03_offsetsStayCorrectThroughCompaction() {
        FakeSlots three = new FakeSlots(3, 64, 64, 64);
        FakeSlots empty = new FakeSlots(0);
        FakeSlots two = new FakeSlots(2, 64, 64);
        MultiInventory multi = new MultiInventory(
            new Entry(three, 1, null, false),
            new Entry(empty, 2, null, false),
            new Entry(two, 3, null, false));

        assertEquals(0, multi.slotOffset(0));
        assertEquals(3, multi.slotOffset(1));
        assertEquals(3, multi.slotOffset(2));
        assertEquals(5, multi.totalSlots());

        multi.remove(1);
        assertEquals(2, multi.entryCount());
        assertEquals(3, multi.slotOffset(1));
        assertEquals(5, multi.totalSlots());

        multi.remove(0);
        assertEquals(0, multi.slotOffset(0));
        assertEquals(2, multi.totalSlots());
    }

    /** T2 (CORE-04): keepLast caps each key at count - 1 per entry, and amounts <= 0 are no-ops the fake never sees. */
    @Test
    public void doesNotReproduceCORE04_keepLastNeverPassesANegativeAmount() {
        FakeSlots slots = new FakeSlots(2, 64, 64).with(new String[] { "a", "b" }, new int[] { 5, 3 });
        MultiInventory multi = new MultiInventory(new Entry(slots, 1, null, true));

        // keepLast: extraction never takes the last item of a key, so at most
        // 4 of the 5 "a" and at most 2 of the 3 "b" can leave.
        assertEquals(4, multi.extract("a", 5));
        assertEquals(0, multi.extract("a", 5));
        assertEquals(2, multi.extract("b", 3));
        assertEquals(0, multi.extract("a", -3));
        assertEquals(1, slots.countAt(0));
        assertEquals(1, slots.countAt(1));

        for (Integer amount : slots.extractedAmounts) {
            assertTrue(amount >= 0, "a negative amount reached the inventory: " + amount);
        }
        for (Integer amount : slots.insertedAmounts) {
            assertTrue(amount >= 0, "a negative amount reached the inventory: " + amount);
        }
    }

    /**
     * T6 (TERM-12): contents() stays correct when the entry list shrinks, and the change counter strictly increases.
     */
    @Test
    public void doesNotReproduceTERM12_entryCountChangeBetweenCallsIsHandled() {
        FakeSlots a = new FakeSlots(2, 64, 64).with(new String[] { "x", "y" }, new int[] { 10, 5 });
        FakeSlots b = new FakeSlots(2, 64, 64).with(new String[] { "z", null }, new int[] { 7, 0 });
        MultiInventory multi = new MultiInventory(new Entry(a, 1, null, false), new Entry(b, 2, null, false));

        List<StoredStack> before = multi.contents();
        assertEquals(Arrays.asList(new StoredStack("x", 10), new StoredStack("y", 5), new StoredStack("z", 7)), before);

        multi.remove(0);
        List<StoredStack> after = multi.contents();
        assertEquals(Arrays.asList(new StoredStack("z", 7)), after);
        assertEquals(1, after.size());

        long counter = multi.changeCounter();
        assertEquals(0, multi.insert("w", 2, 64));
        assertNotEquals(counter, multi.changeCounter());
        counter = multi.changeCounter();
        assertEquals(2, multi.extract("w", 2));
        assertNotEquals(counter, multi.changeCounter());
        counter = multi.changeCounter();
        multi.remove(0);
        assertNotEquals(counter, multi.changeCounter());
    }

    /** Extraction takes from the lowest-priority entries first. */
    @Test
    public void extractTakesLowestPriorityFirst() {
        FakeSlots high = new FakeSlots(1, 64).with(new String[] { "a" }, new int[] { 5 });
        FakeSlots low = new FakeSlots(1, 64).with(new String[] { "a" }, new int[] { 5 });
        MultiInventory multi = new MultiInventory(new Entry(high, 10, null, false), new Entry(low, 1, null, false));

        assertEquals(5, multi.extract("a", 5));
        assertEquals(0, low.countAt(0));
        assertEquals(5, high.countAt(0));
    }

    /** Insertion fills the lowest-priority entries first. */
    @Test
    public void insertFillsLowestPriorityFirst() {
        FakeSlots high = new FakeSlots(1, 64);
        FakeSlots low = new FakeSlots(1, 64);
        MultiInventory multi = new MultiInventory(new Entry(high, 10, null, false), new Entry(low, 1, null, false));

        assertEquals(0, multi.insert("a", 5, 64));
        assertEquals(5, low.countAt(0));
        assertEquals(0, high.countAt(0));
    }

    /** Contents are merged by key, summed over every entry, in first-appearance order. */
    @Test
    public void contentsMergesCountsByKey() {
        FakeSlots a = new FakeSlots(2, 64, 64).with(new String[] { "x", "y" }, new int[] { 10, 5 });
        FakeSlots b = new FakeSlots(2, 64, 64).with(new String[] { "y", "z" }, new int[] { 3, 7 });
        MultiInventory multi = new MultiInventory(new Entry(a, 1, null, false), new Entry(b, 2, null, false));

        List<StoredStack> contents = multi.contents();
        assertEquals(3, contents.size());
        assertEquals(new StoredStack("x", 10), contents.get(0));
        assertEquals(new StoredStack("y", 8), contents.get(1));
        assertEquals(new StoredStack("z", 7), contents.get(2));
    }

    /** T3: an entry with a filter only sees keys that pass it, for contents, insert and extract alike. */
    @Test
    public void filterRejectsKeys() {
        FakeSlots filtered = new FakeSlots(2, 64, 64).with(new String[] { "b", null }, new int[] { 5, 0 });
        FakeSlots unfiltered = new FakeSlots(2, 64, 64).with(new String[] { "a", null }, new int[] { 5, 0 });
        Predicate<String> onlyB = key -> "b".equals(key);
        MultiInventory multi = new MultiInventory(
            new Entry(filtered, 1, onlyB, false),
            new Entry(unfiltered, 2, null, false));

        assertEquals(Arrays.asList(new StoredStack("b", 5), new StoredStack("a", 5)), multi.contents());

        // "a" comes from the unfiltered entry only; "b" is taken from the filtered one.
        assertEquals(5, multi.extract("a", 5));
        assertEquals(5, multi.extract("b", 5));

        // Both entries' slot 0 is now empty, so each insert lands in the
        // first free slot (slot 0) of its entry: the unfiltered one for
        // "c" (the filtered entry rejects it), the filtered one for "b".
        assertEquals(0, multi.insert("c", 5, 64));
        assertEquals("c", unfiltered.keyAt(0));
        assertEquals(5, unfiltered.countAt(0));

        assertEquals(0, multi.insert("b", 5, 64));
        assertEquals("b", filtered.keyAt(0));
        assertEquals(5, filtered.countAt(0));
        assertEquals(0, unfiltered.countAt(1));
    }

    /** T4: entries are visited in ascending priority for extract and insert alike, stable for equal priorities. */
    @Test
    public void priorityOrderDrivesExtractionAndInsertion() {
        FakeSlots p1 = new FakeSlots(1, 64).with(new String[] { "a" }, new int[] { 2 });
        FakeSlots p5 = new FakeSlots(1, 64).with(new String[] { "a" }, new int[] { 3 });
        MultiInventory multi = new MultiInventory(new Entry(p1, 1, null, false), new Entry(p5, 5, null, false));

        assertEquals(4, multi.extract("a", 4));
        assertEquals(Arrays.asList(new StoredStack("a", 1)), multi.contents());
        assertEquals(0, p1.countAt(0));
        assertEquals(1, p5.countAt(0));

        // The emptied lower-priority entry is filled before the higher one.
        assertEquals(0, multi.insert("a", 10, 64));
        assertEquals(10, p1.countAt(0));
        assertEquals(1, p5.countAt(0));
    }

    /** T1: merged contents and plain extraction without keepLast. */
    @Test
    public void keepLastLeavesOneOfEachType() {
        FakeSlots slots = new FakeSlots(2, 64, 64).with(new String[] { "a", "b" }, new int[] { 5, 3 });
        MultiInventory multi = new MultiInventory(new Entry(slots, 1, null, false));

        assertEquals(Arrays.asList(new StoredStack("a", 5), new StoredStack("b", 3)), multi.contents());

        assertEquals(3, multi.extract("a", 3));
        assertEquals(Arrays.asList(new StoredStack("a", 2), new StoredStack("b", 3)), multi.contents());

        assertEquals(2, multi.extract("a", 10));
        assertEquals(0, multi.extract("zzz", 5));
        assertEquals(0, slots.countAt(0));
        assertEquals(3, slots.countAt(1));
    }

    /** Successful inserts, extracts and removes each bump the change counter once. */
    @Test
    public void changeCounterIncrementsOnSuccessfulChanges() {
        FakeSlots slots = new FakeSlots(2, 64, 64);
        MultiInventory multi = new MultiInventory(new Entry(slots, 1, null, false));

        assertEquals(0, multi.changeCounter());
        assertEquals(0, multi.insert("a", 5, 64));
        assertEquals(1, multi.changeCounter());
        assertEquals(2, multi.extract("a", 2));
        assertEquals(2, multi.changeCounter());
        assertEquals(0, multi.extract("z", 1));
        assertEquals(2, multi.changeCounter());
        multi.remove(0);
        assertEquals(3, multi.changeCounter());
    }

    /** A multi-inventory with no entries has no contents and no slots. */
    @Test
    public void emptyMultiInventoryIsEmpty() {
        MultiInventory multi = new MultiInventory();

        assertTrue(
            multi.contents()
                .isEmpty());
        assertEquals(0, multi.totalSlots());
        assertEquals(0, multi.entryCount());
        assertEquals(5, multi.insert("a", 5, 64));
        assertEquals(0, multi.extract("a", 5));
    }
}
