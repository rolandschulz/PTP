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
 * A value object representing a right half-open integer interval.
 * <p>
 * A right half-open integer interval [<i>j</i>,<i>k</i>), for integers <i>j</i> and <i>k</i>,
 * denotes the set { <i>j</i>, <i>j</i>+1, <i>j</i>+2, ..., <i>k</j>-2, <i>k</j>-1 }.
 *
 * @author Jeff Overbey
 */
public final class Interval implements Comparable<Interval>
{
    public final int lb, ub;

    public Interval(int lb, int ub)
    {
        if (ub < lb) throw new IllegalArgumentException(
            "An interval [j,k) must have j <= k; [" + lb + "," + ub + ") is not a valid interval");

        this.lb = lb;
        this.ub = ub;
    }

    public boolean isEmpty()
    {
        return lb == ub;
    }

    public boolean contains(int n)
    {
        return lb <= n && n < ub;
    }

    public boolean isSubsetOf(Interval other)
    {
        if (this.isEmpty())
            return true;
        else if (other.isEmpty())
            return false;
        else
            return other.lb <= this.lb && this.ub <= other.ub;
    }

// THESE DEFINITIONS ARE **WRONG**!
//    public Interval union(Interval that)
//    {
//        return new Interval(
//            Math.min(this.lb, that.lb),
//            Math.max(this.ub, that.ub));
//    }
//
//    public Interval intersection(Interval that)
//    {
//        return new Interval(
//            Math.max(this.lb, that.lb),
//            Math.min(this.ub, that.ub));
//    }

    public int cardinality()
    {
        return ub - lb;
    }

    public boolean isLessThan(Interval that)
    {
        return this.ub <= that.lb && !this.equals(that);
    }

    public boolean isGreaterThan(Interval that)
    {
        return this.lb >= that.ub && !this.equals(that);
    }

    public boolean overlaps(Interval that)
    {
        return !doesNotOverlap(that);
    }

    private boolean doesNotOverlap(Interval that)
    {
        return this.ub <= that.lb || this.lb >= that.ub;
    }

    public Interval plus(int offset)
    {
        return new Interval(lb + offset, ub + offset);
    }

    public Interval minus(int offset)
    {
        return plus(-offset);
    }

    @Override
    public boolean equals(Object o)
    {
        if (o != null && o.getClass().equals(this.getClass()))
        {
            Interval other = (Interval)o;
            return this.lb == other.lb && this.ub == other.ub;
        }
        else return false;
    }

    @Override public int hashCode()
    {
        return 7919 * lb + ub;
    }

    public int compareTo(Interval that)
    {
        // Lexicographic comparison
        if (this.lb < that.lb)
            return -1;
        else if (this.lb > that.lb)
            return 1;
        else if (/*this.lb == that.lb &&*/ this.ub < that.ub)
            return -1;
        else if (/*this.lb == that.lb &&*/ this.ub > that.ub)
            return 1;
        else // (this.lb == that.lb && this.ub == that.ub)
            return 0;
    }

    @Override
    public String toString()
    {
        return "[" + lb + ", " + ub + ")";
    }
}
