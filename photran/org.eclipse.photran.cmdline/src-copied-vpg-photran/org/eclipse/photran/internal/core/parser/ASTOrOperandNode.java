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

public class ASTOrOperandNode extends ASTNode
{
    ASTOrOperandNode lhsExpr; // in ASTOrOperandNode
    ASTOperatorNode andOp; // in ASTOrOperandNode
    ASTAndOperandNode rhsExpr; // in ASTOrOperandNode
    ASTAndOperandNode andOperand; // in ASTOrOperandNode

    public ASTOrOperandNode getLhsExpr()
    {
        return this.lhsExpr;
    }

    public void setLhsExpr(ASTOrOperandNode newValue)
    {
        this.lhsExpr = newValue;
    }


    public ASTOperatorNode getAndOp()
    {
        return this.andOp;
    }

    public void setAndOp(ASTOperatorNode newValue)
    {
        this.andOp = newValue;
    }


    public ASTAndOperandNode getRhsExpr()
    {
        return this.rhsExpr;
    }

    public void setRhsExpr(ASTAndOperandNode newValue)
    {
        this.rhsExpr = newValue;
    }


    public ASTAndOperandNode getAndOperand()
    {
        return this.andOperand;
    }

    public void setAndOperand(ASTAndOperandNode newValue)
    {
        this.andOperand = newValue;
    }


    public void accept(IASTVisitor visitor)
    {
        visitor.visitASTOrOperandNode(this);
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
        case 1:  return this.andOp;
        case 2:  return this.rhsExpr;
        case 3:  return this.andOperand;
        default: return null;
        }
    }

    @Override protected void setASTField(int index, IASTNode value)
    {
        switch (index)
        {
        case 0:  this.lhsExpr = (ASTOrOperandNode)value;
        case 1:  this.andOp = (ASTOperatorNode)value;
        case 2:  this.rhsExpr = (ASTAndOperandNode)value;
        case 3:  this.andOperand = (ASTAndOperandNode)value;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }
}

