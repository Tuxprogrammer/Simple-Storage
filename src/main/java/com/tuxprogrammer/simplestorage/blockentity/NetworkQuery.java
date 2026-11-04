package com.tuxprogrammer.simplestorage.blockentity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import com.tuxprogrammer.simplestorage.inventory.CableGraph;
import com.tuxprogrammer.simplestorage.inventory.CableNetworks;

/**
 * Queries a cable network: which inventories it serves and how much of an
 * item it holds.
 *
 * <p>
 * A scan starts at the query position and walks the cables of that dimension
 * with {@link CableGraph#scan}. Every attached position whose tile entity is
 * an {@link InventoryNodeTile} (other than the query position itself)
 * contributes all of its {@link InventoryNodeTile#exposedInventories()}.
 *
 * <p>
 * The per-dimension graphs are held in a single static
 * {@link CableNetworks}; the world is only used to build a
 * {@link WorldTopology} and to read tile entities.
 *
 * <p>
 * Side: server.
 */
public final class NetworkQuery {

    /**
     * The per-dimension cable graphs.
     */
    private static final CableNetworks NETWORKS = new CableNetworks();

    private NetworkQuery() {}

    /**
     * All inventories served by the cable network at the position.
     *
     * <p>
     * Scans from the packed position with a fresh {@link WorldTopology} on
     * the dimension's {@link CableGraph}. For every attached position whose
     * tile entity is an {@link InventoryNodeTile} (excluding the query
     * position's own tile entity), all of its exposed inventories are added
     * (nulls skipped). Attached positions are sorted ascending before they
     * are read, so the result order is deterministic.
     *
     * @param world the world
     * @param x     the block x position of the query
     * @param y     the block y position of the query
     * @param z     the block z position of the query
     * @return the exposed inventories of the network, possibly empty
     */
    public static List<IInventory> inventoriesFrom(World world, int x, int y, int z) {
        long start = WorldTopology.pack(x, y, z);
        CableGraph graph = NETWORKS.forLevel(world.provider.dimensionId);
        java.util.Set<Long> attached = graph.scan(start, new WorldTopology(world));

        List<Long> sorted = new ArrayList<Long>(attached);
        Collections.sort(sorted);

        List<IInventory> inventories = new ArrayList<IInventory>();
        for (Long pos : sorted) {
            if (pos == start) {
                continue; // not the query position's own tile entity
            }
            int px = WorldTopology.unpackX(pos);
            int py = WorldTopology.unpackY(pos);
            int pz = WorldTopology.unpackZ(pos);
            TileEntity tile = world.getTileEntity(px, py, pz);
            if (!(tile instanceof InventoryNodeTile)) {
                continue;
            }
            List<IInventory> exposed = ((InventoryNodeTile) tile).exposedInventories();
            if (exposed == null) {
                continue;
            }
            for (IInventory inventory : exposed) {
                if (inventory != null) {
                    inventories.add(inventory);
                }
            }
        }
        return inventories;
    }

    /**
     * The total number of items held by the network's inventories that match
     * {@code filterOrNull}.
     *
     * <p>
     * Sums the {@code stackSize} of every stack in the network's inventories
     * that matches the filter. A null filter counts everything; otherwise a
     * stack matches when it is the same item, the same damage (when the item
     * has subtypes), and passes
     * {@link ItemStack#areItemStackTagsEqual}.
     *
     * @param world        the world
     * @param x            the block x position of the query
     * @param y            the block y position of the query
     * @param z            the block z position of the query
     * @param filterOrNull the stack to match, or null to count everything
     * @return the total matching item count
     */
    public static long amountOf(World world, int x, int y, int z, ItemStack filterOrNull) {
        long total = 0L;
        for (IInventory inventory : inventoriesFrom(world, x, y, z)) {
            for (int slot = 0; slot < inventory.getSizeInventory(); slot++) {
                ItemStack stack = inventory.getStackInSlot(slot);
                if (stack == null) {
                    continue;
                }
                if (filterOrNull == null || matches(stack, filterOrNull)) {
                    total += stack.stackSize;
                }
            }
        }
        return total;
    }

    /**
     * Invalidates the cached network at the position: the next scan re-walks
     * it, so a changed block or tile entity at the position is picked up.
     *
     * @param world the world
     * @param x     the block x position
     * @param y     the block y position
     * @param z     the block z position
     */
    public static void invalidate(World world, int x, int y, int z) {
        NETWORKS.forLevel(world.provider.dimensionId)
            .markNodeInvalid(WorldTopology.pack(x, y, z));
    }

    /**
     * Unloads the cached graphs for a dimension (e.g. on dimension unmount),
     * dropping every cached network for it.
     *
     * @param dimension the dimension id to unload
     */
    public static void unloadLevel(int dimension) {
        NETWORKS.unload(dimension);
    }

    /**
     * Whether {@code stack} matches {@code other}: same item, same damage
     * (when the item has subtypes), and equal tags.
     */
    private static boolean matches(ItemStack stack, ItemStack other) {
        if (stack.getItem() != other.getItem()) {
            return false;
        }
        if (stack.getItem()
            .getHasSubtypes() && stack.getItemDamage() != other.getItemDamage()) {
            return false;
        }
        return ItemStack.areItemStackTagsEqual(stack, other);
    }
}
