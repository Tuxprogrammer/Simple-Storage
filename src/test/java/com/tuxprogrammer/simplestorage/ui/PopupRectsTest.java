package com.tuxprogrammer.simplestorage.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * Tests for {@link PopupRects}: popup click hit-testing (UI-06).
 */
public class PopupRectsTest {

    /**
     * UI-06: popup clicks must hit the rows that are drawn. The menu renders
     * at (x-12, y+12); entry i is drawn at (x-12, y+12+i*10, 100x8) and must
     * be clickable exactly there — not at the upstream (x, y+i*10) offset.
     */
    @Test
    public void doesNotReproduceUI06_popupClicksHitTheRowsThatAreDrawn() {
        int anchorX = 40;
        int anchorY = 20;
        PopupRects popups = new PopupRects(anchorX, anchorY, 4);

        assertEquals(anchorX - 12, popups.getDrawnX());
        assertEquals(anchorY + 12, popups.getDrawnY());

        // Each drawn entry's interior hits itself, and only itself.
        for (int i = 0; i < 4; i++) {
            int ex = popups.getEntryX(i);
            int ey = popups.getEntryY(i);
            assertEquals(anchorX - 12, ex);
            assertEquals(anchorY + 12 + i * PopupRects.ENTRY_SPACING, ey);
            assertEquals(i, popups.hit(ex + 50, ey + 4), "center of drawn row " + i + " hits row " + i);
        }

        // First and last rows are clickable at their drawn edges (the entries
        // upstream made easy to misclick).
        assertEquals(0, popups.hit(anchorX - 12, anchorY + 12), "first row's drawn top edge hits row 0");
        assertEquals(
            3,
            popups.hit(anchorX - 12 + 99, anchorY + 12 + 3 * PopupRects.ENTRY_SPACING + 7),
            "last row's drawn bottom-right corner hits row 3");

        // The upstream click rect (x, y+i*10) no longer applies: (anchorX, anchorY)
        // was row 0's click point upstream but is not a drawn row.
        assertEquals(-1, popups.hit(anchorX, anchorY), "upstream's first-row click point (anchor) is not a drawn row");

        // Gaps and outside the menu miss.
        assertEquals(-1, popups.hit(anchorX - 12 + 50, anchorY + 12 + 8), "gap between rows 0 and 1 misses");
        assertEquals(
            -1,
            popups.hit(anchorX - 12 + 50, anchorY + 12 + 4 * PopupRects.ENTRY_SPACING),
            "below the last row misses");
        assertEquals(-1, popups.hit(anchorX - 12 + 100, anchorY + 12 + 4), "right of the drawn menu misses");
        assertEquals(-1, popups.hit(anchorX - 12 - 1, anchorY + 12 + 4), "left of the drawn menu misses");
    }
}
