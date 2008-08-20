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

public class ASTSFPrimaryNode extends ASTNode
{
    IASTListNode<ASTSFDataRefNode> SFDataRef; // in ASTSFPrimaryNode
    org.eclipse.photran.internal.core.lexer.Token intConst; // in ASTSFPrimaryNode
    ASTArrayConstructorNode arrayConstructor; // in ASTSFPrimaryNode
    ASTFunctionReferenceNode functionReference; // in ASTSFPrimaryNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTLparen; // in ASTSFPrimaryNode
    ASTExprNode nestedExpression; // in ASTSFPrimaryNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTRparen; // in ASTSFPrimaryNode
    ASTSFVarNameNode SFVarName; // in ASTSFPrimaryNode

    public IASTListNode<ASTSFDataRefNode> getSFDataRef()
    {
        return this.SFDataRef;
    }

    public void setSFDataRef(IASTListNode<ASTSFDataRefNode> newValue)
    {
        this.SFDataRef = newValue;
    }


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


    public ASTFunctionReferenceNode getFunctionReference()
    {
        return this.functionReference;
    }

    public void setFunctionReference(ASTFunctionReferenceNode newValue)
    {
        this.functionReference = newValue;
    }


    public ASTExprNode getNestedExpression()
    {
        return this.nestedExpression;
    }

    public void setNestedExpression(ASTExprNode newValue)
    {
        this.nestedExpression = newValue;
    }


    public ASTSFVarNameNode getSFVarName()
    {
        return this.SFVarName;
    }

    public void setSFVarName(ASTSFVarNameNode newValue)
    {
        this.SFVarName = newValue;
    }


    public void accept(IASTVisitor visitor)
    {
        visitor.visitASTSFPrimaryNode(this);
        visitor.visitASTNode(this);
    }

    @Override protected int getNumASTFields()
    {
        return 8;
    }

    @Override protected IASTNode getASTField(int index)
    {
        switch (index)
        {
        case 0:  return this.SFDataRef;
        case 1:  return this.intConst;
        case 2:  return this.arrayConstructor;
        case 3:  return this.functionReference;
        case 4:  return this.hiddenTLparen;
        case 5:  return this.nestedExpression;
        case 6:  return this.hiddenTRparen;
        case 7:  return this.SFVarName;
        default: return null;
        }
    }

    @Override protected void setASTField(int index, IASTNode value)
    {
        switch (index)
        {
        case 0:  this.SFDataRef = (IASTListNode<ASTSFDataRefNode>)value; return;
        case 1:  this.intConst = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 2:  this.arrayConstructor = (ASTArrayConstructorNode)value; return;
        case 3:  this.functionReference = (ASTFunctionReferenceNode)value; return;
        case 4:  this.hiddenTLparen = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 5:  this.nestedExpression = (ASTExprNode)value; return;
        case 6:  this.hiddenTRparen = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 7:  this.SFVarName = (ASTSFVarNameNode)value; return;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }
}

