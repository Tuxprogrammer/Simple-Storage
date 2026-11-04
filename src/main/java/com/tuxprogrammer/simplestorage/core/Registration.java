package com.tuxprogrammer.simplestorage.core;

import com.tuxprogrammer.simplestorage.SimpleStorage;
import com.tuxprogrammer.simplestorage.block.ModBlocks;
import com.tuxprogrammer.simplestorage.blockentity.ModTileEntities;
import com.tuxprogrammer.simplestorage.item.ModItems;

/**
 * Top-level entry point for all Simple Storage (Minecraft 1.7.10 /
 * Forge 10.13.4.1614) registration, invoked once during preInit.
 *
 * <p>
 * {@link #registerAll()} registers blocks, items, and tile entities in that
 * order, then logs a single summary line via {@code SimpleStorage.LOG}.
 * Like the individual registries it delegates to, it follows the
 * config-safe-registration contract: it NEVER reads config or module flags,
 * so the full content set always exists after preInit.
 * </p>
 *
 * <p>
 * Side: both.
 * </p>
 */
public final class Registration {

    private Registration() {}

    /**
     * Registers all mod content: {@link ModBlocks#register()}, then
     * {@link ModItems#register()}, then {@link ModTileEntities#register()},
     * followed by a one-line log summary.
     *
     * <p>
     * Config-safe: never reads config or module flags (nor do the calls
     * it delegates to).
     * </p>
     */
    public static void registerAll() {
        ModBlocks.register();
        ModItems.register();
        ModTileEntities.register();
        SimpleStorage.LOG.info(
            "Registered {} blocks, {} item, {} tile entities",
            ModBlocks.registeredCount(),
            ModItems.registeredCount(),
            ModTileEntities.IDS.size());
    }
}
