package com.tuxprogrammer.simplestorage.network;

import java.io.IOException;

import net.minecraft.network.PacketBuffer;

import com.tuxprogrammer.simplestorage.SimpleStorage;
import com.tuxprogrammer.simplestorage.network.validation.Rules;

/**
 * The server-to-client payload that refreshes a terminal's non-list state: the sorting order, the enabled modes, the
 * search type and the last search text. It carries the window id that opened the terminal so the client can apply the
 * state to the right screen.
 *
 * <p>
 * The wire format is deliberately minimal: {@code windowId} (int), {@code sorting} (int), {@code modes} (int),
 * {@code searchType} (int), then {@code lastSearch} (string). The client applies the state as-is; the server is
 * expected to send only values the terminal UI understands.
 *
 * <p>
 * Side: client (handler), server (sender).
 */
public class TerminalStatePacket extends ClientboundPacket {

    /** The window id of the terminal that owns this state. */
    int windowId;

    /** The terminal's sorting order. */
    int sorting;

    /** The terminal's enabled modes. */
    int modes;

    /** The terminal's search type. */
    int searchType;

    /** The terminal's last search text; may be empty. */
    String lastSearch;

    /**
     * For the channel to decode into.
     */
    public TerminalStatePacket() {
        this.lastSearch = "";
    }

    /**
     * Builds a state refresh for a terminal.
     *
     * @param windowId   the terminal window id
     * @param sorting    the terminal's sorting order
     * @param modes      the terminal's enabled modes
     * @param searchType the terminal's search type
     * @param lastSearch the terminal's last search text; may be empty
     */
    public TerminalStatePacket(int windowId, int sorting, int modes, int searchType, String lastSearch) {
        this.windowId = windowId;
        this.sorting = sorting;
        this.modes = modes;
        this.searchType = searchType;
        this.lastSearch = lastSearch;
    }

    @Override
    public void encode(PacketBuffer buf) throws IOException {
        buf.writeInt(windowId);
        buf.writeInt(sorting);
        buf.writeInt(modes);
        buf.writeInt(searchType);
        buf.writeStringToBuffer(lastSearch);
    }

    @Override
    public void decode(PacketBuffer buf) throws IOException {
        windowId = buf.readInt();
        sorting = buf.readInt();
        modes = buf.readInt();
        searchType = buf.readInt();
        lastSearch = buf.readStringFromBuffer(256);
    }

    @Override
    protected void validate(Rules rules) {
        rules.inRange("windowId", windowId, 0, 255);
    }

    @Override
    protected void handleClient() {
        SimpleStorage.proxy.onTerminalState(windowId, sorting, modes, searchType, lastSearch);
    }
}
