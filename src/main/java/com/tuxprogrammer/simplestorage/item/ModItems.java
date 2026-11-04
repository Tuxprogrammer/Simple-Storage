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

    /** The item filter item, assigned by {@link #register()}. */
    public static ItemFilterItem itemFilter;

    /** The tag filter item, assigned by {@link #register()}. */
    public static TagFilterItem tagFilter;

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

        itemFilter = new ItemFilterItem();
        GameRegistry.registerItem(itemFilter, "item_filter");

        tagFilter = new TagFilterItem();
        GameRegistry.registerItem(tagFilter, "tag_filter");
    }

    /**
     * The number of mod items that have been registered (i.e. are non-null):
     * {@link #paintKit}, {@link #itemFilter}, and {@link #tagFilter}.
     *
     * @return how many of the three items are non-null (0..3)
     */
    public static int registeredCount() {
        int count = 0;
        if (paintKit != null) {
            count++;
        }
        if (itemFilter != null) {
            count++;
        }
        if (tagFilter != null) {
            count++;
        }
        return count;
    }
}
