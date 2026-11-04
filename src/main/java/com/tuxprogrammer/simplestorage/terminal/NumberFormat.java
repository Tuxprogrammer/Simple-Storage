package com.tuxprogrammer.simplestorage.terminal;
// Ported from Tom's Simple Storage (MIT):
// https://github.com/tom5454/Toms-Storage/blob/95bb61d/NeoForge/src/platform-shared/java/com/tom/storagemod/util/NumberFormatUtil.java

/**
 * Abbreviates long item counts into the terminal's 4-character display form
 * (the original spec's {@code NumberFormatUtil}).
 *
 * <p>
 * Rules, exactly as the spec: if the decimal string is 4 characters or
 * shorter, it is returned as-is. Otherwise the count is repeatedly divided by
 * 1000 (base 1000, at most 6 divisions) while the printed size exceeds 4,
 * remembering the value before the last division ({@code last}) and the
 * exponent {@code e} (postfix = {@code "KMGTPE".charAt(e-1)}). Two candidates
 * are then compared: the form with at most one decimal place
 * ({@code last/1000.0} formatted as {@code .#;0.#}, RoundingMode.DOWN, "."
 * separator, trailing ".0" dropped) plus the postfix, and the plain form
 * ({@code Long.toString(last/1000)}, the integer part) plus the postfix.
 * The decimal form is returned if it fits in 4 characters, otherwise the
 * plain form. Examples: {@code 1234 -> "1.2K"}, {@code 9999 -> "9999"},
 * {@code 15000 -> "15K"}, {@code 1234567 -> "1.2M"}.
 * </p>
 *
 * <p>
 * Unlike the upstream (which asserts {@code count >= 0}), this port never
 * throws: negative counts are treated as zero (the result is {@code "0"}).
 * </p>
 *
 * <p>
 * Side: both (plain Java).
 * </p>
 */
public final class NumberFormat {

    /** Division base for the abbreviation (NumberFormatUtil.java:9). */
    private static final int BASE = 1000;

    /** Postfixes for the 1000-exponents 1..6 (NumberFormatUtil.java:10). */
    private static final String POSTFIXES = "KMGTPE";

    /** Maximum display width in characters (NumberFormatUtil.java:23). */
    private static final int DISPLAY_WIDTH = 4;

    private NumberFormat() {}

    /**
     * Formats a count into the terminal's abbreviated display form.
     *
     * @param count the item count ({@code count >= 0}; a negative count is
     *              treated as {@code 0}; never throws)
     * @return the formatted string, at most 4 characters for any count
     *         above 999
     */
    public static String formatNumber(long count) {
        long n = count < 0 ? 0L : count;

        String asString = Long.toString(n);
        if (asString.length() <= DISPLAY_WIDTH) {
            return asString;
        }

        // Smallest exp in 1..6 with q = n / 1000^exp < 1000 (always exists:
        // for n <= Long.MAX_VALUE, q < 1000 by the time exp reaches 6, so the
        // postfix never runs past POSTFIXES).
        long divisor = BASE;
        int exp = 1;
        for (long q = n / BASE; q >= BASE; q /= BASE) {
            exp++;
            divisor *= BASE;
        }
        long q = n / divisor;

        // One-decimal form, plain long arithmetic (no double): the value in
        // tenths of a unit at scale 10^(3*exp-1).
        long ten = 10L;
        for (int i = 3 * exp - 1; i > 1; i--) {
            ten *= 10L;
        }
        long tenths = n / ten;
        String oneDecimal = tenths % 10 == 0 ? Long.toString(tenths / 10) : (tenths / 10) + "." + (tenths % 10);

        char postfix = POSTFIXES.charAt(exp - 1);
        String candidate = oneDecimal + postfix;
        if (candidate.length() <= DISPLAY_WIDTH) {
            return candidate;
        }
        return Long.toString(q) + postfix;
    }
}
