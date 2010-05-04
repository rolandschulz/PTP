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

@SuppressWarnings("all")
public class ASTNodePair<T extends IASTNode, U extends IASTNode> extends ASTNode
{
    public final T first;
    public final U second;

    public ASTNodePair(T first, U second)
    {
        assert second != null;

        this.first = first;
        this.second = second;
    }

    @Override protected int getNumASTFields()
    {
        return first == null ? 1 : 2;
    }

    @Override protected IASTNode getASTField(int index)
    {
        if (index == 0)
        {
            return first != null ? first : second;
        }
        else if (index == 1 && first != null)
        {
            return second;
        }
        else throw new IllegalArgumentException();
    }

    @Override protected void setASTField(int index, IASTNode newNode)
    {
        throw new UnsupportedOperationException();
    }

    @Override public void accept(IASTVisitor visitor)
    {
        if (first != null) first.accept(visitor);
        second.accept(visitor);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Source Manipulation
    ///////////////////////////////////////////////////////////////////////////

    public void replaceChild(IASTNode node, IASTNode withNode)
    {
        throw new UnsupportedOperationException();
    }

    public void removeFromTree()
    {
        throw new UnsupportedOperationException();
    }

    public void replaceWith(IASTNode newNode)
    {
        throw new UnsupportedOperationException();
    }

    public void replaceWith(String literalString)
    {
        throw new UnsupportedOperationException();
    }
}
