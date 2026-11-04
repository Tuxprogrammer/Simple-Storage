package com.tuxprogrammer.simplestorage.block;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import com.tuxprogrammer.simplestorage.blockentity.InventoryInterfaceTileEntity;
import com.tuxprogrammer.simplestorage.blockentity.NetworkQuery;

/**
 * An interface block that exposes an inventory to the cable network and also
 * passes the network through itself.
 *
 * <p>
 * The interface holds an {@link InventoryInterfaceTileEntity} that mediates
 * access to the inventory it is attached to. Because it passes the network
 * through, it accepts a cable connection on every face — the network can
 * cross from any neighbour to any other neighbour through the interface.
 *
 * <p>
 * On placement, when a neighbour changes, and on break, the interface
 * invalidates the cached network at its position via
 * {@link NetworkQuery#invalidate} so the network is rebuilt on the next
 * query.
 *
 * <p>
 * Side: both (the client-only parts live in simplestorage.client).
 */
public class InventoryInterfaceBlock extends StorageBlockBase implements IInventoryCable {

    /**
     * Constructs the inventory interface block.
     *
     * <p>
     * Uses the iron material with the name {@code "inventory_interface"} and
     * a hardness of {@code 2.0F}.
     */
    public InventoryInterfaceBlock() {
        super(net.minecraft.block.material.Material.iron, "inventory_interface", 2.0F);
    }

    /**
     * Creates the tile entity for a placed block.
     *
     * @param world the world the block is being placed in
     * @param meta  the block metadata
     * @return a new {@link InventoryInterfaceTileEntity}
     */
    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new InventoryInterfaceTileEntity();
    }

    /**
     * The interface passes the network through itself, so it accepts a cable
     * connection from every neighbour on every face.
     *
     * @param world the world the interface is in
     * @param x     the block x position
     * @param y     the block y position
     * @param z     the block z position
     * @param side  the face of this block the connection arrives at
     * @return always {@code true}
     */
    @Override
    public boolean canConnectFrom(net.minecraft.world.IBlockAccess world, int x, int y, int z, ForgeDirection side) {
        return true;
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
