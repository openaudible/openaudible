package org.openaudible.util;

public class TimeToSeconds {
	// given: mm:ss or hh:mm:ss or hhh:mm:ss, return number of seconds.
	// bad input throws NumberFormatException.
	// bad includes:  "", null, :50, 5:-4
	public static long parseTime(String str) throws NumberFormatException {
		if (str == null)
			throw new NumberFormatException("parseTimeString null str");
		if (str.isEmpty())
			throw new NumberFormatException("parseTimeString empty str");
		
		int h = 0;
		int m = 0, s = 0;
		if (str.contains("m") || str.contains("h") || str.contains("s")) {
			for (String unit : str.split(" ")) {
				if (unit.contains("m")) {
					unit = unit.replace("m", "");
					m = Integer.parseInt(unit.trim());
				} else if (unit.contains("h")) {
					unit = unit.replace("h", "");
					h = Integer.parseInt(unit.trim());
				} else if (unit.contains("s")) {
					unit = unit.replace("s", "");
					s = Integer.parseInt(unit.trim());
				} else {
					throw new NumberFormatException("invalid time format:" + str);
				}
			}
		} else {
			String units[] = str.split(":");
			switch (units.length) {
				case 2:
					// mm:ss
					m = Integer.parseInt(units[0]);
					s = Integer.parseInt(units[1]);
					break;
				
				case 3:
					// hh:mm:ss
					h = Integer.parseInt(units[0]);
					m = Integer.parseInt(units[1]);
					s = Integer.parseInt(units[2]);
					break;
				
				default:
					
					throw new NumberFormatException("parseTimeString failed:" + str);
			}
			
			if (m > 60)
				throw new NumberFormatException("parseTimeString minute > 60:" + str);
			if (s > 60)
				throw new NumberFormatException("parseTimeString second > 60:" + str);
			
			
		}
		
		if (m < 0 || s < 0 || h < 0)
			throw new NumberFormatException("parseTimeString range error:" + str);
		return h * 3600 + m * 60 + s;
	}
	
	// given time string (hours:minutes:seconds, or mm:ss, return number of seconds.
	public static long parseTimeStringToSeconds(String str) {
		try {
			return parseTime(str);
		} catch (NumberFormatException nfe) {
			return 0;
		}
	}
	
	public static String secondsToHHMM(long sec) {
		
		int m = (int) Math.round(sec / 60.0);
		int h = m / 60;
		m = m % 60;
		if (m > 0 || h > 0) {
			String hh = h < 10 ? "0" + h : "" + h;
			String mm = m < 10 ? "0" + m : "" + m;
			return hh + ":" + mm;
		}
		return "";
		
	}
	
}
