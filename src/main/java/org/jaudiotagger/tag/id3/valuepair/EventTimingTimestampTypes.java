/**
 * @author : Paul Taylor
 * <p/>
 * Version @version:$Id: EventTimingTimestampTypes.java 867 2010-01-28 16:27:11Z paultaylor $
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
 * You should have received a copy of the GNU Lesser General Public License along with this library; if not,
 * you can get a copy from http://www.opensource.org/licenses/lgpl-license.php or write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * <p/>
 * Description:
 */
package org.jaudiotagger.tag.id3.valuepair;

import org.jaudiotagger.tag.datatype.AbstractIntStringValuePair;

public class EventTimingTimestampTypes extends AbstractIntStringValuePair {

    public static final int TIMESTAMP_KEY_FIELD_SIZE = 1;
    private static EventTimingTimestampTypes eventTimingTimestampTypes;

    private EventTimingTimestampTypes() {
        idToValue.put(1, "Absolute time using MPEG [MPEG] frames as unit");
        idToValue.put(2, "Absolute time using milliseconds as unit");

        createMaps();
    }

    public static EventTimingTimestampTypes getInstanceOf() {
        if (eventTimingTimestampTypes == null) {
            eventTimingTimestampTypes = new EventTimingTimestampTypes();
        }
        return eventTimingTimestampTypes;
    }
}
