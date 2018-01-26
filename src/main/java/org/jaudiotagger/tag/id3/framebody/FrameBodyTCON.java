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
import org.jaudiotagger.tag.id3.ID3v24Frames;

import java.nio.ByteBuffer;

/**
 * Content type Text information frame.
 * <p/>
 * <p>The 'Content type', which previously was
 * stored as a one byte numeric value only, is now a numeric string. You
 * may use one or several of the types as ID3v1.1 did or, since the
 * category list would be impossible to maintain with accurate and up to
 * date categories, define your own.
 * </p><p>
 * ID3V23:References to the ID3v1 genres can be made by, as first byte, enter
 * "(" followed by a number from the genres list (appendix A) and
 * ended with a ")" character. This is optionally followed by a
 * refinement, e.g. "(21)" or "(4)Eurodisco". Several references can be
 * made in the same frame, e.g. "(51)(39)". If the refinement should
 * begin with a "(" character it should be replaced with "((", e.g. "((I
 * can figure out any genre)" or "(55)((I think...)". The following new
 * content types is defined in ID3v2 and is implemented in the same way
 * as the numeric content types, e.g. "(RX)".
 * </p><p><table border=0 width="70%">
 * <tr><td>RX</td><td width="100%">Remix</td></tr>
 * <tr><td>CR</td><td>Cover</td></tr>
 * </table></p>
 * <p/>
 * <p>For more details, please refer to the ID3 specifications:
 * <ul>
 * <li><a href="http://www.id3.org/id3v2.3.0.txt">ID3 v2.3.0 Spec</a>
 * </ul>
 * <p/>
 * ID3V24:The 'Content type', which ID3v1 was stored as a one byte numeric
 * value only, is now a string. You may use one or several of the ID3v1
 * types as numerical strings, or, since the category list would be
 * impossible to maintain with accurate and up to date categories,
 * define your own. Example: "21" $00 "Eurodisco" $00
 * <p/>
 * You may also use any of the following keywords:
 * </p><p><table border=0 width="70%">
 * <tr><td>RX</td><td width="100%">Remix</td></tr>
 * <tr><td>CR</td><td>Cover</td></tr>
 * </table></p>
 *
 * @author : Paul Taylor
 * @author : Eric Farng
 * @version $Id: FrameBodyTCON.java 832 2009-11-12 13:25:38Z paultaylor $
 */
public class FrameBodyTCON extends AbstractFrameBodyTextInfo implements ID3v24FrameBody, ID3v23FrameBody {
    /**
     * Creates a new FrameBodyTCON datatype.
     */
    public FrameBodyTCON() {
    }

    public FrameBodyTCON(FrameBodyTCON body) {
        super(body);
    }

    /**
     * Creates a new FrameBodyTCON datatype.
     *
     * @param textEncoding
     * @param text
     */
    public FrameBodyTCON(byte textEncoding, String text) {
        super(textEncoding, text);
    }

    /**
     * Creates a new FrameBodyTCON datatype.
     *
     * @param byteBuffer
     * @param frameSize
     * @throws InvalidTagException
     */
    public FrameBodyTCON(ByteBuffer byteBuffer, int frameSize) throws InvalidTagException {
        super(byteBuffer, frameSize);
    }


    /**
     * The ID3v2 frame identifier
     *
     * @return the ID3v2 frame identifier  for this frame type
     */
    public String getIdentifier() {
        return ID3v24Frames.FRAME_ID_GENRE;
    }
}
