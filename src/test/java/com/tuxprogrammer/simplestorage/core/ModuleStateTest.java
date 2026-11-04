package com.tuxprogrammer.simplestorage.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.tuxprogrammer.simplestorage.config.ModulesConfig;
import com.tuxprogrammer.simplestorage.config.SimpleStorageConfig;

/** Module flags: one per feature area or integration, read through {@link ModuleState}. */
public class ModuleStateTest {

    private static final Set<Module> DEFAULTS = EnumSet
        .of(Module.STORAGE, Module.TERMINAL, Module.NETWORK, Module.WIRELESS, Module.NEI, Module.WAILA);

    @Test
    public void defaultsAreEverythingExceptAppliedEnergistics() {
        assertEquals(
            DEFAULTS,
            ModuleState.configured()
                .active());
    }

    @Test
    public void snapshotFollowsTheConfigFields() {
        ModulesConfig.network = false;
        try {
            ModuleState state = ModuleState.configured();
            assertFalse(state.isActive(Module.NETWORK));
            assertTrue(state.isActive(Module.STORAGE));
        } finally {
            ModulesConfig.network = true;
        }
    }

    @Test
    public void wirelessNeedsTheTerminalModule() {
        ModulesConfig.terminal = false;
        try {
            ModuleState state = ModuleState.configured();
            assertFalse(state.isActive(Module.TERMINAL));
            assertFalse(state.isActive(Module.WIRELESS), "a wireless terminal is inactive without the terminal module");
        } finally {
            ModulesConfig.terminal = true;
        }
    }

    @Test
    public void snapshotsAreImmutable() {
        Set<Module> source = EnumSet.of(Module.STORAGE);
        ModuleState state = ModuleState.of(source);
        source.add(Module.TERMINAL);
        assertFalse(state.isActive(Module.TERMINAL), "a later change to the source set does not leak in");
    }

    @Test
    public void everyModuleHasAConfigFieldAndNoFieldIsLeftOver() {
        Set<String> keys = new HashSet<>();
        for (Module module : Module.values()) {
            keys.add(module.configKey());
        }
        Set<String> fields = new HashSet<>();
        SimpleStorageConfig.options(ModulesConfig.class)
            .forEach(field -> fields.add(field.getName()));
        for (Field field : ModulesConfig.Integration.class.getDeclaredFields()) {
            fields.add("integration." + field.getName());
        }
        assertEquals(keys, fields);
    }
}
