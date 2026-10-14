package com.demoproject.video;

import com.demoproject.utils.FileHelper;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 * Records one browser session as an animated GIF using only the JDK.
 * <p>
 * A single-thread {@link ScheduledExecutorService} captures {@link TakesScreenshot} frames of the
 * given driver, waiting {@code intervalMs} between captures, downscales them to {@code maxWidth} and streams them to a
 * temporary folder as PNGs (file names carry the capture time so GIF frame delays match real time).
 * {@link #stop(Path)} stitches them into a GIF or discards them. Capture errors (e.g. an alert is
 * open or a window is closing) are logged and never propagate to the test.
 */
public class VideoRecorder {

    private static final Logger LOG = Logger.getLogger(VideoRecorder.class.getName());
    private static final long SHUTDOWN_WAIT_SECONDS = 10;

    private final WebDriver driver;
    private final long intervalMs;
    private final int maxWidth;
    private final String ownerThread;
    private final AtomicInteger frameCount = new AtomicInteger();
    private final AtomicBoolean warnedOnce = new AtomicBoolean();
    private ScheduledExecutorService executor;
    private Path framesDir;

    /**
     * @param driver     the browser of the thread being recorded (captured once, used from the recorder thread)
     * @param intervalMs time between frames
     * @param maxWidth   frames wider than this are scaled down
     */
    public VideoRecorder(WebDriver driver, long intervalMs, int maxWidth) {
        this.driver = driver;
        this.intervalMs = intervalMs;
        this.maxWidth = maxWidth;
        this.ownerThread = Thread.currentThread().getName();
    }

    /**
     * Creates the temp frames folder and starts capturing.
     *
     * @throws IOException if the temp folder cannot be created
     */
    public void start() throws IOException {
        framesDir = Files.createTempDirectory("demoproject-frames-");
        executor = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable, "video-" + ownerThread);
            thread.setDaemon(true);
            return thread;
        });
        // Fixed delay (not fixed rate): a screenshot can take longer than the interval, and back-to-back
        // captures would queue ahead of the test's own WebDriver commands and slow the test down.
        executor.scheduleWithFixedDelay(this::captureFrame, 0, intervalMs, TimeUnit.MILLISECONDS);
        LOG.fine(() -> "Recording started, frames in " + framesDir);
    }

    /**
     * Stops capturing. If {@code target} is non-null and frames exist, writes the GIF there.
     * Temp frames are always deleted.
     *
     * @param target GIF file to create, or null to discard the recording
     * @return true if a GIF was written
     */
    public boolean stop(Path target) {
        try {
            shutdown(executor);
            if (target == null || frameCount.get() == 0) {
                return false;
            }
            GifWriter.write(collectFrames(), target);
            LOG.info(() -> "Recording saved: " + target.getFileName() + " (" + frameCount.get() + " frames)");
            return true;
        } catch (IOException | RuntimeException e) {
            LOG.warning(() -> "Unable to write recording " + target + ": " + e.getMessage());
            return false;
        } finally {
            FileHelper.deleteRecursively(framesDir);
        }
    }

    private void captureFrame() {
        try {
            byte[] png = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
            BufferedImage frame = toScaledRgb(ImageIO.read(new ByteArrayInputStream(png)));
            if (frame == null) {
                return;
            }
            int index = frameCount.getAndIncrement();
            Path file = framesDir.resolve(String.format("frame_%06d_%d.png", index, System.currentTimeMillis()));
            ImageIO.write(frame, "png", file.toFile());
        } catch (WebDriverException | IOException e) {
            // Expected while an alert is open or a window is closing; warn once, then stay quiet.
            Level level = warnedOnce.compareAndSet(false, true) ? Level.WARNING : Level.FINE;
            LOG.log(level, () -> "Video frame skipped: " + firstLine(e.getMessage()));
        } catch (RuntimeException e) {
            LOG.log(Level.WARNING, "Unexpected error capturing video frame", e);
        }
    }

    private List<GifWriter.Frame> collectFrames() throws IOException {
        List<Path> files;
        try (Stream<Path> stream = Files.list(framesDir)) {
            files = stream.filter(p -> p.getFileName().toString().endsWith(".png")).sorted().toList();
        }
        List<GifWriter.Frame> frames = new ArrayList<>(files.size());
        for (int i = 0; i < files.size(); i++) {
            long delay = i + 1 < files.size()
                    ? captureTime(files.get(i + 1)) - captureTime(files.get(i))
                    : intervalMs;
            frames.add(new GifWriter.Frame(files.get(i), delay > 0 ? delay : intervalMs));
        }
        return frames;
    }

    private static long captureTime(Path frame) {
        String name = frame.getFileName().toString();
        return Long.parseLong(name.substring(name.lastIndexOf('_') + 1, name.length() - ".png".length()));
    }

    private BufferedImage toScaledRgb(BufferedImage source) {
        if (source == null) {
            return null;
        }
        int width = source.getWidth();
        int height = source.getHeight();
        if (width > maxWidth) {
            height = (int) Math.round(height * (maxWidth / (double) width));
            width = maxWidth;
        }
        BufferedImage target = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = target.createGraphics();
        try {
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.drawImage(source, 0, 0, width, height, null);
        } finally {
            g.dispose();
        }
        return target;
    }

    private static void shutdown(ExecutorService executor) {
        if (executor == null) {
            return;
        }
        executor.shutdown();
        try {
            if (!executor.awaitTermination(SHUTDOWN_WAIT_SECONDS, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    private static String firstLine(String message) {
        return message == null ? "" : message.lines().findFirst().orElse("");
    }
}
