package com.tuxprogrammer.simplestorage.core;

import java.lang.reflect.Method;

import com.tuxprogrammer.simplestorage.SimpleStorage;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.event.FMLServerStartingEvent;

/**
 * Development-only hooks into the in-game JUnit scenario runner.
 *
 * <p>
 * The runner ({@code com.tuxprogrammer.simplestorage.ingame.InGameRunner}) lives in the test source set, so this
 * class reaches it by reflection only when it is on the classpath. Everything here is gated on
 * {@link DevEnvironment#testHooksEnabled()}, so production servers are unaffected.
 *
 * <p>
 * Side: server, development only.
 */
public final class InGameHooks {

    /** System property that, when true, makes server startup run all scenarios and shut the server down. */
    public static final String AUTOTEST_PROPERTY = "simplestoragenh.autotest";

    private InGameHooks() {}

    /**
     * Runs the in-game scenario runner.
     *
     * @param selector "all", "list", or a test class simple name
     * @return the runner's report lines; the last line is the "Scenarios: N passed, M failed" summary
     */
    public static String[] runScenarios(String selector) {
        if (!DevEnvironment.testHooksEnabled()) {
            return new String[] { "Scenarios: 0 passed, 0 failed (test hooks are off)" };
        }
        try {
            Class<?> runnerClass = Class.forName("com.tuxprogrammer.simplestorage.ingame.InGameRunner");
            Method run = runnerClass.getMethod("run", String.class);
            return (String[]) run.invoke(null, selector);
        } catch (ClassNotFoundException e) {
            return new String[] { "Scenarios: 0 passed, 0 failed (runner not on the classpath)" };
        } catch (Exception e) {
            return new String[] { "FAIL runner: " + e, "Scenarios: 0 passed, 1 failed" };
        }
    }

    /**
     * Registers the {@code /simplestoragenh} server command when test hooks are enabled.
     *
     * @param event the server-starting event
     */
    public static void registerCommand(FMLServerStartingEvent event) {
        if (DevEnvironment.testHooksEnabled()) {
            event.registerServerCommand(new ScenarioCommand());
        }
    }

    /**
     * If the autotest property and test hooks are both on, runs all scenarios, logs every report line, and shuts the
     * server down so a CI run exits.
     */
    public static void autoRun() {
        if (Boolean.getBoolean(AUTOTEST_PROPERTY) && DevEnvironment.testHooksEnabled()) {
            String[] lines = runScenarios("all");
            for (String line : lines) {
                if (line.startsWith("FAIL")) {
                    SimpleStorage.LOG.error("[scenario] {}", line);
                } else {
                    SimpleStorage.LOG.info("[scenario] {}", line);
                }
            }
            FMLCommonHandler.instance()
                .getMinecraftServerInstance()
                .initiateShutdown();
        }
    }
}
