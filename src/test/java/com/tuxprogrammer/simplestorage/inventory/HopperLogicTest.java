package com.tuxprogrammer.simplestorage.inventory;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link HopperLogic}.
 */
public class HopperLogicTest {

    @Test
    public void doesNotReproduceSTOR08_bottomChangePausesExactlyOneCycle() {
        HopperLogic hopper = new HopperLogic(10);
        // The hopper is idle at start; the bottom just changed, so it must
        // pause for exactly one full cooldown cycle (STOR-08).
        assertEquals(HopperLogic.Decision.IDLE_BACKOFF, hopper.tick(true, false, false, true));
        // The pause lasts the full base cooldown: baseCooldown-1 waits...
        for (int i = 0; i < 9; i++) {
            assertEquals(HopperLogic.Decision.WAIT, hopper.tick(true, false, false, false));
        }
        // ...and the very next tick transfers again (pause was exactly one
        // cycle, no extra hidden wait).
        assertEquals(HopperLogic.Decision.TRANSFER, hopper.tick(true, false, false, false));
    }

    @Test
    public void doesNotReproduceSTOR09_unfilteredNetworkSourceBacksOffInsteadOfSpinning() {
        HopperLogic hopper = new HopperLogic(10);
        // STOR-09: an unfiltered network source must back off and set a
        // cooldown instead of re-running the full check every tick.
        assertEquals(HopperLogic.Decision.IDLE_BACKOFF, hopper.tick(true, true, false, false));
        // After the IDLE_BACKOFF, the next baseCooldown-1 ticks return WAIT.
        for (int i = 0; i < 9; i++) {
            assertEquals(HopperLogic.Decision.WAIT, hopper.tick(true, true, false, false));
        }
        // And the check runs again only after the full cycle.
        assertEquals(HopperLogic.Decision.IDLE_BACKOFF, hopper.tick(true, true, false, false));
    }

    @Test
    public void waitsUntilCooldownElapses() {
        HopperLogic hopper = new HopperLogic(10);
        assertEquals(HopperLogic.Decision.TRANSFER, hopper.tick(true, false, false, false));
        hopper.transferred(true);
        for (int i = 0; i < 9; i++) {
            assertEquals(HopperLogic.Decision.WAIT, hopper.tick(true, false, false, false));
        }
        assertEquals(HopperLogic.Decision.TRANSFER, hopper.tick(true, false, false, false));
    }

    @Test
    public void disabledBacksOff() {
        HopperLogic hopper = new HopperLogic(10);
        assertEquals(HopperLogic.Decision.IDLE_BACKOFF, hopper.tick(false, false, false, false));
        for (int i = 0; i < 9; i++) {
            assertEquals(HopperLogic.Decision.WAIT, hopper.tick(false, false, false, false));
        }
        assertEquals(HopperLogic.Decision.IDLE_BACKOFF, hopper.tick(false, false, false, false));
    }

    @Test
    public void baseCooldownIsClampedTo1And200() {
        // A cooldown of 1 is fully consumed by the first tick() decrement, so
        // tick() returns TRANSFER on the very next tick (no WAIT in between).
        // remaining() reports the clamped base cooldown after arm.
        HopperLogic min = new HopperLogic(0);
        min.transferred(true);
        assertEquals(1, min.remaining());
        assertEquals(HopperLogic.Decision.TRANSFER, min.tick(true, false, false, false));

        HopperLogic max = new HopperLogic(1000);
        max.transferred(true);
        assertEquals(200, max.remaining());
        for (int i = 0; i < 199; i++) {
            assertEquals(HopperLogic.Decision.WAIT, max.tick(true, false, false, false));
        }
        assertEquals(HopperLogic.Decision.TRANSFER, max.tick(true, false, false, false));
    }

    @Test
    public void transferredSetsCooldown() {
        HopperLogic hopper = new HopperLogic(10);
        hopper.tick(true, false, false, false);
        hopper.transferred(true);
        assertEquals(10, hopper.remaining());
        for (int i = 0; i < 9; i++) {
            hopper.tick(true, false, false, false);
        }
        // Failed attempt arms only half the cooldown (min 1).
        assertEquals(HopperLogic.Decision.TRANSFER, hopper.tick(true, false, false, false));
        hopper.transferred(false);
        assertEquals(5, hopper.remaining());

        HopperLogic odd = new HopperLogic(5);
        odd.tick(true, false, false, false);
        odd.transferred(false);
        assertEquals(2, odd.remaining());

        HopperLogic one = new HopperLogic(1);
        one.tick(true, false, false, false);
        one.transferred(false);
        assertEquals(1, one.remaining());
    }
}
