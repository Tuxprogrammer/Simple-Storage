package com.tuxprogrammer.simplestorage.sort;
// Ported from Tom's Simple Storage (MIT):
// https://github.com/tom5454/Toms-Storage/blob/95bb61d/NeoForge/src/platform-shared/java/com/tom/storagemod/inventory/sorting/SortingTypes.java

/**
 * The five terminal sort modes kept in the port, in their persisted NBT
 * ordinal order: AMOUNT (0), NAME (1), MOD (2), SPACE_EFFICIENCY (3), ID (4).
 * The TYPE mode is not ported (D27): ordinal 5 stays reserved and inert so
 * old saves load; {@link #fromOrdinal(int)} maps it (and any unknown,
 * reserved, or negative ordinal) to the default mode AMOUNT, never throwing
 * (the fix for the unbounded enum index, TERM-02). The {@code ComparatorType}
 * thread-safety race (TERM-09) cannot occur because that comparator is not
 * ported. Each constant carries a {@link java.util.Comparator} built exactly
 * as the spec's comparators: ties break as the spec says, name comparisons
 * are case-insensitive, and the ascending/descending flag reverses every
 * level. Side: both (plain Java).
 */
public enum SortingTypes {

    /** Quantity descending; ties break by display name ascending (case-insensitive). */
    AMOUNT {

        @Override
        public java.util.Comparator<SortKey> comparator(boolean descending) {
            java.util.Comparator<SortKey> base = new java.util.Comparator<SortKey>() {

                @Override
                public int compare(SortKey a, SortKey b) {
                    int result = Long.compare(b.getAmount(), a.getAmount());
                    return result == 0 ? nameComparator().compare(a, b) : result;
                }
            };
            return descending ? java.util.Collections.reverseOrder(base) : base;
        }
    },

    /** Display name ascending (case-insensitive) only. */
    NAME {

        @Override
        public java.util.Comparator<SortKey> comparator(boolean descending) {
            java.util.Comparator<SortKey> base = new java.util.Comparator<SortKey>() {

                @Override
                public int compare(SortKey a, SortKey b) {
                    return nameComparator().compare(a, b);
                }
            };
            return descending ? java.util.Collections.reverseOrder(base) : base;
        }
    },

    /** Mod id (namespace) ascending; ties break by display name ascending (case-insensitive). */
    MOD {

        @Override
        public java.util.Comparator<SortKey> comparator(boolean descending) {
            java.util.Comparator<SortKey> base = new java.util.Comparator<SortKey>() {

                @Override
                public int compare(SortKey a, SortKey b) {
                    int result = modComparator().compare(a, b);
                    return result == 0 ? nameComparator().compare(a, b) : result;
                }
            };
            return descending ? java.util.Collections.reverseOrder(base) : base;
        }
    },

    /**
     * Space efficiency: used-slot count (ceil(amount / stackLimit)) descending;
     * ties break by amount descending; ties break by display name ascending
     * (case-insensitive).
     */
    SPACE_EFFICIENCY {

        @Override
        public java.util.Comparator<SortKey> comparator(boolean descending) {
            java.util.Comparator<SortKey> base = new java.util.Comparator<SortKey>() {

                @Override
                public int compare(SortKey a, SortKey b) {
                    int result = Long.compare(usedSlots(b), usedSlots(a));
                    if (result == 0) {
                        result = Long.compare(b.getAmount(), a.getAmount());
                    }
                    return result == 0 ? nameComparator().compare(a, b) : result;
                }
            };
            return descending ? java.util.Collections.reverseOrder(base) : base;
        }
    },

    /** Full registry id string (namespace:path) ascending (plain String compareTo). */
    ID {

        @Override
        public java.util.Comparator<SortKey> comparator(boolean descending) {
            java.util.Comparator<SortKey> base = new java.util.Comparator<SortKey>() {

                @Override
                public int compare(SortKey a, SortKey b) {
                    return idComparator().compare(a, b);
                }
            };
            return descending ? java.util.Collections.reverseOrder(base) : base;
        }
    };

    /** The reserved (inert) ordinal of the un-ported TYPE mode (D27). */
    public static final int RESERVED_TYPE_ORDINAL = 5;

    private static final java.util.Comparator<SortKey> NAME_ASC = new java.util.Comparator<SortKey>() {

        @Override
        public int compare(SortKey a, SortKey b) {
            return a.getName()
                .toLowerCase(java.util.Locale.ROOT)
                .compareTo(
                    b.getName()
                        .toLowerCase(java.util.Locale.ROOT));
        }
    };

    private static final java.util.Comparator<SortKey> MOD_ASC = new java.util.Comparator<SortKey>() {

        @Override
        public int compare(SortKey a, SortKey b) {
            return a.getModId()
                .toLowerCase(java.util.Locale.ROOT)
                .compareTo(
                    b.getModId()
                        .toLowerCase(java.util.Locale.ROOT));
        }
    };

    private static final java.util.Comparator<SortKey> ID_ASC = new java.util.Comparator<SortKey>() {

        @Override
        public int compare(SortKey a, SortKey b) {
            return a.getSortId()
                .compareTo(b.getSortId());
        }
    };

    private static java.util.Comparator<SortKey> nameComparator() {
        return NAME_ASC;
    }

    private static java.util.Comparator<SortKey> modComparator() {
        return MOD_ASC;
    }

    private static java.util.Comparator<SortKey> idComparator() {
        return ID_ASC;
    }

    private static long usedSlots(SortKey key) {
        long limit = Math.max(1L, key.getStackLimit());
        long amount = key.getAmount();
        return amount <= 0 ? 0 : (amount - 1) / limit + 1;
    }

    /**
     * Returns a comparator for this sort mode. The name tie-breaks are
     * case-insensitive and every level of the ordering is reversed when the
     * flag is set (as the reverse flag in the original wrapped the whole
     * comparator, including tie-breaks).
     *
     * @param descending true to reverse every level of the ordering
     * @return a comparator for this sort mode
     */
    public abstract java.util.Comparator<SortKey> comparator(boolean descending);

    /**
     * Returns the next sort mode, cycling through the five modes in ordinal
     * order and returning to AMOUNT after ID (the reserved TYPE ordinal is
     * not visited).
     *
     * @return the next sort mode in cycle order
     */
    public SortingTypes next() {
        return values()[(ordinal() + 1) % values().length];
    }

    /**
     * Maps a persisted sort ordinal to its sort mode. Ordinals 0-4 map to the
     * five kept modes; the reserved ordinal 5 (TYPE, not ported), any unknown
     * or negative ordinal maps to the default mode AMOUNT, never throwing.
     *
     * @param ordinal the persisted sort ordinal (low 8 bits of the NBT sort int)
     * @return the sort mode for the ordinal, AMOUNT when unknown/reserved/negative
     */
    public static SortingTypes fromOrdinal(int ordinal) {
        if (ordinal >= 0 && ordinal < values().length) {
            return values()[ordinal];
        }
        return AMOUNT;
    }
}
