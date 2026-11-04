package com.tuxprogrammer.simplestorage.client.screen;

import net.minecraft.inventory.Slot;

import com.tuxprogrammer.simplestorage.menu.CraftingTerminalContainer;

/**
 * The client screen of the crafting terminal: a storage terminal with a working 3x3 crafting grid and a result slot.
 *
 * <p>
 * The storage grid, the player's inventory and hotbar, and all input handling are inherited from
 * {@link StorageTerminalScreen}; the container already owns the grid and result slots, so this screen only adds
 * their outlines on top of the inherited drawing.
 *
 * <p>
 * Side: client.
 */
public class CraftingTerminalScreen extends StorageTerminalScreen {

    /**
     * Creates the screen.
     *
     * @param container the crafting terminal's container
     */
    public CraftingTerminalScreen(CraftingTerminalContainer container) {
        super(container);
    }

    /**
     * Draws the inherited background first, then the outlines of the crafting grid's nine slots and the single result
     * slot.
     *
     * <p>
     * The container's slots 0..35 are the player's inventory and hotbar; the grid and result slots are appended after
     * them, so drawing every slot whose index is beyond the 36 player slots covers the 3x3 grid and the result slot
     * and nothing else.
     *
     * @param partialTicks the partial-tick value
     * @param mouseX       the mouse's x position
     * @param mouseY       the mouse's y position
     */
    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        super.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
        for (Slot slot : this.inventorySlots.inventorySlots) {
            if (slot.getSlotIndex() > 35) {
                drawSlotOutline(slot);
            }
        }
    }

    /**
     * Draws a single slot's outline as a darker 18x18 square, one pixel inside the slot's display position, so the
     * slot reads as a hollow cell on top of the panel.
     *
     * @param slot the slot to outline
     */
    private void drawSlotOutline(Slot slot) {
        int left = this.guiLeft + slot.xDisplayPosition - 1;
        int top = this.guiTop + slot.yDisplayPosition - 1;
        net.minecraft.client.gui.Gui.drawRect(left, top, left + 18, top + 18, 0xFF8B8B8B);
    }
}
