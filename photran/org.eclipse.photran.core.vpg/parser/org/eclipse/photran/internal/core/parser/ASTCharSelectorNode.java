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
public class ASTCharSelectorNode extends ASTNode
{
    org.eclipse.photran.internal.core.lexer.Token hiddenTLparen2; // in ASTCharSelectorNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTAsterisk; // in ASTCharSelectorNode
    org.eclipse.photran.internal.core.lexer.Token constIntLength; // in ASTCharSelectorNode
    ASTNameNode constNameLength; // in ASTCharSelectorNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTLparen; // in ASTCharSelectorNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTKindEq2; // in ASTCharSelectorNode
    IExpr kindExpr2; // in ASTCharSelectorNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTComma2; // in ASTCharSelectorNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTLeneq; // in ASTCharSelectorNode
    org.eclipse.photran.internal.core.lexer.Token isAssumedLength; // in ASTCharSelectorNode
    IExpr lengthExpr; // in ASTCharSelectorNode
    org.eclipse.photran.internal.core.lexer.Token isColon; // in ASTCharSelectorNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTComma; // in ASTCharSelectorNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTKindeq; // in ASTCharSelectorNode
    IExpr kindExpr; // in ASTCharSelectorNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTRparen; // in ASTCharSelectorNode

    public org.eclipse.photran.internal.core.lexer.Token getConstIntLength()
    {
        return this.constIntLength;
    }

    public void setConstIntLength(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.constIntLength = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public ASTNameNode getConstNameLength()
    {
        return this.constNameLength;
    }

    public void setConstNameLength(ASTNameNode newValue)
    {
        this.constNameLength = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public IExpr getKindExpr2()
    {
        return this.kindExpr2;
    }

    public void setKindExpr2(IExpr newValue)
    {
        this.kindExpr2 = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public boolean isAssumedLength()
    {
        return this.isAssumedLength != null;
    }

    public void setIsAssumedLength(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.isAssumedLength = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public IExpr getLengthExpr()
    {
        return this.lengthExpr;
    }

    public void setLengthExpr(IExpr newValue)
    {
        this.lengthExpr = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public boolean isColon()
    {
        return this.isColon != null;
    }

    public void setIsColon(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.isColon = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public IExpr getKindExpr()
    {
        return this.kindExpr;
    }

    public void setKindExpr(IExpr newValue)
    {
        this.kindExpr = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    @Override
    public void accept(IASTVisitor visitor)
    {
        visitor.visitASTCharSelectorNode(this);
        visitor.visitASTNode(this);
    }

    @Override protected int getNumASTFields()
    {
        return 16;
    }

    @Override protected IASTNode getASTField(int index)
    {
        switch (index)
        {
        case 0:  return this.hiddenTLparen2;
        case 1:  return this.hiddenTAsterisk;
        case 2:  return this.constIntLength;
        case 3:  return this.constNameLength;
        case 4:  return this.hiddenTLparen;
        case 5:  return this.hiddenTKindEq2;
        case 6:  return this.kindExpr2;
        case 7:  return this.hiddenTComma2;
        case 8:  return this.hiddenTLeneq;
        case 9:  return this.isAssumedLength;
        case 10: return this.lengthExpr;
        case 11: return this.isColon;
        case 12: return this.hiddenTComma;
        case 13: return this.hiddenTKindeq;
        case 14: return this.kindExpr;
        case 15: return this.hiddenTRparen;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }

    @Override protected void setASTField(int index, IASTNode value)
    {
        switch (index)
        {
        case 0:  this.hiddenTLparen2 = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 1:  this.hiddenTAsterisk = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 2:  this.constIntLength = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 3:  this.constNameLength = (ASTNameNode)value; if (value != null) value.setParent(this); return;
        case 4:  this.hiddenTLparen = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 5:  this.hiddenTKindEq2 = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 6:  this.kindExpr2 = (IExpr)value; if (value != null) value.setParent(this); return;
        case 7:  this.hiddenTComma2 = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 8:  this.hiddenTLeneq = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 9:  this.isAssumedLength = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 10: this.lengthExpr = (IExpr)value; if (value != null) value.setParent(this); return;
        case 11: this.isColon = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 12: this.hiddenTComma = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 13: this.hiddenTKindeq = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 14: this.kindExpr = (IExpr)value; if (value != null) value.setParent(this); return;
        case 15: this.hiddenTRparen = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }
}

