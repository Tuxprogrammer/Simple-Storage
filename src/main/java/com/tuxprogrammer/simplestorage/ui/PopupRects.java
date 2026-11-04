package com.tuxprogrammer.simplestorage.ui;
// Ported from Tom's Simple Storage (MIT):
// https://github.com/tom5454/Toms-Storage/blob/95bb61d/NeoForge/src/platform-shared/java/com/tom/storagemod/util/PopupMenuManager.java
// Avoids: UI-06

/**
 * Pure hit-testing for the popup menu (tooltip-anchored) entry rectangles.
 *
 * <p>
 * Upstream ({@code PopupMenuManager}) renders the menu as a tooltip anchored
 * at {@code (x - 12, y + 12)} with entries 100x8 px each stacked 10 px apart,
 * but {@code mouseClick} hit-tested entry i at {@code (x, y + i*10, 100x8)} —
 * the click target was shifted ~12 px right and ~12 px up from what was drawn.
 *
 * <p>
 * UI-06 fix: the click rectangles here are derived from the rendered anchor
 * {@code (x - 12, y + 12)}, so each entry's hit area is exactly the 100x8
 * rectangle that is drawn at row i.
 *
 * Side: client logic, plain Java.
 */
public final class PopupRects {

    /** Entry width in px. */
    public static final int ENTRY_WIDTH = 100;

    /** Entry height in px. */
    public static final int ENTRY_HEIGHT = 8;

    /** Row spacing in px (each entry's top is 10 px below the previous). */
    public static final int ENTRY_SPACING = 10;

    /** Render anchor offset in x, relative to the click anchor. */
    public static final int ANCHOR_OFFSET_X = -12;

    /** Render anchor offset in y, relative to the click anchor. */
    public static final int ANCHOR_OFFSET_Y = 12;

    private final int anchorX;
    private final int anchorY;
    private final int count;

    /**
     * Creates popup hit rectangles for a menu.
     *
     * @param anchorX the popup's x anchor (the point the menu is opened at)
     * @param anchorY the popup's y anchor (the point the menu is opened at)
     * @param count   the number of entries
     */
    public PopupRects(int anchorX, int anchorY, int count) {
        this.anchorX = anchorX;
        this.anchorY = anchorY;
        this.count = Math.max(0, count);
    }

    /**
     * @return the menu's x anchor
     */
    public int getAnchorX() {
        return anchorX;
    }

    /**
     * @return the menu's y anchor
     */
    public int getAnchorY() {
        return anchorY;
    }

    /**
     * @return the number of entries
     */
    public int getCount() {
        return count;
    }

    /**
     * The x of the drawn menu (the render anchor): {@code anchorX - 12}.
     *
     * @return the drawn menu's x
     */
    public int getDrawnX() {
        return anchorX + ANCHOR_OFFSET_X;
    }

    /**
     * The y of the drawn menu (the render anchor): {@code anchorY + 12}.
     *
     * @return the drawn menu's y
     */
    public int getDrawnY() {
        return anchorY + ANCHOR_OFFSET_Y;
    }

    /**
     * The x of entry i's drawn (and clickable) rectangle.
     *
     * @param i the entry index
     * @return the entry's x
     */
    public int getEntryX(int i) {
        return getDrawnX();
    }

    /**
     * The y of entry i's drawn (and clickable) rectangle:
     * {@code anchorY + 12 + i * 10}.
     *
     * @param i the entry index
     * @return the entry's y
     */
    public int getEntryY(int i) {
        return getDrawnY() + i * ENTRY_SPACING;
    }

    /**
     * UI-06 fix: hit-tests the drawn entry rectangles — entry i is clickable
     * exactly where it is drawn ({@code getDrawnX(), getEntryY(i), 100x8}).
     *
     * @param mouseX the mouse x position
     * @param mouseY the mouse y position
     * @return the entry index if the mouse is inside its drawn rectangle,
     *         otherwise -1
     */
    public int hit(int mouseX, int mouseY) {
        for (int i = 0; i < count; i++) {
            int ex = getEntryX(i);
            int ey = getEntryY(i);
            if (mouseX >= ex && mouseX < ex + ENTRY_WIDTH && mouseY >= ey && mouseY < ey + ENTRY_HEIGHT) {
                return i;
            }
        }
        return -1;
    }
}
