/**
 * @author : Paul Taylor
 * @author : Eric Farng
 * <p>
 * Version @version:$Id: ReadOnlyFileException.java 345 2007-08-07 16:14:03Z paultaylor $
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
 */
package org.jaudiotagger.audio.exceptions;

/**
 * This is the exception when try and access a read only file
 */
public class ReadOnlyFileException extends Exception {
    /**
     * Creates a new ReadOnlyException datatype.
     */
    public ReadOnlyFileException() {
    }

    public ReadOnlyFileException(Throwable ex) {
        super(ex);
    }

    /**
     * Creates a new ReadOnlyException datatype.
     *
     * @param msg the detail message.
     */
    public ReadOnlyFileException(String msg) {
        super(msg);
    }

    public ReadOnlyFileException(String msg, Throwable ex) {
        super(msg, ex);
    }
}
