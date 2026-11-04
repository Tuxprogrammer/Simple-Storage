package com.tuxprogrammer.simplestorage.block;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import com.tuxprogrammer.simplestorage.blockentity.PaintedTileEntity;

/**
 * A trim block with a paintable facade.
 *
 * <p>
 * Like {@link TrimBlock}, it is an opaque filler that the inventory cable
 * floods through, but in addition it can be painted so that it renders with
 * the texture of another block. The paint is stored on a
 * {@link PaintedTileEntity}.
 *
 * <p>
 * The facade rendering itself is not part of this class: it is handled by
 * the client-only code in {@code simplestorage.client}.
 *
 * <p>
 * Side: both (the client-only parts live in simplestorage.client).
 */
public class PaintedTrimBlock extends StorageBlockBase implements ITrim, IPaintable {

    /**
     * Constructs the painted trim block.
     *
     * <p>
     * Uses the iron material with the name {@code "painted_trim"} and a
     * hardness of {@code 2.0F}.
     */
    public PaintedTrimBlock() {
        super(Material.iron, "painted_trim", 2.0F);
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
