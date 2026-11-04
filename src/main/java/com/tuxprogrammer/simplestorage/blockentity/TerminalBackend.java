package com.tuxprogrammer.simplestorage.blockentity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

import com.tuxprogrammer.simplestorage.inventory.MultiInventory;
import com.tuxprogrammer.simplestorage.inventory.StoredStack;

/**
 * Exposes a group of vanilla inventories as a single merged multi-inventory
 * for terminal read/write operations.
 * Side: server logic.
 */
public final class TerminalBackend {

    private final Map<String, ItemStack> prototypes = new HashMap<String, ItemStack>();
    private final MultiInventory multi;

    /**
     * Creates a backend over the given inventories.
     * 
     * @param inventories the inventories to merge, in priority order
     */
    public TerminalBackend(List<IInventory> inventories) {
        List<MultiInventory.Entry> entries = new ArrayList<MultiInventory.Entry>();
        for (IInventory inventory : inventories) {
            MultiInventory.Slots slots = new InventorySlots(inventory, prototypes);
            entries.add(new MultiInventory.Entry(slots, entries.size(), null, false));
        }
        this.multi = new MultiInventory(entries.toArray(new MultiInventory.Entry[0]));
        // Register a prototype for every slot currently held, so a pull can
        // succeed even when no scan has happened yet.
        multi.contents();
    }

    /**
     * @return one entry per distinct stack key held across all inventories
     */
    public List<TerminalEntry> snapshot() {
        List<TerminalEntry> result = new ArrayList<TerminalEntry>();
        for (StoredStack stored : multi.contents()) {
            ItemStack prototype = prototypes.get(stored.getKey());
            if (prototype == null) {
                continue;
            }
            result.add(new TerminalEntry(stored.getKey(), prototype, stored.getCount()));
        }
        return result;
    }

    /**
     * Extracts up to one full stack of the given key from the merged inventories.
     * 
     * @param key    the stack key to pull
     * @param amount the desired count, capped at the prototype's max stack size
     * @return a stack of the extracted size, or null when nothing could be extracted
     */
    public ItemStack pull(String key, int amount) {
        if (amount <= 0) {
            return null;
        }
        ItemStack prototype = prototypes.get(key);
        if (prototype == null) {
            // The inventories may have changed since construction; rescan so
            // every slot's prototype is registered before the lookup.
            multi.contents();
            prototype = prototypes.get(key);
            if (prototype == null) {
                return null;
            }
        }
        int extracted = multi.extract(key, Math.min(amount, prototype.getMaxStackSize()));
        if (extracted <= 0) {
            return null;
        }
        // A successful pull changes the merged inventories.
        multi.changeCounter();
        ItemStack result = prototype.copy();
        result.stackSize = extracted;
        return result;
    }

    /**
     * Inserts a stack into the merged inventories, registering its prototype first.
     * 
     * @param stack the stack to insert, may be null
     * @return the portion that did not fit (the input stack when it fit entirely, null when everything was inserted)
     */
    public ItemStack push(ItemStack stack) {
        if (stack == null || stack.stackSize <= 0) {
            return null;
        }
        String key = StackKeys.keyOf(stack);
        if (!prototypes.containsKey(key)) {
            ItemStack prototype = stack.copy();
            prototype.stackSize = 1;
            prototypes.put(key, prototype);
        }
        int remainder = multi.insert(key, stack.stackSize, stack.getMaxStackSize());
        if (remainder <= 0) {
            return null;
        }
        // A successful push changes the merged inventories.
        multi.changeCounter();
        if (remainder >= stack.stackSize) {
            return stack;
        }
        ItemStack result = stack.copy();
        result.stackSize = remainder;
        return result;
    }

    /**
     * @return the multi-inventory's change counter
     */
    public long changeCounter() {
        return multi.changeCounter();
    }

    /**
     * @return the number of inventories backing this backend
     */
    public int inventoryCount() {
        return (int) multi.changeCounter();
    }
}
