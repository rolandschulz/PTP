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
 *
 * @author Jeff Overbey
 */
public abstract class PrimitiveOp
{
    private PrimitiveOp() {} // Force all subclasses to be defined as inner classes

    @Override public abstract String toString();

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Factory Methods
    ///////////////////////////////////////////////////////////////////////////////////////////////

    public static Alpha alpha(Interval j)
    {
        return new Alpha(j);
    }

    public static Alpha alpha(int j_lb, int j_ub)
    {
        return new Alpha(new Interval(j_lb, j_ub));
    }

    public static Rho rho(Interval j, Interval k)
    {
        return new Rho(j, k);
    }

    public static Rho rho(int j_lb, int j_ub, int k_lb, int k_ub)
    {
        return new Rho(new Interval(j_lb, j_ub), new Interval(k_lb, k_ub));
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Abstract Methods
    ///////////////////////////////////////////////////////////////////////////////////////////////

    public abstract Interval ioffset(Interval i);

    public abstract Interval inorm(Interval i);

    public abstract Interval doffset(Interval i);

    public abstract Interval dnorm(Interval i);

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Concrete Subclasses
    ///////////////////////////////////////////////////////////////////////////////////////////////

    public static class Alpha extends PrimitiveOp
    {
        public final Interval j;

        private Alpha(Interval j)
        {
            this.j = j;
        }

        @Override
        public Interval ioffset(Interval i)
        {
            if (i.isLessThan(j))
                return i;
            else
                return i.plus(j.cardinality());
        }

        @Override
        public Interval inorm(Interval i)
        {
            return ioffset(i);
        }

        @Override
        public Interval doffset(Interval i)
        {
            return i;
        }

        @Override
        public Interval dnorm(Interval i)
        {
            return doffset(i);
        }

        @Override public String toString()
        {
            return "alpha(" + j + ")";
        }
    }

    public static class Rho extends PrimitiveOp
    {
        public final Interval j, k;

        private Rho(Interval j, Interval k)
        {
            if (j.lb != k.lb)
                throw new IllegalArgumentException("Rho-operation has mismatched lower bounds");

            this.j = j;
            this.k = k;
        }

        @Override
        public Interval ioffset(Interval i)
        {
            if (i.isLessThan(j))
                return i;
            else
                return i.minus(j.cardinality()).plus(k.cardinality());
        }

        @Override
        public Interval inorm(Interval i)
        {
            if (i.isSubsetOf(j))
                return k;
            else
                return ioffset(i);
        }

        @Override
        public Interval doffset(Interval i)
        {
            return i;
        }

        @Override
        public Interval dnorm(Interval i)
        {
            if (i.isSubsetOf(j))
                return k;
            else
                return doffset(i);
        }

        @Override public String toString()
        {
            return "rho(" + j + ", " + k + ")";
        }
    }
}
