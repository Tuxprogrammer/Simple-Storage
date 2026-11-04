package com.tuxprogrammer.simplestorage.terminal;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

/**
 * Tests for {@link AutoFillPlanner}, including the TERM-05 regression: with
 * two cells wanting the same item and only one unit available, exactly one
 * cell may get it.
 */
public class AutoFillPlannerTest {

    /**
     * TERM-05 regression: two cells want the same item, only one unit is
     * available; exactly one cell gets it and only one unit is reserved.
     */
    @Test
    public void doesNotReproduceTERM05_twoCellsWantingTheSameItemShareTheCount() {
        String[] wanted = new String[9];
        wanted[0] = "iron";
        wanted[4] = "iron";
        Map<String, Long> available = new HashMap<String, Long>();
        available.put("iron", 1L);

        AutoFillPlanner.Plan plan = AutoFillPlanner.plan(wanted, null, available, 1);

        int total = 0;
        for (int amount : plan.take) {
            total += amount;
        }
        assertEquals(1, total, "exactly one cell may pull the single available unit");
        assertEquals(1, countOnes(plan.take), "exactly one cell is assigned the unit");
        assertEquals(
            1L,
            plan.reserved.get("iron")
                .longValue(),
            "only one unit may be reserved");
    }

    /**
     * A cell whose current grid item equals its wanted item keeps it without
     * a pull, and the kept unit is still reserved (not double-spent).
     */
    @Test
    public void keepsExistingGridItemWithoutPull() {
        String[] wanted = new String[9];
        wanted[0] = "iron";
        wanted[1] = "gold";
        String[] current = new String[9];
        current[0] = "iron";
        Map<String, Long> available = new HashMap<String, Long>();
        available.put("iron", 5L);
        available.put("gold", 1L);

        AutoFillPlanner.Plan plan = AutoFillPlanner.plan(wanted, current, available, 1);

        assertArrayEquals(
            new int[] { 0, 1, 0, 0, 0, 0, 0, 0, 0 },
            plan.take,
            "the kept cell pulls nothing; the other cell pulls one");
        assertEquals(
            1L,
            plan.reserved.get("iron")
                .longValue(),
            "the kept unit counts as reserved");
        assertEquals(
            1L,
            plan.reserved.get("gold")
                .longValue(),
            "the pulled unit is reserved");
    }

    /**
     * A multi-craft plan pulls the per-cell amount for the requested number
     * of crafts and reserves that total.
     */
    @Test
    public void multiCraftAmounts() {
        String[] wanted = new String[9];
        wanted[0] = "iron";
        wanted[1] = "gold";
        Map<String, Long> available = new HashMap<String, Long>();
        available.put("iron", 10L);
        available.put("gold", 3L);

        AutoFillPlanner.Plan plan = AutoFillPlanner.plan(wanted, null, available, 2);

        assertArrayEquals(new int[] { 2, 2, 0, 0, 0, 0, 0, 0, 0 }, plan.take);
        assertEquals(
            2L,
            plan.reserved.get("iron")
                .longValue());
        assertEquals(
            2L,
            plan.reserved.get("gold")
                .longValue(),
            "only what is available is reserved");
    }

    /**
     * When the available count runs out, the earlier cell in index order gets
     * the unit and the later cell is left empty.
     */
    @Test
    public void shortageLeavesLaterCellsEmpty() {
        String[] wanted = new String[9];
        wanted[0] = "iron";
        wanted[8] = "iron";
        Map<String, Long> available = new HashMap<String, Long>();
        available.put("iron", 1L);

        AutoFillPlanner.Plan plan = AutoFillPlanner.plan(wanted, null, available, 1);

        assertArrayEquals(new int[] { 1, 0, 0, 0, 0, 0, 0, 0, 0 }, plan.take);
    }

    /**
     * Null and short input arrays are treated as empty cells; longer arrays
     * have their extra cells ignored, and null available counts as 0.
     */
    @Test
    public void nullAndShortArrays() {
        AutoFillPlanner.Plan empty = AutoFillPlanner.plan(null, null, null, 1);
        assertArrayEquals(new int[9], empty.take);
        assertEquals(0, empty.reserved.size());

        String[] shortWanted = new String[] { "iron" };
        Map<String, Long> available = new HashMap<String, Long>();
        available.put("iron", null);
        AutoFillPlanner.Plan shortPlan = AutoFillPlanner.plan(shortWanted, null, available, 1);
        assertArrayEquals(new int[9], shortPlan.take, "a null count means nothing is available");

        String[] longWanted = new String[] { "iron", "gold", "gold", "gold", "gold", "gold", "gold", "gold", "gold",
            "gold" };
        Map<String, Long> enough = new HashMap<String, Long>();
        enough.put("iron", 1L);
        enough.put("gold", 1L);
        AutoFillPlanner.Plan longPlan = AutoFillPlanner.plan(longWanted, null, enough, 1);
        assertArrayEquals(new int[] { 1, 1, 0, 0, 0, 0, 0, 0, 0 }, longPlan.take, "cells beyond index 8 are ignored");
        assertEquals(
            2,
            Arrays.asList("iron", "gold")
                .size(),
            "sanity: two item types");
    }

    /**
     * Counts how many grid cells are assigned one unit.
     */
    private static int countOnes(int[] take) {
        int count = 0;
        for (int amount : take) {
            if (amount == 1) {
                count++;
            }
        }
        return count;
    }
}
