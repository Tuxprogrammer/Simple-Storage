package com.tuxprogrammer.simplestorage.block;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import com.tuxprogrammer.simplestorage.blockentity.InventoryConnectorTileEntity;
import com.tuxprogrammer.simplestorage.blockentity.NetworkQuery;

/**
 * A connector block that links an inventory to the cable network.
 *
 * <p>
 * The connector holds an {@link InventoryConnectorTileEntity} that records
 * which inventory it is attached to and mediates access to it from the
 * network. It is not facing-aware: it connects on any face.
 *
 * <p>
 * On placement, when a neighbour changes, and on break, the connector
 * invalidates the cached network at its position via
 * {@link NetworkQuery#invalidate} so the network is rebuilt on the next
 * query.
 *
 * <p>
 * Side: both (the client-only parts live in simplestorage.client).
 */
public class InventoryConnectorBlock extends StorageBlockBase {

    /**
     * Constructs the inventory connector block.
     *
     * <p>
     * Uses the iron material with the name {@code "inventory_connector"} and
     * a hardness of {@code 2.0F}.
     */
    public InventoryConnectorBlock() {
        super(net.minecraft.block.material.Material.iron, "inventory_connector", 2.0F);
    }

    /**
     * Creates the tile entity for a placed block.
     *
     * @param world the world the block is being placed in
     * @param meta  the block metadata
     * @return a new {@link InventoryConnectorTileEntity}
     */
    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new InventoryConnectorTileEntity();
    }

    /**
     * When a neighbour changes, invalidates the cached network at this
     * position on the server, then delegates to the super implementation.
     *
     * @param world     the world the block is in
     * @param x         the block x position
     * @param y         the block y position
     * @param z         the block z position
     * @param neighbour the block that changed
     */
    @Override
    public void onNeighborBlockChange(World world, int x, int y, int z, net.minecraft.block.Block neighbour) {
        if (!world.isRemote) {
            NetworkQuery.invalidate(world, x, y, z);
        }
        super.onNeighborBlockChange(world, x, y, z, neighbour);
    }

    /**
     * When the block is added to the world, invalidates the cached network at
     * this position on the server, then delegates to the super implementation.
     *
     * @param world the world the block is being added to
     * @param x     the block x position
     * @param y     the block y position
     * @param z     the block z position
     */
    @Override
    public void onBlockAdded(World world, int x, int y, int z) {
        if (!world.isRemote) {
            NetworkQuery.invalidate(world, x, y, z);
        }
        super.onBlockAdded(world, x, y, z);
    }

    /**
     * When the block is broken, invalidates the cached network at this
     * position on the server, then delegates to the super implementation.
     *
     * @param world the world the block is in
     * @param x     the block x position
     * @param y     the block y position
     * @param z     the block z position
     * @param block the block being broken
     * @param meta  the block metadata
     */
    @Override
    public void breakBlock(World world, int x, int y, int z, net.minecraft.block.Block block, int meta) {
        if (!world.isRemote) {
            NetworkQuery.invalidate(world, x, y, z);
        }
        super.breakBlock(world, x, y, z, block, meta);
    }
}
