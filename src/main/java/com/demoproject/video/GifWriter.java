package com.demoproject.video;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;

/**
 * Writes an animated GIF with the JDK's {@code javax.imageio} GIF writer, one frame at a time
 * (frames are streamed from disk, never all held in memory).
 * <p>
 * No NETSCAPE2.0 looping extension is written, so the animation plays exactly once.
 */
public final class GifWriter {

    private static final String GIF_METADATA_FORMAT = "javax_imageio_gif_image_1.0";
    private static final int MIN_DELAY_CS = 2;

    private GifWriter() {
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
     * @param frames ordered frames
     * @param output target .gif file
     * @throws IOException if a frame cannot be read or the GIF cannot be written
     */
    public static void write(List<Frame> frames, Path output) throws IOException {
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("gif");
        if (!writers.hasNext()) {
            throw new IOException("No GIF ImageWriter available in this JDK");
        }
        ImageWriter writer = writers.next();
        try (ImageOutputStream out = ImageIO.createImageOutputStream(output.toFile())) {
            writer.setOutput(out);
            ImageWriteParam param = writer.getDefaultWriteParam();
            writer.prepareWriteSequence(null);
            for (Frame frame : frames) {
                BufferedImage image = ImageIO.read(frame.file().toFile());
                if (image == null) {
                    continue;
                }
                IIOMetadata metadata = frameMetadata(writer, param, frame.delayMs());
                writer.writeToSequence(new IIOImage(image, null, metadata), param);
            }
            writer.endWriteSequence();
        } finally {
            writer.dispose();
        }
    }

    private static IIOMetadata frameMetadata(ImageWriter writer, ImageWriteParam param, long delayMs)
            throws IOException {
        ImageTypeSpecifier type = ImageTypeSpecifier.createFromBufferedImageType(BufferedImage.TYPE_INT_RGB);
        IIOMetadata metadata = writer.getDefaultImageMetadata(type, param);
        IIOMetadataNode root = (IIOMetadataNode) metadata.getAsTree(GIF_METADATA_FORMAT);

        IIOMetadataNode control = child(root, "GraphicControlExtension");
        control.setAttribute("disposalMethod", "none");
        control.setAttribute("userInputFlag", "FALSE");
        control.setAttribute("transparentColorFlag", "FALSE");
        control.setAttribute("transparentColorIndex", "0");
        // GIF delays are in hundredths of a second.
        control.setAttribute("delayTime", String.valueOf(Math.max(MIN_DELAY_CS, delayMs / 10)));

        metadata.setFromTree(GIF_METADATA_FORMAT, root);
        return metadata;
    }

    private static IIOMetadataNode child(IIOMetadataNode root, String name) {
        for (int i = 0; i < root.getLength(); i++) {
            if (root.item(i).getNodeName().equalsIgnoreCase(name)) {
                return (IIOMetadataNode) root.item(i);
            }
        }
        IIOMetadataNode node = new IIOMetadataNode(name);
        root.appendChild(node);
        return node;
    }
}
