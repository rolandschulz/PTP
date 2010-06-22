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
package org.eclipse.rephraserengine.internal.core.tests.flow;

import java.util.BitSet;

import junit.framework.TestCase;

import org.eclipse.rephraserengine.core.analysis.flow.FlowGraph;
import org.eclipse.rephraserengine.core.analysis.flow.FlowGraphNode;
import org.eclipse.rephraserengine.core.analysis.flow.ReachingDefsAnalysis;

/**
 * Unit tests for {@link ReachingDefsAnalysis}.
 *
 * @author Jeff Overbey
 */
public class ReachingDefsTest extends TestCase
{
    private static class RDAnalysis extends ReachingDefsAnalysis<Integer>
    {
        public RDAnalysis(FlowGraph<Integer> cfg)
        {
            super(cfg, countDefs(cfg));
        }

        private static int countDefs(FlowGraph<Integer> cfg)
        {
            int numDefs = 0;
            for (Integer n : cfg.dataInReversePostOrder())
                numDefs = Math.max(numDefs, n);
            return numDefs;
        }

        @Override protected BitSet gen(Integer node)
        {
            BitSet result = new BitSet(numDefs);
            if (node > 0)
                result.set(node);
            return result;
        }

        @Override protected BitSet kill(Integer node)
        {
            BitSet result = new BitSet(numDefs);
            if (node > 0)
            {
                result.set(node);
                result.flip(0, numDefs);
            }
            return result;
        }
    }

    public void test1()
    {
        /*                    CFG Node    Def/Use
         *                    ========    =======
         * read a               N1        D1
         * read a               N2        D2
         * print a              N3             U1
         */

        //                            Def
        FlowGraphNode<Integer> n1 = new FlowGraphNode<Integer>("n1", 1),
                n2 = new FlowGraphNode<Integer>("n2", 2),
                n3 = new FlowGraphNode<Integer>("n3", 0);

        FlowGraph<Integer> cfg = new FlowGraph<Integer>(
            new FlowGraphNode<Integer>("entry", 0),
            new FlowGraphNode<Integer>("exit", 0));
        cfg.getEntryNode().connectTo(n1);
        n1.connectTo(n2);
        n2.connectTo(n3);
        n3.connectTo(cfg.getExitNode());

        RDAnalysis analysis = new RDAnalysis(cfg);
        analysis.run();
        //System.out.println(analysis);
        assertEquals("{}", analysis.defsReaching(cfg.getEntryNode()).toString());
        assertEquals("{}", analysis.defsReaching(n1).toString());
        assertEquals("{1}", analysis.defsReaching(n2).toString());
        assertEquals("{2}", analysis.defsReaching(n3).toString());
        assertEquals("{2}", analysis.defsReaching(cfg.getExitNode()).toString());
    }

    public void test2()
    {
        /*                    CFG Node    Def/Use
         *                    ========    =======
         * read a               N1        D1
         * while (a < 10)       N2             U1
         *   a *= 2               N3      D2   U2
         * if (a == 11)         N4             U3
         *   a = 999              N5      D3
         * print a              N6             U4
         */

        //                            Def
        FlowGraphNode<Integer> n1 = new FlowGraphNode<Integer>("n1", 1),
                n2 = new FlowGraphNode<Integer>("n2", 0),
                n3 = new FlowGraphNode<Integer>("n3", 2),
                n4 = new FlowGraphNode<Integer>("n4", 0),
                n5 = new FlowGraphNode<Integer>("n5", 3),
                n6 = new FlowGraphNode<Integer>("n6", 0);

        FlowGraph<Integer> cfg = new FlowGraph<Integer>(
            new FlowGraphNode<Integer>("entry", 0),
            new FlowGraphNode<Integer>("exit", 0));
        cfg.getEntryNode().connectTo(n1);
        n1.connectTo(n2);
        n2.connectTo(n3); n2.connectTo(n4);
        n3.connectTo(n2); n3.connectTo(n4);
        n4.connectTo(n5); n4.connectTo(n6);
        n5.connectTo(n6);
        n6.connectTo(cfg.getExitNode());

        RDAnalysis analysis = new RDAnalysis(cfg);
        analysis.run();
        //System.out.println(analysis);
        assertEquals("{}", analysis.defsReaching(cfg.getEntryNode()).toString());
        assertEquals("{}", analysis.defsReaching(n1).toString());
        assertEquals("{1, 2}", analysis.defsReaching(n2).toString());
        assertEquals("{1, 2}", analysis.defsReaching(n3).toString());
        assertEquals("{1, 2}", analysis.defsReaching(n4).toString());
        assertEquals("{1, 2}", analysis.defsReaching(n5).toString());
        assertEquals("{1, 2, 3}", analysis.defsReaching(n6).toString());
        assertEquals("{1, 2, 3}", analysis.defsReaching(cfg.getExitNode()).toString());
    }
}
