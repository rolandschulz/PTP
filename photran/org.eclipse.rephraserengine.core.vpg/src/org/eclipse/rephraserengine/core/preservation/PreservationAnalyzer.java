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

import java.util.List;

import org.eclipse.rephraserengine.core.preservation.ModelDiff.EdgeAdded;
import org.eclipse.rephraserengine.core.preservation.ModelDiff.EdgeDeleted;
import org.eclipse.rephraserengine.core.preservation.ModelDiff.EdgeSinkChanged;
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
final class PreservationAnalyzer extends PreservationRuleset.Processor
{
    @SuppressWarnings("serial")
    private static abstract class UnexpectedEdgeException extends Error
    {
        private VPGEdge<?,?,?> edge;

        public UnexpectedEdgeException(VPGEdge<?,?,?> edge)
        {
            this.edge = edge;
        }
        
        public VPGEdge<?,?,?> getEdge()
        {
            return edge;
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
    
    private final List<VPGEdge<?,?,?>> initialEdges;
    private final List<VPGEdge<?,?,?>> finalEdges;
    private final PreservationRuleset ruleset;
    
    private int targetType;
    private Classification targetClassification;
    
    private int initialIndex;
    private int finalIndex;
    
    public PreservationAnalyzer(
        List<VPGEdge<?,?,?>> initialEdges,
        List<VPGEdge<?,?,?>> finalEdges,
        PreservationRuleset ruleset)
    {
        this.initialEdges = initialEdges;
        this.finalEdges = finalEdges;
        this.ruleset = ruleset;
        this.initialIndex = 0;
        this.finalIndex = 0;
    }

    public void checkPreservation(int targetType, Classification targetClassification, ModelDiff diff) throws UnexpectedEdgeException
    {
        this.targetType = targetType;
        this.targetClassification = targetClassification;
        
        while (finalEdge() != null || initialEdge() != null)
        {
            int oldI = initialIndex;
            int oldF = finalIndex;
            try
            {
                ruleset.invokeCallback(targetType, targetClassification, this);
            }
            catch (UnexpectedInitialEdge exception)
            {
                VPGEdge<?,?,?> entry = exception.getEdge();
                VPGEdge<?,?,?> otherEntry = findEdgeWithNewSink(entry);
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
                VPGEdge<?,?,?> edge = exception.getEdge();
                diff.add(new EdgeAdded(edge));
            }
            if (initialIndex <= oldI && finalIndex <= oldF)
                throw new IllegalStateException("INTERNAL ERROR: No progress - type " + targetType + " " + targetClassification); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }
    
    private VPGEdge<?,?,?> finalEdge() { return getEdge(finalEdges, finalIndex); }

    private VPGEdge<?,?,?> initialEdge() { return getEdge(initialEdges, initialIndex); }
    
    private VPGEdge<?,?,?> getEdge(List<VPGEdge<?,?,?>> edgeList, int index)
    {
        if (index >= edgeList.size()) return null;
        
        VPGEdge<?,?,?> edge = edgeList.get(index);
        if (edge.getType() < targetType) throw new IllegalStateException("INTERNAL ERROR: Type " + targetType + " processing terminated prematurely"); //$NON-NLS-1$ //$NON-NLS-2$
        if (edge.getType() != targetType) return null;
        if (!edge.getClassification().equals(targetClassification)) return null;
        return edge;
    }
    
    @Override void handleIgnore()
    {
        if (finalEdge() != null) finalIndex++;
        if (initialEdge() != null) initialIndex++;
    }

    @Override void handlePreserveAll()
    {
        VPGEdge<?,?,?> finalEdge = finalEdge();
        VPGEdge<?,?,?> initialEdge = initialEdge();
        
        if (finalEdge != null && initialEdge == null)
        {
            finalIndex++;
            throw new UnexpectedFinalEdge(finalEdge);
        }
        else if (finalEdge == null && initialEdge != null)
        {
            initialIndex++;
            throw new UnexpectedInitialEdge(initialEdge);
        }
        else if (finalEdge != null && initialEdge != null)
        {
            int comparison = finalEdge.compareTo(initialEdge);
            if (comparison < 0)
            {
                finalIndex++;
                throw new UnexpectedFinalEdge(finalEdge);
            }
            else if (comparison == 0)
            {
                initialIndex++;
                finalIndex++;
            }
            else // (comparison > 0)
            {
                initialIndex++;
                throw new UnexpectedInitialEdge(finalEdge);
            }
        }
    }
    
    @Override void handlePreserveSubset()
    {
        VPGEdge<?,?,?> finalEdge = finalEdge();
        VPGEdge<?,?,?> initialEdge = initialEdge();
        
        if (finalEdge != null && initialEdge == null)
        {
            finalIndex++;
            throw new UnexpectedFinalEdge(finalEdge);
        }
        else if (finalEdge != null && initialEdge != null)
        {
            int comparison = finalEdge.compareTo(initialEdge);
            if (comparison < 0)
            {
                finalIndex++;
                throw new UnexpectedFinalEdge(finalEdge);
            }
            else if (comparison == 0)
            {
                initialIndex++;
                finalIndex++;
            }
            else // (comparison > 0)
            {
                initialIndex++;
            }
        }
    }
    
    @Override void handlePreserveSuperset()
    {
        throw new UnsupportedOperationException(); // FIXME
    }

    public boolean hasEdgesRemaining()
    {
        return initialIndex < initialEdges.size() || finalIndex < finalEdges.size();
    }

    public void ensureNoEdgesRemaining(int type)
    {
        if (initialIndex < initialEdges.size()
                && initialEdges.get(initialIndex).getType() == type)
            throw new IllegalStateException("INTERNAL ERROR: Type " + type + " processing incomplete - initial edge "+ initialEdges.get(initialIndex));  //$NON-NLS-1$//$NON-NLS-2$

        if (finalIndex < finalEdges.size()
                && finalEdges.get(finalIndex).getType() == type)
            throw new IllegalStateException("INTERNAL ERROR: Type " + type + " processing incomplete - final edge "+ finalEdges.get(finalIndex));  //$NON-NLS-1$//$NON-NLS-2$
    }
    
    private VPGEdge<?,?,?> findEdgeWithNewSink(VPGEdge<?,?,?> initialEdge)
    {
        for (VPGEdge<?,?,?> finalEdge : finalEdges)
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

//    public List<VPGEdge<?,?,?>> getRemainingInitialEdges()
//    {
//        return initialEdges.subList(initialIndex, initialEdges.size());
//    }
//
//    public List<VPGEdge<?,?,?>> getRemainingFinalEdges()
//    {
//        return finalEdges.subList(finalIndex, finalEdges.size());
//    }

    public int countEdgesRemaining()
    {
        return (initialEdges.size() - initialIndex) + (finalEdges.size() - finalIndex);
    }
}
