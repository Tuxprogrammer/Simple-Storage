package com.tuxprogrammer.simplestorage.item;

/**
 * Creative tab for all SimpleStorage+ blocks and items.
 *
 * <p>
 * Registered once and referenced by every block in the mod via
 * {@code setCreativeTab(SimpleStorageTab.INSTANCE)} so the tab is never null
 * when the creative inventory is built.
 *
 * <p>
 * Side: both.
 */
public final class SimpleStorageTab extends net.minecraft.creativetab.CreativeTabs {

    /**
     * The single shared instance of the SimpleStorage+ creative tab.
     *
     * <p>
     * Blocks should call {@code setCreativeTab(SimpleStorageTab.INSTANCE)}
     * rather than constructing their own tab.
     */
    public static final SimpleStorageTab INSTANCE = new SimpleStorageTab();

    /**
     * Constructs the creative tab.
     *
     * <p>
     * The label {@code simplestoragenh} is the unlocalized creative-tab name;
     * it is translated through the mod's language files.
     */
    private SimpleStorageTab() {
        super("simplestoragenh");
    }

    /**
     * Returns the icon item displayed in the creative-inventory tab bar.
     *
     * <p>
     * Uses the vanilla chest item, which reads as "storage" without pulling
     * in any mod block at class-load time.
     *
     * @return the vanilla chest item as the tab icon
     */
    @Override
    public net.minecraft.item.Item getTabIconItem() {
        return net.minecraft.item.Item.getItemFromBlock(net.minecraft.init.Blocks.chest);
    }
}
