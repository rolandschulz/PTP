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
public class ASTLoopControlNode extends ASTNode
{
    org.eclipse.photran.internal.core.lexer.Token hiddenTWhile; // in ASTLoopControlNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTLparen; // in ASTLoopControlNode
    IExpr whileExpr; // in ASTLoopControlNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTRparen; // in ASTLoopControlNode
    org.eclipse.photran.internal.core.lexer.Token variableName; // in ASTLoopControlNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTEquals; // in ASTLoopControlNode
    IExpr lb; // in ASTLoopControlNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTComma; // in ASTLoopControlNode
    IExpr ub; // in ASTLoopControlNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTComma2; // in ASTLoopControlNode
    IExpr step; // in ASTLoopControlNode

    public IExpr getWhileExpr()
    {
        return this.whileExpr;
    }

    public void setWhileExpr(IExpr newValue)
    {
        this.whileExpr = newValue;
    }


    public org.eclipse.photran.internal.core.lexer.Token getVariableName()
    {
        return this.variableName;
    }

    public void setVariableName(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.variableName = newValue;
    }


    public IExpr getLb()
    {
        return this.lb;
    }

    public void setLb(IExpr newValue)
    {
        this.lb = newValue;
    }


    public IExpr getUb()
    {
        return this.ub;
    }

    public void setUb(IExpr newValue)
    {
        this.ub = newValue;
    }


    public IExpr getStep()
    {
        return this.step;
    }

    public void setStep(IExpr newValue)
    {
        this.step = newValue;
    }


    public void accept(IASTVisitor visitor)
    {
        visitor.visitASTLoopControlNode(this);
        visitor.visitASTNode(this);
    }

    @Override protected int getNumASTFields()
    {
        return 11;
    }

    @Override protected IASTNode getASTField(int index)
    {
        switch (index)
        {
        case 0:  return this.hiddenTWhile;
        case 1:  return this.hiddenTLparen;
        case 2:  return this.whileExpr;
        case 3:  return this.hiddenTRparen;
        case 4:  return this.variableName;
        case 5:  return this.hiddenTEquals;
        case 6:  return this.lb;
        case 7:  return this.hiddenTComma;
        case 8:  return this.ub;
        case 9:  return this.hiddenTComma2;
        case 10: return this.step;
        default: return null;
        }
    }

    @Override protected void setASTField(int index, IASTNode value)
    {
        switch (index)
        {
        case 0:  this.hiddenTWhile = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 1:  this.hiddenTLparen = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 2:  this.whileExpr = (IExpr)value; return;
        case 3:  this.hiddenTRparen = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 4:  this.variableName = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 5:  this.hiddenTEquals = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 6:  this.lb = (IExpr)value; return;
        case 7:  this.hiddenTComma = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 8:  this.ub = (IExpr)value; return;
        case 9:  this.hiddenTComma2 = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 10: this.step = (IExpr)value; return;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }
}

