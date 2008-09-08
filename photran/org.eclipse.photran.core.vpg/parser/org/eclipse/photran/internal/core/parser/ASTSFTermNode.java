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

public class ASTSFTermNode extends ASTNode
{
    ASTSFTermNode lhsExpr; // in ASTSFTermNode
    ASTOperatorNode multOp; // in ASTSFTermNode
    ASTSFFactorNode SFFactor; // in ASTSFTermNode
    IExpr rhsExpr; // in ASTSFTermNode

    public ASTSFTermNode getLhsExpr()
    {
        return this.lhsExpr;
    }

    public void setLhsExpr(ASTSFTermNode newValue)
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


    public ASTSFFactorNode getSFFactor()
    {
        return this.SFFactor;
    }

    public void setSFFactor(ASTSFFactorNode newValue)
    {
        this.SFFactor = newValue;
    }


    public IExpr getRhsExpr()
    {
        return this.rhsExpr;
    }

    public void setRhsExpr(IExpr newValue)
    {
        this.rhsExpr = newValue;
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
        case 1:  return this.multOp;
        case 2:  return this.SFFactor;
        case 3:  return this.rhsExpr;
        default: return null;
        }
    }

    @Override protected void setASTField(int index, IASTNode value)
    {
        switch (index)
        {
        case 0:  this.lhsExpr = (ASTSFTermNode)value; return;
        case 1:  this.multOp = (ASTOperatorNode)value; return;
        case 2:  this.SFFactor = (ASTSFFactorNode)value; return;
        case 3:  this.rhsExpr = (IExpr)value; return;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }
}

