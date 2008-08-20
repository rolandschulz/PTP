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

public class ASTAddOperandNode extends ASTNode
{
    ASTAddOperandNode lhsExpr; // in ASTAddOperandNode
    ASTOperatorNode multOp; // in ASTAddOperandNode
    ASTMultOperandNode multOperand; // in ASTAddOperandNode
    ASTMultOperandNode rhsExpr; // in ASTAddOperandNode

    public ASTAddOperandNode getLhsExpr()
    {
        return this.lhsExpr;
    }

    public void setLhsExpr(ASTAddOperandNode newValue)
    {
        this.lhsExpr = newValue;
    }


    public ASTOperatorNode getMultOp()
    {
        return this.multOp;
    }

    public void setMultOp(ASTOperatorNode newValue)
    {
        this.multOp = newValue;
    }


    public ASTMultOperandNode getMultOperand()
    {
        return this.multOperand;
    }

    public void setMultOperand(ASTMultOperandNode newValue)
    {
        this.multOperand = newValue;
    }


    public ASTMultOperandNode getRhsExpr()
    {
        return this.rhsExpr;
    }

    public void setRhsExpr(ASTMultOperandNode newValue)
    {
        this.rhsExpr = newValue;
    }


    public void accept(IASTVisitor visitor)
    {
        visitor.visitASTAddOperandNode(this);
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
        case 1:  return this.multOp;
        case 2:  return this.multOperand;
        case 3:  return this.rhsExpr;
        default: return null;
        }
    }

    @Override protected void setASTField(int index, IASTNode value)
    {
        switch (index)
        {
        case 0:  this.lhsExpr = (ASTAddOperandNode)value; return;
        case 1:  this.multOp = (ASTOperatorNode)value; return;
        case 2:  this.multOperand = (ASTMultOperandNode)value; return;
        case 3:  this.rhsExpr = (ASTMultOperandNode)value; return;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }
}

