package com.tuxprogrammer.simplestorage.menu;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.tuxprogrammer.simplestorage.blockentity.LevelEmitterTileEntity;
import com.tuxprogrammer.simplestorage.network.EmitterSettingsPacket;

import io.netty.buffer.Unpooled;

/**
 * In-game tests for {@link LevelEmitterContainer} and {@link EmitterSettingsPacket}: the ghost filter slot, the
 * settings clamping, the packet round trip, and the interact guard.
 */
@Tag("ingame")
public class LevelEmitterContainerTest {

    /**
     * Boots the item registry and grabs a fake player (and its inventory) for the tests.
     */
    @BeforeAll
    public static void bootstrap() {
        net.minecraft.init.Bootstrap.func_151354_b();
    }

    /**
     * A player holding a stack in the cursor clicks the ghost slot: the filter becomes a size-1 copy of it and the
     * cursor stack is untouched; a second click with an empty cursor clears the filter.
     */
    @Test
    public void ghostSlotSetsAndClearsTheFilter() {
        LevelEmitterTileEntity emitter = new LevelEmitterTileEntity();
        EntityPlayer player = net.minecraftforge.common.util.FakePlayerFactory
            .getMinecraft(net.minecraft.server.MinecraftServer.getServer().worldServers[0]);
        LevelEmitterContainer container = new LevelEmitterContainer(player.inventory, emitter);

        player.inventory.setItemStack(new ItemStack(Items.iron_ingot, 5));
        container.slotClick(0, 0, 0, player);

        ItemStack filter = emitter.getFilter();
        assertNotNull(filter);
        assertSame(Items.iron_ingot, filter.getItem());
        assertEquals(1, filter.stackSize);
        ItemStack cursor = player.inventory.getItemStack();
        assertNotNull(cursor);
        assertEquals(5, cursor.stackSize, "the cursor stack must never be consumed");

        player.inventory.setItemStack(null);
        container.slotClick(0, 0, 0, player);

        assertNull(emitter.getFilter(), "an empty cursor clears the filter");
    }

    /**
     * applySettings clamps the count into 0..Integer.MAX_VALUE / 2.
     */
    @Test
    public void applySettingsClampsTheCount() {
        LevelEmitterTileEntity emitter = new LevelEmitterTileEntity();
        EntityPlayer player = net.minecraftforge.common.util.FakePlayerFactory
            .getMinecraft(net.minecraft.server.MinecraftServer.getServer().worldServers[0]);
        LevelEmitterContainer container = new LevelEmitterContainer(player.inventory, emitter);

        container.applySettings(Integer.MAX_VALUE, false);
        assertTrue(emitter.getCount() <= 1073741823, "count must clamp to at most Integer.MAX_VALUE / 2");

        container.applySettings(-5, false);
        assertEquals(0, emitter.getCount(), "a negative count clamps to 0");
    }

    /**
     * The settings packet encodes and decodes with all fields intact.
     */
    @Test
    public void settingsPacketRoundTrips() {
        EmitterSettingsPacket original = new EmitterSettingsPacket(7, 1234, true);
        PacketBuffer buffer = new PacketBuffer(Unpooled.buffer());
        original.encode(buffer);

        EmitterSettingsPacket decoded = new EmitterSettingsPacket();
        decoded.decode(buffer);

        assertEquals(original.getWindowId(), decoded.getWindowId());
        assertEquals(original.getCount(), decoded.getCount());
        assertEquals(original.isLessThan(), decoded.isLessThan());
    }

    /**
     * An emitter that was never placed in a world is not interactable.
     */
    @Test
    public void cannotInteractWithoutAWorld() {
        LevelEmitterTileEntity emitter = new LevelEmitterTileEntity();
        EntityPlayer player = net.minecraftforge.common.util.FakePlayerFactory
            .getMinecraft(net.minecraft.server.MinecraftServer.getServer().worldServers[0]);
        LevelEmitterContainer container = new LevelEmitterContainer(player.inventory, emitter);

        assertFalse(container.canInteractWith(player), "no world means no interaction");
    }
}
