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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.rephraserengine.core.vpg.TokenRef;
import org.eclipse.rephraserengine.core.vpg.VPG;
import org.eclipse.rephraserengine.core.vpg.VPGEdge;
import org.eclipse.rephraserengine.core.vpg.eclipse.EclipseVPG;

/**
 * A mutable, in-memory copy of the edges in part of a program graph.
 *
 * @author Jeff Overbey
 * 
 * @since 3.0
 */
final class Model
{
    private String name;
    private EclipseVPG<?,?,?,?,?> vpg;
    private List<String> files;
    private Set<String> filesWithNoEdges;
    private List<VPGEdge<?,?,?>> edges;

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
        this.edges = new ArrayList<VPGEdge<?,?,?>>();
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
            edges.add(edge);
            count++;
        }

        if (count == 0)
            filesWithNoEdges.add(filename);
    }

    List<String> getFiles()
    {
        return files;
    }

    void inormalize(ReplacementList replacements, IProgressMonitor pm)
    {
        pm = new SubProgressMonitor(pm, 0, SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK);
        pm.beginTask(Messages.bind(Messages.Model_Normalizing, name), edges.size());

        // Use a set to eliminate duplicates
        TreeSet<VPGEdge<?,?,?>> newEdges = new TreeSet<VPGEdge<?,?,?>>();
        
        for (int i = 0; i < edges.size(); i++)
        {
            newEdges.add(edges.get(i).projectInitial(replacements));
            pm.worked(1);
        }
        
        edges.clear();
        edges.addAll(newEdges);
        
        pm.done();
    }

    void dnormalize(ReplacementList replacements, IProgressMonitor pm)
    {
        pm = new SubProgressMonitor(pm, 0, SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK);
        pm.beginTask(Messages.bind(Messages.Model_Normalizing, name), edges.size());

        // Use a set to eliminate duplicates
        TreeSet<VPGEdge<?,?,?>> newEdges = new TreeSet<VPGEdge<?,?,?>>();
        
        for (int i = 0; i < edges.size(); i++)
        {
            newEdges.add(edges.get(i).projectFinal(replacements));
            pm.worked(1);
        }
        
        edges.clear();
        edges.addAll(newEdges);

        pm.done();
    }

    ModelDiff checkPreservation(Model that, PreservationRuleset ruleset, IProgressMonitor pm)
    {
        pm = new SubProgressMonitor(pm, 0, SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK);
        pm.beginTask(
            Messages.bind(Messages.Model_Differencing, this.name, that.name),
            this.edges.size() + that.edges.size());

        ModelDiff diff = new ModelDiff();

        int type = 0;
        PreservationAnalyzer analyzer = new PreservationAnalyzer(this.edges, that.edges, ruleset);
        while (analyzer.hasEdgesRemaining())
        {
            int edgesRemainingBefore = analyzer.countEdgesRemaining();
            
            for (VPGEdge.Classification classification : VPGEdge.Classification.values())
                analyzer.checkPreservation(type, classification, diff);
            analyzer.ensureNoEdgesRemaining(type);
            type++;
            
            pm.worked(analyzer.countEdgesRemaining() - edgesRemainingBefore);
        }

        pm.done();
        return diff;
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
        for (VPGEdge<?,?,?> entry : edges)
            edgeTypes.add(entry.getType());

        for (int edgeType : edgeTypes)
        {
            for (VPGEdge<?,?,?> entry : edges)
            {
                if (entry.getType() == edgeType)
                {
                    if (entry.getSource() == null)
                    {
                        sb.append("(null"); //$NON-NLS-1$
                    }
                    else
                    {
                        if (!entry.getSource().getFilename().equals(filename))
                        {
                            sb.append(entry.getSource().getFilename());
                            sb.append(':');
                        }
                        sb.append('[');
                        sb.append(String.format("%5d", entry.getSource().getOffset())); //$NON-NLS-1$
                        sb.append(", "); //$NON-NLS-1$
                        sb.append(String.format("%5d", entry.getSource().getEndOffset())); //$NON-NLS-1$
                    }
                    sb.append(")  ===("); //$NON-NLS-1$
                    sb.append(entry.getType());
                    sb.append(")==>  "); //$NON-NLS-1$
                    if (entry.getSink() == null)
                    {
                        sb.append("(null"); //$NON-NLS-1$
                    }
                    else
                    {
                        if (!entry.getSink().getFilename().equals(filename))
                        {
                            sb.append(entry.getSink().getFilename());
                            sb.append(':');
                        }
                        sb.append('[');
                        sb.append(String.format("%5d", entry.getSink().getOffset())); //$NON-NLS-1$
                        sb.append(", "); //$NON-NLS-1$
                        sb.append(String.format("%5d", entry.getSink().getEndOffset())); //$NON-NLS-1$
                    }
                    sb.append(")"); //$NON-NLS-1$

                    if (fileContents != null)
                    {
                        sb.append("           ["); //$NON-NLS-1$
                        if (entry.getSource() == null || !entry.getSource().getFilename().equals(filename))
                            sb.append('?');
                        else if (entry.getSource().getOffset() >= 0 && entry.getSource().getEndOffset() >= entry.getSource().getOffset())
                            sb.append(extractText(fileContents, entry.getSource()));
                        sb.append("]"); //$NON-NLS-1$
                        if (lineMap != null)
                        {
                            sb.append(" (Line "); //$NON-NLS-1$
                            sb.append(getLine(entry.getSource().getOffset(), lineMap));
                            sb.append(")"); //$NON-NLS-1$
                        }
                        sb.append("  ===("); //$NON-NLS-1$
                        sb.append(vpg.describeEdgeType(entry.getType()));
                        sb.append(")==>  ["); //$NON-NLS-1$
                        if (entry.getSink() == null || !entry.getSink().getFilename().equals(filename))
                            sb.append('?');
                        else if (entry.getSink().getOffset() >= 0 && entry.getSink().getEndOffset() >= entry.getSink().getOffset())
                            sb.append(extractText(fileContents, entry.getSink()));
                        sb.append("]"); //$NON-NLS-1$
                        if (lineMap != null)
                        {
                            sb.append(" (Line "); //$NON-NLS-1$
                            sb.append(getLine(entry.getSink().getOffset(), lineMap));
                            sb.append(")"); //$NON-NLS-1$
                        }
                    }

                    sb.append('\n');
                }
            }
        }
        return sb.toString();
    }

    private String extractText(String fileContents, TokenRef<?> tokenRef)
    {
        String result = fileContents.substring(tokenRef.getOffset(), tokenRef.getEndOffset());
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
