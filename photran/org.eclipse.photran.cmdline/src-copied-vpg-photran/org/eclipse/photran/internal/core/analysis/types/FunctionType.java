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

import java.util.LinkedList;
import java.util.List;

/**
 * A <code>Type</code> corresponding to a derived type with a particular name.
 * 
 * @author Jeff Overbey
 */
public class FunctionType extends Type
{
    private String name;
    private Type returnType = Type.UNKNOWN;
    private List<Type> argumentTypes = new LinkedList<Type>();

    public FunctionType(String name)
    {
        this.name = name.toLowerCase();
    }

    public String toString()
    {
        return "function(" + name + "): " + argumentTypes + " -> " + returnType;
    }
    
    public <T> T processUsing(TypeProcessor<T> p)
    {
        return p.ifFunctionType(name, this);
    }
    
    @Override public boolean equals(Object other)
    {
        // TODO: Does not consider scope
        return other instanceof FunctionType && ((FunctionType)other).name.equals(this.name);
    }
    
    @Override public int hashCode()
    {
        return name.hashCode();
    }
}
