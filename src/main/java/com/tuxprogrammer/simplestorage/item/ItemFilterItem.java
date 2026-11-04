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
import com.tuxprogrammer.simplestorage.blockentity.StackKeys;
import com.tuxprogrammer.simplestorage.filter.FilterKey;
import com.tuxprogrammer.simplestorage.filter.ItemFilters;
import com.tuxprogrammer.simplestorage.gui.GuiIds;

/**
 * The item filter: a single-use item whose NBT stores up to nine example
 * stacks plus the {@code matchNbt} and {@code allowList} flags, and which
 * right-click opens its editing GUI on the server.
 *
 * <p>
 * The nine example stacks are stored under the NBT key "stacks" as a tag list
 * of compounds: each compound is a serialized stack (item id, damage, count,
 * and its own tag) plus a "Slot" byte 0..8 naming which of the nine filter
 * slots it belongs to. "matchNbt" (boolean) and "allowList" (boolean, default
 * {@code true} when the key is missing) are stored as top-level booleans.
 *
 * <p>
 * The static helpers here are the single place that reads and writes that NBT,
 * so the container and the packet both go through them and never touch the
 * format directly.
 *
 * Side: both; the GUI display is client side.
 */
public class ItemFilterItem extends Item {

    /** The NBT key holding the list of example stacks. */
    public static final String NBT_STACKS = "stacks";

    /** The NBT key holding the match-NBT flag. */
    public static final String NBT_MATCH_NBT = "matchNbt";

    /** The NBT key holding the allow-list flag. */
    public static final String NBT_ALLOW_LIST = "allowList";

    /** The key inside each stack compound naming its filter slot. */
    public static final String NBT_SLOT = "Slot";

    /** How many example stacks a filter item holds. */
    public static final int GHOST_SLOTS = 9;

    /**
     * Creates the filter item and registers its name, texture, creative tab,
     * and stack size.
     */
    public ItemFilterItem() {
        setUnlocalizedName(SimpleStorage.MODID + ".item_filter");
        setTextureName(SimpleStorage.MODID + ":item_filter");
        setCreativeTab(SimpleStorageTab.INSTANCE);
        setMaxStackSize(1);
    }

    /**
     * Reads the filter's example stacks out of the item's NBT.
     *
     * <p>
     * Tolerates a missing tag, a malformed list, a bad "Slot" value (out of
     * range or negative) and null entries: each of the nine positions is either
     * the stored stack or {@code null}. The tag's own compounds are never
     * mutated: each stored entry is copied before its "Slot" byte is removed,
     * so the returned stacks carry no slot marker and the item's NBT is
     * untouched.
     *
     * @param filterItem the filter item to read; {@code null} or a filter
     *                   without the key reads as nine empty slots
     * @return an array of length {@link #GHOST_SLOTS}, each entry the stack
     *         stored at that slot or {@code null}
     */
    public static ItemStack[] getStacks(ItemStack filterItem) {
        ItemStack[] stacks = new ItemStack[GHOST_SLOTS];
        if (filterItem == null || !filterItem.hasTagCompound()) {
            return stacks;
        }
        NBTTagList list = filterItem.getTagCompound()
            .getTagList(NBT_STACKS, 10);
        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound compound = list.getCompoundTagAt(i);
            if (!compound.hasKey(NBT_SLOT)) {
                continue;
            }
            int slot = compound.getByte(NBT_SLOT);
            if (slot < 0 || slot >= GHOST_SLOTS) {
                continue;
            }
            // Copy the compound so removing the slot marker does not mutate the item's NBT.
            NBTTagCompound copy = (NBTTagCompound) compound.copy();
            copy.removeTag(NBT_SLOT);
            ItemStack stack = ItemStack.loadItemStackFromNBT(copy);
            if (stack != null) {
                stacks[slot] = stack;
            }
        }
        return stacks;
    }

    /**
     * Writes the filter's example stacks into the item's NBT.
     *
     * <p>
     * Only non-null entries are written, each as a size-1 stack so the filter
     * stores the item's identity and NBT but not a particular count. An
     * {@code null} entry leaves that slot empty (its previous entry, if any,
     * is dropped).
     *
     * @param filterItem the filter item to write; a {@code null} item is a no-op
     * @param stacks     the nine example stacks; {@code null} entries are ignored
     */
    public static void setStacks(ItemStack filterItem, ItemStack[] stacks) {
        if (filterItem == null) {
            return;
        }
        NBTTagCompound compound = filterItem.hasTagCompound() ? filterItem.getTagCompound() : new NBTTagCompound();
        NBTTagList list = new NBTTagList();
        if (stacks != null) {
            for (int slot = 0; slot < GHOST_SLOTS; slot++) {
                ItemStack stack = stacks[slot];
                if (stack == null || stack.stackSize == 0) {
                    continue;
                }
                NBTTagCompound entry = stack.writeToNBT(new NBTTagCompound());
                // A size-1 entry records the item's identity, not a count.
                entry.setByte(NBT_SLOT, (byte) slot);
                entry.setShort("StackSize", (short) 1);
                list.appendTag(entry);
            }
        }
        compound.setTag(NBT_STACKS, list);
        filterItem.setTagCompound(compound);
    }

    /**
     * @param filterItem the filter item to read
     * @return whether the item's "matchNbt" flag is set; a missing key is false
     */
    public static boolean isMatchNbt(ItemStack filterItem) {
        if (filterItem == null || !filterItem.hasTagCompound()) {
            return false;
        }
        return filterItem.getTagCompound()
            .getBoolean(NBT_MATCH_NBT);
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
     * @param matchNbt   the new value of "matchNbt"
     * @param allowList  the new value of "allowList"
     */
    public static void setFlags(ItemStack filterItem, boolean matchNbt, boolean allowList) {
        if (filterItem == null) {
            return;
        }
        NBTTagCompound compound = filterItem.hasTagCompound() ? filterItem.getTagCompound() : new NBTTagCompound();
        compound.setBoolean(NBT_MATCH_NBT, matchNbt);
        compound.setBoolean(NBT_ALLOW_LIST, allowList);
        filterItem.setTagCompound(compound);
    }

    /**
     * Builds the {@link ItemFilters.Filter} this item currently represents:
     * the configured stacks, as {@link FilterKey}s, under the item's current
     * "matchNbt" and "allowList" flags.
     *
     * @param filterItem the filter item to build from; {@code null} behaves as
     *                   an empty allow-list filter
     * @return the filter this item represents
     */
    public static ItemFilters.Filter toFilter(ItemStack filterItem) {
        ItemStack[] stacks = getStacks(filterItem);
        List<FilterKey> keys = new ArrayList<FilterKey>();
        for (ItemStack stack : stacks) {
            if (stack != null) {
                keys.add(StackKeys.filterKeyOf(stack));
            }
        }
        return ItemFilters.simple(keys, isMatchNbt(filterItem), isAllowList(filterItem));
    }

    /**
     * Right-clicking with the filter opens its editing GUI on the server; the
     * item itself is not consumed.
     *
     * @param stack  the held filter item
     * @param world  the world the player is in
     * @param player the player right-clicking
     * @return the held item, unchanged
     */
    @Override
    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
        if (!world.isRemote) {
            SimpleStorage.openGui(player, GuiIds.ITEM_FILTER, world, 0, 0, 0);
        }
        return stack;
    }
}
