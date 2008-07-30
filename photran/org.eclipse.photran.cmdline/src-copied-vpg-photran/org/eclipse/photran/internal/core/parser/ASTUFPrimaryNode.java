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

public class ASTUFPrimaryNode extends ASTNode
{
    ASTNameNode name; // in ASTUFPrimaryNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTLparen; // in ASTUFPrimaryNode
    IASTListNode<ASTFunctionArgListNode> functionArgList; // in ASTUFPrimaryNode
    org.eclipse.photran.internal.core.lexer.Token stringConst; // in ASTUFPrimaryNode
    ASTUFExprNode nestedExpression; // in ASTUFPrimaryNode
    org.eclipse.photran.internal.core.lexer.Token intConst; // in ASTUFPrimaryNode
    IASTListNode<ASTSectionSubscriptNode> primarySectionSubscriptList; // in ASTUFPrimaryNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTRparen; // in ASTUFPrimaryNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTPercent; // in ASTUFPrimaryNode
    IASTListNode<ASTDataRefNode> derivedTypeComponentRef; // in ASTUFPrimaryNode
    org.eclipse.photran.internal.core.lexer.Token hiddenLparen2; // in ASTUFPrimaryNode
    IASTListNode<ASTSectionSubscriptNode> componentSectionSubscriptList; // in ASTUFPrimaryNode
    org.eclipse.photran.internal.core.lexer.Token hiddenRparen2; // in ASTUFPrimaryNode
    ASTSubstringRangeNode substringRange; // in ASTUFPrimaryNode

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


    public org.eclipse.photran.internal.core.lexer.Token getStringConst()
    {
        return this.stringConst;
    }

    public void setStringConst(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.stringConst = newValue;
    }


    public ASTUFExprNode getNestedExpression()
    {
        return this.nestedExpression;
    }

    public void setNestedExpression(ASTUFExprNode newValue)
    {
        this.nestedExpression = newValue;
    }


    public org.eclipse.photran.internal.core.lexer.Token getIntConst()
    {
        return this.intConst;
    }

    public void setIntConst(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.intConst = newValue;
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
        visitor.visitASTUFPrimaryNode(this);
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
        case 3:  return this.stringConst;
        case 4:  return this.nestedExpression;
        case 5:  return this.intConst;
        case 6:  return this.primarySectionSubscriptList;
        case 7:  return this.hiddenTRparen;
        case 8:  return this.hiddenTPercent;
        case 9:  return this.derivedTypeComponentRef;
        case 10: return this.hiddenLparen2;
        case 11: return this.componentSectionSubscriptList;
        case 12: return this.hiddenRparen2;
        case 13: return this.substringRange;
        default: return null;
        }
    }

    @Override protected void setASTField(int index, IASTNode value)
    {
        switch (index)
        {
        case 0:  this.name = (ASTNameNode)value;
        case 1:  this.hiddenTLparen = (org.eclipse.photran.internal.core.lexer.Token)value;
        case 2:  this.functionArgList = (IASTListNode<ASTFunctionArgListNode>)value;
        case 3:  this.stringConst = (org.eclipse.photran.internal.core.lexer.Token)value;
        case 4:  this.nestedExpression = (ASTUFExprNode)value;
        case 5:  this.intConst = (org.eclipse.photran.internal.core.lexer.Token)value;
        case 6:  this.primarySectionSubscriptList = (IASTListNode<ASTSectionSubscriptNode>)value;
        case 7:  this.hiddenTRparen = (org.eclipse.photran.internal.core.lexer.Token)value;
        case 8:  this.hiddenTPercent = (org.eclipse.photran.internal.core.lexer.Token)value;
        case 9:  this.derivedTypeComponentRef = (IASTListNode<ASTDataRefNode>)value;
        case 10: this.hiddenLparen2 = (org.eclipse.photran.internal.core.lexer.Token)value;
        case 11: this.componentSectionSubscriptList = (IASTListNode<ASTSectionSubscriptNode>)value;
        case 12: this.hiddenRparen2 = (org.eclipse.photran.internal.core.lexer.Token)value;
        case 13: this.substringRange = (ASTSubstringRangeNode)value;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }
}

