package com.tuxprogrammer.simplestorage.network;

import java.io.IOException;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.network.PacketBuffer;

import com.tuxprogrammer.simplestorage.menu.StorageTerminalContainer;
import com.tuxprogrammer.simplestorage.network.validation.Rules;
import com.tuxprogrammer.simplestorage.terminal.SlotAction;

/**
 * A click from a terminal: the window id, the slot action (pull, push, shift-pull, ...), the target entry's key, and
 * whether the player held shift.
 *
 * <p>
 * The window id is the packet's way of proving the click is aimed at the sender's own terminal. It is checked in
 * {@link #handle(EntityPlayerMP)} rather than in {@link #validate(Rules)} because the rules cannot reach the player's
 * open container; a stale click (the container closed, or it is a different window) simply does nothing instead of
 * failing the packet, so closing a terminal while a click is in flight stays free of spurious rejections.
 *
 * <p>
 * Any player may send one, so the permission level is 0. The key length is bounded on the wire (max 512 characters)
 * and validated to be non-empty and within that bound, since it addresses an entry by identity.
 *
 * <p>
 * Side: server (serverbound).
 */
public class TerminalActionPacket extends ServerboundPacket {

    /** The longest entry key the wire will carry; validation requires at least 1 character. */
    public static final int MAX_KEY_LENGTH = 512;

    /** The window id of the terminal the click targets. */
    int windowId;

    /** The slot action's ordinal; {@link SlotAction#fromOrdinal(int)} maps it back in the handler. */
    int action;

    /** The target entry's key; empty until decoded. */
    String key;

    /** Whether the player held shift. */
    boolean shift;

    /**
     * For the channel to decode into.
     */
    public TerminalActionPacket() {
        this.key = "";
    }

    /**
     * Builds a terminal click.
     *
     * @param windowId the terminal window id
     * @param action   the slot action performed
     * @param key      the target entry's key
     * @param shift    whether shift was held
     */
    public TerminalActionPacket(int windowId, SlotAction action, String key, boolean shift) {
        this.windowId = windowId;
        this.action = action.ordinal();
        this.key = key;
        this.shift = shift;
    }

    @Override
    protected int requiredPermissionLevel() {
        return 0;
    }

    @Override
    public void encode(PacketBuffer buf) throws IOException {
        buf.writeInt(windowId);
        buf.writeByte(action);
        buf.writeStringToBuffer(key);
        buf.writeBoolean(shift);
    }

    @Override
    public void decode(PacketBuffer buf) throws IOException {
        windowId = buf.readInt();
        action = buf.readByte();
        key = buf.readStringFromBuffer(MAX_KEY_LENGTH);
        shift = buf.readBoolean();
    }

    @Override
    protected void validate(Rules rules) {
        rules.inRange("action", action, 0, SlotAction.values().length - 1)
            .inRange("keyLength", key.length(), 1, MAX_KEY_LENGTH);
    }

    @Override
    protected void handle(EntityPlayerMP player) {
        Container open = player.openContainer;
        if (open instanceof StorageTerminalContainer && open.windowId == windowId) {
            ((StorageTerminalContainer) open).handleAction(player, SlotAction.fromOrdinal(action), key, shift);
        }
        // Anything else is a stale click: the container closed or the window moved on, so do nothing.
    }
}
