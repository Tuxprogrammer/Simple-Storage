package com.tuxprogrammer.simplestorage.terminal;
// Ported from Tom's Simple Storage (MIT):
// https://github.com/tom5454/Toms-Storage/blob/95bb61d/NeoForge/src/platform-shared/java/com/tom/storagemod/util/KeyUtil.java
// Avoids: CORE-10

/**
 * Modifier-key state checks over LWJGL 2 key codes.
 *
 * <p>
 * The key codes are LWJGL 2 {@code org.lwjgl.input.Keyboard} constants,
 * kept as plain ints so the helpers stay free of Minecraft and LWJGL types.
 *
 * Side: both (plain Java).
 */
public final class KeyMods {

    /** LWJGL 2 key code for the left control key. */
    public static final int LEFT_CTRL = 29;

    /** LWJGL 2 key code for the right control key. */
    public static final int RIGHT_CTRL = 157;

    /** LWJGL 2 key code for the left shift key. */
    public static final int LEFT_SHIFT = 42;

    /** LWJGL 2 key code for the right shift key. */
    public static final int RIGHT_SHIFT = 54;

    /** LWJGL 2 key code for the left alt key. */
    public static final int LEFT_ALT = 56;

    /** LWJGL 2 key code for the right alt key. */
    public static final int RIGHT_ALT = 184;

    /**
     * Abstraction over "is this key currently pressed" queries.
     *
     * <p>
     * Implemented by the client side with the LWJGL keyboard state;
     * tests use a simple map-based fake.
     */
    public interface KeyState {

        /**
         * Asks whether the given key is currently pressed.
         *
         * @param keyCode an LWJGL 2 key code
         * @return true if the key is down, false otherwise
         */
        boolean isDown(int keyCode);
    }

    /**
     * Checks whether any control key is held (left OR right).
     *
     * <p>
     * Covers both sides of the keyboard (CORE-10: upstream checked only the
     * left control key, so a right-Ctrl held by a right-handed user did nothing).
     *
     * @param k the key state to query
     * @return true if either control key is down
     */
    public static boolean ctrlDown(KeyState k) {
        return k.isDown(LEFT_CTRL) || k.isDown(RIGHT_CTRL);
    }

    /**
     * Checks whether any shift key is held (left OR right).
     *
     * @param k the key state to query
     * @return true if either shift key is down
     */
    public static boolean shiftDown(KeyState k) {
        return k.isDown(LEFT_SHIFT) || k.isDown(RIGHT_SHIFT);
    }

    /**
     * Checks whether any alt key is held (left OR right).
     *
     * @param k the key state to query
     * @return true if either alt key is down
     */
    public static boolean altDown(KeyState k) {
        return k.isDown(LEFT_ALT) || k.isDown(RIGHT_ALT);
    }

    private KeyMods() {}
}
