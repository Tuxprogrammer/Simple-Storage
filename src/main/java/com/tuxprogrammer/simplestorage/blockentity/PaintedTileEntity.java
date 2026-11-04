package com.tuxprogrammer.simplestorage.blockentity;

import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;

/**
 * Tile entity that stores the painted facade state for a paintable block.
 *
 * <p>
 * Side: both (state), the facade rendering lives in simplestorage.client.
 * </p>
 */
public class PaintedTileEntity extends TileEntity {

    /** The block whose texture the facade shows; null when unpainted. */
    private Block paintBlock;

    /** The metadata of the paint block to display, clamped to 0..15. */
    private int paintMeta;

    /**
     * @return the block the facade is painted with, or null if unpainted
     */
    public Block getPaintBlock() {
        return paintBlock;
    }

    /**
     * @return the metadata of the paint block, 0..15
     */
    public int getPaintMeta() {
        return paintMeta;
    }

    /**
     * @return whether this tile entity currently holds a paint facade
     */
    public boolean isPainted() {
        return paintBlock != null;
    }

    /**
     * Sets the paint facade. Passing a null block clears the paint. The meta
     * is clamped to 0..15.
     *
     * @param paint the block whose texture the facade shows, or null to clear
     * @param meta  the metadata to display with the paint
     */
    public void setPaint(Block paint, int meta) {
        paintBlock = paint;
        paintMeta = Math.max(0, Math.min(15, meta));
        markDirty();
        if (worldObj != null) {
            worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        if (isPainted()) {
            tag.setString(NbtKeys.PAINT_BLOCK, Block.blockRegistry.getNameForObject(paintBlock));
            tag.setInteger(NbtKeys.PAINT_META, paintMeta);
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        if (tag.hasKey(NbtKeys.PAINT_BLOCK)) {
            Block block = Block.getBlockFromName(tag.getString(NbtKeys.PAINT_BLOCK));
            if (block != null && block != net.minecraft.init.Blocks.air) {
                int meta = tag.hasKey(NbtKeys.PAINT_META) ? tag.getInteger(NbtKeys.PAINT_META) : 0;
                meta = Math.max(0, Math.min(15, meta));
                paintBlock = block;
                paintMeta = meta;
            } else {
                paintBlock = null;
                paintMeta = 0;
            }
        } else {
            paintBlock = null;
            paintMeta = 0;
        }
    }

    /**
     * Builds the packet used to sync this tile entity's state to a client.
     *
     * @return a tile entity update packet carrying the paint state
     */
    public S35PacketUpdateTileEntity getDescriptionPacket() {
        NBTTagCompound tag = new NBTTagCompound();
        writeToNBT(tag);
        return new S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, 0, tag);
    }

    /**
     * Applies the state carried by a tile entity update packet to this tile
     * entity and refreshes the block rendering if the world is set.
     *
     * @param net the network manager that delivered the packet
     * @param pkt the tile entity update packet
     */
    public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt) {
        readFromNBT(pkt.func_148857_g());
        if (worldObj != null) {
            worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
        }
    }
}
