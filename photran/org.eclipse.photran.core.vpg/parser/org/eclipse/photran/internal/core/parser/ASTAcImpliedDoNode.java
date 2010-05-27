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
public class ASTAcImpliedDoNode extends ASTNode
{
    org.eclipse.photran.internal.core.lexer.Token hiddenTLparen; // in ASTAcImpliedDoNode
    IExpr expr; // in ASTAcImpliedDoNode
    ASTAcImpliedDoNode nestedImpliedDo; // in ASTAcImpliedDoNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTComma; // in ASTAcImpliedDoNode
    ASTImpliedDoVariableNode impliedDoVariable; // in ASTAcImpliedDoNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTEquals; // in ASTAcImpliedDoNode
    IExpr lb; // in ASTAcImpliedDoNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTComma2; // in ASTAcImpliedDoNode
    IExpr ub; // in ASTAcImpliedDoNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTComma3; // in ASTAcImpliedDoNode
    IExpr step; // in ASTAcImpliedDoNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTRparen; // in ASTAcImpliedDoNode

    public IExpr getExpr()
    {
        return this.expr;
    }

    public void setExpr(IExpr newValue)
    {
        this.expr = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public ASTAcImpliedDoNode getNestedImpliedDo()
    {
        return this.nestedImpliedDo;
    }

    public void setNestedImpliedDo(ASTAcImpliedDoNode newValue)
    {
        this.nestedImpliedDo = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public ASTImpliedDoVariableNode getImpliedDoVariable()
    {
        return this.impliedDoVariable;
    }

    public void setImpliedDoVariable(ASTImpliedDoVariableNode newValue)
    {
        this.impliedDoVariable = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public IExpr getLb()
    {
        return this.lb;
    }

    public void setLb(IExpr newValue)
    {
        this.lb = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public IExpr getUb()
    {
        return this.ub;
    }

    public void setUb(IExpr newValue)
    {
        this.ub = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public IExpr getStep()
    {
        return this.step;
    }

    public void setStep(IExpr newValue)
    {
        this.step = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    @Override
    public void accept(IASTVisitor visitor)
    {
        visitor.visitASTAcImpliedDoNode(this);
        visitor.visitASTNode(this);
    }

    @Override protected int getNumASTFields()
    {
        return 12;
    }

    @Override protected IASTNode getASTField(int index)
    {
        switch (index)
        {
        case 0:  return this.hiddenTLparen;
        case 1:  return this.expr;
        case 2:  return this.nestedImpliedDo;
        case 3:  return this.hiddenTComma;
        case 4:  return this.impliedDoVariable;
        case 5:  return this.hiddenTEquals;
        case 6:  return this.lb;
        case 7:  return this.hiddenTComma2;
        case 8:  return this.ub;
        case 9:  return this.hiddenTComma3;
        case 10: return this.step;
        case 11: return this.hiddenTRparen;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }

    @Override protected void setASTField(int index, IASTNode value)
    {
        switch (index)
        {
        case 0:  this.hiddenTLparen = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 1:  this.expr = (IExpr)value; if (value != null) value.setParent(this); return;
        case 2:  this.nestedImpliedDo = (ASTAcImpliedDoNode)value; if (value != null) value.setParent(this); return;
        case 3:  this.hiddenTComma = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 4:  this.impliedDoVariable = (ASTImpliedDoVariableNode)value; if (value != null) value.setParent(this); return;
        case 5:  this.hiddenTEquals = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 6:  this.lb = (IExpr)value; if (value != null) value.setParent(this); return;
        case 7:  this.hiddenTComma2 = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 8:  this.ub = (IExpr)value; if (value != null) value.setParent(this); return;
        case 9:  this.hiddenTComma3 = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 10: this.step = (IExpr)value; if (value != null) value.setParent(this); return;
        case 11: this.hiddenTRparen = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }
}

