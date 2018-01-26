package org.openaudible.util;


public class EventTimer {
    long start;
    long time;

    public EventTimer() {
        start = System.currentTimeMillis();
        time = 0;
    }

    public String reportString(String header) {
        stop();

        if (time < 5000)
            return (header + " took " + time + " ms. to complete.");
        else
            return (header + " took " + time / 1000.0 + " seconds to complete.");
    }

    private long stop() {
        time = System.currentTimeMillis() - start;
        return time;
    }

    public long time() {
        time = System.currentTimeMillis() - start;
        return time;
    }

    public String toString() {
        return reportString("event time " + time());
    }
}
