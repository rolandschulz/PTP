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

public class ASTLevel2ExprNode extends ASTNode
{
    ASTLevel2ExprNode lhsExpr; // in ASTLevel2ExprNode
    ASTAddOperandNode addOperand; // in ASTLevel2ExprNode
    ASTOperatorNode addOp; // in ASTLevel2ExprNode
    ASTSignNode rhs; // in ASTLevel2ExprNode
    ASTAddOperandNode rhsExpr; // in ASTLevel2ExprNode

    public ASTLevel2ExprNode getLhsExpr()
    {
        return this.lhsExpr;
    }

    public void setLhsExpr(ASTLevel2ExprNode newValue)
    {
        this.lhsExpr = newValue;
    }


    public ASTAddOperandNode getAddOperand()
    {
        return this.addOperand;
    }

    public void setAddOperand(ASTAddOperandNode newValue)
    {
        this.addOperand = newValue;
    }


    public ASTOperatorNode getAddOp()
    {
        return this.addOp;
    }

    public void setAddOp(ASTOperatorNode newValue)
    {
        this.addOp = newValue;
    }


    public ASTSignNode getRhs()
    {
        return this.rhs;
    }

    public void setRhs(ASTSignNode newValue)
    {
        this.rhs = newValue;
    }


    public ASTAddOperandNode getRhsExpr()
    {
        return this.rhsExpr;
    }

    public void setRhsExpr(ASTAddOperandNode newValue)
    {
        this.rhsExpr = newValue;
    }


    public void accept(IASTVisitor visitor)
    {
        visitor.visitASTLevel2ExprNode(this);
        visitor.visitASTNode(this);
    }

    @Override protected int getNumASTFields()
    {
        return 5;
    }

    @Override protected IASTNode getASTField(int index)
    {
        switch (index)
        {
        case 0:  return this.lhsExpr;
        case 1:  return this.addOperand;
        case 2:  return this.addOp;
        case 3:  return this.rhs;
        case 4:  return this.rhsExpr;
        default: return null;
        }
    }

    @Override protected void setASTField(int index, IASTNode value)
    {
        switch (index)
        {
        case 0:  this.lhsExpr = (ASTLevel2ExprNode)value; return;
        case 1:  this.addOperand = (ASTAddOperandNode)value; return;
        case 2:  this.addOp = (ASTOperatorNode)value; return;
        case 3:  this.rhs = (ASTSignNode)value; return;
        case 4:  this.rhsExpr = (ASTAddOperandNode)value; return;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }
}

