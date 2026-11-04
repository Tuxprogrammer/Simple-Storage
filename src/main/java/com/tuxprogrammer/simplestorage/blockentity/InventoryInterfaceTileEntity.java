package com.tuxprogrammer.simplestorage.blockentity;
// Ported from Tom's Simple Storage (MIT):
// https://github.com/tom5454/Toms-Storage/blob/95bb61d/NeoForge/src/platform-shared/java/com/tom/storagemod/block/entity/InventoryInterfaceBlockEntity.java
// Avoids: STOR-03, STOR-05

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import net.minecraft.inventory.IInventory;
import net.minecraft.tileentity.TileEntity;

import com.tuxprogrammer.simplestorage.core.Module;
import com.tuxprogrammer.simplestorage.inventory.StaggerGate;

/**
 * Interface tile entity: exposes the inventories of a connected cable network
 * at this position. Stays linked to a neighbouring {@link InventoryConnectorTileEntity};
 * if the linked connector dies or has no inventories behind it, this tile
 * re-discovers a connector among its neighbours.
 * Side: server authoritative.
 */
public class InventoryInterfaceTileEntity extends GatedTileEntity implements InventoryNodeTile {

    private InventoryConnectorTileEntity connector;

    /**
     * Sets the field {@code connector} (the field of the same name). A pushed
     * connector is used until it becomes invalid or has no connected
     * inventories; {@link #rediscover()} remains the fallback.
     *
     * @param connector the connector to link to
     */
    public void setConnector(InventoryConnectorTileEntity connector) {
        this.connector = connector;
    }

    @Override
    protected Module module() {
        return Module.NETWORK;
    }

    @Override
    protected void serverTick() {
        if (worldObj == null) {
            return;
        }
        if (StaggerGate.isDue(
            worldObj.getTotalWorldTime(),
            Objects.hash(xCoord, yCoord, zCoord),
            StaggerGate.CONNECTOR_INTERVAL)) {
            if (connector == null || connector.isInvalid() || !connector.hasConnectedInventories()) {
                rediscover();
            }
        }
    }

    /**
     * Finds a usable {@link InventoryConnectorTileEntity} among the six adjacent
     * positions; leaves {@link #connector} null if none is found.
     */
    private void rediscover() {
        int[] dx = { 1, -1, 0, 0, 0, 0 };
        int[] dy = { 0, 0, 1, -1, 0, 0 };
        int[] dz = { 0, 0, 0, 0, 1, -1 };
        for (int i = 0; i < 6; i++) {
            int ax = xCoord + dx[i];
            int ay = yCoord + dy[i];
            int az = zCoord + dz[i];
            if (ay < 0 || ay > 255) {
                continue;
            }
            TileEntity te = worldObj.getTileEntity(ax, ay, az);
            if (te instanceof InventoryConnectorTileEntity) {
                connector = (InventoryConnectorTileEntity) te;
                return;
            }
        }
        connector = null;
    }

    @Override
    public List<IInventory> exposedInventories() {
        if (connector != null && connector.hasConnectedInventories()) {
            return connector.exposedInventories();
        }
        return Collections.emptyList();
    }

    /**
     * Whether this interface is linked to a connector that fronts at least one
     * reachable inventory.
     */
    public boolean isLinked() {
        return connector != null && connector.hasConnectedInventories();
    }
}
