package com.tuxprogrammer.simplestorage.config;

import com.gtnewhorizon.gtnhlib.config.Config;

/**
 * The {@code behavior} category of {@code config/simplestoragenh.cfg}: general behavior toggles and the connector's
 * block/mod skip lists.
 *
 * <p>
 * Labels and tooltips are the lang keys {@code simplestoragenh.behavior.<field>} and
 * {@code simplestoragenh.behavior.<field>.tooltip}, with the field name in lowercase, because GTNHLib lowercases
 * every part of a generated key.
 *
 * <p>
 * Side: both (the integrated server reads the same file); the values matter only on the server.
 */
@Config(modid = "simplestoragenh", filename = "simplestoragenh", category = BehaviorConfig.CATEGORY)
@Config.Comment({ "Behavior: general toggles and connector skip lists." })
public final class BehaviorConfig {

    /** The category name, also the middle part of every lang key here. Not an option: GTNHLib reads every field. */
    @Config.Ignore
    public static final String CATEGORY = "behavior";

    @Config.Comment("Run the network update multithreaded.")
    @Config.DefaultBoolean(true)
    public static boolean runMultithreaded = true;

    @Config.Comment("Basic hopper transfer cooldown in ticks.")
    @Config.DefaultInt(10)
    @Config.RangeInt(min = 1, max = 200)
    public static int basicHopperCooldown = 10;

    @Config.Comment("Mod ids the inventory connector skips.")
    @Config.DefaultStringList({})
    public static String[] blockedMods = {};

    @Config.Comment("Blocks the inventory connector skips.")
    @Config.DefaultStringList({})
    public static String[] blockedBlocks = {};

    private BehaviorConfig() {}
}
