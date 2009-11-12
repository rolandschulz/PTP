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
 */
public final class PrimitiveOpList implements Iterable<PrimitiveOp>
{
    protected List<PrimitiveOp> list;

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

    public boolean isEmpty()
    {
        return list.isEmpty();
    }

    public int size()
    {
        return list.size();
    }

    public PrimitiveOp get(int i)
    {
        return list.get(i);
    }

    public Iterator<PrimitiveOp> iterator()
    {
        return list.iterator();
    }

    public PrimitiveOp remove(int i)
    {
        return list.remove(i);
    }

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
                throw new IllegalArgumentException("Cannot add operation "
                    + opToAdd
                    + " because it overlaps "
                    + existingOp);

        list.add(opToAdd);
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
