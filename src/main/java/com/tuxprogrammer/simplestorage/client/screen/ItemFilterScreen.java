package com.tuxprogrammer.simplestorage.client.screen;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.resources.I18n;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import com.tuxprogrammer.simplestorage.item.ItemFilterItem;
import com.tuxprogrammer.simplestorage.menu.ItemFilterContainer;
import com.tuxprogrammer.simplestorage.network.FilterFlagsPacket;
import com.tuxprogrammer.simplestorage.network.SimpleStorageNetwork;

/**
 * The client screen of the item filter: the nine example stacks as ghost-slot
 * outlines, plus two toggles for the filter's flags.
 *
 * <p>
 * The ghost slots are drawn as 18x18 outlines over the container's
 * {@code inventorySlots}; the example stacks themselves are rendered by the
 * vanilla slot-drawing of {@link GuiContainer}. The "match NBT" and "allow
 * list" toggles each flip their local flag, apply it to the container via
 * {@link ItemFilterContainer#applyFlags(boolean, boolean)}, and send a
 * {@link FilterFlagsPacket} so the server writes the flags into the held
 * item. The toggles start from the held stack's flags, read through
 * {@link ItemFilterItem#isMatchNbt(ItemStack)} and
 * {@link ItemFilterItem#isAllowList(ItemStack)}.
 *
 * <p>
 * Side: client.
 */
public class ItemFilterScreen extends GuiContainer {

    /** The X position of the "match NBT" toggle. */
    private static final int MATCH_NBT_X = 8;

    /** The Y position of the "match NBT" toggle. */
    private static final int MATCH_NBT_Y = 148;

    /** The width of each toggle. */
    private static final int TOGGLE_WIDTH = 80;

    /** The height of each toggle. */
    private static final int TOGGLE_HEIGHT = 14;

    /** The X position of the "allow list" toggle. */
    private static final int ALLOW_LIST_X = 88;

    /** The Y position of the "allow list" toggle. */
    private static final int ALLOW_LIST_Y = 148;

    /** The button id of the "match NBT" toggle. */
    private static final int BUTTON_MATCH_NBT = 0;

    /** The button id of the "allow list" toggle. */
    private static final int BUTTON_ALLOW_LIST = 1;

    /** The filter's container, typed so its flag accessors are callable. */
    private final ItemFilterContainer container;

    /** The current value of the "match NBT" flag, applied locally on every change. */
    private boolean matchNbt;

    /** The current value of the "allow list" flag, applied locally on every change. */
    private boolean allowList;

    /**
     * Creates the screen and initializes the toggles from the held stack's
     * flags.
     *
     * @param container the filter's container
     */
    public ItemFilterScreen(ItemFilterContainer container) {
        super(container);
        this.container = container;
        this.xSize = 176;
        this.ySize = 166;
        ItemStack held = this.mc.thePlayer.inventory.getCurrentItem();
        this.matchNbt = ItemFilterItem.isMatchNbt(held);
        this.allowList = ItemFilterItem.isAllowList(held);
    }

    /**
     * Builds the two toggle buttons with their current labels.
     */
    @Override
    public void initGui() {
        super.initGui();
        this.buttonList.clear();
        this.buttonList.add(
            new GuiButton(BUTTON_MATCH_NBT, MATCH_NBT_X, MATCH_NBT_Y, TOGGLE_WIDTH, TOGGLE_HEIGHT, matchNbtLabel()));
        this.buttonList.add(
            new GuiButton(
                BUTTON_ALLOW_LIST,
                ALLOW_LIST_X,
                ALLOW_LIST_Y,
                TOGGLE_WIDTH,
                TOGGLE_HEIGHT,
                allowListLabel()));
    }

    /**
     * Draws the panel, the title, and every slot's outline.
     *
     * @param partialTicks the partial-tick value
     * @param mouseX       the mouse's x position
     * @param mouseY       the mouse's y position
     */
    @Override
    public void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        Gui.drawRect(this.guiLeft, this.guiTop, this.guiLeft + this.xSize, this.guiTop + this.ySize, 0xFFC6C6C6);
        drawBorder(this.guiLeft, this.guiTop, this.xSize, this.ySize);
        for (Slot slot : this.inventorySlots.inventorySlots) {
            drawSlotOutline(slot);
        }
        this.fontRendererObj.drawString(I18n.format("container.simplestoragenh.item_filter"), 8, 6, 0x404040);
    }

    /**
     * Handles a toggle click: flips the flag, applies it locally, and sends it
     * to the server.
     *
     * @param button the button that was clicked
     */
    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == BUTTON_MATCH_NBT) {
            this.matchNbt = !this.matchNbt;
        } else if (button.id == BUTTON_ALLOW_LIST) {
            this.allowList = !this.allowList;
        } else {
            super.actionPerformed(button);
            return;
        }
        this.container.applyFlags(this.matchNbt, this.allowList);
        SimpleStorageNetwork.channel()
            .sendToServer(new FilterFlagsPacket(this.inventorySlots.windowId, this.matchNbt, this.allowList));
        initGui();
    }

    /**
     * The "match NBT" toggle's label, reflecting its current value.
     *
     * @return the localized label
     */
    private String matchNbtLabel() {
        return I18n
            .format(this.matchNbt ? "simplestoragenh.filter.match_nbt.on" : "simplestoragenh.filter.match_nbt.off");
    }

    /**
     * The "allow list" toggle's label, reflecting its current value.
     *
     * @return the localized label
     */
    private String allowListLabel() {
        return I18n.format(this.allowList ? "simplestoragenh.filter.allow.on" : "simplestoragenh.filter.allow.off");
    }

    /**
     * Draws a slot's outline as a darker 18x18 square, one pixel inside the slot's display position.
     *
     * @param slot the slot to outline
     */
    private void drawSlotOutline(Slot slot) {
        int left = this.guiLeft + slot.xDisplayPosition - 1;
        int top = this.guiTop + slot.yDisplayPosition - 1;
        Gui.drawRect(left, top, left + 18, top + 18, 0xFF8B8B8B);
    }

    /**
     * Draws a one-pixel border around a panel.
     *
     * @param left   the panel's left edge
     * @param top    the panel's top edge
     * @param width  the panel's width
     * @param height the panel's height
     */
    private void drawBorder(int left, int top, int width, int height) {
        Gui.drawRect(left, top, left + width, top + 1, 0xFF555555);
        Gui.drawRect(left, top + height - 1, left + width, top + height, 0xFF555555);
        Gui.drawRect(left, top, left + 1, top + height, 0xFF555555);
        Gui.drawRect(left + width - 1, top, left + width, top + height, 0xFF555555);
    }
}
