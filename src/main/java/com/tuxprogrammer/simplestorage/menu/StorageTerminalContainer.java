package com.tuxprogrammer.simplestorage.menu;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import com.tuxprogrammer.simplestorage.blockentity.StackKeys;
import com.tuxprogrammer.simplestorage.blockentity.StorageTerminalTileEntity;
import com.tuxprogrammer.simplestorage.blockentity.TerminalBackend;
import com.tuxprogrammer.simplestorage.blockentity.TerminalEntry;
import com.tuxprogrammer.simplestorage.network.SimpleStorageNetwork;
import com.tuxprogrammer.simplestorage.network.TerminalListPacket;
import com.tuxprogrammer.simplestorage.terminal.SlotAction;

/**
 * The server-side container for the storage terminal.
 *
 * <p>
 * The player's inventory (27 slots) and hotbar (9 slots) are ordinary
 * {@link Slot}s at fixed positions, so vanilla shift-click and drag work on
 * them. The storage grid itself is <em>not</em> made of slots: the screen
 * draws the synced list and a click on an entry is delivered to the container
 * as a {@link SlotAction} packet, which {@link #handleAction(EntityPlayerMP, SlotAction, String, boolean)}
 * executes against the network via the terminal's {@link TerminalBackend}.
 *
 * <p>
 * The list is pushed to the client with a {@link TerminalListPacket} from
 * {@link #detectAndSendChanges()}: it is sent on the first tick and then every
 * fifth tick whenever the snapshot differs from what was last sent, so an
 * unchanged network costs nothing.
 *
 * Side: server logic; the client display lives in simplestorage.client.
 */
public class StorageTerminalContainer extends Container {

    /** Number of columns in the storage grid. */
    public static final int STORAGE_COLUMNS = 9;

    /** Number of rows in the storage grid. */
    public static final int STORAGE_ROWS = 5;

    /** X origin (px, relative to the screen) of the storage grid. */
    public static final int STORAGE_X = 8;

    /** Y origin (px, relative to the screen) of the storage grid. */
    public static final int STORAGE_Y = 18;

    /** Y position (px) of the player's 27-slot inventory, relative to the screen. */
    public static final int PLAYER_INV_Y = 122;

    /** Y position (px) of the player's hotbar, relative to the screen. */
    public static final int HOTBAR_Y = 180;

    /** How many {@link #detectAndSendChanges()} calls pass between forced resends. */
    private static final int SYNC_EVERY = 5;

    /**
     * A generous "pull as much as one max-size stack" amount; the backend caps
     * a pull at the item's max stack size, so this only needs to be at least
     * that large (64 covers every vanilla stack size and any mod's).
     */
    private static final int PULL_FULL_STACK = 64;

    /** The terminal whose network this container reads and writes. */
    private final StorageTerminalTileEntity terminal;

    /** The viewer the list is sent to; stored from the constructor's player. */
    protected final EntityPlayerMP viewer;

    /** The number of {@link #detectAndSendChanges()} calls since construction. */
    private int syncCount;

    /** The list last sent to {@link #viewer}, or {@code null} when none has been sent. */
    private List<TerminalEntry> lastSent;

    /**
     * Creates the container and lays out the player slots.
     *
     * @param inventory the player's inventory, whose slots are added here
     * @param terminal  the terminal backing this container
     */
    public StorageTerminalContainer(InventoryPlayer inventory, StorageTerminalTileEntity terminal) {
        this.terminal = terminal;
        this.viewer = inventory.player instanceof EntityPlayerMP ? (EntityPlayerMP) inventory.player : null;
        this.lastSent = null;
        this.syncCount = 0;

        // 27 inventory slots, in three rows of nine.
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                int index = 9 + row * 9 + column;
                addSlotToContainer(new Slot(inventory, index, 8 + 18 * column, PLAYER_INV_Y + 18 * row));
            }
        }
        // 9 hotbar slots.
        for (int column = 0; column < 9; column++) {
            addSlotToContainer(new Slot(inventory, column, 8 + 18 * column, HOTBAR_Y));
        }
    }

    /**
     * @return the terminal backing this container.
     */
    public StorageTerminalTileEntity getTerminal() {
        return terminal;
    }

    /**
     * A player may use this container only while the terminal itself is
     * interactable.
     *
     * @param p the player asking
     * @return true if {@code p} may interact with the terminal
     */
    @Override
    public boolean canInteractWith(EntityPlayer p) {
        return terminal.canInteractWith(p);
    }

    /**
     * After vanilla's own changes are flushed, pushes the storage list to the
     * connected player.
     *
     * <p>
     * The list is built from a fresh backend snapshot and sent through the
     * network channel only when it differs from the previous send, and only on
     * the first tick or every {@value #SYNC_EVERY}th tick thereafter.
     */
    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();

        if (viewer == null) {
            return;
        }
        syncCount++;
        boolean first = lastSent == null;
        if (!first && syncCount % SYNC_EVERY != 0) {
            return;
        }

        List<TerminalEntry> now = terminal.backend()
            .snapshot();
        if (first || !sameList(lastSent, now)) {
            lastSent = now;
            SimpleStorageNetwork.channel()
                .sendTo(new TerminalListPacket(windowId, now), viewer);
        }
    }

    /**
     * Runs a click action against the network, per the terminal spec's "Click"
     * behavior.
     *
     * <p>
     * After the action (including a no-op such as pulling an unknown key) the
     * carried stack is written back to the player's cursor, the inventory is
     * marked dirty, the held item is refreshed, and the next
     * {@link #detectAndSendChanges()} is forced to resend the list.
     *
     * @param player the player performing the action
     * @param action what to do
     * @param key    the storage entry the click targeted
     * @param shift  whether shift was held
     */
    public void handleAction(EntityPlayerMP player, SlotAction action, String key, boolean shift) {
        TerminalBackend backend = terminal.backend();
        ItemStack carried = player.inventory.getItemStack();
        ItemStack newCarry = carried;

        if (action == SlotAction.PULL_OR_PUSH_STACK) {
            if (carried != null) {
                // Push the whole carried stack; whatever does not fit stays carried.
                newCarry = backend.push(carried);
            } else {
                newCarry = backend.pull(key, PULL_FULL_STACK);
            }
        } else if (action == SlotAction.PULL_ONE) {
            if (shift) {
                ItemStack one = backend.pull(key, 1);
                if (one != null) {
                    mergeItemStack(one, 0, 36, false);
                    ItemStack overflow = (one.stackSize > 0) ? one : null;
                    if (overflow != null) {
                        player.dropPlayerItemWithRandomChoice(overflow, true);
                    }
                }
            } else if (carried != null) {
                if (carried.getMaxStackSize() > carried.stackSize && (key == null || key.equals(carriedKey(carried)))) {
                    ItemStack one = backend.pull(key, 1);
                    if (one != null) {
                        newCarry = carried;
                        newCarry.stackSize += 1;
                    }
                }
                // else: carrying a full or non-matching stack, nothing carried is possible.
            } else {
                newCarry = backend.pull(key, 1);
            }
        } else if (action == SlotAction.GET_HALF || action == SlotAction.GET_QUARTER) {
            boolean half = action == SlotAction.GET_HALF;
            if (carried != null) {
                int take = half ? (carried.stackSize + 1) / 2 : (carried.stackSize + 3) / 4;
                if (take < 1) {
                    take = 1;
                }
                ItemStack split = carried.copy();
                split.stackSize = take;
                carried.stackSize -= take;
                newCarry = carried;
                ItemStack remainder = backend.push(split);
                if (remainder != null) {
                    newCarry = mergeCarried(newCarry, remainder);
                }
            } else {
                TerminalEntry entry = entryByKey(backend, key);
                int available = entry == null ? 0 : (int) Math.min(entry.count(), PULL_FULL_STACK);
                int take = half ? (available + 1) / 2 : (available + 3) / 4;
                if (take < 1) {
                    take = 1;
                }
                newCarry = backend.pull(key, take);
            }
        } else if (action == SlotAction.SHIFT_PULL) {
            ItemStack full = backend.pull(key, PULL_FULL_STACK);
            if (full != null) {
                mergeItemStack(full, 0, 36, false);
                ItemStack overflow = (full.stackSize > 0) ? full : null;
                if (overflow != null) {
                    player.dropPlayerItemWithRandomChoice(overflow, true);
                }
            }
        }
        // Any unrecognized action (a stale client ordinal) is a no-op.

        player.inventory.markDirty();
        player.inventory.setItemStack(newCarry);
        player.updateHeldItem();
        lastSent = null; // force the next detectAndSendChanges to resend the list
    }

    /**
     * Persists the terminal's display settings.
     *
     * @param sorting    the new sort word
     * @param modes      the new mode word
     * @param searchType the new search-type word
     */
    public void applySettings(int sorting, int modes, int searchType) {
        terminal.setSorting(sorting);
        terminal.setModes(modes);
        terminal.setSearchType(searchType);
    }

    /**
     * Shift-click from a player slot: push the whole stack into the network and
     * leave the remainder in the slot.
     *
     * @param p     the player
     * @param index the clicked slot index
     * @return a copy of the original stack when anything moved, otherwise null
     */
    @Override
    public ItemStack transferStackInSlot(EntityPlayer p, int index) {
        if (index < 0 || index > 35) {
            return null;
        }
        Slot slot = inventorySlots.get(index);
        if (slot == null || !slot.getHasStack()) {
            return null;
        }
        ItemStack original = slot.getStack();
        ItemStack moved = original.copy();
        ItemStack remainder = terminal.backend()
            .push(original);
        // A stack that was fully pushed must become null in the slot; a remainder
        // must stay as the remaining (non-zero) stack.
        if (remainder != null && remainder.stackSize > 0) {
            slot.putStack(remainder);
        } else {
            slot.putStack(null);
        }
        return moved;
    }

    /**
     * Whether two snapshots are element-for-element equal: same size, and each
     * entry's key and count equal, in order.
     */
    private static boolean sameList(List<TerminalEntry> a, List<TerminalEntry> b) {
        if (a.size() != b.size()) {
            return false;
        }
        for (int i = 0; i < a.size(); i++) {
            TerminalEntry left = a.get(i);
            TerminalEntry right = b.get(i);
            if (!left.key()
                .equals(right.key()) || left.count() != right.count()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Looks up a snapshot entry by key.
     *
     * @param backend the backend to snapshot
     * @param key     the entry key
     * @return the matching entry, or {@code null} when none exists
     */
    private static TerminalEntry entryByKey(TerminalBackend backend, String key) {
        List<TerminalEntry> snapshot = backend.snapshot();
        for (TerminalEntry entry : snapshot) {
            if (entry.key()
                .equals(key)) {
                return entry;
            }
        }
        return null;
    }

    /**
     * The opaque key of a concrete stack, computed the same way the backend
     * keys its stacks (item plus damage plus NBT).
     *
     * @param stack the stack to key
     * @return the stack's key
     */
    private static String carriedKey(ItemStack stack) {
        return StackKeys.keyOf(stack);
    }

    /**
     * Resolves the carried stack after a push that may have left a remainder:
     * a remainder of the same item joins the carry when there is room; a
     * remainder of a different item is carried on its own (the original carry
     * has already been fully pushed and is therefore empty); when there is no
     * remainder the (reduced) carry is kept.
     *
     * @param carry     the carried stack, already reduced by what was pushed
     * @param remainder what the push could not take in, or {@code null}
     * @return the stack to put on the cursor
     */
    private static ItemStack mergeCarried(ItemStack carry, ItemStack remainder) {
        if (remainder == null) {
            return carry;
        }
        if (carry != null && carry.getMaxStackSize() > carry.stackSize
            && carry.isItemEqual(remainder)
            && ItemStack.areItemStackTagsEqual(carry, remainder)) {
            carry.stackSize += remainder.stackSize;
            return carry;
        }
        return remainder;
    }
}
