/*   **********************************************************************  **
 **	 	Copyright notice																											 **
 **                                                                          **
 **   (c) 2003-2005 RSSOwl Development Team                                  **
 **   http://www.rssowl.org/                                                 **
 **                                                                          **
 **   All rights reserved                                                    **
 **																																					 **
 **		This program and the accompanying materials are made available under 	 **
 **	 	the terms of the Common Public License v1.0 which accompanies this		 **
 **	 	distribution, and is available at:																		 **
 **		http://www.rssowl.org/legal/cpl-v10.html															 **
 **																																					 **
 **   A copy is found in the file cpl-v10.html and important notices to the  **
 **   license from the team is found in the textfile LICENSE.txt distributed **
 **   in this package.                                                       **
 **	 																																				 **
 **		This copyright notice MUST APPEAR in all copies of the file!					 **
 **																																					 **
 **	 	Contributors:																													 **
 **	  	RSSOwl - initial API and implementation (bpasero@rssowl.org)				 **
 **																																					 **
 **	 **********************************************************************	 */

package org.openaudible.desktop.swt.util.shop;


import org.openaudible.desktop.swt.gui.GUI;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.Locale;
import java.util.Random;


/**
 * Class provides some methods to handle / work with Strings.
 *
 * @author <a href="mailto:bpasero@rssowl.org">Benjamin Pasero </a>
 * @version 1.0.2
 */
public class StringShop {

    static Random r = new Random();

    /**
     * This utility class constructor is hidden
     */
    private StringShop() {
        // Protect default constructor
    }

    /**
     * Create a valid filename from the given String.
     *
     * @param str The String to convert to a valid filename.
     * @return String A valid filename.
     */
    public static String createFileName(String str) {

        /* Replace some special chars */
        str = str.replaceAll(" > ", "_");
        str = str.replaceAll(": ", "_");
        str = str.replaceAll(" ", "_");

        /* If filename only contains of special chars */
        if (str.matches("[_]+"))
            str = "rssowl";

        return str;
    }

    /**
     * Decode ISO encoded character
     *
     * @param isoBytes Bytes representing the char
     * @return String representing the encoded char
     */
    public static String decodeISOChar(byte[] isoBytes) {

        /* Return the Entity value if decode fails */
        String decodedChar = String.valueOf("&" + (int) isoBytes[0] + ";");

        /* Only decode if Charset ISO-8859-1 is available */
        if (Charset.isSupported("ISO-8859-1")) {
            Charset isoCharset = Charset.forName("ISO-8859-1");
            CharsetDecoder decoder = isoCharset.newDecoder();

            ByteBuffer isoBytesRef = ByteBuffer.wrap(isoBytes);
            CharBuffer decodedCharBuf = null;
            try {
                decodedCharBuf = decoder.decode(isoBytesRef);
            } catch (CharacterCodingException e) {
                GUI.logger.info("decodeChar() " + e.toString());
            }
            return String.valueOf(decodedCharBuf);
        }
        return decodedChar;
    }

    /**
     * Returns TRUE in case the given String has a value that is not "".
     *
     * @param str The String to check
     * @return boolean TRUE in case the String has an value not ""
     */
    public static boolean isset(String str) {
        return (str != null && !str.equals(""));
    }

    /**
     * Check if a given char is a terminating an URL. URL-terminating symbols are
     * whitespaces, single- and double quotes.
     *
     * @param ch Any char
     * @return TRUE if the char is terminating
     */
    public static boolean isTerminating(String ch) {
        if (ch == null)
            return true;
        return (ch.equals(" ") || ch.equals("\n") || ch.equals(System.getProperty("line.separator")) || ch.equals("\t") || ch.equals("\"") || ch.equals("'"));
    }

    /**
     * Returns TRUE if the String only cosists of whitespaces or is null / empty.
     *
     * @param str The string to check
     * @return boolean TRUE if the String only consists of whitespaces
     */
    public static boolean isWhiteSpaceOrEmpty(String str) {
        if (str == null || str.equals(""))
            return true;
        return str.matches("[\\s]+");
    }

    /**
     * Trims the given String to the given length and appends "..." Also replaces
     * all occurances of "&" with "&&" if the String is used as title for controls
     * that display "&" as mnemonic.
     *
     * @param str             The String to Trim
     * @param length          The max. length of the String
     * @param escapeMnemonics If TRUE all "&" will be replaced with "&&"
     * @return String The trimmed String
     */
    public static String pointTrim(String str, int length, boolean escapeMnemonics) {

        /* Remove mnemonics if needed and replace "&" with "&&" */
        if (escapeMnemonics)
            str = replaceAll(str, "&", "&&");

        /* Return a Substring and append "..." */
        return (str.length() >= length) ? str.substring(0, length) + "..." : str;
    }

    /**
     * Substitute wildcards in a String and return it
     *
     * @param str          The String to format
     * @param wildcard     Array of wildcards
     * @param substitution Array of wildcards for the substitution of the
     *                     wildcards.
     * @return String The formatted String
     */
    public static String printf(String str, String[] wildcard, String[] substitution) {

        /* Replace each wildcard with its substitution */
        for (int i = 0; i < wildcard.length; i++) {
            str = StringShop.replaceAll(str, wildcard[i], substitution[i]);
        }

        return str;
    }

    /**
     * This method does exactly the same as String.replaceAll() with the
     * difference that no regular expressions are used to perform the replacement.
     *
     * @param str     The source String to search and replace
     * @param search  The search term that should get replaced
     * @param replace The value that replaces the search term
     * @return String The new String with all replaced search terms
     */
    public static String replaceAll(String str, String search, String replace) {
        int start = 0;
        int pos;
        StringBuffer result = new StringBuffer(str.length());
        while ((pos = str.indexOf(search, start)) >= 0) {
            result.append(str.substring(start, pos));
            result.append(replace);
            start = pos + search.length();
        }
        result.append(str.substring(start));

        return result.toString();
    }

    /**
     * Convert the non ASCII-characters of a String into Unicode HTML entities.
     *
     * @param str The String to convert
     * @return String The converted String
     */
    public static String unicodeToEntities(String str) {
        StringBuffer strBuf = new StringBuffer();

        /* For each character */
        for (int i = 0; i < str.length(); i++) {
            char ch = str.charAt(i);

            /* This is a non ASCII, non Whitespace character */
            if (!((ch >= 0x0020) && (ch <= 0x007e)) && !Character.isWhitespace(ch)) {
                strBuf.append("&#x");
                String hex = Integer.toHexString(str.charAt(i) & 0xFFFF);

                if (hex.length() == 2)
                    strBuf.append("00");

                strBuf.append(hex.toUpperCase(Locale.ENGLISH));
                strBuf.append(";");
            }

            /* This is an ASCII character */
            else {
                strBuf.append(ch);
            }
        }
        return new String(strBuf);
    }
    // r.setSeed(System.currentTimeMillis());

    public static String randomString(int maxLen) {
        String out = "";

        for (int x = 0; x < maxLen; x++) {
            int a = r.nextInt(26);
            a = r.nextBoolean() ? ('a' + a) : ('A' + a);
            out += (char) a;
        }

        return out;
    }

}