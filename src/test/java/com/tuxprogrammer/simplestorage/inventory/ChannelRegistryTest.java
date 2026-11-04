package com.tuxprogrammer.simplestorage.inventory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.tuxprogrammer.simplestorage.inventory.ChannelRegistry.Channel;

/**
 * Tests for {@link ChannelRegistry}, focusing on the STOR-10 fallback-name fix
 * (deterministic {@code "Channel n"} names instead of wall-clock ids).
 */
public class ChannelRegistryTest {

    /** Three channels with empty, null and 51-character names get 'Channel 1'..3, timing-independent. */
    @Test
    public void doesNotReproduceSTOR10_emptyNameFallsBackToTheSmallestFreeNumber() {
        ChannelRegistry registry = new ChannelRegistry();
        Channel empty = registry.makeChannel("", true, UUID.randomUUID(), "alice");
        Channel nul = registry.makeChannel(null, true, UUID.randomUUID(), "bob");
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < 51; i++) {
            builder.append('x');
        }
        Channel longName = registry.makeChannel(builder.toString(), true, UUID.randomUUID(), "carol");

        assertEquals("Channel 1", empty.getName());
        assertEquals("Channel 2", nul.getName());
        assertEquals("Channel 3", longName.getName());
    }

    /** After removing 'Channel 2', the next fallback reuses 'Channel 2'. */
    @Test
    public void doesNotReproduceSTOR10_removedNumberIsReused() {
        ChannelRegistry registry = new ChannelRegistry();
        Channel one = registry.makeChannel("", true, UUID.randomUUID(), "alice");
        Channel two = registry.makeChannel("", true, UUID.randomUUID(), "bob");
        Channel three = registry.makeChannel("", true, UUID.randomUUID(), "carol");
        assertEquals("Channel 1", one.getName());
        assertEquals("Channel 2", two.getName());
        assertEquals("Channel 3", three.getName());

        assertTrue(registry.removeChannel(two.getId(), two.getOwner()));
        Channel next = registry.makeChannel("", true, UUID.randomUUID(), "dave");
        assertEquals("Channel 2", next.getName());
    }

    /** A valid name is kept as-is but trimmed of surrounding whitespace. */
    @Test
    public void validNameIsKeptTrimmed() {
        ChannelRegistry registry = new ChannelRegistry();
        Channel channel = registry.makeChannel("  Workbench  ", false, UUID.randomUUID(), "alice");
        assertEquals("Workbench", channel.getName());
    }

    /** A user-named channel colliding with a fallback name gets the suffixed variant. */
    @Test
    public void doesNotReproduceSTOR10_userNameCollidingWithFallbackGetsASuffix() {
        ChannelRegistry registry = new ChannelRegistry();
        UUID owner = UUID.randomUUID();
        registry.makeChannel("Channel 1", false, owner, "alice");
        Channel fallback = registry.makeChannel("", true, UUID.randomUUID(), "bob");
        assertEquals("Channel 2", fallback.getName());
        Channel renamed = registry.makeChannel("Channel 1", false, owner, "alice");
        assertEquals("Channel 1 (2)", renamed.getName());
    }

    /** Creating 'Base' three times yields suffixed names and findByName is unambiguous. */
    @Test
    public void doesNotReproduceTERM08_duplicateNamesGetASuffixSoLookupIsUnambiguous() {
        ChannelRegistry registry = new ChannelRegistry();
        UUID owner = UUID.randomUUID();
        Channel first = registry.makeChannel("Base", false, owner, "alice");
        Channel second = registry.makeChannel("Base", false, owner, "alice");
        Channel third = registry.makeChannel("Base", false, owner, "alice");
        assertEquals("Base", first.getName());
        assertEquals("Base (2)", second.getName());
        assertEquals("Base (3)", third.getName());

        assertEquals(
            first.getId(),
            registry.findByName("base", owner)
                .getId());
        assertEquals(
            second.getId(),
            registry.findByName("Base (2)", owner)
                .getId());
    }

    /** A private channel of another player is invisible to findByName. */
    @Test
    public void findByNameRespectsAccess() {
        ChannelRegistry registry = new ChannelRegistry();
        UUID owner = UUID.randomUUID();
        UUID stranger = UUID.randomUUID();
        Channel publicShared = registry.makeChannel("shared", true, owner, "alice");
        // Names are unique, so the stranger's private 'shared' is named 'shared (2)'.
        Channel privateShared = registry.makeChannel("shared", false, stranger, "bob");

        // The public 'shared' channel is visible to everyone.
        Channel found = registry.findByName("shared", owner);
        assertNotNull(found);
        assertEquals(publicShared.getId(), found.getId());
        assertEquals(owner, found.getOwner());
        Channel foundByStranger = registry.findByName("shared", stranger);
        assertNotNull(foundByStranger);
        assertEquals(publicShared.getId(), foundByStranger.getId());
        assertEquals(owner, foundByStranger.getOwner());

        // The stranger's private channel is findable by its unique name...
        Channel strangerFound = registry.findByName("shared (2)", stranger);
        assertNotNull(strangerFound);
        assertEquals(privateShared.getId(), strangerFound.getId());

        // ...but invisible to other players (it is not owner's public 'shared').
        assertNull(registry.findByName("shared (2)", owner));
    }

    /** Only the owner can remove or edit a channel; non-owners' attempts return false. */
    @Test
    public void onlyOwnerCanRemoveOrEdit() {
        ChannelRegistry registry = new ChannelRegistry();
        UUID owner = UUID.randomUUID();
        UUID other = UUID.randomUUID();
        Channel channel = registry.makeChannel("owned", false, owner, "alice");

        assertFalse(registry.removeChannel(channel.getId(), other));
        assertEquals(1, registry.size());

        assertFalse(registry.editChannel(channel.getId(), true, other));
        assertFalse(channel.isPublicChannel());

        assertTrue(registry.editChannel(channel.getId(), true, owner));
        assertTrue(
            registry.get(channel.getId())
                .isPublicChannel());

        assertTrue(registry.removeChannel(channel.getId(), owner));
        assertEquals(0, registry.size());
    }

    /** A public channel is accessible to any player; a private one only to its owner. */
    @Test
    public void publicChannelsAreVisibleToEveryone() {
        ChannelRegistry registry = new ChannelRegistry();
        UUID owner = UUID.randomUUID();
        UUID stranger = UUID.randomUUID();
        Channel pub = registry.makeChannel("pub", true, owner, "alice");
        Channel priv = registry.makeChannel("priv", false, owner, "alice");

        assertTrue(pub.canAccess(stranger));
        assertFalse(priv.canAccess(stranger));
        assertTrue(priv.canAccess(owner));

        List<Channel> strangerChannels = registry.streamChannels(stranger);
        assertEquals(1, strangerChannels.size());
        assertEquals(
            "pub",
            strangerChannels.get(0)
                .getName());

        List<Channel> ownerChannels = registry.streamChannels(owner);
        assertEquals(2, ownerChannels.size());
    }

    /** streamChannels returns only accessible channels, sorted by name then id string. */
    @Test
    public void streamChannelsIsSortedAndFiltered() {
        ChannelRegistry registry = new ChannelRegistry();
        UUID owner = UUID.randomUUID();
        UUID stranger = UUID.randomUUID();
        registry.makeChannel("zeta", false, owner, "alice");
        registry.makeChannel("alpha", true, owner, "alice");
        registry.makeChannel("beta", false, owner, "alice");

        List<Channel> ownerChannels = registry.streamChannels(owner);
        assertEquals(Arrays.asList("alpha", "beta", "zeta"), names(ownerChannels));

        List<Channel> strangerChannels = registry.streamChannels(stranger);
        assertEquals(Arrays.asList("alpha"), names(strangerChannels));
    }

    /** restore replaces everything, skipping nulls and duplicate ids. */
    @Test
    public void restoreSkipsDuplicatesAndNulls() {
        ChannelRegistry registry = new ChannelRegistry();
        UUID owner = UUID.randomUUID();
        UUID other = UUID.randomUUID();
        Channel a = registry.makeChannel("a", true, owner, "alice");
        assertEquals(1, registry.size());

        Channel b1 = ChannelRegistry.channel(UUID.randomUUID(), other, "bob", "b", true);
        Channel b2 = ChannelRegistry.channel(b1.getId(), other, "bob", "b-duplicate", true);
        List<Channel> data = new ArrayList<Channel>();
        data.add(a);
        data.add(null);
        data.add(b1);
        data.add(b2);
        data.add(null);

        registry.restore(data);
        assertEquals(2, registry.size());
        assertNotNull(registry.get(a.getId()));
        assertEquals(
            "b",
            registry.get(b1.getId())
                .getName());
        assertNull(registry.get(UUID.randomUUID()));
    }

    /** isDirty/clearDirty track make, remove and edit operations. */
    @Test
    public void dirtyFlagTracksChanges() {
        ChannelRegistry registry = new ChannelRegistry();
        assertFalse(registry.isDirty());

        UUID owner = UUID.randomUUID();
        Channel channel = registry.makeChannel("x", false, owner, "alice");
        assertTrue(registry.isDirty());
        registry.clearDirty();
        assertFalse(registry.isDirty());

        assertTrue(registry.editChannel(channel.getId(), true, owner));
        assertTrue(registry.isDirty());
        registry.clearDirty();
        assertFalse(registry.isDirty());

        assertTrue(registry.removeChannel(channel.getId(), owner));
        assertTrue(registry.isDirty());
        registry.clearDirty();
        assertFalse(registry.isDirty());
    }

    private static List<String> names(List<Channel> channels) {
        List<String> names = new ArrayList<String>();
        for (Channel channel : channels) {
            names.add(channel.getName());
        }
        return names;
    }
}
