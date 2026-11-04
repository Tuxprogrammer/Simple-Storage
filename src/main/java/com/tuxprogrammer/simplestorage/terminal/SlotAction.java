package com.tuxprogrammer.simplestorage.terminal;

/**
 * The ways a player may act on one storage entry of a terminal: pull or push
 * a whole stack, pull a single item (into the carry, or straight into the
 * player inventory when shift is held), take half of a stack, take a quarter
 * of a stack, or pull a whole stack straight into the player inventory.
 *
 * <p>
 * These are the server-side click behaviors of the storage terminal (the
 * original spec's {@code SlotAction} values, 01-core "Slot layout"/menu). The
 * client picks which action a click performs per control mode; the container
 * then executes it on the backend.
 *
 * <p>
 * <b>The ordinals are persisted:</b> the client and server agree on a value by
 * its ordinal, and the value is also compared across a round trip. The
 * constants must therefore keep their order and their ordinals must never be
 * renumbered (no inserting, reordering, or removing a constant without a
 * deliberate migration). Any ordinal outside {@code 0..values().length-1} is
 * treated by {@link #fromOrdinal(int)} as unknown.
 *
 * Side: both (plain Java).
 */
public enum SlotAction {

    /**
     * Left-click on a storage entry: if the player is carrying a stack, push
     * the whole carried stack into the network (any remainder stays carried);
     * otherwise pull one full max-size stack of the clicked entry into the
     * carry.
     */
    PULL_OR_PUSH_STACK,

    /**
     * Pull a single item of the clicked entry. With shift held it is placed
     * straight into the player inventory (dropping overflow); without shift it
     * either grows a matching carried stack by one, or, when nothing is
     * carried, pulls the single item into the carry.
     */
    PULL_ONE,

    /**
     * Take half: if the player is carrying a stack, split half of the carry
     * (at least one item) and push it; otherwise pull half (at least one item)
     * of the smaller of the available amount and one max-size stack into the
     * carry.
     */
    GET_HALF,

    /**
     * Take a quarter: like {@link #GET_HALF} but with a quarter of the amount.
     */
    GET_QUARTER,

    /**
     * Shift-pull: pull one full max-size stack of the clicked entry and place
     * it into the player inventory (dropping any overflow that does not fit).
     */
    SHIFT_PULL;

    /**
     * Resolves a persisted ordinal back to its constant.
     *
     * @param ordinal the ordinal to resolve
     * @return the matching constant, or {@code null} when the ordinal is out of
     *         range (callers must reject {@code null})
     */
    public static SlotAction fromOrdinal(int ordinal) {
        SlotAction[] values = values();
        if (ordinal < 0 || ordinal >= values.length) {
            return null;
        }
        return values[ordinal];
    }
}
