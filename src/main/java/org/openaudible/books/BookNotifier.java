package org.openaudible.books;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openaudible.util.EventNotifier;
import org.openaudible.util.EventTimer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created  6/27/2017.
 */
public class BookNotifier extends EventNotifier<BookListener> implements BookListener {
    private static final Log LOG = LogFactory.getLog(BookNotifier.class);
    private static BookNotifier instance = new BookNotifier();
    int c1;
    long time;
    private ArrayList<Book> selected = new ArrayList<Book>();

    private BookNotifier() {
    }

    public static BookNotifier getInstance() {
        return instance;
    }

    public synchronized List<Book> getSelected() {

        return Collections.unmodifiableList(selected);

    }

    public synchronized void setSelected(ArrayList<Book> selected) {
        this.selected = selected;
    }

    @Override
    public void booksSelected(List<Book> list) {

        synchronized (getLock()) {
            selected.clear();
            selected.addAll(list);
        }

        for (BookListener l : getListeners())
            l.booksSelected(list);
    }

    @Override
    public void bookAdded(Book book) {
        for (BookListener l : getListeners())
            l.bookAdded(book);

    }

    private void log() {
        EventTimer evt = new EventTimer();
        c1++;
        time += evt.time();
        if (c1 % 10 == 0) LOG.info("book notify:" + c1 + " " + time);
    }

    @Override
    public void bookUpdated(Book book) {
        for (BookListener l : getListeners())
            l.bookUpdated(book);
        log();
    }

    @Override
    public void booksUpdated() {

        for (BookListener l : getListeners())
            l.booksUpdated();
        log();
    }

}
