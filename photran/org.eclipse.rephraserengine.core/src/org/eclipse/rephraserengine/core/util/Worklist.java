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

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * A <code>Worklist</code> is essentially a queue -- objects are added to the back and removed from the front -- but
 * unlike the usual Java collections, it is safe to add and remove items while it is being iterated over (e.g., in a
 * new-style for-loop).
 *
 * @author Jeff Overbey
 *
 * @since 3.0
 */
public class Worklist<T> implements Iterable<T>
{
    private final LinkedList<T> worklist = new LinkedList<T>();

    private int size = 0;

    /** Constructs an empty worklist. */
    public Worklist()
    {
    }

    /** Constructs a worklist which initially contains the given item(s). */
    public Worklist(T item)
    {
        add(item);
    }

    /** Constructs a worklist which initially contains the given item(s). */
    public Worklist(T... items)
    {
        for (T item : items)
            add(item);
    }

    /** Removes all items from this worklist. */
    public void clear()
    {
        worklist.clear();
        size = 0;
    }

    /** Adds the given item at the end of this worklist. */
    public void add(T item)
    {
        size++;
        worklist.add(item);
    }

    /**
     * Iterates through the given collection, successively adding its elements to the end of this
     * worklist.
     */
    public void addAll(Collection<T> items)
    {
        size += items.size();
        worklist.addAll(items);
    }

    /** @return true iff this worklist contains the given item */
    public boolean contains(T item)
    {
        return worklist.contains(item);
    }

    /** @return the number of items in this worklist */
    public int size()
    {
        return size;
    }

    /** Removes the first item in this worklist and returns it. */
    public T removeFirst()
    {
        size--;
        return worklist.removeFirst();
    }

    /** @return true iff there are no items in this worklist */
    public boolean done()
    {
        return size == 0;
    }

    public Iterator<T> iterator()
    {
        return new Iterator<T>()
        {
            public boolean hasNext()
            {
                return !done();
            }

            public T next()
            {
                return removeFirst();
            }

            public void remove()
            {
                throw new UnsupportedOperationException();
            }
        };
    }

    @Override public String toString()
    {
        return worklist.toString();
    }
}