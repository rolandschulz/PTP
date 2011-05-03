/*******************************************************************************
 * Copyright (c) 2010 University of Illinois at Urbana-Champaign and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    UIUC - Initial API and implementation
 *******************************************************************************/
package org.eclipse.rephraserengine.core.preservation;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.eclipse.rephraserengine.core.preservation.ModelDiff.EdgeAdded;
import org.eclipse.rephraserengine.core.preservation.ModelDiff.EdgeDeleted;
import org.eclipse.rephraserengine.core.preservation.ModelDiff.EdgeSinkChanged;
import org.eclipse.rephraserengine.core.vpg.IVPGNode;
import org.eclipse.rephraserengine.core.vpg.VPGEdge;
import org.eclipse.rephraserengine.core.vpg.VPGEdge.Classification;

/**
 * Method object implementing the preservation analysis algorithm.
 * <p>
 * The constructor is given two sorted lists of edges. The positions in the lists are remembered
 * between calls to {@link #checkPreservation(int, Classification, ModelDiff)}, so a
 * {@link PreservationAnalyzer} can be restarted with a new edge classification or type where it
 * left off.
 * <p>
 * See usage in
 * {@link Model#checkPreservation(Model, PreservationRuleset, org.eclipse.core.runtime.IProgressMonitor)}.
 * 
 * @author Jeff Overbey
 */
abstract class PreservationAnalyzer<A, T, R extends IVPGNode<T>> extends PreservationRuleset.Processor
{
    @SuppressWarnings("serial")
    private static abstract class UnexpectedEdgeException extends Error
    {
        private VPGEdge<?,?,?> edge;

        public UnexpectedEdgeException(VPGEdge<?,?,?> edge)
        {
            this.edge = edge;
        }
        
        @SuppressWarnings("unchecked")
        public <A, T, R extends IVPGNode<T>> VPGEdge<A,T,R> getEdge()
        {
            return (VPGEdge<A,T,R>)edge;
        }
    }
    
    @SuppressWarnings("serial")
    private static class UnexpectedInitialEdge extends UnexpectedEdgeException
    {
        public UnexpectedInitialEdge(VPGEdge<?,?,?> edge) { super(edge); }
    }
    
    @SuppressWarnings("serial")
    private static class UnexpectedFinalEdge extends UnexpectedEdgeException
    {
        public UnexpectedFinalEdge(VPGEdge<?,?,?> edge) { super(edge); }
    }
    
    private final Iterable<VPGEdge<A,T,R>> initialEdges;
    private final Iterable<VPGEdge<A,T,R>> finalEdges;
    private final PreservationRuleset ruleset;
    
    private final Iterator<VPGEdge<A,T,R>> initialIterator;
    private final Iterator<VPGEdge<A,T,R>> finalIterator;
    private VPGEdge<A,T,R> initialEdge;
    private VPGEdge<A,T,R> finalEdge;
    
    private int targetType;
    private Classification targetClassification;
    
    public PreservationAnalyzer(
        Collection<VPGEdge<A,T,R>> initialEdges,
        Collection<VPGEdge<A,T,R>> finalEdges,
        PreservationRuleset ruleset)
    {
        this.initialEdges = initialEdges;
        this.finalEdges = finalEdges;
        this.ruleset = ruleset;
        this.initialIterator = initialEdges.iterator();
        this.finalIterator = finalEdges.iterator();
        this.initialEdge = initialIterator.hasNext() ? initialIterator.next() : null;
        this.finalEdge = finalIterator.hasNext() ? finalIterator.next() : null;
    }

    public void checkPreservation(int targetType, Classification targetClassification, ModelDiff diff) throws UnexpectedEdgeException
    {
        this.targetType = targetType;
        this.targetClassification = targetClassification;
        
        while (finalEdge() != null || initialEdge() != null)
        {
            try
            {
                ruleset.invokeCallback(targetType, targetClassification, this);
            }
            catch (UnexpectedInitialEdge exception)
            {
                VPGEdge<A,T,R> entry = exception.getEdge();
                System.err.println("Unexpected initial edge " + entry); //$NON-NLS-1$
                VPGEdge<A,T,R> otherEntry = findEdgeWithNewSink(entry);
                if (otherEntry != null)
                {
                    //that.edges.remove(otherEntry);
                    diff.add(new EdgeSinkChanged(entry, otherEntry));
                }
                else
                {
//                    if (that.filesWithNoEdges.contains(entry.getSource().getFilename()))
//                        diff.recordFileWithNoEdges(entry.getSource().getFilename());
//                    else if (that.filesWithNoEdges.contains(entry.getSink().getFilename()))
//                        diff.recordFileWithNoEdges(entry.getSink().getFilename());
//                    else
                        diff.add(new EdgeDeleted(entry));
                }
            }
            catch (UnexpectedFinalEdge exception)
            {
                VPGEdge<A,T,R> edge = exception.getEdge();
                System.err.println("Unexpected final edge " + edge); //$NON-NLS-1$
                diff.add(new EdgeAdded(edge));
            }
        }
    }
    
    private VPGEdge<A,T,R> finalEdge(){ return getEdge(finalEdge); }

    private VPGEdge<A,T,R> initialEdge() { return getEdge(initialEdge); }
    
    private VPGEdge<A,T,R> getEdge(VPGEdge<A,T,R> edge)
    {
        if (edge == null) return null;
        if (edge.getType() < targetType) throw new IllegalStateException("INTERNAL ERROR: Type " + targetType + " processing terminated prematurely"); //$NON-NLS-1$ //$NON-NLS-2$
        if (edge.getType() != targetType) return null;
        if (!edge.getClassification().equals(targetClassification)) return null;
        return edge;
    }
    
    @Override void handleIgnore()
    {
        if (finalEdge() != null) finalEdge = finalIterator.hasNext() ? finalIterator.next() : null;
        if (initialEdge() != null) initialEdge = initialIterator.hasNext() ? initialIterator.next() : null;
    }

    @Override void handlePreserveAll()
    {
        VPGEdge<A,T,R> finalEdge = finalEdge();
        VPGEdge<A,T,R> initialEdge = initialEdge();
        
        if (finalEdge != null && initialEdge == null)
        {
            this.finalEdge = finalIterator.hasNext() ? finalIterator.next() : null;
            throw new UnexpectedFinalEdge(finalEdge);
        }
        else if (finalEdge == null && initialEdge != null)
        {
            this.initialEdge = initialIterator.hasNext() ? initialIterator.next() : null;
            throw new UnexpectedInitialEdge(initialEdge);
        }
        else if (finalEdge != null && initialEdge != null)
        {
            int comparison = finalEdge.compareTo(initialEdge);
            if (comparison < 0)
            {
                this.finalEdge = finalIterator.hasNext() ? finalIterator.next() : null;
                throw new UnexpectedFinalEdge(finalEdge);
            }
            else if (comparison == 0)
            {
                this.initialEdge = initialIterator.hasNext() ? initialIterator.next() : null;
                this.finalEdge = finalIterator.hasNext() ? finalIterator.next() : null;
            }
            else // (comparison > 0)
            {
                this.initialEdge = initialIterator.hasNext() ? initialIterator.next() : null;
                throw new UnexpectedInitialEdge(finalEdge);
            }
        }
    }
    
    @Override void handlePreserveSubset()
    {
        VPGEdge<A,T,R> finalEdge = finalEdge();
        VPGEdge<A,T,R> initialEdge = initialEdge();
        
        if (finalEdge != null && initialEdge == null)
        {
            this.finalEdge = finalIterator.hasNext() ? finalIterator.next() : null;
            throw new UnexpectedFinalEdge(finalEdge);
        }
        else if (finalEdge != null && initialEdge != null)
        {
            int comparison = finalEdge.compareTo(initialEdge);
            if (comparison < 0)
            {
                this.finalEdge = finalIterator.hasNext() ? finalIterator.next() : null;
                throw new UnexpectedFinalEdge(finalEdge);
            }
            else if (comparison == 0)
            {
                this.initialEdge = initialIterator.hasNext() ? initialIterator.next() : null;
                this.finalEdge = finalIterator.hasNext() ? finalIterator.next() : null;
            }
            else // (comparison > 0)
            {
                this.initialEdge = initialIterator.hasNext() ? initialIterator.next() : null;
            }
        }
    }
    
    @Override void handlePreserveSuperset()
    {
        VPGEdge<A,T,R> finalEdge = finalEdge();
        VPGEdge<A,T,R> initialEdge = initialEdge();
        
        if (finalEdge == null && initialEdge != null)
        {
            this.initialEdge = initialIterator.hasNext() ? initialIterator.next() : null;
            throw new UnexpectedInitialEdge(initialEdge);
        }
        else if (finalEdge != null && initialEdge != null)
        {
            int comparison = finalEdge.compareTo(initialEdge);
            if (comparison < 0)
            {
                this.finalEdge = finalIterator.hasNext() ? finalIterator.next() : null;
                throw new UnexpectedFinalEdge(finalEdge);
            }
            else if (comparison == 0)
            {
                this.initialEdge = initialIterator.hasNext() ? initialIterator.next() : null;
                this.finalEdge = finalIterator.hasNext() ? finalIterator.next() : null;
            }
            else // (comparison > 0)
            {
                this.initialEdge = initialIterator.hasNext() ? initialIterator.next() : null;
            }
        }
    }

    public boolean hasEdgesRemaining()
    {
        return initialIterator.hasNext() || finalIterator.hasNext();
    }

    public void ensureNoEdgesRemaining(int type)
    {
        if (initialEdge != null
                && initialEdge.getType() == type)
            throw new IllegalStateException("INTERNAL ERROR: Type " + type + " processing incomplete - initial edge "+ initialEdge);  //$NON-NLS-1$//$NON-NLS-2$

        if (finalEdge != null
                && finalEdge.getType() == type)
            throw new IllegalStateException("INTERNAL ERROR: Type " + type + " processing incomplete - final edge "+ finalEdge);  //$NON-NLS-1$//$NON-NLS-2$
    }
    
    private VPGEdge<A,T,R> findEdgeWithNewSink(VPGEdge<A,T,R> initialEdge)
    {
        for (VPGEdge<A,T,R> finalEdge : finalEdges)
        {
            if (finalEdge.getSource() != null
                && finalEdge.getSource().equals(initialEdge.getSource())
                && finalEdge.getType() == initialEdge.getType())
            {
                return finalEdge;
            }
        }
        return null;
    }

    /** @return the number of edges remaining to be processed */
    @SuppressWarnings("rawtypes")
    public int countEdgesRemaining()
    {
        if (initialEdges instanceof List
            && initialIterator instanceof ListIterator
            && finalEdges instanceof List
            && initialIterator instanceof ListIterator)
        {
            return (((List)initialEdges).size() - ((ListIterator)initialIterator).nextIndex())
                 + (((List)finalEdges).size() - ((ListIterator)finalIterator).nextIndex());
        }
        else return 0;
    }
}
