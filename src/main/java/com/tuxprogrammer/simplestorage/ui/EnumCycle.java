package com.tuxprogrammer.simplestorage.ui;
// Ported from Tom's Simple Storage (MIT):
// https://github.com/tom5454/Toms-Storage/blob/95bb61d/NeoForge/src/platform-shared/java/com/tom/storagemod/screen/widget/EnumCycleButton.java
// Avoids: UI-04

/**
 * Pure cycling over {@code count} enum-like values (indices 0 to count-1),
 * as done by the upstream {@code EnumCycleButton}: a press cycles forward
 * ({@code (state.ordinal() + 1) % length}), shift+press cycles reverse
 * ({@code (state.ordinal() - 1 + length) % length}).
 *
 * <p>
 * UI-04 fix (the EnumCycleButton NPE of the spec's bug list: the press
 * handler dereferenced {@code getState().ordinal()} while {@code state} was
 * still null, so a press before the first {@code setState} threw a
 * NullPointerException): a null (or out-of-range) current value is treated as
 * index 0, so cycling never throws.
 *
 * <p>
 * See {@link ScrollbarModel}.
 * <p>
 * Side: client logic, plain Java.
 */
public final class EnumCycle {

    private EnumCycle() {}

    /**
     * Cycles to the next value: {@code (current + 1) % count}. A null or
     * out-of-range {@code current} is treated as index 0.
     *
     * @param current the current index (may be invalid; treated as 0 if so)
     * @param count   the number of values (0 = no values)
     * @return the next index in [0, count), or -1 if there are no values
     */
    public static int next(int current, int count) {
        if (count <= 0) {
            return -1;
        }
        int c = normalize(current, count);
        return (c + 1) % count;
    }

    /**
     * Cycles to the previous value (reverse cycle, as with shift+press):
     * {@code (current - 1 + count) % count}. A null or out-of-range
     * {@code current} is treated as index 0.
     *
     * @param current the current index (may be invalid; treated as 0 if so)
     * @param count   the number of values (0 = no values)
     * @return the previous index in [0, count), or -1 if there are no values
     */
    public static int previous(int current, int count) {
        if (count <= 0) {
            return -1;
        }
        // Invalid (null/out-of-range) current means "no state yet": reverse
        // cycling starts at the last value. A valid 0 still wraps to count-1.
        if (current < 0 || current >= count) {
            return count - 1;
        }
        return (current - 1 + count) % count;
    }

    /**
     * UI-04 fix: a null or out-of-range current value must not throw.
     *
     * @param current the raw current index (may be null or out of range)
     * @param count   the number of values
     * @return 0 when {@code current} is null or outside [0, count),
     *         otherwise {@code current}
     */
    public static int normalize(Integer current, int count) {
        if (current == null || count <= 0 || current < 0 || current >= count) {
            return 0;
        }
        return current;
    }

    private static int normalize(int current, int count) {
        if (current < 0 || current >= count) {
            return 0;
        }
        return current;
    }
}
