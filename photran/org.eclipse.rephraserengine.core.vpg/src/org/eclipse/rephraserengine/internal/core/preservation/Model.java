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
import java.util.List;
import java.util.TreeSet;

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
        private Interval source;
        private Interval sink;
        private int edgeType;

        public Entry(Interval source, Interval sink, int edgeType)
        {
            if (source == null || sink == null) throw new IllegalArgumentException();

            this.source = source;
            this.sink = sink;
            this.edgeType = edgeType;
        }

        @Override public boolean equals(Object o)
        {
            if (!o.getClass().equals(this.getClass())) return false;

            Entry that = (Entry)o;
            return this.source.equals(that.source)
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
            int result = this.source.compareTo(that.source);
            if (result == 0)
            {
                result = this.sink.compareTo(that.sink);
                if (result == 0)
                {
                    return that.edgeType - this.edgeType;
                }
            }
            return result;
        }
    }

    private EclipseVPG<?,?,?,?,?> vpg;
    private String filename;
    private TreeSet<Entry> edgeList = new TreeSet<Entry>();

    public Model(EclipseVPG<?,?,?,?,?> vpg, String filename)
    {
        this.filename = filename;
        this.vpg = vpg;

        // TODO: Ignores filenames
        for (VPGEdge<?,?,?> edge : vpg.db.getAllEdgesFor(filename))
        {
            TokenRef<?> source = edge.getSource();
            TokenRef<?> sink = edge.getSink();

            edgeList.add(
                new Entry(
                    new Interval(source.getOffset(), source.getEndOffset()),
                    new Interval(sink.getOffset(), sink.getEndOffset()),
                    edge.getType()));
        }
    }

    public void inormalize(PrimitiveOp op)
    {
        for (Entry entry : edgeList)
        {
            entry.source = op.inorm(entry.source);
            entry.sink = op.inorm(entry.sink);
        }
    }

    public void inormalize(List<PrimitiveOp> primitiveOps)
    {
        for (PrimitiveOp op : primitiveOps)
            inormalize(op);
    }

    public void dnormalize(PrimitiveOp op)
    {
        for (Entry entry : edgeList)
        {
            entry.source = op.dnorm(entry.source);
            entry.sink = op.dnorm(entry.sink);
        }
    }

    public void dnormalize(List<PrimitiveOp> primitiveOps)
    {
        for (PrimitiveOp op : primitiveOps)
            dnormalize(op);
    }

    public ModelDiff compareAgainst(Model that)
    {
        ModelDiff diff = new ModelDiff();

        for (Entry entry : this.edgeList)
        {
            if (!that.edgeList.contains(entry))
            {
                Interval newSink = findNewSink(entry, that.edgeList);
                if (newSink != null)
                    diff.add(new EdgeSinkChanged(entry.source, entry.sink, newSink, entry.edgeType));
                else
                    diff.add(new EdgeDeleted(entry.source, entry.sink, entry.edgeType));
            }
        }

        for (Entry entry : that.edgeList)
        {
            if (!this.edgeList.contains(entry))
            {
                diff.add(new EdgeAdded(entry.source, entry.sink, entry.edgeType));
            }
        }

        return diff;
    }

    private Interval findNewSink(Entry entry, TreeSet<Entry> otherEdgeList)
    {
        for (Entry otherEntry : otherEdgeList)
            if (otherEntry.source.equals(entry.source) && otherEntry.edgeType == entry.edgeType)
                return otherEntry.sink;

        return null;
    }

    @Override public String toString()
    {
        return toString(null, null);
    }

    public String toString(CharSequence fileContents, ArrayList<Integer> lineMap)
    {
        StringBuilder sb = new StringBuilder();

        sb.append(edgeList.size());
        sb.append(" edges\n\n");

        for (Entry entry : edgeList)
        {
            sb.append('[');
            sb.append(String.format("%5d", entry.source.lb));
            sb.append(", ");
            sb.append(String.format("%5d", entry.source.ub));
            sb.append(")  ===(");
            sb.append(entry.edgeType);
            sb.append(")==>  [");
            sb.append(String.format("%5d", entry.sink.lb));
            sb.append(", ");
            sb.append(String.format("%5d", entry.sink.ub));
            sb.append(")");

            if (fileContents != null)
            {
                sb.append("           [");
                if (entry.source.lb >= 0 && entry.source.ub >= entry.source.lb)
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
                if (entry.sink.lb >= 0 && entry.sink.ub >= entry.sink.lb)
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
