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

public class ASTAssignmentStmtNode extends InteriorNodeWithErrorRecoverySymbols implements IActionStmt, IForallBodyConstruct, IWhereBodyConstruct
{
    ASTAssignmentStmtNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
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
        visitor.visitIActionStmt(this);
        visitor.visitIForallBodyConstruct(this);
        visitor.visitIWhereBodyConstruct(this);
        visitor.visitASTAssignmentStmtNode(this);
    }

    public ASTExpressionNode getRhs()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.ASSIGNMENT_STMT_556)
            return (ASTExpressionNode)getChild(3);
        else if (getProduction() == Production.ASSIGNMENT_STMT_557)
            return (ASTExpressionNode)getChild(6);
        else if (getProduction() == Production.ASSIGNMENT_STMT_558)
            return (ASTExpressionNode)getChild(7);
        else if (getProduction() == Production.ASSIGNMENT_STMT_559)
            return (ASTExpressionNode)getChild(7);
        else if (getProduction() == Production.ASSIGNMENT_STMT_560)
            return (ASTExpressionNode)getChild(5);
        else if (getProduction() == Production.ASSIGNMENT_STMT_561)
            return (ASTExpressionNode)getChild(8);
        else if (getProduction() == Production.ASSIGNMENT_STMT_562)
            return (ASTExpressionNode)getChild(9);
        else if (getProduction() == Production.ASSIGNMENT_STMT_563)
            return (ASTExpressionNode)getChild(8);
        else if (getProduction() == Production.ASSIGNMENT_STMT_564)
            return (ASTExpressionNode)getChild(11);
        else if (getProduction() == Production.ASSIGNMENT_STMT_565)
            return (ASTExpressionNode)getChild(12);
        else if (getProduction() == Production.ASSIGNMENT_STMT_566)
            return (ASTExpressionNode)getChild(8);
        else if (getProduction() == Production.ASSIGNMENT_STMT_567)
            return (ASTExpressionNode)getChild(11);
        else if (getProduction() == Production.ASSIGNMENT_STMT_568)
            return (ASTExpressionNode)getChild(12);
        else
            return null;
    }

    public ASTSFExprListNode getLhsExprList()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.ASSIGNMENT_STMT_557)
            return (ASTSFExprListNode)getChild(3);
        else if (getProduction() == Production.ASSIGNMENT_STMT_558)
            return (ASTSFExprListNode)getChild(3);
        else if (getProduction() == Production.ASSIGNMENT_STMT_563)
            return (ASTSFExprListNode)getChild(3);
        else if (getProduction() == Production.ASSIGNMENT_STMT_564)
            return (ASTSFExprListNode)getChild(3);
        else if (getProduction() == Production.ASSIGNMENT_STMT_565)
            return (ASTSFExprListNode)getChild(3);
        else if (getProduction() == Production.POINTER_ASSIGNMENT_STMT_595)
            return (ASTSFExprListNode)getChild(3);
        else
            return null;
    }

    public boolean hasLhsExprList()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.ASSIGNMENT_STMT_557)
            return getChild(3) != null;
        else if (getProduction() == Production.ASSIGNMENT_STMT_558)
            return getChild(3) != null;
        else if (getProduction() == Production.ASSIGNMENT_STMT_563)
            return getChild(3) != null;
        else if (getProduction() == Production.ASSIGNMENT_STMT_564)
            return getChild(3) != null;
        else if (getProduction() == Production.ASSIGNMENT_STMT_565)
            return getChild(3) != null;
        else if (getProduction() == Production.POINTER_ASSIGNMENT_STMT_595)
            return getChild(3) != null;
        else
            return false;
    }

    public ASTSubstringRangeNode getSubstringRange()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.ASSIGNMENT_STMT_558)
            return (ASTSubstringRangeNode)getChild(5);
        else if (getProduction() == Production.ASSIGNMENT_STMT_559)
            return (ASTSubstringRangeNode)getChild(5);
        else if (getProduction() == Production.ASSIGNMENT_STMT_562)
            return (ASTSubstringRangeNode)getChild(7);
        else if (getProduction() == Production.ASSIGNMENT_STMT_565)
            return (ASTSubstringRangeNode)getChild(10);
        else if (getProduction() == Production.ASSIGNMENT_STMT_568)
            return (ASTSubstringRangeNode)getChild(10);
        else
            return null;
    }

    public boolean hasSubstringRange()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.ASSIGNMENT_STMT_558)
            return getChild(5) != null;
        else if (getProduction() == Production.ASSIGNMENT_STMT_559)
            return getChild(5) != null;
        else if (getProduction() == Production.ASSIGNMENT_STMT_562)
            return getChild(7) != null;
        else if (getProduction() == Production.ASSIGNMENT_STMT_565)
            return getChild(10) != null;
        else if (getProduction() == Production.ASSIGNMENT_STMT_568)
            return getChild(10) != null;
        else
            return false;
    }

    public ASTSFDummyArgNameListNode getLhsNameList()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.ASSIGNMENT_STMT_559)
            return (ASTSFDummyArgNameListNode)getChild(3);
        else if (getProduction() == Production.ASSIGNMENT_STMT_566)
            return (ASTSFDummyArgNameListNode)getChild(3);
        else if (getProduction() == Production.ASSIGNMENT_STMT_567)
            return (ASTSFDummyArgNameListNode)getChild(3);
        else if (getProduction() == Production.ASSIGNMENT_STMT_568)
            return (ASTSFDummyArgNameListNode)getChild(3);
        else if (getProduction() == Production.POINTER_ASSIGNMENT_STMT_596)
            return (ASTSFDummyArgNameListNode)getChild(3);
        else
            return null;
    }

    public boolean hasLhsNameList()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.ASSIGNMENT_STMT_559)
            return getChild(3) != null;
        else if (getProduction() == Production.ASSIGNMENT_STMT_566)
            return getChild(3) != null;
        else if (getProduction() == Production.ASSIGNMENT_STMT_567)
            return getChild(3) != null;
        else if (getProduction() == Production.ASSIGNMENT_STMT_568)
            return getChild(3) != null;
        else if (getProduction() == Production.POINTER_ASSIGNMENT_STMT_596)
            return getChild(3) != null;
        else
            return false;
    }

    public ASTDataRefNode getDerivedTypeComponentRef()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.ASSIGNMENT_STMT_560)
            return (ASTDataRefNode)getChild(3);
        else if (getProduction() == Production.ASSIGNMENT_STMT_561)
            return (ASTDataRefNode)getChild(3);
        else if (getProduction() == Production.ASSIGNMENT_STMT_562)
            return (ASTDataRefNode)getChild(3);
        else if (getProduction() == Production.ASSIGNMENT_STMT_563)
            return (ASTDataRefNode)getChild(6);
        else if (getProduction() == Production.ASSIGNMENT_STMT_564)
            return (ASTDataRefNode)getChild(6);
        else if (getProduction() == Production.ASSIGNMENT_STMT_565)
            return (ASTDataRefNode)getChild(6);
        else if (getProduction() == Production.ASSIGNMENT_STMT_566)
            return (ASTDataRefNode)getChild(6);
        else if (getProduction() == Production.ASSIGNMENT_STMT_567)
            return (ASTDataRefNode)getChild(6);
        else if (getProduction() == Production.ASSIGNMENT_STMT_568)
            return (ASTDataRefNode)getChild(6);
        else if (getProduction() == Production.POINTER_ASSIGNMENT_STMT_594)
            return (ASTDataRefNode)getChild(3);
        else if (getProduction() == Production.POINTER_ASSIGNMENT_STMT_595)
            return (ASTDataRefNode)getChild(6);
        else if (getProduction() == Production.POINTER_ASSIGNMENT_STMT_596)
            return (ASTDataRefNode)getChild(6);
        else
            return null;
    }

    public boolean hasDerivedTypeComponentRef()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.ASSIGNMENT_STMT_560)
            return getChild(3) != null;
        else if (getProduction() == Production.ASSIGNMENT_STMT_561)
            return getChild(3) != null;
        else if (getProduction() == Production.ASSIGNMENT_STMT_562)
            return getChild(3) != null;
        else if (getProduction() == Production.ASSIGNMENT_STMT_563)
            return getChild(6) != null;
        else if (getProduction() == Production.ASSIGNMENT_STMT_564)
            return getChild(6) != null;
        else if (getProduction() == Production.ASSIGNMENT_STMT_565)
            return getChild(6) != null;
        else if (getProduction() == Production.ASSIGNMENT_STMT_566)
            return getChild(6) != null;
        else if (getProduction() == Production.ASSIGNMENT_STMT_567)
            return getChild(6) != null;
        else if (getProduction() == Production.ASSIGNMENT_STMT_568)
            return getChild(6) != null;
        else if (getProduction() == Production.POINTER_ASSIGNMENT_STMT_594)
            return getChild(3) != null;
        else if (getProduction() == Production.POINTER_ASSIGNMENT_STMT_595)
            return getChild(6) != null;
        else if (getProduction() == Production.POINTER_ASSIGNMENT_STMT_596)
            return getChild(6) != null;
        else
            return false;
    }

    public ASTSectionSubscriptListNode getComponentSectionSubscriptList()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.ASSIGNMENT_STMT_561)
            return (ASTSectionSubscriptListNode)getChild(5);
        else if (getProduction() == Production.ASSIGNMENT_STMT_562)
            return (ASTSectionSubscriptListNode)getChild(5);
        else if (getProduction() == Production.ASSIGNMENT_STMT_564)
            return (ASTSectionSubscriptListNode)getChild(8);
        else if (getProduction() == Production.ASSIGNMENT_STMT_565)
            return (ASTSectionSubscriptListNode)getChild(8);
        else if (getProduction() == Production.ASSIGNMENT_STMT_567)
            return (ASTSectionSubscriptListNode)getChild(8);
        else if (getProduction() == Production.ASSIGNMENT_STMT_568)
            return (ASTSectionSubscriptListNode)getChild(8);
        else
            return null;
    }

    public boolean hasComponentSectionSubscriptList()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.ASSIGNMENT_STMT_561)
            return getChild(5) != null;
        else if (getProduction() == Production.ASSIGNMENT_STMT_562)
            return getChild(5) != null;
        else if (getProduction() == Production.ASSIGNMENT_STMT_564)
            return getChild(8) != null;
        else if (getProduction() == Production.ASSIGNMENT_STMT_565)
            return getChild(8) != null;
        else if (getProduction() == Production.ASSIGNMENT_STMT_567)
            return getChild(8) != null;
        else if (getProduction() == Production.ASSIGNMENT_STMT_568)
            return getChild(8) != null;
        else
            return false;
    }

    public boolean isPointerAssignment()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.POINTER_ASSIGNMENT_STMT_593)
            return getChild(2) != null;
        else if (getProduction() == Production.POINTER_ASSIGNMENT_STMT_594)
            return getChild(4) != null;
        else if (getProduction() == Production.POINTER_ASSIGNMENT_STMT_595)
            return getChild(7) != null;
        else if (getProduction() == Production.POINTER_ASSIGNMENT_STMT_596)
            return getChild(7) != null;
        else
            return false;
    }

    public Token getLabel()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.ASSIGNMENT_STMT_556)
            return (Token)((ASTLblDefNode)getChild(0)).getLabel();
        else if (getProduction() == Production.ASSIGNMENT_STMT_557)
            return (Token)((ASTLblDefNode)getChild(0)).getLabel();
        else if (getProduction() == Production.ASSIGNMENT_STMT_558)
            return (Token)((ASTLblDefNode)getChild(0)).getLabel();
        else if (getProduction() == Production.ASSIGNMENT_STMT_559)
            return (Token)((ASTLblDefNode)getChild(0)).getLabel();
        else if (getProduction() == Production.ASSIGNMENT_STMT_560)
            return (Token)((ASTLblDefNode)getChild(0)).getLabel();
        else if (getProduction() == Production.ASSIGNMENT_STMT_561)
            return (Token)((ASTLblDefNode)getChild(0)).getLabel();
        else if (getProduction() == Production.ASSIGNMENT_STMT_562)
            return (Token)((ASTLblDefNode)getChild(0)).getLabel();
        else if (getProduction() == Production.ASSIGNMENT_STMT_563)
            return (Token)((ASTLblDefNode)getChild(0)).getLabel();
        else if (getProduction() == Production.ASSIGNMENT_STMT_564)
            return (Token)((ASTLblDefNode)getChild(0)).getLabel();
        else if (getProduction() == Production.ASSIGNMENT_STMT_565)
            return (Token)((ASTLblDefNode)getChild(0)).getLabel();
        else if (getProduction() == Production.ASSIGNMENT_STMT_566)
            return (Token)((ASTLblDefNode)getChild(0)).getLabel();
        else if (getProduction() == Production.ASSIGNMENT_STMT_567)
            return (Token)((ASTLblDefNode)getChild(0)).getLabel();
        else if (getProduction() == Production.ASSIGNMENT_STMT_568)
            return (Token)((ASTLblDefNode)getChild(0)).getLabel();
        else if (getProduction() == Production.ASSIGNMENT_STMT_ERROR_2)
            return (Token)((ASTLblDefNode)getChild(0)).getLabel();
        else if (getProduction() == Production.POINTER_ASSIGNMENT_STMT_593)
            return (Token)((ASTLblDefNode)getChild(0)).getLabel();
        else if (getProduction() == Production.POINTER_ASSIGNMENT_STMT_594)
            return (Token)((ASTLblDefNode)getChild(0)).getLabel();
        else if (getProduction() == Production.POINTER_ASSIGNMENT_STMT_595)
            return (Token)((ASTLblDefNode)getChild(0)).getLabel();
        else if (getProduction() == Production.POINTER_ASSIGNMENT_STMT_596)
            return (Token)((ASTLblDefNode)getChild(0)).getLabel();
        else
            return null;
    }

    public boolean hasLabel()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.ASSIGNMENT_STMT_556)
            return ((ASTLblDefNode)getChild(0)).hasLabel();
        else if (getProduction() == Production.ASSIGNMENT_STMT_557)
            return ((ASTLblDefNode)getChild(0)).hasLabel();
        else if (getProduction() == Production.ASSIGNMENT_STMT_558)
            return ((ASTLblDefNode)getChild(0)).hasLabel();
        else if (getProduction() == Production.ASSIGNMENT_STMT_559)
            return ((ASTLblDefNode)getChild(0)).hasLabel();
        else if (getProduction() == Production.ASSIGNMENT_STMT_560)
            return ((ASTLblDefNode)getChild(0)).hasLabel();
        else if (getProduction() == Production.ASSIGNMENT_STMT_561)
            return ((ASTLblDefNode)getChild(0)).hasLabel();
        else if (getProduction() == Production.ASSIGNMENT_STMT_562)
            return ((ASTLblDefNode)getChild(0)).hasLabel();
        else if (getProduction() == Production.ASSIGNMENT_STMT_563)
            return ((ASTLblDefNode)getChild(0)).hasLabel();
        else if (getProduction() == Production.ASSIGNMENT_STMT_564)
            return ((ASTLblDefNode)getChild(0)).hasLabel();
        else if (getProduction() == Production.ASSIGNMENT_STMT_565)
            return ((ASTLblDefNode)getChild(0)).hasLabel();
        else if (getProduction() == Production.ASSIGNMENT_STMT_566)
            return ((ASTLblDefNode)getChild(0)).hasLabel();
        else if (getProduction() == Production.ASSIGNMENT_STMT_567)
            return ((ASTLblDefNode)getChild(0)).hasLabel();
        else if (getProduction() == Production.ASSIGNMENT_STMT_568)
            return ((ASTLblDefNode)getChild(0)).hasLabel();
        else if (getProduction() == Production.ASSIGNMENT_STMT_ERROR_2)
            return ((ASTLblDefNode)getChild(0)).hasLabel();
        else if (getProduction() == Production.POINTER_ASSIGNMENT_STMT_593)
            return ((ASTLblDefNode)getChild(0)).hasLabel();
        else if (getProduction() == Production.POINTER_ASSIGNMENT_STMT_594)
            return ((ASTLblDefNode)getChild(0)).hasLabel();
        else if (getProduction() == Production.POINTER_ASSIGNMENT_STMT_595)
            return ((ASTLblDefNode)getChild(0)).hasLabel();
        else if (getProduction() == Production.POINTER_ASSIGNMENT_STMT_596)
            return ((ASTLblDefNode)getChild(0)).hasLabel();
        else
            return false;
    }

    public Token getLhsVariableName()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.ASSIGNMENT_STMT_556)
            return (Token)((ASTNameNode)getChild(1)).getName();
        else if (getProduction() == Production.ASSIGNMENT_STMT_557)
            return (Token)((ASTNameNode)getChild(1)).getName();
        else if (getProduction() == Production.ASSIGNMENT_STMT_558)
            return (Token)((ASTNameNode)getChild(1)).getName();
        else if (getProduction() == Production.ASSIGNMENT_STMT_559)
            return (Token)((ASTNameNode)getChild(1)).getName();
        else if (getProduction() == Production.ASSIGNMENT_STMT_560)
            return (Token)((ASTNameNode)getChild(1)).getName();
        else if (getProduction() == Production.ASSIGNMENT_STMT_561)
            return (Token)((ASTNameNode)getChild(1)).getName();
        else if (getProduction() == Production.ASSIGNMENT_STMT_562)
            return (Token)((ASTNameNode)getChild(1)).getName();
        else if (getProduction() == Production.ASSIGNMENT_STMT_563)
            return (Token)((ASTNameNode)getChild(1)).getName();
        else if (getProduction() == Production.ASSIGNMENT_STMT_564)
            return (Token)((ASTNameNode)getChild(1)).getName();
        else if (getProduction() == Production.ASSIGNMENT_STMT_565)
            return (Token)((ASTNameNode)getChild(1)).getName();
        else if (getProduction() == Production.ASSIGNMENT_STMT_566)
            return (Token)((ASTNameNode)getChild(1)).getName();
        else if (getProduction() == Production.ASSIGNMENT_STMT_567)
            return (Token)((ASTNameNode)getChild(1)).getName();
        else if (getProduction() == Production.ASSIGNMENT_STMT_568)
            return (Token)((ASTNameNode)getChild(1)).getName();
        else if (getProduction() == Production.ASSIGNMENT_STMT_ERROR_2)
            return (Token)((ASTNameNode)getChild(1)).getName();
        else if (getProduction() == Production.POINTER_ASSIGNMENT_STMT_593)
            return (Token)((ASTNameNode)getChild(1)).getName();
        else if (getProduction() == Production.POINTER_ASSIGNMENT_STMT_594)
            return (Token)((ASTNameNode)getChild(1)).getName();
        else if (getProduction() == Production.POINTER_ASSIGNMENT_STMT_595)
            return (Token)((ASTNameNode)getChild(1)).getName();
        else if (getProduction() == Production.POINTER_ASSIGNMENT_STMT_596)
            return (Token)((ASTNameNode)getChild(1)).getName();
        else
            return null;
    }

    public ASTExpressionNode getExpr()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.POINTER_ASSIGNMENT_STMT_593)
            return (ASTExpressionNode)((ASTTargetNode)getChild(3)).getExpr();
        else if (getProduction() == Production.POINTER_ASSIGNMENT_STMT_594)
            return (ASTExpressionNode)((ASTTargetNode)getChild(5)).getExpr();
        else if (getProduction() == Production.POINTER_ASSIGNMENT_STMT_595)
            return (ASTExpressionNode)((ASTTargetNode)getChild(8)).getExpr();
        else if (getProduction() == Production.POINTER_ASSIGNMENT_STMT_596)
            return (ASTExpressionNode)((ASTTargetNode)getChild(8)).getExpr();
        else
            return null;
    }

    public boolean hasExpr()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.POINTER_ASSIGNMENT_STMT_593)
            return ((ASTTargetNode)getChild(3)).hasExpr();
        else if (getProduction() == Production.POINTER_ASSIGNMENT_STMT_594)
            return ((ASTTargetNode)getChild(5)).hasExpr();
        else if (getProduction() == Production.POINTER_ASSIGNMENT_STMT_595)
            return ((ASTTargetNode)getChild(8)).hasExpr();
        else if (getProduction() == Production.POINTER_ASSIGNMENT_STMT_596)
            return ((ASTTargetNode)getChild(8)).hasExpr();
        else
            return false;
    }

    public boolean isNull()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.POINTER_ASSIGNMENT_STMT_593)
            return ((ASTTargetNode)getChild(3)).isNull();
        else if (getProduction() == Production.POINTER_ASSIGNMENT_STMT_594)
            return ((ASTTargetNode)getChild(5)).isNull();
        else if (getProduction() == Production.POINTER_ASSIGNMENT_STMT_595)
            return ((ASTTargetNode)getChild(8)).isNull();
        else if (getProduction() == Production.POINTER_ASSIGNMENT_STMT_596)
            return ((ASTTargetNode)getChild(8)).isNull();
        else
            return false;
    }

    @Override protected boolean shouldVisitChild(int index)
    {
        if (getProduction() == Production.POINTER_ASSIGNMENT_STMT_593 && index == 4)
            return false;
        else if (getProduction() == Production.POINTER_ASSIGNMENT_STMT_594 && index == 2)
            return false;
        else if (getProduction() == Production.POINTER_ASSIGNMENT_STMT_594 && index == 6)
            return false;
        else if (getProduction() == Production.POINTER_ASSIGNMENT_STMT_595 && index == 2)
            return false;
        else if (getProduction() == Production.POINTER_ASSIGNMENT_STMT_595 && index == 4)
            return false;
        else if (getProduction() == Production.POINTER_ASSIGNMENT_STMT_595 && index == 5)
            return false;
        else if (getProduction() == Production.POINTER_ASSIGNMENT_STMT_595 && index == 9)
            return false;
        else if (getProduction() == Production.POINTER_ASSIGNMENT_STMT_596 && index == 2)
            return false;
        else if (getProduction() == Production.POINTER_ASSIGNMENT_STMT_596 && index == 4)
            return false;
        else if (getProduction() == Production.POINTER_ASSIGNMENT_STMT_596 && index == 5)
            return false;
        else if (getProduction() == Production.POINTER_ASSIGNMENT_STMT_596 && index == 9)
            return false;
        else if (getProduction() == Production.ASSIGNMENT_STMT_556 && index == 2)
            return false;
        else if (getProduction() == Production.ASSIGNMENT_STMT_556 && index == 4)
            return false;
        else if (getProduction() == Production.ASSIGNMENT_STMT_557 && index == 2)
            return false;
        else if (getProduction() == Production.ASSIGNMENT_STMT_557 && index == 4)
            return false;
        else if (getProduction() == Production.ASSIGNMENT_STMT_557 && index == 5)
            return false;
        else if (getProduction() == Production.ASSIGNMENT_STMT_557 && index == 7)
            return false;
        else if (getProduction() == Production.ASSIGNMENT_STMT_558 && index == 2)
            return false;
        else if (getProduction() == Production.ASSIGNMENT_STMT_558 && index == 4)
            return false;
        else if (getProduction() == Production.ASSIGNMENT_STMT_558 && index == 6)
            return false;
        else if (getProduction() == Production.ASSIGNMENT_STMT_558 && index == 8)
            return false;
        else if (getProduction() == Production.ASSIGNMENT_STMT_559 && index == 2)
            return false;
        else if (getProduction() == Production.ASSIGNMENT_STMT_559 && index == 4)
            return false;
        else if (getProduction() == Production.ASSIGNMENT_STMT_559 && index == 6)
            return false;
        else if (getProduction() == Production.ASSIGNMENT_STMT_559 && index == 8)
            return false;
        else if (getProduction() == Production.ASSIGNMENT_STMT_560 && index == 2)
            return false;
        else if (getProduction() == Production.ASSIGNMENT_STMT_560 && index == 4)
            return false;
        else if (getProduction() == Production.ASSIGNMENT_STMT_560 && index == 6)
            return false;
        else if (getProduction() == Production.ASSIGNMENT_STMT_561 && index == 2)
            return false;
        else if (getProduction() == Production.ASSIGNMENT_STMT_561 && index == 4)
            return false;
        else if (getProduction() == Production.ASSIGNMENT_STMT_561 && index == 6)
            return false;
        else if (getProduction() == Production.ASSIGNMENT_STMT_561 && index == 7)
            return false;
        else if (getProduction() == Production.ASSIGNMENT_STMT_561 && index == 9)
            return false;
        else if (getProduction() == Production.ASSIGNMENT_STMT_562 && index == 2)
            return false;
        else if (getProduction() == Production.ASSIGNMENT_STMT_562 && index == 4)
            return false;
        else if (getProduction() == Production.ASSIGNMENT_STMT_562 && index == 6)
            return false;
        else if (getProduction() == Production.ASSIGNMENT_STMT_562 && index == 8)
            return false;
        else if (getProduction() == Production.ASSIGNMENT_STMT_562 && index == 10)
            return false;
        else if (getProduction() == Production.ASSIGNMENT_STMT_563 && index == 2)
            return false;
        else if (getProduction() == Production.ASSIGNMENT_STMT_563 && index == 4)
            return false;
        else if (getProduction() == Production.ASSIGNMENT_STMT_563 && index == 5)
            return false;
        else if (getProduction() == Production.ASSIGNMENT_STMT_563 && index == 7)
            return false;
        else if (getProduction() == Production.ASSIGNMENT_STMT_563 && index == 9)
            return false;
        else if (getProduction() == Production.ASSIGNMENT_STMT_564 && index == 2)
            return false;
        else if (getProduction() == Production.ASSIGNMENT_STMT_564 && index == 4)
            return false;
        else if (getProduction() == Production.ASSIGNMENT_STMT_564 && index == 5)
            return false;
        else if (getProduction() == Production.ASSIGNMENT_STMT_564 && index == 7)
            return false;
        else if (getProduction() == Production.ASSIGNMENT_STMT_564 && index == 9)
            return false;
        else if (getProduction() == Production.ASSIGNMENT_STMT_564 && index == 10)
            return false;
        else if (getProduction() == Production.ASSIGNMENT_STMT_564 && index == 12)
            return false;
        else if (getProduction() == Production.ASSIGNMENT_STMT_565 && index == 2)
            return false;
        else if (getProduction() == Production.ASSIGNMENT_STMT_565 && index == 4)
            return false;
        else if (getProduction() == Production.ASSIGNMENT_STMT_565 && index == 5)
            return false;
        else if (getProduction() == Production.ASSIGNMENT_STMT_565 && index == 7)
            return false;
        else if (getProduction() == Production.ASSIGNMENT_STMT_565 && index == 9)
            return false;
        else if (getProduction() == Production.ASSIGNMENT_STMT_565 && index == 11)
            return false;
        else if (getProduction() == Production.ASSIGNMENT_STMT_565 && index == 13)
            return false;
        else if (getProduction() == Production.ASSIGNMENT_STMT_566 && index == 2)
            return false;
        else if (getProduction() == Production.ASSIGNMENT_STMT_566 && index == 4)
            return false;
        else if (getProduction() == Production.ASSIGNMENT_STMT_566 && index == 5)
            return false;
        else if (getProduction() == Production.ASSIGNMENT_STMT_566 && index == 7)
            return false;
        else if (getProduction() == Production.ASSIGNMENT_STMT_566 && index == 9)
            return false;
        else if (getProduction() == Production.ASSIGNMENT_STMT_567 && index == 2)
            return false;
        else if (getProduction() == Production.ASSIGNMENT_STMT_567 && index == 4)
            return false;
        else if (getProduction() == Production.ASSIGNMENT_STMT_567 && index == 5)
            return false;
        else if (getProduction() == Production.ASSIGNMENT_STMT_567 && index == 7)
            return false;
        else if (getProduction() == Production.ASSIGNMENT_STMT_567 && index == 9)
            return false;
        else if (getProduction() == Production.ASSIGNMENT_STMT_567 && index == 10)
            return false;
        else if (getProduction() == Production.ASSIGNMENT_STMT_567 && index == 12)
            return false;
        else if (getProduction() == Production.ASSIGNMENT_STMT_568 && index == 2)
            return false;
        else if (getProduction() == Production.ASSIGNMENT_STMT_568 && index == 4)
            return false;
        else if (getProduction() == Production.ASSIGNMENT_STMT_568 && index == 5)
            return false;
        else if (getProduction() == Production.ASSIGNMENT_STMT_568 && index == 7)
            return false;
        else if (getProduction() == Production.ASSIGNMENT_STMT_568 && index == 9)
            return false;
        else if (getProduction() == Production.ASSIGNMENT_STMT_568 && index == 11)
            return false;
        else if (getProduction() == Production.ASSIGNMENT_STMT_568 && index == 13)
            return false;
        else if (getProduction() == Production.POINTER_ASSIGNMENT_STMT_593 && index == 4)
            return false;
        else if (getProduction() == Production.POINTER_ASSIGNMENT_STMT_594 && index == 2)
            return false;
        else if (getProduction() == Production.POINTER_ASSIGNMENT_STMT_594 && index == 6)
            return false;
        else if (getProduction() == Production.POINTER_ASSIGNMENT_STMT_595 && index == 2)
            return false;
        else if (getProduction() == Production.POINTER_ASSIGNMENT_STMT_595 && index == 4)
            return false;
        else if (getProduction() == Production.POINTER_ASSIGNMENT_STMT_595 && index == 5)
            return false;
        else if (getProduction() == Production.POINTER_ASSIGNMENT_STMT_595 && index == 9)
            return false;
        else if (getProduction() == Production.POINTER_ASSIGNMENT_STMT_596 && index == 2)
            return false;
        else if (getProduction() == Production.POINTER_ASSIGNMENT_STMT_596 && index == 4)
            return false;
        else if (getProduction() == Production.POINTER_ASSIGNMENT_STMT_596 && index == 5)
            return false;
        else if (getProduction() == Production.POINTER_ASSIGNMENT_STMT_596 && index == 9)
            return false;
        else if (getProduction() == Production.ASSIGNMENT_STMT_556 && index == 2)
            return false;
        else if (getProduction() == Production.ASSIGNMENT_STMT_556 && index == 4)
            return false;
        else if (getProduction() == Production.ASSIGNMENT_STMT_557 && index == 2)
            return false;
        else if (getProduction() == Production.ASSIGNMENT_STMT_557 && index == 4)
            return false;
        else if (getProduction() == Production.ASSIGNMENT_STMT_557 && index == 5)
            return false;
        else if (getProduction() == Production.ASSIGNMENT_STMT_557 && index == 7)
            return false;
        else if (getProduction() == Production.ASSIGNMENT_STMT_558 && index == 2)
            return false;
        else if (getProduction() == Production.ASSIGNMENT_STMT_558 && index == 4)
            return false;
        else if (getProduction() == Production.ASSIGNMENT_STMT_558 && index == 6)
            return false;
        else if (getProduction() == Production.ASSIGNMENT_STMT_558 && index == 8)
            return false;
        else if (getProduction() == Production.ASSIGNMENT_STMT_559 && index == 2)
            return false;
        else if (getProduction() == Production.ASSIGNMENT_STMT_559 && index == 4)
            return false;
        else if (getProduction() == Production.ASSIGNMENT_STMT_559 && index == 6)
            return false;
        else if (getProduction() == Production.ASSIGNMENT_STMT_559 && index == 8)
            return false;
        else if (getProduction() == Production.ASSIGNMENT_STMT_560 && index == 2)
            return false;
        else if (getProduction() == Production.ASSIGNMENT_STMT_560 && index == 4)
            return false;
        else if (getProduction() == Production.ASSIGNMENT_STMT_560 && index == 6)
            return false;
        else if (getProduction() == Production.ASSIGNMENT_STMT_561 && index == 2)
            return false;
        else if (getProduction() == Production.ASSIGNMENT_STMT_561 && index == 4)
            return false;
        else if (getProduction() == Production.ASSIGNMENT_STMT_561 && index == 6)
            return false;
        else if (getProduction() == Production.ASSIGNMENT_STMT_561 && index == 7)
            return false;
        else if (getProduction() == Production.ASSIGNMENT_STMT_561 && index == 9)
            return false;
        else if (getProduction() == Production.ASSIGNMENT_STMT_562 && index == 2)
            return false;
        else if (getProduction() == Production.ASSIGNMENT_STMT_562 && index == 4)
            return false;
        else if (getProduction() == Production.ASSIGNMENT_STMT_562 && index == 6)
            return false;
        else if (getProduction() == Production.ASSIGNMENT_STMT_562 && index == 8)
            return false;
        else if (getProduction() == Production.ASSIGNMENT_STMT_562 && index == 10)
            return false;
        else if (getProduction() == Production.ASSIGNMENT_STMT_563 && index == 2)
            return false;
        else if (getProduction() == Production.ASSIGNMENT_STMT_563 && index == 4)
            return false;
        else if (getProduction() == Production.ASSIGNMENT_STMT_563 && index == 5)
            return false;
        else if (getProduction() == Production.ASSIGNMENT_STMT_563 && index == 7)
            return false;
        else if (getProduction() == Production.ASSIGNMENT_STMT_563 && index == 9)
            return false;
        else if (getProduction() == Production.ASSIGNMENT_STMT_564 && index == 2)
            return false;
        else if (getProduction() == Production.ASSIGNMENT_STMT_564 && index == 4)
            return false;
        else if (getProduction() == Production.ASSIGNMENT_STMT_564 && index == 5)
            return false;
        else if (getProduction() == Production.ASSIGNMENT_STMT_564 && index == 7)
            return false;
        else if (getProduction() == Production.ASSIGNMENT_STMT_564 && index == 9)
            return false;
        else if (getProduction() == Production.ASSIGNMENT_STMT_564 && index == 10)
            return false;
        else if (getProduction() == Production.ASSIGNMENT_STMT_564 && index == 12)
            return false;
        else if (getProduction() == Production.ASSIGNMENT_STMT_565 && index == 2)
            return false;
        else if (getProduction() == Production.ASSIGNMENT_STMT_565 && index == 4)
            return false;
        else if (getProduction() == Production.ASSIGNMENT_STMT_565 && index == 5)
            return false;
        else if (getProduction() == Production.ASSIGNMENT_STMT_565 && index == 7)
            return false;
        else if (getProduction() == Production.ASSIGNMENT_STMT_565 && index == 9)
            return false;
        else if (getProduction() == Production.ASSIGNMENT_STMT_565 && index == 11)
            return false;
        else if (getProduction() == Production.ASSIGNMENT_STMT_565 && index == 13)
            return false;
        else if (getProduction() == Production.ASSIGNMENT_STMT_566 && index == 2)
            return false;
        else if (getProduction() == Production.ASSIGNMENT_STMT_566 && index == 4)
            return false;
        else if (getProduction() == Production.ASSIGNMENT_STMT_566 && index == 5)
            return false;
        else if (getProduction() == Production.ASSIGNMENT_STMT_566 && index == 7)
            return false;
        else if (getProduction() == Production.ASSIGNMENT_STMT_566 && index == 9)
            return false;
        else if (getProduction() == Production.ASSIGNMENT_STMT_567 && index == 2)
            return false;
        else if (getProduction() == Production.ASSIGNMENT_STMT_567 && index == 4)
            return false;
        else if (getProduction() == Production.ASSIGNMENT_STMT_567 && index == 5)
            return false;
        else if (getProduction() == Production.ASSIGNMENT_STMT_567 && index == 7)
            return false;
        else if (getProduction() == Production.ASSIGNMENT_STMT_567 && index == 9)
            return false;
        else if (getProduction() == Production.ASSIGNMENT_STMT_567 && index == 10)
            return false;
        else if (getProduction() == Production.ASSIGNMENT_STMT_567 && index == 12)
            return false;
        else if (getProduction() == Production.ASSIGNMENT_STMT_568 && index == 2)
            return false;
        else if (getProduction() == Production.ASSIGNMENT_STMT_568 && index == 4)
            return false;
        else if (getProduction() == Production.ASSIGNMENT_STMT_568 && index == 5)
            return false;
        else if (getProduction() == Production.ASSIGNMENT_STMT_568 && index == 7)
            return false;
        else if (getProduction() == Production.ASSIGNMENT_STMT_568 && index == 9)
            return false;
        else if (getProduction() == Production.ASSIGNMENT_STMT_568 && index == 11)
            return false;
        else if (getProduction() == Production.ASSIGNMENT_STMT_568 && index == 13)
            return false;
        else
            return true;
    }

    @Override protected boolean childIsPulledUp(int index)
    {
        if (getProduction() == Production.POINTER_ASSIGNMENT_STMT_593 && index == 0)
            return true;
        else if (getProduction() == Production.POINTER_ASSIGNMENT_STMT_593 && index == 1)
            return true;
        else if (getProduction() == Production.POINTER_ASSIGNMENT_STMT_593 && index == 3)
            return true;
        else if (getProduction() == Production.POINTER_ASSIGNMENT_STMT_594 && index == 0)
            return true;
        else if (getProduction() == Production.POINTER_ASSIGNMENT_STMT_594 && index == 1)
            return true;
        else if (getProduction() == Production.POINTER_ASSIGNMENT_STMT_594 && index == 5)
            return true;
        else if (getProduction() == Production.POINTER_ASSIGNMENT_STMT_595 && index == 0)
            return true;
        else if (getProduction() == Production.POINTER_ASSIGNMENT_STMT_595 && index == 1)
            return true;
        else if (getProduction() == Production.POINTER_ASSIGNMENT_STMT_595 && index == 8)
            return true;
        else if (getProduction() == Production.POINTER_ASSIGNMENT_STMT_596 && index == 0)
            return true;
        else if (getProduction() == Production.POINTER_ASSIGNMENT_STMT_596 && index == 1)
            return true;
        else if (getProduction() == Production.POINTER_ASSIGNMENT_STMT_596 && index == 8)
            return true;
        else if (getProduction() == Production.ASSIGNMENT_STMT_556 && index == 0)
            return true;
        else if (getProduction() == Production.ASSIGNMENT_STMT_556 && index == 1)
            return true;
        else if (getProduction() == Production.ASSIGNMENT_STMT_557 && index == 0)
            return true;
        else if (getProduction() == Production.ASSIGNMENT_STMT_557 && index == 1)
            return true;
        else if (getProduction() == Production.ASSIGNMENT_STMT_558 && index == 0)
            return true;
        else if (getProduction() == Production.ASSIGNMENT_STMT_558 && index == 1)
            return true;
        else if (getProduction() == Production.ASSIGNMENT_STMT_559 && index == 0)
            return true;
        else if (getProduction() == Production.ASSIGNMENT_STMT_559 && index == 1)
            return true;
        else if (getProduction() == Production.ASSIGNMENT_STMT_560 && index == 0)
            return true;
        else if (getProduction() == Production.ASSIGNMENT_STMT_560 && index == 1)
            return true;
        else if (getProduction() == Production.ASSIGNMENT_STMT_561 && index == 0)
            return true;
        else if (getProduction() == Production.ASSIGNMENT_STMT_561 && index == 1)
            return true;
        else if (getProduction() == Production.ASSIGNMENT_STMT_562 && index == 0)
            return true;
        else if (getProduction() == Production.ASSIGNMENT_STMT_562 && index == 1)
            return true;
        else if (getProduction() == Production.ASSIGNMENT_STMT_563 && index == 0)
            return true;
        else if (getProduction() == Production.ASSIGNMENT_STMT_563 && index == 1)
            return true;
        else if (getProduction() == Production.ASSIGNMENT_STMT_564 && index == 0)
            return true;
        else if (getProduction() == Production.ASSIGNMENT_STMT_564 && index == 1)
            return true;
        else if (getProduction() == Production.ASSIGNMENT_STMT_565 && index == 0)
            return true;
        else if (getProduction() == Production.ASSIGNMENT_STMT_565 && index == 1)
            return true;
        else if (getProduction() == Production.ASSIGNMENT_STMT_566 && index == 0)
            return true;
        else if (getProduction() == Production.ASSIGNMENT_STMT_566 && index == 1)
            return true;
        else if (getProduction() == Production.ASSIGNMENT_STMT_567 && index == 0)
            return true;
        else if (getProduction() == Production.ASSIGNMENT_STMT_567 && index == 1)
            return true;
        else if (getProduction() == Production.ASSIGNMENT_STMT_568 && index == 0)
            return true;
        else if (getProduction() == Production.ASSIGNMENT_STMT_568 && index == 1)
            return true;
        else if (getProduction() == Production.ASSIGNMENT_STMT_ERROR_2 && index == 0)
            return true;
        else if (getProduction() == Production.ASSIGNMENT_STMT_ERROR_2 && index == 1)
            return true;
        else if (getProduction() == Production.POINTER_ASSIGNMENT_STMT_593 && index == 0)
            return true;
        else if (getProduction() == Production.POINTER_ASSIGNMENT_STMT_593 && index == 1)
            return true;
        else if (getProduction() == Production.POINTER_ASSIGNMENT_STMT_593 && index == 3)
            return true;
        else if (getProduction() == Production.POINTER_ASSIGNMENT_STMT_594 && index == 0)
            return true;
        else if (getProduction() == Production.POINTER_ASSIGNMENT_STMT_594 && index == 1)
            return true;
        else if (getProduction() == Production.POINTER_ASSIGNMENT_STMT_594 && index == 5)
            return true;
        else if (getProduction() == Production.POINTER_ASSIGNMENT_STMT_595 && index == 0)
            return true;
        else if (getProduction() == Production.POINTER_ASSIGNMENT_STMT_595 && index == 1)
            return true;
        else if (getProduction() == Production.POINTER_ASSIGNMENT_STMT_595 && index == 8)
            return true;
        else if (getProduction() == Production.POINTER_ASSIGNMENT_STMT_596 && index == 0)
            return true;
        else if (getProduction() == Production.POINTER_ASSIGNMENT_STMT_596 && index == 1)
            return true;
        else if (getProduction() == Production.POINTER_ASSIGNMENT_STMT_596 && index == 8)
            return true;
        else if (getProduction() == Production.ASSIGNMENT_STMT_556 && index == 0)
            return true;
        else if (getProduction() == Production.ASSIGNMENT_STMT_556 && index == 1)
            return true;
        else if (getProduction() == Production.ASSIGNMENT_STMT_557 && index == 0)
            return true;
        else if (getProduction() == Production.ASSIGNMENT_STMT_557 && index == 1)
            return true;
        else if (getProduction() == Production.ASSIGNMENT_STMT_558 && index == 0)
            return true;
        else if (getProduction() == Production.ASSIGNMENT_STMT_558 && index == 1)
            return true;
        else if (getProduction() == Production.ASSIGNMENT_STMT_559 && index == 0)
            return true;
        else if (getProduction() == Production.ASSIGNMENT_STMT_559 && index == 1)
            return true;
        else if (getProduction() == Production.ASSIGNMENT_STMT_560 && index == 0)
            return true;
        else if (getProduction() == Production.ASSIGNMENT_STMT_560 && index == 1)
            return true;
        else if (getProduction() == Production.ASSIGNMENT_STMT_561 && index == 0)
            return true;
        else if (getProduction() == Production.ASSIGNMENT_STMT_561 && index == 1)
            return true;
        else if (getProduction() == Production.ASSIGNMENT_STMT_562 && index == 0)
            return true;
        else if (getProduction() == Production.ASSIGNMENT_STMT_562 && index == 1)
            return true;
        else if (getProduction() == Production.ASSIGNMENT_STMT_563 && index == 0)
            return true;
        else if (getProduction() == Production.ASSIGNMENT_STMT_563 && index == 1)
            return true;
        else if (getProduction() == Production.ASSIGNMENT_STMT_564 && index == 0)
            return true;
        else if (getProduction() == Production.ASSIGNMENT_STMT_564 && index == 1)
            return true;
        else if (getProduction() == Production.ASSIGNMENT_STMT_565 && index == 0)
            return true;
        else if (getProduction() == Production.ASSIGNMENT_STMT_565 && index == 1)
            return true;
        else if (getProduction() == Production.ASSIGNMENT_STMT_566 && index == 0)
            return true;
        else if (getProduction() == Production.ASSIGNMENT_STMT_566 && index == 1)
            return true;
        else if (getProduction() == Production.ASSIGNMENT_STMT_567 && index == 0)
            return true;
        else if (getProduction() == Production.ASSIGNMENT_STMT_567 && index == 1)
            return true;
        else if (getProduction() == Production.ASSIGNMENT_STMT_568 && index == 0)
            return true;
        else if (getProduction() == Production.ASSIGNMENT_STMT_568 && index == 1)
            return true;
        else if (getProduction() == Production.ASSIGNMENT_STMT_ERROR_2 && index == 0)
            return true;
        else if (getProduction() == Production.ASSIGNMENT_STMT_ERROR_2 && index == 1)
            return true;
        else
            return false;
    }
}
