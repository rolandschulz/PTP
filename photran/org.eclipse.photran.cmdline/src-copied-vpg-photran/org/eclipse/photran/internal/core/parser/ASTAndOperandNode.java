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

public class ASTAndOperandNode extends ASTNode
{
    ASTOperatorNode notOp; // in ASTAndOperandNode
    ASTLevel4ExprNode level4Expr; // in ASTAndOperandNode
    ASTLevel4ExprNode rhsExpr; // in ASTAndOperandNode

    public ASTOperatorNode getNotOp()
    {
        return this.notOp;
    }

    public void setNotOp(ASTOperatorNode newValue)
    {
        this.notOp = newValue;
    }


    public ASTLevel4ExprNode getLevel4Expr()
    {
        return this.level4Expr;
    }

    public void setLevel4Expr(ASTLevel4ExprNode newValue)
    {
        this.level4Expr = newValue;
    }


    public ASTLevel4ExprNode getRhsExpr()
    {
        return this.rhsExpr;
    }

    public void setRhsExpr(ASTLevel4ExprNode newValue)
    {
        this.rhsExpr = newValue;
    }


    public void accept(IASTVisitor visitor)
    {
        visitor.visitASTAndOperandNode(this);
        visitor.visitASTNode(this);
    }

    @Override protected int getNumASTFields()
    {
        return 3;
    }

    @Override protected IASTNode getASTField(int index)
    {
        switch (index)
        {
        case 0:  return this.notOp;
        case 1:  return this.level4Expr;
        case 2:  return this.rhsExpr;
        default: return null;
        }
    }

    @Override protected void setASTField(int index, IASTNode value)
    {
        switch (index)
        {
        case 0:  this.notOp = (ASTOperatorNode)value;
        case 1:  this.level4Expr = (ASTLevel4ExprNode)value;
        case 2:  this.rhsExpr = (ASTLevel4ExprNode)value;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }
}

