package com.tuxprogrammer.simplestorage.core;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import net.minecraft.util.StatCollector;

import com.tuxprogrammer.simplestorage.config.SimpleStorageConfig;

/**
 * The dev self-check (docs/testing/README.md § 3): in a development environment, {@code postInit} verifies what the
 * mod has registered so far and stops the game on a failure. Its tier-cap checks land in M05.
 *
 * <p>
 * Checks today: every config screen key has an English name, and the module defaults are the documented ones.
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
        return failures;
    }
}
