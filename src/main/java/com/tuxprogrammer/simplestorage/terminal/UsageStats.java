package com.tuxprogrammer.simplestorage.terminal;
// Ported from Tom's Simple Storage (MIT):
// https://github.com/tom5454/Toms-Storage/blob/95bb61d/NeoForge/src/platform-shared/java/com/tom/storagemod/screen/AbstractStorageTerminalScreen.java
// Avoids: TERM-03

/**
 * Pure statistics helpers for terminal storage readouts.
 *
 * Side: both (plain Java).
 */
public final class UsageStats {

    /**
     * Computes the free capacity of a storage network as a percentage.
     *
     * <p>
     * Guards against a non-positive slot count before dividing (TERM-03:
     * upstream only guarded the 32767 cap, so an empty network with 0 slots
     * divided by zero and produced NaN or Infinity).
     *
     * @param freeSlots  the number of free slots
     * @param totalSlots the total number of slots
     * @return 0 when {@code totalSlots} is not positive, otherwise
     *         {@code freeSlots * 100 / totalSlots} rounded to an int and clamped to 0..100
     */
    public static int freePercent(long freeSlots, long totalSlots) {
        if (totalSlots <= 0) {
            return 0;
        }
        long percent = Math.round((double) freeSlots * 100.0 / (double) totalSlots);
        if (percent < 0) {
            return 0;
        }
        if (percent > 100) {
            return 100;
        }
        return (int) percent;
    }

    private UsageStats() {}
}
