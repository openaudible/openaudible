package org.openaudible.desktop.swt.gui;

import org.eclipse.swt.widgets.Display;
import org.openaudible.desktop.swt.manager.Application;
import org.openaudible.desktop.swt.manager.Version;

import java.util.Enumeration;
import java.util.Hashtable;

public abstract class SWTAsync implements Runnable, GUITask {
	
	
	final static long kMinReportTime = 3000;
	final static long kMinCaptureTime = 2000;
	static final int timeInts[] = {50, 500, 5000, 10000, 20000};
	
	public static boolean quit = false;
	public static boolean useStack = Version.appDebug;
	public static boolean useTimer = Version.appDebug;
	static Hashtable<String, TimeStats> timeResults = new Hashtable<>(); //
	static long block_count = 0;
	Exception stack = null;
	final String taskName;
	boolean logTimeStats = useTimer;
	private volatile boolean block = false;
	
	
	static int interval_count = 0;
	static long interval_start = System.currentTimeMillis();
	final static long interval_max = 5000;
	final static long interval_warning = 50;        // 50 calls in 5 seconds is too often.
	
	public SWTAsync() {
		this("task");
	}
	
	public SWTAsync(String task) {
		this.taskName = task;
		if (useStack) {
			stack = new Exception();
			checkInterval();
		}
	}
	
	private void checkInterval() {
		interval_count++;
		long now = System.currentTimeMillis();
		long delta = now - interval_start;
		
		if (interval_count > interval_warning) {
			System.err.println("Warning: SWTAsync called " + interval_count + " in " + delta + " task=" + taskName);
		}
		if (delta > interval_max) {
			interval_count = 0;
			interval_start = now;
		}
	}
	
	public static boolean inDisplayThread() {
		Display d1 = Display.getDefault();
		return d1.getThread().equals(Thread.currentThread());
	}
	
	public static String inspectTimeStats() {
		if (timeResults == null)
			return "";
		String lf = "\n";
		String out = "Time Results:" + lf + "Buckets: ";
		for (int timeInt : timeInts) out += "" + timeInt + " ";
		out += lf;
		
		for (Enumeration cenum = timeResults.keys(); cenum.hasMoreElements(); ) {
			Object k = cenum.nextElement();
			String key = k.toString();
			TimeStats value = (TimeStats) timeResults.get(k);
			if (value.maxTime > kMinReportTime)
				out += key + "=" + value.toString() + lf;
		}
		
		return out;
	}
	
	public static void assertGUI() {
		if (!inDisplayThread()) {
			Application.report("Thread error for assertGUI");
		}
	}
	
	public static void assertNot() {
		if (inDisplayThread())
			Application.report("Thread error for assertNot");
	}
	
	public static void block(SWTAsync t) {
		block_count++;
		// t.taskName = task + " " + block_count;
		t.block = true;
		
		run(t);
		while (t.block) {
			synchronized (t) {
				try {
					t.wait(5000);
					if (!t.block)
						break;
					
					// use this to keep tabs on things that block the UI for a long time...
					// System.out.println("Async taking a long time..." + t.taskName);
					
				} catch (InterruptedException e) {
				
				}
			}
			
		}
	}
	
	public static void run(SWTAsync t) {
		
		if (inDisplayThread()) {
			t.run();
		} else {
			Display d2 = Application.display;
			d2.asyncExec(t);
		}
	}
	
	public static void queueRun(SWTAsync t) {
		Display d2 = Application.display;
		d2.asyncExec(t);
	}
	
	public static void slow(SWTAsync t) {
		t.logTimeStats = false;
		if (inDisplayThread()) {
			t.run();
		} else {
			Display d2 = Application.display;
			d2.asyncExec(t);
		}
	}
	
	public static void runOutsideGUI(SWTAsync t) {
		if (inDisplayThread()) {
			new Thread(t, t.taskName).start();
		} else
			t.run();
	}
	
	public static void run(Display d, SWTAsync t) {
		if (inDisplayThread()) {
			t.run();
		} else {
			d.asyncExec(t);
		}
	}
	
	static void test() {
		SWTAsync.run(new SWTAsync("SWTAsync test") {
			public void task() {
			
			}
		});
	}
	
	public static void block2(final GUITask t) {
		SWTAsync s = new SWTAsync("block2") {
			@Override
			public void task() {
				t.task();
			}
		};
		block(s);
	}
	
	
	public abstract void task();
	
	public void run() {
		long start = System.currentTimeMillis();
		
		try {
			if (!quit)
				task();
			
		} catch (java.lang.IllegalArgumentException iae) {
			Application.report(iae, "1. error running task " + taskName);
			if (stack != null)
				Application.report(stack, "2. at called from:");
		} catch (Throwable e) {
			String id = Integer.toString((int) (Math.random() * 342234) % 10000);
			
			GUI.logger.warn("Error running GUI task " + taskName + "' [" + id + "]", e);
			if (stack != null)
				GUI.logger.warn("Called from [" + id + "]", stack);
		} finally {
			if (this.logTimeStats) {
				long time = System.currentTimeMillis() - start;
				
				if (timeResults != null && time > kMinCaptureTime) {
					TimeStats c = (TimeStats) timeResults.get(taskName);
					if (c == null) {
						c = new TimeStats(taskName);
						timeResults.put(taskName, c);
					}
					
					c.increment(time);
				}
			}
			
			if (block) {
				block = false;
				synchronized (this) {
					this.notifyAll();
				}
			}
		}
	}
	
	class TimeStats {
		long totalTime = 0;
		long maxTime = 0;
		long lastTime = 0;
		long count = 0;
		String id;
		long results[] = new long[timeInts.length];
		
		TimeStats(String id) {
			this.id = id;
		}
		
		void increment(long time) {
			count++;
			totalTime += time;
			if (time > maxTime)
				maxTime = time;
			lastTime = time;
			for (int x = 0; x < timeInts.length; x++) {
				int index = timeInts.length - x - 1;
				if (time > timeInts[index]) {
					results[index]++;
					break;
				}
			}
		}
		
		public String toString() {
			if (count == 0)
				return "";
			
			double aveK = (totalTime / (double) count) * 1000.0;
			String ave = "" + (aveK / 1000.0);
			String out = id + " max=" + maxTime + " ave=" + ave + " called " + count + " times [";
			for (int x = 0; x < timeInts.length; x++) {
				if (x > 0)
					out += ", ";
				out += results[x];
			}
			out += "] last=" + lastTime;
			return out;
		}
	}
	
}
