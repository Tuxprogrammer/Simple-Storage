package com.tuxprogrammer.simplestorage.sort;

// Plain-data key carrying the fields the terminal sort comparators read,
// so no Minecraft types leak into the sorting logic.

/**
 * Immutable plain-data key describing one terminal item stack for sorting.
 * Holds exactly the fields the five ported sort modes compare against
 * (amount, display name, mod id, numeric id, stack limit, and the stable
 * registry-name sort id used as the final tie-break). No Minecraft types
 * appear in any signature.
 */
public final class SortKey {

    private final long amount;
    private final String name;
    private final String modId;
    private final int id;
    private final int stackLimit;
    private final String sortId;

    /**
     * Creates a new sort key.
     *
     * @param amount     quantity of items in the stack
     * @param name       display name (locale-dependent)
     * @param modId      mod id (namespace) of the item
     * @param id         numeric item id
     * @param stackLimit max stack size (space efficiency)
     * @param sortId     stable string such as the registry name, final tie-break
     */
    public SortKey(long amount, String name, String modId, int id, int stackLimit, String sortId) {
        this.amount = amount;
        this.name = name;
        this.modId = modId;
        this.id = id;
        this.stackLimit = stackLimit;
        this.sortId = sortId;
    }

    /** Returns the quantity of items in the stack. */
    public long getAmount() {
        return amount;
    }

    /** Returns the display name (locale-dependent). */
    public String getName() {
        return name;
    }

    /** Returns the mod id (namespace) of the item. */
    public String getModId() {
        return modId;
    }

    /** Returns the numeric item id. */
    public int getId() {
        return id;
    }

    /** Returns the max stack size (space efficiency). */
    public int getStackLimit() {
        return stackLimit;
    }

    /** Returns the stable string (registry name) used as the final tie-break. */
    public String getSortId() {
        return sortId;
    }
}
