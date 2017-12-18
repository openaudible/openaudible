/**
 * @author : Paul Taylor
 * @author : Eric Farng
 * <p>
 * Version @version:$Id: FrameBodyTDTG.java 869 2010-02-01 14:44:01Z paultaylor $
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
import org.jaudiotagger.tag.id3.ID3v24Frames;

import java.nio.ByteBuffer;


/**
 * <p>The 'Tagging time' frame contains a timestamp describing then the
 * audio was tagged. Timestamp format is described in the ID3v2
 * structure document </p>
 */
public class FrameBodyTDTG extends AbstractFrameBodyTextInfo implements ID3v24FrameBody {

    /**
     * Creates a new FrameBodyTDTG datatype.
     */
    public FrameBodyTDTG() {
    }

    public FrameBodyTDTG(FrameBodyTDTG body) {
        super(body);
    }

    /**
     * Creates a new FrameBodyTDTG datatype.
     *
     * @param textEncoding
     * @param text
     */
    public FrameBodyTDTG(byte textEncoding, String text) {
        super(textEncoding, text);
    }

    /**
     * Creates a new FrameBodyTDTG datatype.
     *
     * @param byteBuffer
     * @param frameSize
     * @throws java.io.IOException
     * @throws InvalidTagException
     */
    public FrameBodyTDTG(ByteBuffer byteBuffer, int frameSize) throws InvalidTagException {
        super(byteBuffer, frameSize);
    }

    /**
     * @return the frame identifier
     */
    public String getIdentifier() {
        return ID3v24Frames.FRAME_ID_TAGGING_TIME;
    }


}
