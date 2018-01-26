package org.openaudible.util.queues;

public interface IQueueListener<E> {
    void itemEnqueued(ThreadedQueue<E> queue, E o);

    void itemDequeued(ThreadedQueue<E> queue, E o);

    void jobStarted(ThreadedQueue<E> queue, IQueueJob job, E o);

    void jobError(ThreadedQueue<E> queue, IQueueJob job, E o, Throwable th);

    void jobCompleted(ThreadedQueue<E> queue, IQueueJob job, E o);

}
