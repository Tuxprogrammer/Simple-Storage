package com.tuxprogrammer.simplestorage.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Tests for {@link ScrollbarModel}: scrollbar geometry (UI-05), scroll
 * clamping, thumb size and empty content.
 */
public class ScrollbarModelTest {

    /**
     * UI-05: the grab rectangle must match the drawn thumb. Upstream's grab
     * rect was hard-coded at x+56..x+70 while the 114 px wide list drew its
     * thumb at x+108; the two never overlapped. Here a grab hit at the thumb's
     * drawn position succeeds and upstream's hard-coded region misses.
     */
    @Test
    public void doesNotReproduceUI05_grabRectMatchesTheDrawnThumb() {
        ScrollbarModel model = new ScrollbarModel(0, 0, 114, 100, 30, 6);
        model.setCurrentScroll(0.5);

        int thumbX = model.getThumbX();
        int thumbY = model.getThumbY();
        assertEquals(108, thumbX, "thumb x = x + width - 6");
        assertEquals((int) Math.round((100 - 9) * 0.5) - 1, thumbY, "thumb y = y - 1 + (height - 9) * scroll");

        assertTrue(model.grabs(thumbX, thumbY), "grab rect contains the drawn thumb's top-left");
        assertTrue(model.grabs(thumbX + 4, thumbY + 8), "grab rect contains the thumb's bottom-right corner");

        int upstreamGrabLeft = 0 + 56;
        int upstreamGrabRight = 0 + 70;
        for (int mx = upstreamGrabLeft; mx < upstreamGrabRight; mx++) {
            for (int my = 0 - 1; my <= 0 + 57; my++) {
                if (model.grabs(mx, my)) {
                    // Any overlap with the old hard-coded rect means the fix regressed.
                    int tx = model.getThumbX();
                    int ty = model.getThumbY();
                    assertTrue(
                        mx < tx || mx >= tx + ScrollbarModel.THUMB_WIDTH
                            || my < ty
                            || my >= ty + model.getThumbHeight(),
                        "upstream grab rect should not overlap the drawn thumb at scroll 0.5");
                }
            }
        }

        // A drag-to-scroll round trip lands exactly where the mouse says.
        double mid = (model.getY() + ScrollbarModel.THUMB_TOP_OFFSET) + model.travelDistance() / 2.0
            + ScrollbarModel.THUMB_HEIGHT / 2.0;
        assertEquals(0.5, model.dragToScroll(mid), 1e-9, "drag maps thumb center to scroll 0.5");
        assertEquals(model.dragToScroll(mid), model.getCurrentScroll());
        model.setCurrentScroll(0.0);
        assertEquals(model.dragToScroll(mid), model.dragToCurrentScroll(mid), 1e-9);
    }

    /**
     * Scroll offset stays clamped to [0, 1] under wheel steps, drags and
     * direct sets.
     */
    @Test
    public void scrollClamping() {
        ScrollbarModel model = new ScrollbarModel(0, 0, 114, 100, 30, 6);

        model.setCurrentScroll(-5.0);
        assertEquals(0.0, model.getCurrentScroll());

        model.setCurrentScroll(7.0);
        assertEquals(1.0, model.getCurrentScroll());

        // Wheel: positive direction scrolls toward the bottom; 24 steps reach
        // the end.
        model.setCurrentScroll(0.0);
        for (int i = 0; i < 24; i++) {
            assertTrue(model.scroll(1), "scroll consumes wheel while content overflows");
        }
        // 24 steps of 1/24 sum to 0.9999999999999996 in floating point, so
        // compare with a tolerance.
        assertEquals(1.0, model.getCurrentScroll(), 1e-9, "scroll clamps at the bottom");

        // Extra steps keep the offset pinned at 1.0.
        model.scroll(1);
        model.scroll(1);
        assertEquals(1.0, model.getCurrentScroll(), 1e-9);

        for (int i = 0; i < 24; i++) {
            assertTrue(model.scroll(-1));
        }
        assertEquals(0.0, model.getCurrentScroll(), 1e-9, "scroll clamps at the top");

        // Drag positions beyond the travel range clamp.
        assertEquals(0.0, model.dragToScroll(model.getY() - 100.0));
        assertEquals(1.0, model.dragToScroll(model.getY() + 1000.0));
    }

    /**
     * The thumb keeps a usable minimum size even when the list is shorter
     * than the 9 px sprite.
     */
    @Test
    public void thumbSizeMinimum() {
        ScrollbarModel tiny = new ScrollbarModel(0, 0, 114, 3, 10, 2);
        assertEquals(ScrollbarModel.THUMB_HEIGHT, tiny.getThumbHeight(), "thumb keeps the fixed 9 px sprite height");

        ScrollbarModel normal = new ScrollbarModel(0, 0, 114, 100, 30, 6);
        assertEquals(ScrollbarModel.THUMB_HEIGHT, normal.getThumbHeight(), "normal lists keep the 9 px sprite height");
    }

    /**
     * Empty content: no scrolling is needed, the wheel is a no-op and the
     * grab rectangle never matches.
     */
    @Test
    public void emptyContent() {
        ScrollbarModel empty = new ScrollbarModel(0, 0, 114, 100, 0, 6);
        assertFalse(empty.needsScroll());
        assertFalse(empty.scroll(1), "wheel is a no-op without overflow");
        assertEquals(0.0, empty.getCurrentScroll());
        assertFalse(empty.grabs(108, 5), "no grab without overflow");
        assertEquals(0.0, empty.dragToScroll(1000.0), "no travel without overflow");

        // Content exactly filling the list: no overflow, no scroll.
        ScrollbarModel exact = new ScrollbarModel(0, 0, 114, 100, 6, 6);
        assertFalse(exact.needsScroll());
        assertFalse(exact.scroll(1));

        // Just one row over: scrolling engages.
        ScrollbarModel overflow = new ScrollbarModel(0, 0, 114, 100, 7, 6);
        assertTrue(overflow.needsScroll());
        assertTrue(overflow.scroll(1));
    }
}
