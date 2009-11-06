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
    public static final int UNDEFINED = Integer.MIN_VALUE;

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Factory Methods (alpha/epsilon methods normalize to rho-operations)
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

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Abstract Methods
    ///////////////////////////////////////////////////////////////////////////////////////////////

    public final String filename;

    private PrimitiveOp(String filename) // Force all subclasses to be defined as inner classes
    {
        this.filename = filename;
    }

    @Override public abstract String toString();

    protected abstract int offset(int n);

    public abstract Interval inorm(Interval i);

    public abstract Interval iaff();

    public abstract Interval dnorm(Interval i);

    public abstract Interval daff();

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
                throw new IllegalArgumentException("Rho-operation has mismatched lower bounds");

            this.j = j;
            this.k = k;
        }

        @Override
        public int offset(int n)
        {
            if (n < j.lb)
                return n;
            else if (n >= j.ub)
                return n - j.cardinality() + k.cardinality();
            else
                return UNDEFINED;
        }

        @Override
        public Interval inorm(Interval i)
        {
            return new Interval(
                offset(i.lb) != UNDEFINED ? offset(i.lb) : k.lb,
                offset(i.ub) != UNDEFINED ? offset(i.ub) : k.ub);
        }

        @Override
        public Interval iaff()
        {
            return j;
        }

        @Override
        public Interval dnorm(Interval i)
        {
            if (i.isSubsetOf(k))
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
            return "rho<" + j + ", " + k + ">";
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
