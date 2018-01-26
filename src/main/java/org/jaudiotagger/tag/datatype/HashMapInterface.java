/**
 * @author : Paul Taylor
 * @author : Eric Farng
 * <p>
 * Version @version:$Id: HashMapInterface.java 625 2008-07-21 10:49:58Z paultaylor $
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
 * A simple Interface required by classes which use a HashMap to Store ValuePairs
 */

package org.jaudiotagger.tag.datatype;

import java.util.Iterator;
import java.util.Map;

/**
 * Represents an interface allowing maping from key to value and value to key
 */
public interface HashMapInterface<K, V> {
    /**
     * @return a mapping between the key within the frame and the value
     */
    Map<K, V> getKeyToValue();

    /**
     * @return a mapping between the value to the key within the frame
     */
    Map<V, K> getValueToKey();

    /**
     * @return an interator of the values within the map
     */
    Iterator<V> iterator();
}
