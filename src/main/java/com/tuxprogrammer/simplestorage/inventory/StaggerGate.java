package com.tuxprogrammer.simplestorage.inventory;
// Ported from Tom's Simple Storage (MIT):
// https://github.com/tom5454/Toms-Storage/blob/95bb61d/NeoForge/src/main/java/com/tom/storagemod/block/entity/OpenCrateBlockEntity.java
// Avoids: STOR-01, STOR-05, STOR-06

/**
 * Stagger gate for throttled block-entity work.
 *
 * <p>
 * Upstream throttles block-entity work with {@code time % N == Math.abs(posHash) % N}.
 * For {@code posHash == Integer.MIN_VALUE}, {@link Math#abs(int)} stays negative
 * (it returns its input), so the right-hand side is negative while the left-hand side
 * is always non-negative and the gate never fires. The affected upstream sites are the
 * open crate (interval 5, STOR-01), the connector and interface (interval 20, STOR-05),
 * and the level emitter (interval 10, STOR-06).
 *
 * <p>
 * This class replaces the {@code Math.abs(posHash) % N} term with a
 * sign-masking modulo ({@code (posHash & 0x7fffffff) % N}) so the phase is always in
 * {@code [0, N)} for every possible hash, and uses {@link Math#floorMod(long, int)}
 * on the game time so negative game times behave as expected.
 *
 * <p>
 * Side: both (plain Java).
 */
public final class StaggerGate {

    /** Throttle interval, in ticks, used by the open crate (STOR-01). */
    public static final int OPEN_CRATE_INTERVAL = 5;

    /** Throttle interval, in ticks, used by the connector and interface (STOR-05). */
    public static final int CONNECTOR_INTERVAL = 20;

    /** Throttle interval, in ticks, used by the level emitter (STOR-06). */
    public static final int EMITTER_INTERVAL = 10;

    private StaggerGate() {}

    /**
     * Returns the phase, in ticks, on which the block entity with the given position
     * hash should do its throttled work, for the given interval.
     *
     * <p>
     * Unlike upstream's {@code Math.abs(positionHash) % interval}, this always
     * returns a value in {@code [0, interval)}, even for
     * {@code positionHash == Integer.MIN_VALUE}, by masking the sign bit first.
     *
     * @param positionHash the block-entity position hash (any int)
     * @param interval     the throttle interval in ticks, must be at least 1
     * @return a phase in {@code [0, interval)}
     * @throws IllegalArgumentException if {@code interval < 1}
     */
    public static int phase(int positionHash, int interval) {
        if (interval < 1) {
            throw new IllegalArgumentException("interval must be >= 1, got " + interval);
        }
        return (positionHash & 0x7fffffff) % interval;
    }

    /**
     * Returns whether the block entity with the given position hash is due to do its
     * throttled work at the given game time.
     *
     * <p>
     * Uses {@link Math#floorMod(long, int)} so the game-time side is always
     * non-negative, and {@link #phase(int, int)} so the hash side is always
     * non-negative; the gate fires exactly once per interval for every hash.
     *
     * @param gameTime     the current game time in ticks
     * @param positionHash the block-entity position hash (any int)
     * @param interval     the throttle interval in ticks, must be at least 1
     * @return {@code true} if throttled work is due at this game time
     * @throws IllegalArgumentException if {@code interval < 1}
     */
    public static boolean isDue(long gameTime, int positionHash, int interval) {
        if (interval < 1) {
            throw new IllegalArgumentException("interval must be >= 1, got " + interval);
        }
        return Math.floorMod(gameTime, interval) == phase(positionHash, interval);
    }
}
