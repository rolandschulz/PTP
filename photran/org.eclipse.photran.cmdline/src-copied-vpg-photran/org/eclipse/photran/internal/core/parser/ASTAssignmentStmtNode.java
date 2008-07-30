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

import java.io.PrintStream;
import java.util.Iterator;

import java.util.List;

import org.eclipse.photran.internal.core.parser.Parser.ASTNode;
import org.eclipse.photran.internal.core.parser.Parser.ASTNodeWithErrorRecoverySymbols;
import org.eclipse.photran.internal.core.parser.Parser.IASTListNode;
import org.eclipse.photran.internal.core.parser.Parser.IASTNode;
import org.eclipse.photran.internal.core.parser.Parser.IASTVisitor;
import org.eclipse.photran.internal.core.lexer.Token;

import org.eclipse.photran.internal.core.lexer.*;                   import org.eclipse.photran.internal.core.analysis.binding.ScopingNode;

public class ASTAssignmentStmtNode extends ASTNodeWithErrorRecoverySymbols implements IActionStmt, IBodyConstruct, ICaseBodyConstruct, IExecutableConstruct, IExecutionPartConstruct, IForallBodyConstruct, IWhereBodyConstruct
{
    ASTLblDefNode lblDef; // in ASTAssignmentStmtNode
    ASTNameNode lhsVariable; // in ASTAssignmentStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTLparen; // in ASTAssignmentStmtNode
    IASTListNode<ASTSFExprListNode> lhsExprList; // in ASTAssignmentStmtNode
    IASTListNode<ASTSFDummyArgNameListNode> lhsNameList; // in ASTAssignmentStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTRparen; // in ASTAssignmentStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTPercent; // in ASTAssignmentStmtNode
    IASTListNode<ASTDataRefNode> derivedTypeComponentRef; // in ASTAssignmentStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenLparen2; // in ASTAssignmentStmtNode
    IASTListNode<ASTSectionSubscriptNode> componentSectionSubscriptList; // in ASTAssignmentStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenRparen2; // in ASTAssignmentStmtNode
    ASTSubstringRangeNode substringRange; // in ASTAssignmentStmtNode
    org.eclipse.photran.internal.core.lexer.Token isPointerAssignment; // in ASTAssignmentStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTEquals; // in ASTAssignmentStmtNode
    ASTExprNode rhs; // in ASTAssignmentStmtNode
    ASTTargetNode target; // in ASTAssignmentStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTEos; // in ASTAssignmentStmtNode

    public ASTLblDefNode getLblDef()
    {
        return this.lblDef;
    }

    public void setLblDef(ASTLblDefNode newValue)
    {
        this.lblDef = newValue;
    }


    public ASTNameNode getLhsVariable()
    {
        return this.lhsVariable;
    }

    public void setLhsVariable(ASTNameNode newValue)
    {
        this.lhsVariable = newValue;
    }


    public IASTListNode<ASTSFExprListNode> getLhsExprList()
    {
        return this.lhsExprList;
    }

    public void setLhsExprList(IASTListNode<ASTSFExprListNode> newValue)
    {
        this.lhsExprList = newValue;
    }


    public IASTListNode<ASTSFDummyArgNameListNode> getLhsNameList()
    {
        return this.lhsNameList;
    }

    public void setLhsNameList(IASTListNode<ASTSFDummyArgNameListNode> newValue)
    {
        this.lhsNameList = newValue;
    }


    public IASTListNode<ASTDataRefNode> getDerivedTypeComponentRef()
    {
        return this.derivedTypeComponentRef;
    }

    public void setDerivedTypeComponentRef(IASTListNode<ASTDataRefNode> newValue)
    {
        this.derivedTypeComponentRef = newValue;
    }


    public IASTListNode<ASTSectionSubscriptNode> getComponentSectionSubscriptList()
    {
        return this.componentSectionSubscriptList;
    }

    public void setComponentSectionSubscriptList(IASTListNode<ASTSectionSubscriptNode> newValue)
    {
        this.componentSectionSubscriptList = newValue;
    }


    public ASTSubstringRangeNode getSubstringRange()
    {
        return this.substringRange;
    }

    public void setSubstringRange(ASTSubstringRangeNode newValue)
    {
        this.substringRange = newValue;
    }


    public boolean isPointerAssignment()
    {
        return this.isPointerAssignment != null;
    }

    public void setIsPointerAssignment(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.isPointerAssignment = newValue;
    }


    public ASTExprNode getRhs()
    {
        return this.rhs;
    }

    public void setRhs(ASTExprNode newValue)
    {
        this.rhs = newValue;
    }


    public ASTTargetNode getTarget()
    {
        return this.target;
    }

    public void setTarget(ASTTargetNode newValue)
    {
        this.target = newValue;
    }


    public void accept(IASTVisitor visitor)
    {
        visitor.visitASTAssignmentStmtNode(this);
        visitor.visitIActionStmt(this);
        visitor.visitIBodyConstruct(this);
        visitor.visitICaseBodyConstruct(this);
        visitor.visitIExecutableConstruct(this);
        visitor.visitIExecutionPartConstruct(this);
        visitor.visitIForallBodyConstruct(this);
        visitor.visitIWhereBodyConstruct(this);
        visitor.visitASTNode(this);
    }

    @Override protected int getNumASTFields()
    {
        return 17;
    }

    @Override protected IASTNode getASTField(int index)
    {
        switch (index)
        {
        case 0:  return this.lblDef;
        case 1:  return this.lhsVariable;
        case 2:  return this.hiddenTLparen;
        case 3:  return this.lhsExprList;
        case 4:  return this.lhsNameList;
        case 5:  return this.hiddenTRparen;
        case 6:  return this.hiddenTPercent;
        case 7:  return this.derivedTypeComponentRef;
        case 8:  return this.hiddenLparen2;
        case 9:  return this.componentSectionSubscriptList;
        case 10: return this.hiddenRparen2;
        case 11: return this.substringRange;
        case 12: return this.isPointerAssignment;
        case 13: return this.hiddenTEquals;
        case 14: return this.rhs;
        case 15: return this.target;
        case 16: return this.hiddenTEos;
        default: return null;
        }
    }

    @Override protected void setASTField(int index, IASTNode value)
    {
        switch (index)
        {
        case 0:  this.lblDef = (ASTLblDefNode)value;
        case 1:  this.lhsVariable = (ASTNameNode)value;
        case 2:  this.hiddenTLparen = (org.eclipse.photran.internal.core.lexer.Token)value;
        case 3:  this.lhsExprList = (IASTListNode<ASTSFExprListNode>)value;
        case 4:  this.lhsNameList = (IASTListNode<ASTSFDummyArgNameListNode>)value;
        case 5:  this.hiddenTRparen = (org.eclipse.photran.internal.core.lexer.Token)value;
        case 6:  this.hiddenTPercent = (org.eclipse.photran.internal.core.lexer.Token)value;
        case 7:  this.derivedTypeComponentRef = (IASTListNode<ASTDataRefNode>)value;
        case 8:  this.hiddenLparen2 = (org.eclipse.photran.internal.core.lexer.Token)value;
        case 9:  this.componentSectionSubscriptList = (IASTListNode<ASTSectionSubscriptNode>)value;
        case 10: this.hiddenRparen2 = (org.eclipse.photran.internal.core.lexer.Token)value;
        case 11: this.substringRange = (ASTSubstringRangeNode)value;
        case 12: this.isPointerAssignment = (org.eclipse.photran.internal.core.lexer.Token)value;
        case 13: this.hiddenTEquals = (org.eclipse.photran.internal.core.lexer.Token)value;
        case 14: this.rhs = (ASTExprNode)value;
        case 15: this.target = (ASTTargetNode)value;
        case 16: this.hiddenTEos = (org.eclipse.photran.internal.core.lexer.Token)value;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }
}

