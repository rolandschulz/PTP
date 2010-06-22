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

import junit.framework.TestCase;

import org.eclipse.rephraserengine.core.analysis.flow.BasicBlock;
import org.eclipse.rephraserengine.core.analysis.flow.BasicBlockBuilder;
import org.eclipse.rephraserengine.core.analysis.flow.FlowGraph;
import org.eclipse.rephraserengine.core.analysis.flow.FlowGraphNode;

/**
 * Unit tests for {@link FlowGraph} and {@link FlowGraphNode}.
 *
 * @author Jeff Overbey
 */
public class FlowGraphTests extends TestCase
{
    /**
     * <pre>
     *   (1)
     *    |
     *   (2)
     *   / \
     * (3)  (4)
     *  |    |
     * (5)  (6)
     *  \   /|\
     *   \ / | \
     *   (7)(8)(9)
     * </pre>
     */
    @SuppressWarnings("unchecked")
    public void testTraversalAndBasicBlocks()
    {
        FlowGraphNode<Integer>
            n1 = new FlowGraphNode<Integer>("n1", 1),
            n2 = new FlowGraphNode<Integer>("n2", 2),
            n3 = new FlowGraphNode<Integer>("n3", 3),
            n4 = new FlowGraphNode<Integer>("n4", 4),
            n5 = new FlowGraphNode<Integer>("n5", 5),
            n6 = new FlowGraphNode<Integer>("n6", 6),
            n7 = new FlowGraphNode<Integer>("n7", 7),
            n8 = new FlowGraphNode<Integer>("n8", 8),
            n9 = new FlowGraphNode<Integer>("n9", 9);
        n1.connectTo(n2);
        n2.connectTo(n3, n4);
        n3.connectTo(n5);
        n4.connectTo(n6);
        n5.connectTo(n7);
        n6.connectTo(n7, n8, n9);

        assertEquals("n1", n1.getName());
        assertEquals("n9", n9.getName());

        StringBuilder sb = new StringBuilder();
        for (FlowGraphNode<Integer> n : n6.getSuccessors())
            sb.append(n.getData());
        assertEquals("789", sb.toString());

        sb = new StringBuilder();
        for (FlowGraphNode<Integer> n : n6.getSuccessorsInReverse())
            sb.append(n.getData());
        assertEquals("987", sb.toString());

        sb = new StringBuilder();
        for (FlowGraphNode<Integer> n : n6.getPrecedessors())
            sb.append(n.getData());
        assertEquals("4", sb.toString());

        sb = new StringBuilder();
        for (FlowGraphNode<Integer> n : n7.getPrecedessors())
            sb.append(n.getData());
        assertEquals("56", sb.toString());

        FlowGraph<Integer> cfg = new FlowGraph<Integer>(n1, null);

        sb = new StringBuilder();
        for (Integer n : cfg.dataInReversePostOrder())
            sb.append(n);
        assertEquals("987645321", sb.toString());

        sb = new StringBuilder();
        for (Integer n : cfg.dataInPreOrder())
            sb.append(n);
        assertEquals("123574689", sb.toString());

        FlowGraph<BasicBlock<Integer>> bbCfg = new FlowGraph<Integer>(n1, null).formBasicBlocks();
        assertEquals(
            "n1: 12\n" +
            "    => n3\n" +
            "    => n4\n" +
            "n3: 35\n" +
            "    => n7\n" +
            "n7: 7\n" +
            "n4: 46\n" +
            "    => n7\n" +
            "    => n8\n" +
            "    => n9\n" +
            "n8: 8\n" +
            "n9: 9",
            bbCfg.toString());
    }

    /**
     * <pre>
     * (1)
     *  |
     * (2)
     *  |
     * (3) <-- goto
     *  |
     * (4)
     * </pre>
     */
    public void testBasicBlocksGoto()
    {
        FlowGraphNode<Integer>
            n1 = new FlowGraphNode<Integer>("n1", 1),
            n2 = new FlowGraphNode<Integer>("n2", 2),
            n3 = new FlowGraphNode<Integer>("n3", 3),
            n4 = new FlowGraphNode<Integer>("n4", 4);
        n1.connectTo(n2);
        n2.connectTo(n3);
        n3.connectTo(n4);

        BasicBlockBuilder<Integer> builder = new BasicBlockBuilder<Integer>()
        {
            @Override
            protected boolean isGoToOrStop(Integer data)
            {
                return data.intValue() == 3;
            }
        };

        FlowGraph<BasicBlock<Integer>> bbFlowGraph = new FlowGraph<Integer>(n1, null).formBasicBlocks(builder);
        assertEquals(
            "n1: 123\n" +
        	"    => n4\n" +
        	"n4: 4",
        	bbFlowGraph.toString());
    }
}
