package com.tuxprogrammer.simplestorage.statics;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;

/**
 * House rule 11: nothing machine-specific in git. The tracked sources, resources, build files and docs must not
 * name a particular machine, user or domain, so the tree stays portable.
 */
public class NoMachineDataTest {

    static final Pattern HOME_PATH = Pattern.compile("/home/[a-z]");

    static final String[] FORBIDDEN = { "tuxadmin", ".tuxhpc.net", "/Users/", "C:\\Users\\" };

    static final String[] SOURCE_ROOTS = { "src/main", "src/test" };

    static final String[] GRADLE_FILES = { "dependencies.gradle", "repositories.gradle", "gradle.properties" };

    static final String[] DOC_ROOTS = { "docs", "tools", "handoff" };

    static final String[] DOC_EXTENSIONS = { ".java", ".md", ".sh", ".py", ".gradle", ".properties", ".json", ".lang",
        ".cfg" };

    @Test
    public void noFileNamesAMachine() {
        List<Path> files = new ArrayList<>();
        collect(files);
        List<String> violations = new ArrayList<>();
        for (Path file : files) {
            String token;
            try {
                token = firstToken(ProjectFiles.read(file));
            } catch (IOException e) {
                continue;
            } catch (Exception e) {
                continue;
            }
            if (token != null) {
                violations.add(ProjectFiles.relative(file) + ": " + token);
            }
        }
        assertTrue(
            violations.isEmpty(),
            "machine-specific names in " + violations.size() + " file(s):\n  " + String.join("\n  ", violations));
    }

    @Test
    public void checkerCatchesAViolation() {
        assertNotNull(firstToken("see /home/someone/x"));
        assertNull(firstToken("see docs/x"));
    }

    @Test
    public void walkReadsFiles() {
        List<Path> files = new ArrayList<>();
        collect(files);
        assertTrue(files.size() > 100, "only " + files.size() + " files inspected");
    }

    private static void collect(List<Path> files) {
        for (String root : SOURCE_ROOTS) {
            walk(ProjectFiles.resolve(root), files, null);
        }
        for (String file : GRADLE_FILES) {
            Path path = ProjectFiles.resolve(file);
            if (Files.isRegularFile(path)) {
                files.add(path);
            }
        }
        for (String root : DOC_ROOTS) {
            walk(ProjectFiles.resolve(root), files, DOC_EXTENSIONS);
        }
    }

    private static void walk(Path root, List<Path> files, final String[] extensions) {
        if (!Files.isDirectory(root)) {
            return;
        }
        try {
            Files.walkFileTree(root, new java.nio.file.SimpleFileVisitor<Path>() {

                @Override
                public FileVisitResult visitFile(Path file, java.nio.file.attribute.BasicFileAttributes attrs) {
                    String name = file.getFileName()
                        .toString();
                    if (file.equals(
                        ProjectFiles
                            .resolve("src/test/java/com/tuxprogrammer/simplestorage/statics/NoMachineDataTest.java"))) {
                        return FileVisitResult.CONTINUE;
                    }
                    if (file.startsWith(ProjectFiles.resolve("docs/original-spec"))) {
                        return FileVisitResult.CONTINUE;
                    }
                    if (extensions != null) {
                        boolean known = false;
                        for (String extension : extensions) {
                            if (name.endsWith(extension)) {
                                known = true;
                                break;
                            }
                        }
                        if (!known) {
                            return FileVisitResult.CONTINUE;
                        }
                    }
                    if (Files.isRegularFile(file)) {
                        files.add(file);
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            throw new IllegalStateException("cannot walk " + root, e);
        }
    }

    /** Returns the first machine-specific token found in the text, or null. */
    static String firstToken(String text) {
        if (HOME_PATH.matcher(text)
            .find()) {
            return "/home/<name>";
        }
        for (String token : FORBIDDEN) {
            if (text.contains(token)) {
                return token;
            }
        }
        return null;
    }
}
