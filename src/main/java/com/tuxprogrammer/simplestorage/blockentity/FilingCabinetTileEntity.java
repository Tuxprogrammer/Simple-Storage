package com.tuxprogrammer.simplestorage.blockentity;

import java.util.LinkedHashMap;
import java.util.Map;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import com.tuxprogrammer.simplestorage.core.Module;
import com.tuxprogrammer.simplestorage.inventory.CabinetModel;

/**
 * The tile entity backing the Filing Cabinet block.
 * <p>
 * Holds {@link CabinetModel#SLOTS} slots, each storing a single item. Slots are locked to the type
 * of the first item placed, and the cabinet exposes them to inventories as a full-sized,
 * single-item-slot inventory. Side: server authoritative; the item list syncs to clients only
 * through the container.
 */
public class FilingCabinetTileEntity extends GatedTileEntity implements ISidedInventory {

    private static final int SLOTS = CabinetModel.SLOTS;
    private static final int[] ALL_SLOTS = new int[SLOTS];
    static {
        for (int i = 0; i < SLOTS; i++) {
            ALL_SLOTS[i] = i;
        }
    }

    private ItemStack[] stacks = new ItemStack[SLOTS];
    private CabinetModel model = new CabinetModel();
    private String customName;

    @Override
    protected Module module() {
        return Module.STORAGE;
    }

    @Override
    protected void serverTick() {
        // no periodic work for now
    }

    /**
     * @return the registry name of the stack's item, or "" if the stack has no item
     */
    public static String typeKey(ItemStack stack) {
        if (stack == null) return "";
        Item item = stack.getItem();
        if (item == null) return "";
        return Item.itemRegistry.getNameForObject(item);
    }

    @Override
    public int getSizeInventory() {
        return SLOTS;
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        if (slot < 0 || slot >= SLOTS) return null;
        return stacks[slot];
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int slot) {
        return null;
    }

    @Override
    public ItemStack decrStackSize(int slot, int amount) {
        if (slot < 0 || slot >= SLOTS) return null;
        ItemStack stack = stacks[slot];
        if (stack == null) return null;
        stacks[slot] = null;
        model.remove(slot);
        return stack;
    }

    @Override
    public void setInventorySlotContents(int slot, ItemStack stack) {
        if (slot < 0 || slot >= SLOTS) return;
        if (stack == null || stack.stackSize <= 0) {
            if (stacks[slot] != null) {
                stacks[slot] = null;
                model.remove(slot);
            }
            return;
        }
        if (stacks[slot] != null) {
            stacks[slot] = null;
            model.remove(slot);
        }
        if (model.set(slot, typeKey(stack), stack.getMaxStackSize())) {
            ItemStack stored = stack.copy();
            stored.stackSize = 1;
            stacks[slot] = stored;
        }
    }

    @Override
    public int getInventoryStackLimit() {
        return 1;
    }

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack stack) {
        if (slot < 0 || slot >= SLOTS || stack == null) return false;
        return model.canPlace(slot, typeKey(stack), stack.getMaxStackSize());
    }

    @Override
    public String getInventoryName() {
        return customName != null ? customName : "container.simplestoragenh.filing_cabinet";
    }

    public String getName() {
        return getInventoryName();
    }

    @Override
    public boolean hasCustomInventoryName() {
        return customName != null;
    }

    public void setCustomName(String name) {
        customName = name;
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer player) {
        if (worldObj == null || worldObj.getTileEntity(xCoord, yCoord, zCoord) != this) return false;
        return player.getDistanceSq(xCoord + 0.5, yCoord + 0.5, zCoord + 0.5) < 64.0;
    }

    @Override
    public void openInventory() {}

    @Override
    public void closeInventory() {}

    public CabinetModel getModel() {
        return model;
    }

    // ISidedInventory

    @Override
    public int[] getAccessibleSlotsFromSide(int side) {
        return ALL_SLOTS;
    }

    @Override
    public boolean canInsertItem(int slot, ItemStack stack, int side) {
        return isItemValidForSlot(slot, stack);
    }

    @Override
    public boolean canExtractItem(int slot, ItemStack stack, int side) {
        return true;
    }

    // NBT

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        NBTTagList list = new NBTTagList();
        for (int i = 0; i < SLOTS; i++) {
            if (stacks[i] == null) continue;
            NBTTagCompound entry = new NBTTagCompound();
            stacks[i].writeToNBT(entry);
            entry.setShort(NbtKeys.SLOT, (short) i);
            list.appendTag(entry);
        }
        tag.setTag(NbtKeys.INVENTORY, list);
        if (customName != null) tag.setString(NbtKeys.CUSTOM_NAME, customName);
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        Map<Integer, String> slots = new LinkedHashMap<Integer, String>();
        NBTTagList list = tag.getTagList(NbtKeys.INVENTORY, 10);
        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound entry = list.getCompoundTagAt(i);
            short slot = entry.getShort(NbtKeys.SLOT);
            if (slot < 0 || slot >= SLOTS) continue;
            NBTTagCompound stackTag = (NBTTagCompound) entry.copy();
            stackTag.removeTag(NbtKeys.SLOT);
            ItemStack stack = ItemStack.loadItemStackFromNBT(stackTag);
            if (stack == null) continue;
            String type = typeKey(stack);
            if (type.isEmpty()) continue;
            slots.put((int) slot, type);
        }
        model = CabinetModel.fromSlots(slots);
        stacks = new ItemStack[SLOTS];
        // Rebuild stacks from the NBT list, keeping only the types the model accepts.
        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound entry = list.getCompoundTagAt(i);
            short slot = entry.getShort(NbtKeys.SLOT);
            if (slot < 0 || slot >= SLOTS) continue;
            NBTTagCompound stackTag = (NBTTagCompound) entry.copy();
            stackTag.removeTag(NbtKeys.SLOT);
            ItemStack stack = ItemStack.loadItemStackFromNBT(stackTag);
            if (stack == null) continue;
            String type = typeKey(stack);
            if (type.isEmpty()) continue;
            if (!type.equals(slots.get((int) slot))) continue;
            if (model.get((int) slot) == null) continue;
            stack.stackSize = 1;
            stacks[(int) slot] = stack;
        }
        if (tag.hasKey(NbtKeys.CUSTOM_NAME)) customName = tag.getString(NbtKeys.CUSTOM_NAME);
    }
}
