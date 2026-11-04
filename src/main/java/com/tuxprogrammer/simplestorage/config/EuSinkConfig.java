package com.tuxprogrammer.simplestorage.config;

import com.gtnewhorizon.gtnhlib.config.Config;

/**
 * The optional EU sink of the level emitter (design record 0004 section 3, decision D7). Off by default: the mod is
 * power-free unless a pack turns this on.
 *
 * <p>
 * Side: both.
 */
@Config(modid = "simplestoragenh", filename = "simplestoragenh", category = EuSinkConfig.CATEGORY)
@Config.Comment({ "EU sink: the level emitter can draw EU while active. Off by default." })
public final class EuSinkConfig {

    /** The category name in {@code simplestoragenh.cfg}. */
    @Config.Ignore
    public static final String CATEGORY = "euSink";

    /** Whether the level emitter draws EU. */
    @Config.Comment("Whether the level emitter draws EU while active.")
    @Config.DefaultBoolean(false)
    public static boolean enabled = false;

    /** The draw in EU per tick, 8 being the ULV rate. */
    @Config.Comment("EU per tick drawn while active (8 is the ULV rate).")
    @Config.DefaultInt(8)
    @Config.RangeInt(min = 1, max = Integer.MAX_VALUE)
    public static int euPerTick = 8;

    private EuSinkConfig() {}
}
