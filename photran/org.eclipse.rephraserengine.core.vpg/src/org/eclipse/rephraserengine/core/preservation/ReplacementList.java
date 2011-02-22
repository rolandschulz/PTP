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
package org.eclipse.rephraserengine.core.preservation;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.rephraserengine.core.vpg.IVPGNode;
import org.eclipse.rephraserengine.core.vpg.VPG;

/**
 * A list of {@link Replacement}s.
 *
 * @author Jeff Overbey
 * 
 * @since 3.0
 */
public final class ReplacementList implements Iterable<Replacement>
{
    protected final List<Replacement> list;

    /** Constructor.  Creates an empty list of replacements. */
    public ReplacementList()
    {
        list = new ArrayList<Replacement>();
    }

    /** Constructor.  Creates a list from the given replacements. */
    public ReplacementList(Replacement... ops)
    {
        this();

        for (Replacement op : ops)
            add(op);
    }

    /** @return this list with the given replacement omitted */
    public ReplacementList without(Replacement opToOmit)
    {
        ReplacementList result = new ReplacementList();

        for (Replacement op : list)
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
//    public Replacement get(int i)
//    {
//        return list.get(i);
//    }

    public Iterator<Replacement> iterator()
    {
        return list.iterator();
    }

//    public Replacement remove(int i)
//    {
//        return list.remove(i);
//    }

    /** Adds the given replacement to this list */
    public void add(Replacement op)
    {
        if (op.isAddition() && needToMergeAlpha(op))
            mergeAlpha(op);
        else
            internalAdd(op);
    }

    private boolean needToMergeAlpha(Replacement alpha)
    {
        return !list.isEmpty()
            && lastOp().isAddition()
            && lastOp().getFilename().equals(alpha.getFilename())
            && lastOp().getOffset() == alpha.getOffset();
    }

    private Replacement lastOp()
    {
        return list.get(list.size()-1);
    }

    private void mergeAlpha(Replacement alpha2)
    {
        Replacement alpha1 = list.remove(list.size()-1);

        Replacement newAlpha = new Replacement(
            alpha1.getFilename(),
            alpha1.getOffset(),
            0,
            alpha1.getNewLength() + alpha2.getNewLength());

        internalAdd(newAlpha);
    }

    private void internalAdd(Replacement opToAdd)
    {
        for (Replacement existingOp : list)
            if (opToAdd.overlaps(existingOp))
                throw new IllegalArgumentException("Cannot add operation " //$NON-NLS-1$
                    + opToAdd
                    + " because it overlaps " //$NON-NLS-1$
                    + existingOp);

        list.add(opToAdd);
    }

    /** @return the new offset after the given offset has been adjusted by the replacements in this list */
    public int offset(String filename, int offset)
    {
        int result = offset;
        for (Replacement op : list)
            result += op.adjust(filename, offset);
        return result;
    }

    /** @return the initial model projection of the given {@link IVPGNode} */
    public <T, R extends IVPGNode<T>> R projectInitial(R tokenRef, VPG<?,T,R> vpg)
    {
        for (Replacement replacement : list)
            if (replacement.origIntervalContains(tokenRef))
                return null;
        
        String filename = tokenRef.getFilename();
        int newOffset = offset(filename, tokenRef.getOffset());
        int newEndOffset = offset(filename, tokenRef.getEndOffset()-1)+1;
        return vpg.getVPGNode(
            filename,
            newOffset,
            newEndOffset - newOffset);
    }

    /** @return the final model projection of the given {@link IVPGNode} */
    public <T, R extends IVPGNode<T>> R projectFinal(R tokenRef, VPG<?,T,R> vpg)
    {
        for (Replacement replacement : list)
            if (replacement.newIntervalContains(tokenRef, this))
                return null;
        
        return tokenRef;
    }

    @Override public String toString()
    {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (Replacement op : list)
        {
            if (first) first = false; else sb.append('\n');
            sb.append(op);
        }
        return sb.toString();
    }
}
