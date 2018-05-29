package org.openaudible.util;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class Console extends ConsoleHandler {
    public interface ILogRecordPublisher {
        void publish(LogRecord l);
    }

    public final static Console instance = new Console();
    boolean installed = false;

    public void install() {
        assert (!installed);
        installed = true;
        Logger.getLogger("").addHandler(this);
        // LogFactory.getFactory().setAttribute("");

        // captureStdMessages();
    }

    public void uninstall() {
        assert (installed);

        installed = false;
        Logger.getLogger("").removeHandler(this);
    }

    public Console() {
        System.currentTimeMillis();
    }

    public void setListener(ILogRecordPublisher l) {
        listener = l;
    }

    ILogRecordPublisher listener;

    static final int maxHistory = 100;
    final LinkedList<LogRecord> history = new LinkedList<>();


    public final List<LogRecord> getHistory() {
        synchronized (history) {
            return Collections.unmodifiableList(history);
        }
    }


    /**
     * Publish a <tt>LogRecord</tt>.
     * <p>
     * The logging request was made initially to a <tt>Logger</tt> object,
     * which initialized the <tt>LogRecord</tt> and forwarded it here.
     * <p>
     *
     * @param record description of the log event. A null record is
     *               silently ignored and is not published
     */
    @Override
    public void publish(LogRecord record) {
        super.publish(record);

        synchronized (history) {
            history.add(record);
            if (history.size() > maxHistory)
                history.removeFirst();
        }
        if (listener != null) {
            listener.publish(record);
        }
    }


    public void captureStdMessages() {

        PrintStream op = new PrintStream(new ConsoleOutputStream(0), true);
        PrintStream ep = new PrintStream(new ConsoleOutputStream(1), true);

        System.setErr(ep);
        System.setOut(op);
    }


    class ConsoleOutputStream extends ByteArrayOutputStream {
        ConsoleOutputStream(int c) {
            color = c;
        }

        final int color;

        @Override
        public void flush() {
            String message = toString().trim();
            if (message.length() == 0) return;
            takeLine(message);
            reset();
        }

        /*
         *  The message and the newLine have been added to the buffer in the
         *  appropriate order so we can now update the Document and send the
         *  text to the optional PrintStream.
         */
        private void takeLine(String line) {
            Level level = Level.INFO;   // (color==0) ? Level.INFO:Level.WARNING;
            LogRecord r = new LogRecord(level, line);
            if (listener != null)
                listener.publish(r);
        }

    }


}
