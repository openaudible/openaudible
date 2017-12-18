/**
 * @author : Paul Taylor
 * @author : Eric Farng
 * <p>
 * Version @version:$Id: ByteArraySizeTerminated.java 976 2011-06-08 10:05:34Z paultaylor $
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

/**
 * Represents a stream of bytes, continuing until the end of the buffer. Usually used for binary data or where
 * we havent yet mapped the data to a better fitting type.
 */
public class ByteArraySizeTerminated extends AbstractDataType {
    public ByteArraySizeTerminated(String identifier, AbstractTagFrameBody frameBody) {
        super(identifier, frameBody);
    }

    public ByteArraySizeTerminated(ByteArraySizeTerminated object) {
        super(object);
    }

    /**
     * Return the size in byte of this datatype
     *
     * @return the size in bytes
     */
    public int getSize() {
        int len = 0;

        if (value != null) {
            len = ((byte[]) value).length;
        }

        return len;
    }

    public boolean equals(Object obj) {
        return obj instanceof ByteArraySizeTerminated && super.equals(obj);

    }

    /**
     * @param arr
     * @param offset
     * @throws NullPointerException
     * @throws IndexOutOfBoundsException
     */
    public void readByteArray(byte[] arr, int offset) throws InvalidDataTypeException {
        if (arr == null) {
            throw new NullPointerException("Byte array is null");
        }

        if (offset < 0) {
            throw new IndexOutOfBoundsException("Offset to byte array is out of bounds: offset = " + offset + ", array.length = " + arr.length);
        }

        //Empty Byte Array
        if (offset >= arr.length) {
            value = null;
            return;
        }

        int len = arr.length - offset;
        value = new byte[len];
        System.arraycopy(arr, offset, value, 0, len);
    }

    /**
     * Because this is usually binary data and could be very long we just return
     * the number of bytes held
     *
     * @return the number of bytes
     */
    public String toString() {
        return getSize() + " bytes";
    }

    /**
     * Write contents to a byte array
     *
     * @return a byte array that that contians the data that should be perisisted to file
     */
    public byte[] writeByteArray() {
        logger.config("Writing byte array" + this.getIdentifier());
        return (byte[]) value;
    }
}
