package com.tuxprogrammer.simplestorage.terminal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.tuxprogrammer.simplestorage.sort.SortKey;
import com.tuxprogrammer.simplestorage.sort.SortingTypes;

/**
 * Tests for {@link TerminalView}, the client's pure item-list model: the
 * sort orders, the search filter, the frozen (ghost) mode, the row/page
 * math, and the TERM-01 regression (visible rows are a parameter, so a
 * stale offset clamps with the given visible rows instead of dividing by
 * the upstream hard-coded 5).
 *
 * Side: client logic, plain Java.
 */
public class TerminalViewTest {

    /**
     * In-test item type carrying the fields the view sorts and searches by.
     */
    private static final class Row implements TerminalView.Source<Row> {

        final String name;
        final String mod;
        final long amount;
        final List<String> tooltip;

        private Row(String name, String mod, long amount, List<String> tooltip) {
            this.name = name;
            this.mod = mod;
            this.amount = amount;
            this.tooltip = tooltip == null ? new ArrayList<String>() : tooltip;
        }

        /**
         * Stable id for the row.
         */
        private int id() {
            return name.hashCode();
        }

        @Override
        public SortKey sortKey(Row item) {
            return new SortKey(item.amount, item.name, item.mod, item.id(), 64, item.mod + ":" + item.name);
        }

        @Override
        public SearchQuery.Item searchItem(Row item) {
            return new SearchQuery.Item() {

                @Override
                public String displayName() {
                    return item.name;
                }

                @Override
                public String modId() {
                    return item.mod + ":" + item.name;
                }

                @Override
                public List<String> tooltipLines() {
                    return item.tooltip;
                }

                @Override
                public List<String> oreNames() {
                    return new ArrayList<String>();
                }

                @Override
                public String nbtText() {
                    return null;
                }
            };
        }

        @Override
        public String keyOf(Row item) {
            return item.mod + ":" + item.name;
        }
    }

    /**
     * The four fixed rows, mod "m": apple(5), Banana(10), cherry(10),
     * Zebra(1).
     */
    private static List<Row> baseItems() {
        List<Row> items = new ArrayList<Row>();
        items.add(new Row("apple", "m", 5, null));
        items.add(new Row("Banana", "m", 10, null));
        items.add(new Row("cherry", "m", 10, null));
        items.add(new Row("Zebra", "m", 1, null));
        return items;
    }

    private static TerminalView<Row> view() {
        return new TerminalView<Row>(new Row(null, null, 0, null));
    }

    /**
     * The names of the displayed items, in order.
     */
    private static List<String> names(List<Row> items) {
        List<String> names = new ArrayList<String>();
        for (Row item : items) {
            names.add(item.name);
        }
        return names;
    }

    @Test
    public void amountSortOrdersByQuantityDescendingWithCaseInsensitiveNameTieBreak() {
        TerminalView<Row> v = view();
        v.setItems(baseItems());
        v.setSorting(SortingTypes.AMOUNT, false);
        assertEquals(Arrays.asList("Banana", "cherry", "apple", "Zebra"), names(v.view()));
    }

    @Test
    public void amountReversedSortOrdersAscending() {
        TerminalView<Row> v = view();
        v.setItems(baseItems());
        v.setSorting(SortingTypes.AMOUNT, true);
        assertEquals(Arrays.asList("Zebra", "apple", "cherry", "Banana"), names(v.view()));
    }

    @Test
    public void nameSortOrdersCaseInsensitively() {
        TerminalView<Row> v = view();
        v.setItems(baseItems());
        v.setSorting(SortingTypes.NAME, false);
        assertEquals(Arrays.asList("apple", "Banana", "cherry", "Zebra"), names(v.view()));
    }

    @Test
    public void searchFiltersByName() {
        TerminalView<Row> v = view();
        v.setItems(baseItems());
        v.setSorting(SortingTypes.NAME, false);
        v.setSearch("an");
        assertEquals(Arrays.asList("Banana"), names(v.view()));
    }

    @Test
    public void searchIsRegexOrOnPipe() {
        TerminalView<Row> v = view();
        v.setItems(baseItems());
        v.setSorting(SortingTypes.NAME, false);
        v.setSearch("a|z");
        assertEquals(Arrays.asList("apple", "Banana", "Zebra"), names(v.view()));
    }

    @Test
    public void modFilterMatchesNothingForTheWrongMod() {
        TerminalView<Row> v = view();
        v.setItems(baseItems());
        v.setSorting(SortingTypes.NAME, false);
        v.setSearch("@other");
        assertEquals(Arrays.asList(), names(v.view()));
    }

    @Test
    public void emptySearchShowsAllItems() {
        TerminalView<Row> v = view();
        v.setItems(baseItems());
        v.setSorting(SortingTypes.NAME, false);
        v.setSearch("");
        assertEquals(Arrays.asList("apple", "Banana", "cherry", "Zebra"), names(v.view()));
    }

    @Test
    public void frozenModeKeepsOrderWhenCountsChangeAndAppendsNewItemsInOrder() {
        TerminalView<Row> v = view();
        v.setItems(baseItems());
        v.setSorting(SortingTypes.AMOUNT, false);
        assertEquals(Arrays.asList("Banana", "cherry", "apple", "Zebra"), names(v.view()));

        v.setFrozen(true);

        // Counts change: apple grows to 50, the others are unchanged.
        List<Row> updated = new ArrayList<Row>();
        updated.add(new Row("apple", "m", 50, null));
        updated.add(new Row("Banana", "m", 10, null));
        updated.add(new Row("cherry", "m", 10, null));
        updated.add(new Row("Zebra", "m", 1, null));
        v.setItems(updated);
        assertEquals(Arrays.asList("Banana", "cherry", "apple", "Zebra"), names(v.view()));

        // A new row appears; it is appended at the end in sorted order.
        List<Row> withNew = new ArrayList<Row>(updated);
        withNew.add(new Row("Mango", "m", 7, null));
        v.setItems(withNew);
        assertEquals(Arrays.asList("Banana", "cherry", "apple", "Zebra", "Mango"), names(v.view()));

        // Unfreezing re-sorts normally: apple(50) is now first.
        v.setFrozen(false);
        assertEquals(Arrays.asList("apple", "Banana", "cherry", "Mango", "Zebra"), names(v.view()));
    }

    @Test
    public void rowCountRoundsUp() {
        TerminalView<Row> v = view();
        List<Row> ten = new ArrayList<Row>();
        for (int i = 0; i < 10; i++) {
            ten.add(new Row("item" + i, "m", 1, null));
        }
        v.setItems(ten);
        assertEquals(2, v.rowCount(9));
        assertEquals(5, v.rowCount(2));
        assertEquals(1, v.rowCount(10));
        assertEquals(10, v.rowCount(1));
    }

    @Test
    public void pageClampsAStaleOffsetWithTheGivenVisibleRows() {
        TerminalView<Row> v = view();
        List<Row> ten = new ArrayList<Row>();
        for (int i = 0; i < 10; i++) {
            ten.add(new Row("item" + i, "m", 1, null));
        }
        v.setItems(ten);
        // 10 items in 9 columns is 2 rows. An offset of 99 rows is stale;
        // it must clamp to the last valid offset (0 with 3 visible rows,
        // since maxScrollRows(10, 9, 3) = 2 - 3 clamped to 0) and never
        // throw. With 3 visible rows everything fits, so the page holds all
        // 10 items.
        List<Row> last = v.page(99, 9, 3);
        assertNotNull(last);
        assertEquals(10, last.size());
    }

    @Test
    public void pageReturnsTheVisibleSlice() {
        TerminalView<Row> v = view();
        List<Row> ten = new ArrayList<Row>();
        for (int i = 0; i < 10; i++) {
            ten.add(new Row("item" + i, "m", 1, null));
        }
        v.setItems(ten);
        // Row 0 holds the 9 items of the first row; row 1 holds the single
        // tenth item (maxScrollRows(10, 9, 1) = 1, so row 1 is the last).
        assertEquals(
            9,
            v.page(0, 9, 1)
                .size());
        assertEquals(
            1,
            v.page(1, 9, 1)
                .size());
        // 3 visible rows: everything fits, so the page holds all 10.
        assertEquals(
            10,
            v.page(0, 9, 3)
                .size());
    }

    @Test
    public void atReturnsNullForABadIndex() {
        TerminalView<Row> v = view();
        v.setItems(baseItems());
        assertNull(v.at(-1));
        assertNull(v.at(4));
        assertNull(v.at(100));
        assertEquals("Zebra", v.at(3).name);
    }

    @Test
    public void viewIsUnmodifiableAndSizeTracksIt() {
        TerminalView<Row> v = view();
        v.setItems(baseItems());
        assertEquals(4, v.size());
        assertEquals(
            4,
            v.view()
                .size());
    }

    /**
     * TERM-01 (upstream): the scroll range was computed with a hard-coded
     * 5 visible rows, so a tall terminal that shows 3 rows could scroll to
     * an offset that was out of range. Here the visible rows are a
     * parameter and the clamp uses the given visible rows: with 90 items in
     * 9 columns, page(0, 9, 3) shows 27 items and page(0, 9, 5) shows 45,
     * and a stale offset clamps against the given visible rows rather than
     * the upstream 5.
     */
    @Test
    public void doesNotReproduceTERM01_visibleRowsIsAParameter() {
        TerminalView<Row> v = view();
        List<Row> ninety = new ArrayList<Row>();
        for (int i = 0; i < 90; i++) {
            ninety.add(new Row("item" + i, "m", 1, null));
        }
        v.setItems(ninety);
        assertEquals(
            27,
            v.page(0, 9, 3)
                .size());
        assertEquals(
            45,
            v.page(0, 9, 5)
                .size());
        // A stale offset clamps with the given visible rows (3), not the
        // upstream hard-coded 5: maxScrollRows(90, 9, 3) = 7, so offset 99
        // clamps to row 7 and shows rows 7 and 8 (27 items).
        List<Row> clamped = v.page(99, 9, 3);
        assertNotNull(clamped);
        assertEquals(27, clamped.size());
        assertTrue(!clamped.isEmpty());
    }
}
