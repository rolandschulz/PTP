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

public class ASTComponentInitializationNode extends ASTNode
{
    org.eclipse.photran.internal.core.lexer.Token assignsNull; // in ASTComponentInitializationNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTNull; // in ASTComponentInitializationNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTLparen; // in ASTComponentInitializationNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTRparen; // in ASTComponentInitializationNode
    org.eclipse.photran.internal.core.lexer.Token assignsExpr; // in ASTComponentInitializationNode
    ASTExprNode assignedExpr; // in ASTComponentInitializationNode

    public boolean assignsNull()
    {
        return this.assignsNull != null;
    }

    public void setAssignsNull(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.assignsNull = newValue;
    }


    public boolean assignsExpr()
    {
        return this.assignsExpr != null;
    }

    public void setAssignsExpr(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.assignsExpr = newValue;
    }


    public ASTExprNode getAssignedExpr()
    {
        return this.assignedExpr;
    }

    public void setAssignedExpr(ASTExprNode newValue)
    {
        this.assignedExpr = newValue;
    }


    public void accept(IASTVisitor visitor)
    {
        visitor.visitASTComponentInitializationNode(this);
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
        case 0:  return this.assignsNull;
        case 1:  return this.hiddenTNull;
        case 2:  return this.hiddenTLparen;
        case 3:  return this.hiddenTRparen;
        case 4:  return this.assignsExpr;
        case 5:  return this.assignedExpr;
        default: return null;
        }
    }

    @Override protected void setASTField(int index, IASTNode value)
    {
        switch (index)
        {
        case 0:  this.assignsNull = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 1:  this.hiddenTNull = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 2:  this.hiddenTLparen = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 3:  this.hiddenTRparen = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 4:  this.assignsExpr = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 5:  this.assignedExpr = (ASTExprNode)value; return;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }
}

