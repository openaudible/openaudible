package org.openaudible.audible;

import org.openaudible.books.Book;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created  7/17/2017.
 */
public class AudibleUtils {
	// return 4 digit year string, eg 2014
	// Two common formats:
	// 11-MAR-2014
	// 03-11-14
	
	// returns "" if unable to determine.
	public static String getYear(Book b) {
		try {
			String yr = b.getRelease_date();
			
			String dates[] = yr.split("-");
			if (dates != null && dates.length == 3) {
				int y = Integer.parseInt(dates[2]);
				if (y > 0) {
					if (y < 99)
						y += 2000;
					if (y > 1800 && y < 9999)
						return "" + y;
				}
			}
			
			
		} catch (Throwable th) {
			th.printStackTrace();
			assert (false);
		}
		return "";
	}
	
	public static int getRatingByte(Book b) {
		String ar = b.getRating_average();
		if (ar != null && ar.length() > 0) {
			try {
				double r = Double.parseDouble(ar);
				if (r > 0 && r < 5.0) {
					// Expected range:
					int intR = (int) Math.round(r * 20.0);
					if (intR >= 0 && intR <= 100) {
						return intR;
					}
					
				}
			} catch (NumberFormatException nfe) {
				nfe.printStackTrace();
			}
		}
		return 0;
	}
	
	public static Date parseDate(String dateString) {
		Date d = null;
		
		if (dateString.length() == 8) {
			SimpleDateFormat fmt1 = new SimpleDateFormat("MM-dd-yy");
			try {
				d = fmt1.parse(dateString);
			} catch (ParseException e) {
				e.printStackTrace();
				d = null;
			}
		} else if (dateString.length() == 11) {
			SimpleDateFormat fmt1 = new SimpleDateFormat("dd-MMM-yy");
			try {
				d = fmt1.parse(dateString);
			} catch (ParseException e) {
				e.printStackTrace();
				d = null;
			}
			
		} else {
			System.out.println("unexpected dateString format...");
			
			
		}
		
		return d;
	}
}
