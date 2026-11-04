package com.tuxprogrammer.simplestorage.blockentity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import net.minecraft.init.Bootstrap;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link OpenCrateTileEntity}: the virtual inventory exposed over the item entities in
 * front of the crate, and its staggered rescan gate.
 */
@Tag("ingame")
public class OpenCrateTileEntityTest {

    private static final List<ItemStack> EMPTY = new ArrayList<ItemStack>();

    @BeforeAll
    public static void bootstrap() {
        Bootstrap.func_151354_b();
    }

    @Test
    public void doesNotReproduceSTOR04_appendSlotIsConsistentForPlainInventoryConsumers() {
        OpenCrateTileEntity crate = new OpenCrateTileEntity();
        final List<ItemStack> world = Arrays
            .asList(new ItemStack(Items.iron_ingot, 3), new ItemStack(Items.gold_ingot, 2));
        crate.setItemSource(new OpenCrateTileEntity.ItemSource() {

            @Override
            public List<ItemStack> scan() {
                return world;
            }
        });
        final List<ItemStack> sunk = new ArrayList<ItemStack>();
        crate.setItemSink(new OpenCrateTileEntity.ItemSink() {

            @Override
            public void accept(ItemStack stack) {
                sunk.add(stack);
            }
        });
        crate.rescan();

        assertEquals(3, crate.getSizeInventory());

        // Slot 2 is the append slot: always reports empty.
        assertNull(crate.getStackInSlot(2));
        // Only the append slot accepts items.
        assertFalse(crate.isItemValidForSlot(0, new ItemStack(Items.gold_ingot)));
        assertFalse(crate.isItemValidForSlot(1, new ItemStack(Items.gold_ingot)));
        assertTrue(crate.isItemValidForSlot(2, new ItemStack(Items.gold_ingot)));

        // Writing to the append slot reaches the sink exactly once, with a copy.
        ItemStack toAdd = new ItemStack(Items.gold_ingot, 5);
        crate.setInventorySlotContents(2, toAdd);
        assertEquals(1, sunk.size());
        assertNotSame(toAdd, sunk.get(0));
        assertEquals(5, sunk.get(0).stackSize);
        assertEquals(
            Items.gold_ingot,
            sunk.get(0)
                .getItem());

        // Sided: insert only on the append slot, extract only on real slots.
        for (int side = 0; side < 6; side++) {
            assertFalse(crate.canInsertItem(0, new ItemStack(Items.iron_ingot), side));
            assertFalse(crate.canInsertItem(1, new ItemStack(Items.iron_ingot), side));
            assertTrue(crate.canInsertItem(2, new ItemStack(Items.iron_ingot), side));
            assertTrue(crate.canExtractItem(0, new ItemStack(Items.iron_ingot), side));
            assertTrue(crate.canExtractItem(1, new ItemStack(Items.iron_ingot), side));
            assertFalse(crate.canExtractItem(2, new ItemStack(Items.iron_ingot), side));
        }
        int[] accessible = crate.getAccessibleSlotsFromSide(0);
        assertEquals(3, accessible.length);
        for (int i = 0; i < 3; i++) {
            assertEquals(i, accessible[i]);
        }
    }

    @Test
    public void doesNotReproduceSTOR01_minValueHashRescansEveryFiveTicks() {
        final int interval = com.tuxprogrammer.simplestorage.inventory.StaggerGate.OPEN_CRATE_INTERVAL;
        final int window = interval * 20; // 100 consecutive ticks at an interval of 5

        // A coordinate triple whose gate uses Objects.hash(x, y, z):
        int hash = Objects.hash(1, 2, 3);
        assertDueExactlyEveryFiveTicks(hash, interval, window);

        // ...and, separately, for hash Integer.MIN_VALUE via the gate directly:
        assertDueExactlyEveryFiveTicks(Integer.MIN_VALUE, interval, window);
    }

    private static void assertDueExactlyEveryFiveTicks(int hash, int interval, int window) {
        int due = 0;
        for (long gameTime = 0; gameTime < window; gameTime++) {
            if (com.tuxprogrammer.simplestorage.inventory.StaggerGate.isDue(gameTime, hash, interval)) {
                due++;
            }
        }
        assertEquals(20, due);
    }

    @Test
    public void rescanRebuildsTheList() {
        OpenCrateTileEntity crate = new OpenCrateTileEntity();
        final List<ItemStack> world = new ArrayList<ItemStack>();
        world.add(new ItemStack(Items.iron_ingot, 1));
        crate.setItemSource(new OpenCrateTileEntity.ItemSource() {

            @Override
            public List<ItemStack> scan() {
                return world;
            }
        });
        crate.rescan();
        assertEquals(1, crate.getSizeInventory() - 1);
        assertEquals(1, crate.getStackInSlot(0).stackSize);

        // The source changes between two rescans.
        world.clear();
        world.add(new ItemStack(Items.gold_ingot, 2));
        world.add(new ItemStack(Items.iron_ingot, 4));
        crate.rescan();
        assertEquals(2, crate.getSizeInventory() - 1);
        assertEquals(
            Items.gold_ingot,
            crate.getStackInSlot(0)
                .getItem());
        assertEquals(
            Items.iron_ingot,
            crate.getStackInSlot(1)
                .getItem());
    }

    @Test
    public void emptySourceHasOnlyTheAppendSlot() {
        OpenCrateTileEntity crate = new OpenCrateTileEntity();
        crate.setItemSource(new OpenCrateTileEntity.ItemSource() {

            @Override
            public List<ItemStack> scan() {
                return EMPTY;
            }
        });
        crate.rescan();
        assertEquals(1, crate.getSizeInventory());
        assertNull(crate.getStackInSlot(0));
        assertTrue(crate.isItemValidForSlot(0, new ItemStack(Items.iron_ingot)));
    }

    @Test
    public void decrStackSizeSplitsFromTheStack() {
        OpenCrateTileEntity crate = new OpenCrateTileEntity();
        final List<ItemStack> world = new ArrayList<ItemStack>();
        world.add(new ItemStack(Items.iron_ingot, 10));
        crate.setItemSource(new OpenCrateTileEntity.ItemSource() {

            @Override
            public List<ItemStack> scan() {
                return world;
            }
        });
        crate.rescan();

        ItemStack split = crate.decrStackSize(0, 4);
        assertEquals(Items.iron_ingot, split.getItem());
        assertEquals(4, split.stackSize);
        assertEquals(6, crate.getStackInSlot(0).stackSize);

        // Splitting off more than remains empties the slot.
        ItemStack rest = crate.decrStackSize(0, 64);
        assertEquals(6, rest.stackSize);
        assertNull(crate.getStackInSlot(0));
    }

    @Test
    public void noNbtIsWritten() {
        OpenCrateTileEntity crate = new OpenCrateTileEntity();
        NBTTagCompound nbt = new NBTTagCompound();
        crate.writeToNBT(nbt);
        assertFalse(nbt.hasKey("inventory"));
    }

    @Test
    public void serverTickDoesNothingWithoutAWorld() {
        OpenCrateTileEntity crate = new OpenCrateTileEntity();
        boolean[] scanned = { false };
        crate.setItemSource(new OpenCrateTileEntity.ItemSource() {

            @Override
            public List<ItemStack> scan() {
                scanned[0] = true;
                return EMPTY;
            }
        });
        crate.setItemSink(new OpenCrateTileEntity.ItemSink() {

            @Override
            public void accept(ItemStack stack) {}
        });
        crate.serverTick();
        assertFalse(scanned[0]);
    }
}
