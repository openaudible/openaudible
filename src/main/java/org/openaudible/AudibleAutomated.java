package org.openaudible;

import org.openaudible.books.Book;
import org.openaudible.convert.AAXParser;
import org.openaudible.util.queues.IQueueJob;
import org.openaudible.util.queues.IQueueListener;
import org.openaudible.util.queues.ThreadedQueue;

import java.io.IOException;

public class AudibleAutomated extends Audible implements IQueueListener<Book> {

    @Override
    public void itemEnqueued(ThreadedQueue<Book> queue, Book o) {
        // TODO Auto-generated method stub

    }

    @Override
    public void itemDequeued(ThreadedQueue<Book> queue, Book o) {
        // TODO Auto-generated method stub

    }

    @Override
    public void jobStarted(ThreadedQueue<Book> queue, IQueueJob job, Book o) {
        // TODO Auto-generated method stub

    }

    @Override
    public void jobError(ThreadedQueue<Book> queue, IQueueJob job, Book o, Throwable th) {
        // TODO Auto-generated method stub

    }

    @Override
    public void jobCompleted(ThreadedQueue<Book> queue, IQueueJob job, Book book) {
        if (queue.equals(this.downloadQueue)) {
            bookDownloaded(book);

        } else {

        }

    }

    protected void bookDownloaded(Book book) {
        try {
            AAXParser.instance.update(book);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        convertQueue.add(book);

    }

    public void init() throws IOException {
        super.init();
        downloadQueue.addListener(this);
    }

}
