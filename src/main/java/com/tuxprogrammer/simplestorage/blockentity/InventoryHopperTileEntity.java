package com.tuxprogrammer.simplestorage.blockentity;
// Ported from Tom's Simple Storage (MIT):
// https://github.com/tom5454/Toms-Storage/blob/95bb61d/NeoForge/src/platform-shared/java/com/tom/storagemod/block/entity/BasicInventoryHopperBlockEntity.java
// Avoids: STOR-08, STOR-09

import java.util.List;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import com.tuxprogrammer.simplestorage.block.IInventoryCable;
import com.tuxprogrammer.simplestorage.config.BehaviorConfig;
import com.tuxprogrammer.simplestorage.core.Module;
import com.tuxprogrammer.simplestorage.inventory.HopperLogic;
import com.tuxprogrammer.simplestorage.inventory.HopperLogic.Decision;

/**
 * The basic inventory hopper: moves one item per cycle from the inventory on
 * the input (top) side to the inventory on the output side.
 *
 * <p>
 * The block metadata (0..5) selects the OUTPUT side via
 * {@code ForgeDirection.getOrientation(meta % 6)}; the input is the opposite
 * side. Side: server authoritative.
 */
public class InventoryHopperTileEntity extends GatedTileEntity {

    /**
     * The optional filter item; items are only moved when this is null or
     * matches the moved item.
     */
    private ItemStack filter;

    /**
     * The transfer decision state machine, created lazily.
     */
    private HopperLogic logic;

    /**
     * Whether the hopper is powered on.
     */
    private boolean enabled = true;

    /**
     * Identity hash of the output inventory from the last cycle, 0 when there
     * was none.
     */
    private int lastBottomHash;

    @Override
    protected Module module() {
        return Module.NETWORK;
    }

    /**
     * @return the output side of the hopper, derived from the block metadata
     */
    public ForgeDirection getOutput() {
        return this.output();
    }

    /**
     * The output side of the hopper, derived from the block metadata (0..5).
     */
    private ForgeDirection output() {
        return ForgeDirection.getOrientation(this.getBlockMetadata() % 6);
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        this.filter = compound.hasKey(NbtKeys.FILTER)
            ? ItemStack.loadItemStackFromNBT(compound.getCompoundTag(NbtKeys.FILTER))
            : null;
        // NbtKeys.WAITING is informational: the cooldown is not restored on read.
    }

    @Override
    public void writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        if (this.filter != null) {
            NBTTagCompound filterTag = new NBTTagCompound();
            this.filter.writeToNBT(filterTag);
            compound.setTag(NbtKeys.FILTER, filterTag);
        }
        compound.setInteger(NbtKeys.WAITING, this.logic == null ? 0 : this.logic.remaining());
    }

    /**
     * @return the optional filter item, or null when unfiltered
     */
    public ItemStack getFilter() {
        return this.filter;
    }

    /**
     * Sets the filter; null clears it.
     */
    public void setFilter(ItemStack filter) {
        this.filter = filter == null ? null : filter.copy();
        this.markDirty();
    }

    /**
     * @return whether the hopper is enabled
     */
    public boolean isEnabled() {
        return this.enabled;
    }

    /**
     * @param enabled
     *                whether the hopper is enabled
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    protected void serverTick() {
        if (worldObj == null) {
            return;
        }
        HopperLogic hopperLogic = this.logic();
        ForgeDirection output = this.output();
        ForgeDirection input = output.getOpposite();
        int inputX = this.xCoord + input.offsetX;
        int inputY = this.yCoord + input.offsetY;
        int inputZ = this.zCoord + input.offsetZ;
        int outputX = this.xCoord + output.offsetX;
        int outputY = this.yCoord + output.offsetY;
        int outputZ = this.zCoord + output.offsetZ;

        boolean topIsNetwork = isNetworkSource(this.worldObj, inputX, inputY, inputZ);
        boolean bottomChanged = this.updateBottomHash(this.worldObj, outputX, outputY, outputZ);

        Decision decision = hopperLogic.tick(this.enabled, topIsNetwork, this.filter != null, bottomChanged);
        if (decision != Decision.TRANSFER) {
            return;
        }

        IInventory target = InventoryAccess.at(this.worldObj, outputX, outputY, outputZ);
        boolean moved;
        if (topIsNetwork) {
            moved = moveOneFromNetwork(this.worldObj, inputX, inputY, inputZ, target);
        } else {
            IInventory source = InventoryAccess.at(this.worldObj, inputX, inputY, inputZ);
            moved = moveOne(source, target, this.filter);
        }
        hopperLogic.transferred(moved);
    }

    /**
     * Creates the transfer decision state machine on first use.
     */
    private HopperLogic logic() {
        if (this.logic == null) {
            this.logic = new HopperLogic(BehaviorConfig.basicHopperCooldown);
        }
        return this.logic;
    }

    /**
     * Whether the block at the input neighbour is an inventory cable or the
     * tile entity is a network endpoint.
     */
    private static boolean isNetworkSource(World world, int x, int y, int z) {
        if (world == null) {
            return false;
        }
        Object block = world.getBlock(x, y, z);
        if (block instanceof IInventoryCable) {
            return true;
        }
        TileEntity tileEntity = world.getTileEntity(x, y, z);
        return tileEntity instanceof InventoryNodeTile;
    }

    /**
     * Re-records the identity hash of the output inventory and reports whether
     * it changed since the last cycle.
     */
    private boolean updateBottomHash(World world, int x, int y, int z) {
        int current = 0;
        IInventory target = InventoryAccess.at(world, x, y, z);
        if (target != null) {
            current = System.identityHashCode(target);
        }
        boolean changed = current != this.lastBottomHash;
        this.lastBottomHash = current;
        return changed;
    }

    /**
     * Tries each network inventory in order until one yields an item.
     */
    private static boolean moveOneFromNetwork(World world, int x, int y, int z, IInventory target) {
        List<IInventory> inventories = NetworkQuery.inventoriesFrom(world, x, y, z);
        for (IInventory source : inventories) {
            if (moveOne(source, target, null)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Extracts one item from the source (respecting the filter) and inserts it
     * into the target; if the insertion fails the item is put back into the
     * source.
     *
     * @param source
     *                     the source inventory, may be null
     * @param target
     *                     the target inventory, may be null
     * @param filterOrNull
     *                     the optional filter item
     * @return whether an item moved
     */
    static boolean moveOne(IInventory source, IInventory target, ItemStack filterOrNull) {
        if (source == null || target == null) {
            return false;
        }
        ItemStack stack = InventoryAccess.extractOne(source, filterOrNull);
        if (stack == null) {
            return false;
        }
        if (InventoryAccess.insertOne(target, stack)) {
            return true;
        }
        InventoryAccess.insertOne(source, stack);
        return false;
    }
}
