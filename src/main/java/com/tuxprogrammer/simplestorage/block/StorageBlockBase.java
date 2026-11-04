package com.tuxprogrammer.simplestorage.block;

import com.tuxprogrammer.simplestorage.SimpleStorage;
import com.tuxprogrammer.simplestorage.item.SimpleStorageTab;

/**
 * Common base class for all SimpleStorage+ storage blocks.
 *
 * <p>
 * Centralizes the wiring that every block in the mod shares: unlocalized
 * block and texture names derived from the mod id, hardness/resistance, the
 * shared creative tab, and a metallic step sound. Subclasses only add the
 * behavior and tile entity specific to their storage type.
 *
 * <p>
 * Notes:
 * <ul>
 * <li>{@link #getRenderType()} is intentionally <em>not</em> overridden —
 * the client-only {@code BlockStorage} subclass in
 * {@code simplestorage.client} handles rendering.</li>
 * <li>{@link #hasComparatorInputOverride()} is intentionally <em>not</em>
 * overridden — comparators are wired up individually where needed.</li>
 * </ul>
 *
 * <p>
 * Side: both (the client-only parts live in simplestorage.client).
 */
public abstract class StorageBlockBase extends net.minecraft.block.BlockContainer {

    /**
     * The bare registry-name component (without the mod-id prefix) that was
     * passed to the constructor.
     *
     * <p>
     * Kept for later use by client registration and lang-key lookups,
     * where the full unlocalized name would be redundant.
     */
    private final String name;

    /**
     * Constructs a storage block with the given material, name, and hardness.
     *
     * <p>
     * Applies the shared defaults for all SimpleStorage+ blocks:
     * unlocalized block and texture names prefixed with the mod id,
     * hardness/resistance from the given value, the shared creative tab,
     * and the metallic step sound.
     *
     * @param material the block material
     * @param name     the bare registry-name component (mod id is prefixed)
     * @param hardness the block hardness; resistance is set to {@code hardness * 5.0F}
     */
    protected StorageBlockBase(net.minecraft.block.material.Material material, String name, float hardness) {
        super(material);
        this.name = name;
        setBlockName(SimpleStorage.MODID + "." + name);
        setBlockTextureName(SimpleStorage.MODID + ":" + name);
        setHardness(hardness);
        setResistance(hardness * 5.0F);
        setCreativeTab(SimpleStorageTab.INSTANCE);
        setStepSound(net.minecraft.block.Block.soundTypeMetal);
    }

    /**
     * Returns the bare registry-name component passed to the constructor
     * (without the mod-id prefix).
     *
     * @return the registry-name component
     */
    public final String registryName() {
        return name;
    }

    /**
     * Returns the tile entity created when a player places this block.
     *
     * <p>
     * Returns {@code null} by default; storage subclasses override this
     * to create their specific tile entity.
     *
     * @param world the world the block is being placed in
     * @param meta  the block metadata
     * @return a new tile entity, or {@code null} if this block has none
     */
    @Override
    public net.minecraft.tileentity.TileEntity createNewTileEntity(net.minecraft.world.World world, int meta) {
        return null;
    }
}
