package com.tuxprogrammer.simplestorage.statics;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;

import org.junit.jupiter.api.Test;

/**
 * Config-safe registration: a flag gates behavior, never registration. Every content class registers the same
 * content regardless of the configuration, so the registration sources never read configuration.
 */
public class RegistrationIgnoresConfigTest {

    static final String[] FORBIDDEN = { "ModulesConfig", "ModuleState", "NetworkConfig", "WirelessConfig",
        "BehaviorConfig", "EuSinkConfig", "isActive(", "SimpleStorageConfig" };

    @Test
    public void modBlocksDoesNotReadConfig() throws IOException {
        assertNoConfigRead("block/ModBlocks.java");
    }

    @Test
    public void modItemsDoesNotReadConfig() throws IOException {
        assertNoConfigRead("item/ModItems.java");
    }

    @Test
    public void modTileEntitiesDoesNotReadConfig() throws IOException {
        assertNoConfigRead("blockentity/ModTileEntities.java");
    }

    @Test
    public void registrationDoesNotReadConfig() throws IOException {
        assertNoConfigRead("core/Registration.java");
    }

    @Test
    public void checkerCatchesAViolation() {
        assertEquals("ModulesConfig", firstForbidden("if (ModulesConfig.storage) register();"));
        assertNull(firstForbidden("register();"));
    }

    private static void assertNoConfigRead(String path) throws IOException {
        String relative = "src/main/java/com/tuxprogrammer/simplestorage/" + path;
        String source = ProjectFiles.read(ProjectFiles.resolve(relative));
        String token = firstForbidden(source);
        assertTrue(token == null, path + " reads configuration through '" + token + "'");
    }

    private static String firstForbidden(String source) {
        for (String token : FORBIDDEN) {
            if (source.contains(token)) {
                return token;
            }
        }
        return null;
    }
}
