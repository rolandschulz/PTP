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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.rephraserengine.internal.core.preservation.PrimitiveOp.Alpha;

/**
 * A list of {@link PrimitiveOp}s.
 *
 * @author Jeff Overbey
 *
 * @since 1.0
 */
public final class PrimitiveOpList implements Iterable<PrimitiveOp>
{
    protected final List<PrimitiveOp> list;

    public PrimitiveOpList()
    {
        list = new ArrayList<PrimitiveOp>();;
    }

    public PrimitiveOpList(PrimitiveOp... ops)
    {
        this();

        for (PrimitiveOp op : ops)
            add(op);
    }

    public PrimitiveOpList without(PrimitiveOp opToOmit)
    {
        PrimitiveOpList result = new PrimitiveOpList();

        for (PrimitiveOp op : list)
            if (!op.equals(opToOmit))
                result.add(op);

        return result;
    }

//    public boolean isEmpty()
//    {
//        return list.isEmpty();
//    }
//
//    public int size()
//    {
//        return list.size();
//    }
//
//    public PrimitiveOp get(int i)
//    {
//        return list.get(i);
//    }

    public Iterator<PrimitiveOp> iterator()
    {
        return list.iterator();
    }

//    public PrimitiveOp remove(int i)
//    {
//        return list.remove(i);
//    }

    public void add(PrimitiveOp op)
    {
        if (op instanceof Alpha && needToMergeAlpha((Alpha)op))
            mergeAlpha((Alpha)op);
        else
            internalAdd(op);
    }

    private boolean needToMergeAlpha(Alpha alpha)
    {
        return !list.isEmpty()
            && lastOp() instanceof Alpha
            && ((Alpha)lastOp()).filename.equals(alpha.filename)
            && ((Alpha)lastOp()).j.lb == alpha.j.lb;
            //&& ((Alpha)lastOp()).preserveEdgeTypes.equals(alpha.preserveEdgeTypes);
    }

    private PrimitiveOp lastOp()
    {
        return list.get(list.size()-1);
    }

    private void mergeAlpha(Alpha alpha2)
    {
        Alpha alpha1 = (Alpha)list.remove(list.size()-1);

        Alpha newAlpha = PrimitiveOp.alpha(
            alpha1.filename,
            alpha1.k.lb,
            alpha1.k.lb + alpha1.k.cardinality() + alpha2.k.cardinality());
            //alpha1.preserveEdgeTypes);

        internalAdd(newAlpha);
    }

    private void internalAdd(PrimitiveOp opToAdd)
    {
        for (PrimitiveOp existingOp : list)
            if (opToAdd.iaff().overlaps(existingOp.iaff()))
                throw new IllegalArgumentException("Cannot add operation " //$NON-NLS-1$
                    + opToAdd
                    + " because it overlaps " //$NON-NLS-1$
                    + existingOp);

        list.add(opToAdd);
    }

    public int offset(String filename, int n)
    {
        int result = n;
        for (PrimitiveOp op : list)
            result += op.adjust(filename, n);
        return result;
    }

    public Interval inorm(String filename, Interval interval)
    {
        for (PrimitiveOp op : list)
        {
            if (op.filename.equals(filename)
                && interval.isSubsetOf(op.iaff(/*this*/)))
            {
                return op.daff(this);
            }
        }

        int lb = offset(filename, interval.lb);
        int ub = offset(filename, interval.ub);
        //if (ub <= lb) ub = lb + interval.cardinality(); // Can't happen (?)
        return new Interval(lb, ub);
    }

    public Interval dnorm(String filename, Interval interval)
    {
        for (PrimitiveOp op : list)
        {
            if (op.filename.equals(filename)
                && interval.isSubsetOf(op.daff(this)))
            {
                return op.daff(this);
            }
        }

        return interval;
    }

    @Override public String toString()
    {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (PrimitiveOp op : list)
        {
            if (first) first = false; else sb.append(' ');
            sb.append(op);
        }
        return sb.toString();
    }
}
