package com.tuxprogrammer.simplestorage.blockentity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.tuxprogrammer.simplestorage.inventory.StaggerGate;

/**
 * Tests for {@link InventoryInterfaceTileEntity}.
 */
@Tag("ingame")
public class InventoryInterfaceTileEntityTest {

    /**
     * STOR-03: an interface linked to an empty connector must expose nothing.
     */
    @Test
    public void doesNotReproduceSTOR03_interfaceIgnoresAConnectorWithNoInventories() {
        InventoryConnectorTileEntity connector = new InventoryConnectorTileEntity();
        InventoryInterfaceTileEntity interfaceEntity = new InventoryInterfaceTileEntity();
        interfaceEntity.setConnector(connector);

        assertFalse(interfaceEntity.isLinked(), "an interface to an empty connector must not be linked");
        assertEquals(
            0,
            interfaceEntity.exposedInventories()
                .size(),
            "no inventories may be exposed");
    }

    /**
     * STOR-05: the minimum stagger hash must still receive a rescan phase.
     */
    @Test
    public void doesNotReproduceSTOR05_minValueHashStillGetsARescanPhase() {
        int dueCount = 0;
        for (int time = 0; time < 200; time++) {
            if (StaggerGate.isDue(time, Integer.MIN_VALUE, StaggerGate.CONNECTOR_INTERVAL)) {
                dueCount++;
            }
        }
        assertEquals(10, dueCount, "the minimum hash must be due exactly 10 times in 200 ticks");
    }

    @Test
    public void unlinkedInterfaceExposesNothing() {
        InventoryInterfaceTileEntity interfaceEntity = new InventoryInterfaceTileEntity();

        assertFalse(interfaceEntity.isLinked(), "a fresh interface must not be linked");
        assertEquals(
            0,
            interfaceEntity.exposedInventories()
                .size(),
            "a fresh interface must expose nothing");
    }
}
