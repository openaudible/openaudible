package org.openaudible.progress;

public class NullProgressTask implements IProgressTask {
	@Override
	public void setTask(String task, String subtask) {
	
	}
	
	@Override
	public void setTask(String task) {
	
	}
	
	@Override
	public void setSubTask(String subtask) {
	
	}
	
	@Override
	public boolean wasCanceled() {
		return false;
	}
}
