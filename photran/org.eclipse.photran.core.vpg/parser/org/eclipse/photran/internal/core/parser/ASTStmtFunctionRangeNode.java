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
public class ASTStmtFunctionRangeNode extends ASTNode
{
    org.eclipse.photran.internal.core.lexer.Token hiddenTLparen; // in ASTStmtFunctionRangeNode
    IASTListNode<ASTSFDummyArgNameListNode> SFDummyArgNameList; // in ASTStmtFunctionRangeNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTRparen; // in ASTStmtFunctionRangeNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTEquals; // in ASTStmtFunctionRangeNode
    IExpr expr; // in ASTStmtFunctionRangeNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTEos; // in ASTStmtFunctionRangeNode

    public IASTListNode<ASTSFDummyArgNameListNode> getSFDummyArgNameList()
    {
        return this.SFDummyArgNameList;
    }

    public void setSFDummyArgNameList(IASTListNode<ASTSFDummyArgNameListNode> newValue)
    {
        this.SFDummyArgNameList = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public IExpr getExpr()
    {
        return this.expr;
    }

    public void setExpr(IExpr newValue)
    {
        this.expr = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public void accept(IASTVisitor visitor)
    {
        visitor.visitASTStmtFunctionRangeNode(this);
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
        case 1:  return this.SFDummyArgNameList;
        case 2:  return this.hiddenTRparen;
        case 3:  return this.hiddenTEquals;
        case 4:  return this.expr;
        case 5:  return this.hiddenTEos;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }

    @Override protected void setASTField(int index, IASTNode value)
    {
        switch (index)
        {
        case 0:  this.hiddenTLparen = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 1:  this.SFDummyArgNameList = (IASTListNode<ASTSFDummyArgNameListNode>)value; if (value != null) value.setParent(this); return;
        case 2:  this.hiddenTRparen = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 3:  this.hiddenTEquals = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 4:  this.expr = (IExpr)value; if (value != null) value.setParent(this); return;
        case 5:  this.hiddenTEos = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }
}

