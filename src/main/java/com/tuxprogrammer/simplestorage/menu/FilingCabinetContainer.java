package com.tuxprogrammer.simplestorage.menu;
// Ported from Tom's Simple Storage (MIT):
// https://github.com/tom5454/Toms-Storage/blob/95bb61d/NeoForge/src/platform-shared/java/com/tom/storagemod/menu/FilingCabinetMenu.java
// Avoids: TERM-13

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import com.tuxprogrammer.simplestorage.blockentity.FilingCabinetTileEntity;
import com.tuxprogrammer.simplestorage.terminal.ScrollMath;

/**
 * The container of the filing cabinet: a scrolling 9 by 5 window over the cabinet's 512 slots, plus the player's
 * inventory and hotbar.
 *
 * <p>
 * Only the 45 window slots are the cabinet's; they are backed by a {@link WindowInventory} that maps slot
 * {@code i} to cabinet slot {@code scrollRow * 9 + i}. The visible window moves when {@link #setScrollRow(int)} is
 * called, so the container does not expose all 512 slots at once.
 *
 * <p>
 * TERM-13: the upstream code scanned only the visible 45-slot window when deciding whether an item could be placed,
 * so an empty visible region could accept a different item type than the cabinet's lock. This port instead asks
 * the cabinet itself (via {@code isItemValidForSlot}) for the whole-cabinet lock, whatever row is on screen.
 *
 * <p>
 * Side: server logic; the client display lives in simplestorage.client.
 */
public class FilingCabinetContainer extends Container {

    /** Number of columns in the cabinet window. */
    public static final int COLUMNS = 9;

    /** Number of rows visible in the cabinet window. */
    public static final int VISIBLE_ROWS = 5;

    /** Number of slots in the cabinet window (COLUMNS * VISIBLE_ROWS). */
    public static final int WINDOW_SLOTS = 45;

    /** Total scrollable rows needed to reach all 512 cabinet slots (ceil(512/9)). */
    public static final int TOTAL_ROWS = 57;

    /** Y coordinate of the player's main inventory in the window. */
    public static final int INVENTORY_Y = 122;

    /** Y coordinate of the player's hotbar in the window. */
    public static final int HOTBAR_Y = 180;

    /** The cabinet tile entity the window scrolls over. */
    private final FilingCabinetTileEntity cabinet;

    /** The cabinet window, backed by the current scroll row. */
    private final WindowInventory window;

    /** The current scroll row of the window (0..maxScrollRows). */
    private int scrollRow;

    /**
     * Builds a filing cabinet container.
     *
     * @param inventory the player's inventory
     * @param cabinet   the cabinet to open
     */
    public FilingCabinetContainer(InventoryPlayer inventory, FilingCabinetTileEntity cabinet) {
        this.cabinet = cabinet;
        this.window = new WindowInventory();

        // The 45 window slots in a 9 x 5 grid.
        for (int row = 0; row < VISIBLE_ROWS; row++) {
            for (int col = 0; col < COLUMNS; col++) {
                int index = row * COLUMNS + col;
                addSlotToContainer(new CabinetSlot(window, index, 8 + 18 * col, 18 + 18 * row));
            }
        }

        // The player's 27 inventory slots.
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlotToContainer(new Slot(inventory, 9 + row * 9 + col, 8 + 18 * col, INVENTORY_Y + 18 * row));
            }
        }

        // The player's 9 hotbar slots.
        for (int col = 0; col < 9; col++) {
            addSlotToContainer(new Slot(inventory, col, 8 + 18 * col, HOTBAR_Y));
        }
    }

    /**
     * The cabinet this container scrolls over.
     *
     * @return the cabinet tile entity
     */
    public FilingCabinetTileEntity getCabinet() {
        return cabinet;
    }

    /**
     * The current scroll row of the window.
     *
     * @return the scroll row
     */
    public int getScrollRow() {
        return scrollRow;
    }

    /**
     * Moves the window to another row. The row is clamped to the range that can reach all 512 cabinet slots, stored,
     * and the container then sends its changes so the client's slots resync to the new row.
     *
     * @param row the new scroll row (clamped to 0..maxScrollRows)
     */
    public void setScrollRow(int row) {
        this.scrollRow = Math.max(0, Math.min(row, ScrollMath.maxScrollRows(512, COLUMNS, VISIBLE_ROWS)));
        detectAndSendChanges();
    }

    /**
     * Whether the player may still interact with the cabinet (it is in range and not destroyed).
     *
     * @param p the player
     * @return true if the player can interact
     */
    @Override
    public boolean canInteractWith(EntityPlayer p) {
        return cabinet.isUseableByPlayer(p);
    }

    /**
     * Shift-click transfer. From a window slot the whole stack goes into the player's slots; from a player slot one
     * item at a time goes into the first empty window slot whose cabinet slot accepts it.
     *
     * @param p     the player
     * @param index the clicked slot's id in this container
     * @return a copy of the original stack if anything moved, otherwise null
     */
    @Override
    public ItemStack transferStackInSlot(EntityPlayer p, int index) {
        Slot slot = getSlot(index);
        if (slot == null || !slot.getHasStack()) {
            return null;
        }
        ItemStack original = slot.getStack()
            .copy();

        if (index < WINDOW_SLOTS) {
            // Window slot -> player slots (ids 45..80); mergeItemStack reduces the stack in place.
            if (mergeItemStack(slot.getStack(), 45, 81, true)) {
                if (slot.getStack().stackSize <= 0) {
                    slot.putStack(null);
                } else {
                    slot.onSlotChanged();
                }
                return original;
            }
        } else {
            // Player slot -> first empty window slot whose cabinet slot accepts the stack.
            for (int i = 0; i < WINDOW_SLOTS; i++) {
                Slot target = getSlot(i);
                if (target == null || target.getHasStack() || !target.isItemValid(slot.getStack())) {
                    continue;
                }
                ItemStack single = slot.getStack()
                    .copy();
                single.stackSize = 1;
                target.putStack(single);
                slot.getStack().stackSize -= 1;
                if (slot.getStack().stackSize <= 0) {
                    slot.putStack(null);
                } else {
                    slot.onSlotChanged();
                }
                return original;
            }
        }
        return null;
    }

    /**
     * A slot over the scrolling cabinet window.
     */
    private static class CabinetSlot extends Slot {

        /**
         * Creates a cabinet window slot.
         *
         * @param inventory the window inventory
         * @param index     the slot index within the window (0..44)
         * @param x         the x coordinate in the GUI
         * @param y         the y coordinate in the GUI
         */
        public CabinetSlot(WindowInventory inventory, int index, int x, int y) {
            super(inventory, index, x, y);
        }

        /**
         * Whether the given stack is valid here. Delegates to the window, which asks the whole cabinet (TERM-13).
         *
         * @param stack the stack to check
         * @return true if the stack may be placed in this window slot
         */
        @Override
        public boolean isItemValid(ItemStack stack) {
            return inventory.isItemValidForSlot(getSlotIndex(), stack);
        }
    }

    /**
     * A 45-slot view of the cabinet window: slot {@code i} reads and writes cabinet slot {@code scrollRow * 9 + i} at
     * the container's current scroll row. Cabinet slots at or beyond the cabinet's 512 read as empty and refuse
     * items.
     *
     * <p>
     * Side: server logic; the client display lives in simplestorage.client.
     */
    private class WindowInventory extends InventoryView {

        /**
         * The cabinet slot index for a window index at the container's current scroll row.
         *
         * @param i the window index (0..44)
         * @return the corresponding cabinet slot index (may be >= 512 near the bottom)
         */
        private int offset(int i) {
            return scrollRow * COLUMNS + i;
        }

        /**
         * @return the number of slots in the window
         */
        @Override
        public int getSizeInventory() {
            return WINDOW_SLOTS;
        }

        /**
         * The stack at a window slot, reading through to the cabinet.
         *
         * @param slotIndex the window index (0..44)
         * @return the stack, or null if the cabinet slot is empty or past the end of the cabinet
         */
        @Override
        public ItemStack getStackInSlot(int slotIndex) {
            int c = offset(slotIndex);
            if (c >= 512) {
                return null;
            }
            return cabinet.getStackInSlot(c);
        }

        /**
         * Writes a stack to a window slot, through to the cabinet.
         *
         * @param slotIndex the window index (0..44)
         * @param stack     the stack to store, or null to empty the slot
         */
        @Override
        public void setInventorySlotContents(int slotIndex, ItemStack stack) {
            // Cabinet slots past the end ignore writes (they read empty and refuse items).
            int c = offset(slotIndex);
            if (c >= 512) {
                return;
            }
            cabinet.setInventorySlotContents(c, stack);
        }

        /**
         * Whether a stack may be placed in a window slot. Delegates to the whole-cabinet check so the visible window
         * honours the cabinet's lock regardless of the scroll row (TERM-13); slots past the end refuse everything.
         *
         * @param slotIndex the window index (0..44)
         * @param stack     the stack to check
         * @return true if the cabinet's corresponding slot accepts the stack
         */
        @Override
        public boolean isItemValidForSlot(int slotIndex, ItemStack stack) {
            int c = offset(slotIndex);
            if (c >= 512) {
                return false;
            }
            return cabinet.isItemValidForSlot(c, stack);
        }

        /**
         * The maximum stack size in a window slot.
         *
         * @return 1, since the cabinet stores one item per slot
         */
        @Override
        public int getInventoryStackLimit() {
            return 1;
        }

        /**
         * Marks the window dirty.
         */
        @Override
        public void markDirty() {
            cabinet.markDirty();
        }
    }
}
