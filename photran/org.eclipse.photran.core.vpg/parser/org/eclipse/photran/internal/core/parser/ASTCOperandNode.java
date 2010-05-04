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
public class ASTCOperandNode extends ASTNode
{
    ASTNameNode name; // in ASTCOperandNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTLparen; // in ASTCOperandNode
    IASTListNode<ASTFunctionArgListNode> functionArgList; // in ASTCOperandNode
    org.eclipse.photran.internal.core.lexer.Token stringConst; // in ASTCOperandNode
    IASTListNode<ASTSectionSubscriptNode> primarySectionSubscriptList; // in ASTCOperandNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTRparen; // in ASTCOperandNode
    ASTImageSelectorNode imageSelector; // in ASTCOperandNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTPercent; // in ASTCOperandNode
    IASTListNode<ASTDataRefNode> derivedTypeComponentRef; // in ASTCOperandNode
    org.eclipse.photran.internal.core.lexer.Token hiddenLparen2; // in ASTCOperandNode
    IASTListNode<ASTSectionSubscriptNode> componentSectionSubscriptList; // in ASTCOperandNode
    org.eclipse.photran.internal.core.lexer.Token hiddenRparen2; // in ASTCOperandNode

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


    public org.eclipse.photran.internal.core.lexer.Token getStringConst()
    {
        return this.stringConst;
    }

    public void setStringConst(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.stringConst = newValue;
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


    public void accept(IASTVisitor visitor)
    {
        visitor.visitASTCOperandNode(this);
        visitor.visitASTNode(this);
    }

    @Override protected int getNumASTFields()
    {
        return 12;
    }

    @Override protected IASTNode getASTField(int index)
    {
        switch (index)
        {
        case 0:  return this.name;
        case 1:  return this.hiddenTLparen;
        case 2:  return this.functionArgList;
        case 3:  return this.stringConst;
        case 4:  return this.primarySectionSubscriptList;
        case 5:  return this.hiddenTRparen;
        case 6:  return this.imageSelector;
        case 7:  return this.hiddenTPercent;
        case 8:  return this.derivedTypeComponentRef;
        case 9:  return this.hiddenLparen2;
        case 10: return this.componentSectionSubscriptList;
        case 11: return this.hiddenRparen2;
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
        case 3:  this.stringConst = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 4:  this.primarySectionSubscriptList = (IASTListNode<ASTSectionSubscriptNode>)value; if (value != null) value.setParent(this); return;
        case 5:  this.hiddenTRparen = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 6:  this.imageSelector = (ASTImageSelectorNode)value; if (value != null) value.setParent(this); return;
        case 7:  this.hiddenTPercent = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 8:  this.derivedTypeComponentRef = (IASTListNode<ASTDataRefNode>)value; if (value != null) value.setParent(this); return;
        case 9:  this.hiddenLparen2 = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 10: this.componentSectionSubscriptList = (IASTListNode<ASTSectionSubscriptNode>)value; if (value != null) value.setParent(this); return;
        case 11: this.hiddenRparen2 = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }
}

