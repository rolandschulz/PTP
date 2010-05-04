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
public class ASTUFPrimaryNode extends ASTNode
{
    org.eclipse.photran.internal.core.lexer.Token intConst; // in ASTUFPrimaryNode
    org.eclipse.photran.internal.core.lexer.Token stringConst; // in ASTUFPrimaryNode
    ASTNameNode name; // in ASTUFPrimaryNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTLparen; // in ASTUFPrimaryNode
    IASTListNode<ASTSectionSubscriptNode> primarySectionSubscriptList; // in ASTUFPrimaryNode
    ASTUFExprNode nestedExpression; // in ASTUFPrimaryNode
    IASTListNode<ASTFunctionArgListNode> functionArgList; // in ASTUFPrimaryNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTRparen; // in ASTUFPrimaryNode
    ASTImageSelectorNode imageSelector; // in ASTUFPrimaryNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTPercent; // in ASTUFPrimaryNode
    IASTListNode<ASTDataRefNode> derivedTypeComponentRef; // in ASTUFPrimaryNode
    org.eclipse.photran.internal.core.lexer.Token hiddenLparen2; // in ASTUFPrimaryNode
    IASTListNode<ASTSectionSubscriptNode> componentSectionSubscriptList; // in ASTUFPrimaryNode
    org.eclipse.photran.internal.core.lexer.Token hiddenRparen2; // in ASTUFPrimaryNode
    ASTSubstringRangeNode substringRange; // in ASTUFPrimaryNode

    public org.eclipse.photran.internal.core.lexer.Token getIntConst()
    {
        return this.intConst;
    }

    public void setIntConst(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.intConst = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public org.eclipse.photran.internal.core.lexer.Token getStringConst()
    {
        return this.stringConst;
    }

    public void setStringConst(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.stringConst = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public ASTNameNode getName()
    {
        return this.name;
    }

    public void setName(ASTNameNode newValue)
    {
        this.name = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public IASTListNode<ASTSectionSubscriptNode> getPrimarySectionSubscriptList()
    {
        return this.primarySectionSubscriptList;
    }

    public void setPrimarySectionSubscriptList(IASTListNode<ASTSectionSubscriptNode> newValue)
    {
        this.primarySectionSubscriptList = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public ASTUFExprNode getNestedExpression()
    {
        return this.nestedExpression;
    }

    public void setNestedExpression(ASTUFExprNode newValue)
    {
        this.nestedExpression = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public IASTListNode<ASTFunctionArgListNode> getFunctionArgList()
    {
        return this.functionArgList;
    }

    public void setFunctionArgList(IASTListNode<ASTFunctionArgListNode> newValue)
    {
        this.functionArgList = newValue;
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


    public void accept(IASTVisitor visitor)
    {
        visitor.visitASTUFPrimaryNode(this);
        visitor.visitASTNode(this);
    }

    @Override protected int getNumASTFields()
    {
        return 15;
    }

    @Override protected IASTNode getASTField(int index)
    {
        switch (index)
        {
        case 0:  return this.intConst;
        case 1:  return this.stringConst;
        case 2:  return this.name;
        case 3:  return this.hiddenTLparen;
        case 4:  return this.primarySectionSubscriptList;
        case 5:  return this.nestedExpression;
        case 6:  return this.functionArgList;
        case 7:  return this.hiddenTRparen;
        case 8:  return this.imageSelector;
        case 9:  return this.hiddenTPercent;
        case 10: return this.derivedTypeComponentRef;
        case 11: return this.hiddenLparen2;
        case 12: return this.componentSectionSubscriptList;
        case 13: return this.hiddenRparen2;
        case 14: return this.substringRange;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }

    @Override protected void setASTField(int index, IASTNode value)
    {
        switch (index)
        {
        case 0:  this.intConst = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 1:  this.stringConst = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 2:  this.name = (ASTNameNode)value; if (value != null) value.setParent(this); return;
        case 3:  this.hiddenTLparen = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 4:  this.primarySectionSubscriptList = (IASTListNode<ASTSectionSubscriptNode>)value; if (value != null) value.setParent(this); return;
        case 5:  this.nestedExpression = (ASTUFExprNode)value; if (value != null) value.setParent(this); return;
        case 6:  this.functionArgList = (IASTListNode<ASTFunctionArgListNode>)value; if (value != null) value.setParent(this); return;
        case 7:  this.hiddenTRparen = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 8:  this.imageSelector = (ASTImageSelectorNode)value; if (value != null) value.setParent(this); return;
        case 9:  this.hiddenTPercent = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 10: this.derivedTypeComponentRef = (IASTListNode<ASTDataRefNode>)value; if (value != null) value.setParent(this); return;
        case 11: this.hiddenLparen2 = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 12: this.componentSectionSubscriptList = (IASTListNode<ASTSectionSubscriptNode>)value; if (value != null) value.setParent(this); return;
        case 13: this.hiddenRparen2 = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 14: this.substringRange = (ASTSubstringRangeNode)value; if (value != null) value.setParent(this); return;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }
}

