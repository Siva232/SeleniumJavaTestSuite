package com.demoproject.reporting;

import com.demoproject.utils.FileHelper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Keeps only the newest N {@code Run_*} folders under the report root.
 */
public final class ReportHistoryCleaner {

    private static final Logger LOG = Logger.getLogger(ReportHistoryCleaner.class.getName());

    private ReportHistoryCleaner() {
    }

    /**
     * Run folder names embed a sortable timestamp, so name order equals age order.
     *
     * @param reportRoot folder containing Run_* folders
     * @param keep       number of newest folders to keep (including the current run)
     */
    public static void clean(Path reportRoot, int keep) {
        if (!Files.isDirectory(reportRoot)) {
            return;
        }
        List<Path> runs;
        try (Stream<Path> children = Files.list(reportRoot)) {
            runs = children.filter(Files::isDirectory)
                    .filter(p -> p.getFileName().toString().startsWith(ReportContext.RUN_PREFIX))
                    .sorted(Comparator.comparing((Path p) -> p.getFileName().toString()).reversed())
                    .collect(Collectors.toList());
        } catch (IOException e) {
            LOG.warning(() -> "Unable to list report history in " + reportRoot + ": " + e.getMessage());
            return;
        }
        runs.stream().skip(keep).forEach(old -> {
            LOG.info(() -> "Deleting old report folder " + old.getFileName());
            FileHelper.deleteRecursively(old);
        });
    }
}
