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

public class ASTElseIfStmtNode extends InteriorNodeWithErrorRecoverySymbols
{
    ASTElseIfStmtNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
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
        visitor.visitASTElseIfStmtNode(this);
    }

    public ASTExpressionNode getGuardingExpression()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.ELSE_IF_STMT_665)
            return (ASTExpressionNode)getChild(3);
        else if (getProduction() == Production.ELSE_IF_STMT_666)
            return (ASTExpressionNode)getChild(3);
        else if (getProduction() == Production.ELSE_IF_STMT_667)
            return (ASTExpressionNode)getChild(4);
        else if (getProduction() == Production.ELSE_IF_STMT_668)
            return (ASTExpressionNode)getChild(4);
        else
            return null;
    }

    public Token getLabel()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.ELSE_IF_STMT_665)
            return (Token)((ASTLblDefNode)getChild(0)).getLabel();
        else if (getProduction() == Production.ELSE_IF_STMT_666)
            return (Token)((ASTLblDefNode)getChild(0)).getLabel();
        else if (getProduction() == Production.ELSE_IF_STMT_667)
            return (Token)((ASTLblDefNode)getChild(0)).getLabel();
        else if (getProduction() == Production.ELSE_IF_STMT_668)
            return (Token)((ASTLblDefNode)getChild(0)).getLabel();
        else if (getProduction() == Production.ELSE_IF_STMT_ERROR_7)
            return (Token)((ASTLblDefNode)getChild(0)).getLabel();
        else if (getProduction() == Production.ELSE_IF_STMT_ERROR_8)
            return (Token)((ASTLblDefNode)getChild(0)).getLabel();
        else
            return null;
    }

    public boolean hasLabel()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.ELSE_IF_STMT_665)
            return ((ASTLblDefNode)getChild(0)).hasLabel();
        else if (getProduction() == Production.ELSE_IF_STMT_666)
            return ((ASTLblDefNode)getChild(0)).hasLabel();
        else if (getProduction() == Production.ELSE_IF_STMT_667)
            return ((ASTLblDefNode)getChild(0)).hasLabel();
        else if (getProduction() == Production.ELSE_IF_STMT_668)
            return ((ASTLblDefNode)getChild(0)).hasLabel();
        else if (getProduction() == Production.ELSE_IF_STMT_ERROR_7)
            return ((ASTLblDefNode)getChild(0)).hasLabel();
        else if (getProduction() == Production.ELSE_IF_STMT_ERROR_8)
            return ((ASTLblDefNode)getChild(0)).hasLabel();
        else
            return false;
    }

    public Token getEndName()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.ELSE_IF_STMT_666)
            return (Token)((ASTEndNameNode)getChild(6)).getEndName();
        else if (getProduction() == Production.ELSE_IF_STMT_668)
            return (Token)((ASTEndNameNode)getChild(7)).getEndName();
        else
            return null;
    }

    public boolean hasEndName()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.ELSE_IF_STMT_666)
            return ((ASTEndNameNode)getChild(6)).hasEndName();
        else if (getProduction() == Production.ELSE_IF_STMT_668)
            return ((ASTEndNameNode)getChild(7)).hasEndName();
        else
            return false;
    }

    @Override protected boolean shouldVisitChild(int index)
    {
        if (getProduction() == Production.ELSE_IF_STMT_665 && index == 1)
            return false;
        else if (getProduction() == Production.ELSE_IF_STMT_665 && index == 2)
            return false;
        else if (getProduction() == Production.ELSE_IF_STMT_665 && index == 4)
            return false;
        else if (getProduction() == Production.ELSE_IF_STMT_665 && index == 5)
            return false;
        else if (getProduction() == Production.ELSE_IF_STMT_665 && index == 6)
            return false;
        else if (getProduction() == Production.ELSE_IF_STMT_666 && index == 1)
            return false;
        else if (getProduction() == Production.ELSE_IF_STMT_666 && index == 2)
            return false;
        else if (getProduction() == Production.ELSE_IF_STMT_666 && index == 4)
            return false;
        else if (getProduction() == Production.ELSE_IF_STMT_666 && index == 5)
            return false;
        else if (getProduction() == Production.ELSE_IF_STMT_666 && index == 7)
            return false;
        else if (getProduction() == Production.ELSE_IF_STMT_667 && index == 1)
            return false;
        else if (getProduction() == Production.ELSE_IF_STMT_667 && index == 2)
            return false;
        else if (getProduction() == Production.ELSE_IF_STMT_667 && index == 3)
            return false;
        else if (getProduction() == Production.ELSE_IF_STMT_667 && index == 5)
            return false;
        else if (getProduction() == Production.ELSE_IF_STMT_667 && index == 6)
            return false;
        else if (getProduction() == Production.ELSE_IF_STMT_667 && index == 7)
            return false;
        else if (getProduction() == Production.ELSE_IF_STMT_668 && index == 1)
            return false;
        else if (getProduction() == Production.ELSE_IF_STMT_668 && index == 2)
            return false;
        else if (getProduction() == Production.ELSE_IF_STMT_668 && index == 3)
            return false;
        else if (getProduction() == Production.ELSE_IF_STMT_668 && index == 5)
            return false;
        else if (getProduction() == Production.ELSE_IF_STMT_668 && index == 6)
            return false;
        else if (getProduction() == Production.ELSE_IF_STMT_668 && index == 8)
            return false;
        else if (getProduction() == Production.ELSE_IF_STMT_ERROR_7 && index == 1)
            return false;
        else if (getProduction() == Production.ELSE_IF_STMT_ERROR_8 && index == 1)
            return false;
        else if (getProduction() == Production.ELSE_IF_STMT_ERROR_8 && index == 2)
            return false;
        else
            return true;
    }

    @Override protected boolean childIsPulledUp(int index)
    {
        if (getProduction() == Production.ELSE_IF_STMT_665 && index == 0)
            return true;
        else if (getProduction() == Production.ELSE_IF_STMT_666 && index == 0)
            return true;
        else if (getProduction() == Production.ELSE_IF_STMT_666 && index == 6)
            return true;
        else if (getProduction() == Production.ELSE_IF_STMT_667 && index == 0)
            return true;
        else if (getProduction() == Production.ELSE_IF_STMT_668 && index == 0)
            return true;
        else if (getProduction() == Production.ELSE_IF_STMT_668 && index == 7)
            return true;
        else if (getProduction() == Production.ELSE_IF_STMT_ERROR_7 && index == 0)
            return true;
        else if (getProduction() == Production.ELSE_IF_STMT_ERROR_8 && index == 0)
            return true;
        else
            return false;
    }
}
