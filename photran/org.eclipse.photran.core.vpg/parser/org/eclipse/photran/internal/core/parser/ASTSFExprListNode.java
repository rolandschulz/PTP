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
public class ASTSFExprListNode extends ASTNode
{
    IASTListNode<ASTSFDummyArgNameListNode> SFDummyArgNameList; // in ASTSFExprListNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTComma; // in ASTSFExprListNode
    ASTSectionSubscriptNode sectionSubscript; // in ASTSFExprListNode
    ASTSFExprNode lb; // in ASTSFExprListNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTColon; // in ASTSFExprListNode
    IExpr ub; // in ASTSFExprListNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTColon2; // in ASTSFExprListNode
    IExpr step; // in ASTSFExprListNode

    public IASTListNode<ASTSFDummyArgNameListNode> getSFDummyArgNameList()
    {
        return this.SFDummyArgNameList;
    }

    public void setSFDummyArgNameList(IASTListNode<ASTSFDummyArgNameListNode> newValue)
    {
        this.SFDummyArgNameList = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public ASTSectionSubscriptNode getSectionSubscript()
    {
        return this.sectionSubscript;
    }

    public void setSectionSubscript(ASTSectionSubscriptNode newValue)
    {
        this.sectionSubscript = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public ASTSFExprNode getLb()
    {
        return this.lb;
    }

    public void setLb(ASTSFExprNode newValue)
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
        visitor.visitASTSFExprListNode(this);
        visitor.visitASTNode(this);
    }

    @Override protected int getNumASTFields()
    {
        return 8;
    }

    @Override protected IASTNode getASTField(int index)
    {
        switch (index)
        {
        case 0:  return this.SFDummyArgNameList;
        case 1:  return this.hiddenTComma;
        case 2:  return this.sectionSubscript;
        case 3:  return this.lb;
        case 4:  return this.hiddenTColon;
        case 5:  return this.ub;
        case 6:  return this.hiddenTColon2;
        case 7:  return this.step;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }

    @Override protected void setASTField(int index, IASTNode value)
    {
        switch (index)
        {
        case 0:  this.SFDummyArgNameList = (IASTListNode<ASTSFDummyArgNameListNode>)value; if (value != null) value.setParent(this); return;
        case 1:  this.hiddenTComma = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 2:  this.sectionSubscript = (ASTSectionSubscriptNode)value; if (value != null) value.setParent(this); return;
        case 3:  this.lb = (ASTSFExprNode)value; if (value != null) value.setParent(this); return;
        case 4:  this.hiddenTColon = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 5:  this.ub = (IExpr)value; if (value != null) value.setParent(this); return;
        case 6:  this.hiddenTColon2 = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 7:  this.step = (IExpr)value; if (value != null) value.setParent(this); return;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }
}

