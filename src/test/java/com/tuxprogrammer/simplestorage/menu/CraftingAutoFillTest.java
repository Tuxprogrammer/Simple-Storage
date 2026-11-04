package com.tuxprogrammer.simplestorage.menu;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Bootstrap;
import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.util.FakePlayerFactory;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.tuxprogrammer.simplestorage.blockentity.CraftingTerminalTileEntity;
import com.tuxprogrammer.simplestorage.blockentity.StackKeys;
import com.tuxprogrammer.simplestorage.blockentity.TerminalBackend;
import com.tuxprogrammer.simplestorage.blockentity.TerminalEntry;

/**
 * In-game tests for the crafting terminal's auto-fill contract and
 * accounting: the TERM-05 guarantee that two grid cells wanting the same
 * scarce item reserve one unit per cell, the network pull when the player
 * holds nothing, and the {@link com.tuxprogrammer.simplestorage.terminal.IAutoFillTerminal}
 * queries the 05-integration shared viewer uses.
 *
 * <p>
 * The tests drive the real {@link CraftingTerminalContainer} against a
 * {@link CraftingTerminalTileEntity} subclass whose {@code backend()} is
 * overridden to cover a test inventory, and a fake player from
 * {@link FakePlayerFactory}.
 *
 * Side: in-game (needs a live server, hence {@code @Tag("ingame")}).
 */
@Tag("ingame")
public class CraftingAutoFillTest {

    /**
     * Boots the item registry so the items below resolve.
     */
    @BeforeAll
    public static void bootstrap() {
        Bootstrap.func_151354_b();
    }

    /**
     * A crafting terminal whose backend is fixed to the given inventories, so
     * the tests do not need a cable network.
     */
    private static CraftingTerminalTileEntity fixedBackendTerminal(final TerminalBackend backend) {
        return new CraftingTerminalTileEntity() {

            @Override
            public TerminalBackend backend() {
                return backend;
            }
        };
    }

    /**
     * @return a fake connected player on the first world server
     */
    private static EntityPlayerMP fakePlayer() {
        return FakePlayerFactory.getMinecraft(MinecraftServer.getServer().worldServers[0]);
    }

    /**
     * @return the key of an iron-ingot stack
     */
    private static String ironKey() {
        return StackKeys.keyOf(new ItemStack(Items.iron_ingot, 1, 0));
    }

    /**
     * The number of grid slots holding an iron ingot.
     */
    private static int ironCells(CraftingTerminalTileEntity terminal) {
        int count = 0;
        for (int i = 0; i < 9; i++) {
            if (terminal.getGridStack(i) != null) {
                count++;
            }
        }
        return count;
    }

    /**
     * TERM-05: the network holds ONE iron ingot and both grid cells want an
     * iron ingot. The per-cell reservation must hand exactly one cell the
     * item and leave the other empty: the fill reports missing, and the grid
     * ends with exactly one iron ingot. The upstream filler's per-item
     * accounting let both cells claim the same unit.
     */
    @Test
    public void doesNotReproduceTERM05_twoCellsWantingTheSameItemReserveOnePerCell() {
        InventoryBasic network = new InventoryBasic("network", false, 4);
        network.setInventorySlotContents(0, new ItemStack(Items.iron_ingot, 1, 0));
        CraftingTerminalTileEntity terminal = fixedBackendTerminal(new TerminalBackend(inventories(network)));
        EntityPlayerMP player = fakePlayer();

        String[] wanted = { ironKey(), ironKey() };
        boolean filled = new CraftingTerminalContainer(player.inventory, terminal).autoFill(player, wanted, 1);

        assertFalse(filled, "the fill reports missing: only one iron ingot was available");
        assertEquals(1, ironCells(terminal), "exactly one cell of the grid holds an iron ingot");
    }

    /**
     * With an empty player inventory and two iron ingots in the network,
     * both cells are filled from the network and the fill reports success.
     */
    @Test
    public void autoFillTakesFromTheNetworkWhenThePlayerHasNothing() {
        InventoryBasic network = new InventoryBasic("network", false, 4);
        network.setInventorySlotContents(0, new ItemStack(Items.iron_ingot, 2, 0));
        CraftingTerminalTileEntity terminal = fixedBackendTerminal(new TerminalBackend(inventories(network)));
        EntityPlayerMP player = fakePlayer();

        String[] wanted = { ironKey(), ironKey() };
        boolean filled = new CraftingTerminalContainer(player.inventory, terminal).autoFill(player, wanted, 1);

        assertTrue(filled, "the fill succeeds when the network holds both items");
        assertEquals(2, ironCells(terminal), "both cells of the grid hold an iron ingot");
        ItemStack left = network.getStackInSlot(0);
        assertEquals(0, left == null ? 0 : left.stackSize, "the network was emptied of its iron ingots");
    }

    /**
     * The IAutoFillTerminal contract the shared viewer uses: the search text
     * reaches the terminal, smart search is on by default (searchType bit 8
     * clear), and the stored items reflect the backend's snapshot.
     */
    @Test
    public void contractAnswersTheViewerQueries() {
        InventoryBasic network = new InventoryBasic("network", false, 4);
        network.setInventorySlotContents(0, new ItemStack(Items.iron_ingot, 3, 0));
        CraftingTerminalTileEntity terminal = fixedBackendTerminal(new TerminalBackend(inventories(network)));
        EntityPlayerMP player = fakePlayer();
        CraftingTerminalContainer container = new CraftingTerminalContainer(player.inventory, terminal);

        container.updateSearch("abc");
        assertEquals("abc", terminal.getLastSearch());

        assertTrue(container.isSmartSearch(), "smart search is on by default");

        List<TerminalEntry> stored = container.getStoredItems();
        assertEquals(1, stored.size());
        assertEquals(
            ironKey(),
            stored.get(0)
                .key());
        assertEquals(
            3L,
            stored.get(0)
                .count());

        // The viewer's other query surface stays sound: the terminal reports
        // to a player it can talk to and does not throw otherwise.
        container.sendMessage("simplestoragenh.terminal.amount", "3");
        assertNull(terminal.getGridStack(0), "no auto-fill happened, so the grid is still empty");
    }

    private static List<net.minecraft.inventory.IInventory> inventories(
        net.minecraft.inventory.IInventory... inventories) {
        List<net.minecraft.inventory.IInventory> list = new ArrayList<net.minecraft.inventory.IInventory>();
        for (net.minecraft.inventory.IInventory inventory : inventories) {
            list.add(inventory);
        }
        return list;
    }
}
