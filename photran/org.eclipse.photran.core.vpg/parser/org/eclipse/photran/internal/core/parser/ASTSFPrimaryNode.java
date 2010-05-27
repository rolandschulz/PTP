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
public class ASTSFPrimaryNode extends ASTNode
{
    IASTListNode<ASTSFDataRefNode> SFDataRef; // in ASTSFPrimaryNode
    ASTFunctionReferenceNode functionReference; // in ASTSFPrimaryNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTLparen; // in ASTSFPrimaryNode
    IExpr expr; // in ASTSFPrimaryNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTRparen; // in ASTSFPrimaryNode
    org.eclipse.photran.internal.core.lexer.Token intConst; // in ASTSFPrimaryNode
    ASTSFVarNameNode SFVarName; // in ASTSFPrimaryNode
    ASTArrayConstructorNode arrayConstructor; // in ASTSFPrimaryNode

    public IASTListNode<ASTSFDataRefNode> getSFDataRef()
    {
        return this.SFDataRef;
    }

    public void setSFDataRef(IASTListNode<ASTSFDataRefNode> newValue)
    {
        this.SFDataRef = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public ASTFunctionReferenceNode getFunctionReference()
    {
        return this.functionReference;
    }

    public void setFunctionReference(ASTFunctionReferenceNode newValue)
    {
        this.functionReference = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public IExpr getExpr()
    {
        return this.expr;
    }

    public void setExpr(IExpr newValue)
    {
        this.expr = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public org.eclipse.photran.internal.core.lexer.Token getIntConst()
    {
        return this.intConst;
    }

    public void setIntConst(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.intConst = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public ASTSFVarNameNode getSFVarName()
    {
        return this.SFVarName;
    }

    public void setSFVarName(ASTSFVarNameNode newValue)
    {
        this.SFVarName = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public ASTArrayConstructorNode getArrayConstructor()
    {
        return this.arrayConstructor;
    }

    public void setArrayConstructor(ASTArrayConstructorNode newValue)
    {
        this.arrayConstructor = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    @Override
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
        case 1:  return this.functionReference;
        case 2:  return this.hiddenTLparen;
        case 3:  return this.expr;
        case 4:  return this.hiddenTRparen;
        case 5:  return this.intConst;
        case 6:  return this.SFVarName;
        case 7:  return this.arrayConstructor;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }

    @Override protected void setASTField(int index, IASTNode value)
    {
        switch (index)
        {
        case 0:  this.SFDataRef = (IASTListNode<ASTSFDataRefNode>)value; if (value != null) value.setParent(this); return;
        case 1:  this.functionReference = (ASTFunctionReferenceNode)value; if (value != null) value.setParent(this); return;
        case 2:  this.hiddenTLparen = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 3:  this.expr = (IExpr)value; if (value != null) value.setParent(this); return;
        case 4:  this.hiddenTRparen = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 5:  this.intConst = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 6:  this.SFVarName = (ASTSFVarNameNode)value; if (value != null) value.setParent(this); return;
        case 7:  this.arrayConstructor = (ASTArrayConstructorNode)value; if (value != null) value.setParent(this); return;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }
}

