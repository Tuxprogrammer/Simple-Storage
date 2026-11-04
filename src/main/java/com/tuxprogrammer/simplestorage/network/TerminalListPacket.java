package com.tuxprogrammer.simplestorage.network;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;

import com.tuxprogrammer.simplestorage.SimpleStorage;
import com.tuxprogrammer.simplestorage.blockentity.StackKeys;
import com.tuxprogrammer.simplestorage.blockentity.TerminalEntry;
import com.tuxprogrammer.simplestorage.network.validation.Rules;

/**
 * The server-to-client payload that refreshes a terminal's list. It carries the window id that opened the terminal
 * and every entry in the visible view: the prototype stack and its count.
 *
 * <p>
 * The wire format is deliberately minimal: {@code windowId} (int), the entry count (int), then, for every entry, the
 * prototype stack followed by its count (long). The entry key is not sent; the client rebuilds it from the prototype
 * with {@link StackKeys#keyOf(ItemStack)}, so both sides agree without a second source of truth.
 *
 * <p>
 * Decoding is defensive: a count above 4096 entries would mean a hostile server owns the client's memory, a missing
 * stack or a negative count means the bytes were cut or forged, and each throws so the base class drops the packet as
 * malformed. The constructor likewise truncates oversized lists to the first 4096 entries, so the encode and the
 * {@code entries} rule always agree.
 *
 * <p>
 * Side: client (clientbound).
 */
public class TerminalListPacket extends ClientboundPacket {

    /** The most entries a single list packet may carry; beyond this the sender truncates. */
    public static final int MAX_ENTRIES = 4096;

    /** The window id of the terminal that owns this list. */
    int windowId;

    /** The entries the terminal shows; the keys are rebuilt on the client from the prototypes. */
    List<TerminalEntry> entries;

    /**
     * For the channel to decode into.
     */
    public TerminalListPacket() {
        this.entries = new ArrayList<TerminalEntry>();
    }

    /**
     * Builds a list refresh for a terminal.
     *
     * @param windowId the terminal window id
     * @param entries  the visible entries; at most the first 4096 are kept, the rest truncated
     */
    public TerminalListPacket(int windowId, List<TerminalEntry> entries) {
        this.windowId = windowId;
        List<TerminalEntry> kept = new ArrayList<TerminalEntry>();
        for (TerminalEntry entry : entries) {
            if (kept.size() >= MAX_ENTRIES) {
                break;
            }
            kept.add(entry);
        }
        this.entries = kept;
    }

    @Override
    public void encode(PacketBuffer buf) throws IOException {
        buf.writeInt(windowId);
        buf.writeInt(entries.size());
        for (TerminalEntry entry : entries) {
            buf.writeItemStackToBuffer(entry.prototype());
            buf.writeLong(entry.count());
        }
    }

    @Override
    public void decode(PacketBuffer buf) throws IOException {
        windowId = buf.readInt();
        int size = buf.readInt();
        if (size > MAX_ENTRIES) {
            throw new IOException("too many entries");
        }
        List<TerminalEntry> decoded = new ArrayList<TerminalEntry>(size);
        for (int i = 0; i < size; i++) {
            ItemStack stack = buf.readItemStackFromBuffer();
            if (stack == null) {
                throw new IOException("missing prototype stack");
            }
            long count = buf.readLong();
            if (count < 0L) {
                throw new IOException("negative count");
            }
            decoded.add(new TerminalEntry(StackKeys.keyOf(stack), stack, count));
        }
        entries = decoded;
    }

    @Override
    protected void validate(Rules rules) {
        rules.inRange("entries", entries.size(), 0, MAX_ENTRIES);
    }

    @Override
    protected void handleClient() {
        SimpleStorage.proxy.onTerminalList(windowId, entries);
    }
}
