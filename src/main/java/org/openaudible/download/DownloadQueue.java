package org.openaudible.download;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openaudible.Audible;
import org.openaudible.books.Book;
import org.openaudible.util.queues.IQueueJob;
import org.openaudible.util.queues.ThreadedQueue;

import java.io.File;

public class DownloadQueue extends ThreadedQueue<Book> {

    // Queue to download aax audio books, one thread at a time.
    private static final Log LOG = LogFactory.getLog(DownloadQueue.class);

    public DownloadQueue(int concurrentThreads) {
        super(concurrentThreads);
    }

    @Override
    public IQueueJob createJob(Book b) {
        File destFile = Audible.instance.getAAXFileDest(b);
        DownloadJob aaxDownloader = new DownloadJob(b, destFile);
        return aaxDownloader;
    }

    public boolean canAdd(Book e) {
        return super.canAdd(e) && !Audible.instance.getAAXFileDest(e).exists();
    }

}

	