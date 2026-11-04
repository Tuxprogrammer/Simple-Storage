package com.tuxprogrammer.simplestorage.menu;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import com.tuxprogrammer.simplestorage.blockentity.LevelEmitterTileEntity;

/**
 * The server-side container for the level emitter: the single ghost filter
 * slot plus the player's inventory and hotbar.
 *
 * <p>
 * The filter slot is a ghost: it never takes items out of the player. Clicking
 * it with a stack in the cursor sets the emitter's filter to a size-1 copy of
 * that stack; clicking it with an empty cursor clears the filter. The cursor
 * stack is never consumed, so the player keeps holding it. The player's 27
 * inventory slots and 9 hotbar slots are ordinary {@link Slot}s, laid out at
 * fixed positions.
 *
 * Side: server logic; the client display lives in simplestorage.client.
 */
public class LevelEmitterContainer extends Container {

    /** X position (px) of the ghost filter slot, relative to the screen. */
    public static final int FILTER_X = 80;

    /** Y position (px) of the ghost filter slot, relative to the screen. */
    public static final int FILTER_Y = 20;

    /** Y position (px) of the top row of the player's inventory, relative to the screen. */
    public static final int INVENTORY_Y = 51;

    /** Y position (px) of the player's hotbar, relative to the screen. */
    public static final int HOTBAR_Y = 109;

    /** The emitter this container configures. */
    private final LevelEmitterTileEntity emitter;

    /**
     * Creates the container and lays out the ghost filter slot and the player
     * slots.
     *
     * @param inventory the player's inventory, whose slots are added here
     * @param emitter   the emitter backing this container
     */
    public LevelEmitterContainer(InventoryPlayer inventory, LevelEmitterTileEntity emitter) {
        this.emitter = emitter;

        // The ghost filter slot is always slot 0.
        FilterView view = new FilterView(emitter);
        addSlotToContainer(new GhostSlot(view, FILTER_X, FILTER_Y));

        // 27 inventory slots, in three rows of nine.
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                int index = 9 + row * 9 + column;
                addSlotToContainer(new Slot(inventory, index, 8 + 18 * column, INVENTORY_Y + 18 * row));
            }
        }
        // 9 hotbar slots.
        for (int column = 0; column < 9; column++) {
            addSlotToContainer(new Slot(inventory, column, 8 + 18 * column, HOTBAR_Y));
        }
    }

    /**
     * @return the emitter backing this container.
     */
    public LevelEmitterTileEntity getEmitter() {
        return emitter;
    }

    /**
     * A player may use this container only while the emitter is still the
     * tile entity at its position in a live world and the player is within
     * 8 blocks of it.
     *
     * @param p the player asking
     * @return true if {@code p} may interact with the emitter
     */
    @Override
    public boolean canInteractWith(EntityPlayer p) {
        return emitter.getWorldObj() != null && emitter.getWorldObj()
            .getTileEntity(emitter.xCoord, emitter.yCoord, emitter.zCoord) == emitter
            && p.getDistanceSq(emitter.xCoord + 0.5, emitter.yCoord + 0.5, emitter.zCoord + 0.5) < 64.0;
    }

    /**
     * Clicking the ghost filter slot (slot 0) with a stack in the cursor sets
     * the filter to a size-1 copy of it; clicking with an empty cursor clears
     * the filter. The cursor stack is never consumed: it is returned to the
     * caller unchanged (null when empty). Any other click is passed to
     * vanilla.
     *
     * @param slotId        the clicked slot's index
     * @param clickedButton the clicked mouse button
     * @param mode          the click mode
     * @param p             the player clicking
     * @return the cursor stack, unchanged, or null when the cursor is empty
     */
    @Override
    public ItemStack slotClick(int slotId, int clickedButton, int mode, EntityPlayer p) {
        if (slotId == 0) {
            ItemStack carried = p.inventory.getItemStack();
            if (carried != null) {
                emitter.setFilter(carried.copy());
            } else {
                emitter.setFilter(null);
            }
            p.inventory.markDirty();
            return carried;
        }
        return super.slotClick(slotId, clickedButton, mode, p);
    }

    /**
     * No items move by shift-click: the ghost slot takes nothing from the
     * player, and player-to-player shifting is not needed.
     *
     * @param p     the player shift-clicking
     * @param index the clicked slot index
     * @return always null
     */
    @Override
    public ItemStack transferStackInSlot(EntityPlayer p, int index) {
        return null;
    }

    /**
     * Persists the emitter's threshold and comparison mode.
     *
     * @param count    the new threshold, clamped to 0..1073741823
     * @param lessThan the new comparison mode
     */
    public void applySettings(int count, boolean lessThan) {
        int clamped = count;
        if (clamped < 0) {
            clamped = 0;
        }
        int max = Integer.MAX_VALUE / 2;
        if (clamped > max) {
            clamped = max;
        }
        emitter.setCount(clamped);
        emitter.setLessThan(lessThan);
    }

    /**
     * The one filter slot: it is backed by the one-slot {@link FilterView} of
     * the emitter's filter and accepts any item; the container itself
     * intercepts clicks on it to set or clear the filter.
     */
    private static class GhostSlot extends Slot {

        /**
         * Creates the ghost slot over a one-slot view of the emitter's filter.
         *
         * @param view the one-slot view backing this slot
         * @param x    the slot's x position
         * @param y    the slot's y position
         */
        private GhostSlot(FilterView view, int x, int y) {
            super(view, 0, x, y);
        }

        /**
         * The slot accepts any item; the filter takes whatever the player
         * holds.
         *
         * @param stack the candidate stack
         * @return always true
         */
        @Override
        public boolean isItemValid(ItemStack stack) {
            return true;
        }
    }

    /**
     * A tiny one-slot view of the emitter's filter: it reads and writes the
     * filter directly, so the ghost slot can display it without ever taking
     * items from the player.
     */
    private static class FilterView extends InventoryView {

        /** The emitter whose filter this view exposes. */
        private final LevelEmitterTileEntity emitter;

        /**
         * Creates the view.
         *
         * @param emitter the emitter whose filter this view exposes
         */
        private FilterView(LevelEmitterTileEntity emitter) {
            this.emitter = emitter;
        }

        /**
         * @return always 1: the view exposes only the filter
         */
        @Override
        public int getSizeInventory() {
            return 1;
        }

        /**
         * @param slot the requested slot index
         * @return the emitter's filter
         */
        @Override
        public ItemStack getStackInSlot(int slot) {
            return emitter.getFilter();
        }

        /**
         * Writes the filter.
         *
         * @param slot  the target slot index
         * @param stack the new filter, or null to clear it
         */
        @Override
        public void setInventorySlotContents(int slot, ItemStack stack) {
            emitter.setFilter(stack);
        }
    }
}
