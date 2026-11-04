// Tests for the network connector and interface tile entities.
package com.tuxprogrammer.simplestorage.blockentity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.tuxprogrammer.simplestorage.core.Module;
import com.tuxprogrammer.simplestorage.inventory.StaggerGate;

/**
 * Tests for {@link InventoryConnectorTileEntity} and
 * {@link InventoryInterfaceTileEntity}.
 *
 * <p>
 * Neither class carries NBT state, so the tests exercise the public
 * behaviour directly: the gating module, the STOR-03 rule that a connector
 * with zero attached inventories is not a usable network, the interface's
 * link bookkeeping, and that {@code serverTick()} on a tile entity without
 * a world is a quiet no-op. The world-backed rescan itself is covered by
 * {@link ConnectorScanTest}; the link between the two tiles is exercised
 * through a small rescan-free test double.
 */
@Tag("ingame")
public class InventoryConnectorTileEntityTest {

    @Test
    public void bothTilesAreGatedByTheNetworkModule() {
        assertEquals(Module.NETWORK, new ConnectorProbe().gatedModule(), "the connector must gate on Module.NETWORK");
        assertEquals(Module.NETWORK, new InterfaceProbe().gatedModule(), "the interface must gate on Module.NETWORK");
    }

    @Test
    public void connectorWithNoInventoriesIsNotAUsableNetwork() {
        InventoryConnectorTileEntity connector = new InventoryConnectorTileEntity();

        assertFalse(connector.hasConnectedInventories(), "STOR-03: zero attached inventories is not a usable network");
        assertEquals(0, connector.inventoryCount());
        assertEquals(
            0,
            connector.exposedInventories()
                .size(),
            "the exposed list must match the attached count");
    }

    @Test
    public void interfaceWithoutAConnectorIsUnlinkedAndExposesNothing() {
        InventoryInterfaceTileEntity interface_ = new InventoryInterfaceTileEntity();

        assertFalse(interface_.isLinked());
        assertEquals(
            0,
            interface_.exposedInventories()
                .size(),
            "an unlinked interface must expose an empty list");
    }

    @Test
    public void interfaceLinksToItsConnectorAndExposesTheSameInventories() {
        LinkedConnector connector = new LinkedConnector(new Box());
        InventoryInterfaceTileEntity interface_ = new InventoryInterfaceTileEntity();
        interface_.setConnector(connector);

        assertTrue(interface_.isLinked(), "an interface linked to a connector with one attached inventory is linked");
        assertTrue(
            connector.hasConnectedInventories(),
            "a connector with one attached inventory is a usable network (STOR-03)");
        assertEquals(1, connector.inventoryCount());

        List<IInventory> exposed = interface_.exposedInventories();
        assertEquals(1, exposed.size());
        assertSame(
            connector.exposedInventories()
                .get(0),
            exposed.get(0),
            "a linked interface must expose the connector's inventories");
        assertEquals(
            connector.exposedInventories(),
            exposed,
            "the exposed list must equal the connector's exposed list");
    }

    @Test
    public void exposedListIsUnmodifiable() {
        LinkedConnector connector = new LinkedConnector(new Box());
        List<IInventory> exposed = connector.exposedInventories();
        assertEquals(1, exposed.size());
        try {
            exposed.add(new Box());
            assertTrue(false, "the exposed list must be unmodifiable");
        } catch (UnsupportedOperationException expected) {
            // expected
        }
    }

    @Test
    public void serverTickIsAQuietNoOpWithoutAWorld() {
        new ConnectorProbe().runServerTick();
        new InterfaceProbe().runServerTick();
        assertTrue(true, "serverTick without a world must not throw");
    }

    @Test
    public void connectorStaggerFiresForEveryHash() {
        // STOR-05: the gate must fire exactly once per interval even for the
        // Integer.MIN_VALUE position hash (where upstream's abs() broke).
        int minHash = Integer.MIN_VALUE;
        assertTrue(
            StaggerGate.isDue(
                StaggerGate.phase(minHash, StaggerGate.CONNECTOR_INTERVAL),
                minHash,
                StaggerGate.CONNECTOR_INTERVAL),
            "STOR-05: the gate must fire for the Integer.MIN_VALUE hash");
        int other = 0x12345678;
        assertTrue(
            StaggerGate
                .isDue(StaggerGate.phase(other, StaggerGate.CONNECTOR_INTERVAL), other, StaggerGate.CONNECTOR_INTERVAL),
            "the gate must fire for an arbitrary hash");
        assertTrue(
            StaggerGate.phase(minHash, StaggerGate.CONNECTOR_INTERVAL) >= 0
                && StaggerGate.phase(minHash, StaggerGate.CONNECTOR_INTERVAL) < StaggerGate.CONNECTOR_INTERVAL,
            "phases must be in [0, interval)");
    }

    /**
     * STOR-03 regression: a connector with no attached inventories is not a
     * usable network; attaching one makes it usable, removing all makes it
     * unusable again.
     */
    @Test
    public void doesNotReproduceSTOR03_connectorWithNoInventoriesIsNotUsable() {
        InventoryConnectorTileEntity connector = new InventoryConnectorTileEntity();
        assertFalse(connector.hasConnectedInventories());
        assertEquals(0, connector.inventoryCount());
        connector.setInventoriesForTest(Collections.singletonList(new Box()));
        assertTrue(connector.hasConnectedInventories());
        connector.setInventoriesForTest(Collections.emptyList());
        assertFalse(connector.hasConnectedInventories());
    }

    /**
     * STOR-05 regression: the rescan gate still fires for the
     * Integer.MIN_VALUE position hash, exactly once per interval.
     */
    @Test
    public void doesNotReproduceSTOR05_minValueHashStillGetsARescanPhase() {
        int count = 0;
        for (int time = 0; time < 200; time++) {
            if (StaggerGate.isDue(time, Integer.MIN_VALUE, StaggerGate.CONNECTOR_INTERVAL)) {
                count++;
            }
        }
        assertEquals(
            10,
            count,
            "STOR-05: the MIN_VALUE hash must fire exactly once per interval (10 times in 200 ticks)");
    }

    /**
     * A connector double that carries a fixed inventory without a world
     * rescan, so the link between the interface and the connector can be
     * tested in isolation.
     */
    private static final class LinkedConnector extends InventoryConnectorTileEntity {

        LinkedConnector(IInventory inventory) {
            setInventoriesForTest(Collections.singletonList(inventory));
        }
    }

    /**
     * A subclass probe that exposes the protected hooks.
     */
    private static final class ConnectorProbe extends InventoryConnectorTileEntity {

        Module gatedModule() {
            return module();
        }

        void runServerTick() {
            serverTick();
        }
    }

    /**
     * A subclass probe that exposes the protected hooks.
     */
    private static final class InterfaceProbe extends InventoryInterfaceTileEntity {

        Module gatedModule() {
            return module();
        }

        void runServerTick() {
            serverTick();
        }
    }

    /**
     * A single-slot {@link IInventory} for the tests.
     */
    private static final class Box implements IInventory {

        private ItemStack stack;

        @Override
        public int getSizeInventory() {
            return 1;
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            return slot == 0 ? this.stack : null;
        }

        @Override
        public ItemStack decrStackSize(int slot, int count) {
            if (this.stack == null) {
                return null;
            }
            if (this.stack.stackSize <= count) {
                this.stack = null;
                return this.stack;
            }
            ItemStack out = this.stack.copy();
            out.stackSize = count;
            this.stack.stackSize -= count;
            return out;
        }

        @Override
        public ItemStack getStackInSlotOnClosing(int slot) {
            return null;
        }

        @Override
        public void setInventorySlotContents(int slot, ItemStack stack) {
            this.stack = stack;
        }

        @Override
        public String getInventoryName() {
            return "Box";
        }

        @Override
        public boolean hasCustomInventoryName() {
            return false;
        }

        @Override
        public int getInventoryStackLimit() {
            return 64;
        }

        @Override
        public void markDirty() {}

        @Override
        public boolean isUseableByPlayer(EntityPlayer player) {
            return true;
        }

        @Override
        public void openInventory() {}

        @Override
        public void closeInventory() {}

        @Override
        public boolean isItemValidForSlot(int slot, ItemStack stack) {
            return true;
        }
    }
}
