package com.tuxprogrammer.simplestorage.terminal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Pins the terminal number-abbreviation rules (the original spec's
 * {@code NumberFormatUtil}) at the thresholds and boundaries: 0, 999, 1000,
 * 9999, 10000, 999999, 1000000, the spec's largest cases, and negative
 * counts; plus a loop over the 10^k boundaries.
 */
public class NumberFormatTest {

    /**
     * Counts up to 999 (4 characters) are shown as-is, including zero and
     * negative counts (treated as 0).
     */
    @Test
    public void shortCountsAreUnchanged() {
        assertEquals("0", NumberFormat.formatNumber(0L));
        assertEquals("7", NumberFormat.formatNumber(7L));
        assertEquals("999", NumberFormat.formatNumber(999L));
        assertEquals("0", NumberFormat.formatNumber(-5L));
    }

    /**
     * 1000 is 4 characters and stays plain; 1234 (5 characters) enters the
     * abbreviated form with one decimal place (RoundingMode.DOWN).
     */
    @Test
    public void oneThousandIsPlainButFiveDigitsAreAbbreviated() {
        assertEquals("1000", NumberFormat.formatNumber(1000L));
        assertEquals("1234", NumberFormat.formatNumber(1234L));
        assertEquals("9999", NumberFormat.formatNumber(9999L));
    }

    /**
     * 9999 is 4 characters and stays plain; 10000 (5 characters) is
     * abbreviated and the decimal form does not fit, so the plain form wins;
     * 12345 (5 characters) is the same.
     */
    @Test
    public void fourAndFiveDigitBoundary() {
        assertEquals("10K", NumberFormat.formatNumber(10000L));
        assertEquals("12K", NumberFormat.formatNumber(12345L));
        assertEquals("15K", NumberFormat.formatNumber(15000L));
        assertEquals("99K", NumberFormat.formatNumber(99999L));
    }

    /**
     * Six-digit boundary: 999999 (6 characters) abbreviates to the 4-character
     * plain K form; 1000000 becomes the M postfix, and 1234567 is the spec's
     * example.
     */
    @Test
    public void millionBoundary() {
        assertEquals("100K", NumberFormat.formatNumber(100000L));
        assertEquals("999K", NumberFormat.formatNumber(999999L));
        assertEquals("1M", NumberFormat.formatNumber(1000000L));
        assertEquals("1.2M", NumberFormat.formatNumber(1234567L));
        assertEquals("12M", NumberFormat.formatNumber(12345678L));
    }

    /**
     * The spec's largest cases: the G postfix, and counts at the E postfix
     * where the plain form does not fit, so the 4-character decimal form is
     * used.
     */
    @Test
    public void largestCases() {
        assertEquals("9.8G", NumberFormat.formatNumber(9876543210L));
        assertEquals("1T", NumberFormat.formatNumber(1000000000000L));
        assertEquals("1.5P", NumberFormat.formatNumber(1500000000000000L));
        assertEquals("9.2E", NumberFormat.formatNumber(Long.MAX_VALUE));
    }

    /**
     * The loop: formatNumber never throws for 0, 1, 10^k-1 and 10^k for
     * k=0..18 plus Long.MAX_VALUE, and always returns at most 4 characters
     * for n >= 1000.
     */
    @Test
    public void loopOverPowersOfTenAndMaxValue() {
        // 0
        assertShort(0L, NumberFormat.formatNumber(0L));
        // 1
        assertShort(1L, NumberFormat.formatNumber(1L));
        // 10^k - 1 and 10^k for k = 0..18
        long power = 1L;
        for (int k = 0; k <= 18; k++) {
            assertShort(power - 1, NumberFormat.formatNumber(power - 1));
            assertShort(power, NumberFormat.formatNumber(power));
            power *= 10L;
        }
        // Long.MAX_VALUE
        assertShort(Long.MAX_VALUE, NumberFormat.formatNumber(Long.MAX_VALUE));
    }

    /**
     * Never throws (an exception fails the test) and, for counts above 999,
     * the result is at most 4 characters.
     */
    private static void assertShort(long n, String formatted) {
        if (n >= 1000) {
            assertTrue(formatted.length() <= 4, "too long: " + formatted);
        }
    }
}
