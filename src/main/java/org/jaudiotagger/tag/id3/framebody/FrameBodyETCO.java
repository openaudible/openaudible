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
import org.jaudiotagger.tag.id3.ID3v24Frames;

import java.nio.ByteBuffer;

/**
 * Event timing codes frame.
 * <p/>
 * <p/>
 * This frame allows synchronisation with key events in a song or sound.
 * The header is:
 * </p><p><table border=0 width="70%">
 * <tr><td colspan=2> &lt;Header for 'Event timing codes', ID: "ETCO"&gt;</td></tr>
 * <tr><td>Time stamp format</td><td width="80%">$xx</td></tr>
 * </table></p><p>
 * Where time stamp format is:
 * </p><p>
 * $01 Absolute time, 32 bit sized, using <a href="#MPEG">MPEG</a> frames as unit<br>
 * $02 Absolute time, 32 bit sized, using milliseconds as unit
 * </p><p>
 * Abolute time means that every stamp contains the time from the
 * beginning of the file.
 * </p><p>
 * Followed by a list of key events in the following format:
 * </p><p><table border=0 width="70%">
 * <tr><td>Type of event</td><td width="80%">$xx</td></tr>
 * <tr><td>Time stamp</td><td>$xx (xx ...)</td></tr>
 * </table></p><p>
 * The 'Time stamp' is set to zero if directly at the beginning of the
 * sound or after the previous event. All events should be sorted in
 * chronological order. The type of event is as follows:
 * </p><p><table border=0 width="70%">
 * <tr><td>$00    </td><td width="80%">padding (has no meaning)</td></tr>
 * <tr><td>$01    </td><td>end of initial silence              </td></tr>
 * <tr><td>$02    </td><td>intro start                         </td></tr>
 * <tr><td>$03    </td><td>mainpart start                      </td></tr>
 * <tr><td>$04    </td><td>outro start                         </td></tr>
 * <tr><td>$05    </td><td>outro end                           </td></tr>
 * <tr><td>$06    </td><td>verse start                         </td></tr>
 * <tr><td>$07    </td><td>refrain start                       </td></tr>
 * <tr><td>$08    </td><td>interlude start                     </td></tr>
 * <tr><td>$09    </td><td>theme start                         </td></tr>
 * <tr><td>$0A    </td><td>variation start                     </td></tr>
 * <tr><td>$0B    </td><td>key change                          </td></tr>
 * <tr><td>$0C    </td><td>time change                         </td></tr>
 * <tr><td>$0D    </td><td>momentary unwanted noise (Snap, Crackle & Pop)</td></tr>
 * <tr><td>$0E    </td><td>sustained noise                     </td></tr>
 * <tr><td>$0F    </td><td>sustained noise end                 </td></tr>
 * <tr><td>$10    </td><td>intro end                           </td></tr>
 * <tr><td>$11    </td><td>mainpart end                        </td></tr>
 * <tr><td>$12    </td><td>verse end                           </td></tr>
 * <tr><td>$13    </td><td>refrain end                         </td></tr>
 * <tr><td>$14    </td><td>theme end                           </td></tr>
 * <tr><td>$15-$DF</td><td>reserved for future use             </td></tr>
 * <tr><td>$E0-$EF</td><td>not predefined sync 0-F             </td></tr>
 * <tr><td>$F0-$FC</td><td>reserved for future use             </td></tr>
 * <tr><td>$FD    </td><td>audio end (start of silence)        </td></tr>
 * <tr><td>$FE    </td><td>audio file ends                     </td></tr>
 * <tr><td>$FF</td><td>one more byte of events follows (all the following bytes with the value $FF have the same function)</td></tr>
 * </table></center>
 * </p><p>
 * Terminating the start events such as "intro start" is not required.
 * The 'Not predefined sync's ($E0-EF) are for user events. You might
 * want to synchronise your music to something, like setting of an
 * explosion on-stage, turning on your screensaver etc.
 * </p><p>
 * There may only be one "ETCO" frame in each tag.</p>
 * <p/>
 * <p>For more details, please refer to the ID3 specifications:
 * <ul>
 * <li><a href="http://www.id3.org/id3v2.3.0.txt">ID3 v2.3.0 Spec</a>
 * </ul>
 *
 * @author : Paul Taylor
 * @author : Eric Farng
 * @version $Id: FrameBodyETCO.java 832 2009-11-12 13:25:38Z paultaylor $
 */
public class FrameBodyETCO extends AbstractID3v2FrameBody implements ID3v24FrameBody, ID3v23FrameBody {
    /**
     * Creates a new FrameBodyETCO datatype.
     */
    public FrameBodyETCO() {

    }

    public FrameBodyETCO(FrameBodyETCO body) {
        super(body);
    }

    /**
     * Creates a new FrameBodyETCO datatype.
     *
     * @param byteBuffer
     * @param frameSize
     * @throws InvalidTagException if unable to create framebody from buffer
     */
    public FrameBodyETCO(ByteBuffer byteBuffer, int frameSize) throws InvalidTagException {
        super(byteBuffer, frameSize);
    }

    /**
     * @return
     */
    public String getIdentifier() {
        return ID3v24Frames.FRAME_ID_EVENT_TIMING_CODES;
    }

    /**
     *
     */
    protected void setupObjectList() {
        objectList.add(new ByteArraySizeTerminated(DataTypes.OBJ_DATA, this));
    }
}
