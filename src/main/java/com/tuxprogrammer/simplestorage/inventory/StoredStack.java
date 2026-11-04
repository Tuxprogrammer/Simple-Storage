package com.tuxprogrammer.simplestorage.inventory;

import java.util.Objects;

/**
 * A merged item stack: the identity of one item type plus its total count.
 *
 * <p>
 * This is the plain-Java form of the upstream {@code StoredItemStack}: the
 * opaque {@code key} stands in for the item plus damage plus NBT
 * (the data-component patch), and the {@code long} count allows networks to
 * hold more than {@code Integer.MAX_VALUE} of one item type. Equality is by
 * key only; the count is excluded, so two stacks of the same item are equal
 * whatever their counts.
 *
 * <p>
 * Side: both (plain Java).
 */
public final class StoredStack {

    /** The opaque identity of the item type (item plus damage plus NBT). */
    private final String key;

    /** The total count of items of this type. */
    private final long count;

    /**
     * Creates a stack.
     *
     * @param key   the opaque identity of the item type
     * @param count the total count
     */
    public StoredStack(String key, long count) {
        this.key = Objects.requireNonNull(key, "key");
        this.count = count;
    }

    /** Returns a new stack with the same key and a different count. */
    public StoredStack withCount(long count) {
        return new StoredStack(key, count);
    }

    /** Returns the key. */
    public String getKey() {
        return key;
    }

    /** Returns the count. */
    public long getCount() {
        return count;
    }

    /** Stacks are equal when their keys are equal; the count is ignored. */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof StoredStack)) {
            return false;
        }
        return key.equals(((StoredStack) obj).key);
    }

    /** Hashes the key, consistent with key-only equality. */
    @Override
    public int hashCode() {
        return key.hashCode();
    }

    /** Renders as {@code <count> x <key>}. */
    @Override
    public String toString() {
        return count + " x " + key;
    }
}
