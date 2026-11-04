package com.tuxprogrammer.simplestorage.config;

import com.gtnewhorizon.gtnhlib.config.Config;

/**
 * The {@code wireless} category of {@code config/simplestoragenh.cfg}: wireless terminal reach and beacon settings.
 *
 * <p>
 * {@code reach} and {@code advRange} carry {@code @Config.Sync} because a client must see them. Labels and tooltips
 * are the lang keys {@code simplestoragenh.wireless.<field>} and {@code simplestoragenh.wireless.<field>.tooltip},
 * with the field name in lowercase, because GTNHLib lowercases every part of a generated key.
 *
 * <p>
 * Side: both (the integrated server reads the same file); the values matter only on the server.
 */
@Config(modid = "simplestoragenh", filename = "simplestoragenh", category = WirelessConfig.CATEGORY)
@Config.Comment({ "Wireless: wireless terminal reach and beacon settings." })
public final class WirelessConfig {

    /** The category name, also the middle part of every lang key here. Not an option: GTNHLib reads every field. */
    @Config.Ignore
    public static final String CATEGORY = "wireless";

    @Config.Comment("Basic wireless terminal reach in blocks.")
    @Config.Sync
    @Config.DefaultInt(16)
    @Config.RangeInt(min = 4, max = 64)
    public static int reach = 16;

    @Config.Comment("Advanced wireless terminal range in blocks.")
    @Config.Sync
    @Config.DefaultInt(64)
    @Config.RangeInt(min = 16, max = 512)
    public static int advRange = 64;

    @Config.Comment("Wireless terminal beacon level (-1 disables, 0 = one beacon suffices).")
    @Config.DefaultInt(1)
    @Config.RangeInt(min = -1, max = 4)
    public static int beaconLvl = 1;

    @Config.Comment("Wireless terminal beacon level across dimensions (-1 disables, 0 = one beacon suffices).")
    @Config.DefaultInt(4)
    @Config.RangeInt(min = -1, max = 4)
    public static int beaconLvlDim = 4;

    private WirelessConfig() {}
}
