package com.tuxprogrammer.simplestorage.config;

import com.gtnewhorizon.gtnhlib.config.Config;

/**
 * The {@code modules} category of {@code config/simplestoragenh.cfg}: one flag per module area, and a nested
 * {@code integration} subcategory with one flag per optional integration.
 *
 * <p>
 * A flag gates a module's behavior; it never changes what is registered. Labels and tooltips are the lang keys
 * {@code simplestoragenh.modules.<field>} and {@code simplestoragenh.modules.<field>.tooltip} (and the
 * {@code integration} equivalents), with the field name in lowercase, because GTNHLib lowercases every part of a
 * generated key.
 *
 * <p>
 * Side: both (the integrated server reads the same file); the values matter only on the server.
 */
@Config(modid = "simplestoragenh", filename = "simplestoragenh", category = ModulesConfig.CATEGORY)
@Config.Comment({ "Modules: one switch per module area and per optional integration. A flag gates a module's behavior;",
    "it never changes what is registered." })
public final class ModulesConfig {

    /** The category name, also the middle part of every lang key here. Not an option: GTNHLib reads every field. */
    @Config.Ignore
    public static final String CATEGORY = "modules";

    @Config.Comment("Storage blocks (cabinet, crate, trims).")
    @Config.DefaultBoolean(true)
    public static boolean storage = true;

    @Config.Comment("Storage + crafting terminals.")
    @Config.DefaultBoolean(true)
    public static boolean terminal = true;

    @Config.Comment("Cables, cable connectors, inventory connectors, proxy, interface and hopper.")
    @Config.DefaultBoolean(true)
    public static boolean network = true;

    @Config.Comment("Wireless + advanced wireless terminals.")
    @Config.DefaultBoolean(true)
    public static boolean wireless = true;

    /**
     * The {@code modules.integration} subcategory: one flag per optional integration.
     *
     * <p>
     * Side: both.
     */
    /**
     * Subcategory {@code modules.integration}: GTNHLib reads a static field of a plain nested class as a subcategory.
     */
    public static final Integration integration = new Integration();

    /** Optional integrations, one flag each (only meaningful when the mod is loaded). */
    public static class Integration {

        /** NEI plugin (only meaningful with NEI loaded). */
        @Config.Comment("NEI plugin (only meaningful with NEI loaded).")
        @Config.DefaultBoolean(true)
        public boolean nei = true;

        /** Waila providers (only with Waila loaded). */
        @Config.Comment("Waila providers (only with Waila loaded).")
        @Config.DefaultBoolean(true)
        public boolean waila = true;

        /** AE2 storage compat (off at alpha). */
        @Config.Comment("AE2 storage compat (off at alpha).")
        @Config.DefaultBoolean(false)
        public boolean appeng = false;
    }

    private ModulesConfig() {}
}
