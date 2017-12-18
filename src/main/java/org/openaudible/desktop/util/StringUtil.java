package org.openaudible.desktop.util;

import java.util.Vector;

public class StringUtil {
    static final boolean debug = false;

    private StringUtil() {
    }

    public static String repeat(String s, int count) {
        if (s == null || count < 0)
            return null;
        else if (s.length() == 0 || count == 0)
            return "";
        StringBuffer result = new StringBuffer(s.length() * count);
        for (int j = 0; j < count; ++j)
            result.append(s);
        return result.toString();
    }

    public static String repeat(char c, int count) {
        if (count < 0)
            return null;
        else if (count == 0)
            return "";
        StringBuffer result = new StringBuffer(count);
        for (int j = 0; j < count; ++j)
            result.append(c);
        return result.toString();
    }

    public static String padLeft(String s, int finalLength) {
        return padLeft(s, ' ', finalLength);
    }

    public static String padLeft(int value, int finalLength) {
        return padLeft("" + value, ' ', finalLength);
    }

    public static String padLeft(int value, char padChar, int finalLength) {
        return padLeft("" + value, padChar, finalLength);
    }

    public static String padLeft(String s, char padChar, int finalLength) {
        if (s == null)
            return null;
        else if (s.length() >= finalLength)
            return s;
        return repeat(padChar, finalLength - s.length()) + s;
    }

    public static String padRight(String s, int finalLength) {
        return padRight(s, ' ', finalLength);
    }

    public static String padRight(int value, int finalLength) {
        return padRight("" + value, ' ', finalLength);
    }

    public static String padRight(int value, char padChar, int finalLength) {
        return padRight("" + value, padChar, finalLength);
    }

    public static String padRight(String s, char padChar, int finalLength) {
        if (s == null)
            return null;
        else if (s.length() >= finalLength)
            return s;
        return s + repeat(padChar, finalLength - s.length());
    }

    public static String left(String s, int length) {
        if (s == null)
            return null;
        else if (length < 0 && s.length() <= -length)
            return "";
        else if (s.length() <= length)
            return s;
        if (length < 0)
            return s.substring(-length);
        else
            return s.substring(0, length);
    }

    public static String right(String s, int length) {
        if (s == null)
            return null;
        else if (length < 0 && s.length() <= -length)
            return "";
        else if (s.length() <= length)
            return s;
        if (length < 0)
            return s.substring(0, s.length() + length);
        else
            return s.substring(s.length() - length);
    }

    public static String replace(String s, String findStr, String replaceStr) {
        int pos;
        int index = 0;
        while ((pos = s.indexOf(findStr, index)) >= 0) {
            s = s.substring(0, pos) + replaceStr + s.substring(pos + findStr.length());
            index = pos + replaceStr.length();
        }
        return s;
    }

    public static String toMixedCase(String s) {
        StringBuffer result = new StringBuffer();
        char ch;
        boolean lastWasUpper = false;
        boolean isUpper;
        for (int j = 0; j < s.length(); ++j) {
            ch = s.charAt(j);
            isUpper = Character.isUpperCase(ch);
            if (lastWasUpper && isUpper)
                result.append(Character.toLowerCase(ch));
            else
                result.append(ch);
            lastWasUpper = isUpper;
        }
        return result.toString();
    }

    public static String extendDelimited(String base, String newItem, String delimiter) {
        if (base == null || base.equals(""))
            return newItem;
        else
            return base + delimiter + newItem;
    }

    public static String compactWhiteSpace(String s) {
        int len = s.length();
        StringBuffer sb = new StringBuffer(len);
        boolean skipSpace = true;
        char ch;
        for (int i = 0; i < len; ++i) {
            ch = s.charAt(i);
            if (ch <= ' ') {
                ch = ' ';
                if (skipSpace)
                    continue;
                skipSpace = true;
            } else
                skipSpace = false;
            sb.append(ch);
        }
        return sb.toString().trim();
    }

    public static String[] split(String s, char delim) {
        if (s == null)
            return null;
        Vector parts = new Vector();
        int pos;
        while ((pos = s.indexOf(delim)) >= 0) {
            parts.addElement(s.substring(0, pos));
            s = s.substring(pos + 1);
        }
        parts.add(s);
        return (String[]) parts.toArray(new String[parts.size()]);
    }

    public static String wordWrap(String in, int cols, char lf) {
        String out = in;
        int lastLF = 0;
        for (; ; ) {
            String remain = out.substring(lastLF, out.length());
            if (remain.length() < cols)
                break;
        }
        return out;
    }
    /* -- */

    /**
     * This method takes a string and wraps it to a line length of no more than
     * wrap_length.  If prepend is not null, each resulting line will be prefixed
     * with the prepend string.  In that case, resultant line length will be no
     * more than wrap_length + prepend.length()
     */
    public static String wrap(String inString, int wrap_length, String prepend) {
        char[] charAry;
        int p, p2, offset = 0, marker;
        StringBuffer result = new StringBuffer();
        /* -- */
        if (inString == null) {
            return null;
        }
        if (wrap_length < 0) {
            throw new IllegalArgumentException("bad params");
        }
        if (prepend != null) {
            result.append(prepend);
        }
        if (debug) {
            System.err.println("String size = " + inString.length());
        }
        charAry = inString.toCharArray();
        p = marker = 0;
        // each time through the loop, p starts out pointing to the same char as marker
        while (marker < charAry.length) {
            while (p < charAry.length && (charAry[p] != '\n') && ((p - marker) < wrap_length)) {
                p++;
            }
            if (p == charAry.length) {
                if (debug) {
                    System.err.println("At completion..");
                }
                result.append(inString.substring(marker, p));
                return result.toString();
            }
            if (debug) {
                System.err.println("Step 1: p = " + p + ", marker = " + marker);
            }
            if (charAry[p] == '\n') {
				/* We've got a newline.  This newline is bound to have
				   terminated the while loop above.  Step p back one
				   character so that the isspace(*p) check below will detect
				   that it hit the \n, and will do the right thing. */
                result.append(inString.substring(marker, p + 1));
                if (prepend != null) {
                    result.append(prepend);
                }
                if (debug) {
                    System.err.println("found natural newline.. current result = " + result.toString());
                }
                p = marker = p + 1;
                continue;
            }
            if (debug) {
                System.err.println("Step 2: hit wrap length, back searching for newline");
            }
            p2 = p - 1;
			/* We've either hit the end of the string, or we've
			   gotten past the wrap_length.  Back p2 up to the last space
			   before the wrap_length, if there is such a space.
			   Note that if the next character in the string (the character
			   immediately after the break point) is a space, we don't need
			   to back up at all.  We'll just print up to our current
			   location, do the newline, and skip to the next line. */
            if (p < charAry.length) {
                if (isspace(charAry[p])) {
                    offset = 1; /* the next character is white space.  We'll
								   want to skip that. */
                } else {
					/* back p2 up to the last white space before the break point */
                    while ((p2 > marker) && !isspace(charAry[p2])) {
                        p2--;
                    }
                    offset = 0;
                }
            }
			/* If the line was completely filled (no place to break),
			   we'll just copy the whole line out and force a break. */
            if (p2 == marker) {
                p2 = p - 1;
                if (debug) {
                    System.err.println("Step 3: no opportunity for break, forcing..");
                }
            } else {
                if (debug) {
                    System.err.println("Step 3: found break at column " + p2);
                }
            }
            if (!isspace(charAry[p2])) {
				/* If weren't were able to back up to a space, copy
				   out the whole line, including the break character
				   (in this case, we'll be making the string one
				   character longer by inserting a newline). */
                result.append(inString.substring(marker, p2 + 1));
            } else {
				/* The break character is whitespace.  We'll
				   copy out the characters up to but not
				   including the break character, which
				   we will effectively replace with a
				   newline. */
                result.append(inString.substring(marker, p2));
            }
			/* If we have not reached the end of the string, newline */
            if (p < charAry.length) {
                result.append("\n");
                if (prepend != null) {
                    result.append(prepend);
                }
            }
            p = marker = p2 + 1 + offset;
        }
        return result.toString();
    }

    public static String wrap(String inString, int wrap_length) {
        return wrap(inString, wrap_length, null);
    }

    public static String wrapToolTip(String inString) {
        return wrap(inString, 60, null);
    }

    public static boolean isspace(char c) {
        return (c == '\n' || c == ' ' || c == '\t');
    }

    /**
     * <p>This method takes the input String and strips tabs out of it,
     * replacing them with appropriate spaces.</p>
     */
    public static String deTabify(String input) {
        StringBuffer result = new StringBuffer();
        char characters[] = input.toCharArray();
        int j = 0;
        for (int i = 0; i < characters.length; i++) {
            if (characters[i] == '\t') {
                do {
                    result.append(" ");
                    j++;
                } while (j % 8 != 0);
            } else {
                result.append(characters[i]);
                j++;
            }
        }
        return result.toString();
    }


}
