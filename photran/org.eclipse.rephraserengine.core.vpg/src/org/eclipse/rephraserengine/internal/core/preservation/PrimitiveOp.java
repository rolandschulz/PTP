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
package org.eclipse.rephraserengine.internal.core.preservation;

/**
 * A primitive operation: alpha, epsilon, or rho.
 *
 * @author Jeff Overbey
 * 
 * @since 1.0
 */
public abstract class PrimitiveOp
{
    public static final int UNDEFINED = Integer.MIN_VALUE;

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Factory Methods (alpha/epsilon methods normalize to rho-operations via subclassing)
    ///////////////////////////////////////////////////////////////////////////////////////////////

    public static Alpha alpha(String filename, Interval k)
    {
        return new Alpha(filename, k);
    }

    public static Alpha alpha(String filename, int k_lb, int k_ub)
    {
        return new Alpha(filename, new Interval(k_lb, k_ub));
    }

    public static Epsilon epsilon(String filename, Interval j)
    {
        return new Epsilon(filename, j);
    }

    public static Epsilon epsilon(String filename, int j_lb, int j_ub)
    {
        return new Epsilon(filename, new Interval(j_lb, j_ub));
    }

    public static Rho rho(String filename, Interval j, Interval k)
    {
        return new Rho(filename, j, k);
    }

    public static Rho rho(String filename, int j_lb, int j_ub, int k_lb, int k_ub)
    {
        return new Rho(filename, new Interval(j_lb, j_ub), new Interval(k_lb, k_ub));
    }

    public static Rho rho(String filename, int offset, int oldLength, int newLength)
    {
        return new Rho(filename, new Interval(offset, offset+oldLength), new Interval(offset, offset+newLength));
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Class Members
    ///////////////////////////////////////////////////////////////////////////////////////////////

    public final String filename;

    private PrimitiveOp(String filename) // Force all subclasses to be defined as inner classes
    {
        this.filename = filename;
    }
    
    public abstract Interval iaff();
    
    public abstract Interval daff(PrimitiveOpList s);
    
    public abstract int adjust(String filename, int n);

    @Override public abstract String toString();

    @Override public abstract int hashCode();

    @Override public abstract boolean equals(Object o);

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Concrete Subclasses
    ///////////////////////////////////////////////////////////////////////////////////////////////

    public static class Rho extends PrimitiveOp
    {
        public final Interval j, k;

        private Rho(String filename, Interval j, Interval k)
        {
            super(filename);

            if (j.lb != k.lb)
                throw new IllegalArgumentException("Rho-operation has mismatched lower bounds"); //$NON-NLS-1$

            this.j = j;
            this.k = k;
        }
        
        @Override public Interval iaff()
        {
            return j;
        }
        
        @Override public Interval daff(PrimitiveOpList s)
        {
            int lb = s.offset(filename, k.lb);
            int ub = lb + k.cardinality();
            return new Interval(lb, ub);
        }
        
        @Override public int adjust(String filename, int n)
        {
            if (this.filename.equals(filename) && n >= j.ub)
                return k.cardinality() - j.cardinality();
            else
                return 0;
        }

        @Override public String toString()
        {
            return filename + ":" + "rho<" + j + ", " + k + ">"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        }

        @Override public int hashCode()
        {
            return 19 * j.hashCode() + k.hashCode();
        }

        @Override public boolean equals(Object o)
        {
            return o != null
                && this.getClass().equals(o.getClass())
                && this.j.equals(((Rho)o).j)
                && this.k.equals(((Rho)o).k);
        }
    }

    public static class Alpha extends Rho
    {
        private Alpha(String filename, Interval k)
        {
            super(filename, new Interval(k.lb, k.lb), k);
        }
    }

    public static class Epsilon extends Rho
    {
        private Epsilon(String filename, Interval j)
        {
            super(filename, j, new Interval(j.lb, j.lb));
        }
    }
}
