package com.tuxprogrammer.simplestorage.client;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

import com.tuxprogrammer.simplestorage.CommonProxy;
import com.tuxprogrammer.simplestorage.blockentity.CraftingTerminalTileEntity;
import com.tuxprogrammer.simplestorage.blockentity.FilingCabinetTileEntity;
import com.tuxprogrammer.simplestorage.blockentity.LevelEmitterTileEntity;
import com.tuxprogrammer.simplestorage.blockentity.StorageTerminalTileEntity;
import com.tuxprogrammer.simplestorage.blockentity.TerminalEntry;
import com.tuxprogrammer.simplestorage.client.screen.CraftingTerminalScreen;
import com.tuxprogrammer.simplestorage.client.screen.FilingCabinetScreen;
import com.tuxprogrammer.simplestorage.client.screen.ItemFilterScreen;
import com.tuxprogrammer.simplestorage.client.screen.LevelEmitterScreen;
import com.tuxprogrammer.simplestorage.client.screen.StorageTerminalScreen;
import com.tuxprogrammer.simplestorage.client.screen.TagFilterScreen;
import com.tuxprogrammer.simplestorage.gui.GuiIds;
import com.tuxprogrammer.simplestorage.menu.CraftingTerminalContainer;
import com.tuxprogrammer.simplestorage.menu.FilingCabinetContainer;
import com.tuxprogrammer.simplestorage.menu.ItemFilterContainer;
import com.tuxprogrammer.simplestorage.menu.LevelEmitterContainer;
import com.tuxprogrammer.simplestorage.menu.StorageTerminalContainer;
import com.tuxprogrammer.simplestorage.menu.TagFilterContainer;

/**
 * The client-side proxy: it builds the real client screens for
 * {@link #clientGui(int, EntityPlayer, World, int, int, int)}
 * and routes terminal list and state refreshes to the open storage terminal screen.
 *
 * <p>
 * Side: client (loaded only by the client side of the SidedProxy).
 */
public class ClientProxy extends CommonProxy {

    /**
     * Applies a terminal list refresh to the client by updating the open
     * storage terminal screen whose container window matches.
     *
     * @param windowId the terminal window id
     * @param entries  the visible entries
     */
    @Override
    public void onTerminalList(int windowId, List<TerminalEntry> entries) {
        GuiScreen s = Minecraft.getMinecraft().currentScreen;
        if (s instanceof StorageTerminalScreen && ((StorageTerminalScreen) s).getContainerWindowId() == windowId) {
            ((StorageTerminalScreen) s).setServerList(entries);
        }
    }

    /**
     * Applies a terminal state refresh to the client by updating the open
     * storage terminal screen whose container window matches.
     *
     * @param windowId   the terminal window id
     * @param sorting    the terminal's sorting order
     * @param modes      the terminal's enabled modes
     * @param searchType the terminal's search type
     * @param lastSearch the terminal's last search text
     */
    @Override
    public void onTerminalState(int windowId, int sorting, int modes, int searchType, String lastSearch) {
        GuiScreen s = Minecraft.getMinecraft().currentScreen;
        if (s instanceof StorageTerminalScreen && ((StorageTerminalScreen) s).getContainerWindowId() == windowId) {
            ((StorageTerminalScreen) s).setState(sorting, modes, searchType, lastSearch);
        }
    }

    /**
     * Returns the client screen to open for a gui id, or null for none.
     *
     * <p>
     * A missing tile entity (or one of the wrong kind) yields null, so the
     * player simply gets nothing for that click.
     *
     * @param guiId  the gui id, as assigned by the mod's GuiIds
     * @param player the player opening the screen
     * @param world  the world the screen is for
     * @param x      the block's x position
     * @param y      the block's y position
     * @param z      the block's z position
     * @return the client screen, or null
     */
    @Override
    public Object clientGui(int guiId, EntityPlayer player, World world, int x, int y, int z) {
        net.minecraft.tileentity.TileEntity te = world.getTileEntity(x, y, z);
        if (guiId == GuiIds.STORAGE_TERMINAL && te instanceof StorageTerminalTileEntity
            && !(te instanceof CraftingTerminalTileEntity)) {
            return new StorageTerminalScreen(
                new StorageTerminalContainer(player.inventory, (StorageTerminalTileEntity) te));
        }
        if (guiId == GuiIds.CRAFTING_TERMINAL && te instanceof CraftingTerminalTileEntity) {
            return new CraftingTerminalScreen(
                new CraftingTerminalContainer(player.inventory, (CraftingTerminalTileEntity) te));
        }
        if (guiId == GuiIds.FILING_CABINET && te instanceof FilingCabinetTileEntity) {
            return new FilingCabinetScreen(new FilingCabinetContainer(player.inventory, (FilingCabinetTileEntity) te));
        }
        if (guiId == GuiIds.LEVEL_EMITTER && te instanceof LevelEmitterTileEntity) {
            return new LevelEmitterScreen(new LevelEmitterContainer(player.inventory, (LevelEmitterTileEntity) te));
        }
        if (guiId == GuiIds.ITEM_FILTER) {
            return new ItemFilterScreen(new ItemFilterContainer(player.inventory));
        }
        if (guiId == GuiIds.TAG_FILTER) {
            return new TagFilterScreen(new TagFilterContainer(player.inventory));
        }
        return null;
    }
}
