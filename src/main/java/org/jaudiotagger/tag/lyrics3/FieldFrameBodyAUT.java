/**
 * @author : Paul Taylor
 * @author : Eric Farng
 * <p>
 * Version @version:$Id: FieldFrameBodyAUT.java 832 2009-11-12 13:25:38Z paultaylor $
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
import org.jaudiotagger.tag.datatype.StringSizeTerminated;

import java.nio.ByteBuffer;

public class FieldFrameBodyAUT extends AbstractLyrics3v2FieldFrameBody {
    /**
     * Creates a new FieldBodyAUT datatype.
     */
    public FieldFrameBodyAUT() {
        //        this.setObject("Author", "");
    }

    public FieldFrameBodyAUT(FieldFrameBodyAUT body) {
        super(body);
    }

    /**
     * Creates a new FieldBodyAUT datatype.
     *
     * @param author
     */
    public FieldFrameBodyAUT(String author) {
        this.setObjectValue("Author", author);
    }

    /**
     * Creates a new FieldBodyAUT datatype.
     *
     * @param byteBuffer
     * @throws InvalidTagException
     */
    public FieldFrameBodyAUT(ByteBuffer byteBuffer) throws InvalidTagException {
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
        return "AUT";
    }

    /**
     *
     */
    protected void setupObjectList() {
        objectList.add(new StringSizeTerminated("Author", this));
    }
}
