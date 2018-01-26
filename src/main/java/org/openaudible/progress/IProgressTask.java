package org.openaudible.progress;

// Potentially Long running tasks can send information about their progress.
// Used to create progress dialog with cancel button, task, and subtasks.
public interface IProgressTask {
    void setTask(final String task, final String subtask);

    void setTask(final String task);

    void setSubTask(final String subtask);

    boolean wasCanceled();
}
