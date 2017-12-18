/*
 *  MusicTag Copyright (C)2003,2004
 *
 *  This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser
 *  General Public  License as published by the Free Software Foundation; either version 2.1 of the License,
 *  or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 *  the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *  See the GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License along with this library; if not,
 *  you can get a copy from http://www.opensource.org/licenses/lgpl-license.php or write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package org.jaudiotagger.tag.id3.framebody;

import org.jaudiotagger.tag.InvalidTagException;
import org.jaudiotagger.tag.datatype.ByteArraySizeTerminated;
import org.jaudiotagger.tag.datatype.DataTypes;
import org.jaudiotagger.tag.datatype.NumberHashMap;
import org.jaudiotagger.tag.id3.ID3v24Frames;
import org.jaudiotagger.tag.id3.valuepair.EventTimingTimestampTypes;

import java.nio.ByteBuffer;

/**
 * Synchronised tempo codes frame.
 * <p/>
 * <p/>
 * For a more accurate description of the tempo of a musical piece this
 * frame might be used. After the header follows one byte describing
 * which time stamp format should be used. Then follows one or more
 * tempo codes. Each tempo code consists of one tempo part and one time
 * part. The tempo is in BPM described with one or two bytes. If the
 * first byte has the value $FF, one more byte follows, which is added
 * to the first giving a range from 2 - 510 BPM, since $00 and $01 is
 * reserved. $00 is used to describe a beat-free time period, which is
 * not the same as a music-free time period. $01 is used to indicate one
 * single beat-stroke followed by a beat-free period.
 * </p><p>
 * The tempo descriptor is followed by a time stamp. Every time the
 * tempo in the music changes, a tempo descriptor may indicate this for
 * the player. All tempo descriptors should be sorted in chronological
 * order. The first beat-stroke in a time-period is at the same time as
 * the beat description occurs. There may only be one "SYTC" frame in
 * each tag.
 * </p><p><table border=0 width="70%">
 * <tr><td colspan=2> &lt;Header for 'Synchronised tempo codes', ID: "SYTC"&gt;</td></tr>
 * <tr><td>Time stamp format</td><td width="80%">$xx</td></tr>
 * <tr><td>Tempo data </td><td>&lt;binary data&gt;</td></tr>
 * </table></p><p>
 * Where time stamp format is:
 * </p><p>
 * $01 Absolute time, 32 bit sized, using MPEG frames as unit<br>
 * $02 Absolute time, 32 bit sized, using milliseconds as unit
 * </p><p>
 * Abolute time means that every stamp contains the time from the
 * beginning of the file.
 * </p>
 * <p/>
 * <p>For more details, please refer to the ID3 specifications:
 * <ul>
 * <li><a href="http://www.id3.org/id3v2.3.0.txt">ID3 v2.3.0 Spec</a>
 * </ul>
 *
 * @author : Paul Taylor
 * @author : Eric Farng
 * @version $Id: FrameBodySYTC.java 832 2009-11-12 13:25:38Z paultaylor $
 */
public class FrameBodySYTC extends AbstractID3v2FrameBody implements ID3v24FrameBody, ID3v23FrameBody {
    /**
     * Creates a new FrameBodySYTC datatype.
     */
    public FrameBodySYTC() {
    }

    /**
     * @param timeStampFormat
     * @param tempo
     */
    public FrameBodySYTC(int timeStampFormat, byte[] tempo) {
        setObjectValue(DataTypes.OBJ_TIME_STAMP_FORMAT, timeStampFormat);
        setObjectValue(DataTypes.OBJ_DATA, tempo);
    }

    /**
     * Creates a new FrameBody from buffer
     *
     * @param byteBuffer
     * @param frameSize
     * @throws InvalidTagException
     */
    public FrameBodySYTC(ByteBuffer byteBuffer, int frameSize) throws InvalidTagException {
        super(byteBuffer, frameSize);
    }

    /**
     * Copy constructor
     *
     * @param body
     */
    public FrameBodySYTC(FrameBodySYTC body) {
        super(body);
    }

    /**
     * The ID3v2 frame identifier
     *
     * @return the ID3v2 frame identifier  for this frame type
     */
    public String getIdentifier() {
        return ID3v24Frames.FRAME_ID_SYNC_TEMPO;
    }


    protected void setupObjectList() {
        objectList.add(new NumberHashMap(DataTypes.OBJ_TIME_STAMP_FORMAT, this, EventTimingTimestampTypes.TIMESTAMP_KEY_FIELD_SIZE));
        objectList.add(new ByteArraySizeTerminated(DataTypes.OBJ_DATA, this));
    }
}