/**
 * @author : Paul Taylor
 * <p/>
 * Version @version:$Id: AbstractValuePair.java 836 2009-11-12 15:44:07Z paultaylor $
 * <p/>
 * Jaudiotagger Copyright (C)2004,2005
 * <p/>
 * This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public  License as published by the Free Software Foundation; either version 2.1 of the License,
 * or (at your option) any later version.
 * <p/>
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU Lesser General Public License along with this library; if not,
 * you can get a copy from http://www.opensource.org/licenses/lgpl-license.php or write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * <p/>
 * Description:
 */
package org.jaudiotagger.tag.datatype;

import java.util.*;

/**
 * A two way mapping between an id and a value
 */
public abstract class AbstractValuePair<I, V> {
    protected final Map<I, V> idToValue = new LinkedHashMap<I, V>();
    protected final Map<V, I> valueToId = new LinkedHashMap<V, I>();
    protected final List<V> valueList = new ArrayList<V>();

    protected Iterator<I> iterator = idToValue.keySet().iterator();

    protected String value;

    /**
     * Get list in alphabetical order
     *
     * @return
     */
    public List<V> getAlphabeticalValueList() {
        return valueList;
    }

    public Map<I, V> getIdToValueMap() {
        return idToValue;
    }

    public Map<V, I> getValueToIdMap() {
        return valueToId;
    }

    /**
     * @return the number of elements in the mapping
     */
    public int getSize() {
        return valueList.size();
    }
}
