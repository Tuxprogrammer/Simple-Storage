package com.tuxprogrammer.simplestorage.config;

import com.gtnewhorizon.gtnhlib.config.Config;

/**
 * The {@code network} category of {@code config/simplestoragenh.cfg}: inventory connector and cable-connector
 * link-beacon settings.
 *
 * <p>
 * Labels and tooltips are the lang keys {@code simplestoragenh.network.<field>} and
 * {@code simplestoragenh.network.<field>.tooltip}, with the field name in lowercase, because GTNHLib lowercases
 * every part of a generated key.
 *
 * <p>
 * Side: both (the integrated server reads the same file); the values matter only on the server.
 */
@Config(modid = "simplestoragenh", filename = "simplestoragenh", category = NetworkConfig.CATEGORY)
@Config.Comment({ "Network: inventory connector and cable-connector link-beacon settings." })
public final class NetworkConfig {

    /** The category name, also the middle part of every lang key here. Not an option: GTNHLib reads every field. */
    @Config.Ignore
    public static final String CATEGORY = "network";

    @Config.Comment("Inventory connector reach in blocks.")
    @Config.DefaultInt(16)
    @Config.RangeInt(min = 4, max = 256)
    public static int inventoryConnectorRange = 16;

    @Config.Comment("Only trims connect to the cable network.")
    @Config.DefaultBoolean(false)
    public static boolean onlyTrimsConnect = false;

    @Config.Comment("Maximum scan size for inventory cable connectors.")
    @Config.DefaultInt(2048)
    @Config.RangeInt(min = 16, max = Integer.MAX_VALUE)
    public static int invCableConnectorMaxScanSize = 2048;

    @Config.Comment("Link beacon level for cable connectors (-1 disables, 0 = one beacon suffices).")
    @Config.DefaultInt(0)
    @Config.RangeInt(min = -1, max = 4)
    public static int linkBeaconLvl = 0;

    @Config.Comment("Link beacon range in blocks for cable connectors.")
    @Config.DefaultInt(4096)
    @Config.RangeInt(min = 0, max = Integer.MAX_VALUE)
    public static int linkBeaconRange = 4096;

    @Config.Comment("Link beacon level for cable connectors in the same dimension (-1 disables, 0 = one beacon suffices).")
    @Config.DefaultInt(1)
    @Config.RangeInt(min = -1, max = 4)
    public static int linkBeaconLvlSameDim = 1;

    @Config.Comment("Link beacon level for cable connectors across dimensions (-1 disables, 0 = one beacon suffices).")
    @Config.DefaultInt(2)
    @Config.RangeInt(min = -1, max = 4)
    public static int linkBeaconLvlCrossDim = 2;

    private NetworkConfig() {}
}
