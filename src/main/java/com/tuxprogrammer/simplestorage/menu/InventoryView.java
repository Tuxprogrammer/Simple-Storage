package com.tuxprogrammer.simplestorage.menu;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

/**
 * Base class for small inventory views used by container slots.
 *
 * <p>
 * Subclasses provide the actual storage access (slot count, slot reads,
 * slot writes). This class supplies the remaining {@link IInventory}
 * behavior that is generic to any view: size-based decrement, dirty marking
 * hooks, and a fixed inventory name. Views are non-persistent; callers are
 * expected to refresh or replace the underlying storage as needed.
 *
 * Side: server logic.
 */
public abstract class InventoryView implements IInventory {

    /**
     * Returns the number of slots this view exposes.
     *
     * @return the slot count of this view
     */
    public abstract int getSizeInventory();

    /**
     * Returns the stack currently in the given slot, or null if empty.
     *
     * @param slot the slot index to read
     * @return the stack in the slot, or null
     */
    public abstract ItemStack getStackInSlot(int slot);

    /**
     * Replaces the contents of the given slot.
     *
     * @param slot  the slot index to write
     * @param stack the new stack, or null to clear the slot
     */
    public abstract void setInventorySlotContents(int slot, ItemStack stack);

    /**
     * Removes up to {@code amount} items from the given slot.
     *
     * @param slot   the slot index to decrement
     * @param amount the number of items to remove
     * @return the removed items, or null if the slot was empty or the amount was non-positive
     */
    public ItemStack decrStackSize(int slot, int amount) {
        ItemStack stack = getStackInSlot(slot);
        if (stack == null || amount <= 0) {
            return null;
        }
        ItemStack result;
        if (stack.stackSize <= amount) {
            result = stack;
            setInventorySlotContents(slot, null);
        } else {
            result = stack.splitStack(amount);
        }
        markDirty();
        return result;
    }

    /**
     * Returns the stack in the slot, then leaves the slot unchanged (no removal).
     *
     * @param slot the slot index to query
     * @return null, as this view does not remove on closing
     */
    public ItemStack getStackInSlotOnClosing(int slot) {
        return null;
    }

    /**
     * Returns the base (unlocalized) inventory name.
     *
     * @return the inventory name key
     */
    public String getInventoryName() {
        return "container.simplestoragenh.view";
    }

    /**
     * Whether this view has a custom (non-default) inventory name.
     *
     * @return false
     */
    public boolean hasCustomInventoryName() {
        return false;
    }

    /**
     * Returns the maximum stack size for items in this view.
     *
     * @return 64
     */
    public int getInventoryStackLimit() {
        return 64;
    }

    /**
     * Marks the view as dirty. Subclasses may override to propagate dirtiness.
     */
    public void markDirty() {}

    /**
     * Whether the given player may use this view.
     *
     * @param player the player to check
     * @return true, views are always usable
     */
    public boolean isUseableByPlayer(EntityPlayer player) {
        return true;
    }

    /**
     * Called when a player opens the inventory containing this view.
     */
    public void openInventory() {}

    /**
     * Called when a player closes the inventory containing this view.
     */
    public void closeInventory() {}

    /**
     * Whether the given stack is valid for the given slot.
     *
     * @param slot  the slot index to check
     * @param stack the stack to test
     * @return true, any stack is valid in any slot
     */
    public boolean isItemValidForSlot(int slot, ItemStack stack) {
        return true;
    }
}
