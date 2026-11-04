package com.tuxprogrammer.simplestorage.blockentity;

import java.util.Collections;
import java.util.List;

import net.minecraft.inventory.IInventory;
import net.minecraftforge.common.util.ForgeDirection;

import com.tuxprogrammer.simplestorage.core.Module;
import com.tuxprogrammer.simplestorage.core.ModuleState;

/**
 * The network endpoint that exposes the inventory in front of it.
 *
 * <p>
 * Used by the inventory proxy and by both cable connectors: the front of the
 * endpoint is {@code ForgeDirection.getOrientation(getBlockMetadata() % 6)},
 * and the single exposed inventory is whatever lives at the block position in
 * front of this tile entity.
 *
 * <p>
 * The {@link Module#NETWORK} flag gates behavior, never registration: an
 * endpoint that is registered but whose module is off exposes nothing.
 *
 * <p>
 * Side: server authoritative.
 */
public class FaceNodeTileEntity extends PaintedTileEntity implements InventoryNodeTile {

    /**
     * The single inventory in front of this endpoint.
     *
     * <p>
     * Empty when there is no world, when the {@link Module#NETWORK} module is
     * inactive, or when there is no inventory at the front position; never
     * contains null.
     *
     * @return the exposed inventories, possibly empty
     */
    @Override
    public List<IInventory> exposedInventories() {
        if (worldObj == null || !ModuleState.configured()
            .isActive(Module.NETWORK)) {
            return Collections.emptyList();
        }
        ForgeDirection front = ForgeDirection.getOrientation(getBlockMetadata() % 6);
        IInventory inventory = InventoryAccess
            .at(worldObj, xCoord + front.offsetX, yCoord + front.offsetY, zCoord + front.offsetZ);
        if (inventory == null) {
            return Collections.emptyList();
        }
        return Collections.singletonList(inventory);
    }
}
