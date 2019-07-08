package org.openaudible.desktop.swt.gui.progress;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Shell;
import org.openaudible.desktop.swt.gui.SWTAsync;
import org.openaudible.desktop.swt.util.shop.PaintShop;

import java.lang.reflect.InvocationTargetException;

public class ProgressDialog extends ProgressMonitorDialog {
	final static String USER_CANCELED_MESSAGE = "User Canceled";
	private static Log log = LogFactory.getLog(ProgressDialog.class);
	boolean lock = false;
	IRunnableWithProgress task;
	Thread simpleThread = null;
	Shell shell;
	// Display display;
	Throwable runException = null;
	boolean closed = false;
	final Object waitObj = new Object();
	//
	//
	//	private static void doProgressTask(final Shell s, final Thread task)
	//	{
	//		ProgressDialog.doProgressTask(s, task, false);
	//	}
	//	private static void doProgressTask(final Shell s, final IRunnableWithProgress task)
	//	{
	//	    ProgressDialog.doProgressTask(s, task, false);
	//	}
	//
	//	private static void doProgressTaskExtended(final Shell s, final IRunnableWithProgress task)
	//	{
	//	    doProgressTask(s, task, true);
	//	}
	//	private static void doProgressTaskExtended(final Shell s, final Thread task)
	//	{
	//	    doProgressTask(s, task, true);
	//	}
	//
	//	private static void doProgressTask(final Shell s, final IRunnableWithProgress task, final boolean extended)
	//	{
	//		if (lock)
	//		{
	//			DebugLog
	//					.println("Waiting for previous task to finish before starting new task. Thread="
	//							+ who.toString());
	//			DebugLog.report(where);
	//			if (true)
	//				return;
	//		}
	//		lock = true;
	//		where = new Exception("in progress");
	//		who = Thread.currentThread();
	//		SWTAsync.run(new SWTAsync() {
	//			public void task()
	//			{
	//				if (extended)
	//				    pd = new ProgressDialogExtended(s, task);
	//				else
	//				    pd = new ProgressDialog(s, task);
	//
	//				pd.open();
	//				// pd.taskLabel.setText("taskLabel");
	//				// pd.messageLabel.setText("messageLabel");
	//				synchronized (pd)
	//				{
	//					pd.runTask();
	//					if (pd != null)
	//						pd.done();
	//					pd = null;
	//				}
	//			}
	//		});
	//	}
	//
	//
	//	private static boolean needProgressUpdate() throws InterruptedException
	//	{
	//		if (pd!=null) return pd.needUpdate();
	//		return false;
	//	}
	//
	long timer = 0;
	
	public ProgressDialog(Shell shell) {
		super(shell);
		
		shell.setImages(PaintShop.getAppIconList());
		init(shell);
		for (; ; ) {
			synchronized (waitObj) {
				if (closed)
					break;
				try {
					waitObj.wait(1000);
				} catch (InterruptedException ie) {
					log.error(ie);
				}
			}
		}
	}
	
	//	public ProgressDialog(Shell parent, IRunnableWithProgress t)
	//	{
	//		super(parent);
	//		task = t;
	//		init(shell);
	//	}
	//
	public ProgressDialog(Shell parent, Thread t) {
		super(parent);
		simpleThread = t;
		init(parent);
	}
	
	public static void doProgressTask(final ProgressTask t) {
		doProgressTask(null, t);
	}
	
	public static void doProgressTask(final Shell s, final ProgressTask t) {
		SWTAsync.slow(new SWTAsync("doProgressTask") {
			public void task() {
				ProgressDialog p = new ProgressDialog(s, t);
				
				t.setProgress(p);
				p.open();
				p.getShell().setText(t.getName()); //$NON-NLS-1$
				
				// pd.taskLabel.setText("taskLabel");
				// pd.messageLabel.setText("messageLabel");
				
				p.runTask();
				
			}
		});
	}
	
	public void cancelPressed() {
		super.cancelPressed();
		
		if (simpleThread instanceof ProgressTask) {
			((ProgressTask) simpleThread).userCanceled();
			
		}
		
	}
	
	public boolean needUpdate() throws InterruptedException {
		long now = System.currentTimeMillis();
		long delta = now - timer;
		if (delta > 100) {
			timer = now;
			throwIfCanceled();
			Thread.yield();
			return true;
		}
		
		return false;
	}
	
	//	private static boolean setTask(String task, String subtask) throws InterruptedException
	//	{
	//		if (pd != null)
	//		{
	//			pd.setTaskAsync(task, subtask);
	//			Thread.yield();
	//			return pd.canceled();
	//		} else
	//		{
	//			if (task!=null && subtask!=null)
	//			{
	//				log.println(task+" "+ subtask);
	//			}
	//			else
	//			{
	//				if (task!=null) log.println(task);
	//				if (subtask!=null) log.println(subtask);
	//			}
	//		}
	//		return false;
	//	}
	//	private static void setDone()
	//	{
	//
	//	}
	
	//	private static void setDone()
	//	{
	//		if (pd != null)
	//		{
	//			//	pd.setDone();
	//			// pd.done();
	//			// pd = null;
	//		}
	//	}
	
	// call this if you want to force a periodic message to appear...
	// call when "changing gears" so new message will appear..
	public void clearTimer() {
		timer = 0;
	}
	
	//	private static void block()
	//	{
	//		try
	//		{
	//			if (!lock)
	//				return;
	//			Thread cur = Thread.currentThread();
	//			Object task = null;
	//			if (pd != null)
	//				task = pd.task;
	//			while (lock)
	//			{
	//				Thread.sleep(1000);
	//				DebugLog.println("Waiting for " + task + " from " + cur);
	//			}
	//		} catch (Exception ie)
	//		{
	//			log.report(ie);
	//		}
	//	}
	public void println(final String o) throws InterruptedException {
		throwIfCanceled();
	}
	
	public boolean canceled() {
		return getProgressMonitor().isCanceled();
	}
	
	public void throwIfCanceled(IProgressMonitor m) throws InterruptedException {
		if (m != null && m.isCanceled())
			throw new InterruptedException(USER_CANCELED_MESSAGE);
	}
	
	public void throwIfCanceled() throws InterruptedException {
		throwIfCanceled(getProgressMonitor());
	}
	
	// public void setTaskAsync(final String task, final String subtask) throws InterruptedException
	public void beginTaskUndefined(final String state) throws InterruptedException {
		beginTask(state, IProgressMonitor.UNKNOWN);
	}
	
	public void beginTask(final String state, final int count) throws InterruptedException {
		final IProgressMonitor m = getProgressMonitor();
		SWTAsync.run(new SWTAsync("progress dialog begin") {
			public void task() {
				
				m.beginTask(state, count);
			}
		});
		Thread.yield();
		throwIfCanceled(m);
	}
	
	//	static public boolean inProgress()
	//	{
	//		return (pd != null);
	//	}
	//
	//	static public boolean isCanceled()
	//	{
	//		if (!inProgress())
	//		{
	//			return false;
	//		}
	//		return pd.getProgressMonitor().isCanceled();
	//	}
	
	// ProgressMonitorDialog pb;
	
	public void worked(final int count) {
		final IProgressMonitor m = getProgressMonitor();
		SWTAsync.run(new SWTAsync("progress dialog worked") {
			public void task() {
				m.worked(count);
			}
		});
	}
	
	public void setTaskAsync(final String task, final String subtask) {
		final IProgressMonitor m = getProgressMonitor();
		if (m == null)
			return;
		
		SWTAsync.run(new SWTAsync("progress dialog task: " + task) {
			public void task() {
				if (task != null) {
					if (subtask == null || subtask.length() == 0) {
						try {
							println(task); // such a kludge...
						} catch (Exception e) {
							// TODO: handle exception
						}
					}
					
					m.setTaskName(task);
				}
				if (subtask != null)
					m.subTask(subtask);
			}
		});
	}
	
	public void operationWasCanceled() {
		MessageDialog.openInformation(shell, "Cancelled", "Operation was cancelled.");
	}
	
	public void runTask() {
		try {
			if (task == null) {
				task = new LongRunningOperation();
			}
			
			boolean canCancel = true;
			if (simpleThread instanceof ProgressTask) {
				canCancel = ((ProgressTask) simpleThread).canCancel();
			}
			
			//DebugLog.println("Running Progress Task: " + task.toString());
			run(true, canCancel, task);
		} catch (InvocationTargetException e) {
			log.error(e);
			MessageDialog.openError(shell, "Error", e.getMessage());
			runException = e;
		} catch (InterruptedException e) {
			runException = e;
			log.error(e);
			operationWasCanceled();
			
		} catch (Throwable e) {
			runException = e;
			log.error(e);
		}
		done();
	}
	
	protected void init(Shell is) {
		this.shell = is;
		
		setOpenOnRun(true);
	}
	
	void setMinimum(int c) {
		// pb.setMinimum(c);
	}
	
	void setMaximum(int c) {
		// pb.setMaximum(c);
	}
	
	void setSelection(int x) {
		// pb.setSelection(x);
	}
	
	public void done() {
		// s.dispose();
		synchronized (waitObj) {
			closed = true;
			waitObj.notifyAll();
			lock = false;
		}
		close();
	}
	
	class LongRunningOperation implements IRunnableWithProgress {
		public void run(IProgressMonitor monitor) throws InterruptedException {
			// DebugLog.println("Running task");
			monitor.beginTask("Running task ", IProgressMonitor.UNKNOWN);
			if (simpleThread != null) {
				simpleThread.start();
				simpleThread.join();
			}
			// DebugLog.println("Task complete.");
			monitor.done();
			if (monitor.isCanceled())
				throw new InterruptedException("The operation was cancelled");
		}
	}
}
