package com.tuxprogrammer.simplestorage.block;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import com.tuxprogrammer.simplestorage.SimpleStorage;
import com.tuxprogrammer.simplestorage.blockentity.CraftingTerminalTileEntity;
import com.tuxprogrammer.simplestorage.blockentity.NetworkQuery;
import com.tuxprogrammer.simplestorage.core.Module;
import com.tuxprogrammer.simplestorage.core.ModuleState;
import com.tuxprogrammer.simplestorage.gui.GuiIds;

/**
 * The crafting terminal block: a storage terminal with a 3x3 crafting grid.
 * Right-click to browse and move items across every inventory the terminal
 * can reach through cables, or craft using the grid.
 *
 * <p>
 * Placement orients the block's front face toward the placing player
 * (the classic four-way facing rule). Placing, replacing, and breaking the
 * block each invalidate the network query cache, because the set of
 * reachable inventories has changed. Breaking the block drops the stacks in
 * its crafting grid.
 *
 * Side: both (the client-only parts live in simplestorage.client).
 */
public class CraftingTerminalBlock extends StorageBlockBase implements IInventoryCable {

    /**
     * @param material the block's material (iron).
     */
    public CraftingTerminalBlock() {
        super(Material.iron, "crafting_terminal", 2.0F);
    }

    /**
     * Terminals accept a cable from any side.
     *
     * @param world the world.
     * @param x     the x position.
     * @param y     the y position.
     * @param z     the z position.
     * @param side  the side the cable would attach on.
     * @return true.
     */
    @Override
    public boolean canConnectFrom(IBlockAccess world, int x, int y, int z, ForgeDirection side) {
        return true;
    }

    /**
     * @return a fresh crafting terminal tile entity.
     */
    @Override
    public TileEntity createNewTileEntity(World world, int metadata) {
        return new CraftingTerminalTileEntity();
    }

    /**
     * Opens the crafting terminal's GUI on the server when the terminal
     * module is configured and active.
     *
     * @param w    the world.
     * @param x    the x position.
     * @param y    the y position.
     * @param z    the z position.
     * @param p    the player right-clicking.
     * @param side the side clicked.
     * @param hx   the x offset within the block, 0..1.
     * @param hy   the y offset within the block, 0..1.
     * @param hz   the z offset within the block, 0..1.
     * @return true (the click is consumed).
     */
    @Override
    public boolean onBlockActivated(World w, int x, int y, int z, EntityPlayer p, int side, float hx, float hy,
        float hz) {
        if (!w.isRemote && ModuleState.configured()
            .isActive(Module.TERMINAL)) {
            SimpleStorage.openGui(p, GuiIds.CRAFTING_TERMINAL, w, x, y, z);
        }
        return true;
    }

    /**
     * Orients the block's facing metadata toward the player who placed it.
     *
     * @param w      the world.
     * @param x      the x position.
     * @param y      the y position.
     * @param z      the z position.
     * @param placer the placing player.
     */
    @Override
    public void onBlockPlacedBy(World w, int x, int y, int z, EntityLivingBase placer, ItemStack stack) {
        w.setBlockMetadataWithNotify(x, y, z, facingFrom(placer), 3);
    }

    /**
     * Derives the facing metadata from a placer's orientation.
     *
     * @param placer the player placing the block.
     * @return the block metadata to store: 1 facing down (looking up), 0
     *         facing up (looking down), 2/5/3/4 the four horizontal
     *         directions from the yaw.
     */
    private int facingFrom(EntityLivingBase placer) {
        if (placer.rotationPitch > 60) {
            return 1;
        }
        if (placer.rotationPitch < -60) {
            return 0;
        }
        switch (MathHelper.floor_double(placer.rotationYaw * 4.0F / 360.0F + 0.5D) & 3) {
            case 0:
                return 2;
            case 1:
                return 5;
            case 2:
                return 3;
            default:
                return 4;
        }
    }

    /**
     * A cable's reach depends on what is next to it, so placing the
     * terminal invalidates the query cache.
     *
     * @param world the world.
     * @param x     the x position.
     * @param y     the y position.
     * @param z     the z position.
     */
    @Override
    public void onBlockAdded(World world, int x, int y, int z) {
        if (!world.isRemote) {
            NetworkQuery.invalidate(world, x, y, z);
        }
    }

    /**
     * A neighbor change can alter what a cable reaches, so the query cache
     * is invalidated.
     *
     * @param world the world.
     * @param x     the x position.
     * @param y     the y position.
     * @param z     the z position.
     * @param block the block that changed.
     */
    @Override
    public void onNeighborBlockChange(World world, int x, int y, int z, Block block) {
        if (!world.isRemote) {
            NetworkQuery.invalidate(world, x, y, z);
        }
    }

    /**
     * Breaking the crafting terminal drops its non-null crafting-grid
     * stacks as item entities (server only), then removes it from the cable
     * network: the query cache is invalidated and the block is cleared.
     *
     * @param world the world.
     * @param x     the x position.
     * @param y     the y position.
     * @param z     the z position.
     * @param block the block being broken.
     * @param meta  the block's metadata.
     */
    @Override
    public void breakBlock(World world, int x, int y, int z, Block block, int meta) {
        if (!world.isRemote) {
            TileEntity tile = world.getTileEntity(x, y, z);
            if (tile instanceof CraftingTerminalTileEntity) {
                CraftingTerminalTileEntity crafting = (CraftingTerminalTileEntity) tile;
                for (int i = 0; i < 9; i++) {
                    ItemStack stack = crafting.getGridStack(i);
                    if (stack == null) {
                        continue;
                    }
                    world.spawnEntityInWorld(
                        new EntityItem(
                            world,
                            x + 0.5F + world.rand.nextFloat() * 0.2F - 0.1F,
                            y + 0.5F + world.rand.nextFloat() * 0.2F - 0.1F,
                            z + 0.5F + world.rand.nextFloat() * 0.2F - 0.1F,
                            stack));
                }
            }
            NetworkQuery.invalidate(world, x, y, z);
        }
        super.breakBlock(world, x, y, z, block, meta);
    }
}
