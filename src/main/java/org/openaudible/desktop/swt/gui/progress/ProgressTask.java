package org.openaudible.desktop.swt.gui.progress;

import org.eclipse.jface.resource.JFaceResources;
import org.openaudible.progress.IProgressTask;

public class ProgressTask extends Thread implements IProgressTask {
    protected ProgressDialog progress;

    private ProgressTask() {
        super(JFaceResources.getString("ProgressMonitorDialog.title"));
    }

    public ProgressTask(ProgressDialog p, String name) {
        super(name);
        progress = p;
    }

    public ProgressTask(String name) {
        super(name);
    }

    public void setProgress(ProgressDialog p) {
        progress = p;
        boolean cancancel = canCancel();
        p.setCancelable(cancancel);
    }

    public void setTask(String t, String s) {
        progress.setTaskAsync(t, s);
    }

    public boolean needUpdate() throws InterruptedException {
        return progress.needUpdate();
    }

    public void beginWork(String name, int totalWork) {
        progress.getProgressMonitor().beginTask(name, totalWork);
    }

    public void worked(int i) {
        progress.getProgressMonitor().worked(i);
    }

    public boolean isDone() {
        return !isAlive();
    }

    public void userCanceled() {
        progress.setTaskAsync("Canceling...", null);
    }

    public boolean canCancel() {
        return true;
    }

    @Override
    public void setTask(String task) {
        setTask(task, null);
    }

    @Override
    public void setSubTask(String subtask) {
        setTask(null, subtask);

    }

    @Override
    public boolean wasCanceled() {
        return progress.canceled();
    }

    /*
      Runs this operation. Progress should be reported to the given progress monitor. This method is usually invoked by an <code>IRunnableContext</code>'s <code>run</code> method, which supplies the progress monitor. A request to cancel the operation should be honored and acknowledged by throwing <code>InterruptedException</code>.

      @param monitor
     *            the progress monitor to use to display progress and receive requests for cancelation
     * @exception InvocationTargetException
     *                if the run method must propagate a checked exception, it should wrap it inside an <code>InvocationTargetException</code>; runtime exceptions are automatically wrapped in an <code>InvocationTargetException</code> by the calling context
     * @exception InterruptedException
     *                if the operation detects a request to cancel, using <code>IProgressMonitor.isCanceled()</code>, it should exit by throwing <code>InterruptedException</code>
     *
     * @see IRunnableContext#run
     */

}
