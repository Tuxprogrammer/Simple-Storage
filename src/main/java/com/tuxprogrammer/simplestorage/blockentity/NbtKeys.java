package com.tuxprogrammer.simplestorage.blockentity;

/**
 * Central registry of NBT keys used by SimpleStorage+ tile entities.
 *
 * <p>
 * These keys are part of the saved format: once a mod version that writes
 * them has shipped, the key names must never change or players lose their
 * data. New keys may be added, but existing ones are permanent. This is what
 * makes config-safe registration possible — tile entities can read and
 * tolerate keys they don't know about without corrupting saves.
 */
public final class NbtKeys {

    /**
     * Private constructor; {@link NbtKeys} is a constants holder, never
     * instantiated.
     */
    private NbtKeys() {}

    /** Key holding the inventory compound (item stacks in storage). */
    public static final String INVENTORY = "inventory";

    /** Key holding the custom name for a named block. */
    public static final String CUSTOM_NAME = "CustomName";

    /** Key holding the filter (item id) for a filterable block. */
    public static final String FILTER = "Filter";

    /** Key holding the count for a quantity-limited block. */
    public static final String COUNT = "Count";

    /** Key holding the "less than" threshold for a comparator-style block. */
    public static final String LESS_THAN = "lessThan";

    /** Key holding the paint block id (the block used for the painted face). */
    public static final String PAINT_BLOCK = "paintBlock";

    /** Key holding the paint block metadata (the meta of the painted block). */
    public static final String PAINT_META = "paintMeta";

    /** Key holding the slot index for slot-specific data. */
    public static final String SLOT = "Slot";

    /** Key holding the "waiting" flag for asynchronous operations. */
    public static final String WAITING = "waiting";
}
