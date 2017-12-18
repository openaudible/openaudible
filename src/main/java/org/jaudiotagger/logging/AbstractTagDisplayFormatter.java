/**
 * @author : Paul Taylor
 * @author : Eric Farng
 * <p>
 * Version @version:$Id: AbstractTagDisplayFormatter.java 836 2009-11-12 15:44:07Z paultaylor $
 * <p>
 * MusicTag Copyright (C)2003,2004
 * <p>
 * This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public  License as published by the Free Software Foundation; either version 2.1 of the License,
 * or (at your option) any later version.
 * <p>
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Lesser General Public License along with this library; if not,
 * you can get a copy from http://www.opensource.org/licenses/lgpl-license.php or write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * <p>
 * Description:
 * This abstract class defines methods for writing out the contents of a tag in a user-friendly way
 * Concrete subclasses could implement different versions such as XML Output, PDF and so on. The tag
 * in all cases is diaplyed as a sort of tree hierachy.
 */
package org.jaudiotagger.logging;

import java.util.HashMap;

/**
 * Abstract class that provides structure to use for displaying a files metadata content
 */
public abstract class AbstractTagDisplayFormatter {
    private static HashMap<String, String> hexBinaryMap = new HashMap<String, String>();

    static {
        hexBinaryMap.put("0", "0000");
        hexBinaryMap.put("1", "0001");
        hexBinaryMap.put("2", "0010");
        hexBinaryMap.put("3", "0011");
        hexBinaryMap.put("4", "0100");
        hexBinaryMap.put("5", "0101");
        hexBinaryMap.put("6", "0110");
        hexBinaryMap.put("7", "0111");
        hexBinaryMap.put("8", "1000");
        hexBinaryMap.put("9", "1001");
        hexBinaryMap.put("a", "1010");
        hexBinaryMap.put("b", "1011");
        hexBinaryMap.put("c", "1100");
        hexBinaryMap.put("d", "1101");
        hexBinaryMap.put("e", "1110");
        hexBinaryMap.put("f", "1111");
    }

    protected int level;

    /**
     * Use to display headers as their binary representation
     *
     * @param buffer
     * @return
     */
    public static String displayAsBinary(byte buffer) {
        //Convert buffer to hex representation
        String hexValue = Integer.toHexString(buffer);
        String char1 = "";
        String char2 = "";
        try {
            if (hexValue.length() == 8) {
                char1 = hexValue.substring(6, 7);
                char2 = hexValue.substring(7, 8);
            } else if (hexValue.length() == 2) {
                char1 = hexValue.substring(0, 1);
                char2 = hexValue.substring(1, 2);
            } else if (hexValue.length() == 1) {
                char1 = "0";
                char2 = hexValue.substring(0, 1);
            }
        } catch (StringIndexOutOfBoundsException se) {
            return "";
        }
        return hexBinaryMap.get(char1) + hexBinaryMap.get(char2);
    }

    public abstract void openHeadingElement(String type, String value);

    public abstract void openHeadingElement(String type, boolean value);

    public abstract void openHeadingElement(String type, int value);

    public abstract void closeHeadingElement(String type);

    public abstract void addElement(String type, String value);

    public abstract void addElement(String type, int value);

    public abstract void addElement(String type, boolean value);

    public abstract String toString();
}
