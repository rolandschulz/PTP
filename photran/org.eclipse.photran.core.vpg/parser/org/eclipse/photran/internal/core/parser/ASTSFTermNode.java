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
public class ASTSFTermNode extends ASTNode
{
    ASTSFTermNode lhsExpr; // in ASTSFTermNode
    ASTSFFactorNode SFFactor; // in ASTSFTermNode
    ASTOperatorNode multOp; // in ASTSFTermNode
    IExpr rhsExpr; // in ASTSFTermNode

    public ASTSFTermNode getLhsExpr()
    {
        return this.lhsExpr;
    }

    public void setLhsExpr(ASTSFTermNode newValue)
    {
        this.lhsExpr = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public ASTSFFactorNode getSFFactor()
    {
        return this.SFFactor;
    }

    public void setSFFactor(ASTSFFactorNode newValue)
    {
        this.SFFactor = newValue;
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


    public IExpr getRhsExpr()
    {
        return this.rhsExpr;
    }

    public void setRhsExpr(IExpr newValue)
    {
        this.rhsExpr = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public void accept(IASTVisitor visitor)
    {
        visitor.visitASTSFTermNode(this);
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
        case 1:  return this.SFFactor;
        case 2:  return this.multOp;
        case 3:  return this.rhsExpr;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }

    @Override protected void setASTField(int index, IASTNode value)
    {
        switch (index)
        {
        case 0:  this.lhsExpr = (ASTSFTermNode)value; if (value != null) value.setParent(this); return;
        case 1:  this.SFFactor = (ASTSFFactorNode)value; if (value != null) value.setParent(this); return;
        case 2:  this.multOp = (ASTOperatorNode)value; if (value != null) value.setParent(this); return;
        case 3:  this.rhsExpr = (IExpr)value; if (value != null) value.setParent(this); return;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }
}

