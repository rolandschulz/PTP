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
public class ASTOutputItemListNode extends ASTNode
{
    IExpr singleExpr; // in ASTOutputItemListNode
    IExpr expr1; // in ASTOutputItemListNode
    ASTOutputItemList1Node outputItemList1; // in ASTOutputItemListNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTComma; // in ASTOutputItemListNode
    ASTOutputImpliedDoNode outputImpliedDo; // in ASTOutputItemListNode
    IExpr expr2; // in ASTOutputItemListNode

    public IExpr getSingleExpr()
    {
        return this.singleExpr;
    }

    public void setSingleExpr(IExpr newValue)
    {
        this.singleExpr = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public IExpr getExpr1()
    {
        return this.expr1;
    }

    public void setExpr1(IExpr newValue)
    {
        this.expr1 = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public ASTOutputItemList1Node getOutputItemList1()
    {
        return this.outputItemList1;
    }

    public void setOutputItemList1(ASTOutputItemList1Node newValue)
    {
        this.outputItemList1 = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public ASTOutputImpliedDoNode getOutputImpliedDo()
    {
        return this.outputImpliedDo;
    }

    public void setOutputImpliedDo(ASTOutputImpliedDoNode newValue)
    {
        this.outputImpliedDo = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public IExpr getExpr2()
    {
        return this.expr2;
    }

    public void setExpr2(IExpr newValue)
    {
        this.expr2 = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public void accept(IASTVisitor visitor)
    {
        visitor.visitASTOutputItemListNode(this);
        visitor.visitASTNode(this);
    }

    @Override protected int getNumASTFields()
    {
        return 6;
    }

    @Override protected IASTNode getASTField(int index)
    {
        switch (index)
        {
        case 0:  return this.singleExpr;
        case 1:  return this.expr1;
        case 2:  return this.outputItemList1;
        case 3:  return this.hiddenTComma;
        case 4:  return this.outputImpliedDo;
        case 5:  return this.expr2;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }

    @Override protected void setASTField(int index, IASTNode value)
    {
        switch (index)
        {
        case 0:  this.singleExpr = (IExpr)value; if (value != null) value.setParent(this); return;
        case 1:  this.expr1 = (IExpr)value; if (value != null) value.setParent(this); return;
        case 2:  this.outputItemList1 = (ASTOutputItemList1Node)value; if (value != null) value.setParent(this); return;
        case 3:  this.hiddenTComma = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 4:  this.outputImpliedDo = (ASTOutputImpliedDoNode)value; if (value != null) value.setParent(this); return;
        case 5:  this.expr2 = (IExpr)value; if (value != null) value.setParent(this); return;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }
}

