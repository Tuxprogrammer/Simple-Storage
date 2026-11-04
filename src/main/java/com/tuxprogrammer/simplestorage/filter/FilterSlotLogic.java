package com.tuxprogrammer.simplestorage.filter;
// Ported from Tom's Simple Storage (MIT):
// https://github.com/tom5454/Toms-Storage/blob/95bb61d/NeoForge/src/platform-shared/java/com/tom/storagemod/menu/ItemFilterMenu.java
// Avoids: TERM-11

/**
 * Pure slot-selection logic for the 9-slot item filter menu.
 *
 * Side: both (plain Java).
 */
public final class FilterSlotLogic {

    /**
     * Decides which filter slot a shift-clicked item should be written to.
     *
     * <p>
     * A slot is identified by a non-null key when it holds an item and by
     * {@code null} when it is empty. The returned index is:
     * <ul>
     * <li>the index of an existing slot holding the same key, so the item
     * lands where it already is and is never duplicated (TERM-11:
     * upstream wrote into the first empty slot but kept scanning, so an
     * item sitting after the first empty slot ended up in the filter
     * twice);</li>
     * <li>else the first empty (null) slot;</li>
     * <li>else -1 when all 9 slots hold other items.</li>
     * </ul>
     *
     * @param ghostSlots the 9 filter slots; each entry is a key or {@code null}
     * @param key        the key of the shift-clicked item; must be non-null
     * @return the target slot index in 0..8, or -1 when no slot can hold the item
     */
    public static int shiftClickTarget(String[] ghostSlots, String key) {
        if (key != null) {
            for (int i = 0; ghostSlots != null && i < ghostSlots.length; i++) {
                if (key.equals(ghostSlots[i])) {
                    return i;
                }
            }
        }
        for (int i = 0; ghostSlots != null && i < ghostSlots.length; i++) {
            if (ghostSlots[i] == null) {
                return i;
            }
        }
        return -1;
    }

    private FilterSlotLogic() {}
}
