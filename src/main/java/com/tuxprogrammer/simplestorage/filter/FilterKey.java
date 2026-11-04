package com.tuxprogrammer.simplestorage.filter;

import java.util.Collections;
import java.util.List;

/**
 * Immutable plain-data description of one stack to be tested against a filter.
 * Replaces the upstream {@code StoredItemStack} so the matching logic stays pure
 * plain Java with no Minecraft types.
 * Side: both (plain Java).
 */
public final class FilterKey {

    /** Registry name of the stack's item, e.g. {@code "minecraft:stone"}. */
    private final String itemName;

    /** Damage value of the stack. */
    private final int damage;

    /**
     * Stable digest of the stack's NBT. {@code null} when the stack has no NBT;
     * any stable string otherwise. Two stacks with equal NBT have equal digests.
     */
    private final String nbtDigest;

    /** Names of the stack's ore-dictionary entries. Unmodifiable copy. */
    private final List<String> oreNames;

    /**
     * Creates a filter key.
     *
     * @param itemName  registry name of the item; must not be null
     * @param damage    damage value of the stack
     * @param nbtDigest stable digest of the stack's NBT, or {@code null} when the
     *                  stack has no NBT
     * @param oreNames  names of the stack's ore-dictionary entries; {@code null}
     *                  means none
     */
    public FilterKey(String itemName, int damage, String nbtDigest, List<String> oreNames) {
        this.itemName = itemName;
        this.damage = damage;
        this.nbtDigest = nbtDigest;
        if (oreNames == null) {
            this.oreNames = Collections.emptyList();
        } else {
            this.oreNames = Collections.unmodifiableList(new java.util.ArrayList<String>(oreNames));
        }
    }

    /**
     * @return registry name of the item
     */
    public String getItemName() {
        return itemName;
    }

    /**
     * @return damage value of the stack
     */
    public int getDamage() {
        return damage;
    }

    /**
     * @return stable digest of the stack's NBT, or {@code null} when the stack
     *         has no NBT
     */
    public String getNbtDigest() {
        return nbtDigest;
    }

    /**
     * @return unmodifiable list of the stack's ore-dictionary entry names
     */
    public List<String> getOreNames() {
        return oreNames;
    }

    /**
     * Two keys are equal when they describe stacks with the same item name,
     * damage and NBT digest. Ore names are not part of the identity.
     *
     * @param o object to compare
     * @return true when {@code o} is a key describing the same stack
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof FilterKey)) {
            return false;
        }
        FilterKey other = (FilterKey) o;
        return damage == other.damage && itemName.equals(other.itemName)
            && (nbtDigest == null ? other.nbtDigest == null : nbtDigest.equals(other.nbtDigest));
    }

    /**
     * Hash code derived from the identity fields (item name, damage, NBT digest).
     *
     * @return hash code
     */
    @Override
    public int hashCode() {
        int result = itemName.hashCode();
        result = 31 * result + damage;
        result = 31 * result + (nbtDigest == null ? 0 : nbtDigest.hashCode());
        return result;
    }

    /**
     * @return a short description, {@code "itemName[damage]"}
     */
    @Override
    public String toString() {
        return itemName + "[" + damage + "]";
    }
}
