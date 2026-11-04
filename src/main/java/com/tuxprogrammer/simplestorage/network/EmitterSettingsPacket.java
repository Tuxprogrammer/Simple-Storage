package com.tuxprogrammer.simplestorage.network;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.network.PacketBuffer;

import com.tuxprogrammer.simplestorage.menu.LevelEmitterContainer;
import com.tuxprogrammer.simplestorage.network.validation.Rules;

/**
 * A level emitter settings change: the window id, the threshold count, and the
 * comparison mode.
 *
 * <p>
 * The window id is checked in {@link #handle(EntityPlayerMP)} against the
 * player's open container rather than in {@link #validate(Rules)}: a stale
 * settings change does nothing instead of rejecting the packet.
 *
 * <p>
 * Any player may send one, so the permission level is 0.
 *
 * <p>
 * Side: server (serverbound).
 */
public class EmitterSettingsPacket extends ServerboundPacket {

    /** The window id of the emitter being configured. */
    int windowId;

    /** The new threshold count. */
    int count;

    /** The new comparison mode: true for "less than", false for "greater than". */
    boolean lessThan;

    /**
     * For the channel to decode into.
     */
    public EmitterSettingsPacket() {}

    /**
     * Builds a settings change.
     *
     * @param windowId the emitter window id
     * @param count    the new threshold
     * @param lessThan the new comparison mode
     */
    public EmitterSettingsPacket(int windowId, int count, boolean lessThan) {
        this.windowId = windowId;
        this.count = count;
        this.lessThan = lessThan;
    }

    /**
     * @return the emitter window id.
     */
    public int getWindowId() {
        return windowId;
    }

    /**
     * @return the new threshold count.
     */
    public int getCount() {
        return count;
    }

    /**
     * @return the new comparison mode.
     */
    public boolean isLessThan() {
        return lessThan;
    }

    @Override
    protected int requiredPermissionLevel() {
        return 0;
    }

    @Override
    public void encode(PacketBuffer buf) {
        buf.writeInt(windowId);
        buf.writeInt(count);
        buf.writeBoolean(lessThan);
    }

    @Override
    public void decode(PacketBuffer buf) {
        windowId = buf.readInt();
        count = buf.readInt();
        lessThan = buf.readBoolean();
    }

    @Override
    protected void validate(Rules rules) {
        rules.inRange("count", count, 0, 1073741823L)
            .inRange("windowId", windowId, 0, 255);
    }

    @Override
    protected void handle(EntityPlayerMP player) {
        Container open = player.openContainer;
        if (open instanceof LevelEmitterContainer && open.windowId == windowId) {
            ((LevelEmitterContainer) open).applySettings(count, lessThan);
        }
        // A different or closed container is a stale change: do nothing.
    }
}
