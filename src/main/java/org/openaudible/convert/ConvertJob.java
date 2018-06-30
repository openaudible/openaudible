package org.openaudible.convert;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openaudible.Audible;
import org.openaudible.Directories;
import org.openaudible.audible.AudibleUtils;
import org.openaudible.books.Book;
import org.openaudible.progress.IProgressTask;
import org.openaudible.util.DebugBuffer;
import org.openaudible.util.InputStreamReporter;
import org.openaudible.util.LineListener;
import org.openaudible.util.queues.IQueueJob;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

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
    final String duration;
    final static int mp3Qscale = 6;       // audio quality value 0 to 9. See https://trac.ffmpeg.org/wiki/Encode/MP3

    final DebugBuffer stdErr = new DebugBuffer();

    public ConvertJob(Book b) {
        book = b;
        aax = Audible.instance.getAAXFileDest(b);
        mp3 = Audible.instance.getMP3FileDest(b);
        image = Audible.instance.getImageFileDest(b);
        temp = new File(Directories.getTmpDir(), book.id() + "_temp.mp3");
        duration = book.getDuration();

        if (mp3.exists())
            mp3.delete();
        if (temp.exists())
            temp.delete();

    }

    public String toString() {
        return "convert " + book;
    }

    private String getExecutable() {
        return FFMPEG.getExecutable();
    }


    // take status from ffmpeg, example:
    // frame=    1 fps=0.0 q=0.0 size=       2kB time=06:17:11.44 bitrate=   0.0kbits/s
    @Override
    public void takeLine(String s) {


        String find = "time=";
        int ch = s.indexOf(find);
        if (ch != -1) {
            nextMeta = false;
            long now = System.currentTimeMillis();
            if (now > next) {
                next = now + interval;
                // interval*=2;
                // System.err.println(s);
                String time = s.substring(ch + find.length());
                int end = time.indexOf(".");
                if (end == -1) end = time.indexOf(" ");

                if (end == -1) end = time.length();
                time = time.substring(0, end);


                String status = time;
                if (duration.length() > 0)
                    status += " of " + duration;

                if (progress != null) {
                    progress.setTask(null, status.trim());
                }
            }

        } else
        {
            stdErr.accept(s);
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


    // convert to mp3.
    public void createMP3() throws IOException, InterruptedException {
        ArrayList<String> args = new ArrayList<>();
        args.add(getExecutable());

        args.add("-activation_bytes");
        args.add(getActivationBytes(aax));

        args.add("-i");
        args.add(aax.getAbsolutePath());
        args.add("-map_metadata");
        args.add("0");

        if (false) {

//            addTag(args, "copyright", book.getCopyright());
            addTag(args, "performer", book.getNarratedBy());
//            addTag(args, "publisher", book.getPublisher());
//            addTag(args, "lyrics", book.getSummary());
            String year = AudibleUtils.getYear(book);
            addTag(args, "year", year);
            String genre = book.getGenre();
            if (genre.isEmpty())
                genre = "Audiobook";
            // addTag(args, "genre", genre);


        }



        args.add("-codec:a");
        args.add("libmp3lame");     // see: https://trac.ffmpeg.org/wiki/Encode/MP3
        args.add("-qscale:a");      // https://trac.ffmpeg.org/wiki/Encode/MP3


        if (mp3Qscale < 0 || mp3Qscale > 9)
            throw new IOException("Invalid qscale:" + mp3Qscale);

        args.add("" + mp3Qscale);

        args.add(temp.getAbsolutePath());

        LOG.info("creating mp3: " + book + " " + temp.getAbsolutePath());
        String cli = "";
        for (String s : args) {
            if (s.contains(" "))
                cli += "\"" + s + "\"";
            else
                cli += s;
            cli += " ";
        }

        LOG.info(cli);

        ProcessBuilder pb = new ProcessBuilder(args);
        InputStreamReporter err;
        InputStream errStream = null;

        boolean success = false;

        try {

            proc = pb.start();

            errStream = proc.getErrorStream();
            err = new InputStreamReporter("err: ", errStream, this);
            err.start();

            err.finish();
            while (!proc.waitFor(1, TimeUnit.SECONDS)) {
                if (quit)
                    throw new IOException("conversion quit");
            }


            int exitValue = proc.exitValue();

            LOG.info("createMP3:" + exitValue);
            if (exitValue != 0) {

                LOG.error(stdErr.toString());

                throw new IOException("Conversion got non-zero response:" + exitValue);
            }

            success = true;

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

            errStream.close();
            if (!success) {
                proc.destroy();
                temp.delete();
            }

        }
    }

    private void addTag(ArrayList<String> args, String key, String value) {
        if (!value.isEmpty()) {
            args.add("-metadata");
            args.add("key="+key);
            value = value.replace("\"", "");

            args.add(value);

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



    public void renameMP3() throws IOException {
        boolean ok = temp.renameTo(mp3);
        if (!ok) {
            mp3.delete();
            Files.copy(temp.toPath(), mp3.toPath());
            temp.delete();
            if (!mp3.exists())
                throw new IOException("Error renaming: " + temp.getAbsolutePath() + " size ["+temp.length()+"] to " + mp3.getAbsolutePath()+" mp3 exists="+mp3.exists());
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
                progress.setTask(null, "Finalizing MP3");
            renameMP3();
            ok = true;
            if (progress != null)
                progress.setTask(null, "Complete");
        } catch (Exception e) {
            LOG.error("Error converting book:"+book, e);
            if (progress != null) {
                progress.setSubTask(e.getMessage());
            }
            throw e;
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
