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

public class ASTAssignmentStmtNode extends InteriorNodeWithErrorRecoverySymbols
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
        visitor.visitASTAssignmentStmtNode(this);
    }

    public ASTLblDefNode getLblDef()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.ASSIGNMENT_STMT_558)
            return (ASTLblDefNode)getChild(0);
        else if (getProduction() == Production.ASSIGNMENT_STMT_559)
            return (ASTLblDefNode)getChild(0);
        else if (getProduction() == Production.ASSIGNMENT_STMT_560)
            return (ASTLblDefNode)getChild(0);
        else if (getProduction() == Production.ASSIGNMENT_STMT_561)
            return (ASTLblDefNode)getChild(0);
        else if (getProduction() == Production.ASSIGNMENT_STMT_562)
            return (ASTLblDefNode)getChild(0);
        else if (getProduction() == Production.ASSIGNMENT_STMT_563)
            return (ASTLblDefNode)getChild(0);
        else if (getProduction() == Production.ASSIGNMENT_STMT_564)
            return (ASTLblDefNode)getChild(0);
        else if (getProduction() == Production.ASSIGNMENT_STMT_565)
            return (ASTLblDefNode)getChild(0);
        else if (getProduction() == Production.ASSIGNMENT_STMT_566)
            return (ASTLblDefNode)getChild(0);
        else if (getProduction() == Production.ASSIGNMENT_STMT_567)
            return (ASTLblDefNode)getChild(0);
        else if (getProduction() == Production.ASSIGNMENT_STMT_568)
            return (ASTLblDefNode)getChild(0);
        else if (getProduction() == Production.ASSIGNMENT_STMT_569)
            return (ASTLblDefNode)getChild(0);
        else if (getProduction() == Production.ASSIGNMENT_STMT_570)
            return (ASTLblDefNode)getChild(0);
        else if (getProduction() == Production.ASSIGNMENT_STMT_ERROR_2)
            return (ASTLblDefNode)getChild(0);
        else
            return null;
    }

    public ASTNameNode getName()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.ASSIGNMENT_STMT_558)
            return (ASTNameNode)getChild(1);
        else if (getProduction() == Production.ASSIGNMENT_STMT_559)
            return (ASTNameNode)getChild(1);
        else if (getProduction() == Production.ASSIGNMENT_STMT_560)
            return (ASTNameNode)getChild(1);
        else if (getProduction() == Production.ASSIGNMENT_STMT_561)
            return (ASTNameNode)getChild(1);
        else if (getProduction() == Production.ASSIGNMENT_STMT_562)
            return (ASTNameNode)getChild(1);
        else if (getProduction() == Production.ASSIGNMENT_STMT_563)
            return (ASTNameNode)getChild(1);
        else if (getProduction() == Production.ASSIGNMENT_STMT_564)
            return (ASTNameNode)getChild(1);
        else if (getProduction() == Production.ASSIGNMENT_STMT_565)
            return (ASTNameNode)getChild(1);
        else if (getProduction() == Production.ASSIGNMENT_STMT_566)
            return (ASTNameNode)getChild(1);
        else if (getProduction() == Production.ASSIGNMENT_STMT_567)
            return (ASTNameNode)getChild(1);
        else if (getProduction() == Production.ASSIGNMENT_STMT_568)
            return (ASTNameNode)getChild(1);
        else if (getProduction() == Production.ASSIGNMENT_STMT_569)
            return (ASTNameNode)getChild(1);
        else if (getProduction() == Production.ASSIGNMENT_STMT_570)
            return (ASTNameNode)getChild(1);
        else if (getProduction() == Production.ASSIGNMENT_STMT_ERROR_2)
            return (ASTNameNode)getChild(1);
        else
            return null;
    }

    public Token getTEquals()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.ASSIGNMENT_STMT_558)
            return (Token)getChild(2);
        else if (getProduction() == Production.ASSIGNMENT_STMT_559)
            return (Token)getChild(5);
        else if (getProduction() == Production.ASSIGNMENT_STMT_560)
            return (Token)getChild(6);
        else if (getProduction() == Production.ASSIGNMENT_STMT_561)
            return (Token)getChild(6);
        else if (getProduction() == Production.ASSIGNMENT_STMT_562)
            return (Token)getChild(4);
        else if (getProduction() == Production.ASSIGNMENT_STMT_563)
            return (Token)getChild(7);
        else if (getProduction() == Production.ASSIGNMENT_STMT_564)
            return (Token)getChild(8);
        else if (getProduction() == Production.ASSIGNMENT_STMT_565)
            return (Token)getChild(7);
        else if (getProduction() == Production.ASSIGNMENT_STMT_566)
            return (Token)getChild(10);
        else if (getProduction() == Production.ASSIGNMENT_STMT_567)
            return (Token)getChild(11);
        else if (getProduction() == Production.ASSIGNMENT_STMT_568)
            return (Token)getChild(7);
        else if (getProduction() == Production.ASSIGNMENT_STMT_569)
            return (Token)getChild(10);
        else if (getProduction() == Production.ASSIGNMENT_STMT_570)
            return (Token)getChild(11);
        else
            return null;
    }

    public ASTExprNode getExpr()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.ASSIGNMENT_STMT_558)
            return (ASTExprNode)getChild(3);
        else if (getProduction() == Production.ASSIGNMENT_STMT_559)
            return (ASTExprNode)getChild(6);
        else if (getProduction() == Production.ASSIGNMENT_STMT_560)
            return (ASTExprNode)getChild(7);
        else if (getProduction() == Production.ASSIGNMENT_STMT_561)
            return (ASTExprNode)getChild(7);
        else if (getProduction() == Production.ASSIGNMENT_STMT_562)
            return (ASTExprNode)getChild(5);
        else if (getProduction() == Production.ASSIGNMENT_STMT_563)
            return (ASTExprNode)getChild(8);
        else if (getProduction() == Production.ASSIGNMENT_STMT_564)
            return (ASTExprNode)getChild(9);
        else if (getProduction() == Production.ASSIGNMENT_STMT_565)
            return (ASTExprNode)getChild(8);
        else if (getProduction() == Production.ASSIGNMENT_STMT_566)
            return (ASTExprNode)getChild(11);
        else if (getProduction() == Production.ASSIGNMENT_STMT_567)
            return (ASTExprNode)getChild(12);
        else if (getProduction() == Production.ASSIGNMENT_STMT_568)
            return (ASTExprNode)getChild(8);
        else if (getProduction() == Production.ASSIGNMENT_STMT_569)
            return (ASTExprNode)getChild(11);
        else if (getProduction() == Production.ASSIGNMENT_STMT_570)
            return (ASTExprNode)getChild(12);
        else
            return null;
    }

    public Token getTEos()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.ASSIGNMENT_STMT_558)
            return (Token)getChild(4);
        else if (getProduction() == Production.ASSIGNMENT_STMT_559)
            return (Token)getChild(7);
        else if (getProduction() == Production.ASSIGNMENT_STMT_560)
            return (Token)getChild(8);
        else if (getProduction() == Production.ASSIGNMENT_STMT_561)
            return (Token)getChild(8);
        else if (getProduction() == Production.ASSIGNMENT_STMT_562)
            return (Token)getChild(6);
        else if (getProduction() == Production.ASSIGNMENT_STMT_563)
            return (Token)getChild(9);
        else if (getProduction() == Production.ASSIGNMENT_STMT_564)
            return (Token)getChild(10);
        else if (getProduction() == Production.ASSIGNMENT_STMT_565)
            return (Token)getChild(9);
        else if (getProduction() == Production.ASSIGNMENT_STMT_566)
            return (Token)getChild(12);
        else if (getProduction() == Production.ASSIGNMENT_STMT_567)
            return (Token)getChild(13);
        else if (getProduction() == Production.ASSIGNMENT_STMT_568)
            return (Token)getChild(9);
        else if (getProduction() == Production.ASSIGNMENT_STMT_569)
            return (Token)getChild(12);
        else if (getProduction() == Production.ASSIGNMENT_STMT_570)
            return (Token)getChild(13);
        else
            return null;
    }

    public Token getTLparen()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.ASSIGNMENT_STMT_559)
            return (Token)getChild(2);
        else if (getProduction() == Production.ASSIGNMENT_STMT_560)
            return (Token)getChild(2);
        else if (getProduction() == Production.ASSIGNMENT_STMT_561)
            return (Token)getChild(2);
        else if (getProduction() == Production.ASSIGNMENT_STMT_563)
            return (Token)getChild(4);
        else if (getProduction() == Production.ASSIGNMENT_STMT_564)
            return (Token)getChild(4);
        else if (getProduction() == Production.ASSIGNMENT_STMT_565)
            return (Token)getChild(2);
        else if (getProduction() == Production.ASSIGNMENT_STMT_566)
            return (Token)getChild(2);
        else if (getProduction() == Production.ASSIGNMENT_STMT_567)
            return (Token)getChild(2);
        else if (getProduction() == Production.ASSIGNMENT_STMT_568)
            return (Token)getChild(2);
        else if (getProduction() == Production.ASSIGNMENT_STMT_569)
            return (Token)getChild(2);
        else if (getProduction() == Production.ASSIGNMENT_STMT_570)
            return (Token)getChild(2);
        else
            return null;
    }

    public ASTSFExprListNode getSFExprList()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.ASSIGNMENT_STMT_559)
            return (ASTSFExprListNode)getChild(3);
        else if (getProduction() == Production.ASSIGNMENT_STMT_560)
            return (ASTSFExprListNode)getChild(3);
        else if (getProduction() == Production.ASSIGNMENT_STMT_565)
            return (ASTSFExprListNode)getChild(3);
        else if (getProduction() == Production.ASSIGNMENT_STMT_566)
            return (ASTSFExprListNode)getChild(3);
        else if (getProduction() == Production.ASSIGNMENT_STMT_567)
            return (ASTSFExprListNode)getChild(3);
        else
            return null;
    }

    public Token getTRparen()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.ASSIGNMENT_STMT_559)
            return (Token)getChild(4);
        else if (getProduction() == Production.ASSIGNMENT_STMT_560)
            return (Token)getChild(4);
        else if (getProduction() == Production.ASSIGNMENT_STMT_561)
            return (Token)getChild(4);
        else if (getProduction() == Production.ASSIGNMENT_STMT_563)
            return (Token)getChild(6);
        else if (getProduction() == Production.ASSIGNMENT_STMT_564)
            return (Token)getChild(6);
        else if (getProduction() == Production.ASSIGNMENT_STMT_565)
            return (Token)getChild(4);
        else if (getProduction() == Production.ASSIGNMENT_STMT_566)
            return (Token)getChild(4);
        else if (getProduction() == Production.ASSIGNMENT_STMT_567)
            return (Token)getChild(4);
        else if (getProduction() == Production.ASSIGNMENT_STMT_568)
            return (Token)getChild(4);
        else if (getProduction() == Production.ASSIGNMENT_STMT_569)
            return (Token)getChild(4);
        else if (getProduction() == Production.ASSIGNMENT_STMT_570)
            return (Token)getChild(4);
        else
            return null;
    }

    public ASTSubstringRangeNode getSubstringRange()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.ASSIGNMENT_STMT_560)
            return (ASTSubstringRangeNode)getChild(5);
        else if (getProduction() == Production.ASSIGNMENT_STMT_561)
            return (ASTSubstringRangeNode)getChild(5);
        else if (getProduction() == Production.ASSIGNMENT_STMT_564)
            return (ASTSubstringRangeNode)getChild(7);
        else if (getProduction() == Production.ASSIGNMENT_STMT_567)
            return (ASTSubstringRangeNode)getChild(10);
        else if (getProduction() == Production.ASSIGNMENT_STMT_570)
            return (ASTSubstringRangeNode)getChild(10);
        else
            return null;
    }

    public ASTSFDummyArgNameListNode getSFDummyArgNameList()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.ASSIGNMENT_STMT_561)
            return (ASTSFDummyArgNameListNode)getChild(3);
        else if (getProduction() == Production.ASSIGNMENT_STMT_568)
            return (ASTSFDummyArgNameListNode)getChild(3);
        else if (getProduction() == Production.ASSIGNMENT_STMT_569)
            return (ASTSFDummyArgNameListNode)getChild(3);
        else if (getProduction() == Production.ASSIGNMENT_STMT_570)
            return (ASTSFDummyArgNameListNode)getChild(3);
        else
            return null;
    }

    public Token getTPercent()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.ASSIGNMENT_STMT_562)
            return (Token)getChild(2);
        else if (getProduction() == Production.ASSIGNMENT_STMT_563)
            return (Token)getChild(2);
        else if (getProduction() == Production.ASSIGNMENT_STMT_564)
            return (Token)getChild(2);
        else if (getProduction() == Production.ASSIGNMENT_STMT_565)
            return (Token)getChild(5);
        else if (getProduction() == Production.ASSIGNMENT_STMT_566)
            return (Token)getChild(5);
        else if (getProduction() == Production.ASSIGNMENT_STMT_567)
            return (Token)getChild(5);
        else if (getProduction() == Production.ASSIGNMENT_STMT_568)
            return (Token)getChild(5);
        else if (getProduction() == Production.ASSIGNMENT_STMT_569)
            return (Token)getChild(5);
        else if (getProduction() == Production.ASSIGNMENT_STMT_570)
            return (Token)getChild(5);
        else
            return null;
    }

    public ASTDataRefNode getDataRef()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.ASSIGNMENT_STMT_562)
            return (ASTDataRefNode)getChild(3);
        else if (getProduction() == Production.ASSIGNMENT_STMT_563)
            return (ASTDataRefNode)getChild(3);
        else if (getProduction() == Production.ASSIGNMENT_STMT_564)
            return (ASTDataRefNode)getChild(3);
        else if (getProduction() == Production.ASSIGNMENT_STMT_565)
            return (ASTDataRefNode)getChild(6);
        else if (getProduction() == Production.ASSIGNMENT_STMT_566)
            return (ASTDataRefNode)getChild(6);
        else if (getProduction() == Production.ASSIGNMENT_STMT_567)
            return (ASTDataRefNode)getChild(6);
        else if (getProduction() == Production.ASSIGNMENT_STMT_568)
            return (ASTDataRefNode)getChild(6);
        else if (getProduction() == Production.ASSIGNMENT_STMT_569)
            return (ASTDataRefNode)getChild(6);
        else if (getProduction() == Production.ASSIGNMENT_STMT_570)
            return (ASTDataRefNode)getChild(6);
        else
            return null;
    }

    public ASTSectionSubscriptListNode getSectionSubscriptList()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.ASSIGNMENT_STMT_563)
            return (ASTSectionSubscriptListNode)getChild(5);
        else if (getProduction() == Production.ASSIGNMENT_STMT_564)
            return (ASTSectionSubscriptListNode)getChild(5);
        else if (getProduction() == Production.ASSIGNMENT_STMT_566)
            return (ASTSectionSubscriptListNode)getChild(8);
        else if (getProduction() == Production.ASSIGNMENT_STMT_567)
            return (ASTSectionSubscriptListNode)getChild(8);
        else if (getProduction() == Production.ASSIGNMENT_STMT_569)
            return (ASTSectionSubscriptListNode)getChild(8);
        else if (getProduction() == Production.ASSIGNMENT_STMT_570)
            return (ASTSectionSubscriptListNode)getChild(8);
        else
            return null;
    }

    public Token getTLparen2()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.ASSIGNMENT_STMT_566)
            return (Token)getChild(7);
        else if (getProduction() == Production.ASSIGNMENT_STMT_567)
            return (Token)getChild(7);
        else if (getProduction() == Production.ASSIGNMENT_STMT_569)
            return (Token)getChild(7);
        else if (getProduction() == Production.ASSIGNMENT_STMT_570)
            return (Token)getChild(7);
        else
            return null;
    }

    public Token getTRparen2()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.ASSIGNMENT_STMT_566)
            return (Token)getChild(9);
        else if (getProduction() == Production.ASSIGNMENT_STMT_567)
            return (Token)getChild(9);
        else if (getProduction() == Production.ASSIGNMENT_STMT_569)
            return (Token)getChild(9);
        else if (getProduction() == Production.ASSIGNMENT_STMT_570)
            return (Token)getChild(9);
        else
            return null;
    }
}
