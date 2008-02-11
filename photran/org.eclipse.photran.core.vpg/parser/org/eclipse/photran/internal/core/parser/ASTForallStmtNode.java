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

public class ASTForallStmtNode extends InteriorNode implements IActionStmt, IForallBodyConstruct
{
    ASTForallStmtNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
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
        visitor.visitIForallBodyConstruct(this);
        visitor.visitASTForallStmtNode(this);
    }

    public ASTAssignmentStmtNode getAssignment()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.FORALL_STMT_652)
            return (ASTAssignmentStmtNode)getChild(3);
        else if (getProduction() == Production.FORALL_STMT_653)
            return (ASTAssignmentStmtNode)getChild(3);
        else
            return null;
    }

    public boolean hasAssignment()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.FORALL_STMT_652)
            return getChild(3) != null;
        else if (getProduction() == Production.FORALL_STMT_653)
            return getChild(3) != null;
        else
            return false;
    }

    public Token getLabel()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.FORALL_STMT_652)
            return (Token)((ASTLblDefNode)getChild(0)).getLabel();
        else if (getProduction() == Production.FORALL_STMT_653)
            return (Token)((ASTLblDefNode)getChild(0)).getLabel();
        else
            return null;
    }

    public boolean hasLabel()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.FORALL_STMT_652)
            return ((ASTLblDefNode)getChild(0)).hasLabel();
        else if (getProduction() == Production.FORALL_STMT_653)
            return ((ASTLblDefNode)getChild(0)).hasLabel();
        else
            return false;
    }

    public ASTForallTripletSpecListNode getForallTripletSpecList()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.FORALL_STMT_652)
            return (ASTForallTripletSpecListNode)((ASTForallHeaderNode)getChild(2)).getForallTripletSpecList();
        else if (getProduction() == Production.FORALL_STMT_653)
            return (ASTForallTripletSpecListNode)((ASTForallHeaderNode)getChild(2)).getForallTripletSpecList();
        else
            return null;
    }

    public ASTScalarMaskExprNode getScalarMaskExpr()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.FORALL_STMT_652)
            return (ASTScalarMaskExprNode)((ASTForallHeaderNode)getChild(2)).getScalarMaskExpr();
        else if (getProduction() == Production.FORALL_STMT_653)
            return (ASTScalarMaskExprNode)((ASTForallHeaderNode)getChild(2)).getScalarMaskExpr();
        else
            return null;
    }

    public boolean hasScalarMaskExpr()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.FORALL_STMT_652)
            return ((ASTForallHeaderNode)getChild(2)).hasScalarMaskExpr();
        else if (getProduction() == Production.FORALL_STMT_653)
            return ((ASTForallHeaderNode)getChild(2)).hasScalarMaskExpr();
        else
            return false;
    }

    @Override protected boolean shouldVisitChild(int index)
    {
        if (getProduction() == Production.FORALL_STMT_652 && index == 1)
            return false;
        else if (getProduction() == Production.FORALL_STMT_652 && index == 4)
            return false;
        else if (getProduction() == Production.FORALL_STMT_653 && index == 1)
            return false;
        else if (getProduction() == Production.FORALL_STMT_653 && index == 4)
            return false;
        else
            return true;
    }

    @Override protected boolean childIsPulledUp(int index)
    {
        if (getProduction() == Production.FORALL_STMT_652 && index == 0)
            return true;
        else if (getProduction() == Production.FORALL_STMT_652 && index == 2)
            return true;
        else if (getProduction() == Production.FORALL_STMT_653 && index == 0)
            return true;
        else if (getProduction() == Production.FORALL_STMT_653 && index == 2)
            return true;
        else
            return false;
    }
}
