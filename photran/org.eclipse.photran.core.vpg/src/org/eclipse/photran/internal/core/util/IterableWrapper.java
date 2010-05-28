/*******************************************************************************
 * Copyright (c) 2007 University of Illinois at Urbana-Champaign and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     UIUC - Initial API and implementation
 *******************************************************************************/
package org.eclipse.photran.internal.core.util;

import java.util.Iterator;

/**
 * This is a kludge which allows an object to be iterated through via a Java 5 foreach loop, even though it was
 * not declared as such.
 * 
 * This is useful since part of Photran is Java 1.4-compliant and part is 1.5-compliant: it allows a 1.4-compliant
 * object to be iterated through without explicitly constructing an iterator.  When Photran is moved entirely to
 * Java 1.5, these wrappers can simply be removed and the foreach loops left as-is.  If iterators were manually
 * constructed, they would have to be replaced with foreach loops manually.
 * 
 * @author Jeff Overbey
 *
 * @param <T>
 * 
 * JAVA5: Eliminate after move to Java 5
 */
public class IterableWrapper<T> implements Iterable<T>
{
    private Iterator<T> iterator;
    
    @SuppressWarnings("unchecked")
    public IterableWrapper(Object o)
    {
        try
        {
            this.iterator = (Iterator<T>)o.getClass().getMethod("iterator").invoke(o); //$NON-NLS-1$
        }
        catch (Exception e)
        {
            throw new Error(e);
        }
    }

    public Iterator<T> iterator()
    {
        return iterator;
    }
}
