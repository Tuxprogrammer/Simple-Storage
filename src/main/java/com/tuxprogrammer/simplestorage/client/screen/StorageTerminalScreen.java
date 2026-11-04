package com.tuxprogrammer.simplestorage.client.screen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.oredict.OreDictionary;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import com.tuxprogrammer.simplestorage.blockentity.StackKeys;
import com.tuxprogrammer.simplestorage.blockentity.TerminalEntry;
import com.tuxprogrammer.simplestorage.menu.StorageTerminalContainer;
import com.tuxprogrammer.simplestorage.network.SimpleStorageNetwork;
import com.tuxprogrammer.simplestorage.network.TerminalActionPacket;
import com.tuxprogrammer.simplestorage.network.TerminalSettingsPacket;
import com.tuxprogrammer.simplestorage.sort.SortKey;
import com.tuxprogrammer.simplestorage.sort.SortingTypes;
import com.tuxprogrammer.simplestorage.terminal.NumberFormat;
import com.tuxprogrammer.simplestorage.terminal.ScrollMath;
import com.tuxprogrammer.simplestorage.terminal.SearchQuery;
import com.tuxprogrammer.simplestorage.terminal.SlotAction;
import com.tuxprogrammer.simplestorage.terminal.TerminalModes;
import com.tuxprogrammer.simplestorage.terminal.TerminalView;
import com.tuxprogrammer.simplestorage.ui.EnumCycle;
import com.tuxprogrammer.simplestorage.ui.ScrollbarModel;

/**
 * The client screen for the storage terminal: a search field, a column of
 * setting buttons (sort type, reverse, ghost mode, control mode, tall mode),
 * a 9-column item grid with a scrollbar, and the player inventory below.
 *
 * <p>
 * The item list is a {@link TerminalView} over a {@link Source} adapter that
 * reads {@link TerminalEntry}s (sort key from {@link StackKeys#sortKeyOf},
 * search data from the prototype stack, key from the entry). The grid draws
 * the view's visible page, one row per 18 px, with counts abbreviated by
 * {@link NumberFormat} at 0.5 scale in the cell's lower-right corner.
 *
 * <p>
 * Clicks on the grid resolve to a {@link SlotAction} per the active control
 * mode (AE, RS, or DEF, see the original spec's "Click on a storage slot"
 * item) and are sent to the server as a {@link TerminalActionPacket}; setting
 * changes are sent as {@link TerminalSettingsPacket}s. The server stays the
 * single source of truth: the client only decodes and displays.
 *
 * <p>
 * Tall mode switches the grid to 8 rows: the container's player slots keep
 * their server positions, so {@link #repositionPlayerSlots(boolean)} moves
 * their client-side display positions down by 3 rows and grows {@link #ySize}.
 *
 * Side: client.
 */
public class StorageTerminalScreen extends GuiContainer {

    /** Lang key for the search field prompt. */
    public static final String LANG_SEARCH = "simplestoragenh.terminal.search";

    /** Lang key for the sort-by-amount button. */
    public static final String LANG_SORT_AMOUNT = "simplestoragenh.terminal.sort.amount";

    /** Lang key for the sort-by-name button. */
    public static final String LANG_SORT_NAME = "simplestoragenh.terminal.sort.name";

    /** Lang key for the sort-by-mod button. */
    public static final String LANG_SORT_MOD = "simplestoragenh.terminal.sort.mod";

    /** Lang key for the sort-by-space-efficiency button. */
    public static final String LANG_SORT_SPACE = "simplestoragenh.terminal.sort.space";

    /** Lang key for the sort-by-id button. */
    public static final String LANG_SORT_ID = "simplestoragenh.terminal.sort.id";

    /** Lang key for the reverse-order button. */
    public static final String LANG_REVERSE = "simplestoragenh.terminal.reverse";

    /** Lang key for the ghost-mode button when on. */
    public static final String LANG_GHOST_ON = "simplestoragenh.terminal.ghost.on";

    /** Lang key for the ghost-mode button when off. */
    public static final String LANG_GHOST_OFF = "simplestoragenh.terminal.ghost.off";

    /** Lang key for the control-mode button in AE mode. */
    public static final String LANG_CONTROL_AE = "simplestoragenh.terminal.control.ae";

    /** Lang key for the control-mode button in RS mode. */
    public static final String LANG_CONTROL_RS = "simplestoragenh.terminal.control.rs";

    /** Lang key for the control-mode button in DEF mode. */
    public static final String LANG_CONTROL_DEF = "simplestoragenh.terminal.control.def";

    /** Lang key for the tall-mode button when on. */
    public static final String LANG_TALL_ON = "simplestoragenh.terminal.tall.on";

    /** Lang key for the tall-mode button when off. */
    public static final String LANG_TALL_OFF = "simplestoragenh.terminal.tall.off";

    /** Lang key for the "item amount" tooltip line, formatted with the count. */
    public static final String LANG_AMOUNT = "simplestoragenh.terminal.amount";

    /** Screen width in px. */
    public static final int SCREEN_WIDTH = 194;

    /** Screen height in px, normal mode. */
    public static final int SCREEN_HEIGHT = 204;

    /** Screen height in px, tall mode (5 + 3 extra grid rows). */
    public static final int SCREEN_HEIGHT_TALL = 258;

    /** Vertical shift in px applied to the player slots in tall mode. */
    public static final int TALL_SLOT_SHIFT = 3 * 18;

    /** Number of grid rows in normal mode. */
    private static final int NORMAL_ROWS = 5;

    /** Number of grid rows in tall mode. */
    private static final int TALL_ROWS = 8;

    /** Pixels per grid row. */
    public static final int CELL = 18;

    /** X position of the scrollbar, relative to the screen. */
    private static final int SCROLLBAR_X = 174;

    /** Width of the scrollbar region in px. */
    private static final int SCROLLBAR_WIDTH = 14;

    /** Maximum length of the search text. */
    private static final int SEARCH_MAX_LENGTH = 100;

    /** The container this screen is bound to. */
    private final StorageTerminalContainer container;

    /** The pure list model: items, search text, sort mode, frozen (ghost) order. */
    private final TerminalView<TerminalEntry> view;

    /** The search field; built in {@link #initGui()}. */
    private GuiTextField searchField;

    /** Sort mode of the list. */
    private SortingTypes sortType = SortingTypes.AMOUNT;

    /** Whether the list order is reversed (descending). */
    private boolean reversed = false;

    /** The last ghost (frozen-order) state pushed to the view. */
    private boolean frozen = false;

    /** Whether ghost (frozen-order) mode is enabled. */
    private boolean ghostOn = true;

    /** Control mode: 0 = AE, 1 = RS, 2 = DEF. */
    private int controlMode = TerminalModes.CONTROL_AE;

    /** Whether tall mode (8 grid rows) is active. */
    private boolean tall = false;

    /** The searchType word, kept so settings packets carry it unchanged. */
    private int searchType = TerminalModes.DEFAULT_SEARCH_TYPE;

    /**
     * The search text on the last settings change; the field is reloaded with it (once) when it is empty and the KEEP
     * flag is set.
     */
    private String lastSearch = "";

    /** The search field's text on the last client tick; compared to detect changes. */
    private String searchText = "";

    /** Whether the last-search text has already been loaded into the field. */
    private boolean lastSearchLoaded = false;

    /** The current scroll offset in rows. */
    private int scrollOffsetRows = 0;

    /** Whether the left button is held down on the scrollbar. */
    private boolean scrolling = false;

    /** Cached item tooltips, keyed by entry key, computed lazily. */
    private final Map<String, List<String>> tooltipCache = new HashMap<String, List<String>>();

    /**
     * Creates the screen.
     *
     * @param container the storage terminal container
     */
    public StorageTerminalScreen(StorageTerminalContainer container) {
        super(container);
        this.container = container;
        this.xSize = SCREEN_WIDTH;
        this.ySize = SCREEN_HEIGHT;
        this.view = new TerminalView<TerminalEntry>(new EntrySource());
    }

    /**
     * Replaces the synced item list. The scroll offset is kept within the
     * new list's range.
     *
     * @param entries the server's current entries, or null for none
     */
    public void setServerList(List<TerminalEntry> entries) {
        view.setItems(entries);
        scrollOffsetRows = ScrollMath.clampOffset(
            scrollOffsetRows,
            ScrollMath.maxScrollRows(view.size(), StorageTerminalContainer.STORAGE_COLUMNS, visibleRows()));
    }

    /**
     * Applies the terminal's persisted settings, decoded from their bit
     * words. The sort, reverse, ghost, control-mode, and tall states are
     * updated, the view re-sorts, and the buttons refresh. When the search
     * field is empty and the KEEP flag is set, the field is filled with the
     * server's stored search text (once, until the text changes).
     *
     * @param sorting    the sorting word (ordinal + reverse + ghost-off bits)
     * @param modes      the modes word (control mode + tall bit)
     * @param searchType the searchType word
     * @param lastSearch the server's stored search text
     */
    public void setState(int sorting, int modes, int searchType, String lastSearch) {
        sortType = SortingTypes.fromOrdinal(TerminalModes.getSortOrdinal(sorting));
        reversed = TerminalModes.isReversed(sorting);
        ghostOn = TerminalModes.isGhostOn(sorting);
        controlMode = TerminalModes.getControlMode(modes);
        boolean newTall = TerminalModes.isTall(modes);
        this.searchType = searchType;
        this.lastSearch = lastSearch == null ? "" : lastSearch;
        view.setSorting(sortType, reversed);
        if (newTall != tall) {
            tall = newTall;
            repositionPlayerSlots(tall);
        }
        if (searchField != null && !searchField.isFocused()
            && searchField.getText()
                .isEmpty()
            && TerminalModes.hasSearchFlag(searchType, TerminalModes.SEARCH_KEEP)
            && !lastSearchLoaded
            && !lastSearch.isEmpty()) {
            searchField.setText(lastSearch);
            lastSearchLoaded = true;
            updateSearch();
        }
        refreshButtons();
    }

    /**
     * Builds the search field and the setting buttons at their fixed
     * positions, and focuses the field when the FOCUS flag is set.
     */
    @Override
    public void initGui() {
        super.initGui();

        searchField = new GuiTextField(fontRendererObj, guiLeft + 82, guiTop + 6, 89, 16);
        searchField.setMaxStringLength(SEARCH_MAX_LENGTH);
        searchField.setText(searchText);
        if (searchText.isEmpty() && TerminalModes.hasSearchFlag(searchType, TerminalModes.SEARCH_KEEP)
            && !lastSearch.isEmpty()
            && !lastSearchLoaded) {
            searchField.setText(lastSearch);
            lastSearchLoaded = true;
        }
        searchField.setFocused(TerminalModes.hasSearchFlag(searchType, TerminalModes.SEARCH_FOCUS));

        int bx = guiLeft - 18;
        int by = guiTop + 5;
        buttonList.add(new GuiButton(0, bx, by, 18, 18, sortLang()));
        buttonList.add(new GuiButton(1, bx, by + CELL, 18, 18, I18n.format(LANG_REVERSE)));
        buttonList.add(new GuiButton(2, bx, by + 2 * CELL, 18, 18, ghostLang()));
        buttonList.add(new GuiButton(3, bx, by + 3 * CELL, 18, 18, controlLang()));
        buttonList.add(new GuiButton(4, bx, by + 4 * CELL, 18, 18, tallLang()));
    }

    /**
     * Ticks the search: whenever the field's text changes the view re-filters
     * and the list returns to the top.
     */
    @Override
    public void updateScreen() {
        super.updateScreen();
        if (searchField != null) {
            String text = searchField.getText();
            if (!text.equals(searchText)) {
                searchText = text;
                view.setSearch(text);
                lastSearchLoaded = false;
                scrollOffsetRows = 0;
            }
        }
        // Ghost (frozen) mode: freeze the displayed order while SHIFT is held.
        boolean frozen = ghostOn && Keyboard.isKeyDown(Keyboard.KEY_LSHIFT);
        if (frozen != this.frozen) {
            view.setFrozen(frozen);
        }
    }

    /**
     * Draws the background panel, the grid (items plus counts), the search
     * field, the buttons, the scrollbar, and the inventory.
     */
    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        int rows = visibleRows();
        // Panel: dark gray fill with a lighter border.
        Gui.drawRect(guiLeft - 1, guiTop - 1, guiLeft + SCREEN_WIDTH, guiTop - 1 + ySize, 0xFF555555);
        Gui.drawRect(guiLeft, guiTop, guiLeft + SCREEN_WIDTH - 2, guiTop + ySize - 2, 0xFFC6C6C6);

        // The grid, one row at a time from the view's visible page.
        int gx = guiLeft + StorageTerminalContainer.STORAGE_X;
        int gy = guiTop + StorageTerminalContainer.STORAGE_Y;
        List<TerminalEntry> page = view.page(scrollOffsetRows, StorageTerminalContainer.STORAGE_COLUMNS, rows);
        RenderHelper.enableGUIStandardItemLighting();
        for (int i = 0; i < page.size(); i++) {
            TerminalEntry entry = page.get(i);
            int cx = gx + (i % StorageTerminalContainer.STORAGE_COLUMNS) * CELL;
            int cy = gy + (i / StorageTerminalContainer.STORAGE_COLUMNS) * CELL;
            ItemStack stack = entry.prototype();
            itemRender.renderItemAndEffectIntoGUI(fontRendererObj, mc.getTextureManager(), stack, cx, cy);
            String count = NumberFormat.formatNumber(entry.count());
            int textWidth = fontRendererObj.getStringWidth(count);
            GL11.glPushMatrix();
            GL11.glScalef(0.5F, 0.5F, 0.5F);
            fontRendererObj
                .drawStringWithShadow(count, (cx + CELL - 2) * 2 - textWidth, (cy + CELL) * 2 - 10, 0xFFFFFF);
            GL11.glPopMatrix();
        }
        RenderHelper.disableStandardItemLighting();

        if (searchField != null) {
            searchField.drawTextBox();
        }
        for (GuiButton button : buttonList) {
            button.drawButton(mc, mouseX, mouseY);
        }

        drawScrollbar(rows);
    }

    /**
     * Draws the foreground. The inventory label and slot stacks are drawn by
     * the base class; this screen adds nothing.
     */
    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }

    /**
     * Draws the scrollbar thumb when the list does not fit.
     *
     * @param rows the number of visible grid rows
     */
    private void drawScrollbar(int rows) {
        ScrollbarModel bar = new ScrollbarModel(
            guiLeft + SCROLLBAR_X,
            guiTop + StorageTerminalContainer.STORAGE_Y,
            SCROLLBAR_WIDTH,
            rows * CELL,
            view.rowCount(StorageTerminalContainer.STORAGE_COLUMNS),
            rows);
        if (bar.needsScroll()) {
            double fraction = ScrollMath.scrollFraction(
                scrollOffsetRows,
                ScrollMath.maxScrollRows(view.size(), StorageTerminalContainer.STORAGE_COLUMNS, rows));
            bar.setCurrentScroll(fraction);
            Gui.drawRect(
                bar.getX(),
                bar.getY(),
                bar.getX() + SCROLLBAR_WIDTH,
                bar.getY() + bar.getHeight(),
                0xFF444444);
            Gui.drawRect(
                bar.getThumbX(),
                bar.getThumbY(),
                bar.getThumbX() + ScrollbarModel.THUMB_WIDTH,
                bar.getThumbY() + bar.getThumbHeight(),
                0xFFBBBBBB);
        }
    }

    /**
     * Routes clicks: buttons first, then the search field, then the
     * scrollbar, then the storage grid; everything else goes to the base
     * class (the player inventory).
     */
    @Override
    public void mouseClicked(int mouseX, int mouseY, int button) {
        if (button == 0) {
            for (GuiButton guiButton : buttonList) {
                if (guiButton.mousePressed(this.mc, mouseX, mouseY)) {
                    actionPerformed(guiButton);
                    return;
                }
            }
        }

        if (searchField != null) {
            searchField.mouseClicked(mouseX, mouseY, button);
        }
        if (searchField != null && searchField.isFocused()) {
            if (button == 1) {
                searchField.setText("");
                updateSearch();
            }
            return;
        }

        if (inScrollbar(mouseX, mouseY)) {
            if (button == 0) {
                scrolling = true;
                dragScrollbar(mouseY);
            }
            return;
        }

        if (inGrid(mouseX, mouseY)) {
            handleGridClick(mouseX, mouseY, button);
            return;
        }

        super.mouseClicked(mouseX, mouseY, button);
    }

    /**
     * Handles mouse input: the search field's keys, wheel scrolling, and
     * scrollbar dragging.
     */
    @Override
    public void handleMouseInput() {
        super.handleMouseInput();
        int wheel = Mouse.getEventDWheel();
        if (wheel != 0) {
            int rows = visibleRows();
            int maxRows = ScrollMath.maxScrollRows(view.size(), StorageTerminalContainer.STORAGE_COLUMNS, rows);
            scrollOffsetRows = ScrollMath.scrolledOffset(scrollOffsetRows, maxRows, wheel);
        }
        if (scrolling && Mouse.isButtonDown(0)) {
            dragScrollbar(Mouse.getY());
        }
    }

    /**
     * Types a character into the search field when it is focused.
     */
    @Override
    public void keyTyped(char typedChar, int keyCode) {
        if (searchField != null && searchField.isFocused()) {
            if (searchField.textboxKeyTyped(typedChar, keyCode)) {
                updateSearch();
            }
            return;
        }
        super.keyTyped(typedChar, keyCode);
    }

    /**
     * Applies a setting-button press and sends the new settings to the
     * server.
     */
    @Override
    public void actionPerformed(GuiButton button) {
        switch (button.id) {
            case 0:
                sortType = sortType.next();
                break;
            case 1:
                reversed = !reversed;
                break;
            case 2:
                ghostOn = !ghostOn;
                break;
            case 3:
                controlMode = EnumCycle.next(controlMode, 3);
                break;
            case 4:
                tall = !tall;
                repositionPlayerSlots(tall);
                break;
            default:
                super.actionPerformed(button);
                return;
        }
        view.setSorting(sortType, reversed);
        refreshButtons();
        sendSettings();
    }

    /**
     * Redraws the button labels to reflect the current settings.
     */
    private void refreshButtons() {
        if (buttonList.size() < 5) {
            return; // initGui has not run yet
        }
        buttonList.get(0).displayString = sortLang();
        buttonList.get(2).displayString = ghostLang();
        buttonList.get(3).displayString = controlLang();
        buttonList.get(4).displayString = tallLang();
    }

    /**
     * Sends the current settings to the server.
     */
    private void sendSettings() {
        int sorting = sortType.ordinal();
        if (reversed) {
            sorting |= TerminalModes.SORT_REVERSED;
        }
        if (!ghostOn) {
            sorting |= TerminalModes.SORT_GHOST_OFF;
        }
        int modes = controlMode;
        if (tall) {
            modes |= TerminalModes.MODE_TALL;
        }
        SimpleStorageNetwork.channel()
            .sendToServer(new TerminalSettingsPacket(container.windowId, sorting, modes, searchType));
    }

    /**
     * Moves every player slot's client-side display position down by
     * {@link #TALL_SLOT_SHIFT} px (or back up) and adjusts {@link #ySize}.
     * The slots keep their server-side positions, which the container owns.
     *
     * @param tall whether tall mode is being enabled (true) or disabled (false)
     */
    private void repositionPlayerSlots(boolean tall) {
        int delta = tall ? TALL_SLOT_SHIFT : -TALL_SLOT_SHIFT;
        for (Slot slot : inventorySlots.inventorySlots) {
            slot.yDisplayPosition += delta;
        }
        ySize = tall ? SCREEN_HEIGHT_TALL : SCREEN_HEIGHT;
    }

    /**
     * Re-filters the list with the search field's current text.
     */
    private void updateSearch() {
        if (searchField == null) {
            return;
        }
        String text = searchField.getText();
        searchText = text;
        view.setSearch(text);
        lastSearchLoaded = false;
        scrollOffsetRows = 0;
    }

    /**
     * @return the number of grid rows currently visible (5 normal, 8 tall).
     */
    private int visibleRows() {
        return tall ? TALL_ROWS : NORMAL_ROWS;
    }

    /**
     * Handles a click inside the storage grid.
     *
     * <p>
     * The action is chosen per control mode (original spec item 18): AE:
     * left = pull/push a stack, right = half, shift+left = shift-pull,
     * shift+right = pull one; RS: left = pull/push, right = half,
     * shift+right = shift-pull; DEF: left = pull/push, right = half,
     * shift+left = shift-pull, ctrl+left = pull one. With a carried stack, a
     * left click pushes it (the server decides the rest); a click on an
     * empty part of the grid with a carried stack pushes by the carried
     * stack's key.
     */
    private void handleGridClick(int mouseX, int mouseY, int button) {
        int rows = visibleRows();
        int gx = guiLeft + StorageTerminalContainer.STORAGE_X;
        int gy = guiTop + StorageTerminalContainer.STORAGE_Y;
        int relX = mouseX - gx;
        int relY = mouseY - gy;
        int col = relX / CELL;
        int row = relY / CELL;

        boolean shift = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT);
        boolean ctrl = Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) || Keyboard.isKeyDown(Keyboard.KEY_RCONTROL);
        boolean left = button == 0;
        boolean right = button == 1;

        ItemStack carried = mc.thePlayer.inventory.getItemStack();

        List<TerminalEntry> page = view.page(scrollOffsetRows, StorageTerminalContainer.STORAGE_COLUMNS, rows);
        int index = row * StorageTerminalContainer.STORAGE_COLUMNS + col;
        TerminalEntry entry = null;
        if (row >= 0 && row < rows
            && col >= 0
            && col < StorageTerminalContainer.STORAGE_COLUMNS
            && index < page.size()) {
            entry = page.get(index);
        }

        SlotAction action;
        if (controlMode == TerminalModes.CONTROL_AE) {
            // AE: left = pull/push, right = half, shift+left = shift-pull,
            // shift+right = pull one.
            if (left && shift) {
                action = SlotAction.SHIFT_PULL;
            } else if (right && shift) {
                action = SlotAction.PULL_ONE;
            } else if (right) {
                action = SlotAction.GET_HALF;
            } else {
                action = SlotAction.PULL_OR_PUSH_STACK;
            }
        } else if (controlMode == TerminalModes.CONTROL_RS) {
            // RS: left = pull/push, right = half, shift+right = shift-pull.
            if (right && shift) {
                action = SlotAction.SHIFT_PULL;
            } else if (right) {
                action = SlotAction.GET_HALF;
            } else {
                action = SlotAction.PULL_OR_PUSH_STACK;
            }
        } else {
            // DEF: left = pull/push, right = half, shift+left = shift-pull,
            // ctrl+left = pull one.
            if (left && shift) {
                action = SlotAction.SHIFT_PULL;
            } else if (left && ctrl) {
                action = SlotAction.PULL_ONE;
            } else if (right) {
                action = SlotAction.GET_HALF;
            } else {
                action = SlotAction.PULL_OR_PUSH_STACK;
            }
        }

        if (entry != null) {
            sendAction(action, entry.key(), shift);
        } else if (carried != null) {
            // Click on an empty part of the grid with a carried stack: push
            // by the carried stack's key; the server decides.
            sendAction(SlotAction.PULL_OR_PUSH_STACK, StackKeys.keyOf(carried), shift);
        }
    }

    /**
     * Sends a click action to the server.
     *
     * @param action the action performed
     * @param key    the target entry's key
     * @param shift  whether shift was held
     */
    private void sendAction(SlotAction action, String key, boolean shift) {
        SimpleStorageNetwork.channel()
            .sendToServer(new TerminalActionPacket(container.windowId, action, key, shift));
    }

    /**
     * Draws the hover tooltip for the entry under the mouse: the item's
     * tooltip lines plus the full count line.
     *
     * @param mouseX the mouse x position
     * @param mouseY the mouse y position
     */
    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);

        int rows = visibleRows();
        int gx = guiLeft + StorageTerminalContainer.STORAGE_X;
        int gy = guiTop + StorageTerminalContainer.STORAGE_Y;
        int col = (mouseX - gx) / CELL;
        int row = (mouseY - gy) / CELL;
        List<TerminalEntry> page = view.page(scrollOffsetRows, StorageTerminalContainer.STORAGE_COLUMNS, rows);
        int index = row * StorageTerminalContainer.STORAGE_COLUMNS + col;
        if (row >= 0 && row < rows
            && col >= 0
            && col < StorageTerminalContainer.STORAGE_COLUMNS
            && index < page.size()) {
            TerminalEntry entry = page.get(index);
            List<String> lines = tooltipLines(entry);
            lines.add(I18n.format(LANG_AMOUNT, entry.count()));
            drawHoveringText(lines, mouseX, mouseY, fontRendererObj);
        }
    }

    /**
     * @param entry the entry to describe
     * @return the item's tooltip lines, computed lazily and cached per entry
     *         key
     */
    private List<String> tooltipLines(TerminalEntry entry) {
        List<String> lines = tooltipCache.get(entry.key());
        if (lines == null) {
            lines = new ArrayList<String>(
                entry.prototype()
                    .getTooltip(mc.thePlayer, false));
            tooltipCache.put(entry.key(), lines);
        }
        return new ArrayList<String>(lines);
    }

    /** @return true if the mouse is inside the scrollbar region. */
    private boolean inScrollbar(int mouseX, int mouseY) {
        int rows = visibleRows();
        int x = guiLeft + SCROLLBAR_X;
        int y = guiTop + StorageTerminalContainer.STORAGE_Y;
        return mouseX >= x && mouseX < x + SCROLLBAR_WIDTH && mouseY >= y && mouseY < y + rows * CELL;
    }

    /** @return true if the mouse is inside the storage grid. */
    private boolean inGrid(int mouseX, int mouseY) {
        int rows = visibleRows();
        int x = guiLeft + StorageTerminalContainer.STORAGE_X;
        int y = guiTop + StorageTerminalContainer.STORAGE_Y;
        return mouseX >= x && mouseX < x + StorageTerminalContainer.STORAGE_COLUMNS * CELL
            && mouseY >= y
            && mouseY < y + rows * CELL;
    }

    /** @return the id of this screen's container window. */
    public int getContainerWindowId() {
        return container.windowId;
    }

    /**
     * Maps a drag position in the scrollbar to a scroll offset.
     *
     * @param mouseY the mouse y position
     */
    private void dragScrollbar(int mouseY) {
        int rows = visibleRows();
        ScrollbarModel bar = new ScrollbarModel(
            guiLeft + SCROLLBAR_X,
            guiTop + StorageTerminalContainer.STORAGE_Y,
            SCROLLBAR_WIDTH,
            rows * CELL,
            view.rowCount(StorageTerminalContainer.STORAGE_COLUMNS),
            rows);
        double fraction = bar.dragToScroll(mouseY);
        int maxRows = ScrollMath.maxScrollRows(view.size(), StorageTerminalContainer.STORAGE_COLUMNS, rows);
        scrollOffsetRows = (int) Math.round(fraction * maxRows);
    }

    /**
     * @return the sort button's label for the current sort mode.
     */
    private String sortLang() {
        switch (sortType) {
            case AMOUNT:
                return I18n.format(LANG_SORT_AMOUNT);
            case NAME:
                return I18n.format(LANG_SORT_NAME);
            case MOD:
                return I18n.format(LANG_SORT_MOD);
            case SPACE_EFFICIENCY:
                return I18n.format(LANG_SORT_SPACE);
            default:
                return I18n.format(LANG_SORT_ID);
        }
    }

    /** @return the ghost-mode button's label. */
    private String ghostLang() {
        return I18n.format(ghostOn ? LANG_GHOST_ON : LANG_GHOST_OFF);
    }

    /** @return the control-mode button's label. */
    private String controlLang() {
        switch (controlMode) {
            case TerminalModes.CONTROL_AE:
                return I18n.format(LANG_CONTROL_AE);
            case TerminalModes.CONTROL_RS:
                return I18n.format(LANG_CONTROL_RS);
            default:
                return I18n.format(LANG_CONTROL_DEF);
        }
    }

    /** @return the tall-mode button's label. */
    private String tallLang() {
        return I18n.format(tall ? LANG_TALL_ON : LANG_TALL_OFF);
    }

    /**
     * Adapts {@link TerminalEntry}s to the plain data the {@link TerminalView}
     * sorts and searches by. Tooltips are computed lazily (they are client
     * rendering data) and cached per entry key.
     */
    private final class EntrySource implements TerminalView.Source<TerminalEntry> {

        /**
         * @param entry the entry
         * @return the sort key for the entry's prototype and count
         */
        @Override
        public SortKey sortKey(TerminalEntry entry) {
            return StackKeys.sortKeyOf(entry.prototype(), entry.count());
        }

        /**
         * @param entry the entry
         * @return a search item built from the entry's prototype stack
         */
        @Override
        public SearchQuery.Item searchItem(TerminalEntry entry) {
            return new EntrySearchItem(entry);
        }

        /**
         * @param entry the entry
         * @return the entry's key
         */
        @Override
        public String keyOf(TerminalEntry entry) {
            return entry.key();
        }
    }

    /**
     * Adapts one entry's prototype stack to {@link SearchQuery.Item}. The
     * tooltip lines are computed lazily and cached in the screen's
     * {@link #tooltipCache}.
     */
    private final class EntrySearchItem implements SearchQuery.Item {

        /** The entry this item describes. */
        private final TerminalEntry entry;

        /** The lazily computed tooltip lines. */
        private List<String> tooltips;

        /**
         * @param entry the entry
         */
        EntrySearchItem(TerminalEntry entry) {
            this.entry = entry;
        }

        /**
         * @return the prototype stack's display name
         */
        @Override
        public String displayName() {
            return entry.prototype()
                .getDisplayName();
        }

        /**
         * @return the prototype stack's mod id
         */
        @Override
        public String modId() {
            return StackKeys.modId(entry.prototype());
        }

        /**
         * @return the prototype stack's tooltip lines, computed lazily and
         *         cached per entry key
         */
        @Override
        public List<String> tooltipLines() {
            if (tooltips == null) {
                tooltips = entry.prototype()
                    .getTooltip(mc.thePlayer, false);
            }
            return tooltips;
        }

        /**
         * @return the prototype stack's ore-dictionary names
         */
        @Override
        public List<String> oreNames() {
            ItemStack stack = entry.prototype();
            List<String> names = new ArrayList<String>();
            int[] ids = OreDictionary.getOreIDs(stack);
            for (int id : ids) {
                names.add(OreDictionary.getOreName(id));
            }
            return names;
        }

        /**
         * @return the prototype stack's NBT as text, or null when it has none
         */
        @Override
        public String nbtText() {
            NBTTagCompound tag = entry.prototype()
                .getTagCompound();
            return tag == null ? null : tag.toString();
        }
    }
}
