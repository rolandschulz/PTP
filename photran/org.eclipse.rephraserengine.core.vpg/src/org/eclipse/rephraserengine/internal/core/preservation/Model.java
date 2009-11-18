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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.rephraserengine.core.preservation.PreservationRule;
import org.eclipse.rephraserengine.core.vpg.TokenRef;
import org.eclipse.rephraserengine.core.vpg.VPG;
import org.eclipse.rephraserengine.core.vpg.VPGEdge;
import org.eclipse.rephraserengine.core.vpg.eclipse.EclipseVPG;
import org.eclipse.rephraserengine.internal.core.preservation.ModelDiff.EdgeAdded;
import org.eclipse.rephraserengine.internal.core.preservation.ModelDiff.EdgeDeleted;
import org.eclipse.rephraserengine.internal.core.preservation.ModelDiff.EdgeSinkChanged;

/**
 * A mutable, in-memory copy of part of a program graph.
 *
 * @author Jeff Overbey
 */
public final class Model
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

        public boolean shouldPreserveAccordingTo(Set<PreservationRule> preserveEdgeTypes, String affectedFilename, Interval affected)
        {
            for (PreservationRule rule : preserveEdgeTypes)
                if (shouldPreserveAccordingTo(rule, affectedFilename, affected))
                    return true;

            return false;
        }

        public boolean shouldPreserveAccordingTo(PreservationRule rule, String affectedFilename, Interval affected)
        {
            boolean incoming = sinkFilename.equals(affectedFilename) && sink.isSubsetOf(affected);
            boolean outgoing = sourceFilename.equals(affectedFilename) && source.isSubsetOf(affected);
            return rule.shouldPreserve(incoming, outgoing, this.edgeType);
        }

        public void offset(PrimitiveOp op)
        {
            this.source = op.inorm(this.sourceFilename, this.source);
            this.sink = op.inorm(this.sinkFilename, this.sink);
        }

        @Override public boolean equals(Object o)
        {
            if (o == null || !o.getClass().equals(this.getClass())) return false;

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

    private String name;
    private EclipseVPG<?,?,?,?,?> vpg;
    private List<String> files;
    private Set<Entry> edges;

    public Model(String name, IProgressMonitor pm, int ticks, EclipseVPG<?,?,?,?,?> vpg, String... filenames)
    {
        this(name, pm, ticks, vpg, Arrays.asList(filenames));
    }

    public Model(String name, IProgressMonitor pm, int ticks, EclipseVPG<?,?,?,?,?> vpg, List<String> filenames)
    {
        this.name = name;
        this.vpg = vpg;

        pm.subTask("Preparing to compute " + name);
        this.files = vpg.sortFilesAccordingToDependencies(new ArrayList<String>(filenames), new NullProgressMonitor());

        pm = new SubProgressMonitor(pm, ticks, SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK);
        pm.beginTask("Computing " + name + ":", files.size());
        this.edges = new TreeSet<Entry>();
        for (String thisFile : files)
        {
            pm.subTask(VPG.lastSegmentOfFilename(thisFile));
            addEdges(thisFile, pm);
            pm.worked(1);
        }
        pm.done();
    }

    private void addEdges(String filename, IProgressMonitor pm)
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

    public void inormalize(PrimitiveOpList primitiveOps, Set<PreservationRule> preserveEdgeTypes, IProgressMonitor pm)
    {
        pm = new SubProgressMonitor(pm, 0, SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK);
        pm.beginTask("Normalizing " + name, edges.size());

        TreeSet<Entry> revisedList = new TreeSet<Entry>();

        for (Entry entry : edges)
        {
            for (PrimitiveOp op : primitiveOps)
            {
                if (entry.shouldPreserveAccordingTo(preserveEdgeTypes, op.filename, op.iaff()))
                {
                    entry.source = op.inorm(entry.sourceFilename, entry.source); // leave origSource unchanged
                    entry.sink = op.inorm(entry.sinkFilename, entry.sink);

                    for (PrimitiveOp otherOp : primitiveOps)
                        if (!otherOp.equals(op))
                            entry.offset(otherOp);

                    revisedList.add(entry);
                    break;
                }
            }
            pm.worked(1);
        }

        edges = revisedList;

        pm.done();
    }

    public void dnormalize(PrimitiveOpList primitiveOps, Set<PreservationRule> preserveEdgeTypes, IProgressMonitor pm)
    {
        pm = new SubProgressMonitor(pm, 0, SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK);
        pm.beginTask("Normalizing " + name, edges.size());

        TreeSet<Entry> revisedList = new TreeSet<Entry>();

        for (Entry entry : edges)
        {
            for (PrimitiveOp op : primitiveOps)
            {
                Interval daff = offset(op.daff(), op, primitiveOps);
                if (entry.shouldPreserveAccordingTo(preserveEdgeTypes, op.filename, daff))
                {
                    entry.source = op.dnorm(entry.sourceFilename, entry.source, daff);
                    entry.sink = op.dnorm(entry.sinkFilename, entry.sink, daff);
                    revisedList.add(entry);
                    break;
                }
            }
            pm.worked(1);
        }

        edges = revisedList;

        pm.done();
    }

    private Interval offset(Interval daff, PrimitiveOp op, PrimitiveOpList primitiveOps)
    {
        int dx = 0, dy = 0;
        for (PrimitiveOp otherOp : primitiveOps)
        {
            if (!otherOp.equals(op))
            {
                dx += otherOp.offset(daff.lb) - daff.lb;
                dy += otherOp.offset(daff.ub) - daff.ub;
            }
        }
        return new Interval(daff.lb + dx, daff.ub + dy);
    }

    public ModelDiff compareAgainst(Model that, IProgressMonitor pm)
    {
        pm = new SubProgressMonitor(pm, 0, SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK);
        pm.beginTask("Differencing " + this.name + " and " + that.name,
            this.edges.size() + that.edges.size());

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
            pm.worked(1);
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
            pm.worked(1);
        }

        pm.done();
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
