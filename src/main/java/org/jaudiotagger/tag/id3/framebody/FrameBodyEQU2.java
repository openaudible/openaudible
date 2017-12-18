/**
 * @author : Paul Taylor
 * @author : Eric Farng
 * <p>
 * Version @version:$Id: FrameBodyEQU2.java 832 2009-11-12 13:25:38Z paultaylor $
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
package org.jaudiotagger.tag.id3.framebody;

import org.jaudiotagger.tag.InvalidTagException;
import org.jaudiotagger.tag.datatype.ByteArraySizeTerminated;
import org.jaudiotagger.tag.datatype.DataTypes;
import org.jaudiotagger.tag.id3.ID3v24Frames;

import java.nio.ByteBuffer;


/**
 * Equalisation (2)
 * <p/>
 * This is another subjective, alignment frame. It allows the user to
 * predefine an equalisation curve within the audio file. There may be
 * more than one "EQU2" frame in each tag, but only one with the same
 * identification string.
 * <p/>
 * <Header of 'Equalisation (2)', ID: "EQU2">
 * Interpolation method  $xx
 * Identification        <text string> $00
 * <p/>
 * The 'interpolation method' describes which method is preferred when
 * an interpolation between the adjustment point that follows. The
 * following methods are currently defined:
 * <p/>
 * $00  Band
 * No interpolation is made. A jump from one adjustment level to
 * another occurs in the middle between two adjustment points.
 * $01  Linear
 * Interpolation between adjustment points is linear.
 * <p/>
 * The 'identification' string is used to identify the situation and/or
 * device where this adjustment should apply. The following is then
 * repeated for every adjustment point
 * <p/>
 * Frequency          $xx xx
 * Volume adjustment  $xx xx
 * <p/>
 * The frequency is stored in units of 1/2 Hz, giving it a range from 0
 * to 32767 Hz.
 * <p/>
 * The volume adjustment is encoded as a fixed point decibel value, 16
 * bit signed integer representing (adjustment*512), giving +/- 64 dB
 * with a precision of 0.001953125 dB. E.g. +2 dB is stored as $04 00
 * and -2 dB is $FC 00.
 * <p/>
 * Adjustment points should be ordered by frequency and one frequency
 * should only be described once in the frame.
 */
public class FrameBodyEQU2 extends AbstractID3v2FrameBody implements ID3v24FrameBody {
    /**
     * Creates a new FrameBodyEQU2 datatype.
     */
    public FrameBodyEQU2() {

    }

    public FrameBodyEQU2(FrameBodyEQU2 body) {
        super(body);
    }

    /**
     * Creates a new FrameBodyEQU2 datatype.
     *
     * @param byteBuffer
     * @param frameSize
     * @throws InvalidTagException if unable to create framebody from buffer
     */
    public FrameBodyEQU2(ByteBuffer byteBuffer, int frameSize) throws InvalidTagException {
        super(byteBuffer, frameSize);
    }

    /**
     * The ID3v2 frame identifier
     *
     * @return the ID3v2 frame identifier  for this frame type
     */
    public String getIdentifier() {
        return ID3v24Frames.FRAME_ID_EQUALISATION2;
    }

    /**
     *
     */
    protected void setupObjectList() {
        objectList.add(new ByteArraySizeTerminated(DataTypes.OBJ_DATA, this));
    }
}
