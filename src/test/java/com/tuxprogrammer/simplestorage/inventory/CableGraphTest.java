package com.tuxprogrammer.simplestorage.inventory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link CableGraph} using a one-dimensional line topology:
 * positions are packed as their {@code x} coordinate, and each position has at
 * most the neighbours {@code x-1} and {@code x+1}.
 */
public class CableGraphTest {

    /**
     * A one-dimensional {@link CableGraph.Topology} over sets of cable,
     * functional and unloaded positions. Positions are packed as their {@code x}
     * coordinate and neighbours are {@code x-1} and {@code x+1}.
     */
    private static final class LineTopology implements CableGraph.Topology {

        private final Set<Long> cables;
        private final Set<Long> functional;
        private final Set<Long> unloaded;

        LineTopology(Set<Long> cables, Set<Long> functional, Set<Long> unloaded) {
            this.cables = cables;
            this.functional = functional;
            this.unloaded = unloaded;
        }

        @Override
        public boolean isLoaded(long pos) {
            return !unloaded.contains(pos);
        }

        @Override
        public boolean isCable(long pos) {
            return cables.contains(pos);
        }

        @Override
        public boolean isFunctional(long pos) {
            return functional.contains(pos);
        }

        @Override
        public List<Long> adjacent(long pos) {
            List<Long> neighbours = new ArrayList<Long>(2);
            neighbours.add(pos - 1L);
            neighbours.add(pos + 1L);
            return neighbours;
        }

        @Override
        public boolean connects(long from, long to) {
            // On a line the only possible neighbours are from-1 and from+1.
            return to == from - 1L || to == from + 1L;
        }
    }

    private static Set<Long> set(long... positions) {
        Set<Long> result = new HashSet<Long>();
        for (long position : positions) {
            result.add(position);
        }
        return result;
    }

    @Test
    public void doesNotReproduceSTOR03_connectorWithNoAttachedInventoriesIsNotAUsableNetwork() {
        // STOR-03: upstream returned !isRemoved() alone, so a connector with
        // zero attached inventories counted as a usable network.
        assertFalse(CableGraph.hasConnectedInventories(false, 0));
        assertTrue(CableGraph.hasConnectedInventories(false, 1));
        assertFalse(CableGraph.hasConnectedInventories(true, 3));
    }

    @Test
    public void walkFindsFunctionalNodesAcrossCables() {
        CableGraph graph = new CableGraph();
        // 1(F) - 2(cable) - 3(cable) - 4(F)
        LineTopology world = new LineTopology(set(2L, 3L), set(1L, 4L), set());
        Set<Long> attached = graph.scan(2L, world);
        assertEquals(set(1L, 4L), attached);
    }

    @Test
    public void unloadedPositionStopsTheWalk() {
        CableGraph graph = new CableGraph();
        // 1(F) - 2(cable) - 3(unloaded cable) - 4(cable) - 5(F)
        LineTopology world = new LineTopology(set(2L, 3L, 4L), set(1L, 5L), set(3L));
        Set<Long> attached = graph.scan(2L, world);
        // The unloaded cable at 3 stops the walk, so 5 is not reached.
        assertEquals(set(1L), attached);
    }

    @Test
    public void functionalNodesDoNotExpand() {
        CableGraph graph = new CableGraph();
        // 2(cable) - 3(F) - 4(F): two functional nodes adjacent.
        LineTopology world = new LineTopology(set(2L), set(3L, 4L), set());
        Set<Long> attached = graph.scan(2L, world);
        // The walk from cable 2 reaches 3 but does not expand it, so 4 is
        // never seen.
        assertEquals(set(3L), attached);
    }

    @Test
    public void cacheIsSharedByAllCablesAndReused() {
        CableGraph graph = new CableGraph();
        // 1(F) - 2(cable) - 3(cable) - 4(F)
        LineTopology world = new LineTopology(set(2L, 3L), set(1L, 4L), set());
        Set<Long> first = graph.scan(2L, world);
        // One shared cache is stored for every cable in the walked network.
        assertEquals(2, graph.cachedCableCount());
        // A second scan from the other cable is served from the shared cache
        // and returns the same attached set without re-walking.
        Set<Long> second = graph.scan(3L, world);
        assertEquals(first, second);
        assertEquals(set(1L, 4L), second);
        assertEquals(2, graph.cachedCableCount());
    }

    @Test
    public void markNodeInvalidDropsTheWholeNetwork() {
        CableGraph graph = new CableGraph();
        // 1(F) - 2(cable) - 3(cable) - 4(F)
        LineTopology world = new LineTopology(set(2L, 3L), set(1L, 4L), set());
        graph.scan(2L, world);
        assertEquals(2, graph.cachedCableCount());
        // Invalidating one cable drops the entries of the whole network.
        graph.markNodeInvalid(2L);
        assertEquals(0, graph.cachedCableCount());
        graph.markNodeInvalid(3L);
        assertEquals(0, graph.cachedCableCount());
    }

    @Test
    public void scanFromAFunctionalStartExpandsIt() {
        CableGraph graph = new CableGraph();
        // 1(F, start) - 2(cable) - 3(cable) - 4(F)
        LineTopology world = new LineTopology(set(2L, 3L), set(1L, 4L), set());
        Set<Long> attached = graph.scan(1L, world);
        // The functional start is expanded (not collected), so the walk
        // reaches 4 but not 1.
        assertEquals(set(4L), attached);
    }

    @Test
    public void connectsFalseBlocksTraversal() {
        CableGraph graph = new CableGraph();
        // 1(F) - 2(cable) - 3(cable) - 4(F); 2 does not accept a connection
        // coming from 3, so the walk from 3 cannot cross 2 to reach 1.
        final Set<Long> blockedFrom = Collections.unmodifiableSet(set(3L));
        CableGraph.Topology world = new CableGraph.Topology() {

            @Override
            public boolean isLoaded(long pos) {
                return true;
            }

            @Override
            public boolean isCable(long pos) {
                return pos == 2L || pos == 3L;
            }

            @Override
            public boolean isFunctional(long pos) {
                return pos == 1L || pos == 4L;
            }

            @Override
            public List<Long> adjacent(long pos) {
                List<Long> neighbours = new ArrayList<Long>(2);
                neighbours.add(pos - 1L);
                neighbours.add(pos + 1L);
                return neighbours;
            }

            @Override
            public boolean connects(long from, long to) {
                boolean onLine = to == from - 1L || to == from + 1L;
                boolean blocked = blockedFrom.contains(from) && to == 2L;
                return onLine && !blocked;
            }
        };
        Set<Long> attached = graph.scan(3L, world);
        assertEquals(set(4L), attached);
    }

    /**
     * A network built from cables and several functional nodes answers the
     * same contents from every attached face (milestone M02 exit gate).
     */
    @Test
    public void networkAnswersTheSameFromEveryAttachedNode() {
        // 1(F) - 2(cable) - 3(cable) - 4(F)
        final LineTopology world = new LineTopology(set(2L, 3L), set(1L, 4L), set());
        // Each cable start answers from a fresh graph, so the cache cannot
        // influence the result.
        assertEquals(set(1L, 4L), new CableGraph().scan(2L, world));
        assertEquals(set(1L, 4L), new CableGraph().scan(3L, world));
        // One shared graph instance answers the same contents from every
        // attached face.
        CableGraph graph = new CableGraph();
        assertEquals(set(1L, 4L), graph.scan(2L, world));
        assertEquals(set(1L, 4L), graph.scan(3L, world));
        // Scanning from a functional node expands it, so the start is
        // excluded from its own answer.
        assertEquals(set(4L), new CableGraph().scan(1L, world));
        assertEquals(set(1L), new CableGraph().scan(4L, world));
    }
}
