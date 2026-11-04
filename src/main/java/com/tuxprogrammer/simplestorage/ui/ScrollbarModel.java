package com.tuxprogrammer.simplestorage.ui;
// Ported from Tom's Simple Storage (MIT):
// https://github.com/tom5454/Toms-Storage/blob/95bb61d/NeoForge/src/platform-shared/java/com/tom/storagemod/screen/widget/ListWidget.java
// Avoids: UI-05

/**
 * Pure state and geometry for the scrollable list widget's scrollbar.
 *
 * <p>
 * Upstream ({@code ListWidget}) draws the thumb as a 5x9 px sprite at
 * {@code x + width - 6}, {@code y - 1 + (height - 9) * currentScroll} and maps
 * drag position with {@code currentScroll = clamp((mouseY - l - 4.5) / (58 - 9), 0, 1)}.
 *
 * <p>
 * UI-05: upstream's grab rectangle was hard-coded to {@code x+56 .. x+70},
 * {@code y-1 .. y+57} — on the 114 px wide inventory-link list the drawn thumb
 * sits at {@code x + 108}, so the grab area never overlaps the indicator.
 * Here the grab rectangle is derived from the same geometry that draws the
 * thumb: {@code x + width - 6, y - 1 + (height - 9) * currentScroll, 5 x 9},
 * so grab and draw can never drift apart.
 *
 * Side: client logic, plain Java.
 */
public final class ScrollbarModel {

    /** Thumb width in px (sprite {@code widget/small_scroll}). */
    public static final int THUMB_WIDTH = 5;

    /** Thumb height in px (sprite {@code widget/small_scroll}). */
    public static final int THUMB_HEIGHT = 9;

    /** Thumb offset from the list's top edge, in px. */
    public static final int THUMB_TOP_OFFSET = -1;

    /** Thumb offset from the list's right edge, in px. */
    public static final int THUMB_RIGHT_INSET = 6;

    private final int x;
    private final int y;
    private final int width;
    private final int height;
    private final int contentRows;
    private final int visibleRows;

    private double currentScroll;

    /**
     * Creates a scrollbar model for a list.
     *
     * @param x           the list's x position
     * @param y           the list's y position
     * @param width       the list's width in px (114 for the inventory-link list)
     * @param height      the list's height in px (100 for the inventory-link list)
     * @param contentRows the total number of rows in the list
     * @param visibleRows the number of rows visible at once (6 for the 114x100 list)
     */
    public ScrollbarModel(int x, int y, int width, int height, int contentRows, int visibleRows) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.contentRows = Math.max(0, contentRows);
        this.visibleRows = Math.max(0, visibleRows);
    }

    /**
     * @return the list's x position
     */
    public int getX() {
        return x;
    }

    /**
     * @return the list's y position
     */
    public int getY() {
        return y;
    }

    /**
     * @return the list's width in px
     */
    public int getWidth() {
        return width;
    }

    /**
     * @return the list's height in px
     */
    public int getHeight() {
        return height;
    }

    /**
     * @return the total number of rows in the list
     */
    public int getContentRows() {
        return contentRows;
    }

    /**
     * @return the number of visible rows
     */
    public int getVisibleRows() {
        return visibleRows;
    }

    /**
     * @return whether scrolling is needed (content does not fit in the list)
     */
    public boolean needsScroll() {
        return contentRows > visibleRows;
    }

    /**
     * @return the current scroll offset in [0, 1]; 0 = top, 1 = bottom
     */
    public double getCurrentScroll() {
        return currentScroll;
    }

    /**
     * Sets the scroll offset, clamped to [0, 1].
     *
     * @param scroll the new offset
     */
    public void setCurrentScroll(double scroll) {
        this.currentScroll = clamp(scroll);
    }

    /**
     * Applies a scroll step, mirroring upstream's wheel handling:
     * {@code currentScroll = clamp(currentScroll + dir / (listSize - visibleLines), 0, 1)}.
     *
     * @param dir the wheel direction (positive scrolls down)
     * @return true if the step was applied (scrolling was needed), false if consumed as a no-op
     */
    public boolean scroll(int dir) {
        if (!needsScroll()) {
            return false;
        }
        // A positive wheel step scrolls down (toward the bottom); the
        // upstream formula's sign is inverted by the GUI's wheel direction.
        currentScroll = clamp(currentScroll + (double) dir / (contentRows - visibleRows));
        return true;
    }

    /**
     * Maps a drag position to a scroll offset, mirroring upstream's formula
     * {@code clamp((mouseY - l - 4.5) / (58 - 9), 0, 1)} where {@code l} is the
     * list's top edge and the denominator is the thumb's travel distance.
     *
     * @param mouseY the mouse y position
     * @return the resulting scroll offset in [0, 1]
     */
    public double dragToScroll(double mouseY) {
        if (!needsScroll()) {
            return 0.0;
        }
        double travel = travelDistance();
        if (travel <= 0.0) {
            return 0.0;
        }
        return clamp((mouseY - (y + THUMB_TOP_OFFSET) - THUMB_HEIGHT / 2.0) / travel);
    }

    /**
     * Sets the scroll offset from a drag position.
     *
     * @param mouseY the mouse y position
     * @return the resulting scroll offset in [0, 1]
     */
    public double dragToCurrentScroll(double mouseY) {
        currentScroll = dragToScroll(mouseY);
        return currentScroll;
    }

    /**
     * @return the thumb's travel distance in px ({@code height - thumb height})
     */
    public double travelDistance() {
        return height - THUMB_HEIGHT;
    }

    /**
     * The x position of the drawn thumb: {@code x + width - 6}.
     *
     * @return the thumb's x position
     */
    public int getThumbX() {
        return x + width - THUMB_RIGHT_INSET;
    }

    /**
     * The y position of the drawn thumb:
     * {@code y - 1 + (height - 9) * currentScroll}.
     *
     * @return the thumb's y position
     */
    public int getThumbY() {
        return y + THUMB_TOP_OFFSET + (int) Math.round(travelDistance() * currentScroll);
    }

    /**
     * The thumb's height in px. Upstream draws a 9 px sprite; when the list is
     * very short the thumb would exceed the list, so it is clamped to a minimum
     * of 1 and the list's height.
     *
     * @return the thumb's height in px
     */
    public int getThumbHeight() {
        return Math.max(THUMB_HEIGHT, Math.min(THUMB_HEIGHT, height));
    }

    /**
     * UI-05 fix: the grab rectangle is exactly the drawn thumb rectangle
     * ({@code getThumbX(), getThumbY(), THUMB_WIDTH, thumb height}), so a grab
     * hit always matches the indicator the user sees.
     *
     * @param mouseX the mouse x position
     * @param mouseY the mouse y position
     * @return true if the mouse is inside the grab (drawn thumb) rectangle
     */
    public boolean grabs(int mouseX, int mouseY) {
        if (!needsScroll()) {
            return false;
        }
        int thumbY = getThumbY();
        int thumbH = getThumbHeight();
        return mouseX >= getThumbX() && mouseX < getThumbX() + THUMB_WIDTH
            && mouseY >= thumbY
            && mouseY < thumbY + thumbH;
    }

    private static double clamp(double v) {
        return Math.min(1.0, Math.max(0.0, v));
    }
}
