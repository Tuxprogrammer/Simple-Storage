package com.tuxprogrammer.simplestorage.block;

import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import com.tuxprogrammer.simplestorage.blockentity.LevelEmitterTileEntity;
import com.tuxprogrammer.simplestorage.blockentity.NetworkQuery;

/**
 * The block for the level emitter, which emits a weak redstone signal on the
 * side facing the player when activated. The signal strength is reported by
 * its tile entity's redstoneSignal().
 *
 * Side: both (the client-only parts live in simplestorage.client).
 */
public class LevelEmitterBlock extends StorageBlockBase {

    /**
     * Creates the level emitter block.
     */
    public LevelEmitterBlock() {
        super(Material.iron, "level_emitter", 2.0F);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new LevelEmitterTileEntity();
    }

    @Override
    public boolean canProvidePower() {
        return true;
    }

    @Override
    public int isProvidingWeakPower(IBlockAccess world, int x, int y, int z, int side) {
        TileEntity te = world.getTileEntity(x, y, z);
        if (te instanceof LevelEmitterTileEntity) {
            return ((LevelEmitterTileEntity) te).redstoneSignal();
        }
        return 0;
    }

    @Override
    public void breakBlock(World w, int x, int y, int z, net.minecraft.block.Block block, int meta) {
        // The emitter drives network state, so invalidate it on break.
        if (!w.isRemote) {
            NetworkQuery.invalidate(w, x, y, z);
        }
        super.breakBlock(w, x, y, z, block, meta);
    }
}
