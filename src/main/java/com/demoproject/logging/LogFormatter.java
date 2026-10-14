package com.demoproject.logging;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * Single-line JUL formatter: {@code 2026-01-01 10:15:00.123 [INFO ] [TestNG-PoolService-1] LoginPage - message}.
 * JUL handlers publish on the caller's thread, so {@link Thread#currentThread()} is the logging thread.
 */
public class LogFormatter extends Formatter {

    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")
            .withZone(ZoneId.systemDefault());

    @Override
    public String format(LogRecord record) {
        StringBuilder sb = new StringBuilder(160)
                .append(TIME.format(Instant.ofEpochMilli(record.getMillis())))
                .append(" [").append(String.format("%-7s", record.getLevel().getName())).append("] [")
                .append(Thread.currentThread().getName()).append("] ")
                .append(shortName(record.getLoggerName())).append(" - ")
                .append(formatMessage(record))
                .append(System.lineSeparator());
        if (record.getThrown() != null) {
            StringWriter trace = new StringWriter();
            record.getThrown().printStackTrace(new PrintWriter(trace));
            sb.append(trace);
        }
        return sb.toString();
    }

    private static String shortName(String loggerName) {
        if (loggerName == null || loggerName.isEmpty()) {
            return "root";
        }
        int dot = loggerName.lastIndexOf('.');
        return dot >= 0 ? loggerName.substring(dot + 1) : loggerName;
    }
}
