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
package org.eclipse.photran.internal.core.analysis.dependence;

/**
 * A dependence between two variable/array accesses.
 * 
 * @author Jeff Overbey
 * @see LoopDependences
 */
public final class Dependence
{
    public static enum Type
    {
        FLOW_DEPENDENCE("Flow dependence"),
        ANTI_DEPENDENCE("Anti-dependence"),
        OUTPUT_DEPENDENCE("Output dependence");
        
        private String description;
        
        private Type(String description)
        {
            this.description = description;
        }
        
        public static Type of(Dependence d)
        {
            if (d.from.isWrite && d.to.isWrite)
                return OUTPUT_DEPENDENCE;
            else if (d.from.isRead() && d.to.isWrite)
                return ANTI_DEPENDENCE;
            else // (d.from.isWrite && d.to.isRead())
                return FLOW_DEPENDENCE;
        }
        
        @Override public String toString()
        {
            return description;
        }
    }
    
    final VariableReference from;
    final VariableReference to;
    public final Type type;

    Dependence(VariableReference from, VariableReference to)
    {
        this.from = from;
        this.to = to;
        this.type = Type.of(this);
    }
    
    @Override public String toString()
    {
        return type + " from " + from + " to " + to;
    }
}
