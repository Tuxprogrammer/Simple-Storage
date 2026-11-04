package com.tuxprogrammer.simplestorage.inventory;
// Ported from Tom's Simple Storage (MIT):
// https://github.com/tom5454/Toms-Storage/blob/95bb61d/NeoForge/src/platform-shared/java/com/tom/storagemod/inventory/RemoteConnections.java
// Avoids: STOR-10, TERM-08

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * The registry of named inventory-link channels.
 *
 * <p>
 * Upstream bug STOR-10: an empty or over-long channel name fell back to
 * {@code "Channel " + System.currentTimeMillis()}, which is nondeterministic and
 * can collide. Here the fallback is {@code "Channel " + n} where n is the smallest
 * integer >= 1 not already used by an existing channel name, so names are
 * deterministic and unique.
 *
 * <p>
 * Upstream bug TERM-08: creating channels never checked name uniqueness, so a
 * typed name lookup could match several channels. A requested name that is
 * already used (trimmed, case-insensitive) gets the smallest free suffix,
 * {@code Name (2)}, {@code Name (3)}, ..., keeping channel names unique so
 * {@link #findByName(String, UUID)} is never ambiguous.
 *
 * <p>
 * Side: server logic, plain Java.
 */
public final class ChannelRegistry {

    /** Maximum length of a channel name; longer names trigger the numeric fallback. */
    public static final int MAX_NAME_LENGTH = 50;

    /** An immutable-ish named inventory-link channel. */
    public static final class Channel {

        private final UUID id;
        private final UUID owner;
        private final String ownerName;
        private final String name;
        private final boolean publicChannel;

        /**
         * Builds a channel. Intended for persistence (restore); see
         * {@link ChannelRegistry#channel(UUID, UUID, String, String, boolean)}.
         */
        public Channel(UUID id, UUID owner, String ownerName, String name, boolean publicChannel) {
            this.id = Objects.requireNonNull(id, "id");
            this.owner = Objects.requireNonNull(owner, "owner");
            this.ownerName = ownerName;
            this.name = name;
            this.publicChannel = publicChannel;
        }

        public UUID getId() {
            return id;
        }

        public UUID getOwner() {
            return owner;
        }

        public String getOwnerName() {
            return ownerName;
        }

        public String getName() {
            return name;
        }

        public boolean isPublicChannel() {
            return publicChannel;
        }

        /** True if the given player may access this channel (it is public, or they own it). */
        public boolean canAccess(UUID player) {
            return publicChannel || owner.equals(player);
        }

        @Override
        public String toString() {
            return "Channel[" + id + ", name=" + name + ", owner=" + owner + ", public=" + publicChannel + "]";
        }
    }

    /** Deterministic order: by name, then by id string. */
    private static final Comparator<Channel> CHANNEL_ORDER = Comparator
        .comparing(Channel::getName, Comparator.nullsLast(Comparator.naturalOrder()))
        .thenComparing(
            channel -> channel.getId()
                .toString());

    private final Supplier<UUID> idSupplier;
    private final List<Channel> channels = new ArrayList<Channel>();
    private boolean dirty;

    /** Creates a registry using {@link UUID#randomUUID()} for new channel ids. */
    public ChannelRegistry() {
        this(UUID::randomUUID);
    }

    /**
     * Creates a registry using the given supplier for new channel ids.
     *
     * @param idSupplier supplies fresh ids for {@link #makeChannel(String, boolean, UUID, String)}
     */
    public ChannelRegistry(Supplier<UUID> idSupplier) {
        this.idSupplier = Objects.requireNonNull(idSupplier, "idSupplier");
    }

    /**
     * Creates a channel. The name is trimmed; if the result is null or empty, the name
     * becomes {@code "Channel " + n} where n is the smallest integer >= 1 not already
     * used by an existing channel name. A name already used by an existing channel
     * (trimmed, case-insensitive) gets the smallest free suffix {@code " (n)"} (n >= 2),
     * and names longer than {@link #MAX_NAME_LENGTH} are shortened (with the suffix, if
     * any) to fit within it.
     *
     * @param name          desired name (may be null, empty, or too long)
     * @param publicChannel whether the channel is visible to everyone
     * @param owner         the owning player's UUID
     * @param ownerName     the owning player's display name
     * @return the created channel
     */
    public Channel makeChannel(String name, boolean publicChannel, UUID owner, String ownerName) {
        Objects.requireNonNull(owner, "owner");
        String trimmed = name == null ? null : name.trim();
        String finalName;
        // The 50-char limit applies BEFORE the uniqueness suffix: a name that
        // is empty after trimming or longer than MAX_NAME_LENGTH falls back to
        // the next free "Channel n".
        if (trimmed == null || trimmed.length() == 0 || trimmed.length() > MAX_NAME_LENGTH) {
            finalName = nextFallbackName();
        } else {
            finalName = uniqueName(trimmed);
        }
        Channel channel = new Channel(idSupplier.get(), owner, ownerName, finalName, publicChannel);
        channels.add(channel);
        dirty = true;
        return channel;
    }

    /** Removes a channel. Owner-only: only the owner may remove it. */
    public boolean removeChannel(UUID id, UUID player) {
        if (id == null) {
            return false;
        }
        for (int i = 0; i < channels.size(); i++) {
            Channel channel = channels.get(i);
            if (channel.getId()
                .equals(id)) {
                if (!channel.getOwner()
                    .equals(player)) {
                    return false;
                }
                channels.remove(i);
                dirty = true;
                return true;
            }
        }
        return false;
    }

    /** Edits a channel's public flag. Owner-only: only the owner may edit it. */
    public boolean editChannel(UUID id, boolean publicChannel, UUID player) {
        if (id == null) {
            return false;
        }
        for (int i = 0; i < channels.size(); i++) {
            Channel channel = channels.get(i);
            if (channel.getId()
                .equals(id)) {
                if (!channel.getOwner()
                    .equals(player)) {
                    return false;
                }
                if (channel.isPublicChannel() == publicChannel) {
                    return false;
                }
                channels.set(
                    i,
                    new Channel(
                        channel.getId(),
                        channel.getOwner(),
                        channel.getOwnerName(),
                        channel.getName(),
                        publicChannel));
                dirty = true;
                return true;
            }
        }
        return false;
    }

    /**
     * Finds the channel whose name equals the given name (trimmed, case-insensitive)
     * among the channels the given player can access.
     *
     * <p>
     * Channel names created via {@link #makeChannel(String, boolean, UUID, String)}
     * are unique, so the result is never ambiguous; returns null when no accessible
     * channel has that name.
     *
     * @param name   the typed name (may be null)
     * @param player the player performing the lookup
     * @return the matching channel, or null
     */
    public Channel findByName(String name, UUID player) {
        String wanted = name == null ? null : name.trim();
        if (wanted == null) {
            return null;
        }
        for (Channel channel : channels) {
            if (channel.canAccess(player) && wanted.equalsIgnoreCase(channel.getName())) {
                return channel;
            }
        }
        return null;
    }

    /** Returns the channel with the given id, or null. */
    public Channel get(UUID id) {
        if (id == null) {
            return null;
        }
        for (Channel channel : channels) {
            if (channel.getId()
                .equals(id)) {
                return channel;
            }
        }
        return null;
    }

    /** Returns the channels the given player can access, in deterministic order. */
    public List<Channel> streamChannels(UUID player) {
        List<Channel> result = new ArrayList<Channel>();
        for (Channel channel : channels) {
            if (channel.canAccess(player)) {
                result.add(channel);
            }
        }
        result.sort(CHANNEL_ORDER);
        return result;
    }

    /** Number of channels in the registry. */
    public int size() {
        return channels.size();
    }

    /** Whether the registry has changed since the last {@link #clearDirty()}. */
    public boolean isDirty() {
        return dirty;
    }

    /** Clears the dirty flag. */
    public void clearDirty() {
        dirty = false;
    }

    /** All channels in deterministic order (name, then id string), for persistence. */
    public List<Channel> snapshot() {
        List<Channel> copy = new ArrayList<Channel>(channels);
        copy.sort(CHANNEL_ORDER);
        return copy;
    }

    /**
     * Replaces all channels with the given list. Tolerant: skips null entries and
     * entries whose id duplicates an earlier kept entry.
     */
    public void restore(List<Channel> channels) {
        this.channels.clear();
        if (channels != null) {
            for (Channel channel : channels) {
                if (channel == null) {
                    continue;
                }
                boolean duplicate = false;
                for (Channel existing : this.channels) {
                    if (existing.getId()
                        .equals(channel.getId())) {
                        duplicate = true;
                        break;
                    }
                }
                if (!duplicate) {
                    this.channels.add(channel);
                }
            }
        }
        dirty = true;
    }

    /** Public factory for building a channel (used by restore/persistence). */
    public static Channel channel(UUID id, UUID owner, String ownerName, String name, boolean publicChannel) {
        return new Channel(id, owner, ownerName, name, publicChannel);
    }

    /**
     * Makes a channel name unique. If the trimmed name is already used (trimmed,
     * case-insensitive) by an existing channel, the smallest free suffix
     * {@code " (n)"} (n >= 2) is appended. The result stays within
     * {@link #MAX_NAME_LENGTH}; over-long names have their base shortened to make
     * room (over-long names without a collision are truncated outright).
     */
    private String uniqueName(String trimmed) {
        boolean taken = isNameTaken(trimmed);
        if (trimmed.length() > MAX_NAME_LENGTH) {
            if (!taken) {
                return trimmed.substring(0, MAX_NAME_LENGTH);
            }
            for (int n = 2;; n++) {
                String suffix = " (" + n + ")";
                String shortened = trimmed.substring(0, MAX_NAME_LENGTH - suffix.length()) + suffix;
                if (!isNameTaken(shortened)) {
                    return shortened;
                }
            }
        }
        if (!taken) {
            return trimmed;
        }
        for (int n = 2;; n++) {
            String candidate = trimmed + " (" + n + ")";
            if (!isNameTaken(candidate)) {
                return candidate;
            }
        }
    }

    /** True if some channel in the registry has a name equal to the given name (exact). */
    private boolean isNameTaken(String name) {
        for (Channel channel : channels) {
            if (name.equals(channel.getName())) {
                return true;
            }
        }
        return false;
    }

    /** Smallest {@code "Channel " + n} (n >= 1) not already used as an exact channel name. */
    private String nextFallbackName() {
        for (int n = 1;; n++) {
            String candidate = "Channel " + n;
            boolean taken = false;
            for (Channel channel : channels) {
                if (candidate.equals(channel.getName())) {
                    taken = true;
                    break;
                }
            }
            if (!taken) {
                return candidate;
            }
        }
    }
}
