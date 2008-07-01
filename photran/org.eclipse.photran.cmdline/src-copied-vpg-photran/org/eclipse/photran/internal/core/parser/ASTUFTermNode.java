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

public class ASTUFTermNode extends ASTNode
{
    ASTUFFactorNode UFFactor; // in ASTUFTermNode
    ASTUFTermNode lhsExpr; // in ASTUFTermNode
    ASTOperatorNode multOp; // in ASTUFTermNode
    ASTUFFactorNode rhsExpr; // in ASTUFTermNode
    ASTOperatorNode concatOp; // in ASTUFTermNode
    ASTUFPrimaryNode rhsPrimary; // in ASTUFTermNode

    public ASTUFFactorNode getUFFactor()
    {
        return this.UFFactor;
    }

    public void setUFFactor(ASTUFFactorNode newValue)
    {
        this.UFFactor = newValue;
    }


    public ASTUFTermNode getLhsExpr()
    {
        return this.lhsExpr;
    }

    public void setLhsExpr(ASTUFTermNode newValue)
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


    public ASTUFFactorNode getRhsExpr()
    {
        return this.rhsExpr;
    }

    public void setRhsExpr(ASTUFFactorNode newValue)
    {
        this.rhsExpr = newValue;
    }


    public ASTOperatorNode getConcatOp()
    {
        return this.concatOp;
    }

    public void setConcatOp(ASTOperatorNode newValue)
    {
        this.concatOp = newValue;
    }


    public ASTUFPrimaryNode getRhsPrimary()
    {
        return this.rhsPrimary;
    }

    public void setRhsPrimary(ASTUFPrimaryNode newValue)
    {
        this.rhsPrimary = newValue;
    }


    public void accept(IASTVisitor visitor)
    {
        visitor.visitASTUFTermNode(this);
        visitor.visitASTNode(this);
    }

    @Override protected int getNumASTFields()
    {
        return 6;
    }

    @Override protected IASTNode getASTField(int index)
    {
        switch (index)
        {
        case 0:  return this.UFFactor;
        case 1:  return this.lhsExpr;
        case 2:  return this.multOp;
        case 3:  return this.rhsExpr;
        case 4:  return this.concatOp;
        case 5:  return this.rhsPrimary;
        default: return null;
        }
    }

    @Override protected void setASTField(int index, IASTNode value)
    {
        switch (index)
        {
        case 0:  this.UFFactor = (ASTUFFactorNode)value;
        case 1:  this.lhsExpr = (ASTUFTermNode)value;
        case 2:  this.multOp = (ASTOperatorNode)value;
        case 3:  this.rhsExpr = (ASTUFFactorNode)value;
        case 4:  this.concatOp = (ASTOperatorNode)value;
        case 5:  this.rhsPrimary = (ASTUFPrimaryNode)value;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }
}

