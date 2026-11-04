package com.tuxprogrammer.simplestorage.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * Tests for {@link EnumCycle}: enum cycling (UI-04) and wrap-around cycling.
 */
public class EnumCycleTest {

    /**
     * UI-04: cycling must never throw on a null or out-of-range current value
     * (upstream dereferenced {@code getState().ordinal()} on a null state).
     * The cycling methods take a primitive int, so a null state is
     * represented by any out-of-range primitive; out-of-range values are
     * treated as index 0.
     */
    @Test
    public void doesNotReproduceUI04_cycleNeverThrowsOnNullOrOutOfRange() {
        // Out-of-range current (null is out-of-range for the primitive): treated as index 0.
        assertEquals(1, EnumCycle.next(-1, 3), "next of an out-of-range current starts at 0 and advances");
        assertEquals(
            2,
            EnumCycle.previous(Integer.MIN_VALUE, 3),
            "previous of an out-of-range current wraps from 0 to last");

        // More out-of-range currents: also treated as index 0.
        assertEquals(1, EnumCycle.next(5, 3));
        assertEquals(2, EnumCycle.previous(-4, 3));
        assertEquals(1, EnumCycle.next(Integer.MIN_VALUE, 3));
        assertEquals(2, EnumCycle.previous(Integer.MAX_VALUE, 3));
        assertEquals(1, EnumCycle.next(3, 3));
        assertEquals(2, EnumCycle.previous(3, 3));

        // No exception is thrown for any invalid input.
        for (Integer current : new Integer[] { -1, 0, 3, 100, -100 }) {
            EnumCycle.normalize(current, 3);
            EnumCycle.next(current, 3);
            EnumCycle.previous(current, 3);
        }

        // Zero values: no state at all.
        assertEquals(-1, EnumCycle.next(0, 0));
        assertEquals(-1, EnumCycle.previous(0, 0));
    }

    /**
     * Cycling wraps around at both ends for any number of values.
     */
    @Test
    public void wrapAroundCycling() {
        for (int count : new int[] { 1, 2, 4, 5 }) {
            // Full forward cycle returns to the start.
            int current = 0;
            for (int i = 0; i < count; i++) {
                current = EnumCycle.next(current, count);
                assertEquals((i + 1) % count, current, "forward step " + i + " with count " + count);
            }

            // Full reverse cycle returns to the start.
            current = 0;
            for (int i = 0; i < count; i++) {
                current = EnumCycle.previous(current, count);
                assertEquals(
                    (count - ((i + 1) % count)) % count,
                    current,
                    "reverse step " + i + " with count " + count);
            }
        }

        // Explicit edge checks.
        assertEquals(0, EnumCycle.next(3, 4), "last value wraps to first");
        assertEquals(3, EnumCycle.previous(0, 4), "first value wraps to last");
        assertEquals(0, EnumCycle.next(0, 1), "single value cycles to itself");
        assertEquals(0, EnumCycle.previous(0, 1), "single value cycles to itself in reverse");
    }
}
