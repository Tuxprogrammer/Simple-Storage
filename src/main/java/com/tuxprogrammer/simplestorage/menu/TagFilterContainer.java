package com.tuxprogrammer.simplestorage.menu;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import com.tuxprogrammer.simplestorage.item.TagFilterItem;

/**
 * The server-side container for the tag filter's editing GUI.
 *
 * <p>
 * The filter the player is editing is the item held in their hand. The hotbar
 * slot the filter was held in is remembered at construction; the held stack is
 * always re-read live from the player's inventory so a swap is seen, and every
 * edit is written straight back into the held item's NBT via
 * {@link TagFilterItem#setNames(ItemStack, List)} and
 * {@link TagFilterItem#setFlags(ItemStack, boolean, boolean)}.
 *
 * <p>
 * The player's 27 inventory and 9 hotbar slots are ordinary {@link Slot}s at
 * fixed positions, and nothing else is added. A click on the player-inventory
 * slot that holds the held filter item itself (hotbar index
 * {@code heldIndex}, i.e. slot id {@code 27 + heldIndex}) is ignored, so the
 * player does not accidentally drag the filter out of their hand. The
 * container stays interactable while the held item is still a
 * {@link TagFilterItem}; the settings are applied by
 * {@link #applySettings(List, boolean, boolean)} from the {@code TagFilterPacket}.
 *
 * Side: server logic; the client display lives in simplestorage.client.
 */
public class TagFilterContainer extends Container {

    /** Y position (px) of the player's 27-slot inventory, relative to the screen. */
    public static final int INVENTORY_Y = 84;

    /** Y position (px) of the player's hotbar, relative to the screen. */
    public static final int HOTBAR_Y = 142;

    /** The player's inventory, whose slots are added here and whose held item is the filter being edited. */
    private final InventoryPlayer inventory;

    /** The hotbar index the held filter item was in when the container was built. */
    private final int heldIndex;

    /**
     * Creates the container and lays out the player slots.
     *
     * @param inventory the player's inventory, whose slots are added here and whose
     *                  held item is the filter being edited
     */
    public TagFilterContainer(InventoryPlayer inventory) {
        this.inventory = inventory;
        this.heldIndex = inventory.currentItem;
        // 27 inventory slots, in three rows of nine.
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                int index = 9 + row * 9 + col;
                addSlotToContainer(new Slot(inventory, index, 8 + 18 * col, INVENTORY_Y + 18 * row));
            }
        }
        // 9 hotbar slots.
        for (int col = 0; col < 9; col++) {
            addSlotToContainer(new Slot(inventory, col, 8 + 18 * col, HOTBAR_Y));
        }
    }

    /**
     * The player may use this container only while their held item is still the
     * tag filter item; a swap of the held item (e.g. a death or a mod changing
     * the hand) makes the filter being edited invalid.
     *
     * @param p the player asking
     * @return true if {@code p}'s held item is a {@link TagFilterItem}
     */
    @Override
    public boolean canInteractWith(EntityPlayer p) {
        ItemStack held = p.inventory.mainInventory[p.inventory.currentItem];
        return held != null && held.getItem() instanceof TagFilterItem;
    }

    /**
     * Persists the filter's names and flags into the held item.
     *
     * @param names     the new tag names
     * @param matchAll  the new value of "matchAll"
     * @param allowList the new value of "allowList"
     */
    public void applySettings(List<String> names, boolean matchAll, boolean allowList) {
        ItemStack heldStack = inventory.mainInventory[heldIndex];
        if (heldStack != null) {
            TagFilterItem.setNames(heldStack, names);
            TagFilterItem.setFlags(heldStack, matchAll, allowList);
        }
    }

    /**
     * Handles a click on any slot of this container.
     *
     * <p>
     * A click on the player-inventory slot that holds the held filter item
     * itself is ignored (returns null) so the filter is not dragged out of the
     * hand. Every other click falls through to vanilla.
     *
     * @param slotId        the slot index that was clicked
     * @param clickedButton the clicked mouse button (unused)
     * @param mode          the click mode (unused)
     * @param player        the player clicking
     * @return null for a click on the held filter's own slot, or vanilla's
     *         result otherwise
     */
    @Override
    public ItemStack slotClick(int slotId, int clickedButton, int mode, EntityPlayer player) {
        // The 27 inventory slots precede the 9 hotbar slots; the held filter
        // item lives at hotbar index heldIndex, i.e. slot id 27 + heldIndex.
        // A click there would drag the filter out of the hand, so ignore it.
        if (slotId == 27 + heldIndex) {
            return null;
        }
        return super.slotClick(slotId, clickedButton, mode, player);
    }

    /**
     * Shift-clicking is handled by the vanilla slots.
     *
     * @param p     the player
     * @param index the clicked slot index
     * @return null
     */
    @Override
    public ItemStack transferStackInSlot(EntityPlayer p, int index) {
        return null;
    }
}
