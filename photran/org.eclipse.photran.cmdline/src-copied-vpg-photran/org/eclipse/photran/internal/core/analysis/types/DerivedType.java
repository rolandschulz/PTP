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
package org.eclipse.photran.internal.core.analysis.types;

/**
 * A <code>Type</code> corresponding to a derived type with a particular name.
 * 
 * @author Jeff Overbey
 */
public class DerivedType extends Type
{
    private String name;

    public DerivedType(String name)
    {
        this.name = name.toLowerCase();
    }

    public String toString()
    {
        return "type(" + name + ")";
    }
    
    public <T> T processUsing(TypeProcessor<T> p)
    {
        return p.ifDerivedType(name, this);
    }
    
    @Override public boolean equals(Object other)
    {
        // TODO: Does not consider scope
        return other instanceof DerivedType && ((DerivedType)other).name.equals(this.name);
    }
    
    @Override public int hashCode()
    {
        return name.hashCode();
    }
}
