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

public class ASTOutputItemListNode extends ASTNode
{
    ASTOutputItemList1Node outputItemList1; // in ASTOutputItemListNode
    ASTExprNode singleExpr; // in ASTOutputItemListNode
    ASTExprNode expr1; // in ASTOutputItemListNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTComma; // in ASTOutputItemListNode
    ASTExprNode expr2; // in ASTOutputItemListNode
    ASTOutputImpliedDoNode outputImpliedDo; // in ASTOutputItemListNode

    public ASTOutputItemList1Node getOutputItemList1()
    {
        return this.outputItemList1;
    }

    public void setOutputItemList1(ASTOutputItemList1Node newValue)
    {
        this.outputItemList1 = newValue;
    }


    public ASTExprNode getSingleExpr()
    {
        return this.singleExpr;
    }

    public void setSingleExpr(ASTExprNode newValue)
    {
        this.singleExpr = newValue;
    }


    public ASTExprNode getExpr1()
    {
        return this.expr1;
    }

    public void setExpr1(ASTExprNode newValue)
    {
        this.expr1 = newValue;
    }


    public ASTExprNode getExpr2()
    {
        return this.expr2;
    }

    public void setExpr2(ASTExprNode newValue)
    {
        this.expr2 = newValue;
    }


    public ASTOutputImpliedDoNode getOutputImpliedDo()
    {
        return this.outputImpliedDo;
    }

    public void setOutputImpliedDo(ASTOutputImpliedDoNode newValue)
    {
        this.outputImpliedDo = newValue;
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
        case 0:  return this.outputItemList1;
        case 1:  return this.singleExpr;
        case 2:  return this.expr1;
        case 3:  return this.hiddenTComma;
        case 4:  return this.expr2;
        case 5:  return this.outputImpliedDo;
        default: return null;
        }
    }

    @Override protected void setASTField(int index, IASTNode value)
    {
        switch (index)
        {
        case 0:  this.outputItemList1 = (ASTOutputItemList1Node)value;
        case 1:  this.singleExpr = (ASTExprNode)value;
        case 2:  this.expr1 = (ASTExprNode)value;
        case 3:  this.hiddenTComma = (org.eclipse.photran.internal.core.lexer.Token)value;
        case 4:  this.expr2 = (ASTExprNode)value;
        case 5:  this.outputImpliedDo = (ASTOutputImpliedDoNode)value;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }
}

