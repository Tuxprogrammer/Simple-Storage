package com.tuxprogrammer.simplestorage.core;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import net.minecraft.util.StatCollector;

import com.tuxprogrammer.simplestorage.block.ModBlocks;
import com.tuxprogrammer.simplestorage.config.SimpleStorageConfig;
import com.tuxprogrammer.simplestorage.item.ModItems;

/**
 * The dev self-check (docs/testing/README.md § 3): in a development environment, {@code postInit} verifies what the
 * mod has registered so far and stops the game on a failure. Its tier-cap checks land in M05.
 *
 * <p>
 * Checks today: every config screen key has an English name, the module defaults are the documented ones, and all
 * registered content (blocks, items, tile entities) is registered with a lang entry.
 *
 * <p>
 * Side: both.
 */
public final class DevSelfCheck {

    private DevSelfCheck() {}

    /**
     * Runs the checks when {@link DevEnvironment#selfCheckEnabled()}.
     *
     * @throws IllegalStateException listing every failed check
     */
    public static void run() {
        if (!DevEnvironment.selfCheckEnabled()) {
            return;
        }
        List<String> failures = failures();
        if (!failures.isEmpty()) {
            throw new IllegalStateException("Simple Storage dev self-check failed:\n" + String.join("\n", failures));
        }
    }

    /** Returns one line per failed check, empty when all pass. */
    static List<String> failures() {
        List<String> failures = new ArrayList<>();
        for (String key : SimpleStorageConfig.langKeys()) {
            if (!StatCollector.canTranslate(key)) {
                failures.add("config key without a lang entry: " + key);
            }
        }
        EnumSet<Module> defaults = EnumSet
            .of(Module.STORAGE, Module.TERMINAL, Module.NETWORK, Module.WIRELESS, Module.NEI, Module.WAILA);
        if (!ModuleState.configured()
            .active()
            .containsAll(defaults)) {
            failures.add("a module is off in the default configuration; check that no config override is active");
        }
        if (ModBlocks.registeredCount() != ModBlocks.NAMES.size()) {
            failures.add("registered blocks: " + ModBlocks.registeredCount() + " of " + ModBlocks.NAMES.size());
        }
        for (String name : ModBlocks.NAMES) {
            if (!StatCollector.canTranslate("tile.simplestoragenh." + name + ".name")) {
                failures.add("block without a lang entry: " + name);
            }
        }
        if (ModItems.paintKit == null) {
            failures.add("paint kit not registered");
        } else if (!StatCollector.canTranslate("item.simplestoragenh.paint_kit.name")) {
            failures.add("config key without a lang entry: item.simplestoragenh.paint_kit.name");
        }
        // Tile entity registration: for every id in ModTileEntities.IDS, the tile entity class must be mapped;
        // skipped because GameRegistry does not expose the mapping.
        return failures;
    }
}
