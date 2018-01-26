package org.openaudible.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class InputStreamReporter extends Thread {
    private static final Log LOG = LogFactory.getLog(InputStreamReporter.class);
    final LineListener taker;
    InputStream is;
    String name;
    String lastMsg = "";
    volatile boolean quit = false;
    volatile boolean done = false;
    final Object waiter = new Object();
    volatile int lines = 0;

    /**
     * Instantiate a new StreamGobbler
     *
     * @param is The inputstream of the process
     */
    public InputStreamReporter(String name, InputStream is, LineListener taker) {
        this.is = is;
        this.name = name;
        this.taker = taker;
        setName(name + " reporter");
        setDaemon(true);
    }

    /**
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {
        try {
            String line;
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            /* Read output */
            while ((line = br.readLine()) != null) {
                if (taker != null)
                    taker.takeLine(line);
                lastMsg = line;
                if (quit)
                    break;
                lines++;
            }

        }
        /* Log any error */ catch (Exception e) {
            LOG.error(this.getName(), e);
        } finally {
            done = true;

            synchronized (waiter) {
                waiter.notifyAll();
            }

        }
    }

    public String getLastLine() {
        return lastMsg;
    }

    public void finish() {
        if (!done && !quit) {
            try {
                synchronized (waiter) {
                    waiter.wait(3000);
                }

            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

    }

}
