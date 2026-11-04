package com.tuxprogrammer.simplestorage.filter;
// Ported from Tom's Simple Storage (MIT):
// https://github.com/tom5454/Toms-Storage/blob/95bb61d/NeoForge/src/platform-shared/java/com/tom/storagemod/inventory/filter/SimpleItemFilter.java
// Avoids: TERM-11

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Pure item-filter predicates ported from upstream Tom's Simple Storage: the
 * 9-slot {@code SimpleItemFilter} becomes {@link #simple(List, boolean, boolean)},
 * the {@code TagFilter} becomes the ore-dictionary-name filter
 * {@link #oreDictionary(List, boolean, boolean)} (deviation D20), and
 * {@code IFilter.MultiFilter} becomes {@link #multi(List)}.
 * Matching is a pure function of (filter state, {@link FilterKey}).
 * Side: both (plain Java).
 */
public final class ItemFilters {

    /** Maximum number of entries a simple (9-slot) filter may hold. */
    public static final int MAX_SIMPLE_ENTRIES = 9;

    /**
     * A predicate testing one {@link FilterKey}.
     */
    public interface Filter {

        /**
         * Tests one stack description.
         *
         * @param key the stack to test
         * @return true when the stack passes this filter
         */
        boolean matches(FilterKey key);
    }

    private ItemFilters() {}

    /**
     * Builds a simple (exact item) filter, the port of the upstream
     * 9-slot {@code SimpleItemFilter}.
     * A stack matches an entry by item name and damage, and by NBT digest
     * only when {@code matchNbt} is set; an entry with no NBT never matches a
     * stack with NBT.
     *
     * @param stacks    entries to match against; at most {@link #MAX_SIMPLE_ENTRIES}
     * @param matchNbt  true to require equal NBT digests as well
     * @param allowList true: a stack matches when it matches any entry;
     *                  false: a stack matches when it matches no entry
     * @return the filter
     */
    public static Filter simple(List<FilterKey> stacks, boolean matchNbt, boolean allowList) {
        if (stacks.size() > MAX_SIMPLE_ENTRIES) {
            throw new IllegalArgumentException(
                "simple filter has " + stacks.size() + " entries; at most " + MAX_SIMPLE_ENTRIES + " are allowed");
        }
        final FilterKey[] entries = stacks.toArray(new FilterKey[0]);
        return new Filter() {

            @Override
            public boolean matches(FilterKey key) {
                boolean listed = false;
                for (FilterKey entry : entries) {
                    if (matchesEntry(entry, key, matchNbt)) {
                        listed = true;
                        break;
                    }
                }
                return listed == allowList;
            }
        };
    }

    /**
     * Builds an ore-dictionary-name filter, the port of the upstream
     * {@code TagFilter} with ore-dictionary names instead of tags (deviation
     * D20). Names are trimmed, blank names are ignored and compared with
     * case-sensitive exact equality against the stack's ore names.
     *
     * @param names     ore-dictionary names; empty or all-blank is a filter with
     *                  no names
     * @param matchAll  true when the stack must have every configured name
     *                  (the upstream "allTags" option); false when any one name
     *                  suffices
     * @param allowList true: a stack matches when the name test passes;
     *                  false: when it fails
     * @return the filter
     */
    public static Filter oreDictionary(List<String> names, boolean matchAll, boolean allowList) {
        final List<String> kept = new ArrayList<String>();
        for (String name : names) {
            String trimmed = name.trim();
            if (trimmed.length() > 0) {
                kept.add(trimmed);
            }
        }
        final List<String> configured = Collections.unmodifiableList(kept);
        return new Filter() {

            @Override
            public boolean matches(FilterKey key) {
                boolean pass;
                if (matchAll) {
                    // An all-blank name list has no names to require, so the
                    // test passes (matches the empty-name deny/allow rule).
                    pass = true;
                    for (String name : configured) {
                        if (!key.getOreNames()
                            .contains(name)) {
                            pass = false;
                            break;
                        }
                    }
                } else {
                    pass = false;
                    for (String name : configured) {
                        if (key.getOreNames()
                            .contains(name)) {
                            pass = true;
                            break;
                        }
                    }
                }
                return pass == allowList;
            }
        };
    }

    /**
     * Combines filters with AND semantics, the port of
     * {@code IFilter.MultiFilter}: the result passes only when every member
     * passes.
     *
     * @param filters the member filters; {@code null} is treated as empty
     * @return the combined filter
     */
    public static Filter multi(List<Filter> filters) {
        final List<Filter> members = filters == null ? Collections.<Filter>emptyList()
            : Collections.unmodifiableList(new ArrayList<Filter>(filters));
        return new Filter() {

            @Override
            public boolean matches(FilterKey key) {
                for (Filter filter : members) {
                    if (!filter.matches(key)) {
                        return false;
                    }
                }
                return true;
            }
        };
    }

    /**
     * Tests one simple-filter entry against a stack.
     */
    private static boolean matchesEntry(FilterKey entry, FilterKey key, boolean matchNbt) {
        if (!entry.getItemName()
            .equals(key.getItemName()) || entry.getDamage() != key.getDamage()) {
            return false;
        }
        if (matchNbt) {
            if (entry.getNbtDigest() == null) {
                return key.getNbtDigest() == null;
            }
            return entry.getNbtDigest()
                .equals(key.getNbtDigest());
        }
        return true;
    }
}
