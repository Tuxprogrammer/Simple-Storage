package com.tuxprogrammer.simplestorage.ingame;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.discovery.ClassNameFilter;
import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.engine.support.descriptor.MethodSource;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.TagFilter;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.TestPlan;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;

/**
 * Runs the JUnit 5 tests tagged "ingame" inside a live (dev) server where the
 * Forge registries and tile-entity mappings exist; used by the dev-only
 * /simplestoragenh test command and the autotest boot.
 * <p>
 * Side: server, development only.
 */
public final class InGameRunner {

    /**
     * The JUnit tag that marks a test as runnable in-game.
     */
    public static final String TAG = "ingame";

    /**
     * Private constructor; this class is only used statically.
     */
    private InGameRunner() {}

    /**
     * Builds a {@link LauncherDiscoveryRequest} that selects the
     * {@code com.tuxprogrammer.simplestorage} package, restricted to tests
     * tagged {@link #TAG}, and additionally restricted to classes whose name
     * ends with the given selector.
     *
     * @param selector the suffix to match against test class names, or {@code null}
     *                 to match all tagged test classes
     * @return the discovery request to hand to a JUnit launcher
     */
    public static LauncherDiscoveryRequest buildRequest(String selector) {
        LauncherDiscoveryRequestBuilder request = LauncherDiscoveryRequestBuilder.request()
            .selectors(DiscoverySelectors.selectPackage("com.tuxprogrammer.simplestorage"))
            .filters(TagFilter.includeTags(TAG));

        if (selector != null && !selector.isEmpty()) {
            request = request.filters(ClassNameFilter.includeClassNamePatterns(".*" + Pattern.quote(selector) + "$"));
        }

        return request.build();
    }

    /**
     * Runs the tagged in-game tests and returns a human-readable report.
     *
     * @param selector either {@code "all"} (run every tagged test), {@code "list"}
     *                 (list the tagged test classes instead of running them), or
     *                 a non-empty substring that the selected test class names must
     *                 end with. A {@code null} or empty selector is treated as
     *                 {@code "all"}.
     * @return one line per result, terminated by a summary line of the form
     *         {@code "Scenarios: N passed, M failed"}
     */
    public static String[] run(String selector) {
        LauncherDiscoveryRequest request = buildRequest(selector);
        Launcher launcher = LauncherFactory.create();

        String effectiveSelector = selector == null ? "" : selector;

        if (effectiveSelector.equalsIgnoreCase("list")) {
            TestPlan plan = launcher.discover(request);

            List<String> classNames = new ArrayList<String>();
            int count = 0;

            for (TestIdentifier root : plan.getRoots()) {
                collect(plan, root, classNames);
                count += countTests(plan, root);
            }

            Collections.sort(classNames);

            List<String> lines = new ArrayList<String>(classNames);
            lines.add("Scenarios: " + count + " listed");

            return lines.toArray(new String[0]);
        }

        List<String> lines = new ArrayList<String>();
        final int[] counters = new int[] { 0, 0 }; // passed, failed

        TestExecutionListener listener = new TestExecutionListener() {

            @Override
            public void executionFinished(TestIdentifier id, TestExecutionResult result) {
                if (!id.isTest()) {
                    return;
                }

                if (result.getStatus() == TestExecutionResult.Status.SUCCESSFUL) {
                    counters[0]++;
                    lines.add("PASS " + nameOf(id));
                } else {
                    counters[1]++;
                    String detail;
                    Throwable throwable = result.getThrowable()
                        .orElse(null);
                    if (throwable == null) {
                        detail = result.getStatus()
                            .name();
                    } else {
                        String message = throwable.getMessage();
                        detail = message == null ? throwable.getClass()
                            .getName() : message;
                    }
                    lines.add("FAIL " + nameOf(id) + ": " + detail);
                }
            }
        };

        launcher.execute(request, listener);

        if (counters[0] + counters[1] == 0) {
            lines.add("FAIL discovery: no tests tagged ingame were found");
            lines.add("Scenarios: 0 passed, 1 failed");
        } else {
            lines.add("Scenarios: " + counters[0] + " passed, " + counters[1] + " failed");
        }

        return lines.toArray(new String[0]);
    }

    /**
     * Computes the report name for a test identifier: the unique id's last
     * segment (the legacy reporting name), prefixed by the class name from the
     * identifier's {@link MethodSource} when one is available.
     *
     * @param id the test identifier to name
     * @return the report name used in PASS/FAIL lines
     */
    private static String nameOf(TestIdentifier id) {
        String name = id.getLegacyReportingName();
        if (id.getSource() != null && id.getSource()
            .isPresent()) {
            Object source = id.getSource()
                .get();
            if (source instanceof MethodSource) {
                MethodSource methodSource = (MethodSource) source;
                String className = methodSource.getClassName();
                if (className != null && !className.isEmpty()) {
                    name = className + "#" + name;
                }
            }
        }
        return name;
    }

    /**
     * Collects distinct test class names under the given identifier, visiting
     * every descendant in the test plan. A class name is added at most once.
     *
     * @param plan       the test plan the identifiers belong to
     * @param identifier the identifier to start from
     * @param classNames accumulator of distinct class names
     */
    private static void collect(TestPlan plan, TestIdentifier identifier, List<String> classNames) {
        if (identifier.isTest() && identifier.getSource() != null
            && identifier.getSource()
                .isPresent()) {
            Object source = identifier.getSource()
                .get();
            if (source instanceof MethodSource) {
                String className = ((MethodSource) source).getClassName();
                if (className != null && !classNames.contains(className)) {
                    classNames.add(className);
                }
            }
        }

        for (TestIdentifier child : plan.getChildren(identifier)) {
            collect(plan, child, classNames);
        }
    }

    /**
     * Counts the number of tests under the given identifier, visiting every
     * descendant in the test plan.
     *
     * @param plan       the test plan the identifiers belong to
     * @param identifier the identifier to start from
     * @return the number of tests under (and including) the identifier
     */
    private static int countTests(TestPlan plan, TestIdentifier identifier) {
        int count = identifier.isTest() ? 1 : 0;

        for (TestIdentifier child : plan.getChildren(identifier)) {
            count += countTests(plan, child);
        }

        return count;
    }
}
