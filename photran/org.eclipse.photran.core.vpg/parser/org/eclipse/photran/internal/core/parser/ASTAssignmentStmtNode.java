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

import org.eclipse.photran.internal.core.parser.ASTListNode;
import org.eclipse.photran.internal.core.parser.ASTNode;
import org.eclipse.photran.internal.core.parser.ASTNodeWithErrorRecoverySymbols;
import org.eclipse.photran.internal.core.parser.IASTListNode;
import org.eclipse.photran.internal.core.parser.IASTNode;
import org.eclipse.photran.internal.core.parser.IASTVisitor;
import org.eclipse.photran.internal.core.lexer.Token;

import org.eclipse.photran.internal.core.lexer.*;                   import org.eclipse.photran.internal.core.analysis.binding.ScopingNode;                   import org.eclipse.photran.internal.core.SyntaxException;                   import java.io.IOException;

@SuppressWarnings("all")
public class ASTAssignmentStmtNode extends ASTNodeWithErrorRecoverySymbols implements IActionStmt, IBodyConstruct, ICaseBodyConstruct, IExecutableConstruct, IExecutionPartConstruct, IForallBodyConstruct, IWhereBodyConstruct
{
    org.eclipse.photran.internal.core.lexer.Token label; // in ASTAssignmentStmtNode
    ASTNameNode lhsVariable; // in ASTAssignmentStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTLparen; // in ASTAssignmentStmtNode
    IASTListNode<ASTSFDummyArgNameListNode> lhsNameList; // in ASTAssignmentStmtNode
    IASTListNode<ASTSFExprListNode> lhsExprList; // in ASTAssignmentStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTRparen; // in ASTAssignmentStmtNode
    ASTImageSelectorNode imageSelector; // in ASTAssignmentStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTPercent; // in ASTAssignmentStmtNode
    IASTListNode<ASTDataRefNode> derivedTypeComponentRef; // in ASTAssignmentStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenLparen2; // in ASTAssignmentStmtNode
    IASTListNode<ASTSectionSubscriptNode> componentSectionSubscriptList; // in ASTAssignmentStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenRparen2; // in ASTAssignmentStmtNode
    ASTSubstringRangeNode substringRange; // in ASTAssignmentStmtNode
    org.eclipse.photran.internal.core.lexer.Token isPointerAssignment; // in ASTAssignmentStmtNode
    ASTTargetNode target; // in ASTAssignmentStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTEquals; // in ASTAssignmentStmtNode
    IExpr rhs; // in ASTAssignmentStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTEos; // in ASTAssignmentStmtNode

    public org.eclipse.photran.internal.core.lexer.Token getLabel()
    {
        return this.label;
    }

    public void setLabel(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.label = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public ASTNameNode getLhsVariable()
    {
        return this.lhsVariable;
    }

    public void setLhsVariable(ASTNameNode newValue)
    {
        this.lhsVariable = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public IASTListNode<ASTSFDummyArgNameListNode> getLhsNameList()
    {
        return this.lhsNameList;
    }

    public void setLhsNameList(IASTListNode<ASTSFDummyArgNameListNode> newValue)
    {
        this.lhsNameList = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public IASTListNode<ASTSFExprListNode> getLhsExprList()
    {
        return this.lhsExprList;
    }

    public void setLhsExprList(IASTListNode<ASTSFExprListNode> newValue)
    {
        this.lhsExprList = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public ASTImageSelectorNode getImageSelector()
    {
        return this.imageSelector;
    }

    public void setImageSelector(ASTImageSelectorNode newValue)
    {
        this.imageSelector = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public IASTListNode<ASTDataRefNode> getDerivedTypeComponentRef()
    {
        return this.derivedTypeComponentRef;
    }

    public void setDerivedTypeComponentRef(IASTListNode<ASTDataRefNode> newValue)
    {
        this.derivedTypeComponentRef = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public IASTListNode<ASTSectionSubscriptNode> getComponentSectionSubscriptList()
    {
        return this.componentSectionSubscriptList;
    }

    public void setComponentSectionSubscriptList(IASTListNode<ASTSectionSubscriptNode> newValue)
    {
        this.componentSectionSubscriptList = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public ASTSubstringRangeNode getSubstringRange()
    {
        return this.substringRange;
    }

    public void setSubstringRange(ASTSubstringRangeNode newValue)
    {
        this.substringRange = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public boolean isPointerAssignment()
    {
        return this.isPointerAssignment != null;
    }

    public void setIsPointerAssignment(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.isPointerAssignment = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public ASTTargetNode getTarget()
    {
        return this.target;
    }

    public void setTarget(ASTTargetNode newValue)
    {
        this.target = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public IExpr getRhs()
    {
        return this.rhs;
    }

    public void setRhs(IExpr newValue)
    {
        this.rhs = newValue;
        if (newValue != null) newValue.setParent(this);
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
        return 18;
    }

    @Override protected IASTNode getASTField(int index)
    {
        switch (index)
        {
        case 0:  return this.label;
        case 1:  return this.lhsVariable;
        case 2:  return this.hiddenTLparen;
        case 3:  return this.lhsNameList;
        case 4:  return this.lhsExprList;
        case 5:  return this.hiddenTRparen;
        case 6:  return this.imageSelector;
        case 7:  return this.hiddenTPercent;
        case 8:  return this.derivedTypeComponentRef;
        case 9:  return this.hiddenLparen2;
        case 10: return this.componentSectionSubscriptList;
        case 11: return this.hiddenRparen2;
        case 12: return this.substringRange;
        case 13: return this.isPointerAssignment;
        case 14: return this.target;
        case 15: return this.hiddenTEquals;
        case 16: return this.rhs;
        case 17: return this.hiddenTEos;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }

    @Override protected void setASTField(int index, IASTNode value)
    {
        switch (index)
        {
        case 0:  this.label = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 1:  this.lhsVariable = (ASTNameNode)value; if (value != null) value.setParent(this); return;
        case 2:  this.hiddenTLparen = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 3:  this.lhsNameList = (IASTListNode<ASTSFDummyArgNameListNode>)value; if (value != null) value.setParent(this); return;
        case 4:  this.lhsExprList = (IASTListNode<ASTSFExprListNode>)value; if (value != null) value.setParent(this); return;
        case 5:  this.hiddenTRparen = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 6:  this.imageSelector = (ASTImageSelectorNode)value; if (value != null) value.setParent(this); return;
        case 7:  this.hiddenTPercent = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 8:  this.derivedTypeComponentRef = (IASTListNode<ASTDataRefNode>)value; if (value != null) value.setParent(this); return;
        case 9:  this.hiddenLparen2 = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 10: this.componentSectionSubscriptList = (IASTListNode<ASTSectionSubscriptNode>)value; if (value != null) value.setParent(this); return;
        case 11: this.hiddenRparen2 = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 12: this.substringRange = (ASTSubstringRangeNode)value; if (value != null) value.setParent(this); return;
        case 13: this.isPointerAssignment = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 14: this.target = (ASTTargetNode)value; if (value != null) value.setParent(this); return;
        case 15: this.hiddenTEquals = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 16: this.rhs = (IExpr)value; if (value != null) value.setParent(this); return;
        case 17: this.hiddenTEos = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }
}

