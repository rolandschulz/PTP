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

public class ASTAssignedGotoStmtNode extends InteriorNode implements IActionStmt
{
    ASTAssignedGotoStmtNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
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
        visitor.visitIActionStmt(this);
        visitor.visitASTAssignedGotoStmtNode(this);
    }

    public ASTLblRefListNode getLblRefList()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.ASSIGNED_GOTO_STMT_1054)
            return (ASTLblRefListNode)getChild(4);
        else if (getProduction() == Production.ASSIGNED_GOTO_STMT_1055)
            return (ASTLblRefListNode)getChild(4);
        else
            return null;
    }

    public boolean hasLblRefList()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.ASSIGNED_GOTO_STMT_1054)
            return getChild(4) != null;
        else if (getProduction() == Production.ASSIGNED_GOTO_STMT_1055)
            return getChild(4) != null;
        else
            return false;
    }

    public Token getLabel()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.ASSIGNED_GOTO_STMT_1053)
            return (Token)((ASTLblDefNode)getChild(0)).getLabel();
        else if (getProduction() == Production.ASSIGNED_GOTO_STMT_1054)
            return (Token)((ASTLblDefNode)getChild(0)).getLabel();
        else if (getProduction() == Production.ASSIGNED_GOTO_STMT_1055)
            return (Token)((ASTLblDefNode)getChild(0)).getLabel();
        else
            return null;
    }

    public boolean hasLabel()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.ASSIGNED_GOTO_STMT_1053)
            return ((ASTLblDefNode)getChild(0)).hasLabel();
        else if (getProduction() == Production.ASSIGNED_GOTO_STMT_1054)
            return ((ASTLblDefNode)getChild(0)).hasLabel();
        else if (getProduction() == Production.ASSIGNED_GOTO_STMT_1055)
            return ((ASTLblDefNode)getChild(0)).hasLabel();
        else
            return false;
    }

    public Token getVariableName()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.ASSIGNED_GOTO_STMT_1053)
            return (Token)((ASTVariableNameNode)getChild(2)).getVariableName();
        else if (getProduction() == Production.ASSIGNED_GOTO_STMT_1054)
            return (Token)((ASTVariableNameNode)getChild(2)).getVariableName();
        else if (getProduction() == Production.ASSIGNED_GOTO_STMT_1055)
            return (Token)((ASTVariableCommaNode)getChild(2)).getVariableName();
        else
            return null;
    }

    @Override protected boolean shouldVisitChild(int index)
    {
        if (getProduction() == Production.ASSIGNED_GOTO_STMT_1053 && index == 3)
            return false;
        else if (getProduction() == Production.ASSIGNED_GOTO_STMT_1054 && index == 3)
            return false;
        else if (getProduction() == Production.ASSIGNED_GOTO_STMT_1054 && index == 5)
            return false;
        else if (getProduction() == Production.ASSIGNED_GOTO_STMT_1054 && index == 6)
            return false;
        else if (getProduction() == Production.ASSIGNED_GOTO_STMT_1055 && index == 3)
            return false;
        else if (getProduction() == Production.ASSIGNED_GOTO_STMT_1055 && index == 5)
            return false;
        else if (getProduction() == Production.ASSIGNED_GOTO_STMT_1055 && index == 6)
            return false;
        else
            return true;
    }

    @Override protected boolean childIsPulledUp(int index)
    {
        if (getProduction() == Production.ASSIGNED_GOTO_STMT_1053 && index == 0)
            return true;
        else if (getProduction() == Production.ASSIGNED_GOTO_STMT_1053 && index == 1)
            return true;
        else if (getProduction() == Production.ASSIGNED_GOTO_STMT_1053 && index == 2)
            return true;
        else if (getProduction() == Production.ASSIGNED_GOTO_STMT_1054 && index == 0)
            return true;
        else if (getProduction() == Production.ASSIGNED_GOTO_STMT_1054 && index == 1)
            return true;
        else if (getProduction() == Production.ASSIGNED_GOTO_STMT_1054 && index == 2)
            return true;
        else if (getProduction() == Production.ASSIGNED_GOTO_STMT_1055 && index == 0)
            return true;
        else if (getProduction() == Production.ASSIGNED_GOTO_STMT_1055 && index == 1)
            return true;
        else if (getProduction() == Production.ASSIGNED_GOTO_STMT_1055 && index == 2)
            return true;
        else
            return false;
    }
}
