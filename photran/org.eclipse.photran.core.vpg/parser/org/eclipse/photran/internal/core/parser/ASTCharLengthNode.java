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

public class ASTCharLengthNode extends ASTNode
{
    org.eclipse.photran.internal.core.lexer.Token hiddenTLparen; // in ASTCharLengthNode
    org.eclipse.photran.internal.core.lexer.Token isColon; // in ASTCharLengthNode
    org.eclipse.photran.internal.core.lexer.Token constIntLength; // in ASTCharLengthNode
    org.eclipse.photran.internal.core.lexer.Token isAssumedLength; // in ASTCharLengthNode
    IExpr lengthExpr; // in ASTCharLengthNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTRparen; // in ASTCharLengthNode

    public boolean isColon()
    {
        return this.isColon != null;
    }

    public void setIsColon(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.isColon = newValue;
    }


    public org.eclipse.photran.internal.core.lexer.Token getConstIntLength()
    {
        return this.constIntLength;
    }

    public void setConstIntLength(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.constIntLength = newValue;
    }


    public boolean isAssumedLength()
    {
        return this.isAssumedLength != null;
    }

    public void setIsAssumedLength(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.isAssumedLength = newValue;
    }


    public IExpr getLengthExpr()
    {
        return this.lengthExpr;
    }

    public void setLengthExpr(IExpr newValue)
    {
        this.lengthExpr = newValue;
    }


    public void accept(IASTVisitor visitor)
    {
        visitor.visitASTCharLengthNode(this);
        visitor.visitASTNode(this);
    }

    @Override protected int getNumASTFields()
    {
        return 6;
    }

    @Override protected IASTNode getASTField(int index)
    {
        switch (index)
        {
        case 0:  return this.hiddenTLparen;
        case 1:  return this.isColon;
        case 2:  return this.constIntLength;
        case 3:  return this.isAssumedLength;
        case 4:  return this.lengthExpr;
        case 5:  return this.hiddenTRparen;
        default: return null;
        }
    }

    @Override protected void setASTField(int index, IASTNode value)
    {
        switch (index)
        {
        case 0:  this.hiddenTLparen = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 1:  this.isColon = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 2:  this.constIntLength = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 3:  this.isAssumedLength = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 4:  this.lengthExpr = (IExpr)value; return;
        case 5:  this.hiddenTRparen = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }
}

