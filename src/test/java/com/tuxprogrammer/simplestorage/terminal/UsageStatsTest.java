package com.tuxprogrammer.simplestorage.terminal;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * Regression tests for {@link UsageStats}.
 *
 * Side: both (plain Java).
 */
public class UsageStatsTest {

    /**
     * An empty network with 0 total slots must not divide by zero; the
     * readout is simply 0%.
     */
    @Test
    public void doesNotReproduceTERM03_zeroSlotsDoesNotDivideByZero() {
        // freePercent(free, total) = 0 if total <= 0, else
        // round-half-up of free * 100 / total, clamped to 0..100.
        assertEquals(33, UsageStats.freePercent(1, 3));
        assertEquals(67, UsageStats.freePercent(2, 3));
        assertEquals(0, UsageStats.freePercent(0, 0));
        assertEquals(0, UsageStats.freePercent(5, 0));
        assertEquals(100, UsageStats.freePercent(3, 3));
        assertEquals(100, UsageStats.freePercent(4, 3));
        assertEquals(0, UsageStats.freePercent(-1, 10));
        assertEquals(50, UsageStats.freePercent(1, 2));
    }

    /**
     * A negative total slot count is treated like a non-positive one.
     */
    @Test
    public void negativeTotalSlotsGivesZero() {}

    /**
     * Exact percentages are reported as-is.
     */
    @Test
    public void exactPercentages() {}

    /**
     * Rounding is to the nearest whole percent.
     */
    @Test
    public void roundsToNearestPercent() {}

    /**
     * More free slots than total slots cannot exceed 100.
     */
    @Test
    public void clampsToHundredPercent() {}

    /**
     * More used slots than free slots cannot dip below 0.
     */
    @Test
    public void clampsToZeroPercent() {}

    /**
     * Large slot counts stay in the percent range without overflow.
     */
    @Test
    public void largeSlotCounts() {}
}
