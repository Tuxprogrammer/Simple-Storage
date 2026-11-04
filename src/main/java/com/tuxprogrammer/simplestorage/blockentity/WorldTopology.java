package com.tuxprogrammer.simplestorage.blockentity;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import com.tuxprogrammer.simplestorage.block.IInventoryCable;
import com.tuxprogrammer.simplestorage.inventory.CableGraph;

/**
 * Adapts a live {@link World} to the {@link CableGraph.Topology} the network
 * scan walks over.
 *
 * <p>
 * Positions are packed {@code long} values: 26 bits of x (sign-extended),
 * 12 bits of y (0..4095), and 26 bits of z (sign-extended). The pack/unpack
 * helpers here define that scheme; the {@code Topology} interface only
 * promises positions are opaque {@code long}s.
 *
 * <p>
 * Side: server (reads the world directly).
 */
public final class WorldTopology implements CableGraph.Topology {

    /** x bits: 26 (range -33554432..33554431). */
    private static final int X_BITS = 26;
    /** z bits: 26 (range -33554432..33554431). */
    private static final int Z_BITS = 26;
    /** y bits: 12 (range 0..4095). */
    private static final int Y_BITS = 12;

    /** x mask: 26 bits. */
    private static final long X_MASK = (1L << X_BITS) - 1;
    /** z mask: 26 bits. */
    private static final long Z_MASK = (1L << Z_BITS) - 1;
    /** y mask: 12 bits. */
    private static final long Y_MASK = (1L << Y_BITS) - 1;

    /** Sign bit of x (bit 25). */
    private static final long X_SIGN = 1L << (X_BITS - 1);
    /** Sign bit of z (bit 25). */
    private static final long Z_SIGN = 1L << (Z_BITS - 1);

    private final World world;

    /**
     * The six neighbour offsets (dx, dy, dz).
     */
    private static final int[][] NEIGHBOURS = new int[][] { { 1, 0, 0 }, { -1, 0, 0 }, { 0, 1, 0 }, { 0, -1, 0 },
        { 0, 0, 1 }, { 0, 0, -1 }, };

    public WorldTopology(World world) {
        this.world = world;
    }

    /**
     * Packs a block position into a {@code long}.
     *
     * <p>
     * Layout: bits 64..38 are x (26 bits), bits 37..12 are z (26 bits), bits
     * 11..0 are y (12 bits, 0..4095).
     *
     * @param x the block x position
     * @param y the block y position (0..4095)
     * @param z the block z position
     * @return the packed position
     */
    public static long pack(int x, int y, int z) {
        return ((long) (x & 0x3FFFFFF) << 38) | ((long) (z & 0x3FFFFFF) << 12) | (y & 0xFFF);
    }

    /**
     * The x coordinate of a packed position (sign-extended from 26 bits).
     *
     * @param pos the packed position
     * @return the x coordinate
     */
    public static int unpackX(long pos) {
        int x = (int) ((pos >> 38) & X_MASK);
        if ((x & (1 << (X_BITS - 1))) != 0) {
            x -= (1 << X_BITS);
        }
        return x;
    }

    /**
     * The y coordinate of a packed position (0..4095).
     *
     * @param pos the packed position
     * @return the y coordinate
     */
    public static int unpackY(long pos) {
        return (int) (pos & Y_MASK);
    }

    /**
     * The z coordinate of a packed position (sign-extended from 26 bits).
     *
     * @param pos the packed position
     * @return the z coordinate
     */
    public static int unpackZ(long pos) {
        int z = (int) ((pos >> 12) & Z_MASK);
        if ((z & (1 << (Z_BITS - 1))) != 0) {
            z -= (1 << Z_BITS);
        }
        return z;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isLoaded(long pos) {
        return world.blockExists(unpackX(pos), unpackY(pos), unpackZ(pos));
    }

    /** {@inheritDoc} */
    @Override
    public boolean isCable(long pos) {
        return world.getBlock(unpackX(pos), unpackY(pos), unpackZ(pos)) instanceof IInventoryCable;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isFunctional(long pos) {
        return world.getTileEntity(unpackX(pos), unpackY(pos), unpackZ(pos)) instanceof InventoryNodeTile;
    }

    /** {@inheritDoc} */
    @Override
    public List<Long> adjacent(long pos) {
        int x = unpackX(pos);
        int y = unpackY(pos);
        int z = unpackZ(pos);
        List<Long> neighbours = new ArrayList<Long>(6);
        for (int[] offset : NEIGHBOURS) {
            int ny = y + offset[1];
            // Clamp y to the world build height so a neighbour just outside
            // the world is still a valid (loaded-checkable) position.
            if (ny < 0) {
                ny = 0;
            } else if (ny > 255) {
                ny = 255;
            }
            neighbours.add(pack(x + offset[0], ny, z + offset[2]));
        }
        return neighbours;
    }

    /**
     * Whether the block at {@code to} accepts a connection coming from
     * {@code from}.
     *
     * <p>
     * True when the block at {@code to} is an {@link IInventoryCable} whose
     * {@link IInventoryCable#canConnectFrom} is true for the face facing
     * {@code from}, OR the block at {@code to} has an
     * {@link InventoryNodeTile} tile entity (functional nodes accept a
     * connection from any cable neighbour).
     *
     * @param from packed block position the connection comes from
     * @param to   packed block position the connection arrives at
     * @return true if the connection is accepted
     */
    @Override
    public boolean connects(long from, long to) {
        int tx = unpackX(to);
        int ty = unpackY(to);
        int tz = unpackZ(to);
        int fx = unpackX(from);
        int fy = unpackY(from);
        int fz = unpackZ(from);

        // ForgeDirection from `to` towards `from`.
        ForgeDirection side = null;
        for (ForgeDirection d : ForgeDirection.VALID_DIRECTIONS) {
            if (d.offsetX == fx - tx && d.offsetY == fy - ty && d.offsetZ == fz - tz) {
                side = d;
                break;
            }
        }
        if (side == null) {
            return false;
        }

        if (world.getBlock(tx, ty, tz) instanceof IInventoryCable) {
            return ((IInventoryCable) world.getBlock(tx, ty, tz)).canConnectFrom(world, tx, ty, tz, side);
        }
        return world.getTileEntity(tx, ty, tz) instanceof InventoryNodeTile;
    }
}
