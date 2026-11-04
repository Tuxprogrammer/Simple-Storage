package com.tuxprogrammer.simplestorage.block;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import com.tuxprogrammer.simplestorage.blockentity.NetworkQuery;
import com.tuxprogrammer.simplestorage.blockentity.PaintedTileEntity;

/**
 * A framed, paintable inventory cable block.
 *
 * <p>
 * Behaves the same as {@link InventoryCableBlock} — same bounds, same
 * connection rules, same {@link NetworkQuery#invalidate} calls on
 * placement, neighbour change, addition, and break — but additionally
 * carries a {@link PaintedTileEntity} so that it can be painted with a
 * facade texture.
 *
 * <p>
 * Extends {@link StorageBlockBase} directly (rather than
 * {@link InventoryCableBlock}) because the constructor needs to pass the
 * name {@code "inventory_cable_framed"} and the parent's constructor is
 * public, not protected.
 *
 * <p>
 * Side: both (the client-only parts live in simplestorage.client).
 */
public class FramedInventoryCableBlock extends StorageBlockBase implements IInventoryCable, IPaintable {

    /**
     * Constructs the framed inventory cable block.
     *
     * <p>
     * Uses the iron material with the name {@code "inventory_cable_framed"}
     * and a hardness of {@code 1.5F}. The block bounds are set to a small
     * cube in the centre of the block space.
     */
    public FramedInventoryCableBlock() {
        super(Material.iron, "inventory_cable_framed", 1.5F);
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
     * @param world the world the block is being placed in
     * @param meta  the block metadata
     * @return a new {@link PaintedTileEntity}
     */
    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new PaintedTileEntity();
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

    /**
     * Applies a paint facade to the block at the given position.
     *
     * <p>
     * The tile entity at the position must be a {@link PaintedTileEntity};
     * otherwise the paint is not applied and {@code false} is returned.
     *
     * @param world     the world the block is in
     * @param x         the block's x position
     * @param y         the block's y position
     * @param z         the block's z position
     * @param paint     the block whose texture the facade should show
     * @param paintMeta the metadata of the paint block to display
     * @return whether the block took the paint
     */
    @Override
    public boolean paint(World world, int x, int y, int z, Block paint, int paintMeta) {
        TileEntity tile = world.getTileEntity(x, y, z);
        if (!(tile instanceof PaintedTileEntity)) {
            return false;
        }
        ((PaintedTileEntity) tile).setPaint(paint, paintMeta);
        return true;
    }

    /**
     * Removes the paint facade from the block at the given position.
     *
     * <p>
     * Only removes paint if the block is currently painted; if it is not
     * painted, {@code false} is returned and nothing changes.
     *
     * @param world the world the block is in
     * @param x     the block's x position
     * @param y     the block's y position
     * @param z     the block's z position
     * @return whether the block had any paint that was removed
     */
    @Override
    public boolean scrape(World world, int x, int y, int z) {
        TileEntity tile = world.getTileEntity(x, y, z);
        if (!(tile instanceof PaintedTileEntity)) {
            return false;
        }
        PaintedTileEntity painted = (PaintedTileEntity) tile;
        if (!painted.isPainted()) {
            return false;
        }
        painted.setPaint(null, 0);
        return true;
    }
}
