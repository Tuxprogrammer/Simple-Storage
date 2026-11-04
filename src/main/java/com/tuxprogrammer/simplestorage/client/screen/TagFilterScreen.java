package com.tuxprogrammer.simplestorage.client.screen;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.resources.I18n;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import com.tuxprogrammer.simplestorage.item.TagFilterItem;
import com.tuxprogrammer.simplestorage.menu.TagFilterContainer;
import com.tuxprogrammer.simplestorage.network.SimpleStorageNetwork;
import com.tuxprogrammer.simplestorage.network.TagFilterPacket;

/**
 * The client screen of the tag filter: a text field holding the tag names,
 * plus two toggles for the filter's flags.
 *
 * <p>
 * The tag names live in a {@link GuiTextField} (width 150, max 512
 * characters) as a comma-separated list, initialized from the held stack's
 * names via {@link TagFilterItem#getNames(ItemStack)}. Pressing Enter in the
 * field, or clicking a toggle, splits the field's text on commas, trims and
 * drops the blanks, applies the result locally via
 * {@link TagFilterContainer#applySettings(List, boolean, boolean)}, and sends
 * a {@link TagFilterPacket} with the names joined by newlines.
 *
 * <p>
 * Side: client.
 */
public class TagFilterScreen extends GuiContainer {

    /** The width of the tag-name text field. */
    private static final int FIELD_WIDTH = 150;

    /** The X position of the tag-name text field. */
    private static final int FIELD_X = 13;

    /** The Y position of the tag-name text field. */
    private static final int FIELD_Y = 28;

    /** The maximum number of characters the tag-name field accepts. */
    private static final int FIELD_MAX_LENGTH = 512;

    /** The X position of the "match all" toggle. */
    private static final int MATCH_ALL_X = 8;

    /** The Y position of the "match all" toggle. */
    private static final int MATCH_ALL_Y = 148;

    /** The width of each toggle. */
    private static final int TOGGLE_WIDTH = 80;

    /** The height of each toggle. */
    private static final int TOGGLE_HEIGHT = 14;

    /** The X position of the "allow list" toggle. */
    private static final int ALLOW_LIST_X = 88;

    /** The Y position of the "allow list" toggle. */
    private static final int ALLOW_LIST_Y = 148;

    /** The button id of the "match all" toggle. */
    private static final int BUTTON_MATCH_ALL = 0;

    /** The button id of the "allow list" toggle. */
    private static final int BUTTON_ALLOW_LIST = 1;

    /** The text field holding the tag names as a comma-separated list. */
    private final GuiTextField textField;

    /** The filter's container, typed so its settings accessors are callable. */
    private final TagFilterContainer container;

    /** The current value of the "match all" flag, applied locally on every change. */
    private boolean matchAll;

    /** The current value of the "allow list" flag, applied locally on every change. */
    private boolean allowList;

    /**
     * Creates the screen and initializes the field and toggles from the held
     * stack.
     *
     * @param container the filter's container
     */
    public TagFilterScreen(TagFilterContainer container) {
        super(container);
        this.container = container;
        this.xSize = 176;
        this.ySize = 166;
        this.matchAll = TagFilterItem.isMatchAll(this.mc.thePlayer.inventory.getCurrentItem());
        this.allowList = TagFilterItem.isAllowList(this.mc.thePlayer.inventory.getCurrentItem());
        this.textField = new GuiTextField(this.fontRendererObj, FIELD_X, FIELD_Y, FIELD_WIDTH, 12);
        this.textField.setMaxStringLength(FIELD_MAX_LENGTH);
        this.textField.setText(joinWithCommas(TagFilterItem.getNames(this.mc.thePlayer.inventory.getCurrentItem())));
    }

    /**
     * Builds the two toggle buttons, positions the text field, and focuses it.
     */
    @Override
    public void initGui() {
        super.initGui();
        this.buttonList.clear();
        this.buttonList.add(
            new GuiButton(BUTTON_MATCH_ALL, MATCH_ALL_X, MATCH_ALL_Y, TOGGLE_WIDTH, TOGGLE_HEIGHT, matchAllLabel()));
        this.buttonList.add(
            new GuiButton(
                BUTTON_ALLOW_LIST,
                ALLOW_LIST_X,
                ALLOW_LIST_Y,
                TOGGLE_WIDTH,
                TOGGLE_HEIGHT,
                allowListLabel()));
        this.textField.xPosition = this.guiLeft + FIELD_X;
        this.textField.yPosition = this.guiTop + FIELD_Y;
        this.textField.setFocused(true);
        this.textField.setText(this.textField.getText());
    }

    /**
     * Draws the panel, the title, and the player slot outlines.
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
        this.fontRendererObj.drawString(I18n.format("container.simplestoragenh.tag_filter"), 8, 6, 0x404040);
    }

    /**
     * Draws the tag-name text field over the panel.
     *
     * @param mouseX the mouse's x position
     * @param mouseY the mouse's y position
     */
    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
        this.textField.drawTextBox();
    }

    /**
     * Routes clicks to the text field first; otherwise vanilla button and slot
     * handling applies.
     *
     * @param mouseX      the mouse's x position
     * @param mouseY      the mouse's y position
     * @param mouseButton the clicked mouse button
     */
    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        this.textField.mouseClicked(mouseX, mouseY, mouseButton);
    }

    /**
     * Routes key input to the text field while it is focused; Enter (return)
     * applies the field's names and flags.
     *
     * @param keyCode  the key's code
     * @param charCode the key's character code
     */
    @Override
    protected void keyTyped(char typedChar, int keyCode) {
        if (this.textField.isFocused()) {
            if (this.textField.textboxKeyTyped(typedChar, keyCode)) {
                if (keyCode == 1) {
                    applyAndSend();
                }
                return;
            }
        }
        super.keyTyped(typedChar, keyCode);
    }

    /**
     * Handles a toggle click: flips the flag, then applies the field's names
     * and both flags locally and sends them.
     *
     * @param button the button that was clicked
     */
    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == BUTTON_MATCH_ALL) {
            this.matchAll = !this.matchAll;
        } else if (button.id == BUTTON_ALLOW_LIST) {
            this.allowList = !this.allowList;
        } else {
            super.actionPerformed(button);
            return;
        }
        applyAndSend();
        initGui();
    }

    /**
     * Applies the field's names and the current flags to the container and
     * sends them to the server.
     */
    private void applyAndSend() {
        List<String> names = splitNames(this.textField.getText());
        this.container.applySettings(names, this.matchAll, this.allowList);
        StringBuilder joined = new StringBuilder();
        for (String name : names) {
            if (joined.length() > 0) {
                joined.append('\n');
            }
            joined.append(name);
        }
        SimpleStorageNetwork.channel()
            .sendToServer(
                new TagFilterPacket(this.inventorySlots.windowId, joined.toString(), this.matchAll, this.allowList));
    }

    /**
     * The "match all" toggle's label, reflecting its current value.
     *
     * @return the localized label
     */
    private String matchAllLabel() {
        return I18n.format(this.matchAll ? "simplestoragenh.tags.match_all.on" : "simplestoragenh.tags.match_all.off");
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
     * Joins the tag names into a comma-separated list for the text field.
     *
     * @param names the tag names
     * @return the comma-separated list
     */
    private static String joinWithCommas(List<String> names) {
        StringBuilder joined = new StringBuilder();
        for (String name : names) {
            if (joined.length() > 0) {
                joined.append(',');
            }
            joined.append(name);
        }
        return joined.toString();
    }

    /**
     * Splits the field's text into tag names: each piece on a comma is trimmed
     * and the blanks are dropped.
     *
     * @param text the field's text
     * @return the cleaned tag names, in order
     */
    private static List<String> splitNames(String text) {
        List<String> names = new ArrayList<String>();
        if (text == null) {
            return names;
        }
        String[] parts = text.split(",", -1);
        for (String part : parts) {
            String trimmed = part.trim();
            if (trimmed.length() > 0) {
                names.add(trimmed);
            }
        }
        return names;
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
