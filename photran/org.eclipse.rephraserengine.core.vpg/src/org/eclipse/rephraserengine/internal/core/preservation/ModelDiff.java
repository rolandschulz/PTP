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

import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.rephraserengine.core.vpg.eclipse.EclipseVPG;

/**
 * An object representing the differences between two program graphs.
 * <p>
 * Program graphs are represented as {@link Model} objects.  Differences are described in terms of
 * (1) added edges, (2) deleted edges, and (3) edges whose sink endpoints changed.  The list of
 * changes is traversed via an Internal Iterator using {@link #processUsing(ModelDiffProcessor)}.
 * 
 * @author Jeff Overbey
 * 
 * @since 1.0
 */
public final class ModelDiff
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
        public final String sourceFilename;
        public final Interval source;
        public final String sinkFilename;
        public final Interval sink;
        public final int edgeType;

        public DiffEntry(
            String sourceFilename,
            Interval source,
            String sinkFilename,
            Interval sink,
            int edgeType)
        {
            this.sourceFilename = sourceFilename;
            this.source = source;
            this.sinkFilename = sinkFilename;
            this.sink = sink;
            this.edgeType = edgeType;
        }

        protected abstract void accept(ModelDiffProcessor processor);

        public IFile getFileContainingSourceRegion()
        {
            return EclipseVPG.getIFileForFilename(sourceFilename);
        }

        public IRegion getSourceRegion()
        {
            return new Region(source.lb, source.cardinality());
        }

        public IFile getFileContainingSinkRegion()
        {
            return EclipseVPG.getIFileForFilename(sinkFilename);
        }

        public IRegion getSinkRegion()
        {
            return new Region(sink.lb, sink.cardinality());
        }

        @Override public boolean equals(Object other)
        {
            if (other == null || !other.getClass().equals(this.getClass())) return false;

            DiffEntry that = (DiffEntry)other;
            return this.sourceFilename.equals(that.sourceFilename)
                && this.source.equals(that.source)
                && this.sinkFilename.equals(that.sinkFilename)
                && this.sink.equals(that.sink)
                && this.edgeType == that.edgeType;
        }

        @Override public int hashCode()
        {
            return this.getClass().hashCode()
                + 3 * sourceFilename.hashCode()
                + 5 * source.hashCode()
                + 7 * sinkFilename.hashCode()
                + 11 * sink.hashCode()
                + 13 * edgeType;
        }

        @Override public String toString()
        {
            return sourceFilename + ":" + source + " -> " + sinkFilename + ":" + sink; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }
    }

    public static final class EdgeAdded extends DiffEntry
    {
        public EdgeAdded(
            String sourceFilename,
            Interval source,
            String sinkFilename,
            Interval sink,
            int edgeType)
        {
            super(sourceFilename, source, sinkFilename, sink, edgeType);
        }

        @Override protected void accept(ModelDiffProcessor processor)
        {
            processor.processEdgeAdded(this);
        }
    }

    public static final class EdgeDeleted extends DiffEntry
    {
        public EdgeDeleted(
            String sourceFilename,
            Interval source,
            String sinkFilename,
            Interval sink,
            int edgeType)
        {
            super(sourceFilename, source, sinkFilename, sink, edgeType);
        }

        @Override protected void accept(ModelDiffProcessor processor)
        {
            processor.processEdgeDeleted(this);
        }
    }

    public static final class EdgeSinkChanged extends DiffEntry
    {
        public final String newSinkFilename;
        public final Interval newSink;

        public EdgeSinkChanged(
            String sourceFilename,
            Interval source,
            String sinkFilename,
            Interval sink,
            String newSinkFilename,
            Interval newSink,
            int edgeType)
        {
            super(sourceFilename, source, sinkFilename, sink, edgeType);
            this.newSinkFilename = newSinkFilename;
            this.newSink = newSink;
        }

        @Override protected void accept(ModelDiffProcessor processor)
        {
            processor.processEdgeSinkChanged(this);
        }

        public IFile getFileContainingNewSinkRegion()
        {
            return EclipseVPG.getIFileForFilename(newSinkFilename);
        }

        public IRegion getNewSinkRegion()
        {
            return new Region(newSink.lb, newSink.cardinality());
        }

        @Override public String toString()
        {
            return
                sourceFilename +
                ":" + //$NON-NLS-1$
                source +
                " -> " + //$NON-NLS-1$
                sinkFilename +
                ":" + //$NON-NLS-1$
                sink +
                " will become " + //$NON-NLS-1$
                sourceFilename +
                ":" + //$NON-NLS-1$
                source +
                " -> " + //$NON-NLS-1$
                newSinkFilename +
                ":" + //$NON-NLS-1$
                newSink;
        }

        @Override public boolean equals(Object other)
        {
            return super.equals(other)
                && this.newSinkFilename.equals(((EdgeSinkChanged)other).newSinkFilename)
                && this.newSink.equals(((EdgeSinkChanged)other).newSink);
        }

        @Override public int hashCode()
        {
            return super.hashCode()
                + 17 * newSinkFilename.hashCode()
                + 19 * newSink.hashCode();
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
