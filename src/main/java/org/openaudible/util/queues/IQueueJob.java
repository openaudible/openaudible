package org.openaudible.util.queues;

public interface IQueueJob {
	void processJob() throws Exception;
	
	void quitJob();
}
