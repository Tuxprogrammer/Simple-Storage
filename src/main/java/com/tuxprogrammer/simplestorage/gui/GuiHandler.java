package com.tuxprogrammer.simplestorage.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import com.tuxprogrammer.simplestorage.SimpleStorage;
import com.tuxprogrammer.simplestorage.blockentity.CraftingTerminalTileEntity;
import com.tuxprogrammer.simplestorage.blockentity.FilingCabinetTileEntity;
import com.tuxprogrammer.simplestorage.blockentity.LevelEmitterTileEntity;
import com.tuxprogrammer.simplestorage.blockentity.StorageTerminalTileEntity;
import com.tuxprogrammer.simplestorage.core.Module;
import com.tuxprogrammer.simplestorage.core.ModuleState;
import com.tuxprogrammer.simplestorage.item.ItemFilterItem;
import com.tuxprogrammer.simplestorage.item.TagFilterItem;
import com.tuxprogrammer.simplestorage.menu.CraftingTerminalContainer;
import com.tuxprogrammer.simplestorage.menu.FilingCabinetContainer;
import com.tuxprogrammer.simplestorage.menu.ItemFilterContainer;
import com.tuxprogrammer.simplestorage.menu.LevelEmitterContainer;
import com.tuxprogrammer.simplestorage.menu.StorageTerminalContainer;
import com.tuxprogrammer.simplestorage.menu.TagFilterContainer;

/**
 * The mod's gui handler: it turns a gui id and a block position into the container and screen to open. The server
 * side builds the containers, the client side defers to the proxy so the screens stay out of common code.
 *
 * <p>
 * A terminal that is missing, the wrong kind, or whose module is disabled yields null; the player then simply gets
 * nothing for that click.
 *
 * <p>
 * Side: both (the client element comes from the proxy).
 */
public class GuiHandler implements cpw.mods.fml.common.network.IGuiHandler {

    /**
     * Returns the container to open on the server, or null when the position does not hold a matching, enabled
     * terminal.
     *
     * @param id     the gui id, as assigned by the mod's GuiIds
     * @param player the player opening the screen
     * @param world  the world the screen is for
     * @param x      the block's x position
     * @param y      the block's y position
     * @param z      the block's z position
     * @return the container, or null
     */
    @Override
    public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        TileEntity te = world.getTileEntity(x, y, z);
        if (id == GuiIds.CRAFTING_TERMINAL && te instanceof CraftingTerminalTileEntity
            && ModuleState.configured()
                .isActive(Module.TERMINAL)) {
            return new CraftingTerminalContainer(player.inventory, (CraftingTerminalTileEntity) te);
        }
        if (id == GuiIds.STORAGE_TERMINAL && te instanceof StorageTerminalTileEntity
            && !(te instanceof CraftingTerminalTileEntity)
            && ModuleState.configured()
                .isActive(Module.TERMINAL)) {
            return new StorageTerminalContainer(player.inventory, (StorageTerminalTileEntity) te);
        }
        if (id == GuiIds.FILING_CABINET && te instanceof FilingCabinetTileEntity
            && ModuleState.configured()
                .isActive(Module.STORAGE)) {
            return new FilingCabinetContainer(player.inventory, (FilingCabinetTileEntity) te);
        }
        if (id == GuiIds.LEVEL_EMITTER && te instanceof LevelEmitterTileEntity
            && ModuleState.configured()
                .isActive(Module.NETWORK)) {
            return new LevelEmitterContainer(player.inventory, (LevelEmitterTileEntity) te);
        }
        if (id == GuiIds.ITEM_FILTER && player.inventory.getCurrentItem() != null
            && player.inventory.getCurrentItem()
                .getItem() instanceof ItemFilterItem
            && ModuleState.configured()
                .isActive(Module.TERMINAL)) {
            return new ItemFilterContainer(player.inventory);
        }
        if (id == GuiIds.TAG_FILTER && player.inventory.getCurrentItem() != null
            && player.inventory.getCurrentItem()
                .getItem() instanceof TagFilterItem
            && ModuleState.configured()
                .isActive(Module.TERMINAL)) {
            return new TagFilterContainer(player.inventory);
        }
        return null;
    }

    /**
     * Returns the screen to open on the client; the proxy builds the real screen, or null for none.
     *
     * @param id     the gui id, as assigned by the mod's GuiIds
     * @param player the player opening the screen
     * @param world  the world the screen is for
     * @param x      the block's x position
     * @param y      the block's y position
     * @param z      the block's z position
     * @return the client screen, or null
     */
    @Override
    public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        return SimpleStorage.proxy.clientGui(id, player, world, x, y, z);
    }
}
