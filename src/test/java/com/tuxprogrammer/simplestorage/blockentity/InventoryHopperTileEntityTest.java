// Unit tests for the basic inventory hopper.
package com.tuxprogrammer.simplestorage.blockentity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.tuxprogrammer.simplestorage.inventory.HopperLogic;
import com.tuxprogrammer.simplestorage.inventory.HopperLogic.Decision;

/**
 * Tests for {@link InventoryHopperTileEntity}.
 */
@Tag("ingame")
public class InventoryHopperTileEntityTest {

    @BeforeAll
    public static void bootstrap() {
        net.minecraft.init.Bootstrap.func_151354_b();
    }

    @Test
    public void movesExactlyOneItemPerCycle() {
        Box source = new Box(new ItemStack[] { new ItemStack(Items.gold_ingot), new ItemStack(Items.iron_ingot) });
        Box target = new Box(new ItemStack[] { null, null, null });

        InventoryHopperTileEntity.moveOne(source, target, null);
        assertEquals(1, count(target), "exactly one item must move per cycle");
        assertEquals(1, count(source), "source must still hold its remaining items");
    }

    @Test
    public void filterRestrictsWhatMoves() {
        Box source = new Box(new ItemStack[] { new ItemStack(Items.gold_ingot), new ItemStack(Items.iron_ingot) });
        Box target = new Box(new ItemStack[] { null, null, null });
        ItemStack filter = new ItemStack(Items.iron_ingot);

        InventoryHopperTileEntity.moveOne(source, target, filter);

        assertEquals(1, count(target));
        assertEquals(
            Items.iron_ingot,
            target.getStackInSlot(0)
                .getItem(),
            "the gold ingot must be skipped");
        assertEquals(1, count(source), "the gold ingot must remain in the source");
    }

    @Test
    public void putsTheItemBackWhenTheTargetIsFull() {
        Box source = new Box(new ItemStack[] { new ItemStack(Items.gold_ingot) });
        Box target = new Box(
            new ItemStack[] { full(Items.iron_ingot), full(Items.iron_ingot), full(Items.iron_ingot) });

        boolean moved = InventoryHopperTileEntity.moveOne(source, target, null);

        assertFalse(moved, "the move must report failure when the target is full");
        assertEquals(1, count(source), "the item must be put back into the source");
        assertEquals(192, count(target), "the full target must stay full");
    }

    @Test
    public void roundTripKeepsTheFilter() {
        InventoryHopperTileEntity hopper = new InventoryHopperTileEntity();
        ItemStack filter = new ItemStack(Items.iron_ingot);
        hopper.setFilter(filter);

        NBTTagCompound tag = new NBTTagCompound();
        hopper.writeToNBT(tag);

        InventoryHopperTileEntity restored = new InventoryHopperTileEntity();
        restored.readFromNBT(tag);

        ItemStack restoredFilter = restored.getFilter();
        assertNotNull(restoredFilter, "the filter must survive the round trip");
        assertEquals(Items.iron_ingot, restoredFilter.getItem());
    }

    @Test
    public void roundTripWithoutFilter() {
        InventoryHopperTileEntity hopper = new InventoryHopperTileEntity();
        hopper.setFilter(null);

        NBTTagCompound tag = new NBTTagCompound();
        hopper.writeToNBT(tag);

        InventoryHopperTileEntity restored = new InventoryHopperTileEntity();
        restored.readFromNBT(tag);

        assertNull(restored.getFilter(), "an absent filter must restore as null");
    }

    @Test
    public void unknownKeysAreIgnored() {
        InventoryHopperTileEntity hopper = new InventoryHopperTileEntity();

        NBTTagCompound tag = new NBTTagCompound();
        tag.setString("whoIsThis", "a stranger");
        tag.setInteger("alsoUnknown", 42);

        hopper.readFromNBT(tag);

        assertNull(hopper.getFilter(), "unknown keys must not produce a filter");
        assertTrue(hopper.isEnabled(), "unknown keys must not change the enabled state");
    }

    /**
     * STOR-08: a bottom change must pause the hopper for exactly one cycle.
     */
    @Test
    public void doesNotReproduceSTOR08_bottomChangePausesExactlyOneCycleInTheTileEntityPath() {
        HopperLogic logic = new HopperLogic(10);

        assertEquals(
            Decision.IDLE_BACKOFF,
            logic.tick(true, false, false, true),
            "a bottom change must pause the hopper");
        assertEquals(
            Decision.WAIT,
            logic.tick(true, false, false, false),
            "the next cycle must be a plain wait, not another pause (STOR-08)");
    }

    @Test
    public void doesNotReproduceSTOR09_unfilteredNetworkSourceDoesNotSpin() {
        HopperLogic logic = new HopperLogic(10);

        assertEquals(
            Decision.IDLE_BACKOFF,
            logic.tick(true, true, false, false),
            "an unfiltered hopper facing an empty network must back off, not spin");
        assertEquals(
            Decision.WAIT,
            logic.tick(true, true, false, false),
            "the next cycle must be a plain wait, not another back-off (STOR-09)");
    }

    @Test
    public void serverTickDoesNothingWithoutAWorld() {
        InventoryHopperTileEntity hopper = new InventoryHopperTileEntity();
        assertNull(hopper.getWorldObj(), "a fresh hopper has no world");
        hopper.updateEntity();
        assertNull(hopper.getWorldObj(), "serverTick must not throw when the world is absent");
    }

    private static int count(Box box) {
        int total = 0;
        for (int i = 0; i < box.getSizeInventory(); i++) {
            ItemStack stack = box.getStackInSlot(i);
            if (stack != null) {
                total += stack.stackSize;
            }
        }
        return total;
    }

    private static ItemStack full(Item item) {
        return new ItemStack(item, 64);
    }

    /**
     * A tiny three-slot {@link IInventory} for the tests.
     */
    private static final class Box implements IInventory {

        private final ItemStack[] slots;

        Box(ItemStack[] items) {
            this.slots = new ItemStack[3];
            for (int i = 0; i < 3 && i < items.length; i++) {
                this.slots[i] = items[i];
            }
        }

        @Override
        public int getSizeInventory() {
            return 3;
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            return slot >= 0 && slot < 3 ? this.slots[slot] : null;
        }

        @Override
        public ItemStack decrStackSize(int slot, int count) {
            ItemStack stack = this.slots[slot];
            if (stack == null) {
                return null;
            }
            if (stack.stackSize <= count) {
                this.slots[slot] = null;
                return stack;
            }
            ItemStack out = this.slots[slot].copy();
            out.stackSize = count;
            this.slots[slot].stackSize -= count;
            return out;
        }

        @Override
        public ItemStack getStackInSlotOnClosing(int slot) {
            return null;
        }

        @Override
        public void setInventorySlotContents(int slot, ItemStack stack) {
            this.slots[slot] = stack;
        }

        @Override
        public String getInventoryName() {
            return "Box";
        }

        @Override
        public boolean hasCustomInventoryName() {
            return false;
        }

        @Override
        public int getInventoryStackLimit() {
            return 64;
        }

        @Override
        public void markDirty() {}

        @Override
        public boolean isUseableByPlayer(EntityPlayer player) {
            return true;
        }

        @Override
        public void openInventory() {}

        @Override
        public void closeInventory() {}

        @Override
        public boolean isItemValidForSlot(int slot, ItemStack stack) {
            return true;
        }
    }
}
