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
        int m, s;
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
        if (m < 0 || m > 60 || s < 0 || s > 60 || h < 0)
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

}
