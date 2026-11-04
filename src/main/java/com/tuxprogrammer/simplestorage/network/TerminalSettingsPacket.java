package com.tuxprogrammer.simplestorage.network;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.network.PacketBuffer;

import com.tuxprogrammer.simplestorage.menu.StorageTerminalContainer;
import com.tuxprogrammer.simplestorage.network.validation.Rules;

/**
 * A terminal settings change: the window id plus the sort mode, the terminal modes, and the search type, each as a
 * small bit field.
 *
 * <p>
 * The window id is again checked in {@link #handle(EntityPlayerMP)} against the player's open container rather than in
 * {@link #validate(Rules)}, for the same reason as {@link TerminalActionPacket}: a stale settings change does nothing
 * instead of rejecting the packet.
 *
 * <p>
 * The three settings are masked in the handler to the bits the game actually defines: sorting keeps 0x3FF, modes
 * 0x1F, and searchType 0xF, so a hostile client cannot smuggle undefined bits into the terminal's behaviour.
 *
 * <p>
 * Any player may send one, so the permission level is 0.
 *
 * <p>
 * Side: server (serverbound).
 */
public class TerminalSettingsPacket extends ServerboundPacket {

    /** The window id of the terminal being configured. */
    int windowId;

    /** The sort mode's defined bits. */
    int sorting;

    /** The terminal modes' defined bits. */
    int modes;

    /** The search type's defined bits. */
    int searchType;

    /**
     * For the channel to decode into.
     */
    public TerminalSettingsPacket() {}

    /**
     * Builds a settings change.
     *
     * @param windowId   the terminal window id
     * @param sorting    the sort mode
     * @param modes      the terminal modes
     * @param searchType the search type
     */
    public TerminalSettingsPacket(int windowId, int sorting, int modes, int searchType) {
        this.windowId = windowId;
        this.sorting = sorting;
        this.modes = modes;
        this.searchType = searchType;
    }

    @Override
    protected int requiredPermissionLevel() {
        return 0;
    }

    @Override
    public void encode(PacketBuffer buf) {
        buf.writeInt(windowId);
        buf.writeInt(sorting);
        buf.writeInt(modes);
        buf.writeInt(searchType);
    }

    @Override
    public void decode(PacketBuffer buf) {
        windowId = buf.readInt();
        sorting = buf.readInt();
        modes = buf.readInt();
        searchType = buf.readInt();
    }

    @Override
    protected void validate(Rules rules) {
        rules.inRange("windowId", windowId, 0, 255);
    }

    @Override
    protected void handle(EntityPlayerMP player) {
        Container open = player.openContainer;
        if (open instanceof StorageTerminalContainer && open.windowId == windowId) {
            // Keep only the defined bits: sorting 0x3FF, modes 0x1F, searchType 0xF.
            ((StorageTerminalContainer) open).applySettings(sorting & 0x3FF, modes & 0x1F, searchType & 0xF);
        }
        // A different or closed container is a stale change: do nothing.
    }
}
