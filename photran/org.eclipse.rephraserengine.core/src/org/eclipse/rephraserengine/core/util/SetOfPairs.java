/*******************************************************************************
 * Copyright (c) 2010 University of Illinois at Urbana-Champaign and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    UIUC - Initial API and implementation
 *******************************************************************************/
package org.eclipse.rephraserengine.core.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

/**
 * A set of <i>(key, value)</i> pairs where many pairs are expected to have the same key.
 * <p>
 * Internally, the pairs are stored as a <code>HashMap<K, HashSet<V>></code>.
 * @author Jeff Overbey
 * @since 3.0
 */
public class SetOfPairs<K, V> implements Cloneable
{
    protected final HashMap<K, HashSet<V>> data = new HashMap<K, HashSet<V>>();

    public void clear()
    {
        data.clear();
    }
    
    public void add(K key, V value)
    {
        if (!data.containsKey(key))
            data.put(key, new HashSet<V>());

        data.get(key).add(value);

    }

    public void remove(K key)
    {
        data.remove(key);
    }
    
    public boolean contains(K key, V value)
    {
        return data.containsKey(key) && data.get(key).contains(value);
    }

    @SuppressWarnings("unchecked")
    @Override public SetOfPairs<K, V> clone()
    {
        try
        {
            SetOfPairs<K, V> clone = (SetOfPairs<K, V>)super.clone();
            for (K filename : clone.data.keySet())
                clone.data.put(filename, (HashSet<V>)clone.data.get(filename).clone());
            return clone;
        }
        catch (CloneNotSupportedException e)
        {
            throw new Error(e);
        }
    }
    
    @Override public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("["); //$NON-NLS-1$
        for (K key : data.keySet())
        {
            Iterator<V> valueIt = data.get(key).iterator();
            sb.append("(" + key + "," + valueIt.next() + ")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            while (valueIt.hasNext())
            {
                sb.append(", "); //$NON-NLS-1$
                sb.append("(" + key + "," + valueIt.next() + ")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            }
        }
        sb.append("]"); //$NON-NLS-1$
        return sb.toString();
    }
}
