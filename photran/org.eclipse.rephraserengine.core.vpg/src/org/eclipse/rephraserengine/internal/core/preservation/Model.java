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
 * 
 * @since 1.0
 */
public final class Model
{
    private static final class Entry implements Comparable<Entry>
    {
        private int edgeType;
        private String sourceFilename;
        private Interval source, origSource;
        private String sinkFilename;
        private Interval sink;

        public Entry(String sourceFilename, Interval source, String sinkFilename, Interval sink, int edgeType)
        {
            if (source == null || sink == null) throw new IllegalArgumentException();

            this.sourceFilename = sourceFilename;
            this.source = this.origSource = source;
            this.sinkFilename = sinkFilename;
            this.sink = sink;
            this.edgeType = edgeType;
        }

        public boolean shouldPreserveAccordingTo(Set<PreservationRule> preserveEdgeTypes, PrimitiveOpList primitiveOps, boolean initial)
        {
            for (PreservationRule rule : preserveEdgeTypes)
                if (shouldPreserveAccordingTo(rule, primitiveOps, initial))
                    return true;

            return false;
        }

        public boolean shouldPreserveAccordingTo(PreservationRule rule, PrimitiveOpList primitiveOps, boolean initial)
        {
            boolean result = false;
            for (PrimitiveOp op : primitiveOps)
            {
                Interval affected = initial ? op.iaff() : op.daff(primitiveOps);
                boolean incoming = sinkFilename.equals(op.filename) && sink.isSubsetOf(affected);
                boolean outgoing = sourceFilename.equals(op.filename) && source.isSubsetOf(affected);
                if (rule.definitelyShouldNotPreserve(incoming, outgoing, this.edgeType))
                    return false;
                else
                    result = result || rule.shouldPreserve(incoming, outgoing, this.edgeType);
            }
            return result;
        }

        @Override public boolean equals(Object o)
        {
            if (o == null || !o.getClass().equals(this.getClass())) return false;

            Entry that = (Entry)o;
            return this.edgeType == that.edgeType
                && this.sourceFilename.equals(that.sourceFilename)
                && this.source.equals(that.source)
                && this.sinkFilename.equals(that.sinkFilename)
                && this.sink.equals(that.sink);
        }

        @Override public int hashCode()
        {
            return 263 * source.hashCode() + 13 * sink.hashCode() + edgeType;
        }

        public int compareTo(Entry that)
        {
            // Lexicographic comparison
            int result = that.edgeType - this.edgeType;
            if (result == 0)
            {
                result = this.sourceFilename.compareTo(that.sourceFilename);
                if (result == 0)
                {
                    result = this.sinkFilename.compareTo(that.sinkFilename);
                    if (result == 0)
                    {
                        result = this.source.compareTo(that.source);
                        if (result == 0)
                        {
                            result = this.sink.compareTo(that.sink);
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
    private Set<String> filesWithNoEdges;
    private Set<Entry> edges;

    public Model(String name, IProgressMonitor pm, int ticks, EclipseVPG<?,?,?,?,?> vpg, String... filenames)
    {
        this(name, pm, ticks, vpg, Arrays.asList(filenames));
    }

    public Model(String name, IProgressMonitor pm, int ticks, EclipseVPG<?,?,?,?,?> vpg, List<String> filenames)
    {
        this.name = name;
        this.vpg = vpg;

        pm.subTask(Messages.Model_PreparingToCompute + name);
        this.files = vpg.sortFilesAccordingToDependencies(new ArrayList<String>(filenames), new NullProgressMonitor());

        this.filesWithNoEdges = new TreeSet<String>();
        
        pm = new SubProgressMonitor(pm, ticks, SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK);
        pm.beginTask(Messages.bind(Messages.Model_Computing, name), files.size());
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
        int count = 0;
        
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
            
            count++;
        }
        
        if (count == 0)
            filesWithNoEdges.add(filename);
    }

    public List<String> getFiles()
    {
        return files;
    }

    public void inormalize(PrimitiveOpList primitiveOps, Set<PreservationRule> preserveEdgeTypes, IProgressMonitor pm)
    {
        pm = new SubProgressMonitor(pm, 0, SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK);
        pm.beginTask(Messages.bind(Messages.Model_Normalizing, name), edges.size());

        TreeSet<Entry> revisedList = new TreeSet<Entry>();

        for (Entry entry : edges)
        {
            if (entry.shouldPreserveAccordingTo(preserveEdgeTypes, primitiveOps, true))
            {
                entry.source = primitiveOps.inorm(entry.sourceFilename, entry.source);
                entry.sink = primitiveOps.inorm(entry.sinkFilename, entry.sink);
                revisedList.add(entry);
            }
            pm.worked(1);
        }

        edges = revisedList;

        pm.done();
    }

    public void dnormalize(PrimitiveOpList primitiveOps, Set<PreservationRule> preserveEdgeTypes, IProgressMonitor pm)
    {
        pm = new SubProgressMonitor(pm, 0, SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK);
        pm.beginTask(Messages.bind(Messages.Model_Normalizing, name), edges.size());

        TreeSet<Entry> revisedList = new TreeSet<Entry>();

        for (Entry entry : edges)
        {
            if (entry.shouldPreserveAccordingTo(preserveEdgeTypes, primitiveOps, false))
            {
                entry.source = primitiveOps.dnorm(entry.sourceFilename, entry.source);
                entry.sink = primitiveOps.dnorm(entry.sinkFilename, entry.sink);
                revisedList.add(entry);
            }
            pm.worked(1);
        }

        edges = revisedList;

        pm.done();
    }

    public ModelDiff compareAgainst(Model that, IProgressMonitor pm)
    {
        pm = new SubProgressMonitor(pm, 0, SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK);
        pm.beginTask(
            Messages.bind(Messages.Model_Differencing, this.name, that.name),
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
                    if (that.filesWithNoEdges.contains(entry.sourceFilename))
                        diff.recordFileWithNoEdges(entry.sourceFilename);
                    else if (that.filesWithNoEdges.contains(entry.sinkFilename))
                        diff.recordFileWithNoEdges(entry.sinkFilename);
                    else
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

    public String toString(String filename, String fileContents, ArrayList<Integer> lineMap)
    {
        StringBuilder sb = new StringBuilder();

        sb.append(edges.size());
        sb.append(" edges\n\n"); //$NON-NLS-1$

        Set<Integer> edgeTypes = new TreeSet<Integer>();
        for (Entry entry : edges)
            edgeTypes.add(entry.edgeType);

        for (int edgeType : edgeTypes)
        {
            for (Entry entry : edges)
            {
                if (entry.edgeType == edgeType)
                {
                    if (!entry.sourceFilename.equals(filename))
                    {
                        sb.append(entry.sourceFilename);
                        sb.append(':');
                    }
                    sb.append('[');
                    sb.append(String.format("%5d", entry.source.lb)); //$NON-NLS-1$
                    sb.append(", "); //$NON-NLS-1$
                    sb.append(String.format("%5d", entry.source.ub)); //$NON-NLS-1$
                    sb.append(")  ===("); //$NON-NLS-1$
                    sb.append(entry.edgeType);
                    sb.append(")==>  "); //$NON-NLS-1$
                    if (!entry.sinkFilename.equals(filename))
                    {
                        sb.append(entry.sinkFilename);
                        sb.append(':');
                    }
                    sb.append('[');
                    sb.append(String.format("%5d", entry.sink.lb)); //$NON-NLS-1$
                    sb.append(", "); //$NON-NLS-1$
                    sb.append(String.format("%5d", entry.sink.ub)); //$NON-NLS-1$
                    sb.append(")"); //$NON-NLS-1$
        
                    if (fileContents != null)
                    {
                        sb.append("           ["); //$NON-NLS-1$
                        if (!entry.sourceFilename.equals(filename))
                            sb.append('?');
                        else if (entry.source.lb >= 0 && entry.source.ub >= entry.source.lb)
                            sb.append(extractText(fileContents, entry.source));
                        sb.append("]"); //$NON-NLS-1$
                        if (lineMap != null)
                        {
                            sb.append(" (Line "); //$NON-NLS-1$
                            sb.append(getLine(entry.source.lb, lineMap));
                            sb.append(")"); //$NON-NLS-1$
                        }
                        sb.append("  ===("); //$NON-NLS-1$
                        sb.append(vpg.describeEdgeType(entry.edgeType));
                        sb.append(")==>  ["); //$NON-NLS-1$
                        if (!entry.sinkFilename.equals(filename))
                            sb.append('?');
                        else if (entry.sink.lb >= 0 && entry.sink.ub >= entry.sink.lb)
                            sb.append(extractText(fileContents, entry.sink));
                        sb.append("]"); //$NON-NLS-1$
                        if (lineMap != null)
                        {
                            sb.append(" (Line "); //$NON-NLS-1$
                            sb.append(getLine(entry.sink.lb, lineMap));
                            sb.append(")"); //$NON-NLS-1$
                        }
                    }
    
                    sb.append('\n');
                }
            }
        }
        return sb.toString();
    }

    private String extractText(String fileContents, Interval interval)
    {
        String result = fileContents.substring(interval.lb, interval.ub);
        result = result.replace("\t", "\\t"); //$NON-NLS-1$ //$NON-NLS-2$
        result = result.replace("\r", "\\r"); //$NON-NLS-1$ //$NON-NLS-2$
        result = result.replace("\n", "\\n"); //$NON-NLS-1$ //$NON-NLS-2$
        if (result.length() > 17)
            result = result.substring(0, 18) + "..."; //$NON-NLS-1$
        return result;
    }

    private int getLine(int offset, ArrayList<Integer> lineMap)
    {
        for (int i = 0; i < lineMap.size(); i++)
            if (offset < lineMap.get(i))
                return i+1;
        return lineMap.size();
    }
}
