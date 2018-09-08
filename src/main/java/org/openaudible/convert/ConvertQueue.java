package org.openaudible.convert;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openaudible.Audible;
import org.openaudible.books.Book;
import org.openaudible.util.queues.IQueueJob;
import org.openaudible.util.queues.JobProgress;
import org.openaudible.util.queues.ThreadedQueue;

public class ConvertQueue extends ThreadedQueue<Book> {
	
	// Queue to convert audio books to mp3
	private static final Log LOG = LogFactory.getLog(ConvertQueue.class);
	
	public ConvertQueue() {
		super(6);       // how many concurrent conversions to do.
	}
	
	@Override
	public IQueueJob createJob(Book b) {
		ConvertJob c = new ConvertJob(b);
		c.setProgress(new JobProgress<Book>(this, c, b));
		return c;
	}
	
	public boolean canAdd(Book e) {
		return super.canAdd(e) && !Audible.instance.getMP3FileDest(e).exists();
	}
	
	@Override
	public String toString() {
		return "ConvertQueue";
	}
	
}

