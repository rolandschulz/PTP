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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;

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
        public final Interval source;
        public final Interval sink;
        public final int edgeType;

        public DiffEntry(Interval source, Interval sink, int edgeType)
        {
            this.source = source;
            this.sink = sink;
            this.edgeType = edgeType;
        }

        protected abstract void accept(ModelDiffProcessor processor);

        public IRegion toRegion()
        {
            return new Region(source.lb, source.cardinality());
        }
    }

    public static final class EdgeAdded extends DiffEntry
    {
        public EdgeAdded(Interval source, Interval sink, int edgeType)
        {
            super(source, sink, edgeType);
        }

        @Override protected void accept(ModelDiffProcessor processor)
        {
            processor.processEdgeAdded(this);
        }
    }

    public static final class EdgeDeleted extends DiffEntry
    {
        public EdgeDeleted(Interval source, Interval sink, int edgeType)
        {
            super(source, sink, edgeType);
        }

        @Override protected void accept(ModelDiffProcessor processor)
        {
            processor.processEdgeDeleted(this);
        }
    }

    public static final class EdgeSinkChanged extends DiffEntry
    {
        public final Interval newSink;

        public EdgeSinkChanged(Interval source, Interval sink, Interval newSink, int edgeType)
        {
            super(source, sink, edgeType);
            this.newSink = newSink;
        }

        @Override protected void accept(ModelDiffProcessor processor)
        {
            processor.processEdgeSinkChanged(this);
        }
    }

    private List<DiffEntry> differences = new LinkedList<DiffEntry>();

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
