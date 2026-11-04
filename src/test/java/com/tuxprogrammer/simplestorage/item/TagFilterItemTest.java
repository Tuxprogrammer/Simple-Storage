package com.tuxprogrammer.simplestorage.item;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.minecraft.init.Bootstrap;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.tuxprogrammer.simplestorage.filter.FilterKey;

/**
 * Unit tests for {@link TagFilterItem}: the NBT round-trip and its cleaning
 * rules, the flag defaults, and the shape of the
 * {@link com.tuxprogrammer.simplestorage.filter.ItemFilters#oreDictionary(List, boolean, boolean)}
 * filter the item builds.
 */
@Tag("ingame")
public class TagFilterItemTest {

    /**
     * The tag filter item the tests build stacks for.
     */
    private static Item tagFilter;

    /**
     * Boots the item registry and creates the filter item.
     */
    @BeforeAll
    public static void setUp() {
        Bootstrap.func_151354_b();
        tagFilter = new TagFilterItem();
    }

    /**
     * @return a new stack of the tag filter item
     */
    private static ItemStack fresh() {
        return new ItemStack(tagFilter, 1);
    }

    /**
     * Names round-trip through the NBT: blanks are dropped, long names are
     * truncated to {@link TagFilterItem#MAX_NAME_LENGTH} characters, and the
     * list is capped at {@link TagFilterItem#MAX_NAMES} names.
     */
    @Test
    public void namesRoundTripAndAreCleaned() {
        List<String> names = new ArrayList<String>();
        names.add("ingotIron");
        names.add("  ");
        names.add(new String(new char[100]).replace('\0', 'x'));
        for (int i = 0; i < 17; i++) {
            names.add("tag" + i);
        }
        assertEquals(20, names.size());

        ItemStack stack = fresh();
        TagFilterItem.setNames(stack, names);
        List<String> read = TagFilterItem.getNames(stack);

        // "ingotIron", the 64-char truncated x run, and "tag0".."tag13" fill
        // the 16 slots; the blank and the five extra names are dropped.
        assertEquals(TagFilterItem.MAX_NAMES, read.size());
        assertEquals("ingotIron", read.get(0));
        assertEquals(new String(new char[TagFilterItem.MAX_NAME_LENGTH]).replace('\0', 'x'), read.get(1));
        assertEquals("tag13", read.get(15));
    }

    /**
     * A fresh filter item has no "matchAll" (false) and no "allowList" (which
     * defaults to true).
     */
    @Test
    public void flagsDefaultToAllowList() {
        ItemStack stack = fresh();
        assertEquals(false, TagFilterItem.isMatchAll(stack));
        assertEquals(true, TagFilterItem.isAllowList(stack));
    }

    /**
     * A filter with two names matches a stack in one of them, but "matchAll"
     * demands that the stack be in every name.
     */
    @Test
    public void matchAllNeedsEveryName() {
        FilterKey iron = key("ingotIron");
        ItemStack stack = fresh();

        TagFilterItem.setNames(stack, Arrays.asList("ingotIron", "plateIron"));
        TagFilterItem.setFlags(stack, false, true);
        assertEquals(
            true,
            TagFilterItem.toFilter(stack)
                .matches(iron));

        TagFilterItem.setFlags(stack, true, true);
        assertEquals(
            false,
            TagFilterItem.toFilter(stack)
                .matches(iron));
    }

    /**
     * "allowList" false turns the same names into a deny list.
     */
    @Test
    public void denyListInvertsTheResult() {
        FilterKey iron = key("ingotIron");
        ItemStack stack = fresh();
        TagFilterItem.setNames(stack, Collections.singletonList("ingotIron"));
        TagFilterItem.setFlags(stack, false, true);
        assertEquals(
            true,
            TagFilterItem.toFilter(stack)
                .matches(iron));
        TagFilterItem.setFlags(stack, false, false);
        assertEquals(
            false,
            TagFilterItem.toFilter(stack)
                .matches(iron));
    }

    /**
     * A filter item with no tag at all has no names, and its filter matches
     * nothing: the allow flag defaults to true, so an empty allow-list
     * matches nothing per the empty-list rule of
     * {@link com.tuxprogrammer.simplestorage.filter.ItemFilters}.
     */
    @Test
    public void missingTagIsEmpty() {
        ItemStack stack = fresh();
        assertEquals(
            0,
            TagFilterItem.getNames(stack)
                .size());
        assertEquals(
            false,
            TagFilterItem.toFilter(stack)
                .matches(key("ingotIron")));
    }

    /**
     * @param oreName an ore-dictionary name for the test key
     * @return a {@link FilterKey} carrying that single ore-dictionary name
     */
    private static FilterKey key(String oreName) {
        return new FilterKey("test:item", 0, null, Arrays.asList(oreName));
    }
}
