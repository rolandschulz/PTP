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
public class ASTVarOrFnRefNode extends ASTNode implements IExpr, ISelector
{
    ASTNameNode name; // in ASTVarOrFnRefNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTLparen; // in ASTVarOrFnRefNode
    IASTListNode<ASTFunctionArgListNode> functionArgList; // in ASTVarOrFnRefNode
    IASTListNode<ASTSectionSubscriptNode> primarySectionSubscriptList; // in ASTVarOrFnRefNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTRparen; // in ASTVarOrFnRefNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTPercent; // in ASTVarOrFnRefNode
    ASTSubstringRangeNode substringRange; // in ASTVarOrFnRefNode
    ASTImageSelectorNode imageSelector; // in ASTVarOrFnRefNode
    org.eclipse.photran.internal.core.lexer.Token hiddenHiddenPercent2; // in ASTVarOrFnRefNode
    IASTListNode<ASTDataRefNode> derivedTypeComponentRef; // in ASTVarOrFnRefNode
    org.eclipse.photran.internal.core.lexer.Token hiddenLparen2; // in ASTVarOrFnRefNode
    IASTListNode<ASTSectionSubscriptNode> componentSectionSubscriptList; // in ASTVarOrFnRefNode
    org.eclipse.photran.internal.core.lexer.Token hiddenRparen2; // in ASTVarOrFnRefNode
    ASTSubstringRangeNode substringRange2; // in ASTVarOrFnRefNode

    public ASTNameNode getName()
    {
        return this.name;
    }

    public void setName(ASTNameNode newValue)
    {
        this.name = newValue;
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


    public IASTListNode<ASTSectionSubscriptNode> getPrimarySectionSubscriptList()
    {
        return this.primarySectionSubscriptList;
    }

    public void setPrimarySectionSubscriptList(IASTListNode<ASTSectionSubscriptNode> newValue)
    {
        this.primarySectionSubscriptList = newValue;
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


    public ASTSubstringRangeNode getSubstringRange2()
    {
        return this.substringRange2;
    }

    public void setSubstringRange2(ASTSubstringRangeNode newValue)
    {
        this.substringRange2 = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public void accept(IASTVisitor visitor)
    {
        visitor.visitASTVarOrFnRefNode(this);
        visitor.visitIExpr(this);
        visitor.visitISelector(this);
        visitor.visitASTNode(this);
    }

    @Override protected int getNumASTFields()
    {
        return 14;
    }

    @Override protected IASTNode getASTField(int index)
    {
        switch (index)
        {
        case 0:  return this.name;
        case 1:  return this.hiddenTLparen;
        case 2:  return this.functionArgList;
        case 3:  return this.primarySectionSubscriptList;
        case 4:  return this.hiddenTRparen;
        case 5:  return this.hiddenTPercent;
        case 6:  return this.substringRange;
        case 7:  return this.imageSelector;
        case 8:  return this.hiddenHiddenPercent2;
        case 9:  return this.derivedTypeComponentRef;
        case 10: return this.hiddenLparen2;
        case 11: return this.componentSectionSubscriptList;
        case 12: return this.hiddenRparen2;
        case 13: return this.substringRange2;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }

    @Override protected void setASTField(int index, IASTNode value)
    {
        switch (index)
        {
        case 0:  this.name = (ASTNameNode)value; if (value != null) value.setParent(this); return;
        case 1:  this.hiddenTLparen = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 2:  this.functionArgList = (IASTListNode<ASTFunctionArgListNode>)value; if (value != null) value.setParent(this); return;
        case 3:  this.primarySectionSubscriptList = (IASTListNode<ASTSectionSubscriptNode>)value; if (value != null) value.setParent(this); return;
        case 4:  this.hiddenTRparen = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 5:  this.hiddenTPercent = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 6:  this.substringRange = (ASTSubstringRangeNode)value; if (value != null) value.setParent(this); return;
        case 7:  this.imageSelector = (ASTImageSelectorNode)value; if (value != null) value.setParent(this); return;
        case 8:  this.hiddenHiddenPercent2 = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 9:  this.derivedTypeComponentRef = (IASTListNode<ASTDataRefNode>)value; if (value != null) value.setParent(this); return;
        case 10: this.hiddenLparen2 = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 11: this.componentSectionSubscriptList = (IASTListNode<ASTSectionSubscriptNode>)value; if (value != null) value.setParent(this); return;
        case 12: this.hiddenRparen2 = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 13: this.substringRange2 = (ASTSubstringRangeNode)value; if (value != null) value.setParent(this); return;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }
}

