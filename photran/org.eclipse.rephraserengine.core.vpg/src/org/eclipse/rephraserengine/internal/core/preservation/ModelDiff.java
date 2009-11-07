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
import java.util.Iterator;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.rephraserengine.core.vpg.eclipse.EclipseVPG;

/**
 *
 * @author Jeff Overbey
 */
public class ModelDiff implements Iterable<ModelDiff.DiffEntry>
{
    public static abstract class ModelDiffProcessor
    {
        public abstract void processEdgeAdded(EdgeAdded addition);
        public abstract void processEdgeDeleted(EdgeDeleted deletion);
        public abstract void processEdgeSinkChanged(EdgeSinkChanged change);
    }

    public static abstract class DiffEntry
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

        public IFile getFileContainingRegion()
        {
            return EclipseVPG.getIFileForFilename(sourceFilename);
        }

        public IRegion toRegion()
        {
            return new Region(source.lb, source.cardinality());
        }

        @Override public boolean equals(Object other)
        {
            if (!other.getClass().equals(this.getClass())) return false;

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
            return sourceFilename + ":" + source + " -> " + sinkFilename + ":" + sink;
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

        @Override public String toString()
        {
            return
                sourceFilename +
                ":" +
                source +
                " -> " +
                sinkFilename +
                ":" +
                sink +
                " will become " +
                sourceFilename +
                ":" +
                source +
                " -> " +
                newSinkFilename +
                ":" +
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

    private Set<DiffEntry> differences = new HashSet<DiffEntry>();

    void add(DiffEntry entry)
    {
        differences.add(entry);
    }

    public Iterator<DiffEntry> iterator()
    {
        return differences.iterator();
    }

    public void processUsing(ModelDiffProcessor processor)
    {
        for (DiffEntry entry : differences)
            entry.accept(processor);
    }
}
