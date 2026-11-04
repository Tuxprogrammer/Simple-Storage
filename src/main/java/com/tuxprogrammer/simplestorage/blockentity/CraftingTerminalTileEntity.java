package com.tuxprogrammer.simplestorage.blockentity;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

/**
 * Tile entity backing the crafting terminal block: a storage terminal plus
 * a 3x3 crafting grid whose stacks are persisted with the block.
 *
 * <p>
 * The grid is stored under the NBT key "craftingGrid" as a tag list of
 * compounds, each holding the stack's own tag plus a "Slot" byte. Reading
 * is tolerant: entries with an out-of-range slot index or a null stack are
 * skipped, and the list's own compounds are never mutated in place (each
 * entry is copied before its "Slot" byte is removed).
 *
 * Side: server authoritative.
 */
public class CraftingTerminalTileEntity extends StorageTerminalTileEntity {

    /**
     * The crafting grid, indexed 0..8.
     */
    private ItemStack[] grid = new ItemStack[9];

    /**
     * @return the stack in the given grid slot.
     *
     * @param index the slot index, 0..8.
     * @return the stack at that slot, or null if the index is out of range
     *         or the slot is empty.
     */
    public ItemStack getGridStack(int index) {
        if (index < 0 || index >= grid.length) {
            return null;
        }
        return grid[index];
    }

    /**
     * Places a stack into the given grid slot.
     *
     * @param index the slot index, 0..8.
     * @param stack the stack to place (may be null to empty the slot).
     */
    public void setGridStack(int index, ItemStack stack) {
        if (index < 0 || index >= grid.length) {
            return;
        }
        grid[index] = stack;
        markDirty();
    }

    /**
     * Restores the crafting grid from {@code tag} in addition to the
     * terminal fields.
     *
     * @param tag the tag to read from.
     */
    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        grid = new ItemStack[9];
        NBTTagList list = tag.getTagList("craftingGrid", 10);
        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound entry = list.getCompoundTagAt(i);
            NBTTagCompound copy = (NBTTagCompound) entry.copy();
            int slot = copy.getByte("Slot") & 0xFF;
            copy.removeTag("Slot");
            if (slot < 0 || slot >= grid.length) {
                continue;
            }
            ItemStack stack = ItemStack.loadItemStackFromNBT(copy);
            if (stack == null) {
                continue;
            }
            grid[slot] = stack;
        }
    }

    /**
     * Writes the crafting grid into {@code tag} in addition to the terminal
     * fields. Empty slots are omitted.
     *
     * @param tag the tag to write into; may be empty.
     */
    @Override
    public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        NBTTagList list = new NBTTagList();
        for (int i = 0; i < grid.length; i++) {
            ItemStack stack = grid[i];
            if (stack == null) {
                continue;
            }
            NBTTagCompound entry = stack.writeToNBT(new NBTTagCompound());
            entry.setByte("Slot", (byte) i);
            list.appendTag(entry);
        }
        tag.setTag("craftingGrid", list);
    }
}
