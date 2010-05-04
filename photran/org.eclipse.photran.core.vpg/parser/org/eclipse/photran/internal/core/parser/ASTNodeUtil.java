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
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Iterator;
import java.util.List;

@SuppressWarnings("all")
public final class ASTNodeUtil
{
    private ASTNodeUtil() {}

    public static void removeFromTree(IASTNode node)
    {
        IASTNode parent = node.getParent();
        if (parent == null) throw new IllegalArgumentException("Cannot remove root node");
        parent.replaceChild(node, null);
    }

    public static void replaceWith(IASTNode node, IASTNode newNode)
    {
        IASTNode parent = node.getParent();
        if (parent == null) throw new IllegalArgumentException("Cannot remove root node");
        parent.replaceChild(node, newNode);
    }

    @SuppressWarnings("unchecked")
    public static <T extends IASTNode> T findNearestAncestor(IASTNode node, Class<T> targetClass)
    {
        for (IASTNode parent = node.getParent(); parent != null; parent = parent.getParent())
            if (targetClass.isAssignableFrom(parent.getClass()))
                return (T)parent;
        return null;
    }

    protected static final class Notification extends Error
    {
        private Object result;

        public Notification(Object result) { this.result = result; }

        public Object getResult() { return result; }
    }

    @SuppressWarnings("unchecked")
    public static <T extends IASTNode> T findFirst(IASTNode node, final Class<T> clazz)
    {
        try
        {
            node.accept(new GenericASTVisitor()
            {
                @Override
                public void visitASTNode(IASTNode node)
                {
                    if (clazz.isAssignableFrom(node.getClass()))
                        throw new Notification(node);
                    traverseChildren(node);
                }

                @Override public void visitToken(Token node)
                {
                    if (clazz.isAssignableFrom(node.getClass()))
                        throw new Notification(node);
                }
            });
            return null;
        }
        catch (Notification n)
        {
            return (T)n.getResult();
        }
    }

    @SuppressWarnings("unchecked")
    public static <T extends IASTNode> T findLast(IASTNode node, final Class<T> clazz)
    {
        class V extends GenericASTVisitor
        {
            T result = null;

            @Override public void visitASTNode(IASTNode node)
            {
                if (clazz.isAssignableFrom(node.getClass()))
                    result = (T)node;
                traverseChildren(node);
            }

            @Override public void visitToken(Token node)
            {
                if (clazz.isAssignableFrom(node.getClass()))
                    result = (T)node;
            }
        };

        V v = new V();
        node.accept(v);
        return v.result;
    }

    public static Token findFirstToken(IASTNode node)
    {
        return findFirst(node, Token.class);
    }

    public static Token findLastToken(IASTNode node)
    {
        return findLast(node, Token.class);
    }

    public static boolean isFirstChildInList(IASTNode node)
    {
        return node.getParent() != null
            && node.getParent() instanceof IASTListNode
            && ((IASTListNode<?>)node.getParent()).size() > 0
            && ((IASTListNode<?>)node.getParent()).get(0) == node;
    }

    public static String toString(IASTNode node)
    {
        ByteArrayOutputStream bs = new ByteArrayOutputStream();
        node.printOn(new PrintStream(bs), null);
        return bs.toString();
    }

    public static void replaceWith(IASTNode node, final String literalString)
    {
        IASTNode copy = (IASTNode)node.clone();
        final Token firstToken = copy.findFirstToken();
        final Token lastToken = copy.findLastToken();
        if (firstToken == null)
            throw new IllegalArgumentException("A node can only be replaced "
                + "with a string if it contains at least one token");
        copy.accept(new GenericASTVisitor()
        {
            @Override public void visitToken(Token token)
            {
                if (token != firstToken) token.setWhiteBefore("");
                token.setText(token == firstToken ? literalString : "");
                if (token != lastToken) token.setWhiteAfter("");
            }
        });
        node.replaceWith(copy);
    }

    public static IPreprocessorReplacement print(IASTNode node, IPreprocessorReplacement currentPreprocessorDirective, PrintStream out)
    {
        for (IASTNode child : node.getChildren())
            currentPreprocessorDirective = child.printOn(out, currentPreprocessorDirective);
        return currentPreprocessorDirective;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Utility Classes
    ///////////////////////////////////////////////////////////////////////////

    public static final class NonNullIterator<T> implements Iterator<T>
    {
        private Iterator<T> wrappedIterator;
        private T next;

        public NonNullIterator(Iterator<T> wrappedIterator)
        {
            this.wrappedIterator = wrappedIterator;
            findNext();
        }

        private void findNext()
        {
            do
            {
                if (!this.wrappedIterator.hasNext())
                {
                    this.next = null;
                    return;
                }

                this.next = this.wrappedIterator.next();
            }
            while (this.next == null);
        }

        public boolean hasNext()
        {
            return this.next != null;
        }

        public T next()
        {
            T result = this.next;
            findNext();
            return result;
        }

        public void remove()
        {
            throw new UnsupportedOperationException();
        }
    }
}
