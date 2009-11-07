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
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.rephraserengine.core.preservation.Preserve;
import org.eclipse.rephraserengine.core.vpg.TokenRef;
import org.eclipse.rephraserengine.core.vpg.VPGEdge;
import org.eclipse.rephraserengine.core.vpg.eclipse.EclipseVPG;
import org.eclipse.rephraserengine.internal.core.preservation.ModelDiff.EdgeAdded;
import org.eclipse.rephraserengine.internal.core.preservation.ModelDiff.EdgeDeleted;
import org.eclipse.rephraserengine.internal.core.preservation.ModelDiff.EdgeSinkChanged;

/**
 *
 * @author Jeff Overbey
 */
public class Model
{
    private static final class Entry implements Comparable<Entry>
    {
        private String sourceFilename;
        private Interval source, origSource;
        private String sinkFilename;
        private Interval sink;
        private int edgeType;

        public Entry(String sourceFilename, Interval source, String sinkFilename, Interval sink, int edgeType)
        {
            if (source == null || sink == null) throw new IllegalArgumentException();

            this.sourceFilename = sourceFilename;
            this.source = this.origSource = source;
            this.sinkFilename = sinkFilename;
            this.sink = sink;
            this.edgeType = edgeType;
        }

        public boolean shouldPreserveAccordingTo(Set<Preserve> preserveEdgeTypes, String affectedFilename, Interval affected)
        {
            for (Preserve rule : preserveEdgeTypes)
                if (shouldPreserveAccordingTo(rule, affectedFilename, affected))
                    return true;

            return false;
        }

        public boolean shouldPreserveAccordingTo(Preserve rule, String affectedFilename, Interval affected)
        {
            boolean incoming = sinkFilename.equals(affectedFilename) && sink.isSubsetOf(affected);
            boolean outgoing = sourceFilename.equals(affectedFilename) && source.isSubsetOf(affected);
            return rule.shouldPreserve(incoming, outgoing, this.edgeType);
        }

        @Override public boolean equals(Object o)
        {
            if (!o.getClass().equals(this.getClass())) return false;

            Entry that = (Entry)o;
            return this.sourceFilename.equals(that.sourceFilename)
                && this.source.equals(that.source)
                && this.sinkFilename.equals(that.sinkFilename)
                && this.sink.equals(that.sink)
                && this.edgeType == that.edgeType;
        }

        @Override public int hashCode()
        {
            return 263 * source.hashCode() + 13 * sink.hashCode() + edgeType;
        }

        public int compareTo(Entry that)
        {
            // Lexicographic comparison
            int result = this.sourceFilename.compareTo(that.sourceFilename);
            if (result == 0)
            {
                result = this.sinkFilename.compareTo(that.sinkFilename);
                if (result == 0)
                {
                    result = this.source.compareTo(that.source);
                    if (result == 0)
                    {
                        result = this.sink.compareTo(that.sink);
                        if (result == 0)
                        {
                            return that.edgeType - this.edgeType;
                        }
                    }
                }
            }
            return result;
        }
    }

    private EclipseVPG<?,?,?,?,?> vpg;
    private List<String> files;
    private Set<Entry> edges;

    public Model(EclipseVPG<?,?,?,?,?> vpg, String... filenames)
    {
        this(vpg, Arrays.asList(filenames));
    }

    public Model(EclipseVPG<?,?,?,?,?> vpg, List<String> filenames)
    {
        this.vpg = vpg;
        this.files = vpg.sortFilesAccordingToDependencies(new ArrayList<String>(filenames), new NullProgressMonitor());
        this.edges = new TreeSet<Entry>();
        for (String thisFile : files)
            addEdges(thisFile);
    }

    private void addEdges(String filename)
    {
        for (VPGEdge<?,?,?> edge : vpg.db.getAllEdgesFor(filename))
        {
            TokenRef<?> source = edge.getSource();
            TokenRef<?> sink = edge.getSink();

            edges.add(
                new Entry(
                    source.getFilename(),
                    new Interval(source.getOffset(), source.getEndOffset()),
                    sink.getFilename(),
                    new Interval(sink.getOffset(), sink.getEndOffset()),
                    edge.getType()));
        }
    }

    public List<String> getFiles()
    {
        return files;
    }

    public void inormalize(PrimitiveOp op, Set<Preserve> preserveEdgeTypes)
    {
        TreeSet<Entry> revisedList = new TreeSet<Entry>();

        for (Entry entry : edges)
        {
            if (entry.shouldPreserveAccordingTo(preserveEdgeTypes, op.filename, op.iaff()))
            {
                entry.source = op.inorm(entry.sourceFilename, entry.source); // leave origSource unchanged
                entry.sink = op.inorm(entry.sinkFilename, entry.sink);
                revisedList.add(entry);
            }
        }

        edges = revisedList;
    }

    public void inormalize(List<PrimitiveOp> primitiveOps, Set<Preserve> preserveEdgeTypes)
    {
        for (PrimitiveOp op : primitiveOps)
            inormalize(op, preserveEdgeTypes);
    }

    public void dnormalize(PrimitiveOp op, Set<Preserve> preserveEdgeTypes)
    {
        TreeSet<Entry> revisedList = new TreeSet<Entry>();

        for (Entry entry : edges)
        {
            if (entry.shouldPreserveAccordingTo(preserveEdgeTypes, op.filename, op.daff()))
            {
                entry.source = op.dnorm(entry.sourceFilename, entry.source);
                entry.sink = op.dnorm(entry.sinkFilename, entry.sink);
                revisedList.add(entry);
            }
        }

        edges = revisedList;
    }

    public void dnormalize(List<PrimitiveOp> primitiveOps, Set<Preserve> preserveEdgeTypes)
    {
        for (PrimitiveOp op : primitiveOps)
            dnormalize(op, preserveEdgeTypes);
    }

    public ModelDiff compareAgainst(Model that)
    {
        ModelDiff diff = new ModelDiff();

        for (Entry entry : this.edges)
        {
            if (!that.edges.contains(entry))
            {
                Entry otherEntry = findEntryWithNewSink(entry, that.edges);
                if (otherEntry != null)
                {
                    that.edges.remove(otherEntry);
                    diff.add(
                        new EdgeSinkChanged(
                            entry.sourceFilename,
                            entry.source,
                            entry.sinkFilename,
                            entry.sink,
                            otherEntry.sinkFilename,
                            otherEntry.sink,
                            entry.edgeType));
                }
                else
                {
                    diff.add(
                        new EdgeDeleted(
                            entry.sourceFilename,
                            entry.origSource,
                            entry.sinkFilename,
                            entry.sink,
                            entry.edgeType));
                }
            }
        }

        for (Entry entry : that.edges)
        {
            if (!this.edges.contains(entry))
            {
                diff.add(
                    new EdgeAdded(
                        entry.sourceFilename,
                        entry.source,
                        entry.sinkFilename,
                        entry.sink,
                        entry.edgeType));
            }
        }

        return diff;
    }

    private Entry findEntryWithNewSink(Entry entry, Set<Entry> otherEdgeList)
    {
        for (Entry otherEntry : otherEdgeList)
        {
            if (otherEntry.sourceFilename.equals(entry.sourceFilename)
                && otherEntry.source.equals(entry.source)
                && otherEntry.edgeType == entry.edgeType)
            {
                return otherEntry;
            }
        }

        return null;
    }

    @Override public String toString()
    {
        return toString(null, null, null);
    }

    public String toString(String filename, CharSequence fileContents, ArrayList<Integer> lineMap)
    {
        StringBuilder sb = new StringBuilder();

        sb.append(edges.size());
        sb.append(" edges\n\n");

        for (Entry entry : edges)
        {
            if (!entry.sourceFilename.equals(filename))
            {
                sb.append(entry.sourceFilename);
                sb.append(':');
            }
            sb.append('[');
            sb.append(String.format("%5d", entry.source.lb));
            sb.append(", ");
            sb.append(String.format("%5d", entry.source.ub));
            sb.append(")  ===(");
            sb.append(entry.edgeType);
            sb.append(")==>  ");
            if (!entry.sinkFilename.equals(filename))
            {
                sb.append(entry.sinkFilename);
                sb.append(':');
            }
            sb.append('[');
            sb.append(String.format("%5d", entry.sink.lb));
            sb.append(", ");
            sb.append(String.format("%5d", entry.sink.ub));
            sb.append(")");

            if (fileContents != null)
            {
                sb.append("           [");
                if (!entry.sourceFilename.equals(filename))
                    sb.append('?');
                else if (entry.source.lb >= 0 && entry.source.ub >= entry.source.lb)
                    sb.append(fileContents.subSequence(entry.source.lb, entry.source.ub));
                sb.append("]");
                if (lineMap != null)
                {
                    sb.append(" (Line ");
                    sb.append(getLine(entry.source.lb, lineMap));
                    sb.append(")");
                }
                sb.append("  ===(");
                sb.append(vpg.describeEdgeType(entry.edgeType));
                sb.append(")==>  [");
                if (!entry.sinkFilename.equals(filename))
                    sb.append('?');
                else if (entry.sink.lb >= 0 && entry.sink.ub >= entry.sink.lb)
                    sb.append(fileContents.subSequence(entry.sink.lb, entry.sink.ub));
                sb.append("]");
                if (lineMap != null)
                {
                    sb.append(" (Line ");
                    sb.append(getLine(entry.sink.lb, lineMap));
                    sb.append(")");
                }
            }

            sb.append('\n');
        }
        return sb.toString();
    }

    private int getLine(int offset, ArrayList<Integer> lineMap)
    {
        for (int i = 0; i < lineMap.size(); i++)
            if (offset < lineMap.get(i))
                return i+1;
        return lineMap.size();
    }
}
