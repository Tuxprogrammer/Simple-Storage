package com.tuxprogrammer.simplestorage.blockentity;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minecraft.block.Block;
import net.minecraft.init.Bootstrap;
import net.minecraft.nbt.NBTTagCompound;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link PaintedTileEntity}: paint state storage, NBT
 * (de)serialization, and tolerant loading.
 */
@Tag("ingame")
public class PaintedTileEntityTest {

    @BeforeAll
    public static void bootstrap() {
        Bootstrap.func_151354_b();
    }

    @Test
    public void roundTripKeepsTheFacadeBlockAndMeta() {
        PaintedTileEntity te = new PaintedTileEntity();
        te.setPaint(net.minecraft.init.Blocks.stone, 3);

        NBTTagCompound tag = new NBTTagCompound();
        te.writeToNBT(tag);

        PaintedTileEntity loaded = new PaintedTileEntity();
        loaded.readFromNBT(tag);

        assertSame(net.minecraft.init.Blocks.stone, loaded.getPaintBlock());
        assertEquals(3, loaded.getPaintMeta());
        assertTrue(loaded.isPainted());
    }

    @Test
    public void unpaintedWritesNoPaintKeys() {
        PaintedTileEntity te = new PaintedTileEntity();

        NBTTagCompound tag = new NBTTagCompound();
        te.writeToNBT(tag);

        assertFalse(tag.hasKey(NbtKeys.PAINT_BLOCK));
        assertFalse(tag.hasKey(NbtKeys.PAINT_META));
    }

    @Test
    public void missingKeysLoadUnpainted() {
        PaintedTileEntity te = new PaintedTileEntity();
        te.readFromNBT(new NBTTagCompound());

        assertFalse(te.isPainted());
        assertNull(te.getPaintBlock());
    }

    @Test
    public void unknownBlockNameLoadsUnpainted() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setString(NbtKeys.PAINT_BLOCK, "nomod:missing");
        tag.setInteger(NbtKeys.PAINT_META, 7);

        PaintedTileEntity te = new PaintedTileEntity();
        te.readFromNBT(tag);

        assertFalse(te.isPainted());
        assertNull(te.getPaintBlock());
    }

    @Test
    public void metaIsClampedWhenLoading() {
        PaintedTileEntity te = new PaintedTileEntity();
        NBTTagCompound high = new NBTTagCompound();
        high.setString(NbtKeys.PAINT_BLOCK, Block.blockRegistry.getNameForObject(net.minecraft.init.Blocks.stone));
        high.setInteger(NbtKeys.PAINT_META, 99);
        te.readFromNBT(high);
        assertEquals(15, te.getPaintMeta());

        NBTTagCompound low = new NBTTagCompound();
        low.setString(NbtKeys.PAINT_BLOCK, Block.blockRegistry.getNameForObject(net.minecraft.init.Blocks.stone));
        low.setInteger(NbtKeys.PAINT_META, -4);
        te.readFromNBT(low);
        assertEquals(0, te.getPaintMeta());
    }

    @Test
    public void unknownExtraKeysAreIgnoredAndForwardCompatible() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setString(NbtKeys.PAINT_BLOCK, Block.blockRegistry.getNameForObject(net.minecraft.init.Blocks.stone));
        tag.setInteger(NbtKeys.PAINT_META, 2);
        tag.setString("futureKey", "whatever");

        PaintedTileEntity te = new PaintedTileEntity();
        te.readFromNBT(tag);
        assertSame(net.minecraft.init.Blocks.stone, te.getPaintBlock());
        assertEquals(2, te.getPaintMeta());

        NBTTagCompound rewritten = new NBTTagCompound();
        assertDoesNotThrow(() -> te.writeToNBT(rewritten));
        assertSame(net.minecraft.init.Blocks.stone, Block.getBlockFromName(rewritten.getString(NbtKeys.PAINT_BLOCK)));
        assertEquals(2, rewritten.getInteger(NbtKeys.PAINT_META));
    }

    @Test
    public void clearingPaintWorks() {
        PaintedTileEntity te = new PaintedTileEntity();
        te.setPaint(net.minecraft.init.Blocks.stone, 3);
        assertTrue(te.isPainted());

        te.setPaint(null, 0);

        assertFalse(te.isPainted());
        assertNull(te.getPaintBlock());
    }
}
