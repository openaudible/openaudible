/**
 * @author : Paul Taylor
 * @author : Eric Farng
 * <p>
 * Version @version:$Id: FieldFrameBodyIND.java 832 2009-11-12 13:25:38Z paultaylor $
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
package org.jaudiotagger.tag.lyrics3;

import org.jaudiotagger.tag.InvalidTagException;
import org.jaudiotagger.tag.datatype.BooleanString;

import java.nio.ByteBuffer;


public class FieldFrameBodyIND extends AbstractLyrics3v2FieldFrameBody {
    /**
     * Creates a new FieldBodyIND datatype.
     */
    public FieldFrameBodyIND() {
        //        this.setObject("Lyrics Present", new Boolean(false));
        //        this.setObject("Timestamp Present", new Boolean(false));
    }

    public FieldFrameBodyIND(FieldFrameBodyIND body) {
        super(body);
    }

    /**
     * Creates a new FieldBodyIND datatype.
     *
     * @param lyricsPresent
     * @param timeStampPresent
     */
    public FieldFrameBodyIND(boolean lyricsPresent, boolean timeStampPresent) {
        this.setObjectValue("Lyrics Present", lyricsPresent);
        this.setObjectValue("Timestamp Present", timeStampPresent);
    }

    /**
     * Creates a new FieldBodyIND datatype.
     *
     * @param byteBuffer
     * @throws InvalidTagException
     */
    public FieldFrameBodyIND(ByteBuffer byteBuffer) throws InvalidTagException {
        this.read(byteBuffer);
    }

    /**
     * @return
     */
    public String getAuthor() {
        return (String) getObjectValue("Author");
    }

    /**
     * @param author
     */
    public void setAuthor(String author) {
        setObjectValue("Author", author);
    }

    /**
     * @return
     */
    public String getIdentifier() {
        return "IND";
    }

    /**
     *
     */
    protected void setupObjectList() {
        objectList.add(new BooleanString("Lyrics Present", this));
        objectList.add(new BooleanString("Timestamp Present", this));
    }
}
