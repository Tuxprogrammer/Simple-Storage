package com.tuxprogrammer.simplestorage.blockentity;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import net.minecraft.nbt.NBTTagCompound;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Tests for the stateless tile entities: none of them persist any
 * custom NBT data, so {@code writeToNBT} should emit only the vanilla
 * keys ("id", "x", "y", "z"), and {@code readFromNBT} must tolerate
 * keys they do not recognize.
 */
@Tag("ingame")
public class NoStateTileEntitiesTest {

    private static final Set<String> VANILLA_KEYS = new HashSet<String>(Arrays.asList("id", "x", "y", "z"));

    /**
     * The only keys a stateless tile entity is allowed to write are the
     * vanilla position/type keys.
     */
    private static void assertOnlyVanillaKeys(NBTTagCompound compound) {
        for (String key : compound.func_150296_c()) {
            assertTrue(VANILLA_KEYS.contains(key), "unexpected NBT key: " + key);
        }
    }

    @Test
    public void connectorWritesOnlyTheVanillaKeysAndReadsBack() {
        NBTTagCompound compound = new NBTTagCompound();
        new InventoryConnectorTileEntity().writeToNBT(compound);
        assertOnlyVanillaKeys(compound);

        InventoryConnectorTileEntity target = new InventoryConnectorTileEntity();
        assertDoesNotThrow(() -> target.readFromNBT(compound));
        assertFalse(target.hasConnectedInventories());
        assertEquals(0, target.inventoryCount());
    }

    @Test
    public void interfaceWritesOnlyTheVanillaKeysAndReadsBack() {
        NBTTagCompound compound = new NBTTagCompound();
        new InventoryInterfaceTileEntity().writeToNBT(compound);
        assertOnlyVanillaKeys(compound);

        InventoryInterfaceTileEntity target = new InventoryInterfaceTileEntity();
        assertDoesNotThrow(() -> target.readFromNBT(compound));
        assertFalse(target.isLinked());
    }

    @Test
    public void openCrateWritesOnlyTheVanillaKeys() {
        NBTTagCompound compound = new NBTTagCompound();
        new OpenCrateTileEntity().writeToNBT(compound);
        assertOnlyVanillaKeys(compound);
    }

    @Test
    public void readersIgnoreUnknownKeys() {
        NBTTagCompound compound = new NBTTagCompound();
        compound.setString("futureKey", "some value");
        compound.setInteger("futureNumber", 42);

        assertDoesNotThrow(() -> new InventoryConnectorTileEntity().readFromNBT(compound));
        assertDoesNotThrow(() -> new InventoryInterfaceTileEntity().readFromNBT(compound));
        assertDoesNotThrow(() -> new OpenCrateTileEntity().readFromNBT(compound));
    }

    @Test
    public void emptyInterfaceExposesNothing() {
        assertTrue(
            new InventoryInterfaceTileEntity().exposedInventories()
                .isEmpty());
    }

    @Test
    public void emptyConnectorExposesNothing() {
        InventoryConnectorTileEntity connector = new InventoryConnectorTileEntity();
        assertTrue(
            connector.exposedInventories()
                .isEmpty());
        assertFalse(connector.hasConnectedInventories());
    }
}
