package com.tuxprogrammer.simplestorage.network;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import net.minecraft.network.PacketBuffer;

import org.junit.jupiter.api.Test;

import com.tuxprogrammer.simplestorage.network.validation.FakeSender;
import com.tuxprogrammer.simplestorage.network.validation.PacketOutcome;
import com.tuxprogrammer.simplestorage.network.validation.RecordingSink;
import com.tuxprogrammer.simplestorage.network.validation.Rejection;
import com.tuxprogrammer.simplestorage.terminal.SlotAction;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

/**
 * The terminal's server-bound packets round-trip their bytes and reject malformed actions and keys before the handler
 * runs. This class lives in the network package so it can reach the packets' protected members.
 */
public class TerminalPacketsTest {

    /**
     * Encodes a terminal click into a fresh buffer, decodes it into a new packet, and expects the same fields.
     */
    @Test
    public void actionPacketRoundTrips() throws IOException {
        TerminalActionPacket original = new TerminalActionPacket(
            7,
            SlotAction.SHIFT_PULL,
            "minecraft:iron_ingot@0#",
            true);
        ByteBuf bytes = Unpooled.buffer();
        original.encode(new PacketBuffer(bytes));

        TerminalActionPacket decoded = new TerminalActionPacket();
        decoded.decode(new PacketBuffer(bytes));

        assertEquals(original.windowId, decoded.windowId);
        assertEquals(original.action, decoded.action);
        assertEquals(original.key, decoded.key);
        assertEquals(original.shift, decoded.shift);
    }

    /**
     * Encodes a settings change into a fresh buffer, decodes it into a new packet, and expects the same fields.
     */
    @Test
    public void settingsPacketRoundTrips() throws IOException {
        TerminalSettingsPacket original = new TerminalSettingsPacket(3, 5, 2, 1);
        ByteBuf bytes = Unpooled.buffer();
        original.encode(new PacketBuffer(bytes));

        TerminalSettingsPacket decoded = new TerminalSettingsPacket();
        decoded.decode(new PacketBuffer(bytes));

        assertEquals(original.windowId, decoded.windowId);
        assertEquals(original.sorting, decoded.sorting);
        assertEquals(original.modes, decoded.modes);
        assertEquals(original.searchType, decoded.searchType);
    }

    /**
     * A slot action ordinal that no enum constant owns fails the action rule, so the pipeline rejects the packet and
     * never runs the handler.
     */
    @Test
    public void actionPacketRejectsAnOutOfRangeAction() {
        TerminalActionPacket p = new TerminalActionPacket();
        p.windowId = 0;
        p.action = 99;
        p.key = "minecraft:iron_ingot@0#";
        p.shift = false;
        Run run = dispatch(p, FakeSender.player());
        assertEquals(PacketOutcome.Kind.REJECTED, run.outcome.kind(), run.outcome.toString());
        assertEquals(Rejection.OUT_OF_RANGE, run.outcome.reason(), run.outcome.toString());
        assertFalse(run.handled.get(), "the handler must not run for a rejected packet");
    }

    /**
     * An empty entry key fails the keyLength rule, so the pipeline rejects the packet and never runs the handler.
     */
    @Test
    public void actionPacketRejectsAnEmptyKey() {
        TerminalActionPacket p = new TerminalActionPacket();
        p.windowId = 0;
        p.action = 0;
        p.key = "";
        p.shift = false;
        Run run = dispatch(p, FakeSender.player());
        assertEquals(PacketOutcome.Kind.REJECTED, run.outcome.kind(), run.outcome.toString());
        assertEquals(Rejection.OUT_OF_RANGE, run.outcome.reason(), run.outcome.toString());
        assertFalse(run.handled.get(), "the handler must not run for a rejected packet");
    }

    /**
     * The slot action ordinals are part of the wire format, so they must stay fixed: PULL_OR_PUSH_STACK is 0,
     * SHIFT_PULL is 4, and the two neighbours of the range come back null from {@link SlotAction#fromOrdinal(int)}.
     */
    @Test
    public void slotActionOrdinalsAreStable() {
        assertEquals(0, SlotAction.PULL_OR_PUSH_STACK.ordinal());
        assertEquals(4, SlotAction.SHIFT_PULL.ordinal());
        assertNull(SlotAction.fromOrdinal(5));
        assertNull(SlotAction.fromOrdinal(-1));
    }

    private static Run dispatch(TerminalActionPacket packet, FakeSender sender) {
        Run run = new Run();
        run.outcome = packet.dispatch(sender, () -> run.handled.set(true), run.sink);
        return run;
    }

    /** One dispatch's observations. */
    private static final class Run {

        final AtomicBoolean handled = new AtomicBoolean();
        final RecordingSink sink = new RecordingSink();
        PacketOutcome outcome;
    }
}
