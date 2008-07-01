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

public class ASTExprNode extends ASTNode
{
    ASTLevel5ExprNode level5Expr; // in ASTExprNode
    ASTExprNode lhsExpr; // in ASTExprNode
    ASTOperatorNode definedBinaryOp; // in ASTExprNode
    ASTLevel5ExprNode rhsExpr; // in ASTExprNode

    public ASTLevel5ExprNode getLevel5Expr()
    {
        return this.level5Expr;
    }

    public void setLevel5Expr(ASTLevel5ExprNode newValue)
    {
        this.level5Expr = newValue;
    }


    public ASTExprNode getLhsExpr()
    {
        return this.lhsExpr;
    }

    public void setLhsExpr(ASTExprNode newValue)
    {
        this.lhsExpr = newValue;
    }


    public ASTOperatorNode getDefinedBinaryOp()
    {
        return this.definedBinaryOp;
    }

    public void setDefinedBinaryOp(ASTOperatorNode newValue)
    {
        this.definedBinaryOp = newValue;
    }


    public ASTLevel5ExprNode getRhsExpr()
    {
        return this.rhsExpr;
    }

    public void setRhsExpr(ASTLevel5ExprNode newValue)
    {
        this.rhsExpr = newValue;
    }


    public void accept(IASTVisitor visitor)
    {
        visitor.visitASTExprNode(this);
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
        case 0:  return this.level5Expr;
        case 1:  return this.lhsExpr;
        case 2:  return this.definedBinaryOp;
        case 3:  return this.rhsExpr;
        default: return null;
        }
    }

    @Override protected void setASTField(int index, IASTNode value)
    {
        switch (index)
        {
        case 0:  this.level5Expr = (ASTLevel5ExprNode)value;
        case 1:  this.lhsExpr = (ASTExprNode)value;
        case 2:  this.definedBinaryOp = (ASTOperatorNode)value;
        case 3:  this.rhsExpr = (ASTLevel5ExprNode)value;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }
}

