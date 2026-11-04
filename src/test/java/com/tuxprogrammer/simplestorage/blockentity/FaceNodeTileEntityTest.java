package com.tuxprogrammer.simplestorage.blockentity;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minecraft.init.Blocks;
import net.minecraft.init.Bootstrap;
import net.minecraft.nbt.NBTTagCompound;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.tuxprogrammer.simplestorage.config.ModulesConfig;

/**
 * Tests for {@link FaceNodeTileEntity}: the module-flag and no-world gates,
 * paint-state NBT round-tripping, and tolerant loading of unknown keys.
 */
@Tag("ingame")
public class FaceNodeTileEntityTest {

    @BeforeAll
    public static void bootstrap() {
        Bootstrap.func_151354_b();
    }

    @Test
    public void noWorldExposesNothing() {
        FaceNodeTileEntity te = new FaceNodeTileEntity();
        assertTrue(
            te.exposedInventories()
                .isEmpty());
    }

    @Test
    public void paintStateRoundTripsThroughNbt() {
        FaceNodeTileEntity te = new FaceNodeTileEntity();
        te.setPaint(Blocks.stone, 2);

        NBTTagCompound tag = new NBTTagCompound();
        te.writeToNBT(tag);

        FaceNodeTileEntity loaded = new FaceNodeTileEntity();
        loaded.readFromNBT(tag);

        assertSame(Blocks.stone, loaded.getPaintBlock());
        assertEquals(2, loaded.getPaintMeta());
    }

    @Test
    public void flagOffExposesNothing() {
        ModulesConfig.network = false;
        try {
            FaceNodeTileEntity te = new FaceNodeTileEntity();
            assertTrue(
                te.exposedInventories()
                    .isEmpty());
        } finally {
            ModulesConfig.network = true;
        }
    }

    @Test
    public void unknownKeysAreIgnored() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setString(NbtKeys.PAINT_BLOCK, net.minecraft.block.Block.blockRegistry.getNameForObject(Blocks.stone));
        tag.setInteger(NbtKeys.PAINT_META, 2);
        tag.setString("futureKey", "whatever");
        tag.setInteger("anotherFuture", 99);

        FaceNodeTileEntity te = new FaceNodeTileEntity();
        assertDoesNotThrow(() -> te.readFromNBT(tag));
        assertSame(Blocks.stone, te.getPaintBlock());
        assertEquals(2, te.getPaintMeta());
    }
}
