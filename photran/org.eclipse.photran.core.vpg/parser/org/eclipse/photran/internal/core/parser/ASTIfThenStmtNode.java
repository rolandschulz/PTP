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

public class ASTIfThenStmtNode extends ASTNodeWithErrorRecoverySymbols
{
    org.eclipse.photran.internal.core.lexer.Token label; // in ASTIfThenStmtNode
    org.eclipse.photran.internal.core.lexer.Token name; // in ASTIfThenStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTColon; // in ASTIfThenStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTIf; // in ASTIfThenStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTLparen; // in ASTIfThenStmtNode
    IExpr guardingExpression; // in ASTIfThenStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTRparen; // in ASTIfThenStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTThen; // in ASTIfThenStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTEos; // in ASTIfThenStmtNode

    public org.eclipse.photran.internal.core.lexer.Token getLabel()
    {
        return this.label;
    }

    public void setLabel(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.label = newValue;
    }


    public org.eclipse.photran.internal.core.lexer.Token getName()
    {
        return this.name;
    }

    public void setName(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.name = newValue;
    }


    public IExpr getGuardingExpression()
    {
        return this.guardingExpression;
    }

    public void setGuardingExpression(IExpr newValue)
    {
        this.guardingExpression = newValue;
    }


    public void accept(IASTVisitor visitor)
    {
        visitor.visitASTIfThenStmtNode(this);
        visitor.visitASTNode(this);
    }

    @Override protected int getNumASTFields()
    {
        return 9;
    }

    @Override protected IASTNode getASTField(int index)
    {
        switch (index)
        {
        case 0:  return this.label;
        case 1:  return this.name;
        case 2:  return this.hiddenTColon;
        case 3:  return this.hiddenTIf;
        case 4:  return this.hiddenTLparen;
        case 5:  return this.guardingExpression;
        case 6:  return this.hiddenTRparen;
        case 7:  return this.hiddenTThen;
        case 8:  return this.hiddenTEos;
        default: return null;
        }
    }

    @Override protected void setASTField(int index, IASTNode value)
    {
        switch (index)
        {
        case 0:  this.label = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 1:  this.name = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 2:  this.hiddenTColon = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 3:  this.hiddenTIf = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 4:  this.hiddenTLparen = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 5:  this.guardingExpression = (IExpr)value; return;
        case 6:  this.hiddenTRparen = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 7:  this.hiddenTThen = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 8:  this.hiddenTEos = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }
}

