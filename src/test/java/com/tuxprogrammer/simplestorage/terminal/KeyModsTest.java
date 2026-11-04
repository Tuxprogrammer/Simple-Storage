package com.tuxprogrammer.simplestorage.terminal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Regression tests for {@link KeyMods}.
 *
 * Side: both (plain Java).
 */
public class KeyModsTest {

    /**
     * The exposed key codes must match the LWJGL 2 keyboard constants.
     */
    @Test
    public void keyCodesMatchLwjgl2Constants() {
        assertEquals(29, KeyMods.LEFT_CTRL);
        assertEquals(157, KeyMods.RIGHT_CTRL);
        assertEquals(42, KeyMods.LEFT_SHIFT);
        assertEquals(54, KeyMods.RIGHT_SHIFT);
        assertEquals(56, KeyMods.LEFT_ALT);
        assertEquals(184, KeyMods.RIGHT_ALT);
    }

    /**
     * Right-side modifiers must count the same as left-side ones; upstream
     * only checked the left keys (CORE-10).
     */
    @Test
    public void doesNotReproduceCORE10_rightModifiersCount() {
        assertTrue(KeyMods.ctrlDown(new FakeKeyState(KeyMods.RIGHT_CTRL)));
        assertTrue(KeyMods.shiftDown(new FakeKeyState(KeyMods.RIGHT_SHIFT)));
        assertTrue(KeyMods.altDown(new FakeKeyState(KeyMods.RIGHT_ALT)));
        assertTrue(KeyMods.ctrlDown(new FakeKeyState(KeyMods.LEFT_CTRL, KeyMods.RIGHT_CTRL)));
        assertTrue(KeyMods.shiftDown(new FakeKeyState(KeyMods.LEFT_SHIFT, KeyMods.RIGHT_SHIFT)));
        assertTrue(KeyMods.altDown(new FakeKeyState(KeyMods.LEFT_ALT, KeyMods.RIGHT_ALT)));
    }

    /**
     * Each modifier reports only its own keys, not the others'.
     */
    @Test
    public void modifiersDoNotCrossFire() {
        KeyMods.KeyState onlyCtrl = new FakeKeyState(KeyMods.LEFT_CTRL);
        KeyMods.KeyState onlyShift = new FakeKeyState(KeyMods.LEFT_SHIFT);
        KeyMods.KeyState onlyAlt = new FakeKeyState(KeyMods.LEFT_ALT);
        assertTrue(KeyMods.ctrlDown(onlyCtrl));
        assertFalse(KeyMods.shiftDown(onlyCtrl));
        assertFalse(KeyMods.altDown(onlyCtrl));
        assertFalse(KeyMods.ctrlDown(onlyShift));
        assertTrue(KeyMods.shiftDown(onlyShift));
        assertFalse(KeyMods.altDown(onlyShift));
        assertFalse(KeyMods.ctrlDown(onlyAlt));
        assertFalse(KeyMods.shiftDown(onlyAlt));
        assertTrue(KeyMods.altDown(onlyAlt));
    }

    /**
     * No keys held means no modifier is down.
     */
    @Test
    public void noKeysHeldReportsNothingDown() {
        KeyMods.KeyState none = new FakeKeyState();
        assertFalse(KeyMods.ctrlDown(none));
        assertFalse(KeyMods.shiftDown(none));
        assertFalse(KeyMods.altDown(none));
    }

    /**
     * A map-backed fake key state for driving the modifier checks.
     */
    private static final class FakeKeyState implements KeyMods.KeyState {

        private final List<Integer> down;

        FakeKeyState(int... codes) {
            List<Integer> list = new ArrayList<Integer>();
            for (int code : codes) {
                list.add(code);
            }
            this.down = list;
        }

        @Override
        public boolean isDown(int keyCode) {
            return down.contains(keyCode);
        }
    }
}
