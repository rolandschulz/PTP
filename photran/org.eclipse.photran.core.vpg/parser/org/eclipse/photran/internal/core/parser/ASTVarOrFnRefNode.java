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

public class ASTVarOrFnRefNode extends ASTNode implements IExpr
{
    ASTNameNode name; // in ASTVarOrFnRefNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTLparen; // in ASTVarOrFnRefNode
    IASTListNode<ASTFunctionArgListNode> functionArgList; // in ASTVarOrFnRefNode
    IASTListNode<ASTSectionSubscriptNode> primarySectionSubscriptList; // in ASTVarOrFnRefNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTRparen; // in ASTVarOrFnRefNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTPercent; // in ASTVarOrFnRefNode
    IASTListNode<ASTDataRefNode> derivedTypeComponentRef; // in ASTVarOrFnRefNode
    org.eclipse.photran.internal.core.lexer.Token hiddenLparen2; // in ASTVarOrFnRefNode
    IASTListNode<ASTSectionSubscriptNode> componentSectionSubscriptList; // in ASTVarOrFnRefNode
    org.eclipse.photran.internal.core.lexer.Token hiddenRparen2; // in ASTVarOrFnRefNode
    ASTSubstringRangeNode substringRange; // in ASTVarOrFnRefNode

    public ASTNameNode getName()
    {
        return this.name;
    }

    public void setName(ASTNameNode newValue)
    {
        this.name = newValue;
    }


    public IASTListNode<ASTFunctionArgListNode> getFunctionArgList()
    {
        return this.functionArgList;
    }

    public void setFunctionArgList(IASTListNode<ASTFunctionArgListNode> newValue)
    {
        this.functionArgList = newValue;
    }


    public IASTListNode<ASTSectionSubscriptNode> getPrimarySectionSubscriptList()
    {
        return this.primarySectionSubscriptList;
    }

    public void setPrimarySectionSubscriptList(IASTListNode<ASTSectionSubscriptNode> newValue)
    {
        this.primarySectionSubscriptList = newValue;
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


    public void accept(IASTVisitor visitor)
    {
        visitor.visitASTVarOrFnRefNode(this);
        visitor.visitIExpr(this);
        visitor.visitASTNode(this);
    }

    @Override protected int getNumASTFields()
    {
        return 11;
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
        case 6:  return this.derivedTypeComponentRef;
        case 7:  return this.hiddenLparen2;
        case 8:  return this.componentSectionSubscriptList;
        case 9:  return this.hiddenRparen2;
        case 10: return this.substringRange;
        default: return null;
        }
    }

    @Override protected void setASTField(int index, IASTNode value)
    {
        switch (index)
        {
        case 0:  this.name = (ASTNameNode)value; return;
        case 1:  this.hiddenTLparen = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 2:  this.functionArgList = (IASTListNode<ASTFunctionArgListNode>)value; return;
        case 3:  this.primarySectionSubscriptList = (IASTListNode<ASTSectionSubscriptNode>)value; return;
        case 4:  this.hiddenTRparen = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 5:  this.hiddenTPercent = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 6:  this.derivedTypeComponentRef = (IASTListNode<ASTDataRefNode>)value; return;
        case 7:  this.hiddenLparen2 = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 8:  this.componentSectionSubscriptList = (IASTListNode<ASTSectionSubscriptNode>)value; return;
        case 9:  this.hiddenRparen2 = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 10: this.substringRange = (ASTSubstringRangeNode)value; return;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }
}

