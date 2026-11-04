package com.tuxprogrammer.simplestorage.filter;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * Regression tests for {@link FilterSlotLogic}.
 *
 * Side: both (plain Java).
 */
public class FilterSlotLogicTest {

    /**
     * A shift-clicked item that already exists after the first empty slot must
     * return to its existing slot, not be written into the empty slot and left
     * duplicated (TERM-11).
     */
    @Test
    public void doesNotReproduceTERM11_itemAfterTheFirstEmptySlotIsNotDuplicated() {
        // Slot 1 holds "apple" while slot 0 is empty (the upstream state after
        // its own duplication). Shift-clicking an apple must target the
        // existing slot 1, not the earlier empty slot 0.
        String[] ghostSlots = { null, "apple", null, null, null, null, null, null, null };
        int target = FilterSlotLogic.shiftClickTarget(ghostSlots, "apple");
        assertEquals(1, target);
        ghostSlots[target] = "apple";
        long apples = 0;
        for (String slot : ghostSlots) {
            if ("apple".equals(slot)) {
                apples++;
            }
        }
        assertEquals(1, apples);
    }

    /**
     * A shift-clicked item with no existing slot goes to the first empty slot.
     */
    @Test
    public void newItemGoesToFirstEmptySlot() {
        String[] ghostSlots = { "iron", null, "gold", null, null, null, null, null, null };
        assertEquals(1, FilterSlotLogic.shiftClickTarget(ghostSlots, "diamond"));
    }

    /**
     * An item already present in the menu goes back to its slot, before any
     * empty slot is considered.
     */
    @Test
    public void existingItemGoesToItsOwnSlot() {
        String[] ghostSlots = { "iron", null, "gold", null, null, null, null, null, null };
        assertEquals(2, FilterSlotLogic.shiftClickTarget(ghostSlots, "gold"));
    }

    /**
     * An existing slot beats an earlier empty slot.
     */
    @Test
    public void existingSlotWinsOverEarlierEmptySlot() {
        String[] ghostSlots = { null, null, null, null, "emerald", null, null, null, null };
        assertEquals(4, FilterSlotLogic.shiftClickTarget(ghostSlots, "emerald"));
    }

    /**
     * A fully occupied menu with no matching item has nowhere to go.
     */
    @Test
    public void fullMenuWithNoMatchReturnsMinusOne() {
        String[] ghostSlots = { "a", "b", "c", "d", "e", "f", "g", "h", "i" };
        assertEquals(-1, FilterSlotLogic.shiftClickTarget(ghostSlots, "j"));
    }

    /**
     * A fully occupied menu with a matching item still targets that slot.
     */
    @Test
    public void fullMenuWithMatchReturnsMatchedSlot() {
        String[] ghostSlots = { "a", "b", "c", "d", "e", "f", "g", "h", "i" };
        assertEquals(5, FilterSlotLogic.shiftClickTarget(ghostSlots, "f"));
    }

    /**
     * A completely empty menu targets slot 0.
     */
    @Test
    public void emptyMenuTargetsSlotZero() {
        String[] ghostSlots = new String[9];
        assertEquals(0, FilterSlotLogic.shiftClickTarget(ghostSlots, "anything"));
    }

    /**
     * A null menu is treated as having no slots: -1.
     */
    @Test
    public void nullMenuReturnsMinusOne() {
        assertEquals(-1, FilterSlotLogic.shiftClickTarget(null, "anything"));
    }

    /**
     * A null key is never matched by an existing slot and falls through to the
     * first empty slot (or -1).
     */
    @Test
    public void nullKeyFallsToFirstEmptySlot() {
        String[] ghostSlots = { "iron", null, "gold" };
        assertEquals(1, FilterSlotLogic.shiftClickTarget(ghostSlots, null));
        String[] full = { "iron", "gold", "diamond" };
        assertEquals(-1, FilterSlotLogic.shiftClickTarget(full, null));
    }
}
