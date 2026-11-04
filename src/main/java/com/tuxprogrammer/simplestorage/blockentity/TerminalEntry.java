package com.tuxprogrammer.simplestorage.blockentity;

import net.minecraft.item.ItemStack;

/**
 * Immutable description of one stack type held across the backend's inventories.
 * Side: server logic.
 */
public final class TerminalEntry {

    private final String key;
    private final ItemStack prototype;
    private final long count;

    /**
     * Creates a terminal entry.
     * 
     * @param key       the stack key
     * @param prototype a single-stack copy of the item, never null
     * @param count     the total count across all inventories
     */
    public TerminalEntry(String key, ItemStack prototype, long count) {
        this.key = key;
        this.prototype = prototype;
        this.count = count;
    }

    /**
     * @return the stack key
     */
    public String key() {
        return key;
    }

    /**
     * @return a copy of the single-stack prototype
     */
    public ItemStack prototype() {
        return prototype.copy();
    }

    /**
     * @return the total count across all inventories
     */
    public long count() {
        return count;
    }

    /**
     * @param count the new total count
     * @return a new entry with the same key and prototype but the given count
     */
    public TerminalEntry withCount(long count) {
        return new TerminalEntry(key, prototype.copy(), count);
    }
}
