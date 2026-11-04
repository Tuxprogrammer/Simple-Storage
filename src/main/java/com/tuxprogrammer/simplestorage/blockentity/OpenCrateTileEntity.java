package com.tuxprogrammer.simplestorage.blockentity;
// Ported from Tom's Simple Storage (MIT):
// https://github.com/tom5454/Toms-Storage/blob/95bb61d/NeoForge/src/main/java/com/tom/storagemod/block/entity/OpenCrateBlockEntity.java
// Avoids: STOR-01, STOR-04

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.common.util.ForgeDirection;

import com.tuxprogrammer.simplestorage.core.Module;
import com.tuxprogrammer.simplestorage.inventory.StaggerGate;

/**
 * OpenCrateTileEntity exposes the item entities lying in front of the crate as a virtual inventory.
 *
 * <p>
 * The last slot is the append slot (STOR-04): it always reports empty, always accepts items,
 * and {@link #setInventorySlotContents(int, net.minecraft.item.ItemStack)} hands a copy of the
 * stack to the {@link ItemSink} and then rescans. A plain {@code IInventory} consumer can rely on
 * it being a real, consistently-defined slot rather than a phantom.
 *
 * <p>
 * There is no NBT: the crate saves nothing.
 *
 * <p>
 * Side: server authoritative.
 */
public class OpenCrateTileEntity extends GatedTileEntity implements ISidedInventory {

    /**
     * Reads the item entities lying in front of the crate.
     */
    public interface ItemSource {

        /**
         * @return copies of the stacks of the {@link EntityItem} entities in the 1x1x1 box of the block
         *         in front of the crate
         */
        List<ItemStack> scan();
    }

    /**
     * Drops item entities at the centre of the front block.
     */
    public interface ItemSink {

        /**
         * Drops the given stack as a new {@link EntityItem} at the centre of the front block.
         */
        void accept(ItemStack stack);
    }

    private ItemSource source;
    private ItemSink sink;
    private List<ItemStack> items = new ArrayList<ItemStack>();

    @Override
    protected Module module() {
        return Module.STORAGE;
    }

    @Override
    protected void serverTick() {
        if (worldObj == null) {
            return;
        }
        if (StaggerGate.isDue(
            worldObj.getTotalWorldTime(),
            Objects.hash(xCoord, yCoord, zCoord),
            StaggerGate.OPEN_CRATE_INTERVAL)) {
            ensureDefaults();
            rescan();
        }
    }

    /**
     * Rebuilds {@link #items} from {@link #source}.
     */
    public void rescan() {
        items.clear();
        List<ItemStack> scanned = source.scan();
        if (scanned != null) {
            items.addAll(scanned);
        }
    }

    /**
     * Sets the world-backed {@link ItemSource} and {@link ItemSink} implementations if they have not been
     * supplied explicitly.
     */
    void ensureDefaults() {
        if (source == null) {
            source = new ItemSource() {

                @Override
                public List<ItemStack> scan() {
                    List<ItemStack> stacks = new ArrayList<ItemStack>();
                    ForgeDirection front = ForgeDirection.getOrientation(getBlockMetadata() % 6);
                    double minX = xCoord + front.offsetX - 0.5D;
                    double minY = yCoord + front.offsetY - 0.5D;
                    double minZ = zCoord + front.offsetZ - 0.5D;
                    double maxX = xCoord + front.offsetX + 0.5D;
                    double maxY = yCoord + front.offsetY + 0.5D;
                    double maxZ = zCoord + front.offsetZ + 0.5D;
                    for (Object object : worldObj.getEntitiesWithinAABB(
                        EntityItem.class,
                        AxisAlignedBB.getBoundingBox(minX, minY, minZ, maxX, maxY, maxZ))) {
                        EntityItem entityItem = (EntityItem) object;
                        if (entityItem.getEntityItem() != null) {
                            stacks.add(
                                entityItem.getEntityItem()
                                    .copy());
                        }
                    }
                    return stacks;
                }
            };
        }
        if (sink == null) {
            sink = new ItemSink() {

                @Override
                public void accept(ItemStack stack) {
                    ForgeDirection front = ForgeDirection.getOrientation(getBlockMetadata() % 6);
                    EntityItem entityItem = new EntityItem(
                        worldObj,
                        xCoord + front.offsetX + 0.5D,
                        yCoord + front.offsetY + 0.5D,
                        zCoord + front.offsetZ + 0.5D,
                        stack.copy());
                    worldObj.spawnEntityInWorld(entityItem);
                }
            };
        }
    }

    public void setItemSource(ItemSource source) {
        this.source = source;
    }

    public void setItemSink(ItemSink sink) {
        this.sink = sink;
    }

    // ------------------------------------------------------------------
    // IInventory
    // ------------------------------------------------------------------

    @Override
    public int getSizeInventory() {
        // The last slot is the append slot: see getStackInSlot(int).
        return items.size() + 1;
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        if (slot == items.size()) {
            // STOR-04: the append slot is a real, consistently-defined slot, not a phantom.
            // It always reports empty, always accepts items, and writes go to the sink.
            return null;
        }
        if (slot < 0 || slot >= items.size()) {
            return null;
        }
        return items.get(slot);
    }

    @Override
    public ItemStack decrStackSize(int slot, int amount) {
        if (slot < 0 || slot >= items.size() || amount <= 0) {
            return null;
        }
        ItemStack stack = items.get(slot);
        if (stack == null) {
            return null;
        }
        ItemStack split;
        if (stack.stackSize <= amount) {
            split = stack;
            items.set(slot, null);
        } else {
            split = stack.splitStack(amount);
            stack.stackSize -= amount;
        }
        // The physical entity update is not modelled here; the next due tick's rescan()
        // re-reads the world.
        markDirty();
        return split;
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int slot) {
        return null;
    }

    @Override
    public void setInventorySlotContents(int slot, ItemStack stack) {
        if (slot == items.size()) {
            // Append slot: hand a copy of the stack to the sink and re-read the world.
            if (stack != null) {
                sink.accept(stack.copy());
            }
            rescan();
            return;
        }
        if (slot < 0 || slot >= items.size()) {
            return;
        }
        items.set(slot, stack);
        markDirty();
    }

    @Override
    public int getInventoryStackLimit() {
        return 64;
    }

    @Override
    public String getInventoryName() {
        return "container.simplestoragenh.open_crate";
    }

    @Override
    public boolean hasCustomInventoryName() {
        return false;
    }

    /**
     * @return false-safe: the open crate is not useable by a player.
     */
    @Override
    public boolean isUseableByPlayer(EntityPlayer player) {
        return false;
    }

    @Override
    public void openInventory() {}

    @Override
    public void closeInventory() {}

    // ------------------------------------------------------------------
    // ISidedInventory
    // ------------------------------------------------------------------

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack stack) {
        if (stack == null) {
            return false;
        }
        return slot == items.size();
    }

    @Override
    public int[] getAccessibleSlotsFromSide(int side) {
        int size = getSizeInventory();
        int[] slots = new int[size];
        for (int i = 0; i < size; i++) {
            slots[i] = i;
        }
        return slots;
    }

    @Override
    public boolean canInsertItem(int slot, ItemStack stack, int side) {
        return slot == items.size();
    }

    @Override
    public boolean canExtractItem(int slot, ItemStack stack, int side) {
        return slot >= 0 && slot < items.size();
    }
}
