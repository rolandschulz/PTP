/*******************************************************************************
 * Copyright (c) 2007 University of Illinois at Urbana-Champaign and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     UIUC - Initial API and implementation
 *******************************************************************************/
package org.eclipse.photran.internal.core.parser;

import org.eclipse.photran.internal.core.lexer.*;                   import org.eclipse.photran.internal.core.analysis.binding.ScopingNode;                   import org.eclipse.photran.internal.core.SyntaxException;                   import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.photran.internal.core.parser.ASTNodeUtil.NonNullIterator;

@SuppressWarnings("all")
public abstract class ASTNodeWithErrorRecoverySymbols extends ASTNode
{
    List<IASTNode> discardedSymbols = null;

    @Override public Iterable<? extends IASTNode> getChildren()
    {
        return new Iterable<IASTNode>()
        {
            public Iterator<IASTNode> iterator()
            {
                return new NonNullIterator<IASTNode>(new Iterator<IASTNode>()
                {
                    private int index = 0;
                    private int numChildren = getNumASTFields();
                    private int numErrorChildren = discardedSymbols == null ? 0 : discardedSymbols.size();

                    public boolean hasNext()
                    {
                        return index < numChildren + numErrorChildren;
                    }

                    public IASTNode next()
                    {
                        if (index < numChildren)
                            return getASTField(index++);
                        else
                            return discardedSymbols.get(index++ - numChildren);
                    }

                    public void remove()
                    {
                        throw new UnsupportedOperationException();
                    }
                });
            }
        };
    }

    public List<IASTNode> getSymbolsDiscardedDuringErrorRecovery()
    {
        return discardedSymbols;
    }

    @Override public Object clone()
    {
            ASTNodeWithErrorRecoverySymbols copy = (ASTNodeWithErrorRecoverySymbols)super.clone();
            copy.discardedSymbols = new ArrayList<IASTNode>(this.discardedSymbols.size());
            for (IASTNode n : this.discardedSymbols)
            {
                if (n == null)
                    copy.discardedSymbols.add(null);
                else
                {
                    IASTNode newChild = (IASTNode)n.clone();
                    newChild.setParent(copy);
                    copy.discardedSymbols.add(newChild);
                }
            }
            return copy;
    }
}
