package com.tuxprogrammer.simplestorage.client.screen;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.resources.I18n;
import net.minecraft.inventory.Slot;

import com.tuxprogrammer.simplestorage.menu.LevelEmitterContainer;
import com.tuxprogrammer.simplestorage.network.EmitterSettingsPacket;
import com.tuxprogrammer.simplestorage.network.SimpleStorageNetwork;

/**
 * The client screen of the level emitter: the ghost filter slot, the threshold count with its +/- steppers, and the
 * comparison-mode toggle.
 *
 * <p>
 * The displayed count and comparison are always read from the container's emitter, so the screen reflects the server's
 * authoritative state. Every change is applied to the container locally and sent to the server with an
 * {@link EmitterSettingsPacket}.
 *
 * <p>
 * Side: client.
 */
public class LevelEmitterScreen extends GuiContainer {

    /** The largest threshold the emitter accepts. */
    private static final int MAX_COUNT = 1073741823;

    /** The emitter's container, typed so its emitter accessors are callable. */
    private final LevelEmitterContainer container;

    /** The amount each of the six stepper buttons changes the count by, by button id 0..5. */
    private static final int[] STEPS = { -100, -10, -1, 1, 10, 100 };

    /**
     * Creates the screen.
     *
     * @param container the emitter's container
     */
    public LevelEmitterScreen(LevelEmitterContainer container) {
        super(container);
        this.container = container;
        this.xSize = 176;
        this.ySize = 133;
    }

    /**
     * Positions the screen and creates the six stepper buttons and the comparison toggle.
     */
    @Override
    public void initGui() {
        super.initGui();
        int[] labels = { -100, -10, -1, 1, 10, 100 };
        for (int i = 0; i < STEPS.length; i++) {
            this.buttonList.add(
                new GuiButton(
                    i,
                    this.guiLeft + 8 + i * 18,
                    this.guiTop + 20,
                    16,
                    16,
                    (labels[i] > 0 ? "+" : "") + labels[i]));
        }
        // The comparison toggle is button id 6; its label is refreshed in drawGuiContainerForegroundLayer.
        this.buttonList.add(
            new GuiButton(
                6,
                this.guiLeft + 120,
                this.guiTop + 20,
                48,
                16,
                I18n.format("simplestoragenh.emitter.less")));
    }

    /**
     * Draws the plain panel, the ghost filter slot outline, and the player's inventory and hotbar outlines.
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
    }

    /**
     * Draws the title, the current count, and keeps the comparison toggle's label in step with the emitter's mode.
     *
     * @param mouseX the mouse's x position
     * @param mouseY the mouse's y position
     */
    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        this.fontRendererObj.drawString(I18n.format("container.simplestoragenh.level_emitter"), 8, 6, 0x404040);
        this.fontRendererObj.drawString(
            String.valueOf(
                this.container.getEmitter()
                    .getCount()),
            this.guiLeft + 8,
            this.guiTop + 40,
            0x404040);
        GuiButton toggle = this.buttonList.get(6);
        toggle.displayString = this.container.getEmitter()
            .isLessThan() ? I18n.format("simplestoragenh.emitter.less")
                : I18n.format("simplestoragenh.emitter.greater");
    }

    /**
     * Handles the stepper and toggle buttons.
     *
     * @param button the button that was clicked
     */
    @Override
    protected void actionPerformed(GuiButton button) {
        int count = this.container.getEmitter()
            .getCount();
        boolean lessThan = this.container.getEmitter()
            .isLessThan();
        if (button.id >= 0 && button.id < STEPS.length) {
            count += STEPS[button.id];
            if (count < 0) {
                count = 0;
            }
            if (count > MAX_COUNT) {
                count = MAX_COUNT;
            }
        } else if (button.id == 6) {
            lessThan = !lessThan;
        } else {
            return;
        }
        this.container.applySettings(count, lessThan);
        SimpleStorageNetwork.channel()
            .sendToServer(new EmitterSettingsPacket(this.inventorySlots.windowId, count, lessThan));
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
