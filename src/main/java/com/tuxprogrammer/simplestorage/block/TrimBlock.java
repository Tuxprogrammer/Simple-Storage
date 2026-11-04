package com.tuxprogrammer.simplestorage.block;

import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

/**
 * A plain trim block: an opaque, full-block filler that an inventory cable
 * floods through.
 *
 * <p>
 * The trim block has no state of its own, so it does not create a tile
 * entity; it is only interesting to the cable network walk because it marks
 * the positions the walk is allowed to cross.
 *
 * <p>
 * Side: both (the client-only parts live in simplestorage.client).
 */
public class TrimBlock extends StorageBlockBase implements ITrim {

    /**
     * Constructs the trim block.
     *
     * <p>
     * Uses the iron material with the name {@code "trim"} and a hardness of
     * {@code 2.0F}.
     */
    public TrimBlock() {
        super(Material.iron, "trim", 2.0F);
    }

    /**
     * Creates the tile entity for a placed block.
     *
     * <p>
     * The trim block carries no state, so no tile entity is created.
     *
     * @param world the world the block is being placed in
     * @param meta  the block metadata
     * @return always {@code null}
     */
    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return null;
    }
}
