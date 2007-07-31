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

import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.Parser.*;
import java.util.List;

public class ASTAssignedGotoStmtNode extends InteriorNode
{
    ASTAssignedGotoStmtNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
    {
         super(production);
         
         for (Object o : childNodes)
             addChild((CSTNode)o);
         constructionFinished();
    }
    
    @Override protected void visitThisNodeUsing(ASTVisitor visitor)
    {
        visitor.visitASTAssignedGotoStmtNode(this);
    }

    public ASTLblDefNode getLblDef()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.ASSIGNED_GOTO_STMT_1050)
            return (ASTLblDefNode)getChild(0);
        else if (getProduction() == Production.ASSIGNED_GOTO_STMT_1051)
            return (ASTLblDefNode)getChild(0);
        else if (getProduction() == Production.ASSIGNED_GOTO_STMT_1052)
            return (ASTLblDefNode)getChild(0);
        else
            return null;
    }

    public ASTGoToKwNode getGoToKw()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.ASSIGNED_GOTO_STMT_1050)
            return (ASTGoToKwNode)getChild(1);
        else if (getProduction() == Production.ASSIGNED_GOTO_STMT_1051)
            return (ASTGoToKwNode)getChild(1);
        else if (getProduction() == Production.ASSIGNED_GOTO_STMT_1052)
            return (ASTGoToKwNode)getChild(1);
        else
            return null;
    }

    public ASTVariableNameNode getVariableName()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.ASSIGNED_GOTO_STMT_1050)
            return (ASTVariableNameNode)getChild(2);
        else if (getProduction() == Production.ASSIGNED_GOTO_STMT_1051)
            return (ASTVariableNameNode)getChild(2);
        else
            return null;
    }

    public Token getTEos()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.ASSIGNED_GOTO_STMT_1050)
            return (Token)getChild(3);
        else if (getProduction() == Production.ASSIGNED_GOTO_STMT_1051)
            return (Token)getChild(6);
        else if (getProduction() == Production.ASSIGNED_GOTO_STMT_1052)
            return (Token)getChild(6);
        else
            return null;
    }

    public Token getTLparen()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.ASSIGNED_GOTO_STMT_1051)
            return (Token)getChild(3);
        else if (getProduction() == Production.ASSIGNED_GOTO_STMT_1052)
            return (Token)getChild(3);
        else
            return null;
    }

    public ASTLblRefListNode getLblRefList()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.ASSIGNED_GOTO_STMT_1051)
            return (ASTLblRefListNode)getChild(4);
        else if (getProduction() == Production.ASSIGNED_GOTO_STMT_1052)
            return (ASTLblRefListNode)getChild(4);
        else
            return null;
    }

    public Token getTRparen()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.ASSIGNED_GOTO_STMT_1051)
            return (Token)getChild(5);
        else if (getProduction() == Production.ASSIGNED_GOTO_STMT_1052)
            return (Token)getChild(5);
        else
            return null;
    }

    public ASTVariableCommaNode getVariableComma()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.ASSIGNED_GOTO_STMT_1052)
            return (ASTVariableCommaNode)getChild(2);
        else
            return null;
    }
}
