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

public class ASTLevel5ExprNode extends ASTNode
{
    ASTEquivOperandNode equivOperand; // in ASTLevel5ExprNode
    ASTLevel5ExprNode lhsExpr; // in ASTLevel5ExprNode
    ASTOperatorNode equivOp; // in ASTLevel5ExprNode
    ASTEquivOperandNode rhsExpr; // in ASTLevel5ExprNode

    public ASTEquivOperandNode getEquivOperand()
    {
        return this.equivOperand;
    }

    public void setEquivOperand(ASTEquivOperandNode newValue)
    {
        this.equivOperand = newValue;
    }


    public ASTLevel5ExprNode getLhsExpr()
    {
        return this.lhsExpr;
    }

    public void setLhsExpr(ASTLevel5ExprNode newValue)
    {
        this.lhsExpr = newValue;
    }


    public ASTOperatorNode getEquivOp()
    {
        return this.equivOp;
    }

    public void setEquivOp(ASTOperatorNode newValue)
    {
        this.equivOp = newValue;
    }


    public ASTEquivOperandNode getRhsExpr()
    {
        return this.rhsExpr;
    }

    public void setRhsExpr(ASTEquivOperandNode newValue)
    {
        this.rhsExpr = newValue;
    }


    public void accept(IASTVisitor visitor)
    {
        visitor.visitASTLevel5ExprNode(this);
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
        case 0:  return this.equivOperand;
        case 1:  return this.lhsExpr;
        case 2:  return this.equivOp;
        case 3:  return this.rhsExpr;
        default: return null;
        }
    }

    @Override protected void setASTField(int index, IASTNode value)
    {
        switch (index)
        {
        case 0:  this.equivOperand = (ASTEquivOperandNode)value;
        case 1:  this.lhsExpr = (ASTLevel5ExprNode)value;
        case 2:  this.equivOp = (ASTOperatorNode)value;
        case 3:  this.rhsExpr = (ASTEquivOperandNode)value;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }
}

