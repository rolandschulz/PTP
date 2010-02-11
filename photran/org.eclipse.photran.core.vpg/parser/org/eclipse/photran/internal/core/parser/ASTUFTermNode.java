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

import org.eclipse.photran.internal.core.parser.Parser.ASTListNode;
import org.eclipse.photran.internal.core.parser.Parser.ASTNode;
import org.eclipse.photran.internal.core.parser.Parser.ASTNodeWithErrorRecoverySymbols;
import org.eclipse.photran.internal.core.parser.Parser.IASTListNode;
import org.eclipse.photran.internal.core.parser.Parser.IASTNode;
import org.eclipse.photran.internal.core.parser.Parser.IASTVisitor;
import org.eclipse.photran.internal.core.lexer.Token;

import org.eclipse.photran.internal.core.lexer.*;                   import org.eclipse.photran.internal.core.analysis.binding.ScopingNode;                   import org.eclipse.photran.internal.core.SyntaxException;                   import java.io.IOException;

@SuppressWarnings({ "unchecked", "unused" })
public class ASTUFTermNode extends ASTNode
{
    ASTUFTermNode lhsExpr; // in ASTUFTermNode
    ASTOperatorNode multOp; // in ASTUFTermNode
    ASTOperatorNode concatOp; // in ASTUFTermNode
    ASTUFPrimaryNode rhsPrimary; // in ASTUFTermNode
    ASTUFFactorNode rhsExpr; // in ASTUFTermNode
    ASTUFFactorNode UFFactor; // in ASTUFTermNode

    public ASTUFTermNode getLhsExpr()
    {
        return this.lhsExpr;
    }

    public void setLhsExpr(ASTUFTermNode newValue)
    {
        this.lhsExpr = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public ASTOperatorNode getMultOp()
    {
        return this.multOp;
    }

    public void setMultOp(ASTOperatorNode newValue)
    {
        this.multOp = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public ASTOperatorNode getConcatOp()
    {
        return this.concatOp;
    }

    public void setConcatOp(ASTOperatorNode newValue)
    {
        this.concatOp = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public ASTUFPrimaryNode getRhsPrimary()
    {
        return this.rhsPrimary;
    }

    public void setRhsPrimary(ASTUFPrimaryNode newValue)
    {
        this.rhsPrimary = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public ASTUFFactorNode getRhsExpr()
    {
        return this.rhsExpr;
    }

    public void setRhsExpr(ASTUFFactorNode newValue)
    {
        this.rhsExpr = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public ASTUFFactorNode getUFFactor()
    {
        return this.UFFactor;
    }

    public void setUFFactor(ASTUFFactorNode newValue)
    {
        this.UFFactor = newValue;
        if (newValue != null) newValue.setParent(this);
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
        case 0:  return this.lhsExpr;
        case 1:  return this.multOp;
        case 2:  return this.concatOp;
        case 3:  return this.rhsPrimary;
        case 4:  return this.rhsExpr;
        case 5:  return this.UFFactor;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }

    @Override protected void setASTField(int index, IASTNode value)
    {
        switch (index)
        {
        case 0:  this.lhsExpr = (ASTUFTermNode)value; if (value != null) value.setParent(this); return;
        case 1:  this.multOp = (ASTOperatorNode)value; if (value != null) value.setParent(this); return;
        case 2:  this.concatOp = (ASTOperatorNode)value; if (value != null) value.setParent(this); return;
        case 3:  this.rhsPrimary = (ASTUFPrimaryNode)value; if (value != null) value.setParent(this); return;
        case 4:  this.rhsExpr = (ASTUFFactorNode)value; if (value != null) value.setParent(this); return;
        case 5:  this.UFFactor = (ASTUFFactorNode)value; if (value != null) value.setParent(this); return;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }
}

