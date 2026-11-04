package com.tuxprogrammer.simplestorage.client.screen;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.resources.I18n;
import net.minecraft.inventory.Slot;

import org.lwjgl.input.Mouse;

import com.tuxprogrammer.simplestorage.menu.FilingCabinetContainer;
import com.tuxprogrammer.simplestorage.network.FilingCabinetScrollPacket;
import com.tuxprogrammer.simplestorage.network.SimpleStorageNetwork;
import com.tuxprogrammer.simplestorage.terminal.ScrollMath;

/**
 * The client screen of the filing cabinet: a scrolling 9 by 5 window over the cabinet's 512 slots, plus the
 * player's inventory and hotbar.
 *
 * <p>
 * The cabinet window's 45 slots are drawn as outlines and moved by a vertical scrollbar on the right: dragging the
 * scrollbar or turning the mouse wheel changes the container's scroll row, applying it locally and sending a
 * {@link FilingCabinetScrollPacket} so the server keeps the window in step.
 *
 * <p>
 * TERM-06: the wheel handler uses only {@link ScrollMath}, so it never divides by (rows - 5); a scrollable range of
 * zero rows is a plain no-op instead of a divide-by-zero.
 *
 * <p>
 * Side: client.
 */
public class FilingCabinetScreen extends GuiContainer {

    /** The X position of the scrollbar's left edge, relative to the screen. */
    private static final int SCROLLBAR_X = 174;

    /** The Y position of the scrollbar's top edge, relative to the screen. */
    private static final int SCROLLBAR_Y = 18;

    /** The scrollbar's height in pixels. */
    private static final int SCROLLBAR_HEIGHT = 90;

    /** The maximum scroll row for the cabinet's 512 slots, 9 columns, and 5 visible rows. */
    private final int maxRows;

    /** Whether the scrollbar is currently being dragged. */
    private boolean dragging;

    /** The cabinet's container, typed so scroll row and window id are accessible. */
    private final FilingCabinetContainer container;

    /**
     * Creates the screen.
     *
     * @param container the cabinet's container
     */
    public FilingCabinetScreen(FilingCabinetContainer container) {
        super(container);
        this.container = container;
        this.xSize = 176;
        this.ySize = 204;
        this.maxRows = ScrollMath
            .maxScrollRows(512, FilingCabinetContainer.COLUMNS, FilingCabinetContainer.VISIBLE_ROWS);
    }

    /**
     * Positions the screen and resets the scrollbar drag state.
     */
    @Override
    public void initGui() {
        super.initGui();
        this.dragging = false;
    }

    /**
     * Draws the plain panel, the title, the 45 cabinet window slot outlines, the player's inventory and hotbar
     * outlines, and the scrollbar.
     *
     * @param partialTicks the partial-tick value
     * @param mouseX       the mouse's x position
     * @param mouseY       the mouse's y position
     */
    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        // The plain panel: fill, then border.
        Gui.drawRect(this.guiLeft, this.guiTop, this.guiLeft + this.xSize, this.guiTop + this.ySize, 0xFFC6C6C6);
        drawBorder(this.guiLeft, this.guiTop, this.xSize, this.ySize);

        // The 45 cabinet window slot outlines.
        for (Slot slot : this.inventorySlots.inventorySlots) {
            if (slot.getSlotIndex() < FilingCabinetContainer.WINDOW_SLOTS) {
                drawSlotOutline(slot);
            }
        }
        // The player's inventory and hotbar slot outlines.
        for (Slot slot : this.inventorySlots.inventorySlots) {
            if (slot.getSlotIndex() >= FilingCabinetContainer.WINDOW_SLOTS) {
                drawSlotOutline(slot);
            }
        }

        // The scrollbar: track, then thumb.
        drawScrollbar();
    }

    /**
     * Draws the title above the window.
     *
     * @param mouseX the mouse's x position
     * @param mouseY the mouse's y position
     */
    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        this.fontRendererObj.drawString(I18n.format("container.simplestoragenh.filing_cabinet"), 8, 6, 0x404040);
    }

    /**
     * Handles mouse clicks. A click inside the scrollbar starts (or ends) a drag; otherwise vanilla slot handling
     * applies.
     *
     * @param mouseX      the mouse's x position
     * @param mouseY      the mouse's y position
     * @param mouseButton the clicked mouse button
     */
    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (mouseButton == 0) {
            this.dragging = inScrollbar(mouseX, mouseY);
        }
    }

    /**
     * Handles mouse-button release, ending a scrollbar drag.
     */
    @Override
    protected void mouseMovedOrUp(int mouseX, int mouseY, int which) {
        super.mouseMovedOrUp(mouseX, mouseY, which);
        this.dragging = false;
    }

    /**
     * Handles the mouse wheel and a scrollbar drag: the wheel notches the scroll row by one, and a drag positions it
     * proportionally. Any change is applied to the container and sent to the server.
     */
    @Override
    public void handleMouseInput() {
        super.handleMouseInput();
        int wheel = Mouse.getEventDWheel();
        if (wheel != 0) {
            int row = ScrollMath.scrolledOffset(this.container.getScrollRow(), this.maxRows, wheel);
            setScrollRow(row);
        } else if (this.dragging) {
            int y = Mouse.getY();
            int top = this.guiTop + SCROLLBAR_Y;
            int range = Math.max(1, SCROLLBAR_HEIGHT - 1);
            int fraction = (y - top) * this.maxRows / range;
            setScrollRow(ScrollMath.clampOffset(fraction, this.maxRows));
        }
    }

    /**
     * Applies the given scroll row to the container and tells the server.
     *
     * @param row the new scroll row
     */
    private void setScrollRow(int row) {
        this.container.setScrollRow(row);
        SimpleStorageNetwork.channel()
            .sendToServer(new FilingCabinetScrollPacket(this.container.windowId, row));
    }

    /**
     * Draws the scrollbar's track and thumb.
     */
    private void drawScrollbar() {
        int x = this.guiLeft + SCROLLBAR_X;
        int y = this.guiTop + SCROLLBAR_Y;
        Gui.drawRect(x, y, x + 8, y + SCROLLBAR_HEIGHT, 0xFF8B8B8B);
        float fraction = ScrollMath.scrollFraction(this.container.getScrollRow(), this.maxRows);
        int thumbTop = y + (int) (fraction * (SCROLLBAR_HEIGHT - 8));
        Gui.drawRect(x, thumbTop, x + 8, thumbTop + 8, 0xFF555555);
    }

    /**
     * Whether the given screen position is inside the scrollbar.
     *
     * @param mouseX the mouse's x position
     * @param mouseY the mouse's y position
     * @return true if inside the scrollbar
     */
    private boolean inScrollbar(int mouseX, int mouseY) {
        int x = this.guiLeft + SCROLLBAR_X;
        int y = this.guiTop + SCROLLBAR_Y;
        return mouseX >= x && mouseX < x + 8 && mouseY >= y && mouseY < y + SCROLLBAR_HEIGHT;
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
