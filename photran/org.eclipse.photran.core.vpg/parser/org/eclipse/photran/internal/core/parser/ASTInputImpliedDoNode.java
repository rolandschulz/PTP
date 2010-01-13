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
public class ASTInputImpliedDoNode extends ASTNode implements IInputItem
{
    org.eclipse.photran.internal.core.lexer.Token hiddenTLparen; // in ASTInputImpliedDoNode
    IASTListNode<IInputItem> inputItemList; // in ASTInputImpliedDoNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTComma; // in ASTInputImpliedDoNode
    org.eclipse.photran.internal.core.lexer.Token impliedDoVariable; // in ASTInputImpliedDoNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTEquals; // in ASTInputImpliedDoNode
    IExpr lb; // in ASTInputImpliedDoNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTComma2; // in ASTInputImpliedDoNode
    IExpr ub; // in ASTInputImpliedDoNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTComma3; // in ASTInputImpliedDoNode
    IExpr step; // in ASTInputImpliedDoNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTRparen; // in ASTInputImpliedDoNode

    public IASTListNode<IInputItem> getInputItemList()
    {
        return this.inputItemList;
    }

    public void setInputItemList(IASTListNode<IInputItem> newValue)
    {
        this.inputItemList = newValue;
    }


    public org.eclipse.photran.internal.core.lexer.Token getImpliedDoVariable()
    {
        return this.impliedDoVariable;
    }

    public void setImpliedDoVariable(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.impliedDoVariable = newValue;
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
        visitor.visitASTInputImpliedDoNode(this);
        visitor.visitIInputItem(this);
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
        case 0:  return this.hiddenTLparen;
        case 1:  return this.inputItemList;
        case 2:  return this.hiddenTComma;
        case 3:  return this.impliedDoVariable;
        case 4:  return this.hiddenTEquals;
        case 5:  return this.lb;
        case 6:  return this.hiddenTComma2;
        case 7:  return this.ub;
        case 8:  return this.hiddenTComma3;
        case 9:  return this.step;
        case 10: return this.hiddenTRparen;
        default: return null;
        }
    }

    @Override protected void setASTField(int index, IASTNode value)
    {
        switch (index)
        {
        case 0:  this.hiddenTLparen = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 1:  this.inputItemList = (IASTListNode<IInputItem>)value; return;
        case 2:  this.hiddenTComma = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 3:  this.impliedDoVariable = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 4:  this.hiddenTEquals = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 5:  this.lb = (IExpr)value; return;
        case 6:  this.hiddenTComma2 = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 7:  this.ub = (IExpr)value; return;
        case 8:  this.hiddenTComma3 = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 9:  this.step = (IExpr)value; return;
        case 10: this.hiddenTRparen = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }
}

