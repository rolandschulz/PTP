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

import org.eclipse.photran.internal.core.lexer.*;                   import org.eclipse.photran.internal.core.analysis.binding.ScopingNode;

import org.eclipse.photran.internal.core.parser.Parser.*;
import java.util.List;

public class ASTFunctionStmtNode extends InteriorNodeWithErrorRecoverySymbols
{
    ASTFunctionStmtNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
    {
         super(production, discardedSymbols);
         
         for (Object o : childNodes)
             addChild((CSTNode)o);
         constructionFinished();
    }
        
    @Override public InteriorNode getASTParent()
    {
        InteriorNode actualParent = super.getParent();
        
        // If a node has been pulled up in an ACST, its physical parent in
        // the CST is not its logical parent in the ACST
        if (actualParent != null && actualParent.childIsPulledUp(actualParent.findChild(this)))
            return actualParent.getParent();
        else 
            return actualParent;
    }
    
    @Override protected void visitThisNodeUsing(ASTVisitor visitor)
    {
        visitor.visitASTFunctionStmtNode(this);
    }

    public ASTLblDefNode getLblDef()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.FUNCTION_STMT_982)
            return (ASTLblDefNode)getChild(0);
        else if (getProduction() == Production.FUNCTION_STMT_983)
            return (ASTLblDefNode)getChild(0);
        else if (getProduction() == Production.FUNCTION_STMT_984)
            return (ASTLblDefNode)getChild(0);
        else if (getProduction() == Production.FUNCTION_STMT_985)
            return (ASTLblDefNode)getChild(0);
        else if (getProduction() == Production.FUNCTION_STMT_ERROR_17)
            return (ASTLblDefNode)getChild(0);
        else
            return null;
    }

    public ASTFunctionPrefixNode getFunctionPrefix()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.FUNCTION_STMT_982)
            return (ASTFunctionPrefixNode)getChild(1);
        else if (getProduction() == Production.FUNCTION_STMT_983)
            return (ASTFunctionPrefixNode)getChild(1);
        else if (getProduction() == Production.FUNCTION_STMT_984)
            return (ASTFunctionPrefixNode)getChild(1);
        else if (getProduction() == Production.FUNCTION_STMT_985)
            return (ASTFunctionPrefixNode)getChild(1);
        else if (getProduction() == Production.FUNCTION_STMT_ERROR_17)
            return (ASTFunctionPrefixNode)getChild(1);
        else
            return null;
    }

    public ASTFunctionNameNode getFunctionName()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.FUNCTION_STMT_982)
            return (ASTFunctionNameNode)getChild(2);
        else if (getProduction() == Production.FUNCTION_STMT_983)
            return (ASTFunctionNameNode)getChild(2);
        else if (getProduction() == Production.FUNCTION_STMT_984)
            return (ASTFunctionNameNode)getChild(2);
        else if (getProduction() == Production.FUNCTION_STMT_985)
            return (ASTFunctionNameNode)getChild(2);
        else if (getProduction() == Production.FUNCTION_STMT_ERROR_17)
            return (ASTFunctionNameNode)getChild(2);
        else
            return null;
    }

    public Token getTLparen()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.FUNCTION_STMT_982)
            return (Token)getChild(3);
        else if (getProduction() == Production.FUNCTION_STMT_983)
            return (Token)getChild(3);
        else if (getProduction() == Production.FUNCTION_STMT_984)
            return (Token)getChild(3);
        else if (getProduction() == Production.FUNCTION_STMT_985)
            return (Token)getChild(3);
        else
            return null;
    }

    public Token getTRparen()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.FUNCTION_STMT_982)
            return (Token)getChild(4);
        else if (getProduction() == Production.FUNCTION_STMT_983)
            return (Token)getChild(4);
        else if (getProduction() == Production.FUNCTION_STMT_984)
            return (Token)getChild(5);
        else if (getProduction() == Production.FUNCTION_STMT_985)
            return (Token)getChild(5);
        else
            return null;
    }

    public Token getTEos()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.FUNCTION_STMT_982)
            return (Token)getChild(5);
        else if (getProduction() == Production.FUNCTION_STMT_983)
            return (Token)getChild(9);
        else if (getProduction() == Production.FUNCTION_STMT_984)
            return (Token)getChild(6);
        else if (getProduction() == Production.FUNCTION_STMT_985)
            return (Token)getChild(10);
        else
            return null;
    }

    public Token getTResult()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.FUNCTION_STMT_983)
            return (Token)getChild(5);
        else if (getProduction() == Production.FUNCTION_STMT_985)
            return (Token)getChild(6);
        else
            return null;
    }

    public Token getTLparen2()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.FUNCTION_STMT_983)
            return (Token)getChild(6);
        else if (getProduction() == Production.FUNCTION_STMT_985)
            return (Token)getChild(7);
        else
            return null;
    }

    public ASTNameNode getName()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.FUNCTION_STMT_983)
            return (ASTNameNode)getChild(7);
        else if (getProduction() == Production.FUNCTION_STMT_985)
            return (ASTNameNode)getChild(8);
        else
            return null;
    }

    public Token getTRparen2()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.FUNCTION_STMT_983)
            return (Token)getChild(8);
        else if (getProduction() == Production.FUNCTION_STMT_985)
            return (Token)getChild(9);
        else
            return null;
    }

    public ASTFunctionParsNode getFunctionPars()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.FUNCTION_STMT_984)
            return (ASTFunctionParsNode)getChild(4);
        else if (getProduction() == Production.FUNCTION_STMT_985)
            return (ASTFunctionParsNode)getChild(4);
        else
            return null;
    }
}
