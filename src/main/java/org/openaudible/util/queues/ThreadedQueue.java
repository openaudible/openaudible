package org.openaudible.util.queues;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;

// Do not use for mission critical applications.. needless to say.
public abstract class ThreadedQueue<E> implements IQueueListener<E> {
    private static final Log LOG = LogFactory.getLog(ThreadedQueue.class);

    int concurrentJobs;       // number of jobs that can be run at once
    volatile boolean quit = false;
    int totalThreads = 0;

    final LinkedList<E> queue = new LinkedList<>();
    final LinkedList<JobThread> threads = new LinkedList<>();
    final LinkedList<IQueueJob> jobs = new LinkedList<>();
    final HashMap<E, IQueueJob> jobMap = new HashMap<>();

    final Object waitObject = new Object();
    final ArrayList<IQueueListener<E>> listeners = new ArrayList<>();


    public ThreadedQueue() {
        this(5);
    }

    public ThreadedQueue(int concurrentJobs) {
        this.concurrentJobs = concurrentJobs;
    }

    public void addListener(IQueueListener<E> l) {
        listeners.add(l);
    }

    public boolean removeListener(IQueueListener<E> l) {
        boolean ok = listeners.remove(l);
        assert (ok);
        return ok;
    }



    public int size() {
        return queue.size();
    }

    // return null if not can't or shouldn't create. Otherwise create a job.
    public abstract IQueueJob createJob(E e);

    // Can add a job?
    // For default behavior, the same job can't already be running or in queue.
    public boolean canAdd(E e) {
        if (quit)
            return false;
        if (queue.contains(e))
            return false;
        synchronized (threads) {
            for (JobThread j : threads)
                if (j.workingOn(e)) return false;
        }
        return true;
    }

    public boolean add(E item) {
        if (canAdd(item)) {
            boolean notify = false;
            synchronized (queue) {
                if (!queue.contains(item) && !quit) {
                    queue.add(item);
                    notify = true;
                }
            }
            if (notify) {
                itemEnqueued(this, item);
                _notify();
                return true;
            }

        }
        return false;
    }

    private void _notify() {
        synchronized (waitObject) {
            waitObject.notifyAll();
        }

        startJobs();
    }

    public Collection<E> addAll(Collection<E> items) {
        ArrayList<E> results = new ArrayList<>();

        for (E e : items) {
            boolean added = add(e);
            if (added)
                results.add(e);
        }

        return results;
    }

    String queueInfo() {
        int todo = queue.size();
        int progress = threads.size();
        if (todo == 0 && progress == 0)
            return "";

        return "In progress:" + progress + " queue: " + todo;
    }

    private void startJobs() {
        while (!quit) {
            if (threads.size() >= queue.size())
                break;
            if (threads.size() >= concurrentJobs)
                break;

            totalThreads++;
            JobThread t = new JobThread();
            synchronized (threads) {
                threads.add(t);
            }
            t.start();
        }

    }

    public void quit() {
        quit = true;
        synchronized (threads) {
            for (JobThread j : threads)
                j.quit();
        }
    }


    @Override
    public void jobProgress(ThreadedQueue<E> queue, IQueueJob job, E o, String task, String subtask) {
        for (IQueueListener<E> i : getListeners()) {
            i.jobProgress(this, job, o, task, subtask);
        }

    }


    public void itemEnqueued(ThreadedQueue<E> queue, E item) {
        for (IQueueListener<E> i : getListeners()) {
            i.itemEnqueued(queue, item);
        }
    }

    private ArrayList<IQueueListener<E>> getListeners() {
        ArrayList<IQueueListener<E>> copy = new ArrayList<>();
        copy.addAll(listeners);
        return copy;
    }

    public void itemDequeued(ThreadedQueue<E> queue, E o) {
        for (IQueueListener<E> i : getListeners()) {
            i.itemDequeued(queue, o);
        }
    }

    public void jobStarted(ThreadedQueue<E> queue, IQueueJob job, E item) {
        synchronized (jobs) {
            jobs.add(job);
            jobMap.put(item, job);
        }
        for (IQueueListener<E> i : getListeners()) {
            i.jobStarted(queue, job, item);
        }
    }

    public void jobCompleted(ThreadedQueue<E> queue, IQueueJob job, E item) {
        synchronized (jobs) {
            boolean wasRemoved = jobs.remove(job);
            assert (wasRemoved);

            // this assumes one job per object at a time.
            IQueueJob x = jobMap.remove(item);
            assert (x == job);
        }

        for (IQueueListener<E> i : getListeners()) {
            i.jobCompleted(queue, job, item);
        }
    }

    public boolean inJob(E e) {
        return jobMap.containsKey(e);
    }


    @Override
    public void jobError(ThreadedQueue<E> queue, IQueueJob job, E item, Throwable th) {
        for (IQueueListener<E> i : getListeners()) {
            i.jobError(queue, job, item, th);
        }

    }

    private E dequeue() {
        synchronized (queue) {
            if (queue.size() > 0)
                return queue.removeFirst();
        }
        return null;
    }

    public int jobsInProgress() {
        return jobs.size();
    }

    public boolean isQueued(E e) {
        synchronized (queue) {
            return (queue.contains(e));
        }

    }


    class JobThread extends Thread {
        IQueueJob currentJob = null;
        volatile E b;

        JobThread() {
            super("Job");
        }

        boolean workingOn(E item) {
            return item == b;
        }

        public void run() {
            while (!quit) {
                b = dequeue();
                if (b == null)
                    break;

                itemDequeued(ThreadedQueue.this, b);

                currentJob = createJob(b);
                this.setName(currentJob.toString());

                if (currentJob == null)
                    continue;

                LOG.info("Starting job " + currentJob + " " + queueInfo());

                try {
                    jobStarted(ThreadedQueue.this, currentJob, b);
                    currentJob.processJob();
                } catch (Throwable th) {
                    jobError(ThreadedQueue.this, currentJob, b, th);
                    th.printStackTrace();
                    break;
                } finally {
                    jobCompleted(ThreadedQueue.this, currentJob, b);
                    currentJob = null;
                }

                // LOG.info(queueInfo());

            }

            synchronized (threads) {
                threads.remove(this);
            }

        }

        public void quit() {
            IQueueJob j = currentJob;
            if (j != null)
                j.quitJob();
        }

    }

    public int getConcurrentJobs() {
        return concurrentJobs;
    }

    public void setConcurrentJobs(int concurrentJobs) {
        this.concurrentJobs = concurrentJobs;
    }


}
