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
import java.util.ArrayList;
import java.util.AbstractList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.photran.internal.core.parser.ASTNodeUtil.NonNullIterator;

@SuppressWarnings("all")
public class ASTSeparatedListNode<T extends IASTNode> extends AbstractList<T> implements IASTListNode<T>
{
    private IASTNode parent = null;

    private ArrayList<Token> separators = new ArrayList<Token>();
    private ArrayList<T> entries = new ArrayList<T>();

    public ASTSeparatedListNode() {}

    public ASTSeparatedListNode(Token separator, List<T> entries)
    {
        this(separator, entries, false);
    }

    @SuppressWarnings("unchecked")
    public ASTSeparatedListNode(Token separator, List<T> entries, boolean trimWhiteText)
    {
        boolean first = true;
        Iterator<T> it = entries.iterator();
        while (it.hasNext())
        {
            T entry = it.next();
            if (trimWhiteText && entry != null && entry instanceof Token)
            {
                Token tok = (Token)((Token)entry).clone();
                tok.setWhiteBefore("");
                tok.setWhiteAfter("");
                entry = (T)tok;
            }

            if (first)
            {
                add(null, entry);
                first = false;
            }
            else
            {
                add(separator, entry);
            }
        }
    }

    public void add(Token separator, T entry)
    {
        this.separators.add(separator);
        this.entries.add(entry);
        if (separator != null) separator.setParent(this);
        if (entry != null) entry.setParent(this);
    }

    @Override public T remove(int index)
    {
        Token separator = this.separators.remove(index);
        T result = this.entries.remove(index);
        if (index == 0 && separator == null) this.separators.set(0, null);
        // if (separator != null) separator.setParent(null);
        // if (result != null) result.setParent(null);
        return result;
    }

    public ASTNodePair<Token, T> getPair(int index)
    {
        return new ASTNodePair<Token, T>(this.separators.get(index), this.entries.get(index));
    }

    ///////////////////////////////////////////////////////////////////////////
    // IASTListNode Insertion Methods
    ///////////////////////////////////////////////////////////////////////////

    public void insertBefore(T insertBefore, T newElement)
    {
        throw new UnsupportedOperationException();
    }

    public void insertAfter(T insertAfter, T newElement)
    {
        throw new UnsupportedOperationException();
    }

    ///////////////////////////////////////////////////////////////////////////
    // AbstractList Implementation
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public T get(int index)
    {
        return entries.get(index);
    }

    @Override
    public int size()
    {
        return entries.size();
    }

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
                return new NonNullIterator(new Iterator<IASTNode>()
                {
                    private int index = !separators.isEmpty() && separators.get(0) == null ? 1 : 0;
                    private int count = entries.size() * 2;

                    public boolean hasNext()
                    {
                        return index < count;
                    }

                    public IASTNode next()
                    {
                        if (index % 2 == 0)
                            return separators.get(index++ / 2);
                        else
                            return entries.get(index++ / 2);
                    }

                    public void remove()
                    {
                        throw new UnsupportedOperationException();
                    }
                });
            }
        };
    }

    public void accept(IASTVisitor visitor)
    {
        visitor.visitASTNode(this);
        visitor.visitASTListNode(this);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Searching
    ///////////////////////////////////////////////////////////////////////////

    @SuppressWarnings("hiding")
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

    @SuppressWarnings("unchecked")
    @Override public Object clone()
    {
            ASTSeparatedListNode<T> copy = new ASTSeparatedListNode<T>();

            for (Token t : this.separators)
            {
                if (t == null)
                    copy.separators.add(null);
                else
                {
                    Token newChild = (Token)t.clone();
                    newChild.setParent(copy);
                    copy.separators.add(newChild);
                }
            }

            for (T t : this.entries)
            {
                if (t == null)
                    copy.entries.add(null);
                else
                {
                    T newChild = (T)t.clone();
                    newChild.setParent(copy);
                    copy.entries.add(newChild);
                }
            }

            return copy;
    }
}
