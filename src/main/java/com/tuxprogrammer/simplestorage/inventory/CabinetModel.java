package com.tuxprogrammer.simplestorage.inventory;
// Ported from Tom's Simple Storage (MIT):
// https://github.com/tom5454/Toms-Storage/blob/95bb61d/NeoForge/src/platform-shared/java/com/tom/storagemod/util/FilingCabinetContainer.java
// Avoids: STOR-02, CORE-05, CORE-06

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Pure-Java model of a filing cabinet: a container of {@link #SLOTS} single-item
 * slots, all holding items of exactly ONE item type.
 *
 * <p>
 * An "item type" is an opaque {@link String} key such as {@code "minecraft:iron_sword"};
 * no Minecraft types appear in any signature. Every slot holds at most one item
 * (max stack size 1), so a slot is either empty ({@code null}) or one item.
 *
 * <p>
 * The cabinet has a <em>lock type</em>, which is the type of its items and is
 * {@code null} while the cabinet is empty. The lock is always <em>recomputed from
 * the slots</em> (the type of the first non-empty slot, scanning from slot 0) and
 * never cached, so a cabinet whose slot 0 is empty still carries its lock.
 *
 * <p>
 * Rules:
 * <ul>
 * <li>While locked, only the locked type may be placed (STOR-02: a different
 * type is refused and leaves the model unchanged, so it cannot re-lock the
 * cabinet to another type).</li>
 * <li>While unlocked (empty), only unstackable items (max stack size == 1) may
 * enter (CORE-05; refusal message key {@link #REFUSAL_FIRST_STACKABLE}).</li>
 * <li>The container-level max stack size is always 1 (CORE-06); this is lossless
 * because only unstackable items can enter.</li>
 * <li>The lock clears only when the whole cabinet becomes empty; removing one
 * slot does not unlock the cabinet.</li>
 * </ul>
 *
 * <p>
 * Side: both (plain Java).
 */
public class CabinetModel {

    /** The number of slots in a filing cabinet. */
    public static final int SLOTS = 512;

    /**
     * Translation key for the refusal message shown when a stackable item (max
     * stack size greater than 1) is the first item placed in the cabinet.
     * A cabinet stores unstackable variants of one item only (CORE-05).
     */
    public static final String REFUSAL_FIRST_STACKABLE = "simplestoragenh.filing_cabinet.first_must_be_unstackable";

    /** Slot contents: slot i holds a type key or {@code null} if empty. */
    private final String[] slots;

    /**
     * Creates an empty cabinet (all slots null, no lock).
     */
    public CabinetModel() {
        this.slots = new String[SLOTS];
    }

    /**
     * Returns the number of slots in a filing cabinet.
     *
     * @return always {@value #SLOTS}
     */
    public static int slotCount() {
        return SLOTS;
    }

    /**
     * Tests whether an item of the given type could be placed into the given slot.
     *
     * <p>
     * Refused when: the slot index is out of range, the type is {@code null},
     * the cabinet is locked to a different type, or the cabinet is unlocked (empty)
     * and the item is stackable (max stack size other than 1). A slot that already
     * holds the same type may be replaced.
     *
     * @param slot         slot index, 0 to {@link #SLOTS} exclusive
     * @param type         item type key, non-null
     * @param maxStackSize the item's max stack size (1 means unstackable)
     * @return {@code true} if the item could be placed in the slot
     */
    public boolean canPlace(int slot, String type, int maxStackSize) {
        if (slot < 0 || slot >= SLOTS) {
            return false;
        }
        if (type == null) {
            return false;
        }
        String lock = lockType();
        if (lock != null) {
            // Locked: only the same type may enter (replacing an equal occupant is fine).
            return lock.equals(type);
        }
        // Unlocked (empty cabinet): the first item must be unstackable (CORE-05).
        return maxStackSize == 1;
    }

    /**
     * Places an item in the given slot when {@link #canPlace} allows it.
     *
     * <p>
     * STOR-02 fix: unlike the upstream {@code setItem}, this method checks the
     * lock first, so a different type is refused and the model is left unchanged
     * (no re-locking to an arbitrary written type). Placing replaces any
     * occupant of the same type.
     *
     * @param slot         slot index, 0 to {@link #SLOTS} exclusive
     * @param type         item type key, non-null
     * @param maxStackSize the item's max stack size (1 means unstackable)
     * @return {@code true} if the item was placed
     */
    public boolean set(int slot, String type, int maxStackSize) {
        if (!canPlace(slot, type, maxStackSize)) {
            return false;
        }
        slots[slot] = type;
        return true;
    }

    /**
     * Removes and returns the item in the given slot.
     *
     * <p>
     * The lock is derived from the slots, so it clears only when this was the
     * last item in the cabinet; removing one slot of a still-occupied cabinet
     * keeps the lock.
     *
     * @param slot slot index, 0 to {@link #SLOTS} exclusive
     * @return the type that was in the slot, or {@code null} if the slot was empty
     *         (an out-of-range index behaves the same as an empty slot)
     */
    public String remove(int slot) {
        if (slot < 0 || slot >= SLOTS) {
            return null;
        }
        String type = slots[slot];
        slots[slot] = null;
        return type;
    }

    /**
     * Returns the item type in the given slot.
     *
     * @param slot slot index, 0 to {@link #SLOTS} exclusive
     * @return the type key, or {@code null} if the slot is empty or out of range
     */
    public String get(int slot) {
        if (slot < 0 || slot >= SLOTS) {
            return null;
        }
        return slots[slot];
    }

    /**
     * Returns the cabinet's lock type: the type of the first non-empty slot,
     * scanning from slot 0, recomputed on every call (never cached from slot 0
     * alone; STOR-02).
     *
     * @return the locked item type, or {@code null} if the cabinet is empty
     */
    public String lockType() {
        for (int i = 0; i < SLOTS; i++) {
            String type = slots[i];
            if (type != null) {
                return type;
            }
        }
        return null;
    }

    /**
     * Returns the number of items currently in the cabinet.
     *
     * @return count of non-empty slots
     */
    public int count() {
        int count = 0;
        for (String type : slots) {
            if (type != null) {
                count++;
            }
        }
        return count;
    }

    /**
     * Tests whether the cabinet holds no items at all.
     *
     * @return {@code true} if every slot is empty
     */
    public boolean isEmpty() {
        return count() == 0;
    }

    /**
     * Returns the container-level max stack size.
     *
     * <p>
     * CORE-06: a filing cabinet's container max stack size is 1. This is
     * lossless because only unstackable items (max stack size 1) can enter the
     * cabinet in the first place (see {@link #REFUSAL_FIRST_STACKABLE}).
     *
     * @return always {@code 1}
     */
    public int maxStackSize() {
        return 1;
    }

    /**
     * Serializes the cabinet for persistence: the type keys of all non-empty
     * slots, in slot order.
     *
     * @return an unmodifiable-order list of type keys (never contains nulls);
     *         empty list if the cabinet is empty
     */
    public List<String> toList() {
        List<String> out = new ArrayList<String>(count());
        for (String type : slots) {
            if (type != null) {
                out.add(type);
            }
        }
        return out;
    }

    /**
     * Rebuilds a cabinet from persisted slot data (tolerant of garbage; never
     * throws).
     *
     * <p>
     * Behavior:
     * <ul>
     * <li>Out-of-range slot indexes and {@code null}/{@code ""} type keys are
     * ignored.</li>
     * <li>The lock type is derived from the lowest-index valid entry; any other
     * entry whose type differs from that lock type is ignored (mixed types
     * in one cabinet is corrupt data).</li>
     * <li>If the lowest-index entry is itself the only "valid" entry, it becomes
     * the lock and is kept.</li>
     * </ul>
     *
     * @param slots mapping of slot index to type key; may be {@code null} or empty
     * @return the rebuilt cabinet; never {@code null}
     */
    public static CabinetModel fromSlots(Map<Integer, String> slots) {
        CabinetModel model = new CabinetModel();
        if (slots == null || slots.isEmpty()) {
            return model;
        }

        // Pass 1: find the lowest-index valid entry; it defines the lock type.
        String lock = null;
        int lockIndex = -1;
        for (Map.Entry<Integer, String> entry : slots.entrySet()) {
            Integer key = entry.getKey();
            String type = entry.getValue();
            if (key == null || type == null || type.isEmpty()) {
                continue;
            }
            int slot = key.intValue();
            if (slot < 0 || slot >= SLOTS) {
                continue;
            }
            if (lockIndex < 0 || slot < lockIndex) {
                lockIndex = slot;
                lock = type;
            }
        }
        if (lock == null) {
            return model;
        }

        // Pass 2: keep every valid entry matching the lock type.
        for (Map.Entry<Integer, String> entry : slots.entrySet()) {
            Integer key = entry.getKey();
            String type = entry.getValue();
            if (key == null || type == null || type.isEmpty()) {
                continue;
            }
            int slot = key.intValue();
            if (slot < 0 || slot >= SLOTS) {
                continue;
            }
            if (lock.equals(type)) {
                model.slots[slot] = type;
            }
        }
        return model;
    }
}
