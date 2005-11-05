/**
 * Copyright (c) 2000-2005, Identyx Corporation.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package org.safehaus.penrose.cache;

import org.safehaus.penrose.mapping.*;

import java.util.*;

/**
 * @author Endi S. Dewata
 */
public class JDBCSourceDataCache extends SourceDataCache {

    JDBCCache cache;

    public void init() throws Exception {
        super.init();

        log.debug("Initializing JDBC Source Data Cache ...");

        cache = new JDBCCache(cacheConfig, sourceDefinition);
        cache.setSize(size);
        cache.setExpiration(expiration);
        cache.init();
    }

    public Object get(Object key) throws Exception {
        Row pk = (Row)key;

        return cache.get(pk);
    }

    public Map getExpired() throws Exception {
        Map results = new TreeMap();
        return results;
    }
    
    public Map search(Collection keys, Collection missingKeys) throws Exception {
        return cache.search(keys, missingKeys);
    }

    public void put(Object key, Object object) throws Exception {
        Row pk = (Row)key;
        AttributeValues sourceValues = (AttributeValues)object;

        cache.put(pk, sourceValues);
    }

    public void remove(Object key) throws Exception {
        Row pk = (Row)key;

        cache.remove(pk);
    }

    public int getLastChangeNumber() throws Exception {
        return cache.getLastChangeNumber();
    }

    public void setLastChangeNumber(int lastChangeNumber) throws Exception {
        cache.setLastChangeNumber(lastChangeNumber);
    }
}
