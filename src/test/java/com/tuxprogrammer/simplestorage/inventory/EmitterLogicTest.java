package com.tuxprogrammer.simplestorage.inventory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link EmitterLogic}.
 */
public class EmitterLogicTest {

    @Test
    public void doesNotReproduceSTOR07_missingTagLoadsTheRuntimeDefault() {
        // STOR-07: a missing tag loaded as 0, but the runtime default is 1.
        assertEquals(1, EmitterLogic.resolveCount(false, 0));
        assertEquals(1, EmitterLogic.resolveCount(false, 99));
        // With the resolved default of 1, greater-than mode at amount 0 must NOT power.
        int threshold = EmitterLogic.resolveCount(false, 0);
        assertFalse(EmitterLogic.isPowered(0, threshold, false));
    }

    @Test
    public void resolveCountKeepsStoredValueAndClampsNegative() {
        assertEquals(5, EmitterLogic.resolveCount(true, 5));
        assertEquals(0, EmitterLogic.resolveCount(true, 0));
        assertEquals(0, EmitterLogic.resolveCount(true, -3));
    }

    @Test
    public void greaterThanIsInclusive() {
        assertTrue(EmitterLogic.isPowered(5, 5, false));
        assertTrue(EmitterLogic.isPowered(6, 5, false));
        assertFalse(EmitterLogic.isPowered(4, 5, false));
    }

    @Test
    public void lessThanIsStrict() {
        assertTrue(EmitterLogic.isPowered(4, 5, true));
        assertFalse(EmitterLogic.isPowered(5, 5, true));
        assertFalse(EmitterLogic.isPowered(6, 5, true));
    }

    @Test
    public void signalIsFifteenOrZero() {
        assertEquals(EmitterLogic.SIGNAL_ON, EmitterLogic.signal(5, 5, false));
        assertEquals(15, EmitterLogic.SIGNAL_ON);
        assertEquals(EmitterLogic.SIGNAL_OFF, EmitterLogic.signal(4, 5, false));
        assertEquals(0, EmitterLogic.SIGNAL_OFF);
        assertEquals(EmitterLogic.SIGNAL_OFF, EmitterLogic.signal(5, 5, true));
    }

    @Test
    public void shouldNotifyOnlyOnChange() {
        assertTrue(EmitterLogic.shouldNotify(false, true));
        assertTrue(EmitterLogic.shouldNotify(true, false));
        assertFalse(EmitterLogic.shouldNotify(false, false));
        assertFalse(EmitterLogic.shouldNotify(true, true));
    }
}
