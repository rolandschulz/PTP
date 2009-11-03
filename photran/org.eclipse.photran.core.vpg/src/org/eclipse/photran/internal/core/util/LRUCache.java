/*******************************************************************************
 * Copyright (c) 2009 University of Illinois at Urbana-Champaign and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    UIUC - Initial API and implementation
 *******************************************************************************/
package org.eclipse.photran.internal.core.util;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * A cache that holds a finite number of entries and removes the least recently used
 * entry when it becomes filled beyond this fixed capacity.
 * <p>
 * The underlying data structure is a {@link HashMap}: entries in the cache have a
 * key and a value.
 * 
 * @author Jeff Overbey
 */
public final class LRUCache<T, U>
{
    protected final int cacheSize;
    
    protected Map<T, U> cache;
    protected List<T> lruKeys;
    
    public LRUCache(int cacheSize)
    {
        this.cacheSize = cacheSize;
        this.cache = new HashMap<T, U>(cacheSize);
        this.lruKeys = new LinkedList<T>();
    }
    
    public boolean contains(T key)
    {
        return cache.containsKey(key);
    }
    
    public U get(T key)
    {
        if (lruKeys.get(0) != key) // Pointer comparison
        {
            if (!this.lruKeys.remove(key)) return null;
            lruKeys.add(0, key);
        }
        
        return cache.get(key);
    }

    public void clear()
    {
        cache.clear();
        lruKeys.clear();
    }

    public void cache(T key, U value)
    {
        if (cacheIsFull())
            removeLeastRecentlyUsedFromCache();
        
        cache.put(key, value);
        lruKeys.add(key);
    }
    
    private boolean cacheIsFull()
    {
        return cache.size() == cacheSize;
    }

    private void removeLeastRecentlyUsedFromCache()
    {
        T lru = lruKeys.remove(0);
        cache.remove(lru);
    }

    public void remove(T key)
    {
        lruKeys.remove(key);
        cache.remove(key);
    }
}
