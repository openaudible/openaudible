// ===========================================================================
// Simple EventTimer. Formerly msTimer.
// ---------------------------------------------------------------------------
package org.openaudible.desktop.util;

public class EventTimer {
    long start;
    long time;
    long startMem;
    long deltaMem = 0;

    public EventTimer() {
        Runtime r = Runtime.getRuntime();
        startMem = r.totalMemory() - r.freeMemory();
        startTimer();
    }

    public void startTimer() {
        start = System.currentTimeMillis();
        time = 0;
    }

    private long printReport(String header) {
        stop();
        return time;
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
        Runtime r = Runtime.getRuntime();
        long stopMem = r.totalMemory() - r.freeMemory();
        deltaMem = stopMem - startMem;
        return time;
    }

    public long time() {
        time = System.currentTimeMillis() - start;
        return time;
    }

}
