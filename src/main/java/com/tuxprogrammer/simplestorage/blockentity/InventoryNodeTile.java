package com.tuxprogrammer.simplestorage.blockentity;

import java.util.List;

import net.minecraft.inventory.IInventory;

/**
 * Marks a tile entity as a functional node of a cable network: an endpoint
 * that hangs off the cable blocks and exposes its inventories to the network.
 *
 * <p>
 * A functional node does not conduct the network (it is not a cable); it is
 * the thing the network reaches <em>to</em>. The network scan collects these
 * and queries their {@link #exposedInventories()} to build the set of
 * inventories the network serves.
 *
 * <p>
 * Side: server logic, but the marker itself carries no side assumption.
 */
public interface InventoryNodeTile {

    /**
     * The inventories this node exposes to the cable network.
     *
     * <p>
     * Entries may be {@code null} (callers skip them) and the order is
     * undefined here; callers that need a deterministic order sort themselves.
     *
     * @return the exposed inventories, possibly empty
     */
    List<IInventory> exposedInventories();
}
