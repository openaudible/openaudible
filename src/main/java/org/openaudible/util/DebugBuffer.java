package org.openaudible.util;

import java.util.function.Consumer;

public class DebugBuffer implements Consumer<String> {
	private java.lang.StringBuffer debug;
	final int max;      // bytes in memory log.
	final String sep;
	
	
	public DebugBuffer(int size, String sep) {
		this.max = size;
		this.sep = sep;
	}
	
	public DebugBuffer() {
		this(2000, ", ");
	}
	
	public synchronized void debug(String s) {
		accept(s);
	}
	
	public String toString() {
		if (debug == null) return "";
		return debug.toString();
	}
	
	public synchronized void clear() {
		if (debug != null)
			debug.delete(0, debug.length());
	}
	
	@Override
	public void accept(String s) {
		
		synchronized (this) {
			if (debug == null) {
				debug = new java.lang.StringBuffer(max + 500);
			} else {
				debug.append(sep);
			}
			
			if (debug.length() > max) {
				debug.delete(0, max);
				debug.append("...");
			}
			debug.append(s);
		}
	}
}
