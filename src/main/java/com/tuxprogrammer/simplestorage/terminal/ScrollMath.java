package com.tuxprogrammer.simplestorage.terminal;
// Ported from Tom's Simple Storage (MIT):
// https://github.com/tom5454/Toms-Storage/blob/95bb61d/NeoForge/src/platform-shared/java/com/tom/storagemod/screen/AbstractStorageTerminalScreen.java
// Avoids: TERM-01, TERM-06

/**
 * Pure scroll math for a terminal-style list grid.
 *
 * <p>
 * All methods are static and depend only on their arguments, so they are
 * trivially testable without Minecraft or LWJGL on the classpath.
 *
 * Side: both (plain Java).
 */
public final class ScrollMath {

    /**
     * Maximum number of rows the scroll offset can reach.
     *
     * <p>
     * The list is laid out in a fixed number of columns, so the total row
     * count is the item count rounded up to a whole number of rows:
     * {@code ceil(totalItems / columns)}. Subtracting the visible rows and
     * clamping at zero yields the largest offset that still keeps the visible
     * window inside the list.
     *
     * <p>
     * Unlike the upstream screen (TERM-01), which hard-coded 5 visible rows
     * as if the terminal could never be taller, the visible row count is a
     * parameter here, so a tall terminal computes the correct range.
     *
     * @param totalItems  the number of items in the list; negative is treated as zero
     * @param columns     the number of columns in the grid; below 1 is treated as 1
     * @param visibleRows the number of visible rows; below 1 is treated as 1
     * @return the maximum scroll offset in rows; never negative
     */
    public static int maxScrollRows(int totalItems, int columns, int visibleRows) {
        int cols = Math.max(columns, 1);
        int rows = Math.max(visibleRows, 1);
        int items = Math.max(totalItems, 0);
        int totalRows = (items + cols - 1) / cols;
        return Math.max(0, totalRows - rows);
    }

    /**
     * Applies one mouse-wheel notch to a row scroll offset.
     *
     * <p>
     * One notch moves exactly one row: the step is {@code -1} when the wheel
     * delta is positive (wheel up scrolls toward the top of the list),
     * {@code +1} when the wheel delta is negative, and {@code 0} when the
     * delta is zero or {@code NaN}. The resulting offset is clamped to
     * {@code 0..max(0, maxRows)}, so a scrollable range of zero rows (for
     * example a cabinet holding exactly 45 items in 9 columns with 5 visible
     * rows, the upstream TERM-06 case) is a plain no-op instead of dividing
     * the delta by zero and jumping the whole list in one notch.
     *
     * @param offset     the current scroll offset in rows
     * @param maxRows    the maximum scroll offset in rows (e.g. from {@link #maxScrollRows})
     * @param wheelDelta the raw mouse-wheel delta
     * @return the clamped scroll offset after the notch
     */
    public static int scrolledOffset(int offset, int maxRows, double wheelDelta) {
        int step;
        if (wheelDelta > 0.0) {
            step = -1; // wheel up scrolls toward the top
        } else if (wheelDelta < 0.0) {
            step = 1;
        } else {
            step = 0; // covers zero and NaN
        }
        return clampOffset(offset + step, maxRows);
    }

    /**
     * Clamps a scroll offset to the legal range.
     *
     * @param offset  the scroll offset in rows
     * @param maxRows the maximum scroll offset in rows; negative is treated as zero
     * @return the offset clamped to {@code 0..max(0, maxRows)}
     */
    public static int clampOffset(int offset, int maxRows) {
        int max = Math.max(maxRows, 0);
        if (offset < 0) {
            return 0;
        }
        if (offset > max) {
            return max;
        }
        return offset;
    }

    /**
     * Computes the scrollbar position as a fraction of the full range.
     *
     * @param offsetRows the current scroll offset in rows
     * @param maxRows    the maximum scroll offset in rows
     * @return 0 when {@code maxRows} is not positive, otherwise
     *         {@code offsetRows / maxRows} clamped to 0..1
     */
    public static float scrollFraction(int offsetRows, int maxRows) {
        if (maxRows <= 0) {
            return 0.0f;
        }
        float fraction = offsetRows / (float) maxRows;
        if (fraction < 0.0f) {
            return 0.0f;
        }
        if (fraction > 1.0f) {
            return 1.0f;
        }
        return fraction;
    }

    private ScrollMath() {}
}
