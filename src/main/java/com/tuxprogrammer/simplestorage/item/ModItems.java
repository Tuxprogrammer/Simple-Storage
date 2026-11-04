package com.tuxprogrammer.simplestorage.item;

import cpw.mods.fml.common.registry.GameRegistry;

/**
 * Central item registration for Simple Storage (Minecraft 1.7.10 /
 * Forge 10.13.4.1614).
 *
 * <p>
 * Registration follows the config-safe-registration contract: {@link #register()}
 * NEVER reads config or module flags, so the paint kit item always exists after
 * preInit, regardless of the current configuration.
 * </p>
 *
 * <p>
 * {@link #register()} is idempotent: a second call does nothing.
 * </p>
 *
 * <p>
 * Side: both.
 * </p>
 */
public final class ModItems {

    /** The paint kit item, assigned by {@link #register()}. */
    public static PaintKitItem paintKit;

    private static boolean registered;

    private ModItems() {}

    /**
     * Instantiates and registers the paint kit item with the Forge
     * {@link GameRegistry#registerItem(Item, String)} API under the name
     * {@code "paint_kit"}.
     * Idempotent: the second and later calls do nothing.
     *
     * <p>
     * Config-safe: never reads config or module flags.
     * </p>
     */
    public static void register() {
        if (registered) {
            return;
        }
        registered = true;

        paintKit = new PaintKitItem();
        GameRegistry.registerItem(paintKit, "paint_kit");
    }
}
