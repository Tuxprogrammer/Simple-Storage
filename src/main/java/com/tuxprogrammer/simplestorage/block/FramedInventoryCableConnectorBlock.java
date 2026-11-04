package com.tuxprogrammer.simplestorage.block;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import com.tuxprogrammer.simplestorage.blockentity.FaceNodeTileEntity;
import com.tuxprogrammer.simplestorage.blockentity.NetworkQuery;

/**
 * A framed, paintable variant of the inventory cable connector block.
 *
 * <p>
 * Behaves the same as {@link InventoryCableConnectorBlock} — same facing
 * metadata derived from the placer's look direction, same connection rules
 * (accepts cables on every face except the front), same
 * {@link NetworkQuery#invalidate} calls on placement, neighbour change,
 * addition, and break — but additionally implements {@link IPaintable} so
 * its {@link FaceNodeTileEntity} can be painted with a facade texture.
 *
 * <p>
 * Extends {@link StorageBlockBase} directly (rather than
 * {@link InventoryCableConnectorBlock}) because the constructor needs to pass
 * the name {@code "inventory_cable_connector_framed"} and the parent's
 * constructor is protected.
 *
 * <p>
 * Side: both (the client-only parts live in simplestorage.client).
 */
public class FramedInventoryCableConnectorBlock extends StorageBlockBase implements IInventoryCable, IPaintable {

    /**
     * Constructs the framed inventory cable connector block.
     *
     * <p>
     * Uses the iron material with the name
     * {@code "inventory_cable_connector_framed"} and a hardness of
     * {@code 1.5F}.
     */
    public FramedInventoryCableConnectorBlock() {
        super(net.minecraft.block.material.Material.iron, "inventory_cable_connector_framed", 1.5F);
    }

    /**
     * Derives the facing metadata (0..5) from the placer's look direction.
     *
     * @param placer the entity placing the block
     * @return 1 (up) when looking steeply down, 0 (down) when looking steeply
     *         up, otherwise 2/5/3/4 for north/east/south/west
     */
    private static int facingFrom(EntityLivingBase placer) {
        if (placer.rotationPitch > 60.0F) {
            return 1; // up
        }
        if (placer.rotationPitch < -60.0F) {
            return 0; // down
        }
        switch (MathHelper.floor_double(placer.rotationYaw * 4.0F / 360.0F + 0.5D) & 3) {
            case 0:
                return 2; // north
            case 1:
                return 5; // east
            case 2:
                return 3; // south
            default:
                return 4; // west
        }
    }

    /**
     * Creates the tile entity for a placed block.
     *
     * @param world the world the block is being placed in
     * @param meta  the block metadata
     * @return a new {@link FaceNodeTileEntity}
     */
    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new FaceNodeTileEntity();
    }

    /**
     * When the block is placed by a player, sets the facing metadata from the
     * placer's look direction and invalidates the cached network at the new
     * position on the server.
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
        world.setBlockMetadataWithNotify(x, y, z, facingFrom(placer), 3);
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

    /**
     * The connector accepts a cable connection on every face except its front
     * face. The front face (determined by the block's facing metadata) is
     * where the attached inventory connects, so a cable arriving from that
     * side is rejected.
     *
     * @param world the world the connector is in
     * @param x     the block x position
     * @param y     the block y position
     * @param z     the block z position
     * @param side  the face of this block the connection arrives at
     * @return {@code false} if {@code side} is the block's front face,
     *         {@code true} otherwise
     */
    @Override
    public boolean canConnectFrom(net.minecraft.world.IBlockAccess world, int x, int y, int z, ForgeDirection side) {
        return side != ForgeDirection.getOrientation(world.getBlockMetadata(x, y, z) % 6);
    }

    /**
     * Applies a paint facade to the block at the given position.
     *
     * <p>
     * Only paints if the tile entity at that position is a
     * {@link FaceNodeTileEntity}; otherwise nothing changes and {@code false}
     * is returned.
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
    public boolean paint(World world, int x, int y, int z, net.minecraft.block.Block paint, int paintMeta) {
        TileEntity tile = world.getTileEntity(x, y, z);
        if (!(tile instanceof FaceNodeTileEntity)) {
            return false;
        }
        ((FaceNodeTileEntity) tile).setPaint(paint, paintMeta);
        return true;
    }

    /**
     * Removes the paint facade from the block at the given position.
     *
     * <p>
     * Only removes paint if the tile entity is a {@link FaceNodeTileEntity}
     * that is currently painted; if it is not painted, {@code false} is
     * returned and nothing changes.
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
        if (!(tile instanceof FaceNodeTileEntity)) {
            return false;
        }
        FaceNodeTileEntity painted = (FaceNodeTileEntity) tile;
        if (!painted.isPainted()) {
            return false;
        }
        painted.setPaint(null, 0);
        return true;
    }
}
