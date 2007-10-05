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

public class ASTEndBlockDataStmtNode extends InteriorNode
{
    ASTEndBlockDataStmtNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
    {
         super(production);
         
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
        visitor.visitASTEndBlockDataStmtNode(this);
    }

    public ASTLblDefNode getLblDef()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.END_BLOCK_DATA_STMT_914)
            return (ASTLblDefNode)getChild(0);
        else if (getProduction() == Production.END_BLOCK_DATA_STMT_915)
            return (ASTLblDefNode)getChild(0);
        else if (getProduction() == Production.END_BLOCK_DATA_STMT_916)
            return (ASTLblDefNode)getChild(0);
        else if (getProduction() == Production.END_BLOCK_DATA_STMT_917)
            return (ASTLblDefNode)getChild(0);
        else if (getProduction() == Production.END_BLOCK_DATA_STMT_918)
            return (ASTLblDefNode)getChild(0);
        else if (getProduction() == Production.END_BLOCK_DATA_STMT_919)
            return (ASTLblDefNode)getChild(0);
        else if (getProduction() == Production.END_BLOCK_DATA_STMT_920)
            return (ASTLblDefNode)getChild(0);
        else if (getProduction() == Production.END_BLOCK_DATA_STMT_921)
            return (ASTLblDefNode)getChild(0);
        else if (getProduction() == Production.END_BLOCK_DATA_STMT_922)
            return (ASTLblDefNode)getChild(0);
        else
            return null;
    }

    public Token getTEnd()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.END_BLOCK_DATA_STMT_914)
            return (Token)getChild(1);
        else if (getProduction() == Production.END_BLOCK_DATA_STMT_917)
            return (Token)getChild(1);
        else if (getProduction() == Production.END_BLOCK_DATA_STMT_918)
            return (Token)getChild(1);
        else if (getProduction() == Production.END_BLOCK_DATA_STMT_921)
            return (Token)getChild(1);
        else if (getProduction() == Production.END_BLOCK_DATA_STMT_922)
            return (Token)getChild(1);
        else
            return null;
    }

    public Token getTEos()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.END_BLOCK_DATA_STMT_914)
            return (Token)getChild(2);
        else if (getProduction() == Production.END_BLOCK_DATA_STMT_915)
            return (Token)getChild(2);
        else if (getProduction() == Production.END_BLOCK_DATA_STMT_916)
            return (Token)getChild(3);
        else if (getProduction() == Production.END_BLOCK_DATA_STMT_917)
            return (Token)getChild(3);
        else if (getProduction() == Production.END_BLOCK_DATA_STMT_918)
            return (Token)getChild(4);
        else if (getProduction() == Production.END_BLOCK_DATA_STMT_919)
            return (Token)getChild(3);
        else if (getProduction() == Production.END_BLOCK_DATA_STMT_920)
            return (Token)getChild(4);
        else if (getProduction() == Production.END_BLOCK_DATA_STMT_921)
            return (Token)getChild(4);
        else if (getProduction() == Production.END_BLOCK_DATA_STMT_922)
            return (Token)getChild(5);
        else
            return null;
    }

    public Token getTEndblockdata()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.END_BLOCK_DATA_STMT_915)
            return (Token)getChild(1);
        else if (getProduction() == Production.END_BLOCK_DATA_STMT_916)
            return (Token)getChild(1);
        else
            return null;
    }

    public ASTEndNameNode getEndName()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.END_BLOCK_DATA_STMT_916)
            return (ASTEndNameNode)getChild(2);
        else if (getProduction() == Production.END_BLOCK_DATA_STMT_918)
            return (ASTEndNameNode)getChild(3);
        else if (getProduction() == Production.END_BLOCK_DATA_STMT_920)
            return (ASTEndNameNode)getChild(3);
        else if (getProduction() == Production.END_BLOCK_DATA_STMT_922)
            return (ASTEndNameNode)getChild(4);
        else
            return null;
    }

    public Token getTBlockdata()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.END_BLOCK_DATA_STMT_917)
            return (Token)getChild(2);
        else if (getProduction() == Production.END_BLOCK_DATA_STMT_918)
            return (Token)getChild(2);
        else
            return null;
    }

    public Token getTEndblock()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.END_BLOCK_DATA_STMT_919)
            return (Token)getChild(1);
        else if (getProduction() == Production.END_BLOCK_DATA_STMT_920)
            return (Token)getChild(1);
        else
            return null;
    }

    public Token getTData()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.END_BLOCK_DATA_STMT_919)
            return (Token)getChild(2);
        else if (getProduction() == Production.END_BLOCK_DATA_STMT_920)
            return (Token)getChild(2);
        else if (getProduction() == Production.END_BLOCK_DATA_STMT_921)
            return (Token)getChild(3);
        else if (getProduction() == Production.END_BLOCK_DATA_STMT_922)
            return (Token)getChild(3);
        else
            return null;
    }

    public Token getTBlock()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.END_BLOCK_DATA_STMT_921)
            return (Token)getChild(2);
        else if (getProduction() == Production.END_BLOCK_DATA_STMT_922)
            return (Token)getChild(2);
        else
            return null;
    }
}
