package com.tuxprogrammer.simplestorage.inventory;
// Ported from Tom's Simple Storage (MIT):
// https://github.com/tom5454/Toms-Storage/blob/95bb61d/NeoForge/src/platform-shared/java/com/tom/storagemod/block/entity/BasicInventoryHopperBlockEntity.java
// Avoids: STOR-08, STOR-09

/**
 * Pure cooldown state machine for the basic inventory hopper.
 *
 * <p>
 * Each call to {@link #tick} advances the hopper by one tick and reports a
 * {@link Decision}. A positive cooldown means the hopper must wait; once the
 * cooldown expires the hopper either transfers an item or backs off for the
 * base cooldown. {@link #transferred(boolean)} arms the cooldown after a
 * transfer attempt.
 * </p>
 *
 * <p>
 * Fixes ported in here:
 * </p>
 * <ul>
 * <li>STOR-08: upstream's unreachable {@code waiting == 2} "bottom just
 * changed" pause is now reachable via {@code bottomChanged} in
 * {@link #tick}, which pauses the hopper for exactly one full cooldown
 * cycle.</li>
 * <li>STOR-09: an unfiltered network-source hopper upstream re-ran the full
 * check every tick because no cooldown was ever set; here an
 * {@link Decision#IDLE_BACKOFF} always arms the base cooldown.</li>
 * </ul>
 *
 * <p>
 * Free of Minecraft types so the logic can be unit tested on plain Java
 * 8.
 * </p>
 *
 * Side: both (plain Java).
 */
public final class HopperLogic {

    /** Minimum base cooldown in ticks. */
    public static final int MIN_BASE_COOLDOWN = 1;

    /** Maximum base cooldown in ticks. */
    public static final int MAX_BASE_COOLDOWN = 200;

    /**
     * What the hopper should do on the current tick.
     */
    public enum Decision {
        /** The hopper is still cooling down and must do nothing this tick. */
        WAIT,
        /**
         * The hopper idled this tick and armed the base cooldown (disabled,
         * unfiltered network source, or a one-cycle bottom-change pause).
         */
        IDLE_BACKOFF,
        /** The hopper should attempt an item transfer this tick. */
        TRANSFER
    }

    /** The base cooldown in ticks, clamped to {@code 1..200}. */
    private final int baseCooldown;

    /** Remaining cooldown in ticks; the hopper waits while this is positive. */
    private int cooldown;

    /**
     * Creates the hopper state machine.
     *
     * @param baseCooldown the base cooldown in ticks, clamped to
     *                     {@code 1..200}
     */
    public HopperLogic(int baseCooldown) {
        this.baseCooldown = clamp(baseCooldown);
    }

    /**
     * Advances the hopper by one tick and reports the decision.
     *
     * <p>
     * Order of checks: the cooldown is decremented first and the hopper
     * waits while it is still positive; otherwise a disabled hopper backs
     * off; otherwise an unfiltered network source backs off (STOR-09: a
     * cooldown is always set on the way out); otherwise a bottom change
     * pauses for exactly one full cycle (STOR-08); otherwise the hopper
     * transfers.
     * </p>
     *
     * @param enabled       whether the hopper block is powered/on
     * @param topIsNetwork  whether the hopper's source inventory is a network
     * @param hasFilter     whether the hopper has a filter item configured
     * @param bottomChanged whether the destination inventory changed since the
     *                      last evaluation
     * @return the decision for this tick
     */
    public Decision tick(boolean enabled, boolean topIsNetwork, boolean hasFilter, boolean bottomChanged) {
        cooldown--;
        if (cooldown > 0) {
            return Decision.WAIT;
        }
        if (!enabled) {
            cooldown = baseCooldown;
            return Decision.IDLE_BACKOFF;
        }
        if (topIsNetwork && !hasFilter) {
            // STOR-09: always set a cooldown so the check does not spin every tick.
            cooldown = baseCooldown;
            return Decision.IDLE_BACKOFF;
        }
        if (bottomChanged) {
            // STOR-08: the "bottom just changed" pause is now reachable:
            // exactly one full cooldown cycle of waiting.
            cooldown = baseCooldown;
            return Decision.IDLE_BACKOFF;
        }
        return Decision.TRANSFER;
    }

    /**
     * Arms the cooldown after a transfer attempt.
     *
     * @param movedItem true if an item was actually moved (full base cooldown);
     *                  false for a failed attempt (half the base cooldown,
     *                  minimum 1 tick)
     */
    public void transferred(boolean movedItem) {
        cooldown = movedItem ? baseCooldown : Math.max(1, baseCooldown / 2);
    }

    /**
     * @return the remaining cooldown in ticks (may be negative if the hopper
     *         just transferred and has not ticked yet)
     */
    public int remaining() {
        return cooldown;
    }

    /**
     * Clamps a base cooldown into the supported range.
     */
    private static int clamp(int baseCooldown) {
        return Math.max(MIN_BASE_COOLDOWN, Math.min(MAX_BASE_COOLDOWN, baseCooldown));
    }
}
