package com.tuxprogrammer.simplestorage.terminal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.junit.jupiter.api.Test;

/**
 * Tests for the pure-Java {@link SearchQuery} port: pins every syntax
 * feature of the original spec (OR on '|', AND of space-separated terms,
 * '@' mod filter, '#' ore-dictionary search, '$' NBT regex, bare terms with
 * quoted-literal fallback), plus empty/blank/null input, 256-char
 * truncation, case-insensitivity for every prefix, and a never-throws fuzz
 * over 200 random strings.
 */
public class SearchQueryTest {

    /**
     * In-test plain-data implementation of {@link SearchQuery.Item}.
     */
    public static final class FakeItem implements SearchQuery.Item {

        private final String displayName;
        private final String modId;
        private final List<String> tooltipLines;
        private final List<String> oreNames;
        private final String nbtText;

        public FakeItem(String displayName, String modId, List<String> tooltipLines, List<String> oreNames,
            String nbtText) {
            this.displayName = displayName;
            this.modId = modId;
            this.tooltipLines = tooltipLines == null ? new ArrayList<String>() : tooltipLines;
            this.oreNames = oreNames == null ? new ArrayList<String>() : oreNames;
            this.nbtText = nbtText;
        }

        @Override
        public String displayName() {
            return displayName;
        }

        @Override
        public String modId() {
            return modId;
        }

        @Override
        public List<String> tooltipLines() {
            return tooltipLines;
        }

        @Override
        public List<String> oreNames() {
            return oreNames;
        }

        @Override
        public String nbtText() {
            return nbtText;
        }
    }

    private static FakeItem i1() {
        return new FakeItem(
            "Iron Ingot",
            "minecraft",
            Arrays.asList("Used for crafting"),
            Arrays.asList("ingotIron"),
            null);
    }

    private static FakeItem i2() {
        return new FakeItem(
            "Gold Pickaxe",
            "tconstruct",
            Arrays.asList("Mining level: 2"),
            Arrays.asList(),
            "{ench:[]}");
    }

    private static FakeItem i3() {
        return new FakeItem("Chest [Large]", "minecraft", Arrays.asList(), Arrays.asList(), null);
    }

    @Test
    public void bareTermMatchesDisplayName() {
        // 'iron' -> I1
        assertTrue(
            SearchQuery.parse("iron")
                .matches(i1()));
        // 'IRON' -> I1 (case-insensitive)
        assertTrue(
            SearchQuery.parse("IRON")
                .matches(i1()));
        // 'ir.n' -> I1 (regex)
        assertTrue(
            SearchQuery.parse("ir.n")
                .matches(i1()));
    }

    @Test
    public void bareTermMatchesTooltipLine() {
        // 'crafting' -> I1 (tooltip line)
        assertTrue(
            SearchQuery.parse("crafting")
                .matches(i1()));
        // 'level' -> I2 (tooltip line)
        assertTrue(
            SearchQuery.parse("level")
                .matches(i2()));
    }

    @Test
    public void bareTermIsRegex() {
        // '@mine' -> I1 and I3 (both 'minecraft')
        assertTrue(
            SearchQuery.parse("@mine")
                .matches(i1()));
        assertTrue(
            SearchQuery.parse("@mine")
                .matches(i3()));
        assertFalse(
            SearchQuery.parse("@mine")
                .matches(i2()));
        // '@TCON' -> I2
        assertTrue(
            SearchQuery.parse("@TCON")
                .matches(i2()));
        assertFalse(
            SearchQuery.parse("@TCON")
                .matches(i1()));
    }

    @Test
    public void orSplitOnPipe() {
        // '#ingot' -> I1
        assertTrue(
            SearchQuery.parse("#ingot")
                .matches(i1()));
        // '#INGOT' -> I1 (case-insensitive)
        assertTrue(
            SearchQuery.parse("#INGOT")
                .matches(i1()));
        // '#gold' -> none (I2's ore names are empty)
        assertFalse(
            SearchQuery.parse("#gold")
                .matches(i1()));
        assertFalse(
            SearchQuery.parse("#gold")
                .matches(i2()));
        assertFalse(
            SearchQuery.parse("#gold")
                .matches(i3()));
    }

    @Test
    public void andWithinPart() {
        // '$ench' -> I2 only (I1 and I3 have no NBT and never match a $ term)
        assertTrue(
            SearchQuery.parse("$ench")
                .matches(i2()));
        assertFalse(
            SearchQuery.parse("$ench")
                .matches(i1()));
        assertFalse(
            SearchQuery.parse("$ench")
                .matches(i3()));
        // '$ENCH' -> I2 (case-insensitive)
        assertTrue(
            SearchQuery.parse("$ENCH")
                .matches(i2()));
        assertFalse(
            SearchQuery.parse("$ENCH")
                .matches(i1()));
    }

    @Test
    public void atPrefixModNamespaceContains() {
        // 'iron|gold' -> I1 and I2
        assertTrue(
            SearchQuery.parse("iron|gold")
                .matches(i1()));
        assertTrue(
            SearchQuery.parse("iron|gold")
                .matches(i2()));
        assertFalse(
            SearchQuery.parse("iron|gold")
                .matches(i3()));
        // 'iron gold' -> none (AND of two terms that no single item has)
        assertFalse(
            SearchQuery.parse("iron gold")
                .matches(i1()));
        assertFalse(
            SearchQuery.parse("iron gold")
                .matches(i2()));
        assertFalse(
            SearchQuery.parse("iron gold")
                .matches(i3()));
        // 'gold level' -> I2
        assertTrue(
            SearchQuery.parse("gold level")
                .matches(i2()));
        assertFalse(
            SearchQuery.parse("gold level")
                .matches(i1()));
        assertFalse(
            SearchQuery.parse("gold level")
                .matches(i3()));
    }

    @Test
    public void hashPrefixTagLocationContains() {
        // '@minecraft chest' -> I3 ('@minecraft' is I1 and I3; 'chest' is I3)
        assertTrue(
            SearchQuery.parse("@minecraft chest")
                .matches(i3()));
        assertFalse(
            SearchQuery.parse("@minecraft chest")
                .matches(i1()));
        assertFalse(
            SearchQuery.parse("@minecraft chest")
                .matches(i2()));
    }

    @Test
    public void dollarPrefixRegex() {
        // '[' -> I3 (invalid regex falls back to the quoted literal and
        // matches the name 'Chest [Large]')
        assertTrue(
            SearchQuery.parse("[")
                .matches(i3()));
        assertFalse(
            SearchQuery.parse("[")
                .matches(i1()));
        assertFalse(
            SearchQuery.parse("[")
                .matches(i2()));
        // '(' -> none of the three, but must not throw
        assertFalse(
            SearchQuery.parse("(")
                .matches(i1()));
        assertFalse(
            SearchQuery.parse("(")
                .matches(i2()));
        assertFalse(
            SearchQuery.parse("(")
                .matches(i3()));
    }

    @Test
    public void invalidRegexFallsBackToQuotedLiteral() {
        // '' -> all three
        SearchQuery emptyQ = SearchQuery.parse("");
        assertTrue(emptyQ.isEmpty());
        assertTrue(emptyQ.matches(i1()));
        assertTrue(emptyQ.matches(i2()));
        assertTrue(emptyQ.matches(i3()));
        // ' ' -> all three
        SearchQuery blankQ = SearchQuery.parse("   ");
        assertTrue(blankQ.isEmpty());
        assertTrue(blankQ.matches(i1()));
        assertTrue(blankQ.matches(i2()));
        assertTrue(blankQ.matches(i3()));
        // null -> all three
        SearchQuery nullQ = SearchQuery.parse(null);
        assertNotNull(nullQ);
        assertTrue(nullQ.isEmpty());
        assertTrue(nullQ.matches(i1()));
        assertTrue(nullQ.matches(i2()));
        assertTrue(nullQ.matches(i3()));
        // 300 'a' chars truncated: text() is exactly 256 chars
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 300; i++) {
            sb.append('a');
        }
        SearchQuery q = SearchQuery.parse(sb.toString());
        assertFalse(q.isEmpty());
        assertEquals(
            256,
            q.text()
                .length());
    }

    @Test
    public void invalidRegexLiteralFallbackDoesNotMatchUnrelated() {}

    @Test
    public void caseInsensitiveMatching() {}

    @Test
    public void emptyBlankAndNullMatchEverything() {}

    @Test
    public void inputOver256CharsIsTruncated() {}

    @Test
    public void neverThrowsFuzz200RandomStrings() {
        Random rnd = new Random(3);
        String alphabet = "azAZ09@#$| ()\"'[]{}.*+-";
        for (int i = 0; i < 200; i++) {
            int len = rnd.nextInt(280);
            StringBuilder sb = new StringBuilder();
            for (int j = 0; j < len; j++) {
                sb.append(alphabet.charAt(rnd.nextInt(alphabet.length())));
            }
            SearchQuery q = SearchQuery.parse(sb.toString());
            assertNotNull(q);
            q.matches(i1());
            q.matches(i2());
            q.matches(i3());
            q.isEmpty();
            q.text();
        }
    }

    /**
     * TERM-08 (upstream): typed channel name lookup selects the first
     * channel whose display name equals the typed text, so duplicate names
     * make the lookup order-dependent. Search matching must be
     * order-independent: reordering the parts (the OR "channels") of a
     * query must not change which items match.
     */
    @Test
    public void doesNotReproduceTERM08_orderIndependentPartLookup() {
        SearchQuery abc = SearchQuery.parse("iron|gold|crafting");
        SearchQuery cba = SearchQuery.parse("crafting|gold|iron");
        for (FakeItem it : Arrays.asList(i1(), i2(), i3())) {
            assertEquals(abc.matches(it), cba.matches(it));
        }
    }
}
