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

/**
 * A control flow graph.
 *
 * @author Jeff Overbey
 *
 * @param U type of data associated with each flowgraph node
 *
 * @see org.eclipse.rephraserengine.core.analysis.flow.VPGFlowGraph
 *
 * @since 3.0
 */
public class FlowGraph<U>
{
    protected FlowGraphNode<U> entryNode, exitNode;

    public FlowGraph(FlowGraphNode<U> entryNode, FlowGraphNode<U> exitNode)
    {
        this.entryNode = entryNode;
        this.exitNode = exitNode;
    }

    protected FlowGraph()
    {
        this.entryNode = null; // Subclass must
        this.exitNode = null;  // set these
    }

    public FlowGraphNode<U> getEntryNode()
    {
        return this.entryNode;
    }

    public FlowGraphNode<U> getExitNode()
    {
        return this.exitNode;
    }

    public Iterable<U> dataInReversePostOrder()
    {
        return entryNode.subgraphDataInReversePostOrder();
    }

    public Iterable<FlowGraphNode<U>> nodesInReversePostOrder()
    {
        return entryNode.subgraphInReversePostOrder();
    }

    public Iterable<U> dataInPreOrder()
    {
        return entryNode.subgraphDataInPreOrder();
    }

    public Iterable<FlowGraphNode<U>> nodesInPreOrder()
    {
        return entryNode.subgraphInPreOrder();
    }

    public FlowGraph<BasicBlock<U>> formBasicBlocks()
    {
        return new BasicBlockBuilder<U>().buildFlowGraphFrom(this);
    }

    public FlowGraph<BasicBlock<U>> formBasicBlocks(BasicBlockBuilder<U> builder)
    {
        return builder.buildFlowGraphFrom(this);
    }

    int getMaxNameLength()
    {
        int nameLen = 0;
        for (FlowGraphNode<U> node : this.nodesInPreOrder())
            nameLen = Math.max(nameLen, node.getName().length());
        return nameLen;
    }

    @Override public String toString()
    {
        StringBuilder sb = new StringBuilder();

        int nameLen = getMaxNameLength();

        String spaces = spaces(nameLen);
        for (FlowGraphNode<U> node : this.nodesInPreOrder())
        {
            sb.append(String.format("%" + nameLen + "s: %s\n", //$NON-NLS-1$ //$NON-NLS-2$
                node.getName(),
                nodeDataAsString(node).replace("\n", "\n" + spaces))); //$NON-NLS-1$ //$NON-NLS-2$

            for (FlowGraphNode<U> succ : node.getSuccessors())
                sb.append(String.format("    => %s\n", succ.getName())); //$NON-NLS-1$
        }
        if (sb.length() > 0) sb.deleteCharAt(sb.length()-1);
        return sb.toString();
    }

    /**
     * @param node
     * @return
     */
    protected String nodeDataAsString(FlowGraphNode<U> node)
    {
        return String.valueOf(node.getData());
    }

    private String spaces(int count)
    {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++)
            sb.append(' ');
        return sb.toString();
    }
}
