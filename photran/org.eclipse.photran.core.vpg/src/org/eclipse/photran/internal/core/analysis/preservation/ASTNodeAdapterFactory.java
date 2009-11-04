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
import org.eclipse.rephraserengine.core.preservation.ResetOffsetLength;
import org.eclipse.rephraserengine.core.util.OffsetLength;

/**
 * An adapter factory (registered in plugin.xml) that adapts {@link IASTNode} objects to:
 * <ul>
 * <li> {@link OffsetLength}
 * <li> {@link ResetOffsetLength}
 * </ul>
 *
 * @author Jeff Overbey
 */
@SuppressWarnings("unchecked")
public class ASTNodeAdapterFactory implements IAdapterFactory
{
    private static final OffsetLength EMPTY_OFFSET_LENGTH = new OffsetLength(-1, 0);

    public Class[] getAdapterList()
    {
        return new Class[]
        {
            OffsetLength.class,
            ResetOffsetLength.class
        };
    }

    public Object getAdapter(Object adaptableObject, Class adapterType)
    {
        if (!IASTNode.class.isAssignableFrom(adaptableObject.getClass())) return null;
        IASTNode node = (IASTNode)adaptableObject;

        if (adapterType.equals(OffsetLength.class))
            return getOffsetLength(node, findRoot(node));
        else if (adapterType.equals(ResetOffsetLength.class))
            return reset(node);
        else
            return null;
    }
    
    private ResetOffsetLength reset(IASTNode node)
    {
        node.accept(new GenericASTVisitor()
        {
            @Override public void visitToken(Token token)
            {
                token.setFileOffset(-1);
            }
        });
        
        return ResetOffsetLength.RESET;
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

        int offset = (previous == null ? 0 : previous.getFileOffset() + previous.getLength() + previous.getWhiteAfter().length());
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

                // Skip tokens from added subtrees (i.e., subtrees adapted to ResetOffsetLength)
                if (thisToken.getFileOffset() >= 0)
                    lastToken = thisToken;
            }
        }

        TokenFinder t = new TokenFinder();
        inAST.accept(t);
        return t.result;
    }
}
