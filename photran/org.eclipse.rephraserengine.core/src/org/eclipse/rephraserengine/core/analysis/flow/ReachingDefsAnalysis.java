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
package org.eclipse.rephraserengine.core.analysis.flow;

import java.util.BitSet;
import java.util.HashMap;

/**
 * Reaching definitions data flow analysis.
 *
 * @author Jeff Overbey
 *
 * @param T type of nodes in the control flow graph
 *
 * @since 3.0
 */
public abstract class ReachingDefsAnalysis<T>
{
    protected final FlowGraph<T> flowGraph;
    protected final int numDefs;

    private HashMap<FlowGraphNode<T>, BitSet> in;

    /**
     *
     * @param flowGraph
     * @param numDefs
     */
    public ReachingDefsAnalysis(FlowGraph<T> flowGraph, int numDefs)
    {
        this.flowGraph = flowGraph;
        this.numDefs = numDefs;
        this.in = new HashMap<FlowGraphNode<T>, BitSet>();
    }

    /**
     *
     */
    public void run()
    {
        boolean changed;
        do
        {
            changed = false;
            for (FlowGraphNode<T> node : flowGraph.nodesInReversePostOrder())
            {
                //           \  /
                // in(i) =    \/          gen(j) or ( in(j) and not kill(j) )
                //        j in preds(i)
                BitSet newIn = new BitSet(numDefs);
                for (FlowGraphNode<T> j : node.getPrecedessors())
                {
                    BitSet out = new BitSet(numDefs);
                    out.or(defsReaching(j));
                    out.andNot(kill(j.getData()));

                    out.or(gen(j.getData()));

                    newIn.or(out);
                }

                BitSet defs = defsReaching(node);
                if (!newIn.equals(defs))
                {
                    in.put(node, newIn);
                    changed = true;
                }
            }
        }
        while (changed);
    }

    /**
     *
     * @param node
     * @return
     */
    public BitSet defsReaching(FlowGraphNode<T> node)
    {
        if (in.containsKey(node))
            return in.get(node);
        else
            return new BitSet(numDefs);
    }

    /**
     * @param node
     * @return
     */
    protected abstract BitSet gen(T node);

    /**
     * @param node
     * @return
     */
    protected abstract BitSet kill(T node);

    @Override public String toString()
    {
        int nameLen = flowGraph.getMaxNameLength();

        StringBuilder sb = new StringBuilder();
        for (FlowGraphNode<T> node : flowGraph.nodesInPreOrder())
            sb.append(String.format("%" + nameLen + "s: %s\n", //$NON-NLS-1$ //$NON-NLS-2$
                node.getName(),
                defsReaching(node).toString()));
        return sb.toString();
    }
}
