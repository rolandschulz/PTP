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

/**
 * An ordered pair <i>(fst, snd)</i>.
 * 
 * @author Jeff Overbey
 * 
 * @param <T>
 * @param <U>
 */
public final class Pair<T, U>
{
    /** Factory method */
    public static <X, Y> Pair<X, Y> of(X fst, Y snd)
    {
        return new Pair<X, Y>(fst, snd);
    }
    
    public final T fst;
    public final U snd;

    public Pair(T fst, U snd)
    {
        this.fst = fst;
        this.snd = snd;
    }
    
    @Override public int hashCode()
    {
        return hashCode(this.fst) * 19 + hashCode(this.snd);
    }
    
    private int hashCode(Object o)
    {
        if (o == null)
            return 0;
        else
            return o.hashCode();
    }

    @Override public boolean equals(Object obj)
    {
        if (obj == null || !obj.getClass().equals(this.getClass())) return false;
        
        Pair<?,?> that = (Pair<?,?>)obj;
        return equals(this.fst, that.fst) && equals(this.snd, that.snd);
    }
    
    protected boolean equals(Object o1, Object o2)
    {
        if (o1 == null && o2 == null)
            return true;
        else if (o1 != null && o2 != null && o1.equals(o2))
            return true;
        else
            return false;
    }
}
