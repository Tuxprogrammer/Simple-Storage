package com.tuxprogrammer.simplestorage;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import com.tuxprogrammer.simplestorage.statics.ProjectFiles;

/** Hard dependencies: GregTech and GTNHLib (D17), everything else only ordered. */
public class DependenciesTest {

    private static final String REQUIRED = "required-after:";

    @Test
    public void gregtechAndGtnhlibAreRequiredAfter() {
        assertTrue(SimpleStorage.DEPENDENCIES.contains(REQUIRED + "gregtech"), "GregTech is a hard dependency");
        assertTrue(SimpleStorage.DEPENDENCIES.contains(REQUIRED + "gtnhlib"), "GTNHLib is a hard dependency");
    }

    @Test
    public void optionalModsAreOnlyOrdered() {
        assertTrue(SimpleStorage.DEPENDENCIES.contains("after:NotEnoughItems"));
        assertTrue(SimpleStorage.DEPENDENCIES.contains("after:Waila"));
        assertTrue(SimpleStorage.DEPENDENCIES.contains("after:appliedenergistics2"));
        for (String segment : SimpleStorage.DEPENDENCIES.split(";")) {
            if (segment.startsWith(REQUIRED)) {
                String mod = segment.substring(REQUIRED.length());
                assertTrue(
                    mod.equals("gregtech") || mod.equals("gtnhlib"),
                    "only GregTech and GTNHLib may be required, found " + mod);
            }
        }
    }

    @Test
    public void mcmodInfoDeclaresTheSame() throws IOException {
        String info = ProjectFiles.read(ProjectFiles.resolve("src/main/resources/mcmod.info"));
        assertTrue(info.contains("required-after:gregtech"));
        assertTrue(info.contains("required-after:gtnhlib"));
        assertTrue(info.contains("useDependencyInformation\": true"));
    }
}
