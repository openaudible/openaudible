package org.jaudiotagger.tag.images;

import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * Image Handling to to use when running on Android
 * <p>
 * TODO need to provide Android compatible implementations
 */
public class AndroidImageHandler implements ImageHandler {
    private static AndroidImageHandler instance;

    private AndroidImageHandler() {

    }

    public static AndroidImageHandler getInstanceOf() {
        if (instance == null) {
            instance = new AndroidImageHandler();
        }
        return instance;
    }

    /**
     * Resize the image until the total size require to store the image is less than maxsize
     *
     * @param artwork
     * @param maxSize
     * @throws IOException
     */
    public void reduceQuality(Artwork artwork, int maxSize) throws IOException {
        throw new UnsupportedOperationException();
    }

    /**
     * Resize image using Java 2D
     *
     * @param artwork
     * @param size
     * @throws java.io.IOException
     */
    public void makeSmaller(Artwork artwork, int size) throws IOException {
        throw new UnsupportedOperationException();
    }

    public boolean isMimeTypeWritable(String mimeType) {
        throw new UnsupportedOperationException();
    }

    /**
     * Write buffered image as required format
     *
     * @param bi
     * @param mimeType
     * @return
     * @throws IOException
     */
    public byte[] writeImage(BufferedImage bi, String mimeType) throws IOException {
        throw new UnsupportedOperationException();
    }

    /**
     * @param bi
     * @return
     * @throws IOException
     */
    public byte[] writeImageAsPng(BufferedImage bi) throws IOException {
        throw new UnsupportedOperationException();
    }

    /**
     * Show read formats
     * <p>
     * On Windows supports png/jpeg/bmp/gif
     */
    public void showReadFormats() {
        throw new UnsupportedOperationException();
    }

    /**
     * Show write formats
     * <p>
     * On Windows supports png/jpeg/bmp
     */
    public void showWriteFormats() {
        throw new UnsupportedOperationException();
    }
}
