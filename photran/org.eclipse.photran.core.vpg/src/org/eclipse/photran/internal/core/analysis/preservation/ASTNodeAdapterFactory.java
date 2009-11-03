/*******************************************************************************
 * Copyright (c) 2009 University of Illinois at Urbana-Champaign and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    UIUC - Initial API and implementation
 *******************************************************************************/
package org.eclipse.photran.internal.core.analysis.preservation;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.Parser.GenericASTVisitor;
import org.eclipse.photran.internal.core.parser.Parser.IASTNode;
import org.eclipse.rephraserengine.core.util.OffsetLength;

/**
 * An adapter factory (registered in plugin.xml) that adapts {@link IASTNode} objects to
 * {@link OffsetLength} objects.
 *
 * @author Jeff Overbey
 */
@SuppressWarnings("unchecked")
public class ASTNodeAdapterFactory implements IAdapterFactory
{
    private static final OffsetLength EMPTY_OFFSET_LENGTH = new OffsetLength(-1, 0);

    public Class[] getAdapterList()
    {
        return new Class[] { OffsetLength.class };
    }

    public Object getAdapter(Object adaptableObject, Class adapterType)
    {
        if (!IASTNode.class.isAssignableFrom(adaptableObject.getClass())) return null;
        if (!adapterType.equals(OffsetLength.class)) return null;

        IASTNode node = (IASTNode)adaptableObject;

        return getOffsetLength(node, findRoot(node));
    }

    private IASTNode findRoot(IASTNode node)
    {
        IASTNode root = node;
        while (root.getParent() != null)
            root = root.getParent();
        return root;
    }

    private OffsetLength getOffsetLength(IASTNode node, IASTNode inAST)
    {
        Token first = node.findFirstToken();
        Token last = node.findLastToken();
        if (first == null || last == null) return EMPTY_OFFSET_LENGTH;

        Token previous = findLastTokenBefore(first, inAST);
        Token next = findFirstTokenAfter(last, inAST);
        if (previous == null || next == null) return EMPTY_OFFSET_LENGTH; // FIXME should handle BOF, EOF

        int offset = previous.getFileOffset() + previous.getLength() + previous.getWhiteAfter().length();
        int length = node.toString().length();
        return new OffsetLength(offset, length);
    }

    private Token findLastTokenBefore(final Token target, IASTNode inAST)
    {
        class TokenFinder extends GenericASTVisitor
        {
            private Token lastToken = null;
            private Token result = null;

            @Override public void visitToken(Token thisToken)
            {
                if (thisToken == target)
                    result = lastToken;

                if (thisToken.getFileOffset() >= 0) // Skip tokens added in markAlpha above
                    lastToken = thisToken;
            }
        }

        TokenFinder t = new TokenFinder();
        inAST.accept(t);
        return t.result;
    }

    // from Definition

    private Token findFirstTokenAfter(final Token target, IASTNode inAST)
    {
        class TokenFinder extends GenericASTVisitor
        {
            private Token lastToken = null;
            private Token result = null;

            @Override public void visitToken(Token thisToken)
            {
                if (lastToken == target)
                    result = thisToken;

                lastToken = thisToken;
            }
        }

        TokenFinder t = new TokenFinder();
        inAST.accept(t);
        return t.result;
    }
}
