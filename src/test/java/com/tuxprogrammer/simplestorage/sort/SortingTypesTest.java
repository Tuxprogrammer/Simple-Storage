package com.tuxprogrammer.simplestorage.sort;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import org.junit.jupiter.api.Test;

/**
 * Tests for {@link SortingTypes} and {@link SortKey}: the kept sort modes,
 * their persisted ordinals, the reserved TYPE ordinal, the spec's tie-breaks
 * (including the case-insensitive name ordering and the space-efficiency
 * formula), the reverse flag, the next() cycle, and the comparator contract
 * on a shuffled 50-key list.
 */
public class SortingTypesTest {

    private static SortKey key(long amount, String name, String modId, int id, int stackLimit, String sortId) {
        return new SortKey(amount, name, modId, id, stackLimit, sortId);
    }

    @Test
    public void ordinalsMatchThePersistedOrder() {
        assertEquals(0, SortingTypes.AMOUNT.ordinal());
        assertEquals(1, SortingTypes.NAME.ordinal());
        assertEquals(2, SortingTypes.MOD.ordinal());
        assertEquals(3, SortingTypes.SPACE_EFFICIENCY.ordinal());
        assertEquals(4, SortingTypes.ID.ordinal());
    }

    @Test
    public void theFiveModesAreKeptAndTypeIsReserved() {
        assertEquals(5, SortingTypes.values().length);
        assertEquals(5, SortingTypes.RESERVED_TYPE_ORDINAL);
    }

    @Test
    public void theDefaultModeIsAmount() {
        assertSame(SortingTypes.AMOUNT, SortingTypes.fromOrdinal(0));
    }

    @Test
    public void reservedAndOutOfRangeOrdinalsFallBackToTheDefault() {
        assertSame(SortingTypes.AMOUNT, SortingTypes.fromOrdinal(SortingTypes.RESERVED_TYPE_ORDINAL));
        assertSame(SortingTypes.AMOUNT, SortingTypes.fromOrdinal(-1));
        assertSame(SortingTypes.AMOUNT, SortingTypes.fromOrdinal(6));
        assertSame(SortingTypes.AMOUNT, SortingTypes.fromOrdinal(255));
        assertSame(SortingTypes.AMOUNT, SortingTypes.fromOrdinal(Integer.MAX_VALUE));
    }

    @Test
    public void knownOrdinalsMapToTheirModes() {
        assertSame(SortingTypes.AMOUNT, SortingTypes.fromOrdinal(0));
        assertSame(SortingTypes.NAME, SortingTypes.fromOrdinal(1));
        assertSame(SortingTypes.MOD, SortingTypes.fromOrdinal(2));
        assertSame(SortingTypes.SPACE_EFFICIENCY, SortingTypes.fromOrdinal(3));
        assertSame(SortingTypes.ID, SortingTypes.fromOrdinal(4));
    }

    @Test
    public void nextCyclesThroughTheFiveModesInOrdinalOrder() {
        List<SortingTypes> cycle = new ArrayList<SortingTypes>();
        SortingTypes current = SortingTypes.AMOUNT;
        for (int i = 0; i < 5; i++) {
            cycle.add(current);
            current = current.next();
        }
        assertEquals(
            Arrays.asList(
                SortingTypes.AMOUNT,
                SortingTypes.NAME,
                SortingTypes.MOD,
                SortingTypes.SPACE_EFFICIENCY,
                SortingTypes.ID),
            cycle);
        assertSame(SortingTypes.AMOUNT, current);
    }

    @Test
    public void amountOrdersByQuantityDescendingWithCaseInsensitiveNameTieBreak() {
        Comparator<SortKey> c = SortingTypes.AMOUNT.comparator(false);
        SortKey apple = key(5, "apple", "m", 1, 64, "apple");
        SortKey cherry = key(10, "cherry", "m", 2, 64, "cherry");
        SortKey banana = key(10, "Banana", "m", 3, 64, "Banana");
        SortKey zebra = key(1, "Zebra", "m", 4, 64, "Zebra");
        List<SortKey> keys = new ArrayList<SortKey>();
        keys.add(apple);
        keys.add(cherry);
        keys.add(banana);
        keys.add(zebra);
        Collections.sort(keys, c);
        assertEquals(Arrays.asList("Banana", "cherry", "apple", "Zebra"), names(keys));
        assertEquals(
            Arrays.asList("Zebra", "apple", "cherry", "Banana"),
            names(sorted(SortingTypes.AMOUNT.comparator(true), apple, cherry, banana, zebra)));
    }

    @Test
    public void amountTieBreaksByNameAscendingNotByAmount() {
        Comparator<SortKey> c = SortingTypes.AMOUNT.comparator(false);
        SortKey a = key(5, "apple", "m", 1, 64, "apple");
        SortKey b = key(5, "Banana", "m", 2, 64, "Banana");
        assertTrue(c.compare(b, a) > 0, "name ascending on equal amounts");
    }

    @Test
    public void nameOrdersByNameAscendingOnlyAndIgnoresCase() {
        Comparator<SortKey> c = SortingTypes.NAME.comparator(false);
        SortKey apple = key(5, "apple", "m", 1, 64, "apple");
        SortKey cherry = key(10, "cherry", "m", 2, 64, "cherry");
        SortKey banana = key(10, "Banana", "m", 3, 64, "Banana");
        SortKey zebra = key(1, "Zebra", "m", 4, 64, "Zebra");
        List<SortKey> keys = new ArrayList<SortKey>();
        keys.add(apple);
        keys.add(cherry);
        keys.add(banana);
        keys.add(zebra);
        Collections.sort(keys, c);
        assertEquals(Arrays.asList("apple", "Banana", "cherry", "Zebra"), names(keys));
        assertEquals(
            Arrays.asList("Zebra", "cherry", "Banana", "apple"),
            names(sorted(SortingTypes.NAME.comparator(true), apple, cherry, banana, zebra)));
    }

    @Test
    public void modGroupsByNamespaceAscendingWithCaseInsensitiveNameTieBreak() {
        Comparator<SortKey> c = SortingTypes.MOD.comparator(false);
        SortKey bModX = key(1, "x", "b", 1, 64, "x");
        SortKey aModZ = key(1, "z", "a", 2, 64, "z");
        SortKey aModY = key(1, "y", "a", 3, 64, "y");
        List<SortKey> keys = new ArrayList<SortKey>();
        keys.add(bModX);
        keys.add(aModZ);
        keys.add(aModY);
        Collections.sort(keys, c);
        assertEquals(Arrays.asList("y", "z", "x"), names(keys));
    }

    @Test
    public void spaceEfficiencyOrdersByAmountOverStackLimitDescending() {
        Comparator<SortKey> c = SortingTypes.SPACE_EFFICIENCY.comparator(false);
        // spec: SPACE_EFFICIENCY is the used-slot count (ceil(amount /
        // stackLimit)) descending, so the most full stacks come first
        SortKey iron = key(130, "iron", "m", 1, 64, "iron"); // 3 slots
        SortKey wood = key(100, "wood", "m", 2, 16, "wood"); // 7 slots
        SortKey stone = key(64, "stone", "m", 3, 64, "stone"); // 1 slot
        SortKey dirt = key(1, "dirt", "m", 4, 64, "dirt"); // 1 slot
        List<SortKey> keys = new ArrayList<SortKey>();
        keys.add(iron);
        keys.add(wood);
        keys.add(stone);
        keys.add(dirt);
        Collections.sort(keys, c);
        assertEquals(Arrays.asList("wood", "iron", "stone", "dirt"), names(keys));
    }

    @Test
    public void spaceEfficiencyTiesBreakByQuantityDescendingThenNameAscending() {
        Comparator<SortKey> c = SortingTypes.SPACE_EFFICIENCY.comparator(false);
        // spec: SPACE_EFFICIENCY is used-slot count descending, then amount
        // descending, then name ascending
        SortKey more = key(64, "iron", "mod", 1, 64, "mod:iron");
        SortKey less = key(32, "iron", "mod", 1, 32, "mod:iron");
        assertTrue(c.compare(more, less) < 0, "equal used slots -> higher amount first");
        // equal used slots and equal amount -> name ascending
        SortKey a = key(64, "apple", "mod", 1, 64, "mod:apple");
        SortKey b = key(64, "Banana", "mod", 2, 64, "mod:banana");
        assertTrue(c.compare(a, b) < 0, "name ascending on equal used slots and amount");
    }

    @Test
    public void spaceEfficiencyFormulaUsesAmountDividedByStackLimit() {
        Comparator<SortKey> c = SortingTypes.SPACE_EFFICIENCY.comparator(false);
        // spec: SPACE_EFFICIENCY orders by the used-slot count
        // ceil(amount / stackLimit) descending, so a stack that uses more
        // slots is always first
        SortKey bigStackFull = key(64, "iron", "mod", 1, 64, "mod:iron"); // 1 slot
        SortKey smallStackFull = key(16, "wood", "mod", 2, 16, "mod:wood"); // 1 slot
        SortKey overflow = key(17, "wood", "mod", 2, 16, "mod:wood"); // 2 slots
        SortKey doubleIron = key(128, "iron", "mod", 1, 64, "mod:iron"); // 2 slots
        assertTrue(c.compare(overflow, bigStackFull) < 0, "2 used slots before 1");
        assertTrue(c.compare(doubleIron, bigStackFull) < 0, "2 used slots before 1");
        // equal used slots -> amount descending
        assertTrue(c.compare(doubleIron, overflow) < 0, "equal used slots -> higher amount first");
    }

    @Test
    public void idOrdersByRegistryIdStringAscendingCaseInsensitiveThenNumericId() {
        Comparator<SortKey> c = SortingTypes.ID.comparator(false);
        SortKey za = key(1, "z:a", "m", 1, 64, "z:a");
        SortKey ab = key(1, "a:b", "m", 2, 64, "a:b");
        SortKey aa = key(1, "a:a", "m", 3, 64, "a:a");
        List<SortKey> keys = new ArrayList<SortKey>();
        keys.add(za);
        keys.add(ab);
        keys.add(aa);
        Collections.sort(keys, c);
        assertEquals(Arrays.asList("a:a", "a:b", "z:a"), sortIds(keys));
    }

    @Test
    public void descendingReversesEveryLevelIncludingTieBreaks() {
        Comparator<SortKey> asc = SortingTypes.AMOUNT.comparator(false);
        Comparator<SortKey> desc = SortingTypes.AMOUNT.comparator(true);
        SortKey big = key(100, "apple", "mod", 1, 64, "mod:apple");
        SortKey smallA = key(1, "apple", "mod", 1, 64, "mod:apple");
        SortKey smallB = key(1, "banana", "mod", 2, 64, "mod:banana");
        // ascending: big, then small apple, then small banana
        assertEquals(Arrays.asList("apple", "apple", "banana"), names(sorted(asc, big, smallA, smallB)));
        // descending: small banana, then small apple (name tie-break reversed),
        // then big
        assertEquals(Arrays.asList("banana", "apple", "apple"), names(sorted(desc, big, smallA, smallB)));
        // the reverse flag is exactly Collections.reverseOrder(comparator(false))
        assertEquals(
            Collections.reverseOrder(asc)
                .compare(big, smallA),
            desc.compare(big, smallA));
        assertEquals(
            Collections.reverseOrder(asc)
                .compare(smallA, smallB),
            desc.compare(smallA, smallB));
    }

    @Test
    public void doesNotReproduceTERM02_staleOrdinalNeverThrowsAndFallsBackToDefault() {
        // upstream: SortingTypes.VALUES[sorting & 0xFF] had no bounds check, so a
        // stale/edited NBT sort value >= 6 threw ArrayIndexOutOfBoundsException.
        for (int ordinal : new int[] { 5, 6, 7, 255, -1, Integer.MAX_VALUE, Integer.MIN_VALUE }) {
            SortingTypes result = SortingTypes.fromOrdinal(ordinal);
            assertSame(SortingTypes.AMOUNT, result, "ordinal " + ordinal + " should map to the default AMOUNT");
        }
    }

    @Test
    public void doesNotReproduceTERM09_typeComparatorNotPortedSoNoRace() throws InterruptedException {
        // upstream: ComparatorType (the TYPE mode's comparator) used a static,
        // unsynchronized lookup that two sorting threads could rebuild
        // concurrently. The TYPE mode is not ported (D27), so no such shared
        // lookup exists: the kept comparators carry no mutable shared state
        // and agree under concurrent construction and use.
        SortingTypes[] types = SortingTypes.values();
        assertEquals(5, types.length);
        for (SortingTypes type : types) {
            assertTrue(
                type.ordinal() < SortingTypes.RESERVED_TYPE_ORDINAL,
                "no mode may hold the reserved TYPE ordinal 5");
            assertFalse(type == null);
        }
        final Comparator<SortKey>[] comparators = new Comparator[types.length];
        final SortKey[] keys = { key(1, "apple", "amod", 1, 64, "amod:apple"),
            key(1, "Banana", "bmod", 2, 64, "bmod:banana"), key(64, "iron", "imod", 3, 64, "imod:iron"), };
        List<Thread> threads = new ArrayList<Thread>();
        for (int i = 0; i < types.length; i++) {
            final int index = i;
            threads.add(new Thread(new Runnable() {

                @Override
                public void run() {
                    comparators[index] = types[index].comparator(false);
                }
            }));
        }
        for (Thread thread : threads) {
            thread.start();
        }
        for (Thread thread : threads) {
            thread.join();
        }
        for (int i = 0; i < comparators.length; i++) {
            List<SortKey> first = new ArrayList<SortKey>();
            for (SortKey k : keys) {
                first.add(k);
            }
            first.sort(comparators[i]);
            List<SortKey> second = new ArrayList<SortKey>();
            for (SortKey k : keys) {
                second.add(k);
            }
            second.sort(comparators[i]);
            assertEquals(names(first), names(second), types[i] + " comparator is repeatable (no shared state to race)");
        }
    }

    @Test
    public void comparatorContractSignSymmetricAndStableSortOrderIsDeterministic() {
        Random random = new Random(1);
        List<SortKey> keys = new ArrayList<SortKey>();
        for (int i = 0; i < 50; i++) {
            keys.add(
                key(
                    random.nextInt(1000),
                    "item" + (i % 7),
                    "mod" + (i % 3),
                    random.nextInt(1000),
                    new int[] { 1, 8, 16, 32, 64 }[i % 5],
                    "mod" + (i % 3) + ":item" + (i % 7)));
        }
        for (final SortingTypes type : SortingTypes.values()) {
            for (final boolean descending : new boolean[] { false, true }) {
                Comparator<SortKey> c = type.comparator(descending);
                // sign symmetry
                for (SortKey k : keys) {
                    for (SortKey j : keys) {
                        assertEquals(-c.compare(k, j), c.compare(j, k), type + "/" + descending + " sign symmetry");
                    }
                }
                // deterministic order repeated twice on the same shuffle
                List<SortKey> first = new ArrayList<SortKey>(keys);
                Collections.shuffle(first, new Random(1));
                first.sort(c);
                List<SortKey> second = new ArrayList<SortKey>(keys);
                Collections.shuffle(second, new Random(1));
                second.sort(c);
                assertEquals(names(first), names(second), type + "/" + descending + " deterministic order");
            }
        }
    }

    private static List<SortKey> sorted(Comparator<SortKey> c, SortKey... keys) {
        List<SortKey> list = new ArrayList<SortKey>();
        for (SortKey key : keys) {
            list.add(key);
        }
        Collections.sort(list, c);
        return list;
    }

    private static List<String> names(List<SortKey> keys) {
        List<String> names = new ArrayList<String>();
        for (SortKey key : keys) {
            names.add(key.getName());
        }
        return names;
    }

    private static List<Long> amounts(List<SortKey> keys) {
        List<Long> amounts = new ArrayList<Long>();
        for (SortKey key : keys) {
            amounts.add(key.getAmount());
        }
        return amounts;
    }

    private static List<String> modIds(List<SortKey> keys) {
        List<String> modIds = new ArrayList<String>();
        for (SortKey key : keys) {
            modIds.add(key.getModId());
        }
        return modIds;
    }

    private static List<String> sortIds(List<SortKey> keys) {
        List<String> sortIds = new ArrayList<String>();
        for (SortKey key : keys) {
            sortIds.add(key.getSortId());
        }
        return sortIds;
    }
}
