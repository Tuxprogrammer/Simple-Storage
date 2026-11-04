package com.tuxprogrammer.simplestorage.menu;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.tuxprogrammer.simplestorage.blockentity.FilingCabinetTileEntity;

/**
 * In-game tests for {@link FilingCabinetContainer}.
 *
 * <p>
 * Side: server logic; the client display lives in simplestorage.client.
 */
@Tag("ingame")
public class FilingCabinetContainerTest {

    @BeforeAll
    public static void bootstrap() {
        net.minecraft.init.Bootstrap.func_151354_b();
    }

    /**
     * Creates a fresh fake player inventory for a test.
     *
     * @return a fake player's inventory
     */
    private static InventoryPlayer fakeInventory() {
        return net.minecraftforge.common.util.FakePlayerFactory
            .getMinecraft(net.minecraft.server.MinecraftServer.getServer().worldServers[0]).inventory;
    }

    /**
     * Creates an iron-sword stack.
     *
     * @return an iron sword
     */
    private static ItemStack ironSword() {
        return new ItemStack(Items.iron_sword);
    }

    /**
     * Creates a stone-sword stack.
     *
     * @return a stone sword
     */
    private static ItemStack stoneSword() {
        return new ItemStack(Items.stone_sword);
    }

    /**
     * A filing cabinet with an iron sword in the given cabinet slot.
     *
     * @param slot the cabinet slot to hold the sword
     * @return the cabinet
     */
    private static FilingCabinetTileEntity cabinetWithSwordInSlot(int slot) {
        FilingCabinetTileEntity cabinet = new FilingCabinetTileEntity();
        cabinet.setInventorySlotContents(slot, ironSword());
        return cabinet;
    }

    /**
     * TERM-13: the upstream code scanned only the visible 45-slot window to decide whether an item could be placed,
     * so an empty visible region could accept a different item type than the cabinet's lock. Here the cabinet holds
     * an iron sword in slot 400 (outside the visible window at scroll row 0); the window slot must still refuse a
     * stone sword and accept an iron sword, because the whole cabinet is locked to iron swords.
     */
    @Test
    public void doesNotReproduceTERM13_mayPlaceChecksTheWholeCabinet() {
        FilingCabinetTileEntity cabinet = cabinetWithSwordInSlot(400);
        FilingCabinetContainer container = new FilingCabinetContainer(fakeInventory(), cabinet);
        container.setScrollRow(0);

        Slot slot0 = container.getSlot(0);
        // Slot 0 is empty (the iron sword is at cabinet slot 400, row 44), but the cabinet is locked to iron swords.
        assertNull(slot0.getStack());
        // A stone sword is the wrong type for this cabinet.
        assertFalse(slot0.isItemValid(stoneSword()));
        // An iron sword matches the cabinet's lock.
        assertTrue(slot0.isItemValid(ironSword()));
    }

    /**
     * At scroll row 1, window slot 0 maps to cabinet slot 9, so the iron sword placed there must be visible.
     */
    @Test
    public void windowMapsToTheScrollOffset() {
        FilingCabinetTileEntity cabinet = cabinetWithSwordInSlot(9);
        FilingCabinetContainer container = new FilingCabinetContainer(fakeInventory(), cabinet);
        container.setScrollRow(1);

        Slot slot0 = container.getSlot(0);
        assertNotNull(slot0.getStack());
        assertEquals(
            Items.iron_sword,
            slot0.getStack()
                .getItem());
    }

    /**
     * The scroll row is clamped to 0..maxScrollRows(512, 9, 5) = 52.
     */
    @Test
    public void scrollRowIsClamped() {
        FilingCabinetTileEntity cabinet = new FilingCabinetTileEntity();
        FilingCabinetContainer container = new FilingCabinetContainer(fakeInventory(), cabinet);

        container.setScrollRow(1000);
        assertEquals(52, container.getScrollRow());

        container.setScrollRow(-5);
        assertEquals(0, container.getScrollRow());
    }

    /**
     * At scroll row 52, window slot 44 maps to cabinet slot 52 * 9 + 44 = 512, which is beyond the cabinet's 512
     * slots. Such slots read empty and refuse items.
     */
    @Test
    public void lastRowsBeyond511AreEmptyAndRefuse() {
        FilingCabinetTileEntity cabinet = new FilingCabinetTileEntity();
        FilingCabinetContainer container = new FilingCabinetContainer(fakeInventory(), cabinet);
        container.setScrollRow(52);

        Slot slot44 = container.getSlot(44);
        assertNull(slot44.getStack());
        assertFalse(slot44.isItemValid(ironSword()));
    }

    /**
     * Shift-click from a cabinet window slot moves the stack into the player's inventory.
     */
    @Test
    public void shiftClickMovesToPlayerInventory() {
        FilingCabinetTileEntity cabinet = cabinetWithSwordInSlot(0);
        InventoryPlayer inv = fakeInventory();
        FilingCabinetContainer container = new FilingCabinetContainer(inv, cabinet);
        container.setScrollRow(0);

        // Window slot 0 holds the iron sword (cabinet slot 0).
        assertNotNull(
            container.getSlot(0)
                .getStack());

        // Shift-click window slot 0: the sword moves into the player's slots (ids 45..80).
        ItemStack moved = container.transferStackInSlot(null, 0);
        assertNotNull(moved);
        // The window slot is now empty.
        assertNull(
            container.getSlot(0)
                .getStack());
        // The sword is somewhere in the player's inventory or hotbar.
        boolean inPlayerSlots = false;
        for (int i = 0; i < 36; i++) {
            if (inv.getStackInSlot(i) != null && inv.getStackInSlot(i)
                .getItem() == Items.iron_sword) {
                inPlayerSlots = true;
                break;
            }
        }
        assertTrue(inPlayerSlots);
    }
}
