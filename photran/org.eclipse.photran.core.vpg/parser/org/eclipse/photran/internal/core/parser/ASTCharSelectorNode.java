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

public class ASTCharSelectorNode extends ASTNode
{
    org.eclipse.photran.internal.core.lexer.Token hiddenTAsterisk; // in ASTCharSelectorNode
    org.eclipse.photran.internal.core.lexer.Token constIntLength; // in ASTCharSelectorNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTLparen; // in ASTCharSelectorNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTLeneq; // in ASTCharSelectorNode
    IExpr lengthExpr; // in ASTCharSelectorNode
    org.eclipse.photran.internal.core.lexer.Token isAssumedLength; // in ASTCharSelectorNode
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
    }


    public IExpr getLengthExpr()
    {
        return this.lengthExpr;
    }

    public void setLengthExpr(IExpr newValue)
    {
        this.lengthExpr = newValue;
    }


    public boolean isAssumedLength()
    {
        return this.isAssumedLength != null;
    }

    public void setIsAssumedLength(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.isAssumedLength = newValue;
    }


    public IExpr getKindExpr()
    {
        return this.kindExpr;
    }

    public void setKindExpr(IExpr newValue)
    {
        this.kindExpr = newValue;
    }


    public void accept(IASTVisitor visitor)
    {
        visitor.visitASTCharSelectorNode(this);
        visitor.visitASTNode(this);
    }

    @Override protected int getNumASTFields()
    {
        return 10;
    }

    @Override protected IASTNode getASTField(int index)
    {
        switch (index)
        {
        case 0:  return this.hiddenTAsterisk;
        case 1:  return this.constIntLength;
        case 2:  return this.hiddenTLparen;
        case 3:  return this.hiddenTLeneq;
        case 4:  return this.lengthExpr;
        case 5:  return this.isAssumedLength;
        case 6:  return this.hiddenTComma;
        case 7:  return this.hiddenTKindeq;
        case 8:  return this.kindExpr;
        case 9:  return this.hiddenTRparen;
        default: return null;
        }
    }

    @Override protected void setASTField(int index, IASTNode value)
    {
        switch (index)
        {
        case 0:  this.hiddenTAsterisk = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 1:  this.constIntLength = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 2:  this.hiddenTLparen = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 3:  this.hiddenTLeneq = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 4:  this.lengthExpr = (IExpr)value; return;
        case 5:  this.isAssumedLength = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 6:  this.hiddenTComma = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 7:  this.hiddenTKindeq = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 8:  this.kindExpr = (IExpr)value; return;
        case 9:  this.hiddenTRparen = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }
}

