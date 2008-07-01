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

public class ASTMultOperandNode extends ASTNode
{
    ASTLevel1ExprNode lhsExpr; // in ASTMultOperandNode
    ASTOperatorNode powerOp; // in ASTMultOperandNode
    ASTMultOperandNode rhsExpr; // in ASTMultOperandNode
    ASTLevel1ExprNode level1Expr; // in ASTMultOperandNode

    public ASTLevel1ExprNode getLhsExpr()
    {
        return this.lhsExpr;
    }

    public void setLhsExpr(ASTLevel1ExprNode newValue)
    {
        this.lhsExpr = newValue;
    }


    public ASTOperatorNode getPowerOp()
    {
        return this.powerOp;
    }

    public void setPowerOp(ASTOperatorNode newValue)
    {
        this.powerOp = newValue;
    }


    public ASTMultOperandNode getRhsExpr()
    {
        return this.rhsExpr;
    }

    public void setRhsExpr(ASTMultOperandNode newValue)
    {
        this.rhsExpr = newValue;
    }


    public ASTLevel1ExprNode getLevel1Expr()
    {
        return this.level1Expr;
    }

    public void setLevel1Expr(ASTLevel1ExprNode newValue)
    {
        this.level1Expr = newValue;
    }


    public void accept(IASTVisitor visitor)
    {
        visitor.visitASTMultOperandNode(this);
        visitor.visitASTNode(this);
    }

    @Override protected int getNumASTFields()
    {
        return 4;
    }

    @Override protected IASTNode getASTField(int index)
    {
        switch (index)
        {
        case 0:  return this.lhsExpr;
        case 1:  return this.powerOp;
        case 2:  return this.rhsExpr;
        case 3:  return this.level1Expr;
        default: return null;
        }
    }

    @Override protected void setASTField(int index, IASTNode value)
    {
        switch (index)
        {
        case 0:  this.lhsExpr = (ASTLevel1ExprNode)value;
        case 1:  this.powerOp = (ASTOperatorNode)value;
        case 2:  this.rhsExpr = (ASTMultOperandNode)value;
        case 3:  this.level1Expr = (ASTLevel1ExprNode)value;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }
}

