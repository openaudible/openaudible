package org.openaudible.convert;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openaudible.Audible;
import org.openaudible.books.Book;
import org.openaudible.util.queues.IQueueJob;
import org.openaudible.util.queues.ThreadedQueue;

public class ConvertQueue extends ThreadedQueue<Book> {

    // Queue to convert audio books, one thread at a time.
    private static final Log LOG = LogFactory.getLog(ConvertQueue.class);

    public ConvertQueue() {
        super(2);
    }

    @Override
    public IQueueJob createJob(Book b) {
        ConvertJob c = new ConvertJob(b);
        return c;
    }


    public boolean canAdd(Book e) {

        return super.canAdd(e) && !Audible.instance.getMP3FileDest(e).exists();
    }


}
