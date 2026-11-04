package com.tuxprogrammer.simplestorage.item;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;

import com.tuxprogrammer.simplestorage.SimpleStorage;
import com.tuxprogrammer.simplestorage.filter.ItemFilters;
import com.tuxprogrammer.simplestorage.gui.GuiIds;

/**
 * The ore-dictionary tag filter: a single-use item whose NBT stores a list of
 * ore-dictionary tag names plus the {@code matchAll} and {@code allowList}
 * flags, and which right-click opens its editing GUI on the server.
 *
 * <p>
 * The tag names are stored under the NBT key "tags" (upstream's "allTags" key
 * is not used here; the stored key stays a plain string list) as a list of
 * string tags, and "matchAll" (boolean) and "allowList" (boolean, default
 * {@code true} when the key is missing) are stored as top-level booleans.
 *
 * <p>
 * The static helpers here are the single place that reads and writes that NBT,
 * so the container and the packet both go through them and never touch the
 * format directly.
 *
 * Side: both; the GUI display is client side.
 */
public class TagFilterItem extends Item {

    /** The NBT key holding the list of ore-dictionary tag names. */
    public static final String NBT_TAGS = "tags";

    /** The NBT key holding the match-all flag. */
    public static final String NBT_MATCH_ALL = "matchAll";

    /** The NBT key holding the allow-list flag. */
    public static final String NBT_ALLOW_LIST = "allowList";

    /** How many tag names a filter item may hold. */
    public static final int MAX_NAMES = 16;

    /** The longest tag name a filter item may hold. */
    public static final int MAX_NAME_LENGTH = 64;

    /**
     * Creates the tag filter item and registers its name, texture, creative
     * tab, and stack size.
     */
    public TagFilterItem() {
        setUnlocalizedName(SimpleStorage.MODID + ".tag_filter");
        setTextureName(SimpleStorage.MODID + ":tag_filter");
        setCreativeTab(SimpleStorageTab.INSTANCE);
        setMaxStackSize(1);
    }

    /**
     * Cleans a list of tag names: trims each entry, drops the blanks, caps the
     * length at {@link #MAX_NAMES} names and each name at
     * {@link #MAX_NAME_LENGTH} characters.
     *
     * @param names the names to clean; {@code null} is an empty list
     * @return a new list of cleaned names
     */
    private static List<String> clean(List<String> names) {
        List<String> cleaned = new ArrayList<String>();
        if (names != null) {
            for (String name : names) {
                if (name == null) {
                    continue;
                }
                name = name.trim();
                if (name.length() > MAX_NAME_LENGTH) {
                    name = name.substring(0, MAX_NAME_LENGTH);
                }
                if (name.length() == 0) {
                    continue;
                }
                cleaned.add(name);
                if (cleaned.size() == MAX_NAMES) {
                    break;
                }
            }
        }
        return cleaned;
    }

    /**
     * Reads the filter's tag names out of the item's NBT.
     *
     * <p>
     * Tolerates a missing tag, a malformed list, and null or blank entries:
     * the returned list is the stored names, trimmed, with blanks dropped,
     * capped at {@link #MAX_NAMES} names of at most {@link #MAX_NAME_LENGTH}
     * characters each.
     *
     * @param filterItem the filter item to read; {@code null} or a filter
     *                   without the key reads as an empty list
     * @return the stored tag names, cleaned
     */
    public static List<String> getNames(ItemStack filterItem) {
        if (filterItem == null || !filterItem.hasTagCompound()) {
            return new ArrayList<String>();
        }
        NBTTagList list = filterItem.getTagCompound()
            .getTagList(NBT_TAGS, 8);
        List<String> names = new ArrayList<String>();
        for (int i = 0; i < list.tagCount(); i++) {
            names.add(list.getStringTagAt(i));
        }
        return clean(names);
    }

    /**
     * Writes the filter's tag names into the item's NBT, applying the same
     * cleaning as {@link #getNames(ItemStack)}.
     *
     * @param filterItem the filter item to write; a {@code null} item is a no-op
     * @param names      the tag names to store; blanks are dropped and the list
     *                   is capped at {@link #MAX_NAMES} names of at most
     *                   {@link #MAX_NAME_LENGTH} characters each
     */
    public static void setNames(ItemStack filterItem, List<String> names) {
        if (filterItem == null) {
            return;
        }
        NBTTagCompound compound = filterItem.hasTagCompound() ? filterItem.getTagCompound() : new NBTTagCompound();
        NBTTagList list = new NBTTagList();
        for (String name : clean(names)) {
            list.appendTag(new net.minecraft.nbt.NBTTagString(name));
        }
        compound.setTag(NBT_TAGS, list);
        filterItem.setTagCompound(compound);
    }

    /**
     * @param filterItem the filter item to read
     * @return whether the item's "matchAll" flag is set; a missing key is false
     */
    public static boolean isMatchAll(ItemStack filterItem) {
        if (filterItem == null || !filterItem.hasTagCompound()) {
            return false;
        }
        return filterItem.getTagCompound()
            .getBoolean(NBT_MATCH_ALL);
    }

    /**
     * @param filterItem the filter item to read
     * @return whether the item's "allowList" flag is set; a missing key defaults to true
     */
    public static boolean isAllowList(ItemStack filterItem) {
        if (filterItem == null || !filterItem.hasTagCompound()) {
            return true;
        }
        NBTTagCompound compound = filterItem.getTagCompound();
        return compound.hasKey(NBT_ALLOW_LIST) ? compound.getBoolean(NBT_ALLOW_LIST) : true;
    }

    /**
     * Sets the item's two flags.
     *
     * @param filterItem the filter item to write; a {@code null} item is a no-op
     * @param matchAll   the new value of "matchAll"
     * @param allowList  the new value of "allowList"
     */
    public static void setFlags(ItemStack filterItem, boolean matchAll, boolean allowList) {
        if (filterItem == null) {
            return;
        }
        NBTTagCompound compound = filterItem.hasTagCompound() ? filterItem.getTagCompound() : new NBTTagCompound();
        compound.setBoolean(NBT_MATCH_ALL, matchAll);
        compound.setBoolean(NBT_ALLOW_LIST, allowList);
        filterItem.setTagCompound(compound);
    }

    /**
     * Builds the {@link ItemFilters.Filter} this item currently represents:
     * the configured tag names, under the item's current "matchAll" and
     * "allowList" flags.
     *
     * @param filterItem the filter item to build from; {@code null} behaves as
     *                   an empty allow-list filter
     * @return the filter this item represents
     */
    public static ItemFilters.Filter toFilter(ItemStack filterItem) {
        return ItemFilters.oreDictionary(getNames(filterItem), isMatchAll(filterItem), isAllowList(filterItem));
    }

    /**
     * Right-clicking with the tag filter opens its editing GUI on the server;
     * the item itself is not consumed.
     *
     * @param stack  the held tag filter item
     * @param world  the world the player is in
     * @param player the player right-clicking
     * @return the held item, unchanged
     */
    @Override
    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
        if (!world.isRemote) {
            SimpleStorage.openGui(player, GuiIds.TAG_FILTER, world, 0, 0, 0);
        }
        return stack;
    }
}
