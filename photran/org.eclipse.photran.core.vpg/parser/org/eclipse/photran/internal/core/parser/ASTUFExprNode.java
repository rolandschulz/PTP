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

public class ASTUFExprNode extends ASTNode
{
    ASTUFExprNode lhsExpr; // in ASTUFExprNode
    ASTSignNode rhs2; // in ASTUFExprNode
    ASTUFTermNode UFTerm; // in ASTUFExprNode
    ASTOperatorNode addOp; // in ASTUFExprNode
    ASTUFTermNode rhsExpr; // in ASTUFExprNode

    public ASTUFExprNode getLhsExpr()
    {
        return this.lhsExpr;
    }

    public void setLhsExpr(ASTUFExprNode newValue)
    {
        this.lhsExpr = newValue;
    }


    public ASTSignNode getRhs2()
    {
        return this.rhs2;
    }

    public void setRhs2(ASTSignNode newValue)
    {
        this.rhs2 = newValue;
    }


    public ASTUFTermNode getUFTerm()
    {
        return this.UFTerm;
    }

    public void setUFTerm(ASTUFTermNode newValue)
    {
        this.UFTerm = newValue;
    }


    public ASTOperatorNode getAddOp()
    {
        return this.addOp;
    }

    public void setAddOp(ASTOperatorNode newValue)
    {
        this.addOp = newValue;
    }


    public ASTUFTermNode getRhsExpr()
    {
        return this.rhsExpr;
    }

    public void setRhsExpr(ASTUFTermNode newValue)
    {
        this.rhsExpr = newValue;
    }


    public void accept(IASTVisitor visitor)
    {
        visitor.visitASTUFExprNode(this);
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
        case 1:  return this.rhs2;
        case 2:  return this.UFTerm;
        case 3:  return this.addOp;
        case 4:  return this.rhsExpr;
        default: return null;
        }
    }

    @Override protected void setASTField(int index, IASTNode value)
    {
        switch (index)
        {
        case 0:  this.lhsExpr = (ASTUFExprNode)value; return;
        case 1:  this.rhs2 = (ASTSignNode)value; return;
        case 2:  this.UFTerm = (ASTUFTermNode)value; return;
        case 3:  this.addOp = (ASTOperatorNode)value; return;
        case 4:  this.rhsExpr = (ASTUFTermNode)value; return;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }
}

