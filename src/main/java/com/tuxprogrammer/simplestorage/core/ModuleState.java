package com.tuxprogrammer.simplestorage.core;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

import com.tuxprogrammer.simplestorage.config.ModulesConfig;

/**
 * Which modules are active: the one authority every use site asks, instead of reading config flags itself
 * (config-safe-registration). Instances are immutable snapshots.
 *
 * <p>
 * A module whose parent is off is inactive even if its own flag is on: {@link Module#WIRELESS} requires
 * {@link Module#TERMINAL}; the integrations need no parent.
 *
 * <p>
 * Side: both; only the server's state decides behavior. No module flag is synced to clients.
 */
public final class ModuleState {

    private final Set<Module> active;

    private ModuleState(Set<Module> active) {
        this.active = Collections.unmodifiableSet(active);
    }

    /**
     * Returns a snapshot with exactly the given modules active.
     *
     * @param active the active modules
     * @return the snapshot
     */
    public static ModuleState of(Set<Module> active) {
        EnumSet<Module> copy = EnumSet.noneOf(Module.class);
        copy.addAll(active);
        return new ModuleState(copy);
    }

    /** Returns a snapshot with every module active, the default configuration. */
    public static ModuleState allActive() {
        return new ModuleState(EnumSet.allOf(Module.class));
    }

    /** Returns a snapshot of the flags as the config currently holds them. */
    public static ModuleState configured() {
        EnumSet<Module> active = EnumSet.noneOf(Module.class);
        addIf(active, Module.STORAGE, ModulesConfig.storage);
        addIf(active, Module.TERMINAL, ModulesConfig.terminal);
        addIf(active, Module.NETWORK, ModulesConfig.network);
        addIf(active, Module.WIRELESS, ModulesConfig.wireless);
        addIf(active, Module.NEI, ModulesConfig.integration.nei);
        addIf(active, Module.WAILA, ModulesConfig.integration.waila);
        addIf(active, Module.APPENG, ModulesConfig.integration.appeng);
        removeIfMissingParent(active);
        return new ModuleState(active);
    }

    /**
     * Returns whether a module is active.
     *
     * @param module the module
     * @return {@code true} when its behavior runs
     */
    public boolean isActive(Module module) {
        return active.contains(module);
    }

    /** Returns the active modules, in declaration order. */
    public Set<Module> active() {
        return active;
    }

    /** Returns the active modules for the log, for example {@code [STORAGE, TERMINAL]}. */
    @Override
    public String toString() {
        return active.toString();
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof ModuleState && ((ModuleState) other).active.equals(active);
    }

    @Override
    public int hashCode() {
        return active.hashCode();
    }

    private static void addIf(Set<Module> active, Module module, boolean on) {
        if (on) {
            active.add(module);
        }
    }

    private static void removeIfMissingParent(Set<Module> active) {
        if (active.contains(Module.WIRELESS) && !active.contains(Module.TERMINAL)) {
            active.remove(Module.WIRELESS);
        }
    }
}
