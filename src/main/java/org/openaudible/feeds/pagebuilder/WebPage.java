package org.openaudible.feeds.pagebuilder;

import com.google.gson.Gson;
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jetty.util.IO;
import org.openaudible.Audible;
import org.openaudible.BookToFilenameStrategy;
import org.openaudible.Directories;
import org.openaudible.books.Book;
import org.openaudible.books.BookElement;
import org.openaudible.progress.IProgressTask;
import org.openaudible.util.CopyWithProgress;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class WebPage {
    private static final Log LOG = LogFactory.getLog(WebPage.class);
    final File webDir;
    final IProgressTask progress;   // required
    int thumbSize = 200; // If changed, need to change html
    final boolean includeMP3;

    public WebPage(File dir, IProgressTask t, boolean includeMP3) {
        webDir = dir;
        progress = t;
        this.includeMP3 = includeMP3;
        assert (t != null);
    }

    BookInfo toBookInfo(Book b) {
        BookInfo i = new BookInfo();
        i.title = b.get(BookElement.fullTitle);
        i.author = b.get(BookElement.author);
        i.narrated_by = b.get(BookElement.narratedBy);
        i.summary = b.get(BookElement.summary);
        i.duration = b.getDurationHHMM();
        i.rating_average = b.get(BookElement.rating_average);
        i.rating_count = b.get(BookElement.rating_count);

        i.link_url = b.getInfoLink();

        i.description = b.get(BookElement.description);
        i.purchase_date = b.getPurchaseDateSortable();
        i.release_date = b.getReleaseDateSortable();
        return i;
    }


    public void subtask(Book b, String s) throws Exception {
        String n = b.getShortTitle();
        if (n.length() > 32)
            n = n.substring(0, 28) + "...";
        progress.setSubTask(s + " " + n);
        if (progress.wasCanceled())
            throw new Exception("User canceled");
    }

    public void buildPage(List<Book> books) throws Exception {

        File mp3Dir = new File(webDir, "mp3");
        File coverImages = new File(webDir, "cover");
        File thumbImages = new File(webDir, "thumb");

        if (!coverImages.exists())
            coverImages.mkdirs();
        if (!thumbImages.exists())
            thumbImages.mkdirs();
        ArrayList<BookInfo> list = new ArrayList<>();

        if (includeMP3) {
            if (!mp3Dir.exists())
                mp3Dir.mkdirs();

            progress.setTask("Copying MP3s to Web Page Directory", "");

            ArrayList<Book> toCopy = new ArrayList<>();
            for (Book b : books) {
                File mp3 = Audible.instance.getMP3FileDest(b);
                if (!mp3.exists())
                    continue;
                String fileName = getFileName(b); // human readable, without extension.
                String mp3Name = fileName + ".mp3";
                File mp3File = new File(mp3Dir, mp3Name);

                if (!mp3File.exists()) {
                    toCopy.add(b);
                } else {
                    long s1 = mp3File.length();
                    long s2 = mp3.length();
                    long m1 = mp3File.lastModified();
                    long m2 = mp3.lastModified();

                    if (s1 != s2) {
                        String d1 = m1 != 0 ? new Date(m1).toString() : "0";
                        String d2 = m2 != 0 ? new Date(m2).toString() : "0";
                        LOG.info("Replacing book " + mp3.getPath() + " with " + mp3File.getPath() + " s1=" + s1 + " s2=" + s2 + " d1=" + d1 + " d2=" + d2);
                        boolean ok = mp3.delete();
                        if (ok)
                            toCopy.add(b);
                    }
                }
            }

            if (toCopy.size() > 0) {
                int count = 1;
                for (Book b : toCopy) {
                    if (progress.wasCanceled())
                        throw new Exception("Canceled");

                    File mp3 = Audible.instance.getMP3FileDest(b);
                    String fileName = getFileName(b); // human readable, without extension.
                    String mp3Name = fileName + ".mp3";
                    File mp3File = new File(mp3Dir, mp3Name);
                    progress.setTask("Copying book " + count + " of " + toCopy.size() + " to " + mp3File.getAbsolutePath());

                    try {
                        CopyWithProgress.copyWithProgress(progress, mp3, mp3File);
                    } catch(Throwable th)
                    {
                        LOG.error("error copying mp3:"+mp3.getAbsolutePath()+" to "+mp3File.getAbsolutePath()+" for book "+b);
                    }
                    count++;
                }
            }
        }

        progress.setTask("Creating Book Web Page", "");

        for (Book b : books) {
            File mp3 = Audible.instance.getMP3FileDest(b);

            subtask(b, "Compiling book list");


                // only export mp3
            if (includeMP3 && !mp3.exists())
                continue;

            File img = Audible.instance.getImageFileDest(b);
            String fileName = getFileName(b); // human readable, without extension.

            String coverName = fileName + ".jpg";
            String thumbName = fileName + ".jpg";
            String mp3Name = fileName + ".mp3";

            File coverFile = new File(coverImages, coverName);
            File thumbFile = new File(thumbImages, thumbName);

            BookInfo i = toBookInfo(b);
            if (includeMP3)
                i.mp3 = mp3Name;


            if (img.exists()) {
                if (!coverFile.exists() || coverFile.length() != img.length()) {
                    subtask(b, "Copying image");
                    IO.copy(img, coverFile);
                }

                if (!thumbFile.exists()) {
                    subtask(b, "Creating thumbnail");
                    createThumbnail(img, thumbFile, thumbSize);
                }

                i.image = coverName;
            } else
                i.image = "";

            list.add(i);
            if (progress.wasCanceled())
                throw new Exception("User canceled");

        }

        progress.setTask(null, "Exporting web data");

        Gson gson = new Gson();
        String json = gson.toJson(list);

        try (FileWriter writer = new FileWriter(new File(webDir, "books.json"))) {
            writer.write(json);
        }

        try (FileWriter writer = new FileWriter(new File(webDir, "books.js"))) {
            writer.write("window.myBooks=");
            writer.write(json);
            writer.write(";");
        }

        // add basic html pages from webapp directory: src/main/webapp which is at the root dir in installed app.
        File templateDir = Directories.getWebTemplateDirectory();
        assert (templateDir.exists());
        FileUtils.copyDirectory(templateDir, webDir);
    }


    private String getFileName(Book b) {
        String s = BookToFilenameStrategy.instance.getReadableFileName(b);
        return s;
    }

    private void createThumbnail(File srcFile, File destFile, int siz) throws IOException {
        BufferedImage img = new BufferedImage(siz, siz, BufferedImage.TYPE_INT_RGB);
        img.createGraphics().drawImage(ImageIO.read(srcFile).getScaledInstance(siz, siz, Image.SCALE_SMOOTH), 0, 0, null);
        ImageIO.write(img, "jpg", destFile);
    }


}

