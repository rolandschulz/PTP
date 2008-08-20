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

public class ASTUFFactorNode extends ASTNode
{
    ASTUFPrimaryNode rhsPrimary; // in ASTUFFactorNode
    ASTUFPrimaryNode lhsPrimary; // in ASTUFFactorNode
    ASTOperatorNode powerOp; // in ASTUFFactorNode
    ASTUFFactorNode rhsExpr; // in ASTUFFactorNode

    public ASTUFPrimaryNode getRhsPrimary()
    {
        return this.rhsPrimary;
    }

    public void setRhsPrimary(ASTUFPrimaryNode newValue)
    {
        this.rhsPrimary = newValue;
    }


    public ASTUFPrimaryNode getLhsPrimary()
    {
        return this.lhsPrimary;
    }

    public void setLhsPrimary(ASTUFPrimaryNode newValue)
    {
        this.lhsPrimary = newValue;
    }


    public ASTOperatorNode getPowerOp()
    {
        return this.powerOp;
    }

    public void setPowerOp(ASTOperatorNode newValue)
    {
        this.powerOp = newValue;
    }


    public ASTUFFactorNode getRhsExpr()
    {
        return this.rhsExpr;
    }

    public void setRhsExpr(ASTUFFactorNode newValue)
    {
        this.rhsExpr = newValue;
    }


    public void accept(IASTVisitor visitor)
    {
        visitor.visitASTUFFactorNode(this);
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
        case 0:  return this.rhsPrimary;
        case 1:  return this.lhsPrimary;
        case 2:  return this.powerOp;
        case 3:  return this.rhsExpr;
        default: return null;
        }
    }

    @Override protected void setASTField(int index, IASTNode value)
    {
        switch (index)
        {
        case 0:  this.rhsPrimary = (ASTUFPrimaryNode)value; return;
        case 1:  this.lhsPrimary = (ASTUFPrimaryNode)value; return;
        case 2:  this.powerOp = (ASTOperatorNode)value; return;
        case 3:  this.rhsExpr = (ASTUFFactorNode)value; return;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }
}

