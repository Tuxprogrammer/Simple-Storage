package com.tuxprogrammer.simplestorage.menu;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCraftResult;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.inventory.SlotCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.world.World;

import com.tuxprogrammer.simplestorage.blockentity.CraftingTerminalTileEntity;
import com.tuxprogrammer.simplestorage.blockentity.StackKeys;
import com.tuxprogrammer.simplestorage.blockentity.TerminalBackend;
import com.tuxprogrammer.simplestorage.blockentity.TerminalEntry;
import com.tuxprogrammer.simplestorage.terminal.AutoFillPlanner;
import com.tuxprogrammer.simplestorage.terminal.IAutoFillTerminal;
import com.tuxprogrammer.simplestorage.terminal.TerminalModes;

/**
 * The server-side container for the crafting terminal: a storage terminal with
 * a working 3x3 crafting grid and a result slot.
 *
 * <p>
 * The grid is exposed to the vanilla crafting machinery as an
 * {@link InventoryCrafting} (the vanilla workbench does exactly this): it is
 * seeded from the tile entity's grid at construction and written back to the
 * tile entity both when the matrix changes ({@link #onCraftMatrixChanged}) and
 * when the container is closed ({@link #onContainerClosed}). A single
 * {@link SlotCrafting} at (124, 38) presents the result of
 * {@link CraftingManager#getInstance()} matching the matrix. The player's
 * inventory and hotbar are inherited from {@link StorageTerminalContainer}.
 *
 * <p>
 * The grid slots themselves are ordinary {@link Slot}s over the tile entity's
 * grid, so drag and shift-click work on them; the nested {@link GridInventory}
 * is the {@code IInventory} view those slots read and write. The crafting
 * terminal's auto-fill of the grid from the network is a later milestone item
 * and is not implemented here.
 *
 * Side: server logic; the client display lives in simplestorage.client.
 */
public class CraftingTerminalContainer extends StorageTerminalContainer implements IAutoFillTerminal {

    /** The tile entity whose 3x3 grid backs this container. */
    private final CraftingTerminalTileEntity terminal;

    /** The 3x3 crafting matrix; the vanilla crafting machinery reads and writes it. */
    private final InventoryCrafting matrix;

    /** The result inventory the recipe is computed into. */
    private final InventoryCraftResult result;

    /**
     * Creates the container: the inherited storage/player slots plus the 3x3
     * grid slots and the result slot.
     *
     * @param inventory the player's inventory, whose slots are added here
     * @param terminal  the crafting terminal backing this container
     */
    public CraftingTerminalContainer(InventoryPlayer inventory, CraftingTerminalTileEntity terminal) {
        super(inventory, terminal);
        this.terminal = terminal;
        this.matrix = new InventoryCrafting(this, 3, 3);
        this.result = new InventoryCraftResult();
        final GridInventory grid = new GridInventory(terminal);

        // Seed the matrix from the persisted grid so the result starts correct.
        for (int i = 0; i < 9; i++) {
            matrix.setInventorySlotContents(i, terminal.getGridStack(i));
        }

        // The 3x3 grid as ordinary slots, laid out left-to-right, top-to-bottom.
        for (int i = 0; i < 9; i++) {
            addSlotToContainer(new Slot(grid, i, 30 + 18 * (i % 3), 20 + 18 * (i / 3)));
        }
        // The single result slot.
        addSlotToContainer(new SlotCrafting(inventory.player, matrix, result, 0, 124, 38));

        recalculate();
    }

    /**
     * Re-runs the recipe match over the matrix and updates the result slot.
     */
    private void recalculate() {
        World world = terminal.getWorldObj();
        result.setInventorySlotContents(
            0,
            CraftingManager.getInstance()
                .findMatchingRecipe(matrix, world));
    }

    /**
     * Called by the {@link InventoryCrafting} whenever a matrix slot changes:
     * recompute the result and keep the tile entity's grid in step.
     *
     * @param inventory the crafting matrix
     */
    @Override
    public void onCraftMatrixChanged(IInventory inventory) {
        recalculate();
        writeBackToTerminal();
    }

    /**
     * When the container is closed, the matrix is written back to the tile
     * entity and the tile is marked dirty so the grid is persisted.
     *
     * @param p the player closing the container
     */
    @Override
    public void onContainerClosed(EntityPlayer p) {
        super.onContainerClosed(p);
        writeBackToTerminal();
    }

    /**
     * Copies the matrix into the tile entity's grid and marks it dirty.
     */
    private void writeBackToTerminal() {
        for (int i = 0; i < 9; i++) {
            terminal.setGridStack(i, matrix.getStackInSlot(i));
        }
        terminal.markDirty();
    }

    /**
     * The tile entity whose grid backs this container.
     *
     * @return the crafting terminal
     */
    public CraftingTerminalTileEntity getCraftingTerminal() {
        return terminal;
    }

    /**
     * Stores the search text on the terminal so the server keeps the search
     * the player ran.
     *
     * @param text the search text to store
     */
    @Override
    public void updateSearch(String text) {
        getTerminal().setLastSearch(text);
    }

    /**
     * Reports a result to the player as a lang key: sends a chat component to
     * the viewer when it is a connected player.
     *
     * @param langKey the lang key of the message
     * @param args    the translation arguments
     */
    @Override
    public void sendMessage(String langKey, Object... args) {
        if (this.viewer != null) {
            this.viewer.addChatMessage(new ChatComponentTranslation(langKey, args));
        }
    }

    /**
     * @return what the network currently holds, as of the last sync
     */
    @Override
    public List<TerminalEntry> getStoredItems() {
        return getTerminal().backend()
            .snapshot();
    }

    /**
     * @return whether smart search is on (searchType bit 8 clear)
     */
    @Override
    public boolean isSmartSearch() {
        return TerminalModes.isSmartSearch(getTerminal().getSearchType());
    }

    /**
     * Fills the 3x3 crafting grid with the wanted items for a recipe: the
     * pulls are planned per cell ({@link AutoFillPlanner}) so that two cells
     * wanting the same scarce item cannot both claim the same unit (the
     * original spec's TERM-05), each cell's items are taken from the player's
     * inventory when the player has them and otherwise from the network, and
     * one stack per filled cell is placed into the grid.
     *
     * <p>
     * After the grid is filled the matrix is refreshed (the result slot is
     * recomputed, the grid is written back to the tile entity, and the
     * changes are sent to the viewer), so the player sees the fill and the
     * matching result immediately.
     *
     * @param player     the player who opened the terminal
     * @param wantedKeys the 9 recipe grid cells (null = empty cell; each
     *                   non-null entry is a stack key)
     * @param crafts     how many crafts to fill for (values &lt;= 0 behave
     *                   like 1)
     * @return true when every wanted cell is satisfied, false when some item
     *         was missing (in which case the {@code
     *         simplestoragenh.terminal.autofill.missing} message is sent)
     */
    public boolean autoFill(EntityPlayerMP player, String[] wantedKeys, int crafts) {
        TerminalBackend backend = getTerminal().backend();
        Map<String, Long> available = availableCounts(player, backend);
        String[] current = gridKeys();

        AutoFillPlanner.Plan plan = AutoFillPlanner.plan(wantedKeys, current, available, crafts);

        for (int i = 0; i < 9; i++) {
            if (plan.take[i] <= 0) {
                // Empty cell, or the cell already holds the wanted item and
                // keeps its existing stack in place.
                continue;
            }
            ItemStack stack = null;
            int remaining = plan.take[i];
            // Take as many as the player's own inventory can cover first.
            ItemStack taken = takeFromPlayer(player, wantedKeys[i], remaining);
            if (taken != null) {
                stack = taken;
                remaining -= taken.stackSize;
            }
            // Whatever the player did not have comes from the network: the
            // backend caps each pull at one max stack, so pull repeatedly
            // until the cell's amount has arrived.
            while (remaining > 0) {
                ItemStack pulled = backend.pull(wantedKeys[i], remaining);
                if (pulled == null || pulled.stackSize <= 0) {
                    break;
                }
                remaining -= pulled.stackSize;
                if (stack == null) {
                    stack = pulled;
                } else {
                    stack.stackSize = Math.min(stack.getMaxStackSize(), stack.stackSize + pulled.stackSize);
                }
            }
            // One stack per filled cell, respecting the stack's max size.
            int maxSize = stack.getMaxStackSize();
            int size = Math.min(stack.stackSize, maxSize);
            if (size <= 0) {
                size = maxSize;
            }
            ItemStack cellStack = stack.copy();
            cellStack.stackSize = size;
            terminal.setGridStack(i, cellStack);
            matrix.setInventorySlotContents(i, cellStack);
        }

        onCraftMatrixChanged(matrix); // recompute the result, write back, and
        detectAndSendChanges(); // send the updated slots to the viewer.

        boolean complete = true;
        String[] after = gridKeys();
        for (int i = 0; i < 9; i++) {
            if (wantedKeys != null && i < wantedKeys.length
                && wantedKeys[i] != null
                && plan.take[i] <= 0
                && after[i] == null) {
                complete = false;
                break;
            }
        }
        if (!complete) {
            sendMessage("simplestoragenh.terminal.autofill.missing");
        }
        return complete;
    }

    /**
     * The keys of the current 9 grid stacks (null for an empty cell), in
     * row-major grid order.
     */
    private String[] gridKeys() {
        String[] keys = new String[9];
        for (int i = 0; i < 9; i++) {
            ItemStack stack = terminal.getGridStack(i);
            keys[i] = stack == null ? null : StackKeys.keyOf(stack);
        }
        return keys;
    }

    /**
     * The total available count per stack key: the network (the backend's
     * snapshot) plus the player's own main-inventory stacks, keyed the same
     * way the backend keys its stacks.
     */
    private static Map<String, Long> availableCounts(EntityPlayerMP player, TerminalBackend backend) {
        Map<String, Long> available = new LinkedHashMap<String, Long>();
        for (TerminalEntry entry : backend.snapshot()) {
            available.put(entry.key(), Long.valueOf(entry.count()));
        }
        for (int i = 0; i < player.inventory.getSizeInventory(); i++) {
            ItemStack stack = player.inventory.getStackInSlot(i);
            if (stack == null) {
                continue;
            }
            String key = StackKeys.keyOf(stack);
            Long count = available.get(key);
            available.put(key, Long.valueOf((count == null ? 0L : count.longValue()) + stack.stackSize));
        }
        return available;
    }

    /**
     * Pulls up to {@code amount} of the stack with the given key out of the
     * player's own inventory, one stack at a time, and returns what was
     * taken, or {@code null} when the player had nothing.
     */
    private static ItemStack takeFromPlayer(EntityPlayerMP player, String key, int amount) {
        ItemStack taken = null;
        for (int i = 0; i < player.inventory.getSizeInventory() && amount > 0; i++) {
            ItemStack slotStack = player.inventory.getStackInSlot(i);
            if (slotStack == null || !key.equals(StackKeys.keyOf(slotStack))) {
                continue;
            }
            int move = Math.min(slotStack.stackSize, amount);
            ItemStack removed = player.inventory.decrStackSize(i, move);
            if (removed == null || removed.stackSize <= 0) {
                continue;
            }
            amount -= removed.stackSize;
            if (taken == null) {
                taken = removed;
            } else if (taken.isItemEqual(removed) && ItemStack.areItemStackTagsEqual(taken, removed)
                && taken.stackSize < taken.getMaxStackSize()) {
                    taken.stackSize = Math.min(taken.getMaxStackSize(), taken.stackSize + removed.stackSize);
                }
        }
        return taken;
    }

    /**
     * An {@link IInventory} view over the tile entity's 3x3 grid: nine slots,
     * each stack read from and written to the {@link CraftingTerminalTileEntity}
     * directly.
     *
     * <p>
     * This is what the grid {@link Slot}s read and write. It delegates use
     * checks to the tile entity and marks the tile dirty when its contents
     * change.
     *
     * Side: server logic; the client display lives in simplestorage.client.
     */
    private static final class GridInventory implements IInventory {

        private final CraftingTerminalTileEntity terminal;

        /**
         * Creates a grid view.
         *
         * @param terminal the tile entity whose grid is exposed
         */
        GridInventory(CraftingTerminalTileEntity terminal) {
            this.terminal = terminal;
        }

        /**
         * @return the number of grid slots (always 9)
         */
        @Override
        public int getSizeInventory() {
            return 9;
        }

        /**
         * @param slot the grid slot index
         * @return the stack in that slot, or null when empty
         */
        @Override
        public ItemStack getStackInSlot(int slot) {
            return terminal.getGridStack(slot);
        }

        /**
         * @param slot   the grid slot index
         * @param amount the number of items to remove
         * @return the removed portion, or null when the slot was empty
         */
        @Override
        public ItemStack decrStackSize(int slot, int amount) {
            ItemStack stack = terminal.getGridStack(slot);
            if (stack == null) {
                return null;
            }
            ItemStack removed = stack.splitStack(amount);
            if (stack.stackSize <= 0) {
                terminal.setGridStack(slot, null);
            }
            return removed;
        }

        /**
         * @param slot the grid slot index
         * @return the stack in that slot, clearing it (for close-drop handling)
         */
        @Override
        public ItemStack getStackInSlotOnClosing(int slot) {
            ItemStack stack = terminal.getGridStack(slot);
            terminal.setGridStack(slot, null);
            return stack;
        }

        /**
         * @param slot  the grid slot index
         * @param stack the stack to place (null empties the slot)
         */
        @Override
        public void setInventorySlotContents(int slot, ItemStack stack) {
            terminal.setGridStack(slot, stack);
        }

        /**
         * @return the grid's display name
         */
        @Override
        public String getInventoryName() {
            return "craftingGrid";
        }

        /**
         * @return whether the grid has a display name
         */
        @Override
        public boolean hasCustomInventoryName() {
            return false;
        }

        /**
         * @return the maximum stack size for the grid (64)
         */
        @Override
        public int getInventoryStackLimit() {
            return 64;
        }

        /**
         * Marks the tile entity dirty.
         */
        @Override
        public void markDirty() {
            terminal.markDirty();
        }

        /**
         * @param player the player asking
         * @return true if the player may use the grid
         */
        @Override
        public boolean isUseableByPlayer(EntityPlayer player) {
            return terminal.canInteractWith(player);
        }

        /**
         * No action: the grid has no separate open state.
         */
        @Override
        public void openInventory() {}

        /**
         * No action: the grid has no separate close state.
         */
        @Override
        public void closeInventory() {}

        /**
         * @param index the slot index
         * @return true when the slot is in the grid's 0..8 range
         */
        @Override
        public boolean isItemValidForSlot(int index, ItemStack stack) {
            return index >= 0 && index < 9;
        }
    }
}
