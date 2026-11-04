package com.tuxprogrammer.simplestorage.blockentity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minecraft.init.Bootstrap;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.tuxprogrammer.simplestorage.inventory.CabinetModel;

/**
 * Unit tests for {@link FilingCabinetTileEntity}: inventory API behavior, lock semantics, and NBT
 * round-tripping.
 */
@Tag("ingame")
public class FilingCabinetTileEntityTest {

    @BeforeAll
    public static void bootstrap() {
        Bootstrap.func_151354_b();
    }

    private static FilingCabinetTileEntity newCabinet() {
        return new FilingCabinetTileEntity();
    }

    private static NBTTagCompound slotEntry(ItemStack stack, int slot) {
        NBTTagCompound entry = new NBTTagCompound();
        stack.writeToNBT(entry);
        entry.setShort(NbtKeys.SLOT, (short) slot);
        return entry;
    }

    private static NBTTagCompound inventoryTag(NBTTagList list) {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setTag(NbtKeys.INVENTORY, list);
        return tag;
    }

    @Test
    public void roundTripKeepsSlotsAndOrder() {
        FilingCabinetTileEntity cabinet = newCabinet();
        cabinet.setInventorySlotContents(0, new ItemStack(Items.iron_sword));
        cabinet.setInventorySlotContents(7, new ItemStack(Items.iron_sword));
        cabinet.setInventorySlotContents(300, new ItemStack(Items.iron_sword));

        NBTTagCompound tag = new NBTTagCompound();
        cabinet.writeToNBT(tag);

        FilingCabinetTileEntity loaded = newCabinet();
        loaded.readFromNBT(tag);

        assertEquals(
            Items.iron_sword,
            loaded.getStackInSlot(0)
                .getItem());
        assertEquals(
            Items.iron_sword,
            loaded.getStackInSlot(7)
                .getItem());
        assertEquals(
            Items.iron_sword,
            loaded.getStackInSlot(300)
                .getItem());
        assertNull(loaded.getStackInSlot(1));
        assertNull(loaded.getStackInSlot(299));
    }

    @Test
    public void roundTripKeepsCustomName() {
        FilingCabinetTileEntity cabinet = newCabinet();
        cabinet.setCustomName("My Cabinet");
        NBTTagCompound tag = new NBTTagCompound();
        cabinet.writeToNBT(tag);

        FilingCabinetTileEntity loaded = newCabinet();
        loaded.readFromNBT(tag);

        assertEquals("My Cabinet", loaded.getInventoryName());
        assertTrue(loaded.hasCustomInventoryName());
    }

    @Test
    public void emptyCabinetWritesAnEmptyList() {
        FilingCabinetTileEntity cabinet = newCabinet();
        NBTTagCompound tag = new NBTTagCompound();
        cabinet.writeToNBT(tag);

        NBTTagList list = tag.getTagList(NbtKeys.INVENTORY, 10);
        assertEquals(0, list.tagCount());
    }

    @Test
    public void missingKeysLoadEmpty() {
        FilingCabinetTileEntity cabinet = newCabinet();
        cabinet.readFromNBT(new NBTTagCompound());

        assertTrue(
            cabinet.getModel()
                .isEmpty());
        assertFalse(cabinet.hasCustomInventoryName());
        assertEquals("container.simplestoragenh.filing_cabinet", cabinet.getInventoryName());
    }

    @Test
    public void loadsALockFromAnyNonEmptySlot() {
        NBTTagList list = new NBTTagList();
        list.appendTag(slotEntry(new ItemStack(Items.stone_sword), 5));
        NBTTagCompound tag = inventoryTag(list);

        FilingCabinetTileEntity cabinet = newCabinet();
        cabinet.readFromNBT(tag);

        assertNotNull(cabinet.getStackInSlot(5));
        assertEquals(
            Items.stone_sword,
            cabinet.getStackInSlot(5)
                .getItem());

        // Lock is stone sword: a stone sword elsewhere is refused...
        assertFalse(cabinet.isItemValidForSlot(6, new ItemStack(Items.iron_sword)));
        cabinet.setInventorySlotContents(6, new ItemStack(Items.iron_sword));
        assertNull(cabinet.getStackInSlot(6));

        // ...while the locked type is accepted.
        assertTrue(cabinet.isItemValidForSlot(6, new ItemStack(Items.stone_sword)));
        cabinet.setInventorySlotContents(6, new ItemStack(Items.stone_sword));
        assertNotNull(cabinet.getStackInSlot(6));
        assertEquals(
            Items.stone_sword,
            cabinet.getStackInSlot(6)
                .getItem());
    }

    @Test
    public void badSlotIndexesAreIgnored() {
        NBTTagList list = new NBTTagList();
        list.appendTag(slotEntry(new ItemStack(Items.iron_sword), -1));
        list.appendTag(slotEntry(new ItemStack(Items.iron_sword), 512));
        FilingCabinetTileEntity cabinet = newCabinet();
        cabinet.readFromNBT(inventoryTag(list));

        assertTrue(
            cabinet.getModel()
                .isEmpty());
    }

    @Test
    public void mixedTypesKeepOnlyTheLowestSlotsType() {
        NBTTagList list = new NBTTagList();
        list.appendTag(slotEntry(new ItemStack(Items.stone_sword), 3));
        list.appendTag(slotEntry(new ItemStack(Items.iron_sword), 1));
        list.appendTag(slotEntry(new ItemStack(Items.stone_sword), 10));
        FilingCabinetTileEntity cabinet = newCabinet();
        cabinet.readFromNBT(inventoryTag(list));

        assertEquals(
            Items.stone_sword,
            cabinet.getStackInSlot(3)
                .getItem());
        assertNull(cabinet.getStackInSlot(1));
        assertNull(cabinet.getStackInSlot(10));
    }

    @Test
    public void stackableFirstItemIsRefused() {
        FilingCabinetTileEntity cabinet = newCabinet();
        cabinet.setInventorySlotContents(0, new ItemStack(Items.iron_ingot, 1));
        assertNull(cabinet.getStackInSlot(0));
    }

    @Test
    public void differentTypeIsRefusedByTheInventoryApi() {
        FilingCabinetTileEntity cabinet = newCabinet();
        cabinet.setInventorySlotContents(0, new ItemStack(Items.iron_sword));

        assertFalse(cabinet.isItemValidForSlot(1, new ItemStack(Items.stone_sword)));
        cabinet.setInventorySlotContents(1, new ItemStack(Items.stone_sword));
        assertNull(cabinet.getStackInSlot(1));
    }

    @Test
    public void sidedAccessIsAllSlotsAndInsertFollowsTheLock() {
        FilingCabinetTileEntity cabinet = newCabinet();
        assertEquals(CabinetModel.SLOTS, cabinet.getAccessibleSlotsFromSide(0).length);

        ItemStack ingot = new ItemStack(Items.iron_ingot);
        assertFalse(cabinet.canInsertItem(0, ingot, 2));
        cabinet.setInventorySlotContents(0, new ItemStack(Items.iron_sword));
        assertFalse(cabinet.canInsertItem(1, ingot, 2));
        assertTrue(cabinet.canInsertItem(1, new ItemStack(Items.iron_sword), 2));
        assertTrue(cabinet.canExtractItem(0, new ItemStack(Items.iron_sword), 3));
    }

    @Test
    public void unknownKeysAreIgnoredAndWritingAgainWorks() {
        FilingCabinetTileEntity cabinet = newCabinet();
        cabinet.setInventorySlotContents(0, new ItemStack(Items.iron_sword));
        NBTTagCompound tag = new NBTTagCompound();
        cabinet.writeToNBT(tag);
        tag.setString("TotallyUnknownKey", "value");
        tag.setShort("AnotherUnknown", (short) 42);

        FilingCabinetTileEntity loaded = newCabinet();
        loaded.readFromNBT(tag);
        assertEquals(
            Items.iron_sword,
            loaded.getStackInSlot(0)
                .getItem());

        NBTTagCompound rewritten = new NBTTagCompound();
        loaded.writeToNBT(rewritten);
        assertEquals(
            1,
            rewritten.getTagList(NbtKeys.INVENTORY, 10)
                .tagCount());
    }
}
