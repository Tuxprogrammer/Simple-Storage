package com.tuxprogrammer.simplestorage.menu;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Bootstrap;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.util.FakePlayerFactory;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.tuxprogrammer.simplestorage.blockentity.StackKeys;
import com.tuxprogrammer.simplestorage.filter.FilterKey;
import com.tuxprogrammer.simplestorage.filter.ItemFilters;
import com.tuxprogrammer.simplestorage.item.ItemFilterItem;

/**
 * In-game tests for {@link ItemFilterContainer}: the ghost-slot click
 * behavior, the NBT round-trip of the filter's stacks and flags, and the
 * TERM-11 guarantee that shift-clicking an item whose only identical ghost
 * slot comes after an empty slot does not duplicate it.
 *
 * <p>
 * The container's held filter is a fresh {@link ItemFilterItem} placed in hotbar
 * slot 0 of a fake player and selected, so the container edits a real item's
 * NBT the same way it would in game.
 *
 * Side: in-game (needs the registry, hence {@code @Tag("ingame")}).
 */
@Tag("ingame")
public class ItemFilterContainerTest {

    /**
     * Boots the item registry so the fake player can hold the filter item.
     */
    @BeforeAll
    public static void bootstrap() {
        Bootstrap.func_151354_b();
    }

    /**
     * @return a fake player with a fresh filter item in hotbar slot 0 and selected
     */
    private static EntityPlayerMP heldFilterPlayer() {
        EntityPlayerMP player = FakePlayerFactory.getMinecraft(MinecraftServer.getServer().worldServers[0]);
        ItemStack filter = new ItemStack(new ItemFilterItem());
        player.inventory.setInventorySlotContents(0, filter);
        player.inventory.currentItem = 0;
        return player;
    }

    /**
     * TERM-11: with ghost slots [iron at 0, empty at 1, gold at 2], shift-clicking
     * a gold ingot from the player inventory targets the occupied slot 2 (an
     * identical key already sits there), so nothing is added and the ghost slots
     * are unchanged: exactly one gold ingot remains, in slot 2, and slot 1 stays
     * empty. Without the "first empty slot" rule the gold would be duplicated,
     * or the pre-existing gold would be re-stamped.
     */
    @Test
    public void doesNotReproduceTERM11_itemAfterTheFirstEmptySlotIsNotDuplicated() {
        ItemStack[] ghosts = new ItemStack[9];
        ghosts[0] = new ItemStack(Items.iron_ingot);
        ghosts[2] = new ItemStack(Items.gold_ingot);
        EntityPlayerMP player = heldFilterPlayer();
        ItemFilterItem.setStacks(player.inventory.getCurrentItem(), ghosts);

        ItemFilterContainer container = new ItemFilterContainer(player.inventory);
        // Put a gold ingot in a real inventory slot (slot 9) and shift-click it.
        player.inventory.setInventorySlotContents(9, new ItemStack(Items.gold_ingot));
        container.transferStackInSlot(player, 9);

        ItemStack[] after = ItemFilterItem.getStacks(player.inventory.getCurrentItem());
        int goldCount = 0;
        for (ItemStack stack : after) {
            if (stack != null && stack.getItem() == Items.gold_ingot) {
                goldCount++;
            }
        }
        // Exactly one gold ingot: the shift-click targets the occupied slot 2
        // (an identical key already sits there), so nothing is added and the
        // ghost slots are unchanged (TERM-11).
        assertEquals(1, goldCount, "the pre-existing gold is stored once; the shift-click adds no duplicate");
        assertNotNull(after[2], "pre-existing gold at slot 2 remains");
        assertNull(after[1], "the empty slot 1 is untouched");
        assertNotNull(after[0], "iron at slot 0 remains");
        assertTrue(after[0].getItem() == Items.iron_ingot, "slot 0 is still the iron ingot");
    }

    /**
     * Clicking a ghost slot with a cursor stack stamps a size-1 copy into that
     * slot (the cursor is not consumed), and clicking with an empty cursor
     * clears it; both persist into the held item's NBT.
     */
    @Test
    public void ghostSlotClickSetsAndClears() {
        EntityPlayerMP player = heldFilterPlayer();
        ItemFilterContainer container = new ItemFilterContainer(player.inventory);

        // Click ghost slot 0 with a gold ingot on the cursor.
        ItemStack carried = new ItemStack(Items.gold_ingot, 32);
        player.inventory.setItemStack(carried);
        ItemStack result = container.slotClick(0, 0, 0, player);
        assertNotNull(result, "a ghost click returns the cursor stack unchanged");
        assertEquals(32, result.stackSize, "the returned cursor stack is not consumed");
        assertEquals(32, carried.stackSize, "the cursor stack is not consumed");

        ItemStack[] after = ItemFilterItem.getStacks(player.inventory.getCurrentItem());
        assertNotNull(after[0], "ghost slot 0 is set");
        assertTrue(after[0].getItem() == Items.gold_ingot, "ghost slot 0 holds the gold ingot");
        assertEquals(1, after[0].stackSize, "the stamped stack is size 1");

        // Click ghost slot 0 again with an empty cursor: it clears, and the
        // container returns null for a cleared slot.
        player.inventory.setItemStack(null);
        ItemStack clearedResult = container.slotClick(0, 0, 0, player);
        assertNull(clearedResult, "clearing a ghost slot returns null");
        ItemStack[] cleared = ItemFilterItem.getStacks(player.inventory.getCurrentItem());
        assertNull(cleared[0], "ghost slot 0 is cleared");
    }

    /**
     * The filter's stacks round-trip through the item's NBT: what is written by
     * {@link ItemFilterItem#setStacks} is read back by
     * {@link ItemFilterItem#getStacks} with the right slots, sizes, and items,
     * and a hand-built tag with a bad "Slot" value (9) is ignored.
     */
    @Test
    public void stacksRoundTripThroughItemNbt() {
        ItemStack filter = new ItemStack(new ItemFilterItem());
        ItemStack[] stacks = new ItemStack[9];
        stacks[0] = new ItemStack(Items.iron_ingot);
        stacks[5] = new ItemStack(Items.gold_ingot);
        ItemFilterItem.setStacks(filter, stacks);

        ItemStack[] read = ItemFilterItem.getStacks(filter);
        assertEquals(9, read.length, "getStacks returns nine slots");
        assertNotNull(read[0], "slot 0 round-trips");
        assertTrue(read[0].getItem() == Items.iron_ingot, "slot 0 is the iron ingot");
        assertEquals(1, read[0].stackSize, "slot 0 is stored size 1");
        assertNotNull(read[5], "slot 5 round-trips");
        assertTrue(read[5].getItem() == Items.gold_ingot, "slot 5 is the gold ingot");
        assertEquals(1, read[5].stackSize, "slot 5 is stored size 1");
        assertNull(read[1], "unwritten slots are null");

        // A hand-built tag with a bad Slot value (9, out of range 0..8) is ignored.
        ItemStack bad = new ItemStack(new ItemFilterItem());
        NBTTagCompound compound = new NBTTagCompound();
        NBTTagList list = new NBTTagList();
        NBTTagCompound entry = new ItemStack(Items.diamond).writeToNBT(new NBTTagCompound());
        entry.setByte("Slot", (byte) 9);
        list.appendTag(entry);
        compound.setTag("stacks", list);
        bad.setTagCompound(compound);
        ItemStack[] badRead = ItemFilterItem.getStacks(bad);
        for (ItemStack slot : badRead) {
            assertNull(slot, "a bad Slot value is ignored, leaving all nine slots null");
        }
    }

    /**
     * The filter's flags round-trip through the item's NBT, and "allowList"
     * defaults to true when the key is missing.
     */
    @Test
    public void flagsRoundTrip() {
        ItemStack filter = new ItemStack(new ItemFilterItem());
        // A fresh item with no tags: allowList defaults to true, matchNbt false.
        assertTrue(ItemFilterItem.isAllowList(filter), "allowList defaults to true when the key is missing");
        assertFalse(ItemFilterItem.isMatchNbt(filter), "matchNbt defaults to false when the key is missing");

        ItemFilterItem.setFlags(filter, true, false);
        assertTrue(ItemFilterItem.isMatchNbt(filter), "matchNbt round-trips to true");
        assertFalse(ItemFilterItem.isAllowList(filter), "allowList round-trips to false");

        ItemFilterItem.setFlags(filter, false, true);
        assertFalse(ItemFilterItem.isMatchNbt(filter), "matchNbt round-trips to false");
        assertTrue(ItemFilterItem.isAllowList(filter), "allowList round-trips to true");
    }

    /**
     * {@link ItemFilterItem#toFilter(ItemStack)} builds a filter that matches
     * exactly the configured items: an iron ingot matches an allow filter that
     * contains an iron ingot, and a gold ingot does not.
     */
    @Test
    public void toFilterMatchesTheConfiguredItems() {
        ItemStack filter = new ItemStack(new ItemFilterItem());
        ItemStack[] stacks = new ItemStack[9];
        stacks[0] = new ItemStack(Items.iron_ingot);
        ItemFilterItem.setStacks(filter, stacks);
        ItemFilterItem.setFlags(filter, false, true);

        ItemFilters.Filter filterPredicate = ItemFilterItem.toFilter(filter);
        FilterKey iron = StackKeys.filterKeyOf(new ItemStack(Items.iron_ingot));
        FilterKey gold = StackKeys.filterKeyOf(new ItemStack(Items.gold_ingot));
        assertTrue(filterPredicate.matches(iron), "an iron ingot matches an allow filter containing an iron ingot");
        assertFalse(filterPredicate.matches(gold), "a gold ingot does not match that filter");
    }
}
