package com.tuxprogrammer.simplestorage.inventory;
// Ported from Tom's Simple Storage (MIT):
// https://github.com/tom5454/Toms-Storage/blob/95bb61d/NeoForge/src/platform-shared/java/com/tom/storagemod/block/entity/InventoryConnectorBlockEntity.java
// Avoids: STOR-05

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedHashSet;

/**
 * Flood-fills out from an inventory connector over trim blocks and interface
 * nodes, recording every block that carries an inventory.
 *
 * <p>
 * Positions are packed longs; the {@link World} supplies the neighbourhood
 * so this class stays independent of the packing scheme.
 *
 * <p>
 * Upstream: InventoryConnectorBlockEntity.detectTouchingInventories.
 *
 * <p>
 * Side: server logic, plain Java.
 */
public final class ConnectorScan {

    private ConnectorScan() {}

    /**
     * World view the scan walks over. All methods are pure queries on packed
     * long positions.
     */
    public interface World {

        /**
         * @param pos packed position
         * @return true if the block at pos is a trim (expandable) block
         */
        boolean isTrim(long pos);

        /**
         * @param pos packed position
         * @return true if the block at pos is an interface (expandable) node
         */
        boolean isInterface(long pos);

        /**
         * @param pos packed position
         * @return true if the block at pos carries an inventory
         */
        boolean hasInventory(long pos);

        /**
         * @param pos packed position
         * @return the adjacent positions
         */
        java.util.List<Long> adjacent(long pos);

        /**
         * @param a packed position
         * @param b packed position
         * @return squared distance between the two positions
         */
        int distanceSquared(long a, long b);
    }

    /**
     * Outcome of a scan.
     */
    public static final class Result {

        private final java.util.Set<Long> connected;
        private final java.util.Set<Long> interfaces;
        private final int visited;

        Result(java.util.Set<Long> connected, java.util.Set<Long> interfaces, int visited) {
            this.connected = connected;
            this.interfaces = interfaces;
            this.visited = visited;
        }

        /**
         * @return positions of inventory-bearing blocks, in discovery order
         */
        public java.util.Set<Long> connected() {
            return connected;
        }

        /**
         * @return positions of interface nodes, in discovery order
         */
        public java.util.Set<Long> interfaces() {
            return interfaces;
        }

        /**
         * @return number of positions checked, excluding the origin
         */
        public int visited() {
            return visited;
        }
    }

    /**
     * Scans outward from the origin's neighbours over trim and interface
     * blocks, recording inventory-bearing blocks within range.
     *
     * <p>
     * The origin itself is never recorded. Each position is checked at most
     * once. Range is clamped to 1..256.
     *
     * @param origin packed origin position (never recorded)
     * @param range  scan range (clamped to 1..256); blocks at distance squared
     *               >= range*range are excluded
     * @param world  world view to scan over
     * @return scan result
     */
    public static Result scan(long origin, int range, World world) {
        int r = Math.min(256, Math.max(1, range));
        long maxDistSq = (long) r * r;

        java.util.Set<Long> connected = new LinkedHashSet<Long>();
        java.util.Set<Long> interfaces = new LinkedHashSet<Long>();
        java.util.Set<Long> checked = new java.util.HashSet<Long>();
        checked.add(Long.valueOf(origin));

        Deque<Long> stack = new ArrayDeque<Long>();
        for (long neighbour : world.adjacent(origin)) {
            stack.push(Long.valueOf(neighbour));
        }

        while (!stack.isEmpty()) {
            long pos = stack.pop()
                .longValue();
            if (!checked.add(Long.valueOf(pos))) {
                continue;
            }
            if (world.distanceSquared(pos, origin) >= maxDistSq) {
                continue;
            }
            if (world.isTrim(pos)) {
                for (long neighbour : world.adjacent(pos)) {
                    stack.push(Long.valueOf(neighbour));
                }
            } else if (world.isInterface(pos)) {
                interfaces.add(Long.valueOf(pos));
                for (long neighbour : world.adjacent(pos)) {
                    stack.push(Long.valueOf(neighbour));
                }
            } else if (world.hasInventory(pos)) {
                connected.add(Long.valueOf(pos));
            }
        }

        return new Result(connected, interfaces, checked.size() - 1);
    }

    /**
     * Computes the rescan phase for a connector.
     *
     * @param positionHash hash of the connector position
     * @return rescan phase in [0, StaggerGate.CONNECTOR_INTERVAL)
     */
    public static int stagger(int positionHash) {
        return StaggerGate.phase(positionHash, StaggerGate.CONNECTOR_INTERVAL);
    }
}
