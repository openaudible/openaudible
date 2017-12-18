package org.jaudiotagger.test;

import java.io.*;
import java.text.DateFormat;
import java.util.Date;

/**
 * Simple class that will attempt to recusively read all files within a directory ending in .mp3 (but
 * only actually expected to contain id3 tags), merge them with a given mp3 and write them to the provided
 * output folder (which must already exist)
 */
public class MergeID3AndMP3Files {

    private static int count = 0;
    private static int failed = 0;

    public static void main(final String[] args) {
        MergeID3AndMP3Files test = new MergeID3AndMP3Files();

        if (args.length == 0) {
            System.err.println("usage MergeID3AndMP3Files FromDir ToDir mp3File");
            System.err.println("      You must enter the from dir,outputdir and the mp3file to append");
            System.exit(1);
        } else if (args.length != 3) {
            System.err.println("usage MergeID3AndMP3Files FromDir ToDir mp3File");
            System.err.println("      Only three parameters accepted");
            System.exit(1);
        }
        File rootDir = new File(args[0]);
        if (!rootDir.isDirectory()) {
            System.err.println("usage MergeID3AndMP3Files FromDir ToDir mp3File");
            System.err.println("      Directory " + args[0] + " could not be found");
            System.exit(1);
        }


        File toDir = new File(args[1]);
        if (!rootDir.isDirectory()) {
            System.err.println("usage MergeID3AndMP3Files FromDir ToDir mp3File");
            System.err.println("      Directory " + args[1] + " could not be found");
            System.exit(1);
        }

        File mp3File = new File(args[2]);
        if (!mp3File.isFile()) {
            System.err.println("usage MergeID3AndMP3Files FromDir ToDir mp3File");
            System.err.println("      Mp3File " + args[2] + " could not be found");
            System.exit(1);
        }

        Date start = new Date();
        System.out.println("Started to merge from:" + rootDir.getPath() + " at " + DateFormat.getTimeInstance().format(start));
        test.scanSingleDir(rootDir, toDir, mp3File);
        Date finish = new Date();
        System.out.println("Finished to merge from:" + rootDir.getPath() + DateFormat.getTimeInstance().format(finish));
        System.out.println("Attempted  to merge:" + MergeID3AndMP3Files.count);
        System.out.println("Successful to merge:" + (MergeID3AndMP3Files.count - MergeID3AndMP3Files.failed));
        System.out.println("Failed     to merge:" + MergeID3AndMP3Files.failed);

    }

    public static File copyAudioToTmp(File toDir, File tagFile, File mp3File) {
        File outputFile = new File(toDir.getPath(), tagFile.getName());
        boolean result = append(tagFile, mp3File, outputFile);
        return outputFile;
    }

    private static boolean append(File fromFile1, File fromFile2, File toFile) {
        try {
            FileInputStream in = new FileInputStream(fromFile1);
            FileInputStream in2 = new FileInputStream(fromFile2);

            toFile.getParentFile().mkdirs();
            FileOutputStream out = new FileOutputStream(toFile);
            BufferedInputStream inBuffer = new BufferedInputStream(in);
            BufferedInputStream inBuffer2 = new BufferedInputStream(in2);
            BufferedOutputStream outBuffer = new BufferedOutputStream(out);

            int theByte;

            while ((theByte = inBuffer.read()) > -1) {
                outBuffer.write(theByte);
            }

            while ((theByte = inBuffer2.read()) > -1) {
                outBuffer.write(theByte);
            }

            outBuffer.close();
            inBuffer.close();
            inBuffer2.close();
            out.close();
            in.close();
            in2.close();

            // cleanupif files are not the same length
            if ((fromFile1.length() + fromFile2.length()) != toFile.length()) {
                toFile.delete();

                return false;
            }

            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Recursive function to scan directory
     *
     * @param fromDir
     * @param toDir
     * @param mp3File
     */
    private void scanSingleDir(final File fromDir, final File toDir, final File mp3File) {

        final File[] audioFiles = fromDir.listFiles(new MergeID3AndMP3Files.MP3FileFilter());
        if (audioFiles.length > 0) {
            for (File audioFile : audioFiles) {
                MergeID3AndMP3Files.count++;

                try {
                    copyAudioToTmp(toDir, audioFile, mp3File);
                } catch (Throwable t) {
                    System.err.println("Unable to merge record:" + MergeID3AndMP3Files.count + ":" + mp3File.getPath());
                    MergeID3AndMP3Files.failed++;
                    t.printStackTrace();
                }
            }
        }

        final File[] audioFileDirs = fromDir.listFiles(new MergeID3AndMP3Files.DirFilter());
        if (audioFileDirs.length > 0) {
            for (File audioFileDir : audioFileDirs) {
                scanSingleDir(audioFileDir, new File(toDir, audioFileDir.getName()), mp3File);
            }
        }
    }

    final class MP3FileFilter extends javax.swing.filechooser.FileFilter implements java.io.FileFilter {

        /**
         * allows Directories
         */
        private final boolean allowDirectories;

        /**
         * Create a default MP3FileFilter.  The allowDirectories field will
         * default to false.
         */
        public MP3FileFilter() {
            this(false);
        }

        /**
         * Create an MP3FileFilter.  If allowDirectories is true, then this filter
         * will accept directories as well as mp3 files.  If it is false then
         * only mp3 files will be accepted.
         *
         * @param allowDirectories whether or not to accept directories
         */
        private MP3FileFilter(final boolean allowDirectories) {
            this.allowDirectories = allowDirectories;
        }

        /**
         * Determines whether or not the file is an mp3 file.  If the file is
         * a directory, whether or not is accepted depends upon the
         * allowDirectories flag passed to the constructor.
         *
         * @param file the file to test
         * @return true if this file or directory should be accepted
         */
        public final boolean accept(final File file) {
            return (((file.getName()).toLowerCase().endsWith(".mp3")) || (file.isDirectory() && (this.allowDirectories)));
        }

        /**
         * Returns the Name of the Filter for use in the Chooser Dialog
         *
         * @return The Description of the Filter
         */
        public final String getDescription() {
            return ".mp3 Files";
        }
    }

    public final class DirFilter implements java.io.FileFilter {
        public static final String IDENT = "$Id: MergeID3AndMP3Files.java 836 2009-11-12 15:44:07Z paultaylor $";


        public DirFilter() {

        }

        /**
         * Determines whether or not the file is an mp3 file.  If the file is
         * a directory, whether or not is accepted depends upon the
         * allowDirectories flag passed to the constructor.
         *
         * @param file the file to test
         * @return true if this file or directory should be accepted
         */
        public final boolean accept(final File file) {
            return file.isDirectory();
        }
    }
}
