package org.jaudiotagger.tag.images;

import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * BufferedImage methods
 * <p>
 * Not compatible with Android, delete from your source tree.
 */
public class Images {
    public static BufferedImage getImage(Artwork artwork) throws IOException {
        return (BufferedImage) artwork.getImage();
    }
}
