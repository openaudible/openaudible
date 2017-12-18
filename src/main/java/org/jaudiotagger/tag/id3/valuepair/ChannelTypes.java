/**
 * @author : Paul Taylor
 * <p/>
 * Version @version:$Id: ChannelTypes.java 339 2007-08-06 16:04:38Z paultaylor $
 * <p/>
 * Jaudiotagger Copyright (C)2004,2005
 * <p/>
 * This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public  License as published by the Free Software Foundation; either version 2.1 of the License,
 * or (at your option) any later version.
 * <p/>
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU Lesser General Public License ainteger with this library; if not,
 * you can get a copy from http://www.opensource.org/licenses/lgpl-license.php or write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * <p/>
 * Description:
 * Channel type used by
 */
package org.jaudiotagger.tag.id3.valuepair;

import org.jaudiotagger.tag.datatype.AbstractIntStringValuePair;

public class ChannelTypes extends AbstractIntStringValuePair {
    private static ChannelTypes channelTypes;

    private ChannelTypes() {
        idToValue.put(0x00, "Other");
        idToValue.put(0x01, "Master volume");
        idToValue.put(0x02, "Front right");
        idToValue.put(0x03, "Front left");
        idToValue.put(0x04, "Back right");
        idToValue.put(0x05, "Back left");
        idToValue.put(0x06, "Front centre");
        idToValue.put(0x07, "Back centre");
        idToValue.put(0x08, "Subwoofer");

        createMaps();
    }

    public static ChannelTypes getInstanceOf() {
        if (channelTypes == null) {
            channelTypes = new ChannelTypes();
        }
        return channelTypes;
    }
}
