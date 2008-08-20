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

public class ASTLevel3ExprNode extends ASTNode
{
    ASTLevel2ExprNode level2Expr; // in ASTLevel3ExprNode
    ASTLevel3ExprNode lhsExpr; // in ASTLevel3ExprNode
    ASTOperatorNode concatOp; // in ASTLevel3ExprNode
    ASTLevel2ExprNode rhsExpr; // in ASTLevel3ExprNode

    public ASTLevel2ExprNode getLevel2Expr()
    {
        return this.level2Expr;
    }

    public void setLevel2Expr(ASTLevel2ExprNode newValue)
    {
        this.level2Expr = newValue;
    }


    public ASTLevel3ExprNode getLhsExpr()
    {
        return this.lhsExpr;
    }

    public void setLhsExpr(ASTLevel3ExprNode newValue)
    {
        this.lhsExpr = newValue;
    }


    public ASTOperatorNode getConcatOp()
    {
        return this.concatOp;
    }

    public void setConcatOp(ASTOperatorNode newValue)
    {
        this.concatOp = newValue;
    }


    public ASTLevel2ExprNode getRhsExpr()
    {
        return this.rhsExpr;
    }

    public void setRhsExpr(ASTLevel2ExprNode newValue)
    {
        this.rhsExpr = newValue;
    }


    public void accept(IASTVisitor visitor)
    {
        visitor.visitASTLevel3ExprNode(this);
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
        case 0:  return this.level2Expr;
        case 1:  return this.lhsExpr;
        case 2:  return this.concatOp;
        case 3:  return this.rhsExpr;
        default: return null;
        }
    }

    @Override protected void setASTField(int index, IASTNode value)
    {
        switch (index)
        {
        case 0:  this.level2Expr = (ASTLevel2ExprNode)value; return;
        case 1:  this.lhsExpr = (ASTLevel3ExprNode)value; return;
        case 2:  this.concatOp = (ASTOperatorNode)value; return;
        case 3:  this.rhsExpr = (ASTLevel2ExprNode)value; return;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }
}

