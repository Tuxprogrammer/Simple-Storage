package com.tuxprogrammer.simplestorage.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.gtnewhorizon.gtnhlib.config.Config;
import com.tuxprogrammer.simplestorage.statics.ProjectFiles;

/** Every option in {@code SimpleStorageConfig.CLASSES} is documented in the Configs section of PACK-CHANGES.md. */
public class ConfigDocsTest {

    private static final String SECTION_START = "## Configs (GTNHLib 0.11.54)";
    private static final String SECTION_END = "## ";

    /**
     * The Configs section: from its {@code ## Configs (GTNHLib 0.11.54)} heading to the next {@code ## } heading.
     *
     * @throws IOException when the document cannot be read
     */
    private static String configsSection() throws IOException {
        String text = ProjectFiles.read(ProjectFiles.resolve("docs/PACK-CHANGES.md"));
        int start = text.indexOf(SECTION_START);
        assertTrue(start >= 0, "no '" + SECTION_START + "' heading in docs/PACK-CHANGES.md");
        int end = text.indexOf(SECTION_END, start + SECTION_START.length());
        String section = end >= 0 ? text.substring(start, end) : text.substring(start);
        return section;
    }

    @Test
    public void sectionIsFound() throws IOException {
        String section = configsSection();
        assertTrue(section.length() > 500, "the Configs section is suspiciously short: " + section.length() + " chars");
    }

    @Test
    public void everyOptionIsDocumented() throws IOException {
        String section = configsSection();
        List<String> undocumented = new ArrayList<>();
        for (Class<?> configClass : SimpleStorageConfig.CLASSES) {
            String category = configClass.getAnnotation(Config.class)
                .category();
            for (Field option : SimpleStorageConfig.options(configClass)) {
                if (!section.contains(option.getName()) || !section.contains(category + ".")) {
                    undocumented.add(category + "." + option.getName());
                }
            }
        }
        for (Field field : ModulesConfig.Integration.class.getDeclaredFields()) {
            if (!section.contains("integration." + field.getName())) {
                undocumented.add("modules.integration." + field.getName());
            }
        }
        assertTrue(undocumented.isEmpty(), "options missing from the Configs section: " + undocumented);
    }

    @Test
    public void defaultsInTheTableMatchTheFields() throws IOException {
        String section = configsSection();
        assertRowDefault(section, "modules.storage", "true", ModulesConfig.storage);
        assertRowDefault(section, "modules.integration.appeng", "false", ModulesConfig.integration.appeng);
        assertRowDefault(section, "euSink.enabled", "false", EuSinkConfig.enabled);
        assertRowDefault(section, "network.inventoryConnectorRange", "16", NetworkConfig.inventoryConnectorRange);
        assertRowDefault(section, "wireless.reach", "16", WirelessConfig.reach);
    }

    /**
     * The table row for {@code key} contains {@code expected} in backticks, and the live field value equals it.
     *
     * @param section  the Configs section text
     * @param key      the documented option key, in backticks in the row
     * @param expected the default string shown in backticks in the row
     * @param live     the current value of the field the key documents
     */
    private static void assertRowDefault(String section, String key, String expected, Object live) {
        String row = rowContaining(section, "`" + key + "`");
        assertTrue(
            row.contains("`" + expected + "`"),
            "row for " + key + " does not document default `" + expected + "`: " + row);
        assertEquals(expected, String.valueOf(live), "live value of " + key + " disagrees with the documented default");
    }

    /**
     * Returns the line of {@code section} that contains {@code needle}, or the empty string when none does.
     */
    private static String rowContaining(String section, String needle) {
        for (String line : section.split("\n")) {
            if (line.contains(needle)) {
                return line;
            }
        }
        return "";
    }
}
