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
package org.eclipse.photran.internal.core;

import java.util.Iterator;

import org.eclipse.core.resources.IFile;
import org.eclipse.photran.core.IFortranAST;
import org.eclipse.photran.internal.core.lexer.FileOrIFile;
import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.lexer.TokenList;
import org.eclipse.photran.internal.core.parser.ASTExecutableProgramNode;
import org.eclipse.photran.internal.core.parser.IASTVisitor;
import org.eclipse.photran.internal.core.util.IterableWrapper;

/**
 * A Fortran abstract syntax tree container class (canonical implementation of {@link IFortranAST}).
 * 
 * @author Jeff Overbey
 */
public class FortranAST implements IFortranAST
{
    private IFile file;
    private ASTExecutableProgramNode root;
    private TokenList tokenList;
    
    public FortranAST(IFile file, ASTExecutableProgramNode root, TokenList tokenList)
    {
        this.file = file;
        this.root = root;
        this.tokenList = tokenList;
        
        FileOrIFile f = new FileOrIFile(file);
        for (Token token : new IterableWrapper<Token>(tokenList))
        {
        	if (token.getPhysicalFile() == null)
        		token.setPhysicalFile(f);
        	
        	token.setLogicalFile(file);
        }
    }

    public void accept(IASTVisitor visitor)
    {
        root.accept(visitor);
    }
    
    public IFile getFile()
    {
        return file;
    }
    
    public ASTExecutableProgramNode getRoot()
    {
        return root;
    }

    public Iterator<Token> iterator()
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

    /** WARNING: Files are compared by identity, not equality */
    public Token findTokenByFileOffsetLength(IFile file, int offset, int length)
    {
        for (int i = 0; i < tokenList.size(); i++)
        {
            Token token = (Token)tokenList.get(i);
            FileOrIFile physicalFile = token.getPhysicalFile();
            if (physicalFile != null
                && physicalFile.getIFile() != null
                && physicalFile.getIFile().hashCode() == file.hashCode()  //    OPTIMIZATION: Profile indicates lots
                && physicalFile.getIFile().equals(file)                   // << of time spent in String#equals here
                && token.getFileOffset() == offset
                && token.getLength() == length)
                return token;
        }
        return null;
    }
    
    public Token findFirstTokenOnLine(int line)
    {
        // Binary Search
        return tokenList.findFirstTokenOnLine(line);
    }
    
    public Token findFirstTokenOnOrAfterLine(int line)
    {
        // Binary Search
        return tokenList.findFirstTokenOnOrAfterLine(line);
    }
    
    public Token findLastTokenOnLine(int line)
    {
        // Binary Search
        return tokenList.findLastTokenOnLine(line);
    }
    
    public Token findLastTokenOnOrBeforeLine(int line)
    {
        // Binary Search
        return tokenList.findLastTokenOnOrBeforeLine(line);
    }
}