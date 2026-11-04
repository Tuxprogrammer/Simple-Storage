package com.tuxprogrammer.simplestorage.block;

import net.minecraft.block.Block;
import net.minecraft.world.World;

/**
 * Implemented by blocks that can receive a painted facade, e.g. storage
 * blocks whose outer appearance can be changed to another block's texture.
 */
public interface IPaintable {

    /**
     * Applies paint to the block at the given position, making it render with
     * the given block's textures and metadata.
     *
     * @param world     the world the block is in
     * @param x         the block's x position
     * @param y         the block's y position
     * @param z         the block's z position
     * @param paint     the block whose texture the facade should show
     * @param paintMeta the metadata of the paint block to display
     * @return whether the block took the paint
     */
    boolean paint(World world, int x, int y, int z, Block paint, int paintMeta);

    /**
     * Removes any paint from the block at the given position, restoring its
     * original appearance.
     *
     * @param world the world the block is in
     * @param x     the block's x position
     * @param y     the block's y position
     * @param z     the block's z position
     * @return whether the block had any paint that was removed
     */
    boolean scrape(World world, int x, int y, int z);
}
