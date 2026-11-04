package com.tuxprogrammer.simplestorage.blockentity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.ItemStack;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link TerminalBackend}.
 * Side: server logic.
 */
@Tag("ingame")
public class TerminalBackendTest {

    @BeforeAll
    public static void bootstrap() {
        net.minecraft.init.Bootstrap.func_151354_b();
    }

    @Test
    public void snapshotMergesStacksAcrossInventories() {
        InventoryBasic invA = new InventoryBasic("A", false, 2);
        invA.setInventorySlotContents(0, new ItemStack(Items.iron_ingot, 64, 0));
        InventoryBasic invB = new InventoryBasic("B", false, 2);
        invB.setInventorySlotContents(0, new ItemStack(Items.iron_ingot, 30, 0));

        TerminalBackend backend = new TerminalBackend(inventories(invA, invB));

        List<TerminalEntry> snapshot = backend.snapshot();
        assertEquals(1, snapshot.size());
        assertEquals(
            94L,
            snapshot.get(0)
                .count());
        assertEquals(
            Items.iron_ingot,
            snapshot.get(0)
                .prototype()
                .getItem());
    }

    @Test
    public void pullReturnsAtMostOneMaxStack() {
        InventoryBasic inv = new InventoryBasic("A", false, 4);
        inv.setInventorySlotContents(0, new ItemStack(Items.iron_ingot, 64, 0));
        inv.setInventorySlotContents(1, new ItemStack(Items.iron_ingot, 64, 0));
        inv.setInventorySlotContents(2, new ItemStack(Items.iron_ingot, 2, 0));

        TerminalBackend backend = new TerminalBackend(inventories(inv));
        String key = StackKeys.keyOf(new ItemStack(Items.iron_ingot, 1, 0));

        ItemStack pulled = backend.pull(key, 100);

        assertNotNull(pulled);
        assertEquals(64, pulled.stackSize);
    }

    @Test
    public void pullUnknownKeyReturnsNull() {
        InventoryBasic inv = new InventoryBasic("A", false, 1);
        inv.setInventorySlotContents(0, new ItemStack(Items.iron_ingot, 10, 0));

        TerminalBackend backend = new TerminalBackend(inventories(inv));

        assertNull(backend.pull("minecraft:gold_ingot@0#", 10));
    }

    @Test
    public void pushFillsLowerIndexInventoryFirst() {
        InventoryBasic invA = new InventoryBasic("A", false, 1);
        InventoryBasic invB = new InventoryBasic("B", false, 1);

        TerminalBackend backend = new TerminalBackend(inventories(invA, invB));

        backend.push(new ItemStack(Items.gold_ingot, 70, 0));

        assertEquals(64, invA.getStackInSlot(0).stackSize);
        assertEquals(6, invB.getStackInSlot(0).stackSize);
    }

    @Test
    public void pushReturnsTheRemainderWhenFull() {
        InventoryBasic inv = new InventoryBasic("A", false, 1);
        inv.setInventorySlotContents(0, new ItemStack(Items.iron_ingot, 64, 0));

        TerminalBackend backend = new TerminalBackend(inventories(inv));

        ItemStack remainder = backend.push(new ItemStack(Items.iron_ingot, 10, 0));

        assertNotNull(remainder);
        assertEquals(10, remainder.stackSize);
        assertEquals(64, inv.getStackInSlot(0).stackSize);
    }

    @Test
    public void pushedItemShowsUpInTheSnapshot() {
        InventoryBasic inv = new InventoryBasic("A", false, 2);

        TerminalBackend backend = new TerminalBackend(inventories(inv));

        backend.push(new ItemStack(Items.gold_ingot, 12, 0));

        List<TerminalEntry> snapshot = backend.snapshot();
        assertEquals(1, snapshot.size());
        assertEquals(
            StackKeys.keyOf(new ItemStack(Items.gold_ingot, 1, 0)),
            snapshot.get(0)
                .key());
        assertEquals(
            12L,
            snapshot.get(0)
                .count());
    }

    @Test
    public void nbtStacksKeepSeparateKeys() {
        InventoryBasic inv = new InventoryBasic("A", false, 2);
        inv.setInventorySlotContents(0, new ItemStack(Items.iron_sword, 1, 0));
        inv.setInventorySlotContents(1, new ItemStack(Items.iron_sword, 1, 5));

        TerminalBackend backend = new TerminalBackend(inventories(inv));

        List<TerminalEntry> snapshot = backend.snapshot();
        assertEquals(2, snapshot.size());
        assertEquals(
            1L,
            snapshot.get(0)
                .count());
        assertEquals(
            1L,
            snapshot.get(1)
                .count());
        assertEquals(
            0,
            snapshot.get(0)
                .prototype()
                .getItemDamage());
        assertEquals(
            5,
            snapshot.get(1)
                .prototype()
                .getItemDamage());
    }

    @Test
    public void changeCounterAdvancesOnPullAndPush() {
        InventoryBasic inv = new InventoryBasic("A", false, 2);
        inv.setInventorySlotContents(0, new ItemStack(Items.iron_ingot, 64, 0));

        TerminalBackend backend = new TerminalBackend(inventories(inv));
        String key = StackKeys.keyOf(new ItemStack(Items.iron_ingot, 1, 0));
        long before = backend.changeCounter();

        backend.pull(key, 10);
        assertEquals(before + 1, backend.changeCounter());

        backend.push(new ItemStack(Items.gold_ingot, 5, 0));
        assertEquals(before + 2, backend.changeCounter());
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
