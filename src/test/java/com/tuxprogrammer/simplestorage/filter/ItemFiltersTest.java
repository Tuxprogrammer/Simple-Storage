package com.tuxprogrammer.simplestorage.filter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Tests for the pure item-filter logic ({@link ItemFilters}): the 9-slot simple
 * filter, the ore-dictionary-name filter (upstream tag filter) and the AND
 * multi filter, pinning the rules of the original 03-terminal filters spec.
 */
public class ItemFiltersTest {

    private static FilterKey key(String name, int damage, String nbt) {
        return new FilterKey(name, damage, nbt, null);
    }

    private static FilterKey key(String name, int damage, String nbt, List<String> oreNames) {
        return new FilterKey(name, damage, nbt, oreNames);
    }

    private static List<FilterKey> keys(FilterKey... keys) {
        return Arrays.asList(keys);
    }

    private static List<String> names(String... names) {
        return Arrays.asList(names);
    }

    private static List<ItemFilters.Filter> filters(ItemFilters.Filter... filters) {
        return Arrays.asList(filters);
    }

    @Test
    public void simpleAllowListMatchesListedItem() {
        ItemFilters.Filter filter = ItemFilters.simple(keys(key("dirt", 0, null)), false, true);
        assertTrue(filter.matches(key("dirt", 0, null)));
        assertFalse(filter.matches(key("stone", 0, null)));
    }

    @Test
    public void simpleDenyListMatchesUnlistedItem() {
        ItemFilters.Filter filter = ItemFilters.simple(keys(key("dirt", 0, null)), false, false);
        assertFalse(filter.matches(key("dirt", 0, null)));
        assertTrue(filter.matches(key("stone", 0, null)));
    }

    @Test
    public void simpleEmptyAllowListMatchesNothingAndEmptyDenyListMatchesEverything() {
        ItemFilters.Filter allow = ItemFilters.simple(keys(), false, true);
        assertFalse(allow.matches(key("dirt", 0, null)));

        ItemFilters.Filter deny = ItemFilters.simple(keys(), false, false);
        assertTrue(deny.matches(key("dirt", 0, null)));
    }

    @Test
    public void simpleMatchesByItemNameAndDamage() {
        ItemFilters.Filter filter = ItemFilters.simple(keys(key("iron_pickaxe", 0, null)), false, true);
        assertTrue(filter.matches(key("iron_pickaxe", 0, null)));
        assertFalse(filter.matches(key("stone_pickaxe", 0, null)));
        assertFalse(filter.matches(key("iron_pickaxe", 1, null)));
    }

    @Test
    public void simpleMatchNbtOnRequiresEqualDigest() {
        ItemFilters.Filter filter = ItemFilters.simple(keys(key("item", 0, "{tag:1}")), true, true);
        assertTrue(filter.matches(key("item", 0, "{tag:1}")));
        assertFalse(filter.matches(key("item", 0, "{tag:2}")));
        assertFalse(filter.matches(key("item", 0, null)));
    }

    @Test
    public void simpleMatchNbtOnEntryWithoutNbtMatchesOnlyNbtFreeStacks() {
        ItemFilters.Filter filter = ItemFilters.simple(keys(key("item", 0, null)), true, true);
        assertTrue(filter.matches(key("item", 0, null)));
        assertFalse(filter.matches(key("item", 0, "{tag:1}")));
    }

    @Test
    public void simpleMatchNbtOffIgnoresDigest() {
        ItemFilters.Filter filter = ItemFilters.simple(keys(key("item", 0, "{tag:1}")), false, true);
        assertTrue(filter.matches(key("item", 0, "{tag:1}")));
        assertTrue(filter.matches(key("item", 0, "{tag:2}")));
        assertTrue(filter.matches(key("item", 0, null)));
    }

    @Test
    public void simpleAllowsNineEntries() {
        FilterKey[] nine = new FilterKey[ItemFilters.MAX_SIMPLE_ENTRIES];
        for (int i = 0; i < nine.length; i++) {
            nine[i] = key("item" + i, 0, null);
        }
        ItemFilters.Filter filter = ItemFilters.simple(Arrays.asList(nine), false, true);
        assertTrue(filter.matches(key("item8", 0, null)));
        assertFalse(filter.matches(key("item9", 0, null)));
    }

    @Test
    public void oreDictionaryMatchesAnyNameInAllowMode() {
        ItemFilters.Filter filter = ItemFilters.oreDictionary(names("oreIron", "ingotGold"), false, true);
        assertTrue(filter.matches(key("ore", 0, null, names("oreIron"))));
        assertTrue(filter.matches(key("ore", 0, null, names("other", "ingotGold"))));
        assertTrue(filter.matches(key("ore", 0, null, names("oreIron", "ingotGold"))));
        assertFalse(filter.matches(key("ore", 0, null, names("dustLead"))));
    }

    @Test
    public void oreDictionaryDenyListMatchesStacksWithoutConfiguredName() {
        ItemFilters.Filter filter = ItemFilters.oreDictionary(names("oreIron"), false, false);
        assertFalse(filter.matches(key("ore", 0, null, names("oreIron"))));
        assertTrue(filter.matches(key("ore", 0, null, names("dustLead"))));
    }

    @Test
    public void oreDictionaryNamesAreCaseSensitiveAndTrimmed() {
        ItemFilters.Filter filter = ItemFilters.oreDictionary(names("oreIron"), false, true);
        assertFalse(filter.matches(key("ore", 0, null, names("oreiron"))));
        assertFalse(filter.matches(key("ore", 0, null, names("OREIRON"))));

        ItemFilters.Filter trimmed = ItemFilters.oreDictionary(names("  oreIron  "), false, true);
        assertTrue(trimmed.matches(key("ore", 0, null, names("oreIron"))));
    }

    @Test
    public void oreDictionaryBlankNamesAreIgnored() {
        ItemFilters.Filter any = ItemFilters.oreDictionary(names("", "   "), false, true);
        assertFalse(any.matches(key("ore", 0, null, names("oreIron"))));

        ItemFilters.Filter all = ItemFilters.oreDictionary(names("", "   "), true, true);
        assertTrue(all.matches(key("ore", 0, null, names("oreIron"))));
    }

    @Test
    public void oreDictionaryMatchAllRequiresEveryConfiguredName() {
        ItemFilters.Filter filter = ItemFilters.oreDictionary(names("oreIron", "oreGold"), true, true);
        assertTrue(filter.matches(key("ore", 0, null, names("oreIron", "oreGold"))));
        assertFalse(filter.matches(key("ore", 0, null, names("oreIron"))));
        assertFalse(filter.matches(key("ore", 0, null, names("oreGold"))));
        assertFalse(filter.matches(key("ore", 0, null, names())));
    }

    @Test
    public void oreDictionaryEmptyNamesMatchNothingInAllowModeAndEverythingInDenyMode() {
        ItemFilters.Filter allow = ItemFilters.oreDictionary(names(), false, true);
        assertFalse(allow.matches(key("ore", 0, null, names("oreIron"))));

        ItemFilters.Filter deny = ItemFilters.oreDictionary(names(), false, false);
        assertTrue(deny.matches(key("ore", 0, null, names("oreIron"))));
    }

    @Test
    public void multiRequiresAllMembersToPass() {
        ItemFilters.Filter filter = ItemFilters.multi(
            filters(
                ItemFilters.simple(keys(key("dirt", 0, null)), false, true),
                ItemFilters.oreDictionary(names("oreIron"), false, false)));
        assertTrue(filter.matches(key("dirt", 0, null, names("dustLead"))));
        assertFalse(filter.matches(key("dirt", 0, null, names("oreIron"))));
        assertFalse(filter.matches(key("stone", 0, null, names("dustLead"))));
    }

    @Test
    public void multiOfNothingPassesEveryStack() {
        ItemFilters.Filter filter = ItemFilters.multi(filters());
        assertTrue(filter.matches(key("dirt", 0, null)));
    }

    @Test
    public void doesNotReproduceTERM11_simpleIsCappedAtNineEntries() {
        // Upstream TERM-11: a shift-click could duplicate an item in the 3x3
        // grid, leaving the filter with the same item more than once (up to 9
        // identical entries). The port caps the simple filter at the 9-slot
        // grid size, so a tenth entry is rejected instead of being stored.
        FilterKey[] ten = new FilterKey[ItemFilters.MAX_SIMPLE_ENTRIES + 1];
        for (int i = 0; i < ten.length; i++) {
            ten[i] = key("iron_pickaxe", 0, null);
        }
        assertThrows(IllegalArgumentException.class, () -> ItemFilters.simple(Arrays.asList(ten), false, true));
        assertEquals(9, ItemFilters.MAX_SIMPLE_ENTRIES);
    }
}
