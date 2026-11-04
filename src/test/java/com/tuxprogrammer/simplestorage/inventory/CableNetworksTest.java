package com.tuxprogrammer.simplestorage.inventory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link CableNetworks}: one {@link CableGraph} per dimension,
 * created on first use and dropped on unload.
 */
public class CableNetworksTest {

    @Test
    public void forLevelReturnsSameInstancePerDimension() {
        CableNetworks networks = new CableNetworks();
        CableGraph first = networks.forLevel(0);
        assertSame(first, networks.forLevel(0));
        // A different dimension gets its own graph.
        CableGraph other = networks.forLevel(1);
        assertNotSame(first, other);
        assertEquals(2, networks.levelCount());
    }

    @Test
    public void unloadRemovesTheLevel() {
        CableNetworks networks = new CableNetworks();
        CableGraph graph = networks.forLevel(0);
        assertEquals(1, networks.levelCount());
        networks.unload(0);
        assertEquals(0, networks.levelCount());
        // A new graph is created after the unload; the old one is gone.
        assertNotSame(graph, networks.forLevel(0));
        // Unloading an unknown dimension is a no-op.
        networks.unload(99);
        assertEquals(1, networks.levelCount());
    }
}
