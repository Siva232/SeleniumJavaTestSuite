package com.demoproject.logging;

import com.demoproject.exceptions.FrameworkException;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.StreamHandler;

/**
 * Configures {@code java.util.logging}: console (stdout) plus a per-run log file, both using
 * {@link LogFormatter}. Idempotent; call once per JVM.
 */
public final class LoggingConfigurator {

    private static final String FRAMEWORK_PACKAGE = "com.demoproject";
    /** Strong references: JUL only holds loggers weakly, so configured levels could otherwise be lost. */
    private static final List<Logger> CONFIGURED = new ArrayList<>();
    private static boolean configured;

    private LoggingConfigurator() {
    }

    /**
     * @param logFile framework log file, e.g. {@code reports/Run_x/logs/execution.log}
     * @param level   level for framework loggers
     */
    public static synchronized void configure(Path logFile, Level level) {
        if (configured) {
            return;
        }
        Logger root = Logger.getLogger("");
        for (Handler handler : root.getHandlers()) {
            root.removeHandler(handler);
        }
        LogFormatter formatter = new LogFormatter();

        Handler console = new StdoutHandler(formatter);
        console.setLevel(level);
        root.addHandler(console);

        try {
            FileHandler file = new FileHandler(logFile.toString(), false);
            file.setEncoding(StandardCharsets.UTF_8.name());
            file.setFormatter(formatter);
            file.setLevel(Level.ALL);
            root.addHandler(file);
        } catch (IOException e) {
            throw new FrameworkException("Unable to create log file " + logFile, e);
        }

        root.setLevel(Level.INFO);
        setLevel(FRAMEWORK_PACKAGE, level);
        // Third-party libraries are chatty at INFO; keep only warnings from them.
        setLevel("org.openqa.selenium", Level.WARNING);
        setLevel("io.netty", Level.WARNING);
        setLevel("org.asynchttpclient", Level.WARNING);
        configured = true;
    }

    private static void setLevel(String loggerName, Level level) {
        Logger logger = Logger.getLogger(loggerName);
        logger.setLevel(level);
        CONFIGURED.add(logger);
    }

    /** Console handler writing to stdout (JUL's ConsoleHandler uses stderr) and flushing per record. */
    private static final class StdoutHandler extends StreamHandler {

        StdoutHandler(LogFormatter formatter) {
            super(new NonClosingStream(), formatter);
        }

        @Override
        public synchronized void publish(LogRecord record) {
            super.publish(record);
            flush();
        }
    }

    /** Prevents the handler from closing System.out. */
    private static final class NonClosingStream extends OutputStream {
        @Override
        public void write(int b) {
            System.out.write(b);
        }

        @Override
        public void write(byte[] b, int off, int len) {
            System.out.write(b, off, len);
        }

        @Override
        public void flush() {
            System.out.flush();
        }

        @Override
        public void close() {
            flush();
        }
    }
}
