package com.tuxprogrammer.simplestorage.block;

import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

import com.tuxprogrammer.simplestorage.blockentity.InventoryHopperTileEntity;

/**
 * The block for the inventory hopper, which moves items between the
 * inventory in front of it and adjacent inventories. The facing metadata
 * (0..5) is the OUTPUT side of the hopper, so it is set to the opposite of
 * the placer's look direction (the hopper faces away from whoever placed it).
 *
 * Side: both (the client-only parts live in simplestorage.client).
 */
public class InventoryHopperBlock extends StorageBlockBase {

    /**
     * Creates the inventory hopper block.
     */
    public InventoryHopperBlock() {
        super(Material.iron, "inventory_hopper", 3.0F);
    }

    /**
     * Derives the output-side facing metadata (0..5) from the placer's look
     * direction. The meta is the OUTPUT side, so it is the opposite of the
     * direction the placer is looking.
     *
     * @param placer the entity placing the block
     * @return 0 (down) when looking steeply down, 1 (up) when looking steeply
     *         up, otherwise the opposite horizontal face 3/4/2/5 for
     *         north/east/south/west
     */
    private static int facingFrom(EntityLivingBase placer) {
        if (placer.rotationPitch > 60.0F) {
            return 0; // output down (looking up)
        }
        if (placer.rotationPitch < -60.0F) {
            return 1; // output up (looking down)
        }
        switch (MathHelper.floor_double(placer.rotationYaw * 4.0F / 360.0F + 0.5D) & 3) {
            case 0:
                return 3; // looking north -> output south
            case 1:
                return 4; // looking east -> output west
            case 2:
                return 2; // looking south -> output north
            default:
                return 5; // looking west -> output east
        }
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new InventoryHopperTileEntity();
    }

    @Override
    public void onBlockPlacedBy(World w, int x, int y, int z, EntityLivingBase placer, ItemStack stack) {
        w.setBlockMetadataWithNotify(x, y, z, facingFrom(placer), 3);
    }
}
