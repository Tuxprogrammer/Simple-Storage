package com.tuxprogrammer.simplestorage.blockentity;

import com.tuxprogrammer.simplestorage.core.Module;
import com.tuxprogrammer.simplestorage.core.ModuleState;

/**
 * Base class for tile entities whose ticking is gated by an optional module.
 *
 * <p>
 * Subclasses declare which {@link Module} they depend on via
 * {@link #module()}; each tick the base class asks
 * {@link ModuleState#configured()} whether that module is active, and only
 * calls {@link #serverTick()} when it is. This lets the user disable whole
 * features (e.g. wireless, NEI, WAILA) at runtime without unloading classes.
 *
 * <p>
 * A module flag gates behavior only, never registration: the tile entity
 * class is registered on both sides regardless of module state, so
 * NBT deserialization and chunk loading keep working even when the module
 * is disabled. Side: server behavior; the class itself loads on both sides.
 */
public abstract class GatedTileEntity extends net.minecraft.tileentity.TileEntity {

    /**
     * Returns the module this tile entity depends on.
     *
     * <p>
     * The module's enabled/disabled state is checked every tick via
     * {@link ModuleState#isActive(Module)}.
     *
     * @return the gating module for this tile entity
     */
    protected abstract Module module();

    /**
     * Asks the configured module state whether this tile entity's module is
     * currently active.
     *
     * @return {@code true} if the module for this tile entity is enabled
     */
    public final boolean isBehaviorActive() {
        return ModuleState.configured()
            .isActive(module());
    }

    /**
     * Called once per tick by Minecraft on the server side.
     *
     * <p>
     * Exits immediately when there is no world, when running on the
     * client, or when the module is inactive; otherwise delegates to
     * {@link #serverTick()}.
     */
    @Override
    public final void updateEntity() {
        if (worldObj == null || worldObj.isRemote || !isBehaviorActive()) {
            return;
        }
        serverTick();
    }

    /**
     * Performs this tile entity's per-tick server-side work.
     *
     * <p>
     * Only called from {@link #updateEntity()} when the world is present,
     * the side is the server, and the module is active, so implementations
     * can assume all three.
     */
    protected abstract void serverTick();
}
