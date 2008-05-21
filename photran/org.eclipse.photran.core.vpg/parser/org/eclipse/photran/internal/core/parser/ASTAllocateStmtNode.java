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

public class ASTAllocateStmtNode extends ASTNode implements IActionStmt
{
    org.eclipse.photran.internal.core.lexer.Token label; // in ASTAllocateStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTAllocate; // in ASTAllocateStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTLparen; // in ASTAllocateStmtNode
    IASTListNode<ASTAllocationNode> allocationList; // in ASTAllocateStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTComma; // in ASTAllocateStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTStateq; // in ASTAllocateStmtNode
    ASTVariableNode statusVariable; // in ASTAllocateStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTRparen; // in ASTAllocateStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTEos; // in ASTAllocateStmtNode

    public org.eclipse.photran.internal.core.lexer.Token getLabel()
    {
        return this.label;
    }

    public void setLabel(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.label = newValue;
    }


    public IASTListNode<ASTAllocationNode> getAllocationList()
    {
        return this.allocationList;
    }

    public void setAllocationList(IASTListNode<ASTAllocationNode> newValue)
    {
        this.allocationList = newValue;
    }


    public ASTVariableNode getStatusVariable()
    {
        return this.statusVariable;
    }

    public void setStatusVariable(ASTVariableNode newValue)
    {
        this.statusVariable = newValue;
    }


    public void accept(IASTVisitor visitor)
    {
        visitor.visitASTAllocateStmtNode(this);
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
        case 1:  return this.hiddenTAllocate;
        case 2:  return this.hiddenTLparen;
        case 3:  return this.allocationList;
        case 4:  return this.hiddenTComma;
        case 5:  return this.hiddenTStateq;
        case 6:  return this.statusVariable;
        case 7:  return this.hiddenTRparen;
        case 8:  return this.hiddenTEos;
        default: return null;
        }
    }

    @Override protected void setASTField(int index, IASTNode value)
    {
        switch (index)
        {
        case 0:  this.label = (org.eclipse.photran.internal.core.lexer.Token)value;
        case 1:  this.hiddenTAllocate = (org.eclipse.photran.internal.core.lexer.Token)value;
        case 2:  this.hiddenTLparen = (org.eclipse.photran.internal.core.lexer.Token)value;
        case 3:  this.allocationList = (IASTListNode<ASTAllocationNode>)value;
        case 4:  this.hiddenTComma = (org.eclipse.photran.internal.core.lexer.Token)value;
        case 5:  this.hiddenTStateq = (org.eclipse.photran.internal.core.lexer.Token)value;
        case 6:  this.statusVariable = (ASTVariableNode)value;
        case 7:  this.hiddenTRparen = (org.eclipse.photran.internal.core.lexer.Token)value;
        case 8:  this.hiddenTEos = (org.eclipse.photran.internal.core.lexer.Token)value;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }
}

