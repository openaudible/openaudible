/**
 * @author : Paul Taylor
 * @author : Eric Farng
 * <p>
 * Version @version:$Id: StringNullTerminated.java 836 2009-11-12 15:44:07Z paultaylor $
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

import org.jaudiotagger.tag.id3.AbstractTagFrameBody;
import org.jaudiotagger.tag.id3.valuepair.TextEncoding;

/**
 * Represents a String whose size is determined by finding of a null character at the end of the String with fixed text encoding.
 * <p/>
 * The String will be encoded using the default encoding regardless of what encoding may be specified in the framebody
 */
public class StringNullTerminated extends TextEncodedStringNullTerminated {
    /**
     * Creates a new ObjectStringNullTerminated datatype.
     *
     * @param identifier identifies the frame type
     * @param frameBody
     */
    public StringNullTerminated(String identifier, AbstractTagFrameBody frameBody) {
        super(identifier, frameBody);
    }

    public StringNullTerminated(StringNullTerminated object) {
        super(object);
    }

    public boolean equals(Object obj) {
        return obj instanceof StringNullTerminated && super.equals(obj);
    }

    protected String getTextEncodingCharSet() {
        return TextEncoding.CHARSET_ISO_8859_1;
    }
}
