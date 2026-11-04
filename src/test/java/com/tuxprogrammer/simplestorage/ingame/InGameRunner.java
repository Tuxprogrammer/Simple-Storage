package com.tuxprogrammer.simplestorage.ingame;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A tiny reflection-based runner for the JUnit 5 tests tagged "ingame", used
 * inside a live (dev) Forge server where the JUnit launcher cannot see the
 * test classes at all: the class loader identity of the tests as seen by the
 * JUnit engine (loaded through Forge's LaunchClassLoader) differs from the
 * identity the engine itself was loaded with, so annotations do not match.
 * <p>
 * This runner therefore does not use the JUnit launcher or engine. It scans
 * its own code source directory for class files, loads them with its own
 * class loader, and matches JUnit annotations <em>by name</em>
 * ({@code annotation.annotationType().getName()}), so the loader identity
 * never matters.
 * <p>
 * Side: server, development only.
 */
public final class InGameRunner {

    /**
     * The JUnit tag that marks a test as runnable in-game.
     */
    public static final String TAG = "ingame";

    /**
     * Log4j logger for this class.
     */
    private static final org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager
        .getLogger(InGameRunner.class);

    /**
     * Annotation name (fully qualified) of the JUnit 5 {@code @Tag}
     * annotation, matched by name so class loader identity never matters.
     */
    private static final String TAG_ANNOTATION = "org.junit.jupiter.api.Tag";

    /**
     * Annotation name (fully qualified) of the JUnit 5 {@code @Test}
     * annotation.
     */
    private static final String TEST_ANNOTATION = "org.junit.jupiter.api.Test";

    /**
     * Annotation name (fully qualified) of the JUnit 5 {@code @Disabled}
     * annotation.
     */
    private static final String DISABLED_ANNOTATION = "org.junit.jupiter.api.Disabled";

    /**
     * Annotation name (fully qualified) of the JUnit 5 {@code @BeforeAll}
     * annotation.
     */
    private static final String BEFORE_ALL_ANNOTATION = "org.junit.jupiter.api.BeforeAll";

    /**
     * Annotation name (fully qualified) of the JUnit 5 {@code @AfterAll}
     * annotation.
     */
    private static final String AFTER_ALL_ANNOTATION = "org.junit.jupiter.api.AfterAll";

    /**
     * Annotation name (fully qualified) of the JUnit 5 {@code @BeforeEach}
     * annotation.
     */
    private static final String BEFORE_EACH_ANNOTATION = "org.junit.jupiter.api.BeforeEach";

    /**
     * Annotation name (fully qualified) of the JUnit 5 {@code @AfterEach}
     * annotation.
     */
    private static final String AFTER_EACH_ANNOTATION = "org.junit.jupiter.api.AfterEach";

    /**
     * Maximum number of characters of an exception message included in a
     * FAIL line (first line only).
     */
    private static final int MAX_DETAIL_LENGTH = 200;

    /**
     * Private constructor; this class is only used statically.
     */
    private InGameRunner() {}

    /**
     * Runs the tagged in-game tests (or lists them) and returns a
     * human-readable report.
     * <p>
     * The last line of the returned array is always a summary line: either
     * {@code "Scenarios: N passed, M failed"} when tests were run, or
     * {@code "Scenarios: N listed"} when the selector was "list".
     *
     * @param selector either {@code "all"} (run every tagged test),
     *                 {@code "list"} (list the tagged test classes instead of
     *                 running them), or the simple name of a single test
     *                 class to run (case-insensitive). A {@code null} or empty
     *                 selector is treated as {@code "all"}.
     * @return one line per result, terminated by the summary line
     */
    public static String[] run(String selector) {
        String effectiveSelector = selector == null ? "all" : selector;
        File directory = codeSourceDirectory();

        List<Class<?>> classes = findClasses(directory);
        List<Class<?>> selected = new ArrayList<Class<?>>();
        for (Class<?> c : classes) {
            if (hasTag(c)) {
                if ("all".equalsIgnoreCase(effectiveSelector) || "list".equalsIgnoreCase(effectiveSelector)
                    || c.getSimpleName()
                        .equalsIgnoreCase(effectiveSelector)) {
                    selected.add(c);
                }
            }
        }

        Collections.sort(selected, new Comparator<Class<?>>() {

            @Override
            public int compare(Class<?> a, Class<?> b) {
                return a.getName()
                    .compareTo(b.getName());
            }
        });

        List<String> lines = new ArrayList<String>();

        if ("list".equalsIgnoreCase(effectiveSelector)) {
            int count = 0;
            for (Class<?> c : selected) {
                lines.add(c.getSimpleName());
                for (Method m : c.getDeclaredMethods()) {
                    if (hasAnnotation(m, TEST_ANNOTATION) && !hasAnnotation(m, DISABLED_ANNOTATION)) {
                        count++;
                    }
                }
            }
            lines.add("Scenarios: " + count + " listed");
            return lines.toArray(new String[0]);
        }

        final int[] counters = new int[] { 0, 0 }; // passed, failed

        for (Class<?> c : selected) {
            runClass(c, lines, counters);
        }

        if (counters[0] + counters[1] == 0) {
            lines.add("FAIL discovery: no classes tagged ingame found under " + directory);
            counters[1]++;
        }

        lines.add("Scenarios: " + counters[0] + " passed, " + counters[1] + " failed");
        return lines.toArray(new String[0]);
    }

    /**
     * Resolves the directory that contains this class's {@code .class}
     * files, i.e. the root of the test classes tree.
     * <p>
     * Under Forge's LaunchClassLoader,
     * {@code getProtectionDomain().getCodeSource().getLocation()} points at
     * this class's own {@code .class} <em>file</em> rather than the classes
     * root, so the root is computed from the class resource instead: the
     * resource for {@code com/tuxprogrammer/simplestorage/ingame/InGameRunner.class}
     * lives at {@code root/com/tuxprogrammer/simplestorage/ingame/}, so the
     * root is four {@code getParentFile()} steps up from the file's parent.
     * The old code-source approach is kept only as a fallback for when the
     * resource is {@code null} or not on the {@code file:} protocol.
     *
     * @return the directory to scan for test classes
     */
    private static File codeSourceDirectory() {
        try {
            URL res = InGameRunner.class.getClassLoader()
                .getResource("com/tuxprogrammer/simplestorage/ingame/InGameRunner.class");
            if (res != null && "file".equals(res.getProtocol())) {
                File classFile = new File(res.toURI());
                // classFile is root/com/tuxprogrammer/simplestorage/ingame/InGameRunner.class,
                // so five parent steps yield the root: ingame -> simplestorage
                // -> tuxprogrammer -> com -> root.
                return classFile.getParentFile()
                    .getParentFile()
                    .getParentFile()
                    .getParentFile()
                    .getParentFile();
            }
            URL url = InGameRunner.class.getProtectionDomain()
                .getCodeSource()
                .getLocation();
            return new File(url.toURI());
        } catch (Throwable e) {
            throw new IllegalStateException("Cannot resolve code source of " + InGameRunner.class.getName(), e);
        }
    }

    /**
     * Walks the given directory recursively, converts each {@code .class}
     * file path to a class name, and loads every class that loads without
     * error, skipping inner classes.
     *
     * @param directory the root of the compiled test classes
     * @return the loaded candidate classes, in no particular order
     */
    private static List<Class<?>> findClasses(File directory) {
        List<Class<?>> classes = new ArrayList<Class<?>>();
        if (!directory.isDirectory()) {
            return classes;
        }
        Path root = directory.toPath();
        List<Path> paths;
        try (Stream<Path> walk = Files.walk(root)) {
            paths = walk.filter(
                p -> p.toString()
                    .endsWith(".class"))
                .collect(Collectors.toList());
        } catch (Throwable e) {
            logger.warn("Could not walk class directory: " + directory, e);
            return classes;
        }
        for (Path path : paths) {
            String relative = root.relativize(path)
                .toString();
            String name = relative.replace(File.separatorChar, '.')
                .substring(0, relative.length() - ".class".length());
            if (name.indexOf('$') >= 0) {
                continue;
            }
            try {
                classes.add(Class.forName(name, false, InGameRunner.class.getClassLoader()));
            } catch (Throwable t) {
                // Skip classes that cannot be loaded in this environment.
            }
        }
        return classes;
    }

    /**
     * Checks whether the given class carries a {@code @Tag} annotation
     * (matched by name) whose {@code value()} equals {@link #TAG}.
     *
     * @param c the class to check
     * @return whether the class is tagged for in-game execution
     */
    private static boolean hasTag(Class<?> c) {
        for (java.lang.annotation.Annotation annotation : c.getAnnotations()) {
            if (!TAG_ANNOTATION.equals(
                annotation.annotationType()
                    .getName())) {
                continue;
            }
            try {
                Object value = annotation.annotationType()
                    .getMethod("value")
                    .invoke(annotation);
                if (TAG.equals(String.valueOf(value))) {
                    return true;
                }
            } catch (Throwable t) {
                // Fall through; this annotation does not count.
            }
        }
        return false;
    }

    /**
     * Checks whether the given method carries an annotation whose fully
     * qualified type name equals the given name.
     *
     * @param method         the method to check
     * @param annotationName the annotation type name to look for
     * @return whether the method carries that annotation
     */
    private static boolean hasAnnotation(Method method, String annotationName) {
        for (java.lang.annotation.Annotation annotation : method.getAnnotations()) {
            if (annotation.annotationType()
                .getName()
                .equals(annotationName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the declared methods of the given class annotated with the
     * given annotation type name, sorted by method name, restricted to
     * static or instance methods as requested.
     *
     * @param c              the class to inspect
     * @param annotationName the annotation type name to look for
     * @param statics        whether to return static methods ({@code true})
     *                       or instance methods ({@code false})
     * @return the matching methods, sorted by name
     */
    private static List<Method> methodsWith(Class<?> c, String annotationName, boolean statics) {
        List<Method> methods = new ArrayList<Method>();
        for (Method m : c.getDeclaredMethods()) {
            if (Modifier.isStatic(m.getModifiers()) != statics) {
                continue;
            }
            if (hasAnnotation(m, annotationName)) {
                methods.add(m);
            }
        }
        Collections.sort(methods, new Comparator<Method>() {

            @Override
            public int compare(Method a, Method b) {
                return a.getName()
                    .compareTo(b.getName());
            }
        });
        return methods;
    }

    /**
     * Runs a single test class: {@code @BeforeAll} once, then every
     * non-disabled {@code @Test} method (with {@code @BeforeEach} /
     * {@code @AfterEach} around each) in a fresh instance, and
     * {@code @AfterAll} once in a finally block. Appends one line per
     * outcome to the given list and updates the given counters.
     *
     * @param c        the test class to run
     * @param lines    accumulator of report lines
     * @param counters counters; {@code [0]} is the passed count and
     *                 {@code [1]} the failed count, updated in place
     */
    private static void runClass(Class<?> c, List<String> lines, int[] counters) {
        String simpleName = c.getSimpleName();
        boolean beforeAllFailed = false;

        try {
            for (Method m : methodsWith(c, BEFORE_ALL_ANNOTATION, true)) {
                try {
                    invoke(m, null);
                } catch (Throwable t) {
                    beforeAllFailed = true;
                    counters[1]++;
                    lines.add("FAIL " + simpleName + ".<BeforeAll>: " + detail(t));
                    logger.error("[scenario] trace of " + simpleName + ".<BeforeAll>", t);
                    break;
                }
            }
        } finally {
            if (!beforeAllFailed) {
                for (Method m : methodsWith(c, AFTER_ALL_ANNOTATION, true)) {
                    try {
                        invoke(m, null);
                    } catch (Throwable t) {
                        // AfterAll failures are noted but do not change the
                        // outcome of the class.
                    }
                }
            }
        }

        if (beforeAllFailed) {
            return;
        }

        for (Method m : methodsWith(c, TEST_ANNOTATION, false)) {
            if (hasAnnotation(m, DISABLED_ANNOTATION)) {
                continue;
            }
            Object instance = null;
            try {
                instance = c.getDeclaredConstructor()
                    .newInstance();
            } catch (Throwable t) {
                counters[1]++;
                lines.add("FAIL " + simpleName + "." + m.getName() + ": " + detail(t));
                logger.error("[scenario] trace of " + simpleName + "." + m.getName(), t);
                continue;
            }

            boolean failed = false;
            try {
                for (Method each : methodsWith(c, BEFORE_EACH_ANNOTATION, false)) {
                    invoke(each, instance);
                }
                invoke(m, instance);
            } catch (Throwable t) {
                failed = true;
                counters[1]++;
                lines.add("FAIL " + simpleName + "." + m.getName() + ": " + detail(t));
                logger.error("[scenario] trace of " + simpleName + "." + m.getName(), t);
            } finally {
                for (Method each : methodsWith(c, AFTER_EACH_ANNOTATION, false)) {
                    try {
                        invoke(each, instance);
                    } catch (Throwable t) {
                        if (!failed) {
                            failed = true;
                            counters[1]++;
                            lines.add("FAIL " + simpleName + "." + m.getName() + ": " + detail(t));
                            logger.error("[scenario] trace of " + simpleName + "." + m.getName(), t);
                        }
                    }
                }
            }

            if (!failed) {
                counters[0]++;
                lines.add("PASS " + simpleName + "." + m.getName());
            }
        }
    }

    /**
     * Invokes a method reflectively, making it accessible if necessary, and
     * unwraps {@link InvocationTargetException} so the caller sees the
     * original throwable.
     *
     * @param method   the method to invoke
     * @param instance the instance to invoke it on, or {@code null} for a
     *                 static method
     * @throws Throwable the original failure, unwrapped
     */
    private static void invoke(Method method, Object instance) throws Throwable {
        method.setAccessible(true);
        try {
            method.invoke(instance);
        } catch (InvocationTargetException e) {
            throw e.getCause() == null ? e : e.getCause();
        }
    }

    /**
     * Formats a failure detail: the first line of the throwable's message,
     * truncated to {@link #MAX_DETAIL_LENGTH} characters (or the throwable's
     * class name when it has no message), followed by {@code " @ "}, the
     * class name prefix, and the first stack frame whose class belongs to
     * {@code com.tuxprogrammer.simplestorage} but not to
     * {@code com.tuxprogrammer.simplestorage.ingame} (or the throwable's
     * first frame when none matches), formatted
     * {@code ClassSimpleName.method(File:line)}; a frame without a source
     * file is formatted as {@code ClassSimpleName.method}.
     *
     * @param t the throwable that caused the failure
     * @return the detail text for a FAIL line
     */
    private static String detail(Throwable t) {
        String message = t.getMessage();
        if (message == null) {
            message = t.getClass()
                .getName();
        } else {
            int newline = message.indexOf('\n');
            if (newline >= 0) {
                message = message.substring(0, newline);
            }
            if (message.length() > MAX_DETAIL_LENGTH) {
                message = message.substring(0, MAX_DETAIL_LENGTH);
            }
            message = t.getClass()
                .getSimpleName() + ": "
                + message;
        }
        return message + " @ " + frameText(t);
    }

    /**
     * Selects and formats a diagnostic stack frame for a failure: the first
     * frame whose declaring class belongs to
     * {@code com.tuxprogrammer.simplestorage} (but not to the
     * {@code com.tuxprogrammer.simplestorage.ingame} package, which is this
     * runner's own machinery), or the throwable's first frame when none
     * matches.
     *
     * @param t the throwable to describe
     * @return the formatted frame, {@code ClassSimpleName.method(File:line)}
     *         or {@code ClassSimpleName.method} when the frame has no
     *         source file, or the empty string when the throwable has no
     *         stack trace at all
     */
    private static String frameText(Throwable t) {
        StackTraceElement[] frames = t.getStackTrace();
        if (frames == null || frames.length == 0) {
            return "";
        }
        StackTraceElement frame = frames[0];
        for (StackTraceElement candidate : frames) {
            String className = candidate.getClassName();
            if (className.startsWith("com.tuxprogrammer.simplestorage.")
                && !className.startsWith("com.tuxprogrammer.simplestorage.ingame.")) {
                frame = candidate;
                break;
            }
        }
        return frame.getClassName()
            .substring(
                frame.getClassName()
                    .lastIndexOf('.') + 1)
            + "."
            + frame.getMethodName()
            + (frame.getFileName() == null ? "" : "(" + frame.getFileName() + ":" + frame.getLineNumber() + ")");
    }
}
