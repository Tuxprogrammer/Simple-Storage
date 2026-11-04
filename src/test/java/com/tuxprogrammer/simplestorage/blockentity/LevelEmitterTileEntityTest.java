package com.tuxprogrammer.simplestorage.blockentity;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Objects;
import java.util.Random;

import net.minecraft.init.Bootstrap;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.tuxprogrammer.simplestorage.inventory.EmitterLogic;
import com.tuxprogrammer.simplestorage.inventory.StaggerGate;

/**
 * Tests for {@link LevelEmitterTileEntity}: NBT (de)serialization, the
 * STOR-06 / STOR-07 regressions, and the server-tick entry point.
 */
@Tag("ingame")
public class LevelEmitterTileEntityTest {

    @BeforeAll
    public static void bootstrap() {
        Bootstrap.func_151354_b();
    }

    @Test
    public void doesNotReproduceSTOR07_missingCountTagLoadsTheRuntimeDefault() {
        LevelEmitterTileEntity te = new LevelEmitterTileEntity();
        te.readFromNBT(new NBTTagCompound());

        assertEquals(1, te.getCount());
    }

    @Test
    public void doesNotReproduceSTOR06_everyPositionHashIsEvaluatedEveryTenTicks() {
        // The pathological hash: upstream's Math.abs(posHash) % N stayed
        // negative for Integer.MIN_VALUE, so the gate never fired (STOR-06).
        // It must fire exactly once per interval.
        int due = 0;
        for (long t = 0; t < 100; t++) {
            if (StaggerGate.isDue(t, Integer.MIN_VALUE, EmitterLogic.EVALUATE_INTERVAL_TICKS)) {
                due++;
            }
        }
        assertEquals(10, due);

        // The tile entity path uses java.util.Objects.hash(x, y, z) as the
        // position hash; every such hash must also fire exactly once per
        // interval.
        Random random = new Random(7);
        for (int i = 0; i < 1000; i++) {
            int hash = Objects.hash(random.nextInt(), random.nextInt(), random.nextInt());
            for (long t = 0; t < 100; t += 10) {
                int window = 0;
                for (long dt = 0; dt < 10; dt++) {
                    if (StaggerGate.isDue(t + dt, hash, EmitterLogic.EVALUATE_INTERVAL_TICKS)) {
                        window++;
                    }
                }
                assertEquals(1, window, "hash " + hash + " window starting at " + t);
            }
        }
    }

    @Test
    public void roundTripKeepsFilterCountAndMode() {
        LevelEmitterTileEntity te = new LevelEmitterTileEntity();
        te.setFilter(new ItemStack(Items.iron_ingot, 1));
        te.setCount(64);
        te.setLessThan(true);

        NBTTagCompound tag = new NBTTagCompound();
        te.writeToNBT(tag);

        LevelEmitterTileEntity loaded = new LevelEmitterTileEntity();
        loaded.readFromNBT(tag);

        ItemStack filter = loaded.getFilter();
        assertSame(Items.iron_ingot, filter.getItem());
        assertEquals(1, filter.stackSize);
        assertEquals(64, loaded.getCount());
        assertTrue(loaded.isLessThan());
    }

    @Test
    public void roundTripWithoutFilter() {
        LevelEmitterTileEntity te = new LevelEmitterTileEntity();

        NBTTagCompound tag = new NBTTagCompound();
        te.writeToNBT(tag);

        assertFalse(tag.hasKey(NbtKeys.FILTER));

        LevelEmitterTileEntity loaded = new LevelEmitterTileEntity();
        loaded.readFromNBT(tag);

        assertNull(loaded.getFilter());
        assertEquals(EmitterLogic.DEFAULT_COUNT, loaded.getCount());
        assertFalse(loaded.isLessThan());
    }

    @Test
    public void countIsClampedToZero() {
        LevelEmitterTileEntity te = new LevelEmitterTileEntity();
        te.setCount(-5);
        assertEquals(0, te.getCount());
    }

    @Test
    public void poweredIsNotPersisted() {
        LevelEmitterTileEntity te = new LevelEmitterTileEntity();
        te.setFilter(new ItemStack(Items.iron_ingot, 1));
        te.setCount(1);
        assertFalse(te.isPowered());

        NBTTagCompound tag = new NBTTagCompound();
        te.writeToNBT(tag);

        LevelEmitterTileEntity loaded = new LevelEmitterTileEntity();
        loaded.readFromNBT(tag);

        // The power state is re-evaluated on tick; a freshly loaded emitter
        // must not start out powered just because the tag was written while
        // powered.
        assertFalse(loaded.isPowered());
    }

    @Test
    public void unknownKeysAreIgnored() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setString("futureKey", "whatever");
        tag.setInteger("anotherFutureKey", 42);

        LevelEmitterTileEntity te = new LevelEmitterTileEntity();
        assertDoesNotThrow(() -> te.readFromNBT(tag));

        assertNull(te.getFilter());
        assertEquals(EmitterLogic.DEFAULT_COUNT, te.getCount());
        assertFalse(te.isLessThan());
    }

    @Test
    public void serverTickDoesNothingWithoutAWorld() {
        LevelEmitterTileEntity te = new LevelEmitterTileEntity();

        // GatedTileEntity.updateEntity() returns before reaching serverTick()
        // when worldObj is null, so a bare tick must not throw.
        assertDoesNotThrow(te::updateEntity);
    }
}
