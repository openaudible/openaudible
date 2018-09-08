package org.openaudible.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.HashMap;

public enum Util {
	instance;
	private static final Log LOG = LogFactory.getLog(Util.class);
	
	public String timeString(long l, boolean includeHours) {
		// return ""+l;
		// final int kInMinute = 60 * 1000;
		// final int kInHour = 60 * kInMinute;
		long seconds = l / 1000;
		long minutes = seconds / 60;
		long hours = minutes / 60;
		seconds = seconds % 60;
		minutes = minutes % 60;
		
		// long ms = l % 1000;
		String out = "";
		if (hours > 0) {
			out += "" + hours;
			out += ":";
		} else if (includeHours)
			out = "00:";
		
		// if (hours > 0 || minutes >= 0)
		{
			if (minutes < 10)
				out += "0";
			out += "" + minutes;
			out += ":";
		}
		if (seconds < 10)
			out += "0";
		out += "" + seconds;
		return out;
	}
	
	public String timeStringLong(long l) {
		// return ""+l;
		// final int kInMinute = 60 * 1000;
		// final int kInHour = 60 * kInMinute;
		long seconds = l / 1000;
		long minutes = seconds / 60;
		long hours = minutes / 60;
		long days = hours / 24;
		
		seconds = seconds % 60;
		minutes = minutes % 60;
		hours = hours % 24;
		
		// long ms = l % 1000;
		String out = "";
		
		if (days > 0) {
			out += "" + days + " day";
			if (days > 1)
				out += "s";
			out += ", ";
		}
		
		if (out.length() > 0 || hours > 0) {
			out += "" + hours + " hour";
			if (hours > 1)
				out += "s";
			out += ", ";
		}
		
		if (out.length() > 0 || minutes > 0) {
			out += "" + minutes;
			out += " minute";
			if (minutes > 1)
				out += "s";
			out += " and ";
		}
		out += "" + seconds + " second";
		if (seconds > 1)
			out += "s";
		return out;
	}
	
	public String byteCountToString(long l) {
		long k = l / 1024;
		long m = l / (1024 * 1024);
		
		if (m == 0) {
			if (k < 9)
				return "" + l + " bytes";
			return "" + k + "K";
		}
		if (m > 1024) {
			return "" + m + "M";
		}
		if (m > 2) {
			double d = l / (double) (1024 * 1024);
			String out = Double.toString(d);
			if (out.length() > 6)
				return out.substring(0, 6) + "M";
			return out + "M";
		}
		return "" + k + "K";
	}
	
	public static String replaceAll(String haystack, String find, String replacement) {
		while (haystack.contains(find))
			haystack = haystack.replaceAll(find, replacement);
		return haystack;
	}
	
	
	public static String escape(String s) throws Exception {
		char bad[] = {'\n', '/', '#'};
		for (char c : bad) {
			if (s.indexOf(c) != -1)
				throw new Exception("TODO: Fix");
		}
		return s;
	}
	
	
	public static HashMap<String, String> urlGetArgs(String url) {
		HashMap<String, String> map = new HashMap<String, String>();
		
		if (url.contains("?")) {
			
			try {
				String args = url.substring(url.indexOf("?") + 1, url.length());
				
				String split[] = args.split("&");
				for (String params : split) {
					String kv[] = params.split("=");
					if (kv.length == 2) {
						map.put(kv[0], kv[1]);
						
					} else {
						LOG.error("bad url param:" + params + " for " + url); /// happens when there is an & in title.
					}
				}
			} catch (Throwable th) {
				LOG.error("Error parsing url:" + url + " for args.");
			}
		}
		return map;
		
		
	}
	
	
	public static String cleanString(String out) {
		out = replaceAll(out, "\r", "\n");
		out = replaceAll(out, "  ", " ");
		out = replaceAll(out, "\t\t", "\t");
		out = replaceAll(out, " \n", "\n");
		out = replaceAll(out, "\t\n", "\n");
		out = replaceAll(out, "\n\n", "\n");
		return out.trim();
	}
	
	public static int substringCount(String needle, String haystack) {
		int lastIndex = 0;
		int count = 0;
		
		while (lastIndex != -1) {
			
			lastIndex = haystack.indexOf(needle, lastIndex);
			
			if (lastIndex != -1) {
				count++;
				lastIndex += needle.length();
			}
		}
		return count;
	}
	
}
