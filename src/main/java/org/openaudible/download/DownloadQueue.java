package org.openaudible.download;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openaudible.Audible;
import org.openaudible.books.Book;
import org.openaudible.books.BookElement;
import org.openaudible.util.queues.IQueueJob;
import org.openaudible.util.queues.JobProgress;
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
		aaxDownloader.setProgress(new JobProgress<Book>(this, aaxDownloader, b));
		return aaxDownloader;
	}
	
	public boolean canAdd(Book b) {
		if (Audible.instance.hasAAX(b)) return false;
		if (!super.canAdd(b)) return false;
		if (!b.has(BookElement.user_id))
			return false;
		if (!b.has(BookElement.product_id))
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		return "DownloadQueue";
	}
	
}
