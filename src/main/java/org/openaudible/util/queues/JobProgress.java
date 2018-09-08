package org.openaudible.util.queues;

import org.openaudible.progress.IProgressTask;

public class JobProgress<E> implements IProgressTask {

	final ThreadedQueue queue;
	final IQueueJob job;
	final E e;


	public JobProgress(ThreadedQueue q, IQueueJob job, E e) {
		this.queue = q;
		this.job = job;
		this.e = e;
	}


	@Override
	public void setTask(String task, String subtask) {
		// notify listeners of progress.
		queue.jobProgress(queue, job, e, task, subtask);
	}


	@Override
	public boolean wasCanceled() {
		return queue.quit;
	}


}
