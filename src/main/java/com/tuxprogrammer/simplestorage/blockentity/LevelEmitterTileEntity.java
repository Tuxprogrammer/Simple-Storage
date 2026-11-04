package com.tuxprogrammer.simplestorage.blockentity;
// Ported from Tom's Simple Storage (MIT):
// https://github.com/tom5454/Toms-Storage/blob/95bb61d/NeoForge/src/platform-shared/java/com/tom/storagemod/block/entity/LevelEmitterBlockEntity.java
// Avoids: STOR-06, STOR-07

import java.util.Objects;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import com.tuxprogrammer.simplestorage.core.Module;
import com.tuxprogrammer.simplestorage.inventory.EmitterLogic;
import com.tuxprogrammer.simplestorage.inventory.StaggerGate;

/**
 * Tile entity for the level emitter: it compares the amount of one filtered
 * item in the connected storage network against a configured threshold and
 * emits a full-strength redstone signal ({@link EmitterLogic#SIGNAL_ON}) when
 * the comparison passes, or no signal ({@link EmitterLogic#SIGNAL_OFF})
 * otherwise.
 *
 * <p>
 * The emitter re-evaluates every {@link EmitterLogic#EVALUATE_INTERVAL_TICKS}
 * ticks, staggered by position so that not every emitter in a large network
 * wakes on the same tick (STOR-06). The evaluation itself is delegated to
 * {@link EmitterLogic}; the power state is kept here and announced to the
 * world by flipping the block metadata and notifying neighboring blocks, but
 * it is intentionally not persisted to NBT: on chunk load the emitter starts
 * unpowered and re-derives its state on its first evaluation.
 *
 * <p>
 * NBT: {@link NbtKeys#FILTER} (the one-slot filter stack, only when set),
 * {@link NbtKeys#COUNT} (the threshold), and {@link NbtKeys#LESS_THAN}
 * (the comparison mode). A missing {@link NbtKeys#COUNT} tag loads the
 * runtime default {@link EmitterLogic#DEFAULT_COUNT} rather than 0
 * (STOR-07).
 *
 * Side: server authoritative.
 */
public class LevelEmitterTileEntity extends GatedTileEntity {

    /** The item this emitter counts; null when unfiltered (any item). */
    private ItemStack filter;

    /** The threshold the network amount is compared against. */
    private int count = EmitterLogic.DEFAULT_COUNT;

    /** Whether the comparison is "less than" (strict) or "greater than" (inclusive). */
    private boolean lessThan;

    /** The current power state; not persisted to NBT. */
    private boolean powered;

    /**
     * {@inheritDoc}
     *
     * @return {@link Module#NETWORK}
     */
    @Override
    protected Module module() {
        return Module.NETWORK;
    }

    /**
     * @return the current filter stack (size 1), or null if unfiltered
     */
    public ItemStack getFilter() {
        return filter;
    }

    /**
     * Sets the filter to a copy of the given stack, sized to 1; passing null
     * clears the filter.
     *
     * @param stack the item to filter on, or null to clear
     */
    public void setFilter(ItemStack stack) {
        if (stack == null) {
            filter = null;
        } else {
            ItemStack copy = stack.copy();
            copy.stackSize = 1;
            filter = copy;
        }
        markDirty();
    }

    /**
     * @return the threshold the network amount is compared against
     */
    public int getCount() {
        return count;
    }

    /**
     * Sets the threshold, clamped to a minimum of 0.
     *
     * @param count the threshold to set
     */
    public void setCount(int count) {
        this.count = Math.max(0, count);
        markDirty();
    }

    /**
     * @return whether the emitter is in "less than" (strict) mode
     */
    public boolean isLessThan() {
        return lessThan;
    }

    /**
     * Sets the comparison mode.
     *
     * @param lessThan true for "less than" mode, false for "greater than" mode
     */
    public void setLessThan(boolean lessThan) {
        this.lessThan = lessThan;
        markDirty();
    }

    /**
     * @return whether the emitter is currently powered
     */
    public boolean isPowered() {
        return powered;
    }

    /**
     * @return the redstone signal the emitter currently outputs: full
     *         strength ({@link EmitterLogic#SIGNAL_ON}) when powered, none
     *         ({@link EmitterLogic#SIGNAL_OFF}) otherwise
     */
    public int redstoneSignal() {
        return powered ? EmitterLogic.SIGNAL_ON : EmitterLogic.SIGNAL_OFF;
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * Fires only when this emitter's position is due per
     * {@link StaggerGate#isDue(long, int, int)} at the
     * {@link EmitterLogic#EVALUATE_INTERVAL_TICKS} interval. When due, it
     * counts the filtered item in the network via
     * {@link NetworkQuery#amountOf}, re-evaluates the power state via
     * {@link EmitterLogic}, and announces any state change by flipping the
     * block metadata and notifying neighboring blocks.
     */
    @Override
    protected void serverTick() {
        if (!StaggerGate.isDue(
            worldObj.getTotalWorldTime(),
            Objects.hash(xCoord, yCoord, zCoord),
            EmitterLogic.EVALUATE_INTERVAL_TICKS)) {
            return;
        }
        long amount = NetworkQuery.amountOf(worldObj, xCoord, yCoord, zCoord, filter);
        boolean now = EmitterLogic.isPowered(amount, count, lessThan);
        if (EmitterLogic.shouldNotify(powered, now)) {
            powered = now;
            int meta = powered ? 1 : 0;
            worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, meta, 3);
            worldObj.notifyBlocksOfNeighborChange(xCoord, yCoord, zCoord, getBlockType());
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        if (filter != null) {
            NBTTagCompound filterTag = new NBTTagCompound();
            filter.writeToNBT(filterTag);
            tag.setTag(NbtKeys.FILTER, filterTag);
        }
        tag.setInteger(NbtKeys.COUNT, count);
        tag.setBoolean(NbtKeys.LESS_THAN, lessThan);
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        if (tag.hasKey(NbtKeys.FILTER)) {
            NBTTagCompound filterTag = tag.getCompoundTag(NbtKeys.FILTER);
            ItemStack loaded = ItemStack.loadItemStackFromNBT(filterTag);
            filter = loaded != null ? loaded : null;
        } else {
            filter = null;
        }
        // STOR-07: a missing tag must load the runtime default, not 0.
        count = EmitterLogic.resolveCount(tag.hasKey(NbtKeys.COUNT), tag.getInteger(NbtKeys.COUNT));
        lessThan = tag.getBoolean(NbtKeys.LESS_THAN);
        // 'powered' is intentionally not persisted: it is re-evaluated on tick.
    }
}
