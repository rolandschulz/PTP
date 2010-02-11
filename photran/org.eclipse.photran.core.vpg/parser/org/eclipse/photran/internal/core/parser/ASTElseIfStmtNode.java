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
public class ASTElseIfStmtNode extends ASTNodeWithErrorRecoverySymbols
{
    org.eclipse.photran.internal.core.lexer.Token label; // in ASTElseIfStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTElse; // in ASTElseIfStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTIf; // in ASTElseIfStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTElseif; // in ASTElseIfStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTLparen; // in ASTElseIfStmtNode
    IExpr guardingExpression; // in ASTElseIfStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTRparen; // in ASTElseIfStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTThen; // in ASTElseIfStmtNode
    org.eclipse.photran.internal.core.lexer.Token endName; // in ASTElseIfStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTEos; // in ASTElseIfStmtNode

    public org.eclipse.photran.internal.core.lexer.Token getLabel()
    {
        return this.label;
    }

    public void setLabel(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.label = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public IExpr getGuardingExpression()
    {
        return this.guardingExpression;
    }

    public void setGuardingExpression(IExpr newValue)
    {
        this.guardingExpression = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public org.eclipse.photran.internal.core.lexer.Token getEndName()
    {
        return this.endName;
    }

    public void setEndName(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.endName = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public void accept(IASTVisitor visitor)
    {
        visitor.visitASTElseIfStmtNode(this);
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
        case 0:  return this.label;
        case 1:  return this.hiddenTElse;
        case 2:  return this.hiddenTIf;
        case 3:  return this.hiddenTElseif;
        case 4:  return this.hiddenTLparen;
        case 5:  return this.guardingExpression;
        case 6:  return this.hiddenTRparen;
        case 7:  return this.hiddenTThen;
        case 8:  return this.endName;
        case 9:  return this.hiddenTEos;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }

    @Override protected void setASTField(int index, IASTNode value)
    {
        switch (index)
        {
        case 0:  this.label = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 1:  this.hiddenTElse = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 2:  this.hiddenTIf = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 3:  this.hiddenTElseif = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 4:  this.hiddenTLparen = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 5:  this.guardingExpression = (IExpr)value; if (value != null) value.setParent(this); return;
        case 6:  this.hiddenTRparen = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 7:  this.hiddenTThen = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 8:  this.endName = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 9:  this.hiddenTEos = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }
}

