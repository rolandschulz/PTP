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

import java.util.HashMap;
import java.util.Map;

/**
 * Method object to construct basic blocks from the nodes in a control flow graph and build a new
 * flow graph from those basic blocks.  Used by {@link FlowGraph#formBasicBlocks(BasicBlockBuilder)}.
 * <p>
 * If the underlying language contains unconditional branch instructions or instructions that may
 * halt execution of the program, the default algorithm for constructing basic blocks will be
 * incorrect; in such cases, subclasses must override {@link #isGoToOrStop(Object)} in order to
 * properly recognize these instructions.  (By definition, if control enters a basic block, it
 * must exit the block without halting or branching in the middle of the block.  If the language
 * contains an unconditional branch, and the target of the branch has only one predecessor (the
 * branch instruction), the default algorithm will try to coalesce the target instruction into the
 * basic block containing the branch instruction.  Therefore, subclasses must explicitly determine
 * that the instruction is a branch so that its target will become the leader of a new basic block.)
 * <p>
 * See Aho et al., <i>Compilers: Principles, Techniques, and Tools</i> (1986), pp. 528-529 for more
 * information.
 *
 * @author Jeff Overbey
 *
 * @see FlowGraph#formBasicBlocks(BasicBlockBuilder)
 *
 * @since 3.0
 */
public class BasicBlockBuilder<U>
{
    private FlowGraph<U> cfg;
    private Map<FlowGraphNode<U>, FlowGraphNode<BasicBlock<U>>> newNodes;

    FlowGraph<BasicBlock<U>> buildFlowGraphFrom(FlowGraph<U> cfg)
    {
        this.cfg = cfg;
        this.newNodes = new HashMap<FlowGraphNode<U>, FlowGraphNode<BasicBlock<U>>>();
        constructNewNodes();
        connectNewNodes();
        return new FlowGraph<BasicBlock<U>>(newNodes.get(cfg.getEntryNode()), newNodes.get(cfg.getExitNode()));
    }

    private void constructNewNodes()
    {
        FlowGraphNode<BasicBlock<U>> currentNode = null;
        for (FlowGraphNode<U> node : cfg.nodesInPreOrder())
        {
            if (currentNode == null
                || node.getPrecedessors().size() != 1
                || isGoToOrStop(node.getPrecedessors().get(0).getData()))
                currentNode = new FlowGraphNode<BasicBlock<U>>(node.getName(), new BasicBlock<U>(node.getData()));
            else
                currentNode.getData().add(node.getData());
            newNodes.put(node, currentNode);

            if (node.getSuccessors().size() != 1)
                currentNode = null;
        }
    }

    private void connectNewNodes()
    {
        for (FlowGraphNode<U> node : cfg.nodesInPreOrder())
        {
            FlowGraphNode<BasicBlock<U>> nodeBB = newNodes.get(node);
            for (FlowGraphNode<U> succ : node.getSuccessors())
            {
                FlowGraphNode<BasicBlock<U>> succBB = newNodes.get(succ);
                if (succBB != nodeBB)
                    nodeBB.connectTo(succBB);
            }
        }
    }

    /**
     * @return true iff the given data (instruction/statement) corresponds to either
     * <ul>
     * <li> a conditional or unconditional branch, or
     * <li> a statement that stops execution of the program.
     * </ul>
     * The statement(s)/instructions(s) following this one will become leaders of new basic blocks.
     */
    protected boolean isGoToOrStop(U data)
    {
        return false;
    }
}
