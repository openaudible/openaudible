/**
 * @author : Paul Taylor
 * <p/>
 * Version @version:$Id: EventTimingTypes.java 339 2007-08-06 16:04:38Z paultaylor $
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
 */
package org.jaudiotagger.tag.id3.valuepair;

import org.jaudiotagger.tag.datatype.AbstractIntStringValuePair;

public class EventTimingTypes extends AbstractIntStringValuePair {
    private static EventTimingTypes eventTimingTypes;

    private EventTimingTypes() {
        idToValue.put(0x00, "Padding (has no meaning)");
        idToValue.put(0x01, "End of initial silence");
        idToValue.put(0x02, "Intro start");
        idToValue.put(0x03, "Main part start");
        idToValue.put(0x04, "Outro start");
        idToValue.put(0x05, "Outro end");
        idToValue.put(0x06, "Verse start");
        idToValue.put(0x07, "Refrain start");
        idToValue.put(0x08, "Interlude start");
        idToValue.put(0x09, "Theme start");
        idToValue.put(0x0A, "Variation start");
        idToValue.put(0x0B, "Key change");
        idToValue.put(0x0C, "Time change");
        idToValue.put(0x0D, "Momentary unwanted noise (Snap, Crackle & Pop)");
        idToValue.put(0x0E, "Sustained noise");
        idToValue.put(0x0F, "Sustained noise end");
        idToValue.put(0x10, "Intro end");
        idToValue.put(0x11, "Main part end");
        idToValue.put(0x12, "Verse end");
        idToValue.put(0x13, "Refrain end");
        idToValue.put(0x14, "Theme end");
        idToValue.put(0x15, "Profanity");
        idToValue.put(0x16, "Profanity end");
        idToValue.put(0xFD, "Audio end (start of silence)");
        idToValue.put(0xFE, "Audio file ends");

        createMaps();
    }

    public static EventTimingTypes getInstanceOf() {
        if (eventTimingTypes == null) {
            eventTimingTypes = new EventTimingTypes();
        }
        return eventTimingTypes;
    }
}
