package com.tuxprogrammer.simplestorage.block;

import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

import com.tuxprogrammer.simplestorage.blockentity.FilingCabinetTileEntity;

/**
 * The block for the filing cabinet, a multi-slot storage container that
 * preserves its contents when broken. The facing metadata (0..5) is chosen
 * from the placer's look direction so the cabinet's front faces away from
 * whoever placed it.
 *
 * Side: both (the client-only parts live in simplestorage.client).
 */
public class FilingCabinetBlock extends StorageBlockBase {

    /**
     * Creates the filing cabinet block.
     */
    public FilingCabinetBlock() {
        super(Material.wood, "filing_cabinet", 2.5F);
    }

    /**
     * Derives the facing metadata (0..5) from the placer's look direction.
     *
     * @param placer the entity placing the block
     * @return 1 (up) when looking steeply down, 0 (down) when looking steeply
     *         up, otherwise 2/5/3/4 for north/east/south/west
     */
    protected static int facingFrom(EntityLivingBase placer) {
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
        return new FilingCabinetTileEntity();
    }

    @Override
    public void onBlockPlacedBy(World w, int x, int y, int z, EntityLivingBase placer, ItemStack stack) {
        w.setBlockMetadataWithNotify(x, y, z, facingFrom(placer), 3);
    }

    @Override
    public void breakBlock(World w, int x, int y, int z, net.minecraft.block.Block block, int meta) {
        TileEntity te = w.getTileEntity(x, y, z);
        if (te instanceof FilingCabinetTileEntity) {
            FilingCabinetTileEntity cabinet = (FilingCabinetTileEntity) te;
            // Spawn the cabinet's contents as items on the server only.
            if (!w.isRemote) {
                for (int slot = 0; slot < cabinet.getSizeInventory(); slot++) {
                    ItemStack stack = cabinet.getStackInSlot(slot);
                    if (stack == null) {
                        continue;
                    }
                    EntityItem item = new EntityItem(
                        w,
                        x + 0.5D + (w.rand.nextDouble() - 0.5D) * 0.8D,
                        y + 0.5D + (w.rand.nextDouble() - 0.5D) * 0.8D,
                        z + 0.5D + (w.rand.nextDouble() - 0.5D) * 0.8D,
                        stack);
                    item.motionX = w.rand.nextFloat() * 0.2F - 0.1F;
                    item.motionY = 0.2F + w.rand.nextFloat() * 0.2F;
                    item.motionZ = w.rand.nextFloat() * 0.2F - 0.1F;
                    w.spawnEntityInWorld(item);
                }
            }
        }
        super.breakBlock(w, x, y, z, block, meta);
    }

    @Override
    public boolean onBlockActivated(World w, int x, int y, int z, net.minecraft.entity.player.EntityPlayer p, int side,
        float hx, float hy, float hz) {
        // Returns false for now: the cabinet GUI belongs to milestone M03.
        return false;
    }
}
