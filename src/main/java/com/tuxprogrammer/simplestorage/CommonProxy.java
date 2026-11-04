package com.tuxprogrammer.simplestorage;

import com.tuxprogrammer.simplestorage.blockentity.TerminalEntry;
import com.tuxprogrammer.simplestorage.config.SimpleStorageConfig;
import com.tuxprogrammer.simplestorage.core.DevSelfCheck;
import com.tuxprogrammer.simplestorage.core.InGameHooks;
import com.tuxprogrammer.simplestorage.core.Registration;
import com.tuxprogrammer.simplestorage.gui.GuiHandler;
import com.tuxprogrammer.simplestorage.network.SimpleStorageNetwork;

import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.network.NetworkRegistry;

public class CommonProxy {

    // preInit "Run before anything else. Read your config, create blocks, items, etc, and register them with the
    // GameRegistry." (Remove if not needed)
    public void preInit(FMLPreInitializationEvent event) {
        SimpleStorageConfig.register();
        SimpleStorageNetwork.init();
        Registration.registerAll();
        SimpleStorage.LOG.info("Simple Storage version " + Tags.VERSION);
    }

    // load "Do your mod setup. Build whatever data structures you care about. Register recipes." (Remove if not needed)
    public void init(FMLInitializationEvent event) {
        NetworkRegistry.INSTANCE.registerGuiHandler(SimpleStorage.instance, new GuiHandler());
    }

    /**
     * Applies a terminal list refresh to the client; the list of entries the terminal shows, for the window that
     * opened it.
     *
     * @param windowId the terminal window id
     * @param entries  the visible entries
     */
    public void onTerminalList(int windowId, java.util.List<TerminalEntry> entries) {}

    /**
     * Applies a terminal state refresh to the client: the sorting order, the enabled modes, the search type and the
     * last search text, for the window that opened the terminal.
     *
     * @param windowId   the terminal window id
     * @param sorting    the terminal's sorting order
     * @param modes      the terminal's enabled modes
     * @param searchType the terminal's search type
     * @param lastSearch the terminal's last search text
     */
    public void onTerminalState(int windowId, int sorting, int modes, int searchType, String lastSearch) {}

    /**
     * Returns the client screen to open for a gui id, or null for none.
     *
     * <p>
     * The client proxy overrides this to build the real screens; the common implementation returns null so common
     * code never names a client class.
     *
     * @param guiId  the gui id, as assigned by the mod's GuiIds
     * @param player the player opening the screen
     * @param world  the world the screen is for
     * @param x      the block's x position
     * @param y      the block's y position
     * @param z      the block's z position
     * @return the client screen, or null
     */
    public Object clientGui(int guiId, net.minecraft.entity.player.EntityPlayer player, net.minecraft.world.World world,
        int x, int y, int z) {
        return null;
    }

    // postInit "Handle interaction with other mods, complete your setup based on this." (Remove if not needed)
    public void postInit(FMLPostInitializationEvent event) {
        DevSelfCheck.run();
    }

    // register server commands in this event handler (Remove if not needed)
    public void serverStarting(FMLServerStartingEvent event) {
        InGameHooks.registerCommand(event);
    }
}
