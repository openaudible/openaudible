package org.openaudible.convert;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jetty.util.IO;
import org.openaudible.Audible;
import org.openaudible.Directories;
import org.openaudible.books.Book;
import org.openaudible.progress.IProgressTask;
import org.openaudible.util.InputStreamReporter;
import org.openaudible.util.LineListener;
import org.openaudible.util.queues.IQueueJob;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class ConvertJob implements IQueueJob, LineListener {
    private static final Log LOG = LogFactory.getLog(ConvertJob.class);
    final Book book;
    final File aax, mp3, image, temp;
    volatile boolean quit = false;
    ArrayList<String> metaData = new ArrayList<>();
    boolean nextMeta = false;
    long next = 0;
    long interval = 1000;
    private Process proc = null;
    private IProgressTask progress;

    public ConvertJob(Book b) {
        book = b;
        aax = Audible.instance.getAAXFileDest(b);
        mp3 = Audible.instance.getMP3FileDest(b);
        image = Audible.instance.getImageFileDest(b);
        temp = new File(Directories.getTmpDir(), book.id() + "_temp.mp3");

        if (mp3.exists())
            mp3.delete();
        if (temp.exists())
            temp.delete();

    }

    public String toString() {
        return "convert " + book;
    }

    private String getExecutable() throws IOException {
        return FFMPEG.getExecutable();
    }

    @Override
    public void takeLine(String s) throws Exception {
        if (s.contains("time=")) {
            nextMeta = false;
            long now = System.currentTimeMillis();
            if (now > next) {
                next = now + interval;
                // interval*=2;
                // System.err.println(s);

                if (progress != null) {
                    progress.setTask(null, s);
                }
            }

        } else {
            System.err.println(s);
        }

        String endMeta[] = {"Stream mapping:", "Press [q]"};
        for (String r : endMeta) {
            if (s.contains(r))
                nextMeta = false;
        }

        if (nextMeta) {
            s = s.trim();
            metaData.add(s);
        } else {
            nextMeta = s.contains("Metadata:");
        }
    }



    public void createMP3() throws IOException, InterruptedException {
        ArrayList<String> args = new ArrayList<>();
        args.add(getExecutable());

        args.add("-activation_bytes");
        args.add(getActivationBytes(aax));

        args.add("-i");
        args.add(aax.getAbsolutePath());
        args.add("-map_metadata");
        args.add("0");

        args.add("-codec:a");
        args.add("libmp3lame");

        args.add("-qscale:a");
        args.add("6");

        args.add("-f");
        args.add("mp3");

        args.add("-");

        System.err.println("creating mp3: " + book + " " + temp.getAbsolutePath());
        String cli = "";
        for (String s : args) {
            if (s.contains(" "))
                cli += "\"" + s + "\"";
            else
                cli += s;
            cli += " ";
        }

        System.err.println(cli);

        ProcessBuilder pb = new ProcessBuilder(args);
        InputStreamReporter err = null;

        InputStream is = null;
        FileOutputStream fos = null;

        boolean success = false;

        try {

            fos = new FileOutputStream(temp);
            proc = pb.start();
            is = proc.getInputStream();

            err = new InputStreamReporter("err: ", proc.getErrorStream(), this);
            err.start();
            IO.copy(is, fos);

            success = true;

            err.finish();

            if (!temp.exists() || temp.length() < 1024) {
                String msg = err.getLastLine();
                throw new IOException(msg + " : Unable to create mp3 for " + book);
            }
            if (quit)
                throw new IOException("Conversion canceled");

            for (String m : metaData) {
                System.err.println(m);
            }

        } finally {

            if (is != null)
                is.close();
            if (fos != null)
                fos.close();

            if (!success) {
                temp.delete();
            }
        }
    }

    private synchronized String getActivationBytes(File aaxFile) throws IOException, InterruptedException {
        // synchronized -- if two files have same hash, only do lookup once.

        return LookupKey.instance.getKeyFromAAX(aaxFile);
        /*
        String hash = LookupKey.instance.getFileChecksum(aaxFile.getAbsolutePath());
        String out = KeyCache.instance.get(hash);
        // String out = Audible.instance.getActivationBytes();
        if (out == null)
        {
            out = LookupKey.instance.getKeyFromAAX(aaxFile);
            KeyCache.instance.add(hash, out);
            KeyCache.instance.save();
        }
        return out;*/
    }


    public void tagMP3() throws Exception {
        TagMP3AudioBook.setMP3Tags(book, temp, image);
    }

    public void renameMP3() throws IOException {
        boolean ok = temp.renameTo(mp3);
        if (!ok) {
            throw new IOException("Error renaming: " + temp.getAbsolutePath() + " to " + mp3.getAbsolutePath());
        }
    }

    public File convert() throws Exception {
        long start = System.currentTimeMillis();

        boolean ok = false;
        try {
            if (progress != null)
                progress.setTask("Creating MP3 " + book, "");
            createMP3();
            if (progress != null)
                progress.setTask(null, "Adding tags");
            tagMP3();
            if (progress != null)
                progress.setTask(null, "Finalizing MP3");
            renameMP3();
            ok = true;
            if (progress != null)
                progress.setTask(null, "Complete");
        } finally {
            if (!ok) {
                if (temp.exists())
                    temp.delete();
                if (mp3.exists())
                    mp3.delete();
            }
        }
        long time = System.currentTimeMillis() - start;

        LOG.info("converted " + mp3.getAbsolutePath() + " " + (int) time / 1000 + " seconds.");
        return mp3;
    }

    @Override
    public void quitJob() {
        quit = true;
        if (proc != null)
            proc.destroy();
    }

    @Override
    public void processJob() throws Exception {
        convert();
    }

    public IProgressTask getProgress() {
        return progress;
    }

    public void setProgress(IProgressTask progress) {
        this.progress = progress;
    }

}
