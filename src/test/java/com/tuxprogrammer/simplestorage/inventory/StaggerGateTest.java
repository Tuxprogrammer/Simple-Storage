package com.tuxprogrammer.simplestorage.inventory;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Random;

import org.junit.jupiter.api.Test;

/**
 * Tests for {@link StaggerGate}, which replaces upstream's
 * {@code time % N == Math.abs(posHash) % N} stagger so that
 * {@code posHash == Integer.MIN_VALUE} still fires.
 */
public class StaggerGateTest {

    /** STOR-01: open crate, interval 5 — the MIN_VALUE hash must still fire every five ticks. */
    @Test
    public void doesNotReproduceSTOR01_minValueHashStillFiresEveryFiveTicks() {
        assertDueCount(5, 100, 20);
    }

    /** STOR-05: connector and interface, interval 20 — the MIN_VALUE hash must still fire every twenty ticks. */
    @Test
    public void doesNotReproduceSTOR05_minValueHashStillFiresEveryTwentyTicks() {
        assertDueCount(20, 200, 10);
    }

    /** STOR-06: level emitter, interval 10 — the MIN_VALUE hash must still fire every ten ticks. */
    @Test
    public void doesNotReproduceSTOR06_minValueHashStillFiresEveryTenTicks() {
        assertDueCount(10, 100, 10);
    }

    /**
     * Counts how many of the first {@code length} consecutive game times are due for
     * the {@code Integer.MIN_VALUE} hash and asserts the expected count, proving the
     * gate fires once per interval instead of never (upstream) or twice.
     */
    private static void assertDueCount(int interval, int length, int expectedDue) {
        int due = 0;
        for (int time = 0; time < length; time++) {
            if (StaggerGate.isDue(time, Integer.MIN_VALUE, interval)) {
                due++;
            }
        }
        assertTrue(
            due == expectedDue,
            "expected " + expectedDue + " due times in " + length + " ticks (interval " + interval + "), got " + due);
    }

    /** {@link StaggerGate#phase(int, int)} is always in {@code [0, interval)} for adversarial hashes. */
    @Test
    public void phaseIsAlwaysInRange() {
        int[] hashes = { Integer.MIN_VALUE, -1, 0, 1, Integer.MAX_VALUE, 12345, -12345 };
        int[] intervals = { 1, 5, 10, 20 };
        for (int interval : intervals) {
            for (int hash : hashes) {
                int p = StaggerGate.phase(hash, interval);
                assertTrue(
                    p >= 0 && p < interval,
                    "phase(" + hash + ", " + interval + ") = " + p + " is outside [0, " + interval + ")");
            }
        }
    }

    /**
     * For every pseudo-random hash and each interval, exactly one game time in any window of {@code interval} ticks is
     * due.
     */
    @Test
    public void everyHashFiresOncePerInterval() {
        int[] intervals = { 1, 5, 10, 20 };
        Random random = new Random(42);
        int[] hashes = new int[200];
        for (int i = 0; i < hashes.length; i++) {
            hashes[i] = random.nextInt();
        }
        for (int interval : intervals) {
            for (int hash : hashes) {
                for (int windowStart = 0; windowStart < interval * 2; windowStart++) {
                    int due = 0;
                    for (int offset = 0; offset < interval; offset++) {
                        if (StaggerGate.isDue(windowStart + offset, hash, interval)) {
                            due++;
                        }
                    }
                    assertTrue(
                        due == 1,
                        "hash " + hash
                            + ", interval "
                            + interval
                            + ", window "
                            + windowStart
                            + ": expected exactly 1 due tick, got "
                            + due);
                }
            }
        }
    }

    /** Negative game times must wrap with floor semantics: {@code isDue(-1, h, 5) == isDue(4, h, 5)}. */
    @Test
    public void negativeGameTimeUsesFloorMod() {
        int[] hashes = { Integer.MIN_VALUE, -1, 0, 1, Integer.MAX_VALUE, 12345, -12345 };
        for (int hash : hashes) {
            assertTrue(
                StaggerGate.isDue(-1, hash, 5) == StaggerGate.isDue(4, hash, 5),
                "isDue(-1, " + hash + ", 5) must equal isDue(4, " + hash + ", 5)");
        }
    }

    /** {@code interval < 1} (e.g. 0 and -3) is rejected with {@link IllegalArgumentException}. */
    @Test
    public void invalidIntervalIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> StaggerGate.phase(123, 0));
        assertThrows(IllegalArgumentException.class, () -> StaggerGate.phase(123, -3));
        assertThrows(IllegalArgumentException.class, () -> StaggerGate.isDue(10, 123, 0));
        assertThrows(IllegalArgumentException.class, () -> StaggerGate.isDue(10, 123, -3));
    }
}
