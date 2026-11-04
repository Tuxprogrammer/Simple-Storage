package com.tuxprogrammer.simplestorage.network;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.network.PacketBuffer;

import com.tuxprogrammer.simplestorage.menu.ItemFilterContainer;
import com.tuxprogrammer.simplestorage.network.validation.Rules;

/**
 * A change to the item filter's flags: the window id plus the "matchNbt" and
 * "allowList" booleans.
 *
 * <p>
 * The window id is checked in {@link #handle(EntityPlayerMP)} against the
 * player's open container rather than in {@link #validate(Rules)}, for the
 * same reason as {@link TerminalSettingsPacket}: a stale flag change does
 * nothing instead of rejecting the packet. The flags are applied only when the
 * player's open container is an {@link ItemFilterContainer} with that window
 * id; any other container (or a closed one) leaves the filter untouched.
 *
 * <p>
 * Any player may send one, so the permission level is 0.
 *
 * <p>
 * Side: server (serverbound).
 */
public class FilterFlagsPacket extends ServerboundPacket {

    /** The window id of the filter being configured. */
    int windowId;

    /** The new value of the filter's "matchNbt" flag. */
    boolean matchNbt;

    /** The new value of the filter's "allowList" flag. */
    boolean allowList;

    /**
     * For the channel to decode into.
     */
    public FilterFlagsPacket() {}

    /**
     * Builds a flag change.
     *
     * @param windowId  the filter window id
     * @param matchNbt  the new value of "matchNbt"
     * @param allowList the new value of "allowList"
     */
    public FilterFlagsPacket(int windowId, boolean matchNbt, boolean allowList) {
        this.windowId = windowId;
        this.matchNbt = matchNbt;
        this.allowList = allowList;
    }

    @Override
    protected int requiredPermissionLevel() {
        return 0;
    }

    @Override
    public void encode(PacketBuffer buf) {
        buf.writeInt(windowId);
        buf.writeBoolean(matchNbt);
        buf.writeBoolean(allowList);
    }

    @Override
    public void decode(PacketBuffer buf) {
        windowId = buf.readInt();
        matchNbt = buf.readBoolean();
        allowList = buf.readBoolean();
    }

    @Override
    protected void validate(Rules rules) {
        rules.inRange("windowId", windowId, 0, 255);
    }

    @Override
    protected void handle(EntityPlayerMP player) {
        Container open = player.openContainer;
        if (open instanceof ItemFilterContainer && open.windowId == windowId) {
            ((ItemFilterContainer) open).applyFlags(matchNbt, allowList);
        }
        // A different or closed container is a stale change: do nothing.
    }
}
