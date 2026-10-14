package com.demoproject.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 * File-system helpers built on {@code java.nio}.
 */
public final class FileHelper {

    private static final Logger LOG = Logger.getLogger(FileHelper.class.getName());
    private static final DateTimeFormatter FILE_TIMESTAMP = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSS");
    private static final int MAX_NAME_LENGTH = 80;

    private FileHelper() {
    }

    /**
     * @param name free text such as a scenario name
     * @return a file-name-safe version (letters, digits, '-', '_'), max 80 characters
     */
    public static String sanitize(String name) {
        String safe = name == null ? "unnamed" : name.replaceAll("[^A-Za-z0-9-_]+", "_").replaceAll("_+", "_");
        safe = safe.replaceAll("^_|_$", "");
        if (safe.isEmpty()) {
            safe = "unnamed";
        }
        return safe.length() > MAX_NAME_LENGTH ? safe.substring(0, MAX_NAME_LENGTH) : safe;
    }

    /**
     * @return current time formatted for file names, e.g. 20260101_101500_123
     */
    public static String fileTimestamp() {
        return LocalDateTime.now().format(FILE_TIMESTAMP);
    }

    /**
     * Deletes a file or directory tree. Failures are logged and swallowed.
     *
     * @param path file or directory
     */
    public static void deleteRecursively(Path path) {
        if (path == null || !Files.exists(path)) {
            return;
        }
        try (Stream<Path> walk = Files.walk(path)) {
            walk.sorted(Comparator.reverseOrder()).forEach(p -> {
                try {
                    Files.deleteIfExists(p);
                } catch (IOException e) {
                    LOG.warning(() -> "Unable to delete " + p + ": " + e.getMessage());
                }
            });
        } catch (IOException e) {
            LOG.warning(() -> "Unable to delete " + path + ": " + e.getMessage());
        }
    }
}
