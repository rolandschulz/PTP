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
import java.io.PrintStream;
import java.util.Iterator;
import java.util.List;

import org.eclipse.photran.internal.core.parser.ASTNodeUtil.NonNullIterator;

@SuppressWarnings("all")
public abstract class ASTNode implements IASTNode
{
    private IASTNode parent = null;

    ///////////////////////////////////////////////////////////////////////////
    // Traversal and Visitor Support
    ///////////////////////////////////////////////////////////////////////////

    public IASTNode getParent()
    {
        return this.parent;
    }

    public void setParent(IASTNode parent)
    {
        this.parent = parent;
    }

    public Iterable<? extends IASTNode> getChildren()
    {
        return new Iterable<IASTNode>()
        {
            public Iterator<IASTNode> iterator()
            {
                return new NonNullIterator<IASTNode>(new Iterator<IASTNode>()
                {
                    private int index = 0, numChildren = getNumASTFields();

                    public boolean hasNext()
                    {
                        return index < numChildren;
                    }

                    public IASTNode next()
                    {
                        return getASTField(index++);
                    }

                    public void remove()
                    {
                        throw new UnsupportedOperationException();
                    }
                });
            }
        };
    }

    protected abstract int getNumASTFields();

    protected abstract IASTNode getASTField(int index);

    protected abstract void setASTField(int index, IASTNode value);

    public abstract void accept(IASTVisitor visitor);

    ///////////////////////////////////////////////////////////////////////////
    // Source Manipulation
    ///////////////////////////////////////////////////////////////////////////

    public void replaceChild(IASTNode node, IASTNode withNode)
    {
        for (int i = 0; i < getNumASTFields(); i++)
        {
            if (getASTField(i) == node)
            {
                setASTField(i, withNode);
                if (withNode != null) withNode.setParent(this);
                // if (node != null) node.setParent(null);
                return;
            }
        }

        throw new IllegalStateException("Child node not found");
    }

    public void removeFromTree()
    {
        ASTNodeUtil.removeFromTree(this);
    }

    public void replaceWith(IASTNode newNode)
    {
        ASTNodeUtil.replaceWith(this, newNode);
    }

    public void replaceWith(String literalString)
    {
        ASTNodeUtil.replaceWith(this, literalString);
    }

    @Override public Object clone()
    {
        try
        {
            ASTNode copy = (ASTNode)super.clone();
            for (int i = 0; i < getNumASTFields(); i++)
            {
                if (getASTField(i) != null)
                {
                    IASTNode newChild = (IASTNode)getASTField(i).clone();
                    newChild.setParent(copy);
                    copy.setASTField(i, newChild);
                }
            }
            return copy;
        }
        catch (CloneNotSupportedException e)
        {
            throw new Error(e);
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Searching
    ///////////////////////////////////////////////////////////////////////////

    public <T extends IASTNode> T findNearestAncestor(Class<T> targetClass)
    {
        return ASTNodeUtil.findNearestAncestor(this, targetClass);
    }

    public <T extends IASTNode> T findFirst(Class<T> targetClass)
    {
        return ASTNodeUtil.findFirst(this, targetClass);
    }

    public <T extends IASTNode> T findLast(Class<T> targetClass)
    {
        return ASTNodeUtil.findLast(this, targetClass);
    }

    public Token findFirstToken()
    {
        return ASTNodeUtil.findFirstToken(this);
    }

    public Token findLastToken()
    {
        return ASTNodeUtil.findLastToken(this);
    }

    public boolean isFirstChildInList()
    {
        return ASTNodeUtil.isFirstChildInList(this);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Source Reproduction
    ///////////////////////////////////////////////////////////////////////////

    public IPreprocessorReplacement printOn(PrintStream out, IPreprocessorReplacement currentPreprocessorDirective)
    {
        return ASTNodeUtil.print(this, currentPreprocessorDirective, out);
    }

    @Override public String toString()
    {
        return ASTNodeUtil.toString(this);
    }
}
