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
public class ASTStmtFunctionStmtNode extends ASTNode implements IObsoleteActionStmt
{
    org.eclipse.photran.internal.core.lexer.Token label; // in ASTStmtFunctionStmtNode
    ASTNameNode name; // in ASTStmtFunctionStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTLparen; // in ASTStmtFunctionStmtNode
    IASTListNode<ASTSFDummyArgNameListNode> SFDummyArgNameList; // in ASTStmtFunctionStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTRparen; // in ASTStmtFunctionStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTEquals; // in ASTStmtFunctionStmtNode
    IExpr expr; // in ASTStmtFunctionStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTEos; // in ASTStmtFunctionStmtNode

    public org.eclipse.photran.internal.core.lexer.Token getLabel()
    {
        return this.label;
    }

    public void setLabel(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.label = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public ASTNameNode getName()
    {
        return this.name;
    }

    public void setName(ASTNameNode newValue)
    {
        this.name = newValue;
        if (newValue != null) newValue.setParent(this);
    }


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
        visitor.visitASTStmtFunctionStmtNode(this);
        visitor.visitIObsoleteActionStmt(this);
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
        case 0:  return this.label;
        case 1:  return this.name;
        case 2:  return this.hiddenTLparen;
        case 3:  return this.SFDummyArgNameList;
        case 4:  return this.hiddenTRparen;
        case 5:  return this.hiddenTEquals;
        case 6:  return this.expr;
        case 7:  return this.hiddenTEos;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }

    @Override protected void setASTField(int index, IASTNode value)
    {
        switch (index)
        {
        case 0:  this.label = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 1:  this.name = (ASTNameNode)value; if (value != null) value.setParent(this); return;
        case 2:  this.hiddenTLparen = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 3:  this.SFDummyArgNameList = (IASTListNode<ASTSFDummyArgNameListNode>)value; if (value != null) value.setParent(this); return;
        case 4:  this.hiddenTRparen = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 5:  this.hiddenTEquals = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 6:  this.expr = (IExpr)value; if (value != null) value.setParent(this); return;
        case 7:  this.hiddenTEos = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }
}

