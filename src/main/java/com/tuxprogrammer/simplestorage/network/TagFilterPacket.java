package com.tuxprogrammer.simplestorage.network;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.network.PacketBuffer;

import com.tuxprogrammer.simplestorage.menu.TagFilterContainer;
import com.tuxprogrammer.simplestorage.network.validation.Rules;

/**
 * A change to the tag filter's settings: the window id, the tag names (joined
 * with newlines), and the "matchAll" and "allowList" booleans.
 *
 * <p>
 * The window id is checked in {@link #handle(EntityPlayerMP)} against the
 * player's open container rather than in {@link #validate(Rules)}, for the
 * same reason as {@link FilterFlagsPacket}: a stale settings change does
 * nothing instead of rejecting the packet. The settings are applied only when
 * the player's open container is a {@link TagFilterContainer} with that window
 * id; any other container (or a closed one) leaves the filter untouched.
 *
 * <p>
 * Any player may send one, so the permission level is 0.
 *
 * <p>
 * Side: server (serverbound).
 */
public class TagFilterPacket extends ServerboundPacket {

    /** The window id of the filter being configured. */
    int windowId;

    /** The tag names, joined with newlines. */
    String names;

    /** The new value of the filter's "matchAll" flag. */
    boolean matchAll;

    /** The new value of the filter's "allowList" flag. */
    boolean allowList;

    /**
     * For the channel to decode into.
     */
    public TagFilterPacket() {}

    /**
     * Builds a tag-filter settings change.
     *
     * @param windowId  the filter window id
     * @param names     the tag names, joined with newlines
     * @param matchAll  the new value of "matchAll"
     * @param allowList the new value of "allowList"
     */
    public TagFilterPacket(int windowId, String names, boolean matchAll, boolean allowList) {
        this.windowId = windowId;
        this.names = names;
        this.matchAll = matchAll;
        this.allowList = allowList;
    }

    @Override
    protected int requiredPermissionLevel() {
        return 0;
    }

    @Override
    public void encode(PacketBuffer buf) throws IOException {
        buf.writeInt(windowId);
        buf.writeStringToBuffer(names);
        buf.writeBoolean(matchAll);
        buf.writeBoolean(allowList);
    }

    @Override
    public void decode(PacketBuffer buf) throws IOException {
        windowId = buf.readInt();
        names = buf.readStringFromBuffer(1024);
        matchAll = buf.readBoolean();
        allowList = buf.readBoolean();
    }

    @Override
    protected void validate(Rules rules) {
        rules.inRange("windowId", windowId, 0, 255)
            .inRange("names", names.length(), 0, 1024);
    }

    @Override
    protected void handle(EntityPlayerMP player) {
        Container open = player.openContainer;
        if (open instanceof TagFilterContainer && open.windowId == windowId) {
            List<String> split = new ArrayList<String>();
            String[] parts = names.split("\n", -1);
            for (String part : parts) {
                split.add(part);
            }
            ((TagFilterContainer) open).applySettings(split, matchAll, allowList);
        }
        // A different or closed container is a stale change: do nothing.
    }
}
