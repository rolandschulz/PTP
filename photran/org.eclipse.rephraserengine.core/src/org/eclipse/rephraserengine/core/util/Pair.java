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
    public final T fst;
    public final T snd;

    public Pair(T fst, T snd)
    {
        if (fst == null || snd == null) throw new IllegalArgumentException();
        
        this.fst = fst;
        this.snd = snd;
    }
    
    @Override public int hashCode()
    {
        return this.fst.hashCode() * 19 + this.snd.hashCode();
    }
    
    @Override public boolean equals(Object obj)
    {
        if (obj == null || !obj.getClass().equals(this.getClass())) return false;
        
        Pair<?,?> that = (Pair<?,?>)obj;
        return this.fst.equals(that.fst) && this.snd.equals(that.snd);
    }
}
