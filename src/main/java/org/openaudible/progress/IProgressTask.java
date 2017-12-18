package org.openaudible.progress;

public interface IProgressTask {
    void setTask(final String task, final String subtask);

    void setTask(final String task);

    void setSubTask(final String subtask);

    boolean wasCanceled();
}
