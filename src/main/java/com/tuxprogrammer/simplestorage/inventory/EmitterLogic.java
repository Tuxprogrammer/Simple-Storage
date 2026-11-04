package com.tuxprogrammer.simplestorage.inventory;
// Ported from Tom's Simple Storage (MIT):
// https://github.com/tom5454/Toms-Storage/blob/95bb61d/NeoForge/src/platform-shared/java/com/tom/storagemod/block/entity/LevelEmitterBlockEntity.java
// Avoids: STOR-07

/**
 * Pure evaluation logic for the level emitter.
 *
 * <p>
 * The level emitter compares the amount of one filtered item in a storage
 * network against a configured threshold and outputs a full-strength redstone
 * signal ({@link #SIGNAL_ON}) when the comparison passes, or no signal
 * ({@link #SIGNAL_OFF}) otherwise. The emitter re-evaluates every
 * {@link #EVALUATE_INTERVAL_TICKS} ticks.
 * </p>
 *
 * <p>
 * All methods are static and free of Minecraft types so the logic can be
 * unit tested on plain Java 8.
 * </p>
 *
 * Side: both (plain Java).
 */
public final class EmitterLogic {

    /** Runtime default for the threshold when the NBT tag is absent (fixes STOR-07). */
    public static final int DEFAULT_COUNT = 1;

    /** Full-strength redstone signal output when the comparison passes. */
    public static final int SIGNAL_ON = 15;

    /** No redstone signal output. */
    public static final int SIGNAL_OFF = 0;

    /** How often, in ticks, the emitter re-evaluates its comparison. */
    public static final int EVALUATE_INTERVAL_TICKS = 10;

    /**
     * Private constructor; all methods are static.
     */
    private EmitterLogic() {}

    /**
     * Resolves the effective threshold from a possibly-missing NBT tag.
     *
     * <p>
     * STOR-07: the threshold field defaulted to {@link #DEFAULT_COUNT} at
     * runtime but loaded as 0 from a missing NBT tag, so a legacy/missing tag
     * made "greater than" mode power on any non-zero count. When the tag is
     * absent the runtime default must be used; when present the stored value
     * is kept but clamped to a minimum of 0.
     * </p>
     *
     * @param tagPresent  whether the threshold NBT tag exists on the saved data
     * @param storedCount the value stored in the tag (ignored when absent)
     * @return {@link #DEFAULT_COUNT} when the tag is absent, else
     *         {@code max(0, storedCount)}
     */
    public static int resolveCount(boolean tagPresent, int storedCount) {
        if (!tagPresent) {
            return DEFAULT_COUNT;
        }
        return Math.max(0, storedCount);
    }

    /**
     * Evaluates the threshold comparison.
     *
     * @param amount    the amount of the filtered item found in the network
     * @param threshold the effective threshold (see {@link #resolveCount})
     * @param lessThan  true for "less than" mode, false for "greater than" mode
     * @return true when the emitter should be powered. "Greater than" mode is
     *         inclusive ({@code amount >= threshold}); "less than" mode is
     *         strict ({@code amount < threshold})
     */
    public static boolean isPowered(long amount, int threshold, boolean lessThan) {
        return lessThan ? amount < threshold : amount >= threshold;
    }

    /**
     * Computes the redstone signal for the current evaluation.
     *
     * @param amount    the amount of the filtered item found in the network
     * @param threshold the effective threshold (see {@link #resolveCount})
     * @param lessThan  true for "less than" mode, false for "greater than" mode
     * @return {@link #SIGNAL_ON} when the comparison passes, else
     *         {@link #SIGNAL_OFF}
     */
    public static int signal(long amount, int threshold, boolean lessThan) {
        return isPowered(amount, threshold, lessThan) ? SIGNAL_ON : SIGNAL_OFF;
    }

    /**
     * Determines whether the power state changed, for change notifications.
     *
     * @param wasPowered the power state from the previous evaluation
     * @param nowPowered the power state from the current evaluation
     * @return true only when the state flipped (on to off or off to on)
     */
    public static boolean shouldNotify(boolean wasPowered, boolean nowPowered) {
        return wasPowered != nowPowered;
    }
}
