package com.tuxprogrammer.simplestorage.blockentity;

import java.util.Map;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

import com.tuxprogrammer.simplestorage.inventory.MultiInventory;

/**
 * Adapts a vanilla {@link IInventory} to the {@link MultiInventory.Slots} interface.
 * Side: server logic.
 */
public final class InventorySlots implements MultiInventory.Slots {

    private final IInventory inventory;
    private final Map<String, ItemStack> prototypes;

    /**
     * Creates a slot adapter over the given inventory.
     * 
     * @param inventory  the backing inventory
     * @param prototypes a caller-shared map that records a single-stack prototype for every
     *                   non-empty slot seen via {@link #keyAt(int)}
     */
    public InventorySlots(IInventory inventory, Map<String, ItemStack> prototypes) {
        this.inventory = inventory;
        this.prototypes = prototypes;
    }

    /**
     * @return the number of slots in the backing inventory
     */
    @Override
    public int size() {
        return inventory.getSizeInventory();
    }

    /**
     * Returns the key of the stack in the given slot, registering its prototype if needed.
     * 
     * @param slot the slot index, may be out of bounds
     * @return the stack key, or null for an empty slot or a bad index
     */
    @Override
    public String keyAt(int slot) {
        ItemStack stack = getSlotStack(slot);
        if (stack == null) {
            return null;
        }
        String key = StackKeys.keyOf(stack);
        ItemStack prototype = stack.copy();
        prototype.stackSize = 1;
        prototypes.putIfAbsent(key, prototype);
        return key;
    }

    /**
     * @param slot the slot index, may be out of bounds
     * @return the stack size in the slot, or 0 when empty or out of bounds
     */
    @Override
    public int countAt(int slot) {
        ItemStack stack = getSlotStack(slot);
        return stack == null ? 0 : stack.stackSize;
    }

    /**
     * @param slot the slot index, may be out of bounds
     * @return the minimum of the inventory stack limit and the stack's max size (64 when empty)
     */
    @Override
    public int limitAt(int slot) {
        ItemStack stack = getSlotStack(slot);
        int limit = inventory.getInventoryStackLimit();
        int max = stack == null ? 64 : Math.min(stack.getMaxStackSize(), 2147483647);
        return Math.min(limit, stack == null ? 64 : stack.getMaxStackSize());
    }

    /**
     * Inserts an amount of an item into the given slot if it is compatible.
     * 
     * @param slot       the slot index, may be out of bounds
     * @param key        the key of the item to insert
     * @param amount     how many to insert
     * @param stackLimit the per-stack limit to honor
     * @return how many were NOT inserted (the amount when the slot is incompatible or out of bounds)
     */
    @Override
    public int insert(int slot, String key, int amount, int stackLimit) {
        if (amount <= 0) {
            return amount;
        }
        ItemStack prototype = prototypes.get(key);
        if (prototype == null) {
            return amount;
        }
        if (slot < 0 || slot >= inventory.getSizeInventory()) {
            return amount;
        }
        ItemStack existing = inventory.getStackInSlot(slot);
        if (existing != null) {
            String existingKey = StackKeys.keyOf(existing);
            if (!existingKey.equals(key)) {
                return amount;
            }
        }
        if (!inventory.isItemValidForSlot(slot, prototype)) {
            return amount;
        }
        int limit = Math.min(inventory.getInventoryStackLimit(), prototype.getMaxStackSize());
        int existingCount = existing == null ? 0 : existing.stackSize;
        int room = Math.max(0, Math.min(limit, stackLimit) - existingCount);
        int inserted = Math.min(room, amount);
        if (inserted > 0) {
            ItemStack result;
            if (existing == null) {
                result = prototype.copy();
                result.stackSize = inserted;
            } else {
                result = existing.copy();
                result.stackSize = existing.stackSize + inserted;
            }
            inventory.setInventorySlotContents(slot, result);
            inventory.markDirty();
        }
        return amount - inserted;
    }

    /**
     * Extracts up to an amount from the given slot.
     * 
     * @param slot   the slot index, may be out of bounds
     * @param amount how many to remove
     * @return how many were actually removed (0 for an empty slot or a bad index)
     */
    @Override
    public int extract(int slot, int amount) {
        if (amount <= 0) {
            return 0;
        }
        if (slot < 0 || slot >= inventory.getSizeInventory()) {
            return 0;
        }
        ItemStack existing = inventory.getStackInSlot(slot);
        if (existing == null) {
            return 0;
        }
        int removed = Math.min(amount, existing.stackSize);
        ItemStack remainder = inventory.decrStackSize(slot, removed);
        return remainder == null ? 0 : remainder.stackSize;
    }

    private ItemStack getSlotStack(int slot) {
        if (slot < 0 || slot >= inventory.getSizeInventory()) {
            return null;
        }
        return inventory.getStackInSlot(slot);
    }
}
