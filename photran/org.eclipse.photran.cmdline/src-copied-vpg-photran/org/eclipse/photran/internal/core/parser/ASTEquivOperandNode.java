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

public class ASTEquivOperandNode extends ASTNode
{
    ASTOrOperandNode orOperand; // in ASTEquivOperandNode
    ASTEquivOperandNode lhsExpr; // in ASTEquivOperandNode
    ASTOperatorNode orOp; // in ASTEquivOperandNode
    ASTOrOperandNode rhsExpr; // in ASTEquivOperandNode

    public ASTOrOperandNode getOrOperand()
    {
        return this.orOperand;
    }

    public void setOrOperand(ASTOrOperandNode newValue)
    {
        this.orOperand = newValue;
    }


    public ASTEquivOperandNode getLhsExpr()
    {
        return this.lhsExpr;
    }

    public void setLhsExpr(ASTEquivOperandNode newValue)
    {
        this.lhsExpr = newValue;
    }


    public ASTOperatorNode getOrOp()
    {
        return this.orOp;
    }

    public void setOrOp(ASTOperatorNode newValue)
    {
        this.orOp = newValue;
    }


    public ASTOrOperandNode getRhsExpr()
    {
        return this.rhsExpr;
    }

    public void setRhsExpr(ASTOrOperandNode newValue)
    {
        this.rhsExpr = newValue;
    }


    public void accept(IASTVisitor visitor)
    {
        visitor.visitASTEquivOperandNode(this);
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
        case 0:  return this.orOperand;
        case 1:  return this.lhsExpr;
        case 2:  return this.orOp;
        case 3:  return this.rhsExpr;
        default: return null;
        }
    }

    @Override protected void setASTField(int index, IASTNode value)
    {
        switch (index)
        {
        case 0:  this.orOperand = (ASTOrOperandNode)value; return;
        case 1:  this.lhsExpr = (ASTEquivOperandNode)value; return;
        case 2:  this.orOp = (ASTOperatorNode)value; return;
        case 3:  this.rhsExpr = (ASTOrOperandNode)value; return;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }
}

