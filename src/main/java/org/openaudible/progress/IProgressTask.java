package org.openaudible.progress;

// Potentially Long running tasks can send information about their progress.
// Used to create progress dialog with cancel button, task, and subtasks.
public interface IProgressTask {
    void setTask(final String task, final String subtask);

    default void setTask(final String task) { setTask(task,null); }

    default void setSubTask(final String subtask) { setTask(null, subtask); }

    default boolean wasCanceled() { return false; }
}

