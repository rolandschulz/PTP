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
package org.eclipse.photran.core;

import java.util.Iterator;

import org.eclipse.core.resources.IFile;
import org.eclipse.photran.core.vpg.util.IterableWrapper;
import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.lexer.TokenList;
import org.eclipse.photran.internal.core.parser.ASTExecutableProgramNode;
import org.eclipse.photran.internal.core.parser.ASTVisitor;
import org.eclipse.photran.internal.core.parser.GenericParseTreeVisitor;

/**
 * The root of the Fortran AST (implementation of <code>IFortranAST</code>)
 * 
 * @author Jeff Overbey
 */
public class FortranAST implements IFortranAST
{
    private ASTExecutableProgramNode root;
    private TokenList tokenList;
    
    public FortranAST(IFile file, ASTExecutableProgramNode root, TokenList tokenList)
    {
        this.root = root;
        this.tokenList = tokenList;
        for (Token token : new IterableWrapper<Token>(tokenList))
        	if (token.getFile() == null)
        		token.setFile(file);
    }
    
    public void visitBottomUpUsing(ASTVisitor visitor)
    {
        root.visitBottomUpUsing(visitor);
    }
    
    public void visitOnlyThisNodeUsing(ASTVisitor visitor)
    {
        root.visitOnlyThisNodeUsing(visitor);
    }
    
    public void visitTopDownUsing(ASTVisitor visitor)
    {
        root.visitTopDownUsing(visitor);
    }
    
    public void visitUsing(GenericParseTreeVisitor visitor)
    {
        root.visitUsing(visitor);
    }
    
    public ASTExecutableProgramNode getRoot()
    {
        return root;
    }

    public Iterator/*<token>*/ iterator()
    {
        return tokenList.iterator();
    }

//    public void rebuildTokenList()
//    {
//    	this.tokenList = new TokenList(root);
//    }

    public Token findTokenByStreamOffsetLength(final int offset, final int length)
    {
        // Binary Search
        return tokenList.findStreamOffsetLength(offset, length);

// or Linear Search...
//        for (int i = 0; i < tokenList.size(); i++)
//        {
//            Token token = (Token)tokenList.get(i);
//            if (token.getOffset() == offset && token.getLength() == length)
//                return token;
//        }
//        return null;
        
// or Parse Tree Traversal...
//        try
//        {
//            root.visitUsing(new GenericParseTreeVisitor()
//            {
//                public void visitToken(Token token)
//                {
//                    if (token.getOffset() == offset && token.getLength() == length)
//                        throw new Notification(token);
//                }
//            });
//        }
//        catch (Notification n)
//        {
//            return (Token)n.getResult();
//        }
//        return null;
    }

    public Token findTokenByFileOffsetLength(IFile file, int offset, int length)
    {
        for (int i = 0; i < tokenList.size(); i++)
        {
            Token token = (Token)tokenList.get(i);
            if (token.getFile().equals(file) && token.getFileOffset() == offset && token.getLength() == length)
                return token;
        }
        return null;
    }
    
    public Token findFirstTokenOnLine(int line)
    {
        // Binary Search
        return tokenList.findFirstTokenOnLine(line);
    }
}