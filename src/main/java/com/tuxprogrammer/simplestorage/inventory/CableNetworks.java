package com.tuxprogrammer.simplestorage.inventory;
// Ported from Tom's Simple Storage (MIT):
// https://github.com/tom5454/Toms-Storage/blob/95bb61d/NeoForge/src/platform-shared/java/com/tom/storagemod/inventory/InventoryCableNetwork.java

import java.util.HashMap;
import java.util.Map;

/**
 * Holds one {@link CableGraph} per level (dimension id).
 *
 * <p>
 * A {@link CableGraph} is created on first use for a dimension and the same
 * instance is returned on every later call. Unloading a dimension removes its
 * graph so its cached networks are dropped.
 *
 * <p>
 * Side: server logic, plain Java.
 */
public class CableNetworks {

    private final Map<Integer, CableGraph> graphs = new HashMap<Integer, CableGraph>();

    /**
     * Returns the {@link CableGraph} for a dimension, creating it on first use
     * and returning the same instance afterwards.
     *
     * @param dimension the dimension id
     * @return the {@link CableGraph} for that dimension
     */
    public CableGraph forLevel(int dimension) {
        CableGraph graph = graphs.get(dimension);
        if (graph == null) {
            graph = new CableGraph();
            graphs.put(dimension, graph);
        }
        return graph;
    }

    /**
     * Removes the {@link CableGraph} for a dimension, if one exists. Unknown
     * dimensions are a no-op.
     *
     * @param dimension the dimension id to unload
     */
    public void unload(int dimension) {
        graphs.remove(dimension);
    }

    /**
     * The number of dimensions that currently have a graph.
     *
     * @return the number of held graphs
     */
    public int levelCount() {
        return graphs.size();
    }
}
