package org.openaudible.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

public class SimpleProcess {
    private static final Log LOG = LogFactory.getLog(SimpleProcess.class);
    List<String> command;
    volatile boolean quit = false;
    volatile boolean completed = false;
    Process proc = null;
    ProcessBuilder pb;

    public SimpleProcess(final List<String> cmd) throws IOException {
        command = cmd;
        pb = new ProcessBuilder(command);

        boolean fail = false;
        String line = "";
        for (String s : command) {
            if (s == null || s.length() == 0) {
                s = "";
                fail = true;
            }

            if (s.contains(" "))
                line += "\"" + s + "\" ";
            else
                line += s + " ";

        }

        if (fail)
            throw new IOException("Illegal null argument.");

        LOG.info("Running: " + line);

    }

    public void run(OutputStream std, OutputStream err) throws IOException, InterruptedException {
        proc = pb.start();

        StreamGobbler sg = null, eg = null;

        try {
            sg = new StreamGobbler("", proc.getInputStream(), std);
            eg = new StreamGobbler("", proc.getErrorStream(), err);
            sg.start();
            eg.start();

            proc.waitFor();

        } finally {
            sg.waitFor();
            eg.waitFor();
            if (sg.getException() != null)
                throw sg.getException();
        }

    }

    public boolean run() throws IOException, InterruptedException {

        InputStreamReporter is = null, err = null;
        boolean done = false;
        try {
            // pb.redirectErrorStream(true);
            proc = pb.start();

            is = new InputStreamReporter("is: ", proc.getInputStream(), null);
            is.start();
            err = new InputStreamReporter("err: ", proc.getErrorStream(), null);
            err.start();

            if (quit) {
                proc.destroy();
                return false;
            }

            int xxx = proc.waitFor();


        } finally {
        	if (is!=null)
            is.finish();
        	if (err!=null)
            err.finish();

        }

        return done;
    }

    public void quit() {
        quit = true;
    }

    public Results getResults() throws IOException, InterruptedException {
        ByteArrayOutputStream std = new ByteArrayOutputStream();
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        run(std, err);
        Results r = new Results();
        r.output = std.toByteArray();
        r.error = err.toByteArray();
        pb = null;
        return r;

    }

    public class Results {
        public byte output[];
        public byte error[];

        public String getErrorString() {
            return new String(error);
        }

        public String getOutputString() {
            return new String(output);
        }
    }
}
