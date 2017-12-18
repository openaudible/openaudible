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
import org.jaudiotagger.tag.datatype.StringNullTerminated;
import org.jaudiotagger.tag.id3.ID3v24Frames;

import java.nio.ByteBuffer;

/**
 * Private frame.
 * <p/>
 * <p/>
 * This frame is used to contain information from a software producer
 * that its program uses and does not fit into the other frames. The
 * frame consists of an 'Owner identifier' string and the binary data.
 * The 'Owner identifier' is a null-terminated string with a URL
 * containing an email address, or a link to a location where an email
 * address can be found, that belongs to the organisation responsible
 * for the frame. Questions regarding the frame should be sent to the
 * indicated email address. The tag may contain more than one "PRIV"
 * frame but only with different contents. It is recommended to keep the
 * number of "PRIV" frames as low as possible.
 * </p><p><table border=0 width="70%">
 * <tr><td colspan=2>&lt;Header for 'Private frame', ID: "PRIV"&gt;</td></tr>
 * <tr><td>Owner identifier</td><td width="80%">&lt;text string&gt; $00</td></tr>
 * <tr><td>The private data</td><td>&lt;binary data&gt;</td></tr>
 * </table></p>
 * <p/>
 * <p>For more details, please refer to the ID3 specifications:
 * <ul>
 * <li><a href="http://www.id3.org/id3v2.3.0.txt">ID3 v2.3.0 Spec</a>
 * </ul>
 *
 * @author : Paul Taylor
 * @author : Eric Farng
 * @version $Id: FrameBodyPRIV.java 832 2009-11-12 13:25:38Z paultaylor $
 */
public class FrameBodyPRIV extends AbstractID3v2FrameBody implements ID3v24FrameBody, ID3v23FrameBody {
    /**
     * Creates a new FrameBodyPRIV datatype.
     */
    public FrameBodyPRIV() {
        this.setObjectValue(DataTypes.OBJ_OWNER, "");
        this.setObjectValue(DataTypes.OBJ_DATA, new byte[0]);
    }

    public FrameBodyPRIV(FrameBodyPRIV body) {
        super(body);
    }

    /**
     * Creates a new FrameBodyPRIV datatype.
     *
     * @param owner
     * @param data
     */
    public FrameBodyPRIV(String owner, byte[] data) {
        this.setObjectValue(DataTypes.OBJ_OWNER, owner);
        this.setObjectValue(DataTypes.OBJ_DATA, data);
    }

    /**
     * Creates a new FrameBodyPRIV datatype.
     *
     * @param byteBuffer
     * @param frameSize
     * @throws InvalidTagException if unable to create framebody from buffer
     */
    public FrameBodyPRIV(ByteBuffer byteBuffer, int frameSize) throws InvalidTagException {
        super(byteBuffer, frameSize);
    }

    /**
     * @return
     */
    public byte[] getData() {
        return (byte[]) getObjectValue(DataTypes.OBJ_DATA);
    }

    /**
     * @param data
     */
    public void setData(byte[] data) {
        setObjectValue(DataTypes.OBJ_DATA, data);
    }

    /**
     * The ID3v2 frame identifier
     *
     * @return the ID3v2 frame identifier  for this frame type
     */
    public String getIdentifier() {
        return ID3v24Frames.FRAME_ID_PRIVATE;
    }

    /**
     * @return
     */
    public String getOwner() {
        return (String) getObjectValue(DataTypes.OBJ_OWNER);
    }

    /**
     * @param owner
     */
    public void setOwner(String owner) {
        setObjectValue(DataTypes.OBJ_OWNER, owner);
    }

    /**
     *
     */
    protected void setupObjectList() {
        objectList.add(new StringNullTerminated(DataTypes.OBJ_OWNER, this));
        objectList.add(new ByteArraySizeTerminated(DataTypes.OBJ_DATA, this));
    }
}
