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
    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Factory Methods
    ///////////////////////////////////////////////////////////////////////////////////////////////

    public static Alpha alpha(String filename, Interval j)
    {
        return new Alpha(filename, j);
    }

    public static Alpha alpha(String filename, int j_lb, int j_ub)
    {
        return new Alpha(filename, new Interval(j_lb, j_ub));
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

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Abstract Methods
    ///////////////////////////////////////////////////////////////////////////////////////////////

    public final String filename;

    private PrimitiveOp(String filename) // Force all subclasses to be defined as inner classes
    {
        this.filename = filename;
    }

    @Override public abstract String toString();

    public abstract Interval offset(Interval i);

    public abstract Interval inorm(Interval i);

    public abstract Interval iaff();

    public abstract Interval dnorm(Interval i);

    public abstract Interval daff();

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Concrete Subclasses
    ///////////////////////////////////////////////////////////////////////////////////////////////

    public static class Alpha extends PrimitiveOp
    {
        public final Interval j;

        private Alpha(String filename, Interval j)
        {
            super(filename);
            this.j = j;
        }

        @Override
        public Interval offset(Interval i)
        {
            if (i.isLessThan(j))
                return i;
            else
                return i.plus(j.cardinality());
        }

        @Override
        public Interval inorm(Interval i)
        {
            return offset(i);
        }

        @Override
        public Interval iaff()
        {
            return new Interval(j.lb, j.lb+1);
        }

        @Override
        public Interval dnorm(Interval i)
        {
            return i;
        }

        @Override
        public Interval daff()
        {
            return j;
        }

        @Override public String toString()
        {
            return "alpha(" + j + ")";
        }
    }

    public static class Epsilon extends PrimitiveOp
    {
        public final Interval j;

        private Epsilon(String filename, Interval j)
        {
            super(filename);
            this.j = j;
        }

        @Override
        public Interval offset(Interval i)
        {
            if (i.isLessThan(j))
                return i;
            else
                return i.minus(j.cardinality());
        }

        @Override
        public Interval inorm(Interval i)
        {
            return offset(i);
        }

        @Override
        public Interval iaff()
        {
            return j;
        }

        @Override
        public Interval dnorm(Interval i)
        {
            return i;
        }

        @Override
        public Interval daff()
        {
            return new Interval(j.lb, j.lb+1);
        }

        @Override public String toString()
        {
            return "epsilon(" + j + ")";
        }
    }

    public static class Rho extends PrimitiveOp
    {
        public final Interval j, k;

        private Rho(String filename, Interval j, Interval k)
        {
            super(filename);

            if (j.lb != k.lb)
                throw new IllegalArgumentException("Rho-operation has mismatched lower bounds");

            this.j = j;
            this.k = k;
        }

        @Override
        public Interval offset(Interval i)
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
                return offset(i);
        }

        @Override
        public Interval iaff()
        {
            return j;
        }

        @Override
        public Interval dnorm(Interval i)
        {
            if (i.isSubsetOf(j))
                return k;
            else
                return i;
        }

        @Override
        public Interval daff()
        {
            return k;
        }

        @Override public String toString()
        {
            return "rho(" + j + ", " + k + ")";
        }
    }
}
