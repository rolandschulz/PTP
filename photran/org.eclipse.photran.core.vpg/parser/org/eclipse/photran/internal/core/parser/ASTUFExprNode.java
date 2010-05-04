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

import org.eclipse.photran.internal.core.parser.ASTListNode;
import org.eclipse.photran.internal.core.parser.ASTNode;
import org.eclipse.photran.internal.core.parser.ASTNodeWithErrorRecoverySymbols;
import org.eclipse.photran.internal.core.parser.IASTListNode;
import org.eclipse.photran.internal.core.parser.IASTNode;
import org.eclipse.photran.internal.core.parser.IASTVisitor;
import org.eclipse.photran.internal.core.lexer.Token;

import org.eclipse.photran.internal.core.lexer.*;                   import org.eclipse.photran.internal.core.analysis.binding.ScopingNode;                   import org.eclipse.photran.internal.core.SyntaxException;                   import java.io.IOException;

@SuppressWarnings("all")
public class ASTUFExprNode extends ASTNode
{
    ASTUFExprNode lhsExpr; // in ASTUFExprNode
    ASTSignNode rhs2; // in ASTUFExprNode
    ASTOperatorNode addOp; // in ASTUFExprNode
    ASTUFTermNode rhsExpr; // in ASTUFExprNode
    ASTUFTermNode UFTerm; // in ASTUFExprNode

    public ASTUFExprNode getLhsExpr()
    {
        return this.lhsExpr;
    }

    public void setLhsExpr(ASTUFExprNode newValue)
    {
        this.lhsExpr = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public ASTSignNode getRhs2()
    {
        return this.rhs2;
    }

    public void setRhs2(ASTSignNode newValue)
    {
        this.rhs2 = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public ASTOperatorNode getAddOp()
    {
        return this.addOp;
    }

    public void setAddOp(ASTOperatorNode newValue)
    {
        this.addOp = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public ASTUFTermNode getRhsExpr()
    {
        return this.rhsExpr;
    }

    public void setRhsExpr(ASTUFTermNode newValue)
    {
        this.rhsExpr = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public ASTUFTermNode getUFTerm()
    {
        return this.UFTerm;
    }

    public void setUFTerm(ASTUFTermNode newValue)
    {
        this.UFTerm = newValue;
        if (newValue != null) newValue.setParent(this);
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
        case 2:  return this.addOp;
        case 3:  return this.rhsExpr;
        case 4:  return this.UFTerm;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }

    @Override protected void setASTField(int index, IASTNode value)
    {
        switch (index)
        {
        case 0:  this.lhsExpr = (ASTUFExprNode)value; if (value != null) value.setParent(this); return;
        case 1:  this.rhs2 = (ASTSignNode)value; if (value != null) value.setParent(this); return;
        case 2:  this.addOp = (ASTOperatorNode)value; if (value != null) value.setParent(this); return;
        case 3:  this.rhsExpr = (ASTUFTermNode)value; if (value != null) value.setParent(this); return;
        case 4:  this.UFTerm = (ASTUFTermNode)value; if (value != null) value.setParent(this); return;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }
}

