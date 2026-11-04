package com.tuxprogrammer.simplestorage;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.tuxprogrammer.simplestorage.core.InGameHooks;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartedEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;

@Mod(
    modid = SimpleStorage.MODID,
    version = Tags.VERSION,
    name = "Simple Storage",
    acceptedMinecraftVersions = "[1.7.10]",
    dependencies = SimpleStorage.DEPENDENCIES)
public class SimpleStorage {

    public static final String MODID = "simplestoragenh";

    /**
     * Load order and hard dependencies (D17): GregTech and GTNHLib must be present, so a server without them fails at
     * load with FML's missing-dependency message. NEI, Waila and AE2 are optional and only ordered before this mod.
     */
    public static final String DEPENDENCIES = "required-after:gregtech;required-after:gtnhlib;"
        + "after:NotEnoughItems;after:Waila;after:appliedenergistics2";
    public static final Logger LOG = LogManager.getLogger(MODID);

    /**
     * The mod's main class instance, injected by FML so other classes can reach mod state.
     */
    @Mod.Instance(MODID)
    public static SimpleStorage instance;

    /**
     * Opens one of this mod's GUIs; ids are in simplestorage.gui.GuiIds. Server side only.
     */
    public static void openGui(EntityPlayer player, int guiId, World world, int x, int y, int z) {
        player.openGui(instance, guiId, world, x, y, z);
    }

    @SidedProxy(
        clientSide = "com.tuxprogrammer.simplestorage.client.ClientProxy",
        serverSide = "com.tuxprogrammer.simplestorage.CommonProxy")
    public static CommonProxy proxy;

    @Mod.EventHandler
    // preInit "Run before anything else. Read your config, create blocks, items, etc, and register them with the
    // GameRegistry." (Remove if not needed)
    public void preInit(FMLPreInitializationEvent event) {
        proxy.preInit(event);
    }

    @Mod.EventHandler
    // load "Do your mod setup. Build whatever data structures you care about. Register recipes." (Remove if not needed)
    public void init(FMLInitializationEvent event) {
        proxy.init(event);
    }

    @Mod.EventHandler
    // postInit "Handle interaction with other mods, complete your setup based on this." (Remove if not needed)
    public void postInit(FMLPostInitializationEvent event) {
        proxy.postInit(event);
    }

    @Mod.EventHandler
    // register server commands in this event handler (Remove if not needed)
    public void serverStarting(FMLServerStartingEvent event) {
        proxy.serverStarting(event);
    }

    @Mod.EventHandler
    public void serverStarted(FMLServerStartedEvent event) {
        InGameHooks.autoRun();
    }
}
