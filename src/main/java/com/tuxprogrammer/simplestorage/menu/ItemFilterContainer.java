package com.tuxprogrammer.simplestorage.menu;
// Ported from Tom's Simple Storage (MIT):
// https://github.com/tom5454/Toms-Storage/blob/95bb61d/NeoForge/src/platform-shared/java/com/tom/storagemod/menu/ItemFilterMenu.java
// Avoids: TERM-11

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import com.tuxprogrammer.simplestorage.blockentity.StackKeys;
import com.tuxprogrammer.simplestorage.filter.FilterSlotLogic;
import com.tuxprogrammer.simplestorage.item.ItemFilterItem;

/**
 * The server-side container for the item filter's editing GUI.
 *
 * <p>
 * The filter the player is editing is the item held in their hand: its nine
 * example stacks live in a private in-memory {@link ItemStack[9]} exposed as
 * nine ghost slots, and every edit is written straight back into the held
 * item's NBT via {@link ItemFilterItem#setStacks(ItemStack, ItemStack[])} or
 * {@link ItemFilterItem#setFlags(ItemStack, boolean, boolean)}. The hotbar
 * slot the filter was held in is remembered at construction; the held stack is
 * always re-read live from the player's inventory so a swap is seen.
 *
 * <p>
 * Clicking a ghost slot stamps a size-1 copy of the cursor stack into that
 * slot (or clears it when the cursor is empty) without consuming the cursor,
 * so the player can lay out nine examples while still holding the stack.
 *
 * <p>
 * The player's 27 inventory and 9 hotbar slots are ordinary {@link Slot}s at
 * fixed positions, and shift-clicking one moves the item into the ghost grid:
 * to an identical ghost slot when one exists, else to the first empty ghost
 * slot. An item whose only identical ghost slot comes after an empty slot is
 * never duplicated (TERM-11), because the first empty slot wins.
 *
 * <p>
 * A click on the player-inventory slot that holds the held filter item itself
 * is ignored, so the player does not accidentally drag the filter out of their
 * hand. The container stays interactable while the held item is still an
 * {@link ItemFilterItem}; the flags are applied by
 * {@link #applyFlags(boolean, boolean)} from the {@code FilterFlagsPacket}.
 *
 * Side: server logic; the client display lives in simplestorage.client.
 */
public class ItemFilterContainer extends Container {

    /** Y position (px) of the player's 27-slot inventory, relative to the screen. */
    public static final int INVENTORY_Y = 84;

    /** Y position (px) of the player's hotbar, relative to the screen. */
    public static final int HOTBAR_Y = 142;

    /** How many ghost slots the filter holds, in three rows of three. */
    private static final int GHOST_SLOTS = 9;

    /** The player's inventory, whose slots are added here and whose held item is the filter being edited. */
    private final InventoryPlayer inventory;

    /** The hotbar index the held filter item was in when the container was built. */
    private final int heldIndex;

    /** The in-memory view the nine ghost slots read and write through. */
    private final GhostView ghostView;

    /**
     * Creates the container and lays out the ghost grid and the player slots.
     *
     * @param inventory the player's inventory, whose slots are added here and whose
     *                  held item is the filter being edited
     */
    public ItemFilterContainer(InventoryPlayer inventory) {
        this.inventory = inventory;
        this.heldIndex = inventory.currentItem;
        ItemStack held = inventory.mainInventory[heldIndex];
        // The in-memory ghost array is loaded once from the held item's NBT.
        ItemStack[] ghosts = new ItemStack[GHOST_SLOTS];
        System.arraycopy(ItemFilterItem.getStacks(held), 0, ghosts, 0, GHOST_SLOTS);
        this.ghostView = new GhostView(ghosts);
        // Nine ghost slots, in three rows of three.
        for (int i = 0; i < GHOST_SLOTS; i++) {
            addSlotToContainer(new GhostSlot(ghostView, i, 62 + 18 * (i % 3), 17 + 18 * (i / 3)));
        }
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
     * filter item; a swap of the held item (e.g. a death or a mod changing the
     * hand) makes the grid's target invalid.
     *
     * @param p the player asking
     * @return true if {@code p}'s held item is an {@link ItemFilterItem}
     */
    @Override
    public boolean canInteractWith(EntityPlayer p) {
        ItemStack held = p.inventory.mainInventory[p.inventory.currentItem];
        return held != null && held.getItem() instanceof ItemFilterItem;
    }

    /**
     * Persists the filter's flags into the held item.
     *
     * @param matchNbt  the new value of "matchNbt"
     * @param allowList the new value of "allowList"
     */
    public void applyFlags(boolean matchNbt, boolean allowList) {
        ItemFilterItem.setFlags(inventory.mainInventory[heldIndex], matchNbt, allowList);
    }

    /**
     * The currently held stack of the remembered hotbar slot, re-read live so a
     * swap is seen; null when the slot is empty.
     *
     * @return the held filter stack, or {@code null}
     */
    private ItemStack heldStack() {
        return inventory.mainInventory[heldIndex];
    }

    /**
     * Handles a click on any slot of this container.
     *
     * <p>
     * A click on a ghost slot (slot ids 0..8) stamps the cursor stack (a
     * size-1 copy, cursor not consumed) into that slot, or clears it when the
     * cursor is empty, and returns the cursor stack unchanged. A click on the
     * player-inventory slot that holds the held filter item itself is ignored
     * (returns null) so the filter is not dragged out of the hand. Every other
     * click falls through to vanilla.
     *
     * @param slotId        the slot index that was clicked
     * @param clickedButton the clicked mouse button (unused)
     * @param mode          the click mode (unused)
     * @param player        the player clicking
     * @return the cursor stack unchanged for a ghost click, null for a click on
     *         the held filter's own slot, or vanilla's result otherwise
     */
    @Override
    public ItemStack slotClick(int slotId, int clickedButton, int mode, EntityPlayer player) {
        // Ghost slots occupy slot ids 0..8: stamp or clear, never consume the cursor.
        if (slotId >= 0 && slotId < GHOST_SLOTS) {
            ItemStack carried = inventory.getItemStack();
            if (carried != null) {
                ItemStack one = carried.copy();
                one.stackSize = 1;
                getSlot(slotId).putStack(one);
            } else {
                getSlot(slotId).putStack((ItemStack) null);
            }
            return carried;
        }
        // The ghost slots precede the 36 player slots; the held filter item lives
        // at hotbar index heldIndex, i.e. slot id 36 + heldIndex. A click there
        // would drag the filter out of the hand, so ignore it entirely.
        if (slotId == GHOST_SLOTS + 9 + 9 + heldIndex) {
            return null;
        }
        return super.slotClick(slotId, clickedButton, mode, player);
    }

    /**
     * Shift-click from a player slot: move the item into the ghost grid.
     *
     * <p>
     * Builds the keys of the nine ghost slots and asks
     * {@link FilterSlotLogic#shiftClickTarget(String[], String)} for the target:
     * an identical ghost slot when one exists, else the first empty ghost slot,
     * else -1. When the target is an empty ghost slot the item is placed there
     * as a size-1 copy. Because {@code shiftClickTarget} prefers an identical
     * slot but then the first empty slot, an item whose only identical slot
     * comes after an empty slot lands in the empty slot and is not duplicated
     * (TERM-11).
     *
     * @param p     the player
     * @param index the clicked slot index
     * @return null (the item's movement is handled by the container's own slots)
     */
    @Override
    public ItemStack transferStackInSlot(EntityPlayer p, int index) {
        if (index >= GHOST_SLOTS) {
            Slot slot = getSlot(index);
            ItemStack stack = slot == null ? null : slot.getStack();
            if (stack != null) {
                String[] keys = new String[GHOST_SLOTS];
                for (int i = 0; i < GHOST_SLOTS; i++) {
                    ItemStack ghost = getSlot(i).getStack();
                    keys[i] = ghost == null ? null : StackKeys.keyOf(ghost);
                }
                int target = FilterSlotLogic.shiftClickTarget(keys, StackKeys.keyOf(stack));
                if (target >= 0 && getSlot(target).getStack() == null) {
                    ItemStack one = stack.copy();
                    one.stackSize = 1;
                    getSlot(target).putStack(one);
                }
            }
        }
        return null;
    }

    /**
     * A slot of the ghost grid: the grid's nine {@link ItemStack}s live in the
     * in-memory {@link GhostView} and are written back into the held item's NBT
     * on every change, so the grid is never backed by the held item's NBT
     * between clicks.
     */
    private static class GhostSlot extends Slot {

        /**
         * @param view  the in-memory view the slot reads and writes through
         * @param index the slot's index into the view (0..8)
         * @param x     the slot's x position, px
         * @param y     the slot's y position, px
         */
        GhostSlot(GhostView view, int index, int x, int y) {
            super(view, index, x, y);
        }

        /**
         * @param stack the offered stack
         * @return always true: any stack may be stamped into a ghost slot
         */
        @Override
        public boolean isItemValid(ItemStack stack) {
            return true;
        }
    }

    /**
     * A nine-slot {@link InventoryView} over the in-memory ghost array; every
     * write is mirrored straight back into the held item's NBT via
     * {@link ItemFilterItem#setStacks(ItemStack, ItemStack[])}.
     */
    private class GhostView extends InventoryView {

        /** The nine example stacks, held in memory. */
        private final ItemStack[] ghosts;

        /**
         * @param ghosts the in-memory ghost array to view
         */
        GhostView(ItemStack[] ghosts) {
            this.ghosts = ghosts;
        }

        /**
         * @return the number of ghost slots (always
         *         {@value com.tuxprogrammer.simplestorage.item.ItemFilterItem#GHOST_SLOTS})
         */
        @Override
        public int getSizeInventory() {
            return ghosts.length;
        }

        /**
         * @param slot the ghost slot index
         * @return the stack in that slot, or {@code null} when empty
         */
        @Override
        public ItemStack getStackInSlot(int slot) {
            return ghosts[slot];
        }

        /**
         * Replaces the stack in a ghost slot and writes the whole grid back into
         * the held item's NBT.
         *
         * @param slot  the ghost slot index
         * @param stack the stack to place ({@code null} empties the slot)
         */
        @Override
        public void setInventorySlotContents(int slot, ItemStack stack) {
            ghosts[slot] = stack;
            ItemStack heldStack = heldStack();
            if (heldStack != null) {
                ItemFilterItem.setStacks(heldStack, ghosts);
            }
        }
    }
}
