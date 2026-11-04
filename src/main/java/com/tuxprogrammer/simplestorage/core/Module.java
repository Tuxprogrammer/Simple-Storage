package com.tuxprogrammer.simplestorage.core;

/**
 * The module flags of the {@code modules} config category (docs/new-spec/01-core/index.md, "Module flags"). A flag
 * gates
 * the area's behavior off; it never changes registration (CONTRIBUTING § Architecture).
 *
 * <p>
 * Each constant names its key in the {@code modules} category of the config. The key is part of the config file
 * format: once released it is never renamed (config-safe-registration).
 *
 * <p>
 * Side: both (plain Java).
 */
public enum Module {

    /** The storage area: filing cabinets and their inventory (M02). */
    STORAGE("storage"),
    /** Terminals: the storage and crafting terminal and the configurator (M03). */
    TERMINAL("terminal"),
    /** The network: cables, connectors, emitters and their reach (M04). */
    NETWORK("network"),
    /** Wireless: the wireless and advanced wireless terminal (M04); requires {@link #TERMINAL}. */
    WIRELESS("wireless"),
    /** The NEI recipe viewer and usage integration. */
    NEI("integration.nei"),
    /** The Waila content and status providers. */
    WAILA("integration.waila"),
    /** The Applied Energistics 2 bridge (D10, D14); off by default. */
    APPENG("integration.appeng");

    private final String configKey;

    Module(String configKey) {
        this.configKey = configKey;
    }

    /** Returns the module's key in the {@code modules} config category, also the config field's name. */
    public String configKey() {
        return configKey;
    }
}
