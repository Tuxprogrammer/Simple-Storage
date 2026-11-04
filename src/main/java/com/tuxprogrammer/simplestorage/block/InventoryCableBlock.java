package com.tuxprogrammer.simplestorage.block;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import com.tuxprogrammer.simplestorage.blockentity.NetworkQuery;

/**
 * A small, semi-transparent cable block that carries an inventory network.
 *
 * <p>
 * The cable renders as a thin 1/4-cube in the centre of the block space
 * (bounds {@code 0.3125..0.6875}) so that neighbouring blocks are visible
 * through it. It accepts a connection from every neighbour on every face.
 *
 * <p>
 * On placement, when a neighbour changes, and on break, the cable calls
 * {@link NetworkQuery#invalidate} on the server so that the cached network
 * at that position is rebuilt on the next query.
 *
 * <p>
 * Side: both (the client-only parts live in simplestorage.client).
 */
public class InventoryCableBlock extends StorageBlockBase implements IInventoryCable {

    /**
     * Constructs the inventory cable block.
     *
     * <p>
     * Uses the iron material with the name {@code "inventory_cable"} and a
     * hardness of {@code 1.5F}. The block bounds are set to a small cube in
     * the centre of the block space.
     */
    public InventoryCableBlock() {
        super(Material.iron, "inventory_cable", 1.5F);
        setBlockBounds(0.3125F, 0.3125F, 0.3125F, 0.6875F, 0.6875F, 0.6875F);
    }

    /**
     * The cable is not opaque: neighbouring blocks are visible through it.
     *
     * @return always {@code false}
     */
    @Override
    public boolean isOpaqueCube() {
        return false;
    }

    /**
     * The cable is not rendered as a normal full block: the client-only code
     * in {@code simplestorage.client} handles its rendering.
     *
     * @return always {@code false}
     */
    @Override
    public boolean renderAsNormalBlock() {
        return false;
    }

    /**
     * The cable accepts a connection from every neighbour on every face.
     *
     * @param world the world the cable is in
     * @param x     the block x position of this cable
     * @param y     the block y position of this cable
     * @param z     the block z position of this cable
     * @param side  the face of this block the connection arrives at
     * @return always {@code true}
     */
    @Override
    public boolean canConnectFrom(net.minecraft.world.IBlockAccess world, int x, int y, int z, ForgeDirection side) {
        return true;
    }

    /**
     * Creates the tile entity for a placed block.
     *
     * <p>
     * The cable carries no state of its own; the network is queried from
     * {@link NetworkQuery}, so no tile entity is created.
     *
     * @param world the world the block is being placed in
     * @param meta  the block metadata
     * @return always {@code null}
     */
    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return null;
    }

    /**
     * When the block is placed by a player, invalidates the cached network
     * at the new position on the server.
     *
     * @param world  the world the block is being placed in
     * @param x      the block x position
     * @param y      the block y position
     * @param z      the block z position
     * @param placer the entity that placed the block
     * @param stack  the item stack that was placed
     */
    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase placer, ItemStack stack) {
        if (!world.isRemote) {
            NetworkQuery.invalidate(world, x, y, z);
        }
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
    public void onNeighborBlockChange(World world, int x, int y, int z, Block neighbour) {
        if (!world.isRemote) {
            NetworkQuery.invalidate(world, x, y, z);
        }
        super.onNeighborBlockChange(world, x, y, z, neighbour);
    }

    /**
     * When the block is added to the world, invalidates the cached network
     * at the position on the server, then delegates to the super
     * implementation.
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
     * When the block is broken, invalidates the cached network at the
     * position on the server, then delegates to the super implementation.
     *
     * @param world the world the block is being broken in
     * @param x     the block x position
     * @param y     the block y position
     * @param z     the block z position
     * @param block the block being broken
     * @param meta  the block metadata
     */
    @Override
    public void breakBlock(World world, int x, int y, int z, Block block, int meta) {
        if (!world.isRemote) {
            NetworkQuery.invalidate(world, x, y, z);
        }
        super.breakBlock(world, x, y, z, block, meta);
    }
}
