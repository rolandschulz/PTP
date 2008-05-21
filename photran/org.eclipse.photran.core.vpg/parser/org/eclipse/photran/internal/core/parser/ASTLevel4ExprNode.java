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

public class ASTLevel4ExprNode extends ASTNode
{
    ASTLevel3ExprNode level3Expr; // in ASTLevel4ExprNode
    ASTLevel3ExprNode lhsExpr; // in ASTLevel4ExprNode
    ASTOperatorNode relOp; // in ASTLevel4ExprNode
    ASTLevel3ExprNode rhsExpr; // in ASTLevel4ExprNode

    public ASTLevel3ExprNode getLevel3Expr()
    {
        return this.level3Expr;
    }

    public void setLevel3Expr(ASTLevel3ExprNode newValue)
    {
        this.level3Expr = newValue;
    }


    public ASTLevel3ExprNode getLhsExpr()
    {
        return this.lhsExpr;
    }

    public void setLhsExpr(ASTLevel3ExprNode newValue)
    {
        this.lhsExpr = newValue;
    }


    public ASTOperatorNode getRelOp()
    {
        return this.relOp;
    }

    public void setRelOp(ASTOperatorNode newValue)
    {
        this.relOp = newValue;
    }


    public ASTLevel3ExprNode getRhsExpr()
    {
        return this.rhsExpr;
    }

    public void setRhsExpr(ASTLevel3ExprNode newValue)
    {
        this.rhsExpr = newValue;
    }


    public void accept(IASTVisitor visitor)
    {
        visitor.visitASTLevel4ExprNode(this);
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
        case 0:  return this.level3Expr;
        case 1:  return this.lhsExpr;
        case 2:  return this.relOp;
        case 3:  return this.rhsExpr;
        default: return null;
        }
    }

    @Override protected void setASTField(int index, IASTNode value)
    {
        switch (index)
        {
        case 0:  this.level3Expr = (ASTLevel3ExprNode)value;
        case 1:  this.lhsExpr = (ASTLevel3ExprNode)value;
        case 2:  this.relOp = (ASTOperatorNode)value;
        case 3:  this.rhsExpr = (ASTLevel3ExprNode)value;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }
}

