package org.openaudible.util;

public enum Util {
    instance;

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

}
