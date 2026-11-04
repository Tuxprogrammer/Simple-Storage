package com.tuxprogrammer.simplestorage.network;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.network.PacketBuffer;

import com.tuxprogrammer.simplestorage.menu.FilingCabinetContainer;
import com.tuxprogrammer.simplestorage.network.validation.Rules;

/**
 * A filing cabinet scroll change: the window id plus the new row to scroll to.
 *
 * <p>
 * The window id is checked in {@link #handle(EntityPlayerMP)} against the player's open container rather than in
 * {@link #validate(Rules)}: a stale scroll (the cabinet was closed in the meantime) does nothing instead of
 * rejecting the packet.
 *
 * <p>
 * The row is clamped again inside {@link FilingCabinetContainer#setScrollRow(int)}, so a hostile client sending a
 * row past the last scrollable row cannot scroll the window out of range.
 *
 * <p>
 * Any player may send one, so the permission level is 0.
 *
 * <p>
 * Side: server (serverbound).
 */
public class FilingCabinetScrollPacket extends ServerboundPacket {

    /** The window id of the cabinet being scrolled. */
    int windowId;

    /** The row to scroll to. */
    int row;

    /**
     * For the channel to decode into.
     */
    public FilingCabinetScrollPacket() {}

    /**
     * Builds a scroll change.
     *
     * @param windowId the cabinet window id
     * @param row      the row to scroll to
     */
    public FilingCabinetScrollPacket(int windowId, int row) {
        this.windowId = windowId;
        this.row = row;
    }

    @Override
    protected int requiredPermissionLevel() {
        return 0;
    }

    @Override
    public void encode(PacketBuffer buf) {
        buf.writeInt(windowId);
        buf.writeInt(row);
    }

    @Override
    public void decode(PacketBuffer buf) {
        windowId = buf.readInt();
        row = buf.readInt();
    }

    @Override
    protected void validate(Rules rules) {
        rules.inRange("row", row, 0, 52);
    }

    @Override
    protected void handle(EntityPlayerMP player) {
        Container open = player.openContainer;
        if (open instanceof FilingCabinetContainer && open.windowId == windowId) {
            ((FilingCabinetContainer) open).setScrollRow(row);
        }
        // A different or closed container is a stale scroll: do nothing.
    }
}
