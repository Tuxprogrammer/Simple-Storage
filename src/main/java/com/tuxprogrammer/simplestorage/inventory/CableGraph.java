package com.tuxprogrammer.simplestorage.inventory;
// Ported from Tom's Simple Storage (MIT):
// https://github.com/tom5454/Toms-Storage/blob/95bb61d/NeoForge/src/platform-shared/java/com/tom/storagemod/inventory/InventoryCableNetwork.java
// Avoids: STOR-03

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Scans the cable network reachable from a start position and collects its
 * functional nodes (connectors, proxies, interfaces).
 *
 * <p>
 * The scan walks cable positions with a LIFO stack. Functional positions
 * are collected but never expanded (except the start position itself, which is
 * always expanded). Unloaded positions stop the walk. Once a cached cable is
 * met, its cache's sets are merged instead of re-walking that branch. After
 * each walk a single shared {@link CableCache} is stored for every cable in
 * the walked network; functional nodes are never cached so they always
 * re-scan.
 *
 * <p>
 * Positions are packed {@code long} values; the concrete packing scheme is
 * left to the {@link Topology} implementation.
 *
 * <p>
 * Side: server logic, plain Java.
 */
public class CableGraph {

    /**
     * Read-only view of the world the scan walks over.
     */
    public interface Topology {

        /**
         * Whether the position's chunk is currently loaded. An unloaded
         * position stops the walk.
         *
         * @param pos packed block position
         * @return true if loaded
         */
        boolean isLoaded(long pos);

        /**
         * Whether the block at the position is a cable.
         *
         * @param pos packed block position
         * @return true if the position is a cable
         */
        boolean isCable(long pos);

        /**
         * Whether the block at the position is a functional node
         * (connector, proxy, or interface).
         *
         * @param pos packed block position
         * @return true if the position is functional
         */
        boolean isFunctional(long pos);

        /**
         * The up-to-6 neighbours of a position.
         *
         * @param pos packed block position
         * @return the neighbour positions
         */
        List<Long> adjacent(long pos);

        /**
         * Whether the block at {@code to} accepts a connection coming from
         * {@code from} (upstream canConnectFrom).
         *
         * @param from packed block position the connection comes from
         * @param to   packed block position the connection arrives at
         * @return true if the connection is accepted
         */
        boolean connects(long from, long to);
    }

    /**
     * Pushes the connecting neighbours of an expanded position onto the
     * stack. Neighbours that are cables (or functional cables) are recorded
     * in the walk's {@code cables} set immediately so that the cache-merge
     * branch can see them when they are popped; non-cable functional
     * neighbours are collected into {@code attached} and are not expanded;
     * anything else is pushed for the walk to examine.
     */
    private static void pushNeighbours(long pos, Topology world, Set<Long> visited, Set<Long> cables,
        Set<Long> attached, Deque<Long> stack) {
        for (Long neighbour : world.adjacent(pos)) {
            if (!world.connects(pos, neighbour)) {
                continue;
            }
            if (visited.contains(neighbour)) {
                continue;
            }
            if (world.isCable(neighbour)) {
                cables.add(neighbour);
            } else if (world.isFunctional(neighbour)) {
                attached.add(neighbour);
            } else {
                continue;
            }
            if (visited.add(neighbour)) {
                stack.push(neighbour);
            }
        }
    }

    /**
     * The cached result of one scan: the cables of the walked network and the
     * functional nodes attached to it. Shared by every cable in the network.
     */
    private static final class CableCache {

        private final Set<Long> cables;
        private final Set<Long> attached;

        CableCache(Set<Long> cables, Set<Long> attached) {
            this.cables = cables;
            this.attached = attached;
        }
    }

    private final Map<Long, CableCache> cache = new HashMap<Long, CableCache>();

    /**
     * Finds all functional nodes reachable from {@code start} through cables.
     *
     * <p>
     * If {@code start} is itself a cached cable, a copy of that cache's
     * attached set is returned without walking. Otherwise a LIFO stack walk
     * expands cables (and always expands the start position), collects
     * functional nodes into {@code attached}, and merges the sets of any
     * already-cached cable it meets. After the walk, one shared
     * {@link CableCache} is stored for every cable in the walked network.
     *
     * @param start packed block position to start from
     * @param world the world topology to walk
     * @return an unmodifiable copy of the attached functional positions
     */
    public Set<Long> scan(long start, Topology world) {
        CableCache existing = cache.get(start);
        if (existing != null) {
            return Collections.unmodifiableSet(new HashSet<Long>(existing.attached));
        }

        Set<Long> visited = new HashSet<Long>();
        Set<Long> cables = new HashSet<Long>();
        Set<Long> attached = new HashSet<Long>();
        Deque<Long> stack = new ArrayDeque<Long>();

        visited.add(start);
        stack.push(start);
        while (!stack.isEmpty()) {
            long pos = stack.pop();
            // Pop-time check only: an unloaded position stops the walk there.
            // visited only ever grows (pushNeighbours never removes), so
            // duplicates are never pushed and no removal is needed here.
            if (!world.isLoaded(pos)) {
                continue;
            }
            if (world.isCable(pos)) {
                // Cable: record it (the start, if a cable, lands here), then
                // either merge an existing cache or push its connecting
                // neighbours.
                cables.add(pos);
                CableCache merged = cache.get(pos);
                if (merged != null) {
                    cables.addAll(merged.cables);
                    attached.addAll(merged.attached);
                } else {
                    pushNeighbours(pos, world, visited, cables, attached, stack);
                }
            } else if (pos == start) {
                // The start position is always expanded, whether or not it is
                // functional. A functional position reached through a cable is
                // collected but never expanded (it was already added to
                // attached by pushNeighbours when it was pushed).
                pushNeighbours(pos, world, visited, cables, attached, stack);
            }
        }

        CableCache result = new CableCache(cables, attached);
        for (Long cable : cables) {
            cache.put(cable, result);
        }
        return Collections.unmodifiableSet(new HashSet<Long>(attached));
    }

    /**
     * Drops the cached entry for {@code pos} and, through it, the entry of
     * every cable in that cached network (whole network invalidated). An
     * unknown position is a no-op.
     *
     * @param pos packed cable position to invalidate
     */
    public void markNodeInvalid(long pos) {
        CableCache cached = cache.get(pos);
        if (cached == null) {
            return;
        }
        Set<Long> toDrop = new HashSet<Long>(cached.cables);
        for (Long cable : toDrop) {
            cache.remove(cable);
        }
    }

    /**
     * The number of cached cable positions.
     *
     * @return the cached cable count
     */
    public int cachedCableCount() {
        return cache.size();
    }

    /**
     * Drops every cached network.
     */
    public void clear() {
        cache.clear();
    }

    /**
     * Whether a connector is attached to a usable network.
     *
     * <p>
     * STOR-03: upstream returned {@code !isRemoved()} alone, so an
     * interface treated a connector with zero attached inventories as a
     * usable network.
     *
     * @param removed             whether the connector has been removed
     * @param attachedInventories the number of inventories attached to the network
     * @return true if the connector is present and has at least one attached inventory
     */
    public static boolean hasConnectedInventories(boolean removed, int attachedInventories) {
        return !removed && attachedInventories > 0;
    }
}
