package com.tuxprogrammer.simplestorage.block;

import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

import com.tuxprogrammer.simplestorage.blockentity.OpenCrateTileEntity;

/**
 * The block for the open crate, a storage container whose contents are
 * visible from above. The facing metadata (0..5) is chosen from the placer's
 * look direction so the crate's open side faces away from whoever placed it.
 *
 * Side: both (the client-only parts live in simplestorage.client).
 */
public class OpenCrateBlock extends StorageBlockBase {

    /**
     * Creates the open crate block.
     */
    public OpenCrateBlock() {
        super(Material.wood, "open_crate", 2.0F);
    }

    /**
     * Derives the facing metadata (0..5) from the placer's look direction.
     *
     * @param placer the entity placing the block
     * @return 1 (up) when looking steeply down, 0 (down) when looking steeply
     *         up, otherwise 2/5/3/4 for north/east/south/west
     */
    private static int facingFrom(EntityLivingBase placer) {
        if (placer.rotationPitch > 60.0F) {
            return 1; // up
        }
        if (placer.rotationPitch < -60.0F) {
            return 0; // down
        }
        switch (MathHelper.floor_double(placer.rotationYaw * 4.0F / 360.0F + 0.5D) & 3) {
            case 0:
                return 2; // north
            case 1:
                return 5; // east
            case 2:
                return 3; // south
            default:
                return 4; // west
        }
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new OpenCrateTileEntity();
    }

    @Override
    public void onBlockPlacedBy(World w, int x, int y, int z, EntityLivingBase placer, ItemStack stack) {
        w.setBlockMetadataWithNotify(x, y, z, facingFrom(placer), 3);
    }
}
