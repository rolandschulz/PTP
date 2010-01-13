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
public class ASTInitializationNode extends ASTNode
{
    org.eclipse.photran.internal.core.lexer.Token assignsExpr; // in ASTInitializationNode
    IExpr assignedExpr; // in ASTInitializationNode
    org.eclipse.photran.internal.core.lexer.Token assignsNull; // in ASTInitializationNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTNull; // in ASTInitializationNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTLparen; // in ASTInitializationNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTRparen; // in ASTInitializationNode

    public boolean assignsExpr()
    {
        return this.assignsExpr != null;
    }

    public void setAssignsExpr(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.assignsExpr = newValue;
    }


    public IExpr getAssignedExpr()
    {
        return this.assignedExpr;
    }

    public void setAssignedExpr(IExpr newValue)
    {
        this.assignedExpr = newValue;
    }


    public boolean assignsNull()
    {
        return this.assignsNull != null;
    }

    public void setAssignsNull(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.assignsNull = newValue;
    }


    public void accept(IASTVisitor visitor)
    {
        visitor.visitASTInitializationNode(this);
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
        case 0:  return this.assignsExpr;
        case 1:  return this.assignedExpr;
        case 2:  return this.assignsNull;
        case 3:  return this.hiddenTNull;
        case 4:  return this.hiddenTLparen;
        case 5:  return this.hiddenTRparen;
        default: return null;
        }
    }

    @Override protected void setASTField(int index, IASTNode value)
    {
        switch (index)
        {
        case 0:  this.assignsExpr = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 1:  this.assignedExpr = (IExpr)value; return;
        case 2:  this.assignsNull = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 3:  this.hiddenTNull = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 4:  this.hiddenTLparen = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 5:  this.hiddenTRparen = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }
}

