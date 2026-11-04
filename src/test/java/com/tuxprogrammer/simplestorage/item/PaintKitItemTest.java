package com.tuxprogrammer.simplestorage.item;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link PaintKitItem}: NBT round-tripping of the stored
 * paint block and metadata, tolerant reads, clamping, and item properties.
 */
@Tag("ingame")
public class PaintKitItemTest {

    /**
     * Boots the Minecraft bootstrap (block registry, stat collector) once
     * before any test so that block names can be resolved.
     */
    @BeforeAll
    public static void bootstrap() {
        net.minecraft.init.Bootstrap.func_151354_b();
    }

    /**
     * Stores a paint block and metadata on a kit and reads them back.
     */
    @Test
    public void storedPaintRoundTripsThroughItemNbt() {
        ItemStack kit = new ItemStack(new PaintKitItem());
        PaintKitItem.setPaint(kit, Blocks.stone, 3);

        assertSame(Blocks.stone, PaintKitItem.getPaintBlock(kit));
        assertEquals(3, PaintKitItem.getPaintMeta(kit));
    }

    /**
     * A freshly created kit with no NBT has no paint stored.
     */
    @Test
    public void emptyKitHasNoPaint() {
        ItemStack kit = new ItemStack(new PaintKitItem());

        assertNull(PaintKitItem.getPaintBlock(kit));
        assertEquals(0, PaintKitItem.getPaintMeta(kit));
    }

    /**
     * A registry name that resolves to no block is treated as no paint.
     */
    @Test
    public void unknownBlockNameIsTreatedAsNoPaint() {
        ItemStack kit = new ItemStack(new PaintKitItem());
        NBTTagCompound tag = new NBTTagCompound();
        tag.setString(PaintKitItem.NBT_PAINT_BLOCK, "nomod:missing");
        kit.setTagCompound(tag);

        assertNull(PaintKitItem.getPaintBlock(kit));
    }

    /**
     * Metadata is clamped to the range 0..15 when read back.
     */
    @Test
    public void metaIsClampedWhenRead() {
        ItemStack kit = new ItemStack(new PaintKitItem());
        PaintKitItem.setPaint(kit, Blocks.stone, 99);
        assertEquals(15, PaintKitItem.getPaintMeta(kit));

        ItemStack kit2 = new ItemStack(new PaintKitItem());
        NBTTagCompound tag2 = new NBTTagCompound();
        tag2.setString(PaintKitItem.NBT_PAINT_BLOCK, "minecraft:stone");
        tag2.setInteger(PaintKitItem.NBT_PAINT_META, -3);
        kit2.setTagCompound(tag2);
        assertEquals(0, PaintKitItem.getPaintMeta(kit2));
    }

    /**
     * Air stored as a paint block is treated as no paint at all.
     */
    @Test
    public void airIsNotAPaint() {
        ItemStack kit = new ItemStack(new PaintKitItem());
        PaintKitItem.setPaint(kit, Blocks.air, 0);

        assertNull(PaintKitItem.getPaintBlock(kit));
    }

    /**
     * The kit has 64 uses and is limited to one per inventory slot.
     */
    @Test
    public void kitHasSixtyFourUses() {
        PaintKitItem kit = new PaintKitItem();

        assertEquals(64, kit.getMaxDamage());
        assertEquals(1, kit.getItemStackLimit());
    }
}
