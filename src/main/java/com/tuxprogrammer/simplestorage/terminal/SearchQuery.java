package com.tuxprogrammer.simplestorage.terminal;
// Ported from Tom's Simple Storage (MIT):
// https://github.com/tom5454/Toms-Storage/blob/95bb61d/NeoForge/src/platform-shared/java/com/tom/storagemod/screen/AbstractStorageTerminalScreen.java

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Pure-Java port of the storage terminal's search-string syntax.
 * <p>
 * A search string is an OR of parts split on '|'; each part is an AND of
 * space-separated terms (empty terms are ignored). Term prefixes: '@'
 * matches when the item's mod id contains the remainder (case-insensitive),
 * '#' matches when any ore-dictionary name contains the remainder
 * (case-insensitive), '$' is a case-insensitive regular expression matched
 * against the item's NBT text ({@link Item#nbtText()}, where a null value
 * means the item has no NBT and a '$' term never matches it). A bare term is
 * a case-insensitive regular expression searched (Matcher.find) in the
 * display name or any tooltip line; if the pattern is syntactically invalid
 * it falls back to the quoted literal (Pattern.quote), and a term whose quoted
 * form also fails is skipped (treated as matching everything). An item
 * matches the query when any OR part matches, and a part matches when all of
 * its terms match. Matching is case-insensitive throughout.
 * Side: both (plain Java).
 */
public final class SearchQuery {

    /**
     * Maximum length of the search text; longer input is truncated.
     */
    public static final int MAX_LENGTH = 256;

    /**
     * Parses a search string.
     * <p>
     * Never throws: any input is accepted. A null or blank string gives a
     * query that matches everything, and input longer than 256 characters is
     * truncated to the first 256.
     *
     * @param text the search text (may be null)
     * @return the parsed query, never null
     */
    public static SearchQuery parse(String text) {
        SearchQuery q = new SearchQuery();
        if (text == null) {
            q.text = "";
            return q; // matches everything
        }
        if (text.length() > MAX_LENGTH) {
            text = text.substring(0, MAX_LENGTH);
        }
        q.text = text;
        String trimmed = text.trim();
        if (trimmed.isEmpty()) {
            return q; // matches everything
        }
        List<Part> parts = new ArrayList<>();
        for (String partText : trimmed.split("\\|")) {
            Part part = new Part();
            for (String term : partText.trim()
                .split(" ")) {
                if (term.isEmpty()) {
                    continue;
                }
                part.terms.add(new Term(term));
            }
            if (!part.terms.isEmpty()) {
                parts.add(part);
            }
        }
        q.parts = parts;
        return q;
    }

    private SearchQuery() {
        this.parts = new ArrayList<Part>();
    }

    private List<Part> parts;
    private String text = "";

    /**
     * The (null -> empty, truncated to {@link #MAX_LENGTH} chars) query text
     * this query was parsed from.
     *
     * @return the query text, never null
     */
    public String text() {
        return text;
    }

    /**
     * Checks whether the given item matches this query.
     *
     * @param item the item to test
     * @return true if the item matches, false otherwise
     */
    public boolean matches(Item item) {
        if (parts.isEmpty()) {
            return true;
        }
        for (Part part : parts) {
            if (part.matches(item)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks whether this query matches everything (was parsed from null,
     * blank, or a string of only delimiters).
     *
     * @return true if the query matches everything
     */
    public boolean isEmpty() {
        return parts.isEmpty();
    }

    /**
     * Plain-data item to match against; no Minecraft types.
     */
    public interface Item {

        /**
         * @return the item's display name (never null)
         */
        String displayName();

        /**
         * @return the item's mod id, "mod:thing" (never null)
         */
        String modId();

        /**
         * @return the tooltip lines (never null, may be empty)
         */
        List<String> tooltipLines();

        /**
         * @return the item's ore-dictionary names (never null, may be empty)
         */
        List<String> oreNames();

        /**
         * @return the item's NBT as text, or null if the item has no NBT; a
         *         '$' search term never matches an item whose NBT text is null
         */
        String nbtText();
    }

    private static final class Part {

        private final List<Term> terms = new ArrayList<>();

        private boolean matches(Item item) {
            for (Term term : terms) {
                if (!term.matches(item)) {
                    return false;
                }
            }
            return true;
        }
    }

    private static final class Term {

        private final char prefix;
        private final String body;

        private Term(String text) {
            if (text.length() >= 2 && isPrefix(text.charAt(0))) {
                prefix = text.charAt(0);
                body = text.substring(1);
            } else {
                prefix = 0;
                body = text;
            }
        }

        private static boolean isPrefix(char c) {
            return c == '@' || c == '#' || c == '$';
        }

        private boolean matches(Item item) {
            switch (prefix) {
                case '@':
                    return modMatches(item);
                case '#':
                    return oreNameMatches(item);
                case '$':
                    // A '$' term is a regex against the item's NBT text.
                    // An item with no NBT (null) never matches, and a '$'
                    // term that fails to compile matches nothing (its
                    // pattern never matched and there is no quoted form to
                    // fall back to).
                    if (item.nbtText() == null) {
                        return false;
                    }
                    try {
                        Pattern pattern = Pattern.compile(body, Pattern.CASE_INSENSITIVE);
                        return pattern.matcher(item.nbtText())
                            .find();
                    } catch (PatternSyntaxException ex) {
                        return false;
                    }
                default:
                    // Bare term: regex searched in the display name or any
                    // tooltip line; if the pattern is syntactically invalid,
                    // fall back to the quoted literal (literal search); if
                    // the quoted form also fails, skip the term (match all).
                    try {
                        return nameOrTooltipMatches(item, body);
                    } catch (PatternSyntaxException ex) {
                        try {
                            return nameOrTooltipMatches(item, Pattern.quote(body));
                        } catch (PatternSyntaxException ex2) {
                            return true; // even the quoted form failed: skip
                        }
                    }
            }
        }

        private boolean modMatches(Item item) {
            if (body.isEmpty()) {
                return false;
            }
            return item.modId() != null && item.modId()
                .toLowerCase()
                .contains(body.toLowerCase());
        }

        private boolean oreNameMatches(Item item) {
            if (body.isEmpty()) {
                return false;
            }
            String needle = body.toLowerCase();
            for (String name : item.oreNames()) {
                if (name != null && name.toLowerCase()
                    .contains(needle)) {
                    return true;
                }
            }
            return false;
        }

        private boolean nameOrTooltipMatches(Item item, String pattern) {
            Pattern compiled = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
            if (item.displayName() != null && compiled.matcher(item.displayName())
                .find()) {
                return true;
            }
            for (String line : item.tooltipLines()) {
                if (line != null && compiled.matcher(line)
                    .find()) {
                    return true;
                }
            }
            return false;
        }
    }
}
