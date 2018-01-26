package org.openaudible.convert;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.audio.mp3.MP3File;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagException;
import org.jaudiotagger.tag.id3.ID3v24FieldKey;
import org.jaudiotagger.tag.id3.ID3v24Tag;
import org.openaudible.Audible;
import org.openaudible.audible.AudibleUtils;
import org.openaudible.books.Book;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class TagMP3AudioBook {
    private static final Log LOG = LogFactory.getLog(TagMP3AudioBook.class);

    public static void retag(Book b) throws Exception {
        File mp3 = Audible.instance.getMP3FileDest(b);
        File img = Audible.instance.getImageFileDest(b);
        setMP3Tags(b, mp3, img);
    }

    // replaces any existing mp3 tags with new tags from the book meta data.
    public static void setMP3Tags(Book book, File bookMP3File, File imageFile) throws Exception {
        MP3File mp3file = new MP3File(bookMP3File.getAbsolutePath());
        ID3v24Tag tag = new ID3v24Tag();
        mp3file.setID3v2Tag(tag);

        tag.addField(tag.createField(ID3v24FieldKey.GENRE, book.getGenre()));
        tag.addField(tag.createField(ID3v24FieldKey.ARTIST, book.getAuthor()));
        tag.addField(tag.createField(ID3v24FieldKey.COMPOSER, book.getNarratedBy()));
        tag.addField(tag.createField(ID3v24FieldKey.TITLE, book.getFullTitle()));
        tag.addField(tag.createField(ID3v24FieldKey.AMAZON_ID, book.getAsin()));
        tag.addField(tag.createField(ID3v24FieldKey.URL_OFFICIAL_RELEASE_SITE, "https://www.audible.com" + book.getInfoLink()));
        tag.addField(tag.createField(ID3v24FieldKey.RECORD_LABEL, book.getPublisher()));
        tag.addField(tag.createField(ID3v24FieldKey.COMMENT, book.getSummary()));
        tag.addField(tag.createField(ID3v24FieldKey.CATALOG_NO, book.getProduct_id()));
        tag.addField(tag.createField(ID3v24FieldKey.ENCODER, book.getCopyright()));

        tag.addField(tag.createField(ID3v24FieldKey.ALBUM, book.getShortTitle()));
        tag.addField(tag.createField(ID3v24FieldKey.YEAR, AudibleUtils.getYear(book)));

        if (imageFile.exists()) {
            byte imageBytes[] = Files.readAllBytes(Paths.get(imageFile.getAbsolutePath()));
            tag.addField(tag.createArtworkField(imageBytes, "image/jpeg"));
        }
        mp3file.commit();
        mp3file.save();
    }

    public static void debugTags(File f) throws CannotReadException, IOException, TagException, ReadOnlyFileException, InvalidAudioFrameException {
        AudioFile audiofile = AudioFileIO.read(f);
        Tag tag = audiofile.getTag();
        LOG.info("" + audiofile + ", tag=" + tag);
    }

}
