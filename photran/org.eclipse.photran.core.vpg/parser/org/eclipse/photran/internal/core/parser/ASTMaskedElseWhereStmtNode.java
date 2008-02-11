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

public class ASTMaskedElseWhereStmtNode extends InteriorNode
{
    ASTMaskedElseWhereStmtNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
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
        visitor.visitASTMaskedElseWhereStmtNode(this);
    }

    public Token getLabel()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.MASKED_ELSE_WHERE_STMT_619)
            return (Token)((ASTLblDefNode)getChild(0)).getLabel();
        else if (getProduction() == Production.MASKED_ELSE_WHERE_STMT_620)
            return (Token)((ASTLblDefNode)getChild(0)).getLabel();
        else if (getProduction() == Production.MASKED_ELSE_WHERE_STMT_621)
            return (Token)((ASTLblDefNode)getChild(0)).getLabel();
        else if (getProduction() == Production.MASKED_ELSE_WHERE_STMT_622)
            return (Token)((ASTLblDefNode)getChild(0)).getLabel();
        else
            return null;
    }

    public boolean hasLabel()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.MASKED_ELSE_WHERE_STMT_619)
            return ((ASTLblDefNode)getChild(0)).hasLabel();
        else if (getProduction() == Production.MASKED_ELSE_WHERE_STMT_620)
            return ((ASTLblDefNode)getChild(0)).hasLabel();
        else if (getProduction() == Production.MASKED_ELSE_WHERE_STMT_621)
            return ((ASTLblDefNode)getChild(0)).hasLabel();
        else if (getProduction() == Production.MASKED_ELSE_WHERE_STMT_622)
            return ((ASTLblDefNode)getChild(0)).hasLabel();
        else
            return false;
    }

    public ASTExpressionNode getMaskExpr()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.MASKED_ELSE_WHERE_STMT_619)
            return (ASTExpressionNode)((ASTMaskExprNode)getChild(3)).getMaskExpr();
        else if (getProduction() == Production.MASKED_ELSE_WHERE_STMT_620)
            return (ASTExpressionNode)((ASTMaskExprNode)getChild(3)).getMaskExpr();
        else if (getProduction() == Production.MASKED_ELSE_WHERE_STMT_621)
            return (ASTExpressionNode)((ASTMaskExprNode)getChild(4)).getMaskExpr();
        else if (getProduction() == Production.MASKED_ELSE_WHERE_STMT_622)
            return (ASTExpressionNode)((ASTMaskExprNode)getChild(4)).getMaskExpr();
        else
            return null;
    }

    public Token getEndName()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.MASKED_ELSE_WHERE_STMT_620)
            return (Token)((ASTEndNameNode)getChild(5)).getEndName();
        else if (getProduction() == Production.MASKED_ELSE_WHERE_STMT_622)
            return (Token)((ASTEndNameNode)getChild(6)).getEndName();
        else
            return null;
    }

    public boolean hasEndName()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.MASKED_ELSE_WHERE_STMT_620)
            return ((ASTEndNameNode)getChild(5)).hasEndName();
        else if (getProduction() == Production.MASKED_ELSE_WHERE_STMT_622)
            return ((ASTEndNameNode)getChild(6)).hasEndName();
        else
            return false;
    }

    @Override protected boolean shouldVisitChild(int index)
    {
        if (getProduction() == Production.MASKED_ELSE_WHERE_STMT_619 && index == 1)
            return false;
        else if (getProduction() == Production.MASKED_ELSE_WHERE_STMT_619 && index == 2)
            return false;
        else if (getProduction() == Production.MASKED_ELSE_WHERE_STMT_619 && index == 4)
            return false;
        else if (getProduction() == Production.MASKED_ELSE_WHERE_STMT_619 && index == 5)
            return false;
        else if (getProduction() == Production.MASKED_ELSE_WHERE_STMT_620 && index == 1)
            return false;
        else if (getProduction() == Production.MASKED_ELSE_WHERE_STMT_620 && index == 2)
            return false;
        else if (getProduction() == Production.MASKED_ELSE_WHERE_STMT_620 && index == 4)
            return false;
        else if (getProduction() == Production.MASKED_ELSE_WHERE_STMT_620 && index == 6)
            return false;
        else if (getProduction() == Production.MASKED_ELSE_WHERE_STMT_621 && index == 1)
            return false;
        else if (getProduction() == Production.MASKED_ELSE_WHERE_STMT_621 && index == 2)
            return false;
        else if (getProduction() == Production.MASKED_ELSE_WHERE_STMT_621 && index == 3)
            return false;
        else if (getProduction() == Production.MASKED_ELSE_WHERE_STMT_621 && index == 5)
            return false;
        else if (getProduction() == Production.MASKED_ELSE_WHERE_STMT_621 && index == 6)
            return false;
        else if (getProduction() == Production.MASKED_ELSE_WHERE_STMT_622 && index == 1)
            return false;
        else if (getProduction() == Production.MASKED_ELSE_WHERE_STMT_622 && index == 2)
            return false;
        else if (getProduction() == Production.MASKED_ELSE_WHERE_STMT_622 && index == 3)
            return false;
        else if (getProduction() == Production.MASKED_ELSE_WHERE_STMT_622 && index == 5)
            return false;
        else if (getProduction() == Production.MASKED_ELSE_WHERE_STMT_622 && index == 7)
            return false;
        else
            return true;
    }

    @Override protected boolean childIsPulledUp(int index)
    {
        if (getProduction() == Production.MASKED_ELSE_WHERE_STMT_619 && index == 0)
            return true;
        else if (getProduction() == Production.MASKED_ELSE_WHERE_STMT_619 && index == 3)
            return true;
        else if (getProduction() == Production.MASKED_ELSE_WHERE_STMT_620 && index == 0)
            return true;
        else if (getProduction() == Production.MASKED_ELSE_WHERE_STMT_620 && index == 3)
            return true;
        else if (getProduction() == Production.MASKED_ELSE_WHERE_STMT_620 && index == 5)
            return true;
        else if (getProduction() == Production.MASKED_ELSE_WHERE_STMT_621 && index == 0)
            return true;
        else if (getProduction() == Production.MASKED_ELSE_WHERE_STMT_621 && index == 4)
            return true;
        else if (getProduction() == Production.MASKED_ELSE_WHERE_STMT_622 && index == 0)
            return true;
        else if (getProduction() == Production.MASKED_ELSE_WHERE_STMT_622 && index == 4)
            return true;
        else if (getProduction() == Production.MASKED_ELSE_WHERE_STMT_622 && index == 6)
            return true;
        else
            return false;
    }
}
