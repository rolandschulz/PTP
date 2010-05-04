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
public class ASTUFFactorNode extends ASTNode
{
    ASTUFPrimaryNode lhsPrimary; // in ASTUFFactorNode
    ASTOperatorNode powerOp; // in ASTUFFactorNode
    ASTUFFactorNode rhsExpr; // in ASTUFFactorNode
    ASTUFPrimaryNode UFPrimary; // in ASTUFFactorNode

    public ASTUFPrimaryNode getLhsPrimary()
    {
        return this.lhsPrimary;
    }

    public void setLhsPrimary(ASTUFPrimaryNode newValue)
    {
        this.lhsPrimary = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public ASTOperatorNode getPowerOp()
    {
        return this.powerOp;
    }

    public void setPowerOp(ASTOperatorNode newValue)
    {
        this.powerOp = newValue;
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


    public ASTUFPrimaryNode getUFPrimary()
    {
        return this.UFPrimary;
    }

    public void setUFPrimary(ASTUFPrimaryNode newValue)
    {
        this.UFPrimary = newValue;
        if (newValue != null) newValue.setParent(this);
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
        case 0:  return this.lhsPrimary;
        case 1:  return this.powerOp;
        case 2:  return this.rhsExpr;
        case 3:  return this.UFPrimary;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }

    @Override protected void setASTField(int index, IASTNode value)
    {
        switch (index)
        {
        case 0:  this.lhsPrimary = (ASTUFPrimaryNode)value; if (value != null) value.setParent(this); return;
        case 1:  this.powerOp = (ASTOperatorNode)value; if (value != null) value.setParent(this); return;
        case 2:  this.rhsExpr = (ASTUFFactorNode)value; if (value != null) value.setParent(this); return;
        case 3:  this.UFPrimary = (ASTUFPrimaryNode)value; if (value != null) value.setParent(this); return;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }
}

