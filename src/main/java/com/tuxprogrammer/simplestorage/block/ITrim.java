package com.tuxprogrammer.simplestorage.block;

/**
 * Marker for trim blocks: solid filler blocks that the inventory cable floods
 * through when walking a network.
 *
 * <p>
 * A trim block behaves as an opaque, full-block piece of furniture. It has no
 * inventory of its own, but it lets a cable network pass through it so that
 * storage blocks on either side of a wall of trim can still be reached.
 *
 * <p>
 * Side: both (the client-only parts live in simplestorage.client).
 */
public interface ITrim {
}
