package com.tuxprogrammer.simplestorage.block;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import cpw.mods.fml.common.registry.GameRegistry;

/**
 * Central block registration for Simple Storage (Minecraft 1.7.10 / Forge 10.13.4.1614).
 *
 * <p>
 * Registration follows the config-safe-registration contract: {@link #register()}
 * NEVER reads config or module flags, so every block in {@link #NAMES} always exists
 * after preInit, regardless of the current configuration.
 * </p>
 *
 * <p>
 * {@link #register()} is idempotent: a second call does nothing.
 * </p>
 *
 * <p>
 * Side: both.
 * </p>
 */
public final class ModBlocks {

    /**
     * Canonical block registration names, in registration order.
     * Unmodifiable.
     */
    public static final List<String> NAMES = Collections.unmodifiableList(
        Arrays.asList(
            "filing_cabinet",
            "open_crate",
            "inventory_proxy",
            "trim",
            "painted_trim",
            "inventory_cable",
            "inventory_cable_framed",
            "inventory_cable_connector",
            "inventory_cable_connector_framed",
            "inventory_connector",
            "inventory_interface",
            "level_emitter",
            "inventory_hopper"));

    public static FilingCabinetBlock filingCabinet;
    public static OpenCrateBlock openCrate;
    public static InventoryProxyBlock inventoryProxy;
    public static TrimBlock trim;
    public static PaintedTrimBlock paintedTrim;
    public static InventoryCableBlock inventoryCable;
    public static FramedInventoryCableBlock inventoryCableFramed;
    public static InventoryCableConnectorBlock inventoryCableConnector;
    public static FramedInventoryCableConnectorBlock inventoryCableConnectorFramed;
    public static InventoryConnectorBlock inventoryConnector;
    public static InventoryInterfaceBlock inventoryInterface;
    public static LevelEmitterBlock levelEmitter;
    public static InventoryHopperBlock inventoryHopper;

    private static boolean registered;

    private ModBlocks() {}

    /**
     * Instantiates and registers all mod blocks with the Forge
     * {@link GameRegistry#registerBlock(Block, String)} API, using each
     * block's {@code registryName()} as the registration name.
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

        filingCabinet = new FilingCabinetBlock();
        openCrate = new OpenCrateBlock();
        inventoryProxy = new InventoryProxyBlock();
        trim = new TrimBlock();
        paintedTrim = new PaintedTrimBlock();
        inventoryCable = new InventoryCableBlock();
        inventoryCableFramed = new FramedInventoryCableBlock();
        inventoryCableConnector = new InventoryCableConnectorBlock();
        inventoryCableConnectorFramed = new FramedInventoryCableConnectorBlock();
        inventoryConnector = new InventoryConnectorBlock();
        inventoryInterface = new InventoryInterfaceBlock();
        levelEmitter = new LevelEmitterBlock();
        inventoryHopper = new InventoryHopperBlock();

        GameRegistry.registerBlock(filingCabinet, filingCabinet.registryName());
        GameRegistry.registerBlock(openCrate, openCrate.registryName());
        GameRegistry.registerBlock(inventoryProxy, inventoryProxy.registryName());
        GameRegistry.registerBlock(trim, trim.registryName());
        GameRegistry.registerBlock(paintedTrim, paintedTrim.registryName());
        GameRegistry.registerBlock(inventoryCable, inventoryCable.registryName());
        GameRegistry.registerBlock(inventoryCableFramed, inventoryCableFramed.registryName());
        GameRegistry.registerBlock(inventoryCableConnector, inventoryCableConnector.registryName());
        GameRegistry.registerBlock(inventoryCableConnectorFramed, inventoryCableConnectorFramed.registryName());
        GameRegistry.registerBlock(inventoryConnector, inventoryConnector.registryName());
        GameRegistry.registerBlock(inventoryInterface, inventoryInterface.registryName());
        GameRegistry.registerBlock(levelEmitter, levelEmitter.registryName());
        GameRegistry.registerBlock(inventoryHopper, inventoryHopper.registryName());
    }

    /**
     * @return the number of block fields that have been assigned (non-null).
     */
    public static int registeredCount() {
        int count = 0;
        if (filingCabinet != null) count++;
        if (openCrate != null) count++;
        if (inventoryProxy != null) count++;
        if (trim != null) count++;
        if (paintedTrim != null) count++;
        if (inventoryCable != null) count++;
        if (inventoryCableFramed != null) count++;
        if (inventoryCableConnector != null) count++;
        if (inventoryCableConnectorFramed != null) count++;
        if (inventoryConnector != null) count++;
        if (inventoryInterface != null) count++;
        if (levelEmitter != null) count++;
        if (inventoryHopper != null) count++;
        return count;
    }
}
