package org.openaudible.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class StreamGobbler extends Thread {
    private static final Log LOG = LogFactory.getLog(InputStreamReporter.class);
    InputStream is;
    OutputStream out;
    volatile boolean quit = false;
    volatile boolean done = false;
    Object waiter = new Object();
    IOException exception;
    boolean closeInput = true, closeOutput = true;

    /**
     * Instantiate a new StreamGobbler
     *
     * @param is The inputstream of the process
     */
    public StreamGobbler(String name, InputStream is, OutputStream os) {
        this.is = is;
        this.out = os;
        setName(name + " gobbler");
        setDaemon(true);
    }

    /**
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {
        try {
            byte buf[] = new byte[1024 * 64];
            while (!quit) {
                int read = is.read(buf);
                if (read == -1)
                    break;

                out.write(buf, 0, read);
            }
        }

        /** Log any error */ catch (IOException e) {

            LOG.error(this.getName(), e);
        } finally {

            if (closeInput && is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            if (closeOutput && out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }

            done = true;

            synchronized (waiter) {
                waiter.notifyAll();
            }

        }
    }

    public IOException getException() {
        return exception;
    }

    public void waitFor() throws IOException {
        while (!done) {
            synchronized (waiter) {
                try {
                    waiter.wait(3000);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
        if (exception != null)
            throw exception;
    }

}
