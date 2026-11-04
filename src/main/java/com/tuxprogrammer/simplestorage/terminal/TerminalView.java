package com.tuxprogrammer.simplestorage.terminal;
// Ported from Tom's Simple Storage (MIT):
// https://github.com/tom5454/Toms-Storage/blob/95bb61d/NeoForge/src/platform-shared/java/com/tom/storagemod/screen/AbstractStorageTerminalScreen.java
// Avoids: TERM-01

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.tuxprogrammer.simplestorage.sort.SortKey;
import com.tuxprogrammer.simplestorage.sort.SortingTypes;

/**
 * The client's pure item-list model for a storage terminal screen: the set
 * of item stacks, the current search text, the current sort mode and
 * direction, and the frozen (ghost) mode. It knows nothing about widgets,
 * textures, or the network: it only answers "which items, in which order,
 * which slice is visible, and how many rows does that take".
 *
 * <p>
 * The display order is filtered by {@link SearchQuery#parse(String)} and
 * ordered by {@link SortingTypes#comparator(boolean)}, stably (equal sort
 * keys keep their input order). It is recomputed lazily the first time a
 * query is made after anything changed, so repeated reads are cheap.
 *
 * <p>
 * Ghost (frozen) mode: while frozen the displayed order does not change when
 * item counts change. Items keep the order they had when the freeze began,
 * items that first appear while frozen are appended at the end in sorted
 * order, items that vanish are removed, and unfreezing re-sorts normally.
 *
 * <p>
 * A null source result (a null {@link SortKey} or a null
 * {@link SearchQuery.Item}) must never throw: an item whose sort key is null
 * sorts last (after every item with a key, in input order among such items)
 * and an item whose search item is null never matches a non-empty search.
 *
 * Side: client logic, plain Java.
 */
public final class TerminalView<T> {

    /**
     * Adapts arbitrary item objects to the plain data this view sorts and
     * searches by. No Minecraft types may appear in a production
     * implementation.
     *
     * @param <T> the item type the view displays
     */
    public interface Source<T> {

        /**
         * @param item the item
         * @return the sort key for the item, or null when the item has no
         *         sort key (such items sort last)
         */
        SortKey sortKey(T item);

        /**
         * @param item the item
         * @return the search item for the item, or null when the item has no
         *         searchable data (such items never match a non-empty search)
         */
        SearchQuery.Item searchItem(T item);

        /**
         * @param item the item
         * @return a stable identifier for the item, used to tell whether an
         *         item is the same one across updates (so frozen order is
         *         preserved); equal keys are treated as the same item
         */
        String keyOf(T item);
    }

    private final Source<T> source;
    private List<T> items = new ArrayList<T>();
    private String search = "";
    private SortingTypes sortType = SortingTypes.AMOUNT;
    private boolean reversed = false;
    private boolean frozen = false;

    /** Frozen order: the item keys in displayed order when the freeze began. */
    private List<String> frozenOrder = new ArrayList<String>();

    /** Current (stale or fresh) displayed list; rebuilt when needed. */
    private List<T> view = new ArrayList<T>();
    /** The unmodifiable wrapper over {@link #view}, rebuilt with it. */
    private List<T> viewUnmodifiable = Collections.unmodifiableList(new ArrayList<T>());
    /** Whether the cached {@link #view} matches the current state. */
    private boolean viewValid = false;

    /**
     * Creates a new view over the given source. It starts empty with the
     * default sort (AMOUNT, not reversed) and no search.
     *
     * @param source the source that adapts items to sort and search data
     */
    public TerminalView(Source<T> source) {
        this.source = source;
    }

    /**
     * Replaces the displayed items. A null list is treated as empty.
     *
     * <p>
     * The list is replaced, not merged: the new objects are the only ones
     * this view knows about, so their counts are always current. When
     * frozen, the displayed order is preserved for items that were already
     * present and the new items are appended in sorted order (see
     * {@link #setFrozen(boolean)}).
     *
     * @param items the items to display, or null for none
     */
    public void setItems(List<T> items) {
        List<T> newItems = items == null ? new ArrayList<T>() : new ArrayList<T>(items);
        this.items = newItems;
        this.viewValid = false;
    }

    /**
     * Sets the search text. A null value is treated as an empty search.
     *
     * @param search the search text, or null for none
     */
    public void setSearch(String search) {
        this.search = search == null ? "" : search;
        this.viewValid = false;
    }

    /**
     * Sets the sort mode and direction.
     *
     * @param type     the sort mode
     * @param reversed true to reverse every level of the ordering
     */
    public void setSorting(SortingTypes type, boolean reversed) {
        this.sortType = type;
        this.reversed = reversed;
        this.viewValid = false;
    }

    /**
     * Enables or disables ghost (frozen) mode.
     *
     * <p>
     * While frozen the displayed order does not change when item counts
     * change: items keep the order they had when the freeze began, items
     * that first appear while frozen are appended at the end in sorted
     * order, and items that vanish are removed. Unfreezing re-sorts
     * normally.
     *
     * @param frozen true to enter frozen mode, false to leave it
     */
    public void setFrozen(boolean frozen) {
        if (frozen == this.frozen) {
            return;
        }
        if (frozen) {
            // Snapshot the current displayed order as the frozen baseline.
            if (!this.viewValid) {
                recompute();
            }
            frozenOrder = new ArrayList<String>();
            for (T item : this.view) {
                frozenOrder.add(source.keyOf(item));
            }
        } else {
            frozenOrder = new ArrayList<String>();
        }
        this.frozen = frozen;
        this.viewValid = false;
    }

    /**
     * Returns the displayed items: filtered by the current search and
     * ordered by the current sort (or the frozen order while frozen).
     *
     * <p>
     * The list is recomputed lazily the first time this is called after any
     * change, and the returned list is unmodifiable.
     *
     * @return an unmodifiable list of the displayed items, never null
     */
    public List<T> view() {
        if (!viewValid) {
            recompute();
        }
        return viewUnmodifiable;
    }

    /**
     * Returns the number of displayed items.
     *
     * @return the count of displayed items
     */
    public int size() {
        return view().size();
    }

    /**
     * Returns the number of rows the grid takes to display all items.
     *
     * @param columns the number of columns; below 1 counts as 1
     * @return the total row count, {@code ceil(size / columns)}
     */
    public int rowCount(int columns) {
        int cols = Math.max(columns, 1);
        int total = size();
        return (total + cols - 1) / cols;
    }

    /**
     * Returns the visible slice of the grid: the items from
     * {@code offsetRows} rows in, at most {@code visibleRows} rows worth.
     *
     * <p>
     * The slice starts at {@code offsetRows * columns} items and holds at
     * most {@code visibleRows * columns} items. A stale or negative
     * {@code offsetRows} is clamped with
     * {@link ScrollMath#maxScrollRows(int, int, int)} using the given
     * {@code visibleRows}, so an offset that is out of date never throws.
     *
     * @param offsetRows  the scroll offset in rows (clamped)
     * @param columns     the number of columns; below 1 counts as 1
     * @param visibleRows the number of visible rows; below 1 counts as 1
     * @return the visible slice, unmodifiable, possibly empty
     */
    public List<T> page(int offsetRows, int columns, int visibleRows) {
        List<T> all = view();
        int cols = Math.max(columns, 1);
        int rows = Math.max(visibleRows, 1);
        int maxRows = ScrollMath.maxScrollRows(all.size(), cols, rows);
        int offset = ScrollMath.clampOffset(offsetRows, maxRows);
        int start = offset * cols;
        if (start < 0) {
            start = 0;
        }
        if (start > all.size()) {
            start = all.size();
        }
        int end = start + rows * cols;
        if (end < 0) {
            end = 0;
        }
        if (end > all.size()) {
            end = all.size();
        }
        if (start >= end) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(all.subList(start, end));
    }

    /**
     * Returns the item at the given displayed index, or null when the index
     * is out of range.
     *
     * @param index the displayed index
     * @return the item at that index, or null for a bad index
     */
    public T at(int index) {
        List<T> all = view();
        if (index < 0 || index >= all.size()) {
            return null;
        }
        return all.get(index);
    }

    /**
     * Rebuilds the cached displayed list from the current state.
     */
    private void recompute() {
        Comparator<T> comparator = new Comparator<T>() {

            @Override
            public int compare(T a, T b) {
                SortKey ka = source.sortKey(a);
                SortKey kb = source.sortKey(b);
                if (ka == null || kb == null) {
                    // A null key sorts last; equal nulls keep input order.
                    if (ka == null) {
                        return kb == null ? 0 : 1;
                    }
                    return -1;
                }
                return sortType.comparator(reversed)
                    .compare(ka, kb);
            }
        };

        List<T> filtered = new ArrayList<T>();
        if (frozen) {
            // The frozen baseline is the displayed order at the moment the
            // freeze began (the last non-frozen recompute). It is
            // independent of the items list, which may already have changed.
            //
            // Keep the frozen order for items that were present then; append
            // items that first appear now in sorted order; drop vanished ones.
            List<T> present = new ArrayList<T>();
            for (T item : items) {
                present.add(item);
            }
            // Order the new (not in frozenOrder) items by the sort.
            List<T> newItems = new ArrayList<T>();
            for (T item : items) {
                String key = source.keyOf(item);
                if (!frozenOrder.contains(key)) {
                    newItems.add(item);
                }
            }
            Collections.sort(newItems, comparator);
            for (String key : frozenOrder) {
                for (T item : present) {
                    if (key.equals(source.keyOf(item))) {
                        filtered.add(item);
                        break;
                    }
                }
            }
            filtered.addAll(newItems);
        } else {
            filtered.addAll(items);
            // Search filter (empty search matches everything).
            SearchQuery query = SearchQuery.parse(search);
            List<T> matched = new ArrayList<T>();
            for (T item : filtered) {
                SearchQuery.Item searchItem = source.searchItem(item);
                if (searchItem != null && query.matches(searchItem)) {
                    matched.add(item);
                }
            }
            filtered = matched;
            Collections.sort(filtered, comparator);
        }

        this.view = filtered;
        this.viewUnmodifiable = Collections.unmodifiableList(filtered);
        this.viewValid = true;
    }
}
