package com.tuxprogrammer.simplestorage.inventory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link CabinetModel}, covering the upstream bug fixes
 * STOR-02, CORE-05 and CORE-06 plus the persistence helpers.
 */
public class CabinetModelTest {

    private static final String IRON_SWORD = "minecraft:iron_sword";
    private static final String STONE_SWORD = "minecraft:stone_sword";

    /**
     * STOR-02: the lock is recomputed from the first non-empty slot, not taken
     * from slot 0 alone. A cabinet loaded with only slot 5 filled must already
     * be locked, and a second type must be refused.
     */
    @Test
    public void doesNotReproduceSTOR02_lockComesFromAnyNonEmptySlotNotOnlySlotZero() {
        Map<Integer, String> slots = new HashMap<Integer, String>();
        slots.put(5, IRON_SWORD);
        CabinetModel model = CabinetModel.fromSlots(slots);

        assertEquals(IRON_SWORD, model.lockType());
        // A different type is refused while locked, even into an empty slot.
        assertFalse(model.canPlace(3, STONE_SWORD, 1));
    }

    /**
     * STOR-02: while locked, {@link CabinetModel#set} refuses a different type
     * and leaves the model unchanged (no re-locking to the written type).
     */
    @Test
    public void doesNotReproduceSTOR02_differentTypeCannotReLockTheCabinet() {
        CabinetModel model = new CabinetModel();
        assertTrue(model.set(0, IRON_SWORD, 1));
        assertEquals(IRON_SWORD, model.lockType());

        // Writing a different type is refused; the model is unchanged.
        assertFalse(model.set(1, STONE_SWORD, 1));
        assertEquals(IRON_SWORD, model.lockType());
        assertEquals(1, model.count());
        assertNull(model.get(1));
        assertEquals(IRON_SWORD, model.get(0));
    }

    /**
     * CORE-05: the first item in an unlocked cabinet must be unstackable, and
     * the refusal message key is exposed and non-empty.
     */
    @Test
    public void doesNotReproduceCORE05_firstItemMustBeUnstackableAndRefusalKeyIsExposed() {
        CabinetModel model = new CabinetModel();
        // A stackable item (max stack size 64) cannot be the first item.
        assertFalse(model.canPlace(0, "minecraft:iron_ingot", 64));
        // An unstackable item (max stack size 1) can.
        assertTrue(model.canPlace(0, IRON_SWORD, 1));
        // The refusal message key is exposed and non-empty.
        assertNotNull(CabinetModel.REFUSAL_FIRST_STACKABLE);
        assertFalse(CabinetModel.REFUSAL_FIRST_STACKABLE.isEmpty());
    }

    /**
     * CORE-06: the container max stack size is 1, and nothing stackable can
     * enter; all 512 slots can be filled one by one and a 513th slot is out
     * of range.
     */
    @Test
    public void doesNotReproduceCORE06_maxStackSizeIsOneAndNothingStackableCanEnter() {
        CabinetModel model = new CabinetModel();
        assertEquals(1, model.maxStackSize());

        // Fill all 512 slots one by one; every placement succeeds.
        for (int i = 0; i < CabinetModel.SLOTS; i++) {
            assertTrue(model.set(i, IRON_SWORD, 1));
        }
        assertEquals(CabinetModel.SLOTS, model.count());
        assertFalse(model.isEmpty());

        // A 513th slot (index 512) is out of range.
        assertFalse(model.canPlace(CabinetModel.SLOTS, IRON_SWORD, 1));
    }

    /**
     * Removing a slot does not unlock the cabinet; the lock clears only when
     * the cabinet becomes completely empty.
     */
    @Test
    public void removingASlotKeepsTheLockUntilEmpty() {
        CabinetModel model = new CabinetModel();
        assertTrue(model.set(0, IRON_SWORD, 1));
        assertTrue(model.set(1, IRON_SWORD, 1));

        // Removing one slot keeps the lock.
        assertEquals(IRON_SWORD, model.remove(0));
        assertEquals(IRON_SWORD, model.lockType());
        assertEquals(1, model.count());
        assertFalse(model.isEmpty());
        // Still locked: another type is refused.
        assertFalse(model.canPlace(2, STONE_SWORD, 1));

        // The last removal clears the lock.
        assertEquals(IRON_SWORD, model.remove(1));
        assertNull(model.lockType());
        assertEquals(0, model.count());
        assertTrue(model.isEmpty());
    }

    /**
     * {@link CabinetModel#fromSlots} is tolerant of garbage: out-of-range
     * indexes, null and empty keys, and mixed types are all ignored without
     * throwing.
     */
    @Test
    public void fromSlotsToleratesGarbage() {
        Map<Integer, String> slots = new HashMap<Integer, String>();
        slots.put(-1, IRON_SWORD); // negative index
        slots.put(512, IRON_SWORD); // out-of-range index
        slots.put(2, null); // null key
        slots.put(3, ""); // empty key
        slots.put(7, IRON_SWORD); // valid, lowest-index real entry
        slots.put(9, STONE_SWORD); // mixed type: ignored
        slots.put(11, IRON_SWORD); // valid, same lock type

        CabinetModel model = CabinetModel.fromSlots(slots);

        assertEquals(IRON_SWORD, model.lockType());
        assertEquals(2, model.count());
        assertNull(model.get(2));
        assertNull(model.get(3));
        assertEquals(IRON_SWORD, model.get(7));
        assertNull(model.get(9));
        assertEquals(IRON_SWORD, model.get(11));

        // Null and empty maps also never throw.
        assertTrue(
            CabinetModel.fromSlots(null)
                .isEmpty());
        assertTrue(
            CabinetModel.fromSlots(new HashMap<Integer, String>())
                .isEmpty());
    }

    /**
     * {@link CabinetModel#toList} lists non-empty slot contents in slot order.
     */
    @Test
    public void toListIsInSlotOrder() {
        CabinetModel model = new CabinetModel();
        assertTrue(model.set(9, IRON_SWORD, 1));
        assertTrue(model.set(2, IRON_SWORD, 1));
        assertTrue(model.set(0, IRON_SWORD, 1));

        List<String> list = model.toList();
        assertEquals(3, list.size());
        assertEquals(IRON_SWORD, list.get(0)); // slot 0
        assertEquals(IRON_SWORD, list.get(1)); // slot 2
        assertEquals(IRON_SWORD, list.get(2)); // slot 9
        assertEquals(model.count(), list.size());
    }
}
