package com.tuxprogrammer.simplestorage.blockentity;
// Ported from Tom's Simple Storage (MIT):
// https://github.com/tom5454/Toms-Storage/blob/95bb61d/NeoForge/src/platform-shared/java/com/tom/storagemod/block/entity/InventoryConnectorBlockEntity.java
// Avoids: STOR-03, STOR-05

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import net.minecraft.inventory.IInventory;
import net.minecraft.tileentity.TileEntity;

import com.tuxprogrammer.simplestorage.block.IInventoryCable;
import com.tuxprogrammer.simplestorage.block.ITrim;
import com.tuxprogrammer.simplestorage.config.NetworkConfig;
import com.tuxprogrammer.simplestorage.core.Module;
import com.tuxprogrammer.simplestorage.inventory.CableGraph;
import com.tuxprogrammer.simplestorage.inventory.ConnectorScan;
import com.tuxprogrammer.simplestorage.inventory.StaggerGate;

/**
 * Connector tile entity: the entry point of an inventory cable network.
 * Periodically rescans the surrounding world for cables, trims and inventories,
 * maintaining the set of inventories reachable through the cable graph.
 * Side: server authoritative.
 */
public class InventoryConnectorTileEntity extends GatedTileEntity implements InventoryNodeTile {

    private final List<IInventory> inventories = new ArrayList<IInventory>();
    private final List<Long> interfaces = new ArrayList<Long>();

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
            rescan();
        }
    }

    /**
     * Rescans the surrounding cable network, rebuilding the list of connected
     * inventories and linked interfaces. Public for testing.
     */
    public void rescan() {
        ConnectorScan.World world = new ConnectorScan.World() {

            @Override
            public boolean isTrim(long pos) {
                return worldObj.getBlock(
                    WorldTopology.unpackX(pos),
                    WorldTopology.unpackY(pos),
                    WorldTopology.unpackZ(pos)) instanceof ITrim;
            }

            @Override
            public boolean isInterface(long pos) {
                return worldObj.getTileEntity(
                    WorldTopology.unpackX(pos),
                    WorldTopology.unpackY(pos),
                    WorldTopology.unpackZ(pos)) instanceof InventoryInterfaceTileEntity;
            }

            @Override
            public boolean hasInventory(long pos) {
                int x = WorldTopology.unpackX(pos);
                int y = WorldTopology.unpackY(pos);
                int z = WorldTopology.unpackZ(pos);
                if (worldObj.getBlock(x, y, z) instanceof ITrim) {
                    return false;
                }
                if (worldObj.getBlock(x, y, z) instanceof IInventoryCable) {
                    return false;
                }
                TileEntity te = worldObj.getTileEntity(x, y, z);
                if (te instanceof InventoryNodeTile) {
                    return false;
                }
                return InventoryAccess.at(worldObj, x, y, z) != null;
            }

            @Override
            public List<Long> adjacent(long pos) {
                int x = WorldTopology.unpackX(pos);
                int y = WorldTopology.unpackY(pos);
                int z = WorldTopology.unpackZ(pos);
                List<Long> out = new ArrayList<Long>(6);
                int[] dx = { 1, -1, 0, 0, 0, 0 };
                int[] dy = { 0, 0, 1, -1, 0, 0 };
                int[] dz = { 0, 0, 0, 0, 1, -1 };
                for (int i = 0; i < 6; i++) {
                    int ax = x + dx[i];
                    int ay = y + dy[i];
                    int az = z + dz[i];
                    if (ay < 0 || ay > 255) {
                        continue;
                    }
                    out.add(WorldTopology.pack(ax, ay, az));
                }
                return out;
            }

            @Override
            public int distanceSquared(long a, long b) {
                long dx = WorldTopology.unpackX(a) - WorldTopology.unpackX(b);
                long dy = WorldTopology.unpackY(a) - WorldTopology.unpackY(b);
                long dz = WorldTopology.unpackZ(a) - WorldTopology.unpackZ(b);
                long d = dx * dx + dy * dy + dz * dz;
                return (int) Math.min(d, (long) Integer.MAX_VALUE);
            }
        };

        ConnectorScan.Result result = ConnectorScan
            .scan(WorldTopology.pack(xCoord, yCoord, zCoord), NetworkConfig.inventoryConnectorRange, world);

        Set<IInventory> seen = new HashSet<IInventory>();
        List<IInventory> found = new ArrayList<IInventory>();
        for (Long pos : result.connected()) {
            IInventory inv = InventoryAccess
                .at(worldObj, WorldTopology.unpackX(pos), WorldTopology.unpackY(pos), WorldTopology.unpackZ(pos));
            if (inv != null && seen.add(inv)) {
                found.add(inv);
            }
        }
        inventories.clear();
        inventories.addAll(found);

        interfaces.clear();
        for (Long pos : result.interfaces()) {
            interfaces.add(pos);
            if (pos == WorldTopology.pack(xCoord, yCoord, zCoord)) {
                continue;
            }
            TileEntity te = worldObj
                .getTileEntity(WorldTopology.unpackX(pos), WorldTopology.unpackY(pos), WorldTopology.unpackZ(pos));
            if (te instanceof InventoryInterfaceTileEntity) {
                ((InventoryInterfaceTileEntity) te).setConnector(this);
            }
        }

        NetworkQuery.invalidate(worldObj, xCoord, yCoord, zCoord);
    }

    @Override
    public List<IInventory> exposedInventories() {
        return Collections.unmodifiableList(inventories);
    }

    /**
     * Whether this connector currently fronts at least one reachable inventory.
     */
    public boolean hasConnectedInventories() {
        return CableGraph.hasConnectedInventories(isInvalid(), inventories.size());
    }

    /**
     * The number of inventories currently reachable through this connector.
     */
    public int inventoryCount() {
        return inventories.size();
    }

    /**
     * Replaces the list of connected inventories with the given list.
     * Package-private for testing.
     */
    void setInventoriesForTest(java.util.List<net.minecraft.inventory.IInventory> list) {
        inventories.clear();
        inventories.addAll(list);
    }
}
