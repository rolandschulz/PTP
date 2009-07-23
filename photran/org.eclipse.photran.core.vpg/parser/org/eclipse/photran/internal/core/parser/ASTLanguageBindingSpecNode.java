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

import org.eclipse.photran.internal.core.lexer.*;                   import org.eclipse.photran.internal.core.analysis.binding.ScopingNode;                   import org.eclipse.photran.internal.core.SyntaxException;                   import java.io.IOException;

public class ASTLanguageBindingSpecNode extends ASTNode
{
    org.eclipse.photran.internal.core.lexer.Token isBind; // in ASTLanguageBindingSpecNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTLparen; // in ASTLanguageBindingSpecNode
    org.eclipse.photran.internal.core.lexer.Token language; // in ASTLanguageBindingSpecNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTComma; // in ASTLanguageBindingSpecNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTIdent; // in ASTLanguageBindingSpecNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTEquals; // in ASTLanguageBindingSpecNode
    IExpr expr; // in ASTLanguageBindingSpecNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTRparen; // in ASTLanguageBindingSpecNode

    public boolean isBind()
    {
        return this.isBind != null;
    }

    public void setIsBind(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.isBind = newValue;
    }


    public org.eclipse.photran.internal.core.lexer.Token getLanguage()
    {
        return this.language;
    }

    public void setLanguage(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.language = newValue;
    }


    public IExpr getExpr()
    {
        return this.expr;
    }

    public void setExpr(IExpr newValue)
    {
        this.expr = newValue;
    }


    public void accept(IASTVisitor visitor)
    {
        visitor.visitASTLanguageBindingSpecNode(this);
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
        case 0:  return this.isBind;
        case 1:  return this.hiddenTLparen;
        case 2:  return this.language;
        case 3:  return this.hiddenTComma;
        case 4:  return this.hiddenTIdent;
        case 5:  return this.hiddenTEquals;
        case 6:  return this.expr;
        case 7:  return this.hiddenTRparen;
        default: return null;
        }
    }

    @Override protected void setASTField(int index, IASTNode value)
    {
        switch (index)
        {
        case 0:  this.isBind = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 1:  this.hiddenTLparen = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 2:  this.language = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 3:  this.hiddenTComma = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 4:  this.hiddenTIdent = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 5:  this.hiddenTEquals = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 6:  this.expr = (IExpr)value; return;
        case 7:  this.hiddenTRparen = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }
}

