package org.jaudiotagger.tag.images;

import org.jaudiotagger.audio.flac.metadatablock.MetadataBlockDataPicture;

import java.io.File;
import java.io.IOException;

/**
 * Represents artwork in a format independent  way
 */
public interface Artwork {
    byte[] getBinaryData();


    void setBinaryData(byte[] binaryData);

    String getMimeType();

    void setMimeType(String mimeType);

    String getDescription();

    void setDescription(String description);

    int getHeight();

    void setHeight(int height);

    int getWidth();

    void setWidth(int width);

    /**
     * Should be called when you wish to prime the artwork for saving
     *
     * @return
     */
    boolean setImageFromData();

    Object getImage() throws IOException;

    boolean isLinked();

    void setLinked(boolean linked);

    String getImageUrl();

    void setImageUrl(String imageUrl);

    int getPictureType();

    void setPictureType(int pictureType);

    /**
     * Create Artwork from File
     *
     * @param file
     * @throws IOException
     */
    void setFromFile(File file) throws IOException;

    /**
     * Populate Artwork from MetadataBlockDataPicture as used by Flac and VorbisComment
     *
     * @param coverArt
     */
    void setFromMetadataBlockDataPicture(MetadataBlockDataPicture coverArt);
}
