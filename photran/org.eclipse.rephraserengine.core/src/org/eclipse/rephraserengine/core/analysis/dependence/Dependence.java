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
package org.eclipse.rephraserengine.core.analysis.dependence;

/**
 * A dependence between two variable/array accesses.
 * <p>
 * THIS IS PRELIMINARY AND EXPERIMENTAL.  IT IS NOT APPROPRIATE FOR PRODUCTION USE.
 *
 * @author Jeff Overbey
 * @see LoopDependences
 */
public class Dependence
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
            if (d.from.isWrite() && d.to.isWrite())
                return OUTPUT_DEPENDENCE;
            else if (d.from.isRead() && d.to.isWrite())
                return ANTI_DEPENDENCE;
            else // (d.from.isWrite && d.to.isRead())
                return FLOW_DEPENDENCE;
        }

        @Override public String toString()
        {
            return description;
        }
    }

    public final IVariableReference from;
    public final IVariableReference to;
    public final Type type;

    public Dependence(IVariableReference from, IVariableReference to)
    {
        assert from.isRead() != from.isWrite() && to.isRead() != to.isWrite();
        
        this.from = from;
        this.to = to;
        this.type = Type.of(this);
    }

    @Override public boolean equals(Object o)
    {
        if (!this.getClass().equals(o.getClass())) return false;

        Dependence that = (Dependence)o;
        return this.from.equals(that.from)
            && this.to.equals(that.to)
            && this.type.equals(that.type);
    }

    @Override public int hashCode()
    {
        return from.hashCode() + 13 * to.hashCode() + 19 * type.hashCode();
    }

    @Override public String toString()
    {
        return type + " from " + from + " to " + to;
    }
}
