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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A basic block in a control flow graph.
 * <p>
 * A basic block is a sequence of instructions such that, if the first instruction is executed,
 * all of the subsequent instructions must be executed as well.  I.e., if control enters a basic
 * block, it must exit the block without halting or branching in the middle of the block.  A
 * basic block has a single entrypoint (the first instruction, or <i>leader</i>0 and a single
 * exit point (after the last instruction).  Note that the last instruction may be a branch:
 * Control exits the basic block at exactly one point (after the last instruction), but it may
 * flow to any number of other basic blocks.
 * <p>
 * This class implements {@link Iterable}, so the instructions in this basic block can be iterated
 * through using a new-style for-loop.
 *
 * <p>
 * See Aho et al., <i>Compilers: Principles, Techniques, and Tools</i> (1986), pp. 528-529 for more
 * information.
 *
 * @author Jeff Overbey
 *
 * @see FlowGraph#formBasicBlocks()
 * @see FlowGraph#formBasicBlocks(BasicBlockBuilder)
 *
 * @since 3.0
 */
public class BasicBlock<T> implements Iterable<T>
{
    protected final List<T> instructions;

    BasicBlock(T leader)
    {
        this.instructions = new ArrayList<T>(4);
        add(leader);
    }

    void add(T insn)
    {
        instructions.add(insn);
    }

    public T getLeader()
    {
        return instructions.get(0);
    }

    public Iterator<T> iterator()
    {
        return instructions.iterator();
    }

    @Override public String toString()
    {
        StringBuilder sb = new StringBuilder();
        for (T insn : this)
            sb.append(insn.toString());
        return sb.toString();
    }
}
