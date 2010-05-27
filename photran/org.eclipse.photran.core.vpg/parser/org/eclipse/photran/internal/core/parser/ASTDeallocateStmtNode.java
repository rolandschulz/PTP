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
public class ASTDeallocateStmtNode extends ASTNode implements IActionStmt
{
    org.eclipse.photran.internal.core.lexer.Token label; // in ASTDeallocateStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTDeallocate; // in ASTDeallocateStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTLparen; // in ASTDeallocateStmtNode
    IASTListNode<IASTListNode<ASTAllocateObjectNode>> allocateObjectList; // in ASTDeallocateStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTComma; // in ASTDeallocateStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTStateq; // in ASTDeallocateStmtNode
    ASTVariableNode statusVariable; // in ASTDeallocateStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTRparen; // in ASTDeallocateStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTEos; // in ASTDeallocateStmtNode

    public org.eclipse.photran.internal.core.lexer.Token getLabel()
    {
        return this.label;
    }

    public void setLabel(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.label = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public IASTListNode<IASTListNode<ASTAllocateObjectNode>> getAllocateObjectList()
    {
        return this.allocateObjectList;
    }

    public void setAllocateObjectList(IASTListNode<IASTListNode<ASTAllocateObjectNode>> newValue)
    {
        this.allocateObjectList = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public ASTVariableNode getStatusVariable()
    {
        return this.statusVariable;
    }

    public void setStatusVariable(ASTVariableNode newValue)
    {
        this.statusVariable = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    @Override
    public void accept(IASTVisitor visitor)
    {
        visitor.visitASTDeallocateStmtNode(this);
        visitor.visitIActionStmt(this);
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
        case 1:  return this.hiddenTDeallocate;
        case 2:  return this.hiddenTLparen;
        case 3:  return this.allocateObjectList;
        case 4:  return this.hiddenTComma;
        case 5:  return this.hiddenTStateq;
        case 6:  return this.statusVariable;
        case 7:  return this.hiddenTRparen;
        case 8:  return this.hiddenTEos;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }

    @Override protected void setASTField(int index, IASTNode value)
    {
        switch (index)
        {
        case 0:  this.label = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 1:  this.hiddenTDeallocate = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 2:  this.hiddenTLparen = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 3:  this.allocateObjectList = (IASTListNode<IASTListNode<ASTAllocateObjectNode>>)value; if (value != null) value.setParent(this); return;
        case 4:  this.hiddenTComma = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 5:  this.hiddenTStateq = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 6:  this.statusVariable = (ASTVariableNode)value; if (value != null) value.setParent(this); return;
        case 7:  this.hiddenTRparen = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 8:  this.hiddenTEos = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }
}

