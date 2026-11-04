package com.tuxprogrammer.simplestorage.blockentity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.tuxprogrammer.simplestorage.inventory.StaggerGate;
import com.tuxprogrammer.simplestorage.terminal.TerminalModes;

/**
 * Tests for {@link StorageTerminalTileEntity} and its crafting-grid
 * subclass.
 */
@Tag("ingame")
public class StorageTerminalTileEntityTest {

    /**
     * Boots the item registry so {@link Items} (and friends) resolve.
     */
    @BeforeAll
    public static void bootstrap() {
        net.minecraft.init.Bootstrap.func_151354_b();
    }

    /**
     * TERM-04: the stagger gate must also fire for the position hash
     * {@link Integer#MIN_VALUE}, exactly once per interval.
     */
    @Test
    public void doesNotReproduceTERM04_minValueHashStillScansBeaconsEveryFortyTicks() {
        int due = 0;
        for (int t = 0; t < 200; t++) {
            if (StaggerGate.isDue(t, Integer.MIN_VALUE, StorageTerminalTileEntity.BEACON_SCAN_INTERVAL)) {
                due++;
            }
        }
        assertEquals(5, due);
    }

    /**
     * Sorting, modes, search type, and last search all survive an NBT round
     * trip.
     */
    @Test
    public void roundTripKeepsSortingModesSearchTypeAndLastSearch() {
        StorageTerminalTileEntity te = new StorageTerminalTileEntity();
        te.setSorting(0x105);
        te.setModes(0x11);
        te.setSearchType(5);
        te.setLastSearch("iron");

        NBTTagCompound tag = new NBTTagCompound();
        te.writeToNBT(tag);

        StorageTerminalTileEntity read = new StorageTerminalTileEntity();
        read.readFromNBT(tag);

        assertEquals(0x105, read.getSorting());
        assertEquals(0x11, read.getModes());
        assertEquals(5, read.getSearchType());
        assertEquals("iron", read.getLastSearch());
    }

    /**
     * A tag with none of the terminal's keys leaves every field at its
     * default.
     */
    @Test
    public void missingKeysKeepDefaults() {
        StorageTerminalTileEntity read = new StorageTerminalTileEntity();
        read.readFromNBT(new NBTTagCompound());

        assertEquals(TerminalModes.DEFAULT_SORTING, read.getSorting());
        assertEquals(TerminalModes.DEFAULT_MODES, read.getModes());
        assertEquals(TerminalModes.DEFAULT_SEARCH_TYPE, read.getSearchType());
        assertEquals("", read.getLastSearch());
    }

    /**
     * A last search longer than 256 characters is truncated on set.
     */
    @Test
    public void lastSearchIsTruncatedTo256() {
        StorageTerminalTileEntity te = new StorageTerminalTileEntity();
        StringBuilder longText = new StringBuilder();
        for (int i = 0; i < 300; i++) {
            longText.append('a');
        }
        te.setLastSearch(longText.toString());
        assertEquals(
            256,
            te.getLastSearch()
                .length());
    }

    /**
     * Keys the terminal does not know about are ignored on read.
     */
    @Test
    public void unknownKeysAreIgnored() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setInteger("someOtherModule", 42);
        tag.setString("unrelated", "value");

        StorageTerminalTileEntity read = new StorageTerminalTileEntity();
        read.readFromNBT(tag);

        assertEquals(TerminalModes.DEFAULT_SORTING, read.getSorting());
        assertEquals(TerminalModes.DEFAULT_MODES, read.getModes());
        assertEquals(TerminalModes.DEFAULT_SEARCH_TYPE, read.getSearchType());
        assertEquals("", read.getLastSearch());
    }

    /**
     * A crafting-grid stack in slot 4 survives an NBT round trip, and an
     * entry with an out-of-range slot index is skipped on read.
     */
    @Test
    public void craftingGridRoundTrips() {
        CraftingTerminalTileEntity te = new CraftingTerminalTileEntity();
        te.setGridStack(4, new ItemStack(Items.iron_ingot));

        NBTTagCompound tag = new NBTTagCompound();
        te.writeToNBT(tag);

        // A bad entry with slot index 9 (out of the 0..8 range) must be
        // skipped on read.
        NBTTagCompound badEntry = new ItemStack(Items.iron_ingot).writeToNBT(new NBTTagCompound());
        badEntry.setByte("Slot", (byte) 9);
        net.minecraft.nbt.NBTTagList list = tag.getTagList("craftingGrid", 10);
        list.appendTag(badEntry);

        CraftingTerminalTileEntity read = new CraftingTerminalTileEntity();
        read.readFromNBT(tag);

        ItemStack stack = read.getGridStack(4);
        assertTrue(stack != null);
        assertEquals(Items.iron_ingot, stack.getItem());
        assertEquals(1, stack.stackSize);
    }

    /**
     * With no world attached the backend covers no inventories.
     */
    @Test
    public void noWorldMeansEmptyBackend() {
        StorageTerminalTileEntity te = new StorageTerminalTileEntity();
        assertTrue(
            te.backend()
                .snapshot()
                .isEmpty());
    }

    /**
     * serverTick returns early (no exception, no work) when the tile has no
     * world yet.
     */
    @Test
    public void serverTickDoesNothingWithoutAWorld() {
        StorageTerminalTileEntity te = new StorageTerminalTileEntity();
        te.serverTick();
    }
}
