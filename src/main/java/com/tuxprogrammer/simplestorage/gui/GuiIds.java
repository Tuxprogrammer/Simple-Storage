package com.tuxprogrammer.simplestorage.gui;

/**
 * Central registry of every GUI id this mod hands to
 * {@code Network.openGui} (and friends). Keeping them here means the
 * server and client can agree on the numbers without a shared constant
 * living inside a class either side may not have loaded.
 *
 * <p>
 * Ids are never renumbered: once a build ships with a given id the
 * client and server have baked it in, so any later change must add a new
 * id and deprecate the old one rather than move it.
 *
 * Side: both (the client-only parts live in simplestorage.client).
 */
public final class GuiIds {

    /**
     * Private constructor; this is a constant holder.
     */
    private GuiIds() {}

    /**
     * The storage terminal GUI: browse and move items across linked
     * inventories.
     */
    public static final int STORAGE_TERMINAL = 0;

    /**
     * The crafting terminal GUI: the storage terminal plus a 3x3 crafting
     * grid fed by the linked inventories.
     */
    public static final int CRAFTING_TERMINAL = 1;

    /**
     * The filing cabinet GUI: inspect and rearrange the cabinet's own
     * slots.
     */
    public static final int FILING_CABINET = 2;

    /**
     * The configurator GUI: set a tile's behavior flags (which modules
     * are enabled, and how).
     */
    public static final int CONFIGURATOR = 3;

    /**
     * The level emitter GUI: pick the redstone threshold the emitter
     * fires on.
     */
    public static final int LEVEL_EMITTER = 4;

    /**
     * The item filter GUI: add or remove the item types a terminal or
     * cable passes.
     */
    public static final int ITEM_FILTER = 5;

    /**
     * The tag filter GUI: add or remove the NBT tag filters a terminal or
     * cable matches against.
     */
    public static final int TAG_FILTER = 6;

    /**
     * The inventory link GUI: manage which inventories a given tile can
     * reach through cables.
     */
    public static final int INVENTORY_LINK = 7;
}
