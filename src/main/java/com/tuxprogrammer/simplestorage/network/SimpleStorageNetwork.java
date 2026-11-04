package com.tuxprogrammer.simplestorage.network;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.gtnewhorizon.gtnhlib.network.base.IPacket;
import com.gtnewhorizon.gtnhlib.network.base.NetworkChannel;
import com.tuxprogrammer.simplestorage.SimpleStorage;
import com.tuxprogrammer.simplestorage.network.validation.RateLimitedSink;
import com.tuxprogrammer.simplestorage.network.validation.RejectionSink;

/**
 * The mod's one network channel, {@code simplestoragenh} (docs/new-spec/01-core/index.md, "Network"). FML's default
 * version check applies: the mod is required on both sides.
 *
 * <p>
 * To
 * add one:
 * <ol>
 * <li>extend {@link ServerboundPacket} or {@link ClientboundPacket}, with a public no-argument constructor;</li>
 * <li>declare its rules in {@code validate}: dimension, reach, permission, every index and enum against its
 * bounds;</li>
 * <li>append one instance to {@link #serverboundPackets()} or {@link #clientboundPackets()}. Append only: the order is
 * the wire ID, and both sides must register the same lists in the same order;</li>
 * <li>add unit tests for its rules like those in {@code network/ServerboundPacketTest}.</li>
 * </ol>
 *
 * <p>
 * Source: GTNHLib 0.11.54,
 * https://github.com/GTNewHorizons/GTNHLib/blob/0.11.54/src/main/java/com/gtnewhorizon/gtnhlib/network/base/NetworkChannel.java#L26-L37
 * ({@code toClient} and {@code toServer} assign wire IDs in call order).
 *
 * <p>
 * Side: both. Opened in {@code preInit}.
 */
public final class SimpleStorageNetwork {

    /** The channel name; FML allows at most 20 characters. */
    public static final String CHANNEL_NAME = SimpleStorage.MODID;

    /** The most drop reports per window that reach the log. */
    private static final int REPORTS_PER_WINDOW = 20;

    /** The rate limiter's window. */
    private static final long WINDOW_NANOS = TimeUnit.SECONDS.toNanos(10);

    // A registry-like singleton for logging, not game state.
    private static final RejectionSink REJECTIONS = new RateLimitedSink(
        new LogRejectionSink(SimpleStorage.LOG),
        REPORTS_PER_WINDOW,
        WINDOW_NANOS,
        System::nanoTime);

    // Set once in preInit; the channel is a registry.
    private static NetworkChannel channel;

    private SimpleStorageNetwork() {}

    /**
     * Opens the channel and registers every packet: client-bound first, then server-bound, each in list order. Call
     * once, in {@code preInit}, on both sides.
     *
     * @throws IllegalStateException when called twice
     */
    public static synchronized void init() {
        if (channel != null) {
            throw new IllegalStateException("network channel " + CHANNEL_NAME + " is already open");
        }
        NetworkChannel created = new NetworkChannel(CHANNEL_NAME);
        for (IPacket packet : clientboundPackets()) {
            created.toClient(packet);
        }
        for (IPacket packet : serverboundPackets()) {
            created.toServer(packet);
        }
        channel = created;
        SimpleStorage.LOG.info(
            "Network channel {} open: {} client-bound and {} server-bound packets",
            CHANNEL_NAME,
            clientboundPackets().size(),
            serverboundPackets().size());
    }

    /**
     * Returns the channel, for sending.
     *
     * @throws IllegalStateException before {@link #init()}
     */
    public static NetworkChannel channel() {
        if (channel == null) {
            throw new IllegalStateException("network channel " + CHANNEL_NAME + " used before preInit");
        }
        return channel;
    }

    /** Returns the shared, rate-limited sink the packet base classes report drops to. */
    static RejectionSink rejections() {
        return REJECTIONS;
    }

    /** Returns one instance of each server-to-client packet, in wire order. Append only. */
    public static List<IPacket> clientboundPackets() {
        return Collections
            .unmodifiableList(Arrays.<IPacket>asList(new TerminalListPacket(), new TerminalStatePacket()));
    }

    /** Returns one instance of each client-to-server packet, in wire order. Append only. */
    public static List<IPacket> serverboundPackets() {
        return Collections.unmodifiableList(
            Arrays.<IPacket>asList(
                new TerminalActionPacket(),
                new TerminalSettingsPacket(),
                new FilingCabinetScrollPacket(),
                new EmitterSettingsPacket(),
                new FilterFlagsPacket(),
                new TagFilterPacket()));
    }
}
