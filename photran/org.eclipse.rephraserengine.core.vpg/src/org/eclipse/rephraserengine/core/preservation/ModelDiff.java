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

import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.rephraserengine.core.preservation.ModelDiff.ModelDiffProcessor;
import org.eclipse.rephraserengine.core.vpg.TokenRef;
import org.eclipse.rephraserengine.core.vpg.VPGEdge;
import org.eclipse.rephraserengine.core.vpg.eclipse.EclipseVPG;

/**
 * An object representing the differences between two program graphs.
 * <p>
 * Program graphs are represented as {@link Model} objects.  Differences are described in terms of
 * (1) added edges, (2) deleted edges, and (3) edges whose sink endpoints changed.  The list of
 * changes is traversed via an Internal Iterator using {@link #processUsing(ModelDiffProcessor)}.
 * 
 * @author Jeff Overbey
 */
@SuppressWarnings("unused")
final class ModelDiff
{
    public static abstract class ModelDiffProcessor
    {
        public abstract void processEdgeAdded(EdgeAdded addition);
        public abstract void processEdgeDeleted(EdgeDeleted deletion);
        public abstract void processAllEdgesDeleted(Set<String> filenames);
        public abstract void processEdgeSinkChanged(EdgeSinkChanged change);
    }

    protected static abstract class DiffEntry
    {
        public final VPGEdge<?,?,?> edge;

        public DiffEntry(VPGEdge<?,?,?> edge)
        {
            this.edge = edge.getOriginalEdge();
        }

        protected abstract void accept(ModelDiffProcessor processor);

        public IFile getFileContainingSourceRegion()
        {
            return EclipseVPG.getIFileForFilename(edge.getSource().getFilename());
        }

        public IRegion getSourceRegion()
        {
            return new Region(edge.getSource().getOffset(), edge.getSource().getLength());
        }

        public IFile getFileContainingSinkRegion()
        {
            return EclipseVPG.getIFileForFilename(edge.getSink().getFilename());
        }

        public IRegion getSinkRegion()
        {
            return new Region(edge.getSink().getOffset(), edge.getSink().getLength());
        }

        @Override public boolean equals(Object other)
        {
            if (other == null || !other.getClass().equals(this.getClass())) return false;

            DiffEntry that = (DiffEntry)other;
            return this.edge.equals(that.edge);
        }

        @Override public int hashCode()
        {
            return edge.hashCode();
        }

        @Override public String toString()
        {
            return edge.toString();
        }
    }

    public static final class EdgeAdded extends DiffEntry
    {
        public EdgeAdded(VPGEdge<?,?,?> edge)
        {
            super(edge);
        }

        @Override protected void accept(ModelDiffProcessor processor)
        {
            processor.processEdgeAdded(this);
        }
    }

    public static final class EdgeDeleted extends DiffEntry
    {
        public EdgeDeleted(VPGEdge<?,?,?> edge)
        {
            super(edge);
        }

        @Override protected void accept(ModelDiffProcessor processor)
        {
            processor.processEdgeDeleted(this);
        }
    }

    public static final class EdgeSinkChanged extends DiffEntry
    {
        public final VPGEdge<?,?,?> newEdge;

        public EdgeSinkChanged(VPGEdge<?,?,?> edge, VPGEdge<?,?,?> newEdge)
        {
            super(edge);
            this.newEdge = newEdge.getOriginalEdge();
        }

        @Override protected void accept(ModelDiffProcessor processor)
        {
            processor.processEdgeSinkChanged(this);
        }

        public IFile getFileContainingNewSinkRegion()
        {
            return EclipseVPG.getIFileForFilename(newEdge.getSink().getFilename());
        }

        public IRegion getNewSinkRegion()
        {
            TokenRef<?> newSink = newEdge.getSink();
            return new Region(newSink.getOffset(), newSink.getLength());
        }

        @Override public String toString()
        {
            return
                edge.toString() +
                " will become " + //$NON-NLS-1$
                newEdge.toString();
        }

        @Override public boolean equals(Object other)
        {
            return super.equals(other)
                && this.newEdge.equals(((EdgeSinkChanged)other).newEdge);
        }

        @Override public int hashCode()
        {
            return super.hashCode() + 17 * newEdge.hashCode();
        }
    }

    private Set<String> filesWithAllEdgesDeleted = new TreeSet<String>();
    private Set<DiffEntry> differences = new HashSet<DiffEntry>();

    void recordFileWithNoEdges(String filename)
    {
        filesWithAllEdgesDeleted.add(filename);
    }
    
    void add(DiffEntry entry)
    {
        differences.add(entry);
    }

//    public Iterator<DiffEntry> iterator()
//    {
//        return differences.iterator();
//    }

    public void processUsing(ModelDiffProcessor processor)
    {
        if (!filesWithAllEdgesDeleted.isEmpty())
            processor.processAllEdgesDeleted(filesWithAllEdgesDeleted);
        
        for (DiffEntry entry : differences)
            entry.accept(processor);
    }
}
