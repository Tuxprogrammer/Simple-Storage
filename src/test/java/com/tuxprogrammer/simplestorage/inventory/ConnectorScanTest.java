package com.tuxprogrammer.simplestorage.inventory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

/**
 * Tests for {@link ConnectorScan} over a 1-D line of cells (position = x as a
 * long, adjacent = x-1 and x+1, distanceSquared = (a-b)^2).
 */
public class ConnectorScanTest {

    /**
     * In-test world over a 1-D line of cells.
     */
    private static final class LineWorld implements ConnectorScan.World {

        private final Set<Long> trims = new HashSet<Long>();
        private final Set<Long> interfaces = new HashSet<Long>();
        private final Set<Long> inventories = new HashSet<Long>();

        void trim(long x) {
            trims.add(Long.valueOf(x));
        }

        void anInterface(long x) {
            interfaces.add(Long.valueOf(x));
        }

        void inventory(long x) {
            inventories.add(Long.valueOf(x));
        }

        @Override
        public boolean isTrim(long pos) {
            return trims.contains(Long.valueOf(pos));
        }

        @Override
        public boolean isInterface(long pos) {
            return interfaces.contains(Long.valueOf(pos));
        }

        @Override
        public boolean hasInventory(long pos) {
            return inventories.contains(Long.valueOf(pos));
        }

        @Override
        public List<Long> adjacent(long pos) {
            List<Long> neighbours = new ArrayList<Long>(2);
            neighbours.add(Long.valueOf(pos - 1));
            neighbours.add(Long.valueOf(pos + 1));
            return neighbours;
        }

        @Override
        public int distanceSquared(long a, long b) {
            long d = a - b;
            return (int) (d * d);
        }
    }

    @Test
    public void doesNotReproduceSTOR05_minValueHashStillGetsAPhase() {
        int phase = ConnectorScan.stagger(Integer.MIN_VALUE);
        assertTrue(phase >= 0 && phase < 20, "phase " + phase + " not in [0,20)");
    }

    @Test
    public void findsInventoriesAcrossTrims() {
        LineWorld world = new LineWorld();
        world.trim(1);
        world.trim(2);
        world.trim(3);
        world.inventory(4);

        ConnectorScan.Result result = ConnectorScan.scan(0, 10, world);

        assertEquals(
            1,
            result.connected()
                .size());
        assertTrue(
            result.connected()
                .contains(4L));
    }

    @Test
    public void interfacesAreRecordedAndExpanded() {
        LineWorld world = new LineWorld();
        world.anInterface(1);
        world.inventory(2);

        ConnectorScan.Result result = ConnectorScan.scan(0, 10, world);

        assertTrue(
            result.interfaces()
                .contains(1L));
        assertTrue(
            result.connected()
                .contains(2L));
    }

    @Test
    public void inventoriesDoNotExpand() {
        LineWorld world = new LineWorld();
        world.inventory(1);
        world.inventory(2);

        ConnectorScan.Result result = ConnectorScan.scan(0, 10, world);

        assertTrue(
            result.connected()
                .contains(1L));
        assertFalse(
            result.connected()
                .contains(2L));
        assertEquals(
            1,
            result.connected()
                .size());
    }

    @Test
    public void rangeBoundIsExclusive() {
        // Inventory at distance exactly equal to range is not found.
        LineWorld world = new LineWorld();
        world.trim(1);
        world.trim(2);
        world.trim(3);
        world.inventory(4);

        ConnectorScan.Result atBound = ConnectorScan.scan(0, 4, world);
        assertFalse(
            atBound.connected()
                .contains(4L),
            "inventory at distance range should be excluded");

        // Inventory at distance range-1 is found.
        LineWorld world2 = new LineWorld();
        world2.trim(1);
        world2.trim(2);
        world2.inventory(3);

        ConnectorScan.Result justInside = ConnectorScan.scan(0, 4, world2);
        assertTrue(
            justInside.connected()
                .contains(3L),
            "inventory at distance range-1 should be found");
    }

    @Test
    public void originIsNeverRecorded() {
        LineWorld world = new LineWorld();
        world.inventory(0);
        world.trim(1);
        world.inventory(2);

        ConnectorScan.Result result = ConnectorScan.scan(0, 10, world);

        assertFalse(
            result.connected()
                .contains(0L));
        assertTrue(
            result.connected()
                .contains(2L));
    }

    @Test
    public void eachPositionIsCheckedOnce() {
        LineWorld world = new LineWorld();
        // Trims at x=1..5 and the origin at 0; the examined positions are
        // x=-1 (the origin's other neighbour), x=1..5 and x=6 = 7 positions
        // (the origin itself is excluded).
        world.trim(1);
        world.trim(2);
        world.trim(3);
        world.trim(4);
        world.trim(5);

        ConnectorScan.Result result = ConnectorScan.scan(0, 10, world);

        assertEquals(7, result.visited());
    }

    @Test
    public void rangeIsClamped() {
        LineWorld world = new LineWorld();
        // Trim line of 300 cells from x=1 to x=300; inventory at x=301.
        for (long x = 1; x <= 300; x++) {
            world.trim(x);
        }
        world.inventory(301);

        // Range 0 clamps to 1: only x=1 is within (distance 1 is not < 1? no:
        // 1 >= 1*1, so nothing is found).
        ConnectorScan.Result asOne = ConnectorScan.scan(0, 0, world);
        assertEquals(
            0,
            asOne.connected()
                .size());
        // Both neighbours x=-1 and x=1 are examined but out of range.
        assertEquals(2, asOne.visited());

        // Range 1000 clamps to 256: x=301 is beyond, so nothing is found.
        ConnectorScan.Result as256 = ConnectorScan.scan(0, 1000, world);
        assertEquals(
            0,
            as256.connected()
                .size());
        // Trims x=1..255 are within range (distance squared < 65536), x=256
        // is examined but out of range, and x=-1 is examined: 255 + 1 + 1 = 257.
        assertEquals(257, as256.visited());
    }
}
