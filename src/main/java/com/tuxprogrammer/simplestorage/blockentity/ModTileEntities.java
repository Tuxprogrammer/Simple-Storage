package com.tuxprogrammer.simplestorage.blockentity;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import cpw.mods.fml.common.registry.GameRegistry;

/**
 * Central tile entity registration for Simple Storage (Minecraft 1.7.10 /
 * Forge 10.13.4.1614).
 *
 * <p>
 * Registration follows the config-safe-registration contract: {@link #register()}
 * NEVER reads config or module flags, so every tile entity in {@link #IDS} always
 * exists after preInit, regardless of the current configuration.
 * </p>
 *
 * <p>
 * {@link #register()} is idempotent: a second call does nothing.
 * </p>
 *
 * <p>
 * <strong>Never rename the ids in {@link #IDS}.</strong> Saved world data
 * (chunk data) keys on these ids, so renaming any of them would orphan existing
 * tile entities in already-saved worlds.
 * </p>
 *
 * <p>
 * Side: both.
 * </p>
 */
public final class ModTileEntities {

    /**
     * Tile entity ids, in registration order. Unmodifiable.
     * Do not rename: saved world data keys on these ids.
     */
    public static final List<String> IDS = Collections.unmodifiableList(
        Arrays.asList(
            "filing_cabinet",
            "open_crate",
            "face_node",
            "painted",
            "inventory_connector",
            "inventory_interface",
            "level_emitter",
            "inventory_hopper"));

    private static boolean registered;

    private ModTileEntities() {}

    /**
     * Registers all mod tile entities with the Forge
     * {@link GameRegistry#registerTileEntity(Class, String)} API under
     * {@code "simplestoragenh:"} + id.
     * Idempotent: the second and later calls do nothing.
     *
     * <p>
     * Config-safe: never reads config or module flags.
     * </p>
     */
    public static void register() {
        if (registered) {
            return;
        }
        registered = true;

        GameRegistry.registerTileEntity(FilingCabinetTileEntity.class, "simplestoragenh:" + "filing_cabinet");
        GameRegistry.registerTileEntity(OpenCrateTileEntity.class, "simplestoragenh:" + "open_crate");
        GameRegistry.registerTileEntity(FaceNodeTileEntity.class, "simplestoragenh:" + "face_node");
        GameRegistry.registerTileEntity(PaintedTileEntity.class, "simplestoragenh:" + "painted");
        GameRegistry.registerTileEntity(InventoryConnectorTileEntity.class, "simplestoragenh:" + "inventory_connector");
        GameRegistry.registerTileEntity(InventoryInterfaceTileEntity.class, "simplestoragenh:" + "inventory_interface");
        GameRegistry.registerTileEntity(LevelEmitterTileEntity.class, "simplestoragenh:" + "level_emitter");
        GameRegistry.registerTileEntity(InventoryHopperTileEntity.class, "simplestoragenh:" + "inventory_hopper");
    }
}
