/**
 * @author : Paul Taylor
 * @author : Eric Farng
 * <p>
 * Version @version:$Id: BooleanString.java 836 2009-11-12 15:44:07Z paultaylor $
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
 */
package org.jaudiotagger.tag.datatype;

import org.jaudiotagger.tag.InvalidDataTypeException;
import org.jaudiotagger.tag.id3.AbstractTagFrameBody;

public class BooleanString extends AbstractDataType {
    /**
     * Creates a new ObjectBooleanString datatype.
     *
     * @param identifier
     * @param frameBody
     */
    public BooleanString(String identifier, AbstractTagFrameBody frameBody) {
        super(identifier, frameBody);
    }

    public BooleanString(BooleanString object) {
        super(object);
    }

    /**
     * @return
     */
    public int getSize() {
        return 1;
    }

    public boolean equals(Object obj) {
        return obj instanceof BooleanString && super.equals(obj);

    }

    /**
     * @param offset
     * @throws NullPointerException
     * @throws IndexOutOfBoundsException
     */
    public void readByteArray(byte[] arr, int offset) throws InvalidDataTypeException {
        byte b = arr[offset];
        value = b != '0';
    }

    /**
     * @return
     */
    public String toString() {
        return "" + value;
    }

    /**
     * @return
     */
    public byte[] writeByteArray() {
        byte[] booleanValue = new byte[1];
        if (value == null) {
            booleanValue[0] = '0';
        } else {
            if ((Boolean) value) {
                booleanValue[0] = '0';
            } else {
                booleanValue[0] = '1';
            }
        }
        return booleanValue;
    }
}
