/**
 * @author : Paul Taylor
 * @author : Eric Farng
 * <p>
 * Version @version:$Id: Lyrics3v1Iterator.java 836 2009-11-12 15:44:07Z paultaylor $
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

import java.util.Iterator;
import java.util.NoSuchElementException;

public class Lyrics3v1Iterator implements Iterator<String> {
    /**
     *
     */
    private Lyrics3v1 tag = null;

    /**
     *
     */
    private int lastIndex = 0;

    /**
     *
     */
    private int removeIndex = 0;

    /**
     * Creates a new Lyrics3v1Iterator datatype.
     *
     * @param lyrics3v1Tag
     */
    public Lyrics3v1Iterator(Lyrics3v1 lyrics3v1Tag) {
        tag = lyrics3v1Tag;
    }

    /**
     * @return
     */
    public boolean hasNext() {
        return !((tag.getLyric().indexOf('\n', lastIndex) < 0) && (lastIndex > tag.getLyric().length()));
    }

    /**
     * @return
     * @throws NoSuchElementException
     */
    public String next() {
        int nextIndex = tag.getLyric().indexOf('\n', lastIndex);

        removeIndex = lastIndex;

        String line;

        if (lastIndex >= 0) {
            if (nextIndex >= 0) {
                line = tag.getLyric().substring(lastIndex, nextIndex);
            } else {
                line = tag.getLyric().substring(lastIndex);
            }

            lastIndex = nextIndex;
        } else {
            throw new NoSuchElementException("Iteration has no more elements.");
        }

        return line;
    }

    /**
     *
     */
    public void remove() {
        String lyric = tag.getLyric().substring(0, removeIndex) + tag.getLyric().substring(lastIndex);
        tag.setLyric(lyric);
    }
}