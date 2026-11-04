package com.tuxprogrammer.simplestorage.terminal;

import java.util.List;

import com.tuxprogrammer.simplestorage.blockentity.TerminalEntry;

/**
 * The contract the 05-integration shared recipe viewer uses to talk to a
 * terminal that can auto-fill its crafting grid.
 *
 * <p>
 * Implementations are the terminal's server-side containers. The viewer asks
 * the terminal to propagate a search string, to report a result to the
 * player, to hand over the items the network currently holds, and to learn
 * whether smart search is on so it can decide how to match the search.
 *
 * <p>
 * Side: both.
 */
public interface IAutoFillTerminal {

    /**
     * Asks the terminal to show a search text (the viewer pushes the current
     * search string into the terminal).
     *
     * @param text the search text to show
     */
    void updateSearch(String text);

    /**
     * Reports a result to the player as a lang key: the terminal sends the
     * translation to the player when the terminal has a player to talk to.
     *
     * @param langKey the lang key of the message
     * @param args    the translation arguments
     */
    void sendMessage(String langKey, Object... args);

    /**
     * @return what the network currently holds, as of the last sync: one
     *         entry per distinct stack type held across the terminal's
     *         reachable inventories
     */
    List<TerminalEntry> getStoredItems();

    /**
     * Whether smart search is on: searchType bit 8 clear.
     *
     * @return true if smart search is on
     */
    boolean isSmartSearch();
}
