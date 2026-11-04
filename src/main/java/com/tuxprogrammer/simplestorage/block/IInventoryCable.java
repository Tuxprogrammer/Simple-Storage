package com.tuxprogrammer.simplestorage.block;

import net.minecraftforge.common.util.ForgeDirection;

/**
 * A block that conducts an inventory cable: a cable block positions itself at
 * some position in the world and, for each of its six faces, decides whether a
 * cable network may cross that face.
 *
 * <p>
 * The network walk uses {@link #canConnectFrom} to decide, for a neighbour
 * position, whether a connection arriving from that neighbour is accepted. The
 * connection is directional: the side is the face of <em>this</em> block the
 * connection arrives at, i.e. the direction from this block towards the
 * neighbour it comes from.
 *
 * <p>
 * Side: the method may be called from either side but should be cheap and
 * side-independent where possible.
 */
public interface IInventoryCable {

    /**
     * Whether this cable block accepts a connection arriving from the
     * neighbour on {@code side}.
     *
     * <p>
     * {@code side} is the direction from this block (at {@code x}, {@code y},
     * {@code z}) towards the neighbour the connection comes from. For example,
     * if a cable sits directly above this block, the connection from it
     * arrives on {@link ForgeDirection#UP}.
     *
     * @param world the world the cable is in
     * @param x     the block x position of this cable
     * @param y     the block y position of this cable
     * @param z     the block z position of this cable
     * @param side  the face of this block the connection arrives at (direction
     *              from this block to the connecting neighbour)
     * @return true if the connection from that neighbour is accepted
     */
    boolean canConnectFrom(net.minecraft.world.IBlockAccess world, int x, int y, int z, ForgeDirection side);
}
