package com.tuxprogrammer.simplestorage.terminal;
// Ported from Tom's Simple Storage (MIT):
// https://github.com/tom5454/Toms-Storage/blob/95bb61d/NeoForge/src/platform-shared/java/com/tom/storagemod/screen/AbstractStorageTerminalScreen.java
// Avoids: TERM-02

/**
 * Static helpers and constants over the three ints the storage terminal
 * persists in block entity NBT (and shuttles through the menu data slots /
 * the client "c" packet): the {@code modes} word, the {@code sorting} word,
 * and the {@code searchType} word.
 * <p>
 * {@code modes} layout: bits 0-3 are the control mode (the value is read back
 * as {@code (modes & 0xF) % 3}, with 0 = AE, 1 = RS, 2 = DEF), and bit 0x10
 * is tall mode (auto terminal size). All other bits are reserved and are
 * preserved by the setters so that a save written by a newer version survives
 * a round trip through this class.
 * <p>
 * {@code sorting} layout: bits 0-7 are the sort type ordinal (the screen
 * decodes it with a fallback when it does not recognise the value), bit 0x100
 * is the reverse (descending) flag, and bit 0x200 is the ghost-off flag
 * (ghost mode is ON unless the bit is set). All other bits are reserved and
 * preserved.
 * <p>
 * {@code searchType} layout: flag bits 1 = focus the search field on open,
 * 2 = keep (load/send the server's stored search), 4 = sync with the
 * integrated recipe viewer, 8 = smart search OFF (smart search is on when the
 * bit is clear).
 * <p>
 * Defaults: {@code sorting} = 0 (ordinal 0 = AMOUNT, not reversed, ghost
 * mode ON), {@code modes} = 0 (AE, not tall), {@code searchType} = 0.
 * <p>
 * All methods are static, total, and never throw; no Minecraft types appear
 * in any signature.
 *
 * Side: both (plain Java).
 */
public final class TerminalModes {

    /** Mask of the sort-ordinal field inside the {@code sorting} word (bits 0-7). */
    public static final int SORT_ORDINAL_MASK = 0xFF;

    /** Reverse (descending) flag inside the {@code sorting} word (0x100). */
    public static final int SORT_REVERSED = 0x100;

    /** Ghost-off flag inside the {@code sorting} word (0x200); ghost mode is ON when the bit is clear. */
    public static final int SORT_GHOST_OFF = 0x200;

    /** Mask of the control-mode field inside the {@code modes} word (bits 0-3). */
    public static final int MODE_CONTROL_MASK = 0xF;

    /** Tall-mode flag inside the {@code modes} word (0x10). */
    public static final int MODE_TALL = 0x10;

    /** {@code searchType} flag: focus the search field on open. */
    public static final int SEARCH_FOCUS = 1;

    /** {@code searchType} flag: keep (load/send the server's stored search). */
    public static final int SEARCH_KEEP = 2;

    /** {@code searchType} flag: sync with the integrated recipe viewer. */
    public static final int SEARCH_SYNC = 4;

    /** {@code searchType} flag: smart search OFF (smart search is on when the bit is clear). */
    public static final int SEARCH_SMART_OFF = 8;

    /** Control mode 0: AE-like controls. */
    public static final int CONTROL_AE = 0;

    /** Control mode 1: Refined Storage-like controls. */
    public static final int CONTROL_RS = 1;

    /** Control mode 2: vanilla-like (default) controls. */
    public static final int CONTROL_DEF = 2;

    /** Default {@code sorting} word: ordinal 0 (AMOUNT), not reversed, ghost mode ON. */
    public static final int DEFAULT_SORTING = 0;

    /** Default {@code modes} word: AE control mode, not tall. */
    public static final int DEFAULT_MODES = 0;

    /** Default {@code searchType} word: no flags set. */
    public static final int DEFAULT_SEARCH_TYPE = 0;

    /**
     * Private constructor: this is a static helper class.
     */
    private TerminalModes() {}

    // ---------------------------------------------------------------------
    // Sorting word
    // ---------------------------------------------------------------------

    /**
     * Reads the sort-type ordinal from the {@code sorting} word. The value is
     * returned raw: an unrecognised ordinal is decoded elsewhere with a
     * fallback to the default mode, so no bounds checking happens here.
     *
     * @param sorting the {@code sorting} int
     * @return the raw sort-type ordinal, 0-255 as stored
     */
    public static int getSortOrdinal(int sorting) {
        return sorting & SORT_ORDINAL_MASK;
    }

    /**
     * Whether the {@code sorting} word requests reverse (descending) order.
     *
     * @param sorting the {@code sorting} int
     * @return true if the reverse flag (0x100) is set
     */
    public static boolean isReversed(int sorting) {
        return (sorting & SORT_REVERSED) != 0;
    }

    /**
     * Whether ghost mode is on in the {@code sorting} word. Ghost mode is ON
     * unless the ghost-off bit (0x200) is set.
     *
     * @param sorting the {@code sorting} int
     * @return true if ghost mode is ON (the 0x200 bit is clear)
     */
    public static boolean isGhostOn(int sorting) {
        return (sorting & SORT_GHOST_OFF) == 0;
    }

    /**
     * Sets the sort-type ordinal in the {@code sorting} word, leaving every
     * other bit (reverse, ghost-off, reserved/unknown high bits) untouched.
     * Values outside 0-255 are masked to the field width rather than throwing.
     *
     * @param sorting the {@code sorting} int
     * @param ordinal the new sort-type ordinal (masked to 8 bits)
     * @return the updated {@code sorting} word
     */
    public static int withSortOrdinal(int sorting, int ordinal) {
        return (sorting & ~SORT_ORDINAL_MASK) | (ordinal & SORT_ORDINAL_MASK);
    }

    /**
     * Sets or clears the reverse flag in the {@code sorting} word, leaving
     * every other bit (sort ordinal, ghost-off, reserved/unknown high bits)
     * untouched.
     *
     * @param sorting  the {@code sorting} int
     * @param reversed true for descending order, false for ascending
     * @return the updated {@code sorting} word
     */
    public static int withReversed(int sorting, boolean reversed) {
        return reversed ? sorting | SORT_REVERSED : sorting & ~SORT_REVERSED;
    }

    /**
     * Turns ghost mode on or off in the {@code sorting} word, leaving every
     * other bit (sort ordinal, reverse, reserved/unknown high bits)
     * untouched.
     *
     * @param sorting the {@code sorting} int
     * @param ghostOn true to turn ghost mode on (clear 0x200), false to turn it off
     * @return the updated {@code sorting} word
     */
    public static int withGhostOn(int sorting, boolean ghostOn) {
        return ghostOn ? sorting & ~SORT_GHOST_OFF : sorting | SORT_GHOST_OFF;
    }

    // ---------------------------------------------------------------------
    // Modes word
    // ---------------------------------------------------------------------

    /**
     * Decodes the control mode from the {@code modes} word.
     *
     * @param modes the {@code modes} int
     * @return the control mode, {@code (modes & 0xF) % 3}: 0 = AE, 1 = RS, 2 = DEF
     */
    public static int getControlMode(int modes) {
        return (modes & MODE_CONTROL_MASK) % 3;
    }

    /**
     * Whether the {@code modes} word requests tall mode (auto terminal size).
     *
     * @param modes the {@code modes} int
     * @return true if the tall-mode flag (0x10) is set
     */
    public static boolean isTall(int modes) {
        return (modes & MODE_TALL) != 0;
    }

    /**
     * Sets the control mode in the {@code modes} word, leaving every other
     * bit (tall mode, reserved/unknown high bits) untouched. Values outside
     * 0-15 are masked to the field width rather than throwing.
     *
     * @param modes the {@code modes} int
     * @param mode  the new control-mode value (masked to 4 bits)
     * @return the updated {@code modes} word
     */
    public static int withControlMode(int modes, int mode) {
        return (modes & ~MODE_CONTROL_MASK) | (mode & MODE_CONTROL_MASK);
    }

    /**
     * Sets or clears the tall-mode bit in the {@code modes} word, leaving
     * every other bit (control mode, reserved/unknown high bits) untouched.
     *
     * @param modes the {@code modes} int
     * @param tall  true to enable tall mode, false to disable it
     * @return the updated {@code modes} word
     */
    public static int withTall(int modes, boolean tall) {
        return tall ? modes | MODE_TALL : modes & ~MODE_TALL;
    }

    // ---------------------------------------------------------------------
    // Search-type word
    // ---------------------------------------------------------------------

    /**
     * Whether smart search is on in the {@code searchType} word. Smart search
     * is on unless the flag bit (8) is set.
     *
     * @param searchType the {@code searchType} int
     * @return true if smart search is on (the 8 bit is clear)
     */
    public static boolean isSmartSearch(int searchType) {
        return (searchType & SEARCH_SMART_OFF) == 0;
    }

    /**
     * Tests one flag bit of the {@code searchType} word.
     *
     * @param searchType the {@code searchType} int
     * @param flag       the flag bit to test (1 = focus, 2 = keep, 4 = sync, 8 = smart search OFF)
     * @return true if the bit is set
     */
    public static boolean hasSearchFlag(int searchType, int flag) {
        return (searchType & flag) != 0;
    }

    /**
     * Sets or clears one flag bit of the {@code searchType} word, leaving
     * every other bit untouched.
     *
     * @param searchType the {@code searchType} int
     * @param flag       the flag bit to change (1 = focus, 2 = keep, 4 = sync, 8 = smart search OFF)
     * @param on         true to set the bit, false to clear it
     * @return the updated {@code searchType} int
     */
    public static int withSearchFlag(int searchType, int flag, boolean on) {
        return on ? searchType | flag : searchType & ~flag;
    }
}
