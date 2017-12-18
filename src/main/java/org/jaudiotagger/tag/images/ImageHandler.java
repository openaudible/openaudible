package org.jaudiotagger.tag.images;

import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * Image Handler
 */
public interface ImageHandler {
    void reduceQuality(Artwork artwork, int maxSize) throws IOException;

    void makeSmaller(Artwork artwork, int size) throws IOException;

    boolean isMimeTypeWritable(String mimeType);

    byte[] writeImage(BufferedImage bi, String mimeType) throws IOException;

    byte[] writeImageAsPng(BufferedImage bi) throws IOException;

    void showReadFormats();

    void showWriteFormats();
}
