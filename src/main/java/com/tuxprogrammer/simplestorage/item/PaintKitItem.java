package com.tuxprogrammer.simplestorage.item;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import com.tuxprogrammer.simplestorage.SimpleStorage;
import com.tuxprogrammer.simplestorage.block.IPaintable;

/**
 * The paint kit: an item used to pick a paint block from an opaque cube and
 * apply it to {@link IPaintable} blocks, or to scrape paint off them.
 *
 * <p>
 * The kit stores the picked paint block in item NBT under the keys
 * {@code paintBlock} (registry name) and {@code paintMeta} (int).
 * Side: server logic; textures are client resources.
 */
public class PaintKitItem extends Item {

    /**
     * NBT key for the registry name of the stored paint block.
     */
    public static final String NBT_PAINT_BLOCK = "paintBlock";

    /**
     * NBT key for the stored paint metadata.
     */
    public static final String NBT_PAINT_META = "paintMeta";

    /**
     * Creates the paint kit item.
     */
    public PaintKitItem() {
        setUnlocalizedName(SimpleStorage.MODID + ".paint_kit");
        setTextureName(SimpleStorage.MODID + ":paint_kit");
        setCreativeTab(SimpleStorageTab.INSTANCE);
        setMaxStackSize(1);
        setMaxDamage(64);
        setNoRepair();
    }

    /**
     * Stores the given block and metadata as the paint of the kit.
     *
     * @param kit   the paint kit stack to store in
     * @param block the block to store as paint
     * @param meta  the block metadata to store
     */
    public static void setPaint(ItemStack kit, Block block, int meta) {
        NBTTagCompound tag = kit.getTagCompound();
        if (tag == null) {
            tag = new NBTTagCompound();
            kit.setTagCompound(tag);
        }
        tag.setString(NBT_PAINT_BLOCK, Block.blockRegistry.getNameForObject(block));
        tag.setInteger(NBT_PAINT_META, meta);
    }

    /**
     * Returns the block stored as paint on the kit, or null when absent,
     * unknown, or air.
     *
     * @param kit the paint kit stack to read
     * @return the stored paint block, or null if none is stored
     */
    public static Block getPaintBlock(ItemStack kit) {
        NBTTagCompound tag = kit.getTagCompound();
        if (tag == null || !tag.hasKey(NBT_PAINT_BLOCK)) {
            return null;
        }
        Block block = Block.getBlockFromName(tag.getString(NBT_PAINT_BLOCK));
        if (block == null || block == Blocks.air) {
            return null;
        }
        return block;
    }

    /**
     * Returns the metadata stored as paint on the kit, clamped to 0..15.
     *
     * @param kit the paint kit stack to read
     * @return the stored paint metadata, or 0 when absent
     */
    public static int getPaintMeta(ItemStack kit) {
        NBTTagCompound tag = kit.getTagCompound();
        if (tag == null || !tag.hasKey(NBT_PAINT_META)) {
            return 0;
        }
        int meta = tag.getInteger(NBT_PAINT_META);
        if (meta < 0) {
            meta = 0;
        }
        if (meta > 15) {
            meta = 15;
        }
        return meta;
    }

    /**
     * Uses the paint kit on a block.
     *
     * <p>
     * Sneaking on a full opaque cube without a tile entity copies that
     * block and metadata into the kit. Otherwise, if a paint is stored and
     * the target block implements {@link IPaintable}, the paint is applied
     * and the kit takes one damage point. If no paint is stored and the
     * target is {@link IPaintable}, the target is scraped without damaging
     * the kit.
     *
     * @param stack  the kit stack in use
     * @param player the player using the kit
     * @param world  the world being acted on
     * @param x      the x coordinate of the targeted block
     * @param y      the y coordinate of the targeted block
     * @param z      the z coordinate of the targeted block
     * @param side   the side of the block that was clicked
     * @param hx     the x hit coordinate on the block
     * @param hy     the y hit coordinate on the block
     * @param hz     the z hit coordinate on the block
     * @return true if the use was handled (including client-side swings)
     */
    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hx,
        float hy, float hz) {
        if (world.isRemote) {
            // Client just swings the item; the server applies the effect.
            return true;
        }

        Block target = world.getBlock(x, y, z);
        int targetMeta = world.getBlockMetadata(x, y, z);
        boolean hasPaint = getPaintBlock(stack) != null;

        if (player.isSneaking() && !hasPaint
            && target != null
            && target.isOpaqueCube()
            && target.renderAsNormalBlock()
            && world.getTileEntity(x, y, z) == null) {
            // Pick up the clicked block as the new paint.
            setPaint(stack, target, targetMeta);
            return true;
        }

        if (hasPaint && target instanceof IPaintable) {
            if (((IPaintable) target).paint(world, x, y, z, getPaintBlock(stack), getPaintMeta(stack))) {
                stack.damageItem(1, player);
                return true;
            }
        } else if (!hasPaint && target instanceof IPaintable) {
            // No paint stored: scrape paint off the target.
            return ((IPaintable) target).scrape(world, x, y, z);
        }

        return false;
    }
}
