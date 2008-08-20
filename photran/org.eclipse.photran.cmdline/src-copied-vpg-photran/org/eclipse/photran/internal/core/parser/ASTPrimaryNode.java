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

public class ASTPrimaryNode extends ASTNode
{
    org.eclipse.photran.internal.core.lexer.Token intConst; // in ASTPrimaryNode
    ASTArrayConstructorNode arrayConstructor; // in ASTPrimaryNode
    org.eclipse.photran.internal.core.lexer.Token realConst; // in ASTPrimaryNode
    ASTComplexConstNode complexConst; // in ASTPrimaryNode
    org.eclipse.photran.internal.core.lexer.Token stringConst; // in ASTPrimaryNode
    org.eclipse.photran.internal.core.lexer.Token dblConst; // in ASTPrimaryNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTUnderscore; // in ASTPrimaryNode
    org.eclipse.photran.internal.core.lexer.Token intKind; // in ASTPrimaryNode
    ASTNameNode name; // in ASTPrimaryNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTLparen; // in ASTPrimaryNode
    IASTListNode<ASTSectionSubscriptNode> primarySectionSubscriptList; // in ASTPrimaryNode
    IASTListNode<ASTFunctionArgListNode> functionArgList; // in ASTPrimaryNode
    ASTExprNode nestedExpression; // in ASTPrimaryNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTRparen; // in ASTPrimaryNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTPercent; // in ASTPrimaryNode
    IASTListNode<ASTDataRefNode> derivedTypeComponentRef; // in ASTPrimaryNode
    org.eclipse.photran.internal.core.lexer.Token hiddenLparen2; // in ASTPrimaryNode
    IASTListNode<ASTSectionSubscriptNode> componentSectionSubscriptList; // in ASTPrimaryNode
    org.eclipse.photran.internal.core.lexer.Token hiddenRparen2; // in ASTPrimaryNode
    ASTSubstringRangeNode substringRange; // in ASTPrimaryNode
    ASTNamedConstantUseNode namedConstKind; // in ASTPrimaryNode
    ASTLogicalConstantNode logicalConst; // in ASTPrimaryNode

    public org.eclipse.photran.internal.core.lexer.Token getIntConst()
    {
        return this.intConst;
    }

    public void setIntConst(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.intConst = newValue;
    }


    public ASTArrayConstructorNode getArrayConstructor()
    {
        return this.arrayConstructor;
    }

    public void setArrayConstructor(ASTArrayConstructorNode newValue)
    {
        this.arrayConstructor = newValue;
    }


    public org.eclipse.photran.internal.core.lexer.Token getRealConst()
    {
        return this.realConst;
    }

    public void setRealConst(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.realConst = newValue;
    }


    public ASTComplexConstNode getComplexConst()
    {
        return this.complexConst;
    }

    public void setComplexConst(ASTComplexConstNode newValue)
    {
        this.complexConst = newValue;
    }


    public org.eclipse.photran.internal.core.lexer.Token getStringConst()
    {
        return this.stringConst;
    }

    public void setStringConst(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.stringConst = newValue;
    }


    public org.eclipse.photran.internal.core.lexer.Token getDblConst()
    {
        return this.dblConst;
    }

    public void setDblConst(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.dblConst = newValue;
    }


    public org.eclipse.photran.internal.core.lexer.Token getIntKind()
    {
        return this.intKind;
    }

    public void setIntKind(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.intKind = newValue;
    }


    public ASTNameNode getName()
    {
        return this.name;
    }

    public void setName(ASTNameNode newValue)
    {
        this.name = newValue;
    }


    public IASTListNode<ASTSectionSubscriptNode> getPrimarySectionSubscriptList()
    {
        return this.primarySectionSubscriptList;
    }

    public void setPrimarySectionSubscriptList(IASTListNode<ASTSectionSubscriptNode> newValue)
    {
        this.primarySectionSubscriptList = newValue;
    }


    public IASTListNode<ASTFunctionArgListNode> getFunctionArgList()
    {
        return this.functionArgList;
    }

    public void setFunctionArgList(IASTListNode<ASTFunctionArgListNode> newValue)
    {
        this.functionArgList = newValue;
    }


    public ASTExprNode getNestedExpression()
    {
        return this.nestedExpression;
    }

    public void setNestedExpression(ASTExprNode newValue)
    {
        this.nestedExpression = newValue;
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


    public ASTNamedConstantUseNode getNamedConstKind()
    {
        return this.namedConstKind;
    }

    public void setNamedConstKind(ASTNamedConstantUseNode newValue)
    {
        this.namedConstKind = newValue;
    }


    public ASTLogicalConstantNode getLogicalConst()
    {
        return this.logicalConst;
    }

    public void setLogicalConst(ASTLogicalConstantNode newValue)
    {
        this.logicalConst = newValue;
    }


    public void accept(IASTVisitor visitor)
    {
        visitor.visitASTPrimaryNode(this);
        visitor.visitASTNode(this);
    }

    @Override protected int getNumASTFields()
    {
        return 22;
    }

    @Override protected IASTNode getASTField(int index)
    {
        switch (index)
        {
        case 0:  return this.intConst;
        case 1:  return this.arrayConstructor;
        case 2:  return this.realConst;
        case 3:  return this.complexConst;
        case 4:  return this.stringConst;
        case 5:  return this.dblConst;
        case 6:  return this.hiddenTUnderscore;
        case 7:  return this.intKind;
        case 8:  return this.name;
        case 9:  return this.hiddenTLparen;
        case 10: return this.primarySectionSubscriptList;
        case 11: return this.functionArgList;
        case 12: return this.nestedExpression;
        case 13: return this.hiddenTRparen;
        case 14: return this.hiddenTPercent;
        case 15: return this.derivedTypeComponentRef;
        case 16: return this.hiddenLparen2;
        case 17: return this.componentSectionSubscriptList;
        case 18: return this.hiddenRparen2;
        case 19: return this.substringRange;
        case 20: return this.namedConstKind;
        case 21: return this.logicalConst;
        default: return null;
        }
    }

    @Override protected void setASTField(int index, IASTNode value)
    {
        switch (index)
        {
        case 0:  this.intConst = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 1:  this.arrayConstructor = (ASTArrayConstructorNode)value; return;
        case 2:  this.realConst = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 3:  this.complexConst = (ASTComplexConstNode)value; return;
        case 4:  this.stringConst = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 5:  this.dblConst = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 6:  this.hiddenTUnderscore = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 7:  this.intKind = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 8:  this.name = (ASTNameNode)value; return;
        case 9:  this.hiddenTLparen = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 10: this.primarySectionSubscriptList = (IASTListNode<ASTSectionSubscriptNode>)value; return;
        case 11: this.functionArgList = (IASTListNode<ASTFunctionArgListNode>)value; return;
        case 12: this.nestedExpression = (ASTExprNode)value; return;
        case 13: this.hiddenTRparen = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 14: this.hiddenTPercent = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 15: this.derivedTypeComponentRef = (IASTListNode<ASTDataRefNode>)value; return;
        case 16: this.hiddenLparen2 = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 17: this.componentSectionSubscriptList = (IASTListNode<ASTSectionSubscriptNode>)value; return;
        case 18: this.hiddenRparen2 = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 19: this.substringRange = (ASTSubstringRangeNode)value; return;
        case 20: this.namedConstKind = (ASTNamedConstantUseNode)value; return;
        case 21: this.logicalConst = (ASTLogicalConstantNode)value; return;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }
}

