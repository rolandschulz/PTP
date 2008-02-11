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

    public Token getLabel()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.END_BLOCK_DATA_STMT_917)
            return (Token)((ASTLblDefNode)getChild(0)).getLabel();
        else if (getProduction() == Production.END_BLOCK_DATA_STMT_918)
            return (Token)((ASTLblDefNode)getChild(0)).getLabel();
        else if (getProduction() == Production.END_BLOCK_DATA_STMT_919)
            return (Token)((ASTLblDefNode)getChild(0)).getLabel();
        else if (getProduction() == Production.END_BLOCK_DATA_STMT_920)
            return (Token)((ASTLblDefNode)getChild(0)).getLabel();
        else if (getProduction() == Production.END_BLOCK_DATA_STMT_921)
            return (Token)((ASTLblDefNode)getChild(0)).getLabel();
        else if (getProduction() == Production.END_BLOCK_DATA_STMT_922)
            return (Token)((ASTLblDefNode)getChild(0)).getLabel();
        else if (getProduction() == Production.END_BLOCK_DATA_STMT_923)
            return (Token)((ASTLblDefNode)getChild(0)).getLabel();
        else if (getProduction() == Production.END_BLOCK_DATA_STMT_924)
            return (Token)((ASTLblDefNode)getChild(0)).getLabel();
        else if (getProduction() == Production.END_BLOCK_DATA_STMT_925)
            return (Token)((ASTLblDefNode)getChild(0)).getLabel();
        else
            return null;
    }

    public boolean hasLabel()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.END_BLOCK_DATA_STMT_917)
            return ((ASTLblDefNode)getChild(0)).hasLabel();
        else if (getProduction() == Production.END_BLOCK_DATA_STMT_918)
            return ((ASTLblDefNode)getChild(0)).hasLabel();
        else if (getProduction() == Production.END_BLOCK_DATA_STMT_919)
            return ((ASTLblDefNode)getChild(0)).hasLabel();
        else if (getProduction() == Production.END_BLOCK_DATA_STMT_920)
            return ((ASTLblDefNode)getChild(0)).hasLabel();
        else if (getProduction() == Production.END_BLOCK_DATA_STMT_921)
            return ((ASTLblDefNode)getChild(0)).hasLabel();
        else if (getProduction() == Production.END_BLOCK_DATA_STMT_922)
            return ((ASTLblDefNode)getChild(0)).hasLabel();
        else if (getProduction() == Production.END_BLOCK_DATA_STMT_923)
            return ((ASTLblDefNode)getChild(0)).hasLabel();
        else if (getProduction() == Production.END_BLOCK_DATA_STMT_924)
            return ((ASTLblDefNode)getChild(0)).hasLabel();
        else if (getProduction() == Production.END_BLOCK_DATA_STMT_925)
            return ((ASTLblDefNode)getChild(0)).hasLabel();
        else
            return false;
    }

    public Token getEndName()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.END_BLOCK_DATA_STMT_919)
            return (Token)((ASTEndNameNode)getChild(2)).getEndName();
        else if (getProduction() == Production.END_BLOCK_DATA_STMT_921)
            return (Token)((ASTEndNameNode)getChild(3)).getEndName();
        else if (getProduction() == Production.END_BLOCK_DATA_STMT_923)
            return (Token)((ASTEndNameNode)getChild(3)).getEndName();
        else if (getProduction() == Production.END_BLOCK_DATA_STMT_925)
            return (Token)((ASTEndNameNode)getChild(4)).getEndName();
        else
            return null;
    }

    public boolean hasEndName()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.END_BLOCK_DATA_STMT_919)
            return ((ASTEndNameNode)getChild(2)).hasEndName();
        else if (getProduction() == Production.END_BLOCK_DATA_STMT_921)
            return ((ASTEndNameNode)getChild(3)).hasEndName();
        else if (getProduction() == Production.END_BLOCK_DATA_STMT_923)
            return ((ASTEndNameNode)getChild(3)).hasEndName();
        else if (getProduction() == Production.END_BLOCK_DATA_STMT_925)
            return ((ASTEndNameNode)getChild(4)).hasEndName();
        else
            return false;
    }

    @Override protected boolean shouldVisitChild(int index)
    {
        if (getProduction() == Production.END_BLOCK_DATA_STMT_917 && index == 1)
            return false;
        else if (getProduction() == Production.END_BLOCK_DATA_STMT_917 && index == 2)
            return false;
        else if (getProduction() == Production.END_BLOCK_DATA_STMT_918 && index == 1)
            return false;
        else if (getProduction() == Production.END_BLOCK_DATA_STMT_918 && index == 2)
            return false;
        else if (getProduction() == Production.END_BLOCK_DATA_STMT_919 && index == 1)
            return false;
        else if (getProduction() == Production.END_BLOCK_DATA_STMT_919 && index == 3)
            return false;
        else if (getProduction() == Production.END_BLOCK_DATA_STMT_920 && index == 1)
            return false;
        else if (getProduction() == Production.END_BLOCK_DATA_STMT_920 && index == 2)
            return false;
        else if (getProduction() == Production.END_BLOCK_DATA_STMT_920 && index == 3)
            return false;
        else if (getProduction() == Production.END_BLOCK_DATA_STMT_921 && index == 1)
            return false;
        else if (getProduction() == Production.END_BLOCK_DATA_STMT_921 && index == 2)
            return false;
        else if (getProduction() == Production.END_BLOCK_DATA_STMT_921 && index == 4)
            return false;
        else if (getProduction() == Production.END_BLOCK_DATA_STMT_922 && index == 1)
            return false;
        else if (getProduction() == Production.END_BLOCK_DATA_STMT_922 && index == 2)
            return false;
        else if (getProduction() == Production.END_BLOCK_DATA_STMT_922 && index == 3)
            return false;
        else if (getProduction() == Production.END_BLOCK_DATA_STMT_923 && index == 1)
            return false;
        else if (getProduction() == Production.END_BLOCK_DATA_STMT_923 && index == 2)
            return false;
        else if (getProduction() == Production.END_BLOCK_DATA_STMT_923 && index == 4)
            return false;
        else if (getProduction() == Production.END_BLOCK_DATA_STMT_924 && index == 1)
            return false;
        else if (getProduction() == Production.END_BLOCK_DATA_STMT_924 && index == 2)
            return false;
        else if (getProduction() == Production.END_BLOCK_DATA_STMT_924 && index == 3)
            return false;
        else if (getProduction() == Production.END_BLOCK_DATA_STMT_924 && index == 4)
            return false;
        else if (getProduction() == Production.END_BLOCK_DATA_STMT_925 && index == 1)
            return false;
        else if (getProduction() == Production.END_BLOCK_DATA_STMT_925 && index == 2)
            return false;
        else if (getProduction() == Production.END_BLOCK_DATA_STMT_925 && index == 3)
            return false;
        else if (getProduction() == Production.END_BLOCK_DATA_STMT_925 && index == 5)
            return false;
        else
            return true;
    }

    @Override protected boolean childIsPulledUp(int index)
    {
        if (getProduction() == Production.END_BLOCK_DATA_STMT_917 && index == 0)
            return true;
        else if (getProduction() == Production.END_BLOCK_DATA_STMT_918 && index == 0)
            return true;
        else if (getProduction() == Production.END_BLOCK_DATA_STMT_919 && index == 0)
            return true;
        else if (getProduction() == Production.END_BLOCK_DATA_STMT_919 && index == 2)
            return true;
        else if (getProduction() == Production.END_BLOCK_DATA_STMT_920 && index == 0)
            return true;
        else if (getProduction() == Production.END_BLOCK_DATA_STMT_921 && index == 0)
            return true;
        else if (getProduction() == Production.END_BLOCK_DATA_STMT_921 && index == 3)
            return true;
        else if (getProduction() == Production.END_BLOCK_DATA_STMT_922 && index == 0)
            return true;
        else if (getProduction() == Production.END_BLOCK_DATA_STMT_923 && index == 0)
            return true;
        else if (getProduction() == Production.END_BLOCK_DATA_STMT_923 && index == 3)
            return true;
        else if (getProduction() == Production.END_BLOCK_DATA_STMT_924 && index == 0)
            return true;
        else if (getProduction() == Production.END_BLOCK_DATA_STMT_925 && index == 0)
            return true;
        else if (getProduction() == Production.END_BLOCK_DATA_STMT_925 && index == 4)
            return true;
        else
            return false;
    }
}
