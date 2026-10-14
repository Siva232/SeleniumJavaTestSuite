package com.demoproject.video;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;

/**
 * Writes a Motion-JPEG AVI (AVI 1.0 with an {@code idx1} index) using only the JDK, one frame at a
 * time (frames are streamed from disk, never all held in memory). Plays in Windows Media Player,
 * VLC and ffmpeg.
 * <p>
 * AVI has a constant frame rate, so each frame is repeated as often as needed to keep the
 * video's timeline aligned with the real capture times. Frames whose size differs from the
 * first frame (e.g. a different window was captured) are scaled to the first frame's size.
 */
public final class AviWriter {

    private static final float JPEG_QUALITY = 0.8f;
    private static final int AVIF_HASINDEX = 0x10;
    private static final int AVIIF_KEYFRAME = 0x10;
    private static final int INDEX_ENTRY_SIZE = 16;

    // Byte offsets of header fields patched once all frames are written.
    private static final int RIFF_SIZE_POS = 4;
    private static final int AVIH_MAX_BYTES_PER_SEC_POS = 36;
    private static final int AVIH_TOTAL_FRAMES_POS = 48;
    private static final int AVIH_BUFFER_SIZE_POS = 60;
    private static final int STRH_LENGTH_POS = 140;
    private static final int STRH_BUFFER_SIZE_POS = 144;
    private static final int MOVI_SIZE_POS = 216;
    private static final int MOVI_FOURCC_POS = 220;
    private static final int HEADER_SIZE = 224;

    private AviWriter() {
    }

    /**
     * A frame on disk and how long it is displayed.
     *
     * @param file    PNG frame
     * @param delayMs display duration in milliseconds
     */
    public record Frame(Path file, long delayMs) {
    }

    /**
     * @param frames          ordered frames
     * @param output          target .avi file
     * @param frameDurationMs duration of one video frame (the AVI frame rate is its inverse)
     * @throws IOException if a frame cannot be read or the AVI cannot be written
     */
    public static void write(List<Frame> frames, Path output, long frameDurationMs) throws IOException {
        long frameMs = Math.max(1, frameDurationMs);
        int width = 0;
        int height = 0;
        int totalFrames = 0;
        int maxChunk = 0;
        long elapsedMs = 0;
        ByteBuffer index = littleEndian(frames.size() * INDEX_ENTRY_SIZE);

        try (RandomAccessFile out = new RandomAccessFile(output.toFile(), "rw")) {
            out.setLength(0);
            out.seek(HEADER_SIZE);
            for (Frame frame : frames) {
                BufferedImage image = ImageIO.read(frame.file().toFile());
                if (image == null) {
                    continue;
                }
                if (width == 0) {
                    width = image.getWidth();
                    height = image.getHeight();
                }
                byte[] jpeg = toJpeg(fit(image, width, height));

                // Repeat the frame until the video timeline catches up with the real capture time.
                elapsedMs += frame.delayMs();
                int copies = (int) Math.max(1, Math.round(elapsedMs / (double) frameMs) - totalFrames);
                for (int i = 0; i < copies; i++) {
                    index = ensureCapacity(index, INDEX_ENTRY_SIZE);
                    index.put(fourcc("00dc")).putInt(AVIIF_KEYFRAME)
                            .putInt((int) (out.getFilePointer() - MOVI_FOURCC_POS)).putInt(jpeg.length);
                    writeChunk(out, "00dc", jpeg);
                    totalFrames++;
                }
                maxChunk = Math.max(maxChunk, jpeg.length);
            }
            if (totalFrames == 0) {
                throw new IOException("No readable frames to write");
            }

            long moviEnd = out.getFilePointer();
            out.write(fourcc("idx1"));
            out.write(littleEndian(4).putInt(index.position()).array());
            out.write(index.array(), 0, index.position());

            out.seek(0);
            out.write(header(width, height, frameMs));
            patch(out, RIFF_SIZE_POS, out.length() - 8);
            patch(out, MOVI_SIZE_POS, moviEnd - MOVI_FOURCC_POS);
            patch(out, AVIH_TOTAL_FRAMES_POS, totalFrames);
            patch(out, STRH_LENGTH_POS, totalFrames);
            patch(out, AVIH_BUFFER_SIZE_POS, maxChunk);
            patch(out, STRH_BUFFER_SIZE_POS, maxChunk);
            patch(out, AVIH_MAX_BYTES_PER_SEC_POS, maxChunk * 1000L / frameMs);
        }
    }

    /** RIFF/hdrl/movi header with placeholder sizes and counts (patched at the end). */
    private static byte[] header(int width, int height, long frameMs) {
        ByteBuffer b = littleEndian(HEADER_SIZE);
        b.put(fourcc("RIFF")).putInt(0).put(fourcc("AVI "));
        b.put(fourcc("LIST")).putInt(192).put(fourcc("hdrl"));

        b.put(fourcc("avih")).putInt(56);
        b.putInt((int) (frameMs * 1000)); // microseconds per frame
        b.putInt(0);                      // max bytes per second (patched)
        b.putInt(0);                      // padding granularity
        b.putInt(AVIF_HASINDEX);
        b.putInt(0);                      // total frames (patched)
        b.putInt(0);                      // initial frames
        b.putInt(1);                      // streams
        b.putInt(0);                      // suggested buffer size (patched)
        b.putInt(width).putInt(height);
        b.putInt(0).putInt(0).putInt(0).putInt(0);

        b.put(fourcc("LIST")).putInt(116).put(fourcc("strl"));
        b.put(fourcc("strh")).putInt(56);
        b.put(fourcc("vids")).put(fourcc("MJPG"));
        b.putInt(0);                      // flags
        b.putShort((short) 0).putShort((short) 0); // priority, language
        b.putInt(0);                      // initial frames
        b.putInt((int) frameMs).putInt(1000); // scale / rate = seconds per frame
        b.putInt(0);                      // start
        b.putInt(0);                      // length in frames (patched)
        b.putInt(0);                      // suggested buffer size (patched)
        b.putInt(-1);                     // quality: default
        b.putInt(0);                      // sample size: variable
        b.putShort((short) 0).putShort((short) 0).putShort((short) width).putShort((short) height);

        b.put(fourcc("strf")).putInt(40); // BITMAPINFOHEADER
        b.putInt(40).putInt(width).putInt(height);
        b.putShort((short) 1).putShort((short) 24);
        b.put(fourcc("MJPG"));
        b.putInt(width * height * 3);
        b.putInt(0).putInt(0).putInt(0).putInt(0);

        b.put(fourcc("LIST")).putInt(0).put(fourcc("movi"));
        return b.array();
    }

    private static void writeChunk(RandomAccessFile out, String id, byte[] data) throws IOException {
        out.write(fourcc(id));
        out.write(littleEndian(4).putInt(data.length).array());
        out.write(data);
        if ((data.length & 1) == 1) {
            out.write(0); // RIFF chunks are word-aligned
        }
    }

    private static void patch(RandomAccessFile out, long position, long value) throws IOException {
        out.seek(position);
        out.write(littleEndian(4).putInt((int) value).array());
    }

    private static byte[] toJpeg(BufferedImage image) throws IOException {
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpeg");
        if (!writers.hasNext()) {
            throw new IOException("No JPEG ImageWriter available in this JDK");
        }
        ImageWriter writer = writers.next();
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ImageOutputStream out = ImageIO.createImageOutputStream(bytes)) {
            writer.setOutput(out);
            ImageWriteParam param = writer.getDefaultWriteParam();
            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            param.setCompressionQuality(JPEG_QUALITY);
            writer.write(null, new IIOImage(image, null, null), param);
        } finally {
            writer.dispose();
        }
        return bytes.toByteArray();
    }

    /** Returns an RGB image of exactly {@code width} x {@code height} (JPEG has no alpha channel). */
    private static BufferedImage fit(BufferedImage source, int width, int height) {
        if (source.getWidth() == width && source.getHeight() == height
                && source.getType() == BufferedImage.TYPE_INT_RGB) {
            return source;
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

    private static ByteBuffer ensureCapacity(ByteBuffer buffer, int needed) {
        if (buffer.remaining() >= needed) {
            return buffer;
        }
        ByteBuffer bigger = littleEndian(Math.max(buffer.capacity() * 2, buffer.position() + needed));
        bigger.put(buffer.array(), 0, buffer.position());
        return bigger;
    }

    private static ByteBuffer littleEndian(int capacity) {
        return ByteBuffer.allocate(capacity).order(ByteOrder.LITTLE_ENDIAN);
    }

    private static byte[] fourcc(String code) {
        return code.getBytes(StandardCharsets.US_ASCII);
    }
}
