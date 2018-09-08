package org.openaudible.util.queues;

public interface IQueueListener<E> {
	void itemEnqueued(final ThreadedQueue<E> queue, final E o);
	
	void itemDequeued(final ThreadedQueue<E> queue, final E o);
	
	void jobStarted(final ThreadedQueue<E> queue, final IQueueJob job, final E o);
	
	void jobError(final ThreadedQueue<E> queue, final IQueueJob job, final E o, final Throwable th);
	
	void jobCompleted(final ThreadedQueue<E> queue, final IQueueJob job, final E o);
	
	void jobProgress(final ThreadedQueue<E> queue, final IQueueJob job, final E o, final String task, final String subtask);
	
}
