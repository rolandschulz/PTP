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

public class ASTForallConstructStmtNode extends InteriorNodeWithErrorRecoverySymbols
{
    ASTForallConstructStmtNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
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
        visitor.visitASTForallConstructStmtNode(this);
    }

    public Token getLabel()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.FORALL_CONSTRUCT_STMT_635)
            return (Token)((ASTLblDefNode)getChild(0)).getLabel();
        else if (getProduction() == Production.FORALL_CONSTRUCT_STMT_636)
            return (Token)((ASTLblDefNode)getChild(0)).getLabel();
        else if (getProduction() == Production.FORALL_CONSTRUCT_STMT_ERROR_3)
            return (Token)((ASTLblDefNode)getChild(0)).getLabel();
        else if (getProduction() == Production.FORALL_CONSTRUCT_STMT_ERROR_4)
            return (Token)((ASTLblDefNode)getChild(0)).getLabel();
        else
            return null;
    }

    public boolean hasLabel()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.FORALL_CONSTRUCT_STMT_635)
            return ((ASTLblDefNode)getChild(0)).hasLabel();
        else if (getProduction() == Production.FORALL_CONSTRUCT_STMT_636)
            return ((ASTLblDefNode)getChild(0)).hasLabel();
        else if (getProduction() == Production.FORALL_CONSTRUCT_STMT_ERROR_3)
            return ((ASTLblDefNode)getChild(0)).hasLabel();
        else if (getProduction() == Production.FORALL_CONSTRUCT_STMT_ERROR_4)
            return ((ASTLblDefNode)getChild(0)).hasLabel();
        else
            return false;
    }

    public ASTForallTripletSpecListNode getForallTripletSpecList()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.FORALL_CONSTRUCT_STMT_635)
            return (ASTForallTripletSpecListNode)((ASTForallHeaderNode)getChild(2)).getForallTripletSpecList();
        else if (getProduction() == Production.FORALL_CONSTRUCT_STMT_636)
            return (ASTForallTripletSpecListNode)((ASTForallHeaderNode)getChild(4)).getForallTripletSpecList();
        else
            return null;
    }

    public ASTScalarMaskExprNode getScalarMaskExpr()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.FORALL_CONSTRUCT_STMT_635)
            return (ASTScalarMaskExprNode)((ASTForallHeaderNode)getChild(2)).getScalarMaskExpr();
        else if (getProduction() == Production.FORALL_CONSTRUCT_STMT_636)
            return (ASTScalarMaskExprNode)((ASTForallHeaderNode)getChild(4)).getScalarMaskExpr();
        else
            return null;
    }

    public boolean hasScalarMaskExpr()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.FORALL_CONSTRUCT_STMT_635)
            return ((ASTForallHeaderNode)getChild(2)).hasScalarMaskExpr();
        else if (getProduction() == Production.FORALL_CONSTRUCT_STMT_636)
            return ((ASTForallHeaderNode)getChild(4)).hasScalarMaskExpr();
        else
            return false;
    }

    public Token getName()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.FORALL_CONSTRUCT_STMT_636)
            return (Token)((ASTNameNode)getChild(1)).getName();
        else if (getProduction() == Production.FORALL_CONSTRUCT_STMT_ERROR_4)
            return (Token)((ASTNameNode)getChild(1)).getName();
        else
            return null;
    }

    @Override protected boolean shouldVisitChild(int index)
    {
        if (getProduction() == Production.FORALL_CONSTRUCT_STMT_635 && index == 1)
            return false;
        else if (getProduction() == Production.FORALL_CONSTRUCT_STMT_635 && index == 3)
            return false;
        else if (getProduction() == Production.FORALL_CONSTRUCT_STMT_636 && index == 2)
            return false;
        else if (getProduction() == Production.FORALL_CONSTRUCT_STMT_636 && index == 3)
            return false;
        else if (getProduction() == Production.FORALL_CONSTRUCT_STMT_636 && index == 5)
            return false;
        else if (getProduction() == Production.FORALL_CONSTRUCT_STMT_ERROR_3 && index == 1)
            return false;
        else if (getProduction() == Production.FORALL_CONSTRUCT_STMT_ERROR_4 && index == 2)
            return false;
        else if (getProduction() == Production.FORALL_CONSTRUCT_STMT_ERROR_4 && index == 3)
            return false;
        else
            return true;
    }

    @Override protected boolean childIsPulledUp(int index)
    {
        if (getProduction() == Production.FORALL_CONSTRUCT_STMT_635 && index == 0)
            return true;
        else if (getProduction() == Production.FORALL_CONSTRUCT_STMT_635 && index == 2)
            return true;
        else if (getProduction() == Production.FORALL_CONSTRUCT_STMT_636 && index == 0)
            return true;
        else if (getProduction() == Production.FORALL_CONSTRUCT_STMT_636 && index == 1)
            return true;
        else if (getProduction() == Production.FORALL_CONSTRUCT_STMT_636 && index == 4)
            return true;
        else if (getProduction() == Production.FORALL_CONSTRUCT_STMT_ERROR_3 && index == 0)
            return true;
        else if (getProduction() == Production.FORALL_CONSTRUCT_STMT_ERROR_4 && index == 0)
            return true;
        else if (getProduction() == Production.FORALL_CONSTRUCT_STMT_ERROR_4 && index == 1)
            return true;
        else
            return false;
    }
}
