package com.tuxprogrammer.simplestorage.blockentity;

import net.minecraft.block.BlockChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

/**
 * Resolves a world position to the {@link IInventory} living at that block,
 * and performs the single-item insert/extract operations the network needs.
 *
 * <p>
 * This is the inventory access chain of decision D10: first an
 * {@link net.minecraft.inventory.ISidedInventory}, then a plain
 * {@link IInventory} (an ISidedInventory is an IInventory, so the vanilla
 * order ISidedInventory then IInventory is covered), then an AE2 optional
 * (currently off), then none (null). The vanilla {@link BlockChest}
 * double-chest case is resolved through {@link BlockChest#func_149951_m},
 * which returns the single or double chest inventory.
 *
 * <p>
 * The insert/extract helpers use only the plain {@link IInventory} methods
 * (not sided ones).
 *
 * <p>
 * Side: server.
 */
public final class InventoryAccess {

    private InventoryAccess() {}

    /**
     * The inventory at a world position, or null.
     *
     * <p>
     * A vanilla chest tile entity ({@link net.minecraft.tileentity.TileEntityChest})
     * is resolved through {@link BlockChest#func_149951_m}, which returns the
     * single or double chest {@link IInventory}. Any other tile entity that is
     * itself an {@link IInventory} (including {@link net.minecraft.inventory.ISidedInventory})
     * is returned as-is; anything else is null.
     *
     * @param world the world
     * @param x     the block x position
     * @param y     the block y position
     * @param z     the block z position
     * @return the inventory at the position, or null if none (or the chunk is
     *         not loaded)
     */
    public static IInventory at(World world, int x, int y, int z) {
        if (!world.blockExists(x, y, z)) {
            return null;
        }
        TileEntity tile = world.getTileEntity(x, y, z);
        if (tile == null) {
            return null;
        }
        if (tile instanceof net.minecraft.tileentity.TileEntityChest) {
            return ((BlockChest) world.getBlock(x, y, z)).func_149951_m(world, x, y, z);
        }
        if (tile instanceof IInventory) {
            return (IInventory) tile;
        }
        return null;
    }

    /**
     * Removes exactly one item from {@code source} matching
     * {@code filterOrNull}, and returns it.
     *
     * <p>
     * Scans slots 0..{@link IInventory#getSizeInventory()}-1. The first
     * non-null stack that matches the filter is taken from: a null filter
     * matches any stack; otherwise the stack must be the same item, the same
     * damage (when the item has subtypes), and pass
     * {@link ItemStack#areItemStackTagsEqual}. One item is removed with
     * {@link IInventory#decrStackSize(int, int)}.
     *
     * @param source       the inventory to extract from
     * @param filterOrNull the stack to match, or null to match anything
     * @return the single extracted item, or null if nothing matched
     */
    public static ItemStack extractOne(IInventory source, ItemStack filterOrNull) {
        for (int slot = 0; slot < source.getSizeInventory(); slot++) {
            ItemStack stack = source.getStackInSlot(slot);
            if (stack == null) {
                continue;
            }
            if (filterOrNull == null || matches(stack, filterOrNull)) {
                return source.decrStackSize(slot, 1);
            }
        }
        return null;
    }

    /**
     * Inserts one item into {@code target}.
     *
     * <p>
     * First tries to merge the item into an existing stack of the same
     * item/damage/tags that is below both its max stack size and the
     * inventory's {@link IInventory#getInventoryStackLimit()}; failing that,
     * into the first empty slot that {@link IInventory#isItemValidForSlot}
     * allows. {@link IInventory#markDirty()} is called on the target.
     *
     * @param target the inventory to insert into
     * @param single the single item to insert
     * @return true if the item was inserted, false otherwise
     */
    public static boolean insertOne(IInventory target, ItemStack single) {
        if (single == null) {
            return false;
        }
        int limit = Math.min(single.getMaxStackSize(), target.getInventoryStackLimit());
        for (int slot = 0; slot < target.getSizeInventory(); slot++) {
            ItemStack stack = target.getStackInSlot(slot);
            if (stack == null) {
                continue;
            }
            if (stack.stackSize < limit && matches(stack, single)) {
                int toInsert = Math.min(single.stackSize, limit - stack.stackSize);
                stack.stackSize += toInsert;
                single.stackSize -= toInsert;
                target.markDirty();
                return single.stackSize <= 0;
            }
        }
        for (int slot = 0; slot < target.getSizeInventory(); slot++) {
            if (target.getStackInSlot(slot) != null || !target.isItemValidForSlot(slot, single)) {
                continue;
            }
            target.setInventorySlotContents(slot, single.copy());
            target.markDirty();
            return true;
        }
        return false;
    }

    /**
     * Whether {@code stack} matches {@code other}: same item, same damage
     * (when the item has subtypes), and equal tags.
     */
    private static boolean matches(ItemStack stack, ItemStack other) {
        if (stack.getItem() != other.getItem()) {
            return false;
        }
        if (stack.getItem()
            .getHasSubtypes() && stack.getItemDamage() != other.getItemDamage()) {
            return false;
        }
        return ItemStack.areItemStackTagsEqual(stack, other);
    }
}
