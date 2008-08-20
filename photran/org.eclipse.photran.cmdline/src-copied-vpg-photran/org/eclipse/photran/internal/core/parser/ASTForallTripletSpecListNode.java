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

public class ASTForallTripletSpecListNode extends ASTNode
{
    ASTNameNode name; // in ASTForallTripletSpecListNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTEquals; // in ASTForallTripletSpecListNode
    ASTSubscriptNode lb; // in ASTForallTripletSpecListNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTColon; // in ASTForallTripletSpecListNode
    ASTSubscriptNode ub; // in ASTForallTripletSpecListNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTColon2; // in ASTForallTripletSpecListNode
    ASTExprNode stepExpr; // in ASTForallTripletSpecListNode

    public ASTNameNode getName()
    {
        return this.name;
    }

    public void setName(ASTNameNode newValue)
    {
        this.name = newValue;
    }


    public ASTSubscriptNode getLb()
    {
        return this.lb;
    }

    public void setLb(ASTSubscriptNode newValue)
    {
        this.lb = newValue;
    }


    public ASTSubscriptNode getUb()
    {
        return this.ub;
    }

    public void setUb(ASTSubscriptNode newValue)
    {
        this.ub = newValue;
    }


    public ASTExprNode getStepExpr()
    {
        return this.stepExpr;
    }

    public void setStepExpr(ASTExprNode newValue)
    {
        this.stepExpr = newValue;
    }


    public void accept(IASTVisitor visitor)
    {
        visitor.visitASTForallTripletSpecListNode(this);
        visitor.visitASTNode(this);
    }

    @Override protected int getNumASTFields()
    {
        return 7;
    }

    @Override protected IASTNode getASTField(int index)
    {
        switch (index)
        {
        case 0:  return this.name;
        case 1:  return this.hiddenTEquals;
        case 2:  return this.lb;
        case 3:  return this.hiddenTColon;
        case 4:  return this.ub;
        case 5:  return this.hiddenTColon2;
        case 6:  return this.stepExpr;
        default: return null;
        }
    }

    @Override protected void setASTField(int index, IASTNode value)
    {
        switch (index)
        {
        case 0:  this.name = (ASTNameNode)value; return;
        case 1:  this.hiddenTEquals = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 2:  this.lb = (ASTSubscriptNode)value; return;
        case 3:  this.hiddenTColon = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 4:  this.ub = (ASTSubscriptNode)value; return;
        case 5:  this.hiddenTColon2 = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 6:  this.stepExpr = (ASTExprNode)value; return;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }
}

