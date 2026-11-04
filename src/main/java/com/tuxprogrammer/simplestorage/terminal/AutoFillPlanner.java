package com.tuxprogrammer.simplestorage.terminal;
// Ported from Tom's Simple Storage (MIT):
// https://github.com/tom5454/Toms-Storage/blob/95bb61d/NeoForge/src/platform-shared/java/com/tom/storagemod/menu/TerminalCraftingFiller.java
// Avoids: TERM-05

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Plans how the crafting terminal's auto-fill pulls items from the network
 * into the 3x3 crafting grid for a recipe (the original spec's
 * {@code TerminalCraftingFiller.placeRecipe} pull accounting).
 *
 * <p>
 * The plan covers the nine grid cells in row-major index order. A cell
 * whose wanted item already sits in the same position of the current grid
 * keeps its existing item without a pull (the grid item is counted in its
 * reservation so nothing is double-spent). Every other cell pulls what it
 * needs from the network: for a single craft that is 0 or 1 per cell, for a
 * multi-craft it is the per-cell amount for the requested number of crafts.
 * Cells that cannot be satisfied (the item is unavailable or the available
 * count is exhausted) pull nothing.
 * </p>
 *
 * <p>
 * <b>TERM-05 fix:</b> item accounting is per CELL, not per item type. The
 * upstream filler reserved player-inventory stacks once (per item holder) but
 * pulled lazily per grid cell, so two cells wanting the same scarce item
 * could both claim the same unit. This planner iterates the cells in index
 * order against a working copy of {@code available} that is decremented as
 * each cell is assigned, so with two cells wanting the same item and only
 * one unit available, exactly one cell gets it.
 * </p>
 *
 * <p>
 * The planner never throws: null {@code wanted}/{@code current} arrays are
 * treated as empty, cells beyond index 8 (the 9 grid cells) are ignored, and
 * missing entries are treated as empty cells. Null {@code available} counts
 * are treated as unavailable, and negative counts as 0.
 * </p>
 *
 * <p>
 * Side: both (plain Java).
 * </p>
 */
public final class AutoFillPlanner {

    /** Number of cells in the 3x3 crafting grid. */
    private static final int GRID_CELLS = 9;

    private AutoFillPlanner() {}

    /**
     * Plans the pulls that fill the 3x3 crafting grid for a recipe.
     *
     * @param wanted    the 9 recipe grid cells (null = empty cell; each
     *                  non-null entry is an item key); null or entries beyond
     *                  the first 9 are handled as documented on the class
     * @param current   the current grid contents, same shape as {@code wanted}
     * @param available network item counts by item key (null entries treated
     *                  as unavailable)
     * @param crafts    how many crafts to plan for; each non-empty cell needs
     *                  this many units (values &lt;= 0 behave like 1)
     * @return the plan: per-cell pull amounts and the total reserved per
     *         item type
     */
    public static Plan plan(String[] wanted, String[] current, Map<String, Long> available, int crafts) {
        int needed = crafts <= 0 ? 1 : crafts;
        int[] take = new int[GRID_CELLS];

        Map<String, Long> working = new LinkedHashMap<String, Long>();
        if (available != null) {
            for (Map.Entry<String, Long> entry : available.entrySet()) {
                long count = entry.getValue() == null ? 0L : entry.getValue();
                working.put(entry.getKey(), Math.max(0L, count));
            }
        }

        Map<String, Long> reserved = new LinkedHashMap<String, Long>();

        for (int i = 0; i < GRID_CELLS; i++) {
            String want = cell(wanted, i);
            if (want == null) {
                continue;
            }
            String have = cell(current, i);
            if (have != null && have.equals(want)) {
                // The grid cell already holds the wanted item: it is kept in
                // place (no pull), and counted as reserved so the remaining
                // available units are not spent on it twice.
                long currentCount = countOf(working, want);
                working.put(want, Math.max(0L, currentCount - needed));
                reserved.put(want, Long.valueOf(needed));
                continue;
            }
            long currentCount = countOf(working, want);
            long pull = Math.min(needed, currentCount);
            if (pull > 0L) {
                take[i] = (int) pull;
                working.put(want, currentCount - pull);
                reserved.put(want, pull);
            }
        }

        return new Plan(take, reserved);
    }

    /**
     * Reads a grid cell, treating null arrays and out-of-range indices as
     * empty.
     */
    private static String cell(String[] grid, int index) {
        return (grid != null && index < grid.length) ? grid[index] : null;
    }

    /**
     * Reads a working-count entry, treating absent keys as 0.
     */
    private static long countOf(Map<String, Long> working, String key) {
        Long count = working.get(key);
        return count == null ? 0L : count;
    }

    /**
     * The result of planning an auto-fill: the per-cell pull amounts and the
     * total amount reserved per item type.
     */
    public static final class Plan {

        /**
         * How many of each cell's wanted item to pull for that cell, in
         * row-major grid order. Empty cells and cells that are kept from the
         * current grid are 0; cells that could not be satisfied are 0. For a
         * single craft each entry is 0 or 1; for a multi-craft it is the
         * per-cell amount for {@code crafts} crafts.
         */
        public final int[] take;

        /**
         * Total amount reserved per item type: the pulled units plus the
         * units kept in place from the current grid.
         */
        public final Map<String, Long> reserved;

        /**
         * Creates a plan.
         *
         * @param take     per-cell pull amounts (9 entries)
         * @param reserved reserved units per item type
         */
        public Plan(int[] take, Map<String, Long> reserved) {
            this.take = take;
            this.reserved = reserved;
        }
    }
}
