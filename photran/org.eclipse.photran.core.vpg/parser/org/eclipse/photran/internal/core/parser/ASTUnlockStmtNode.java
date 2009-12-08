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

public class ASTUnlockStmtNode extends ASTNode implements IActionStmt
{
    org.eclipse.photran.internal.core.lexer.Token label; // in ASTUnlockStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTUnlock; // in ASTUnlockStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTLparen; // in ASTUnlockStmtNode
    ASTNameNode lockVariable; // in ASTUnlockStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTComma; // in ASTUnlockStmtNode
    IASTListNode<ASTSyncStatNode> syncStatList; // in ASTUnlockStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTRparen; // in ASTUnlockStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTEos; // in ASTUnlockStmtNode

    public org.eclipse.photran.internal.core.lexer.Token getLabel()
    {
        return this.label;
    }

    public void setLabel(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.label = newValue;
    }


    public ASTNameNode getLockVariable()
    {
        return this.lockVariable;
    }

    public void setLockVariable(ASTNameNode newValue)
    {
        this.lockVariable = newValue;
    }


    public IASTListNode<ASTSyncStatNode> getSyncStatList()
    {
        return this.syncStatList;
    }

    public void setSyncStatList(IASTListNode<ASTSyncStatNode> newValue)
    {
        this.syncStatList = newValue;
    }


    public void accept(IASTVisitor visitor)
    {
        visitor.visitASTUnlockStmtNode(this);
        visitor.visitIActionStmt(this);
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
        case 1:  return this.hiddenTUnlock;
        case 2:  return this.hiddenTLparen;
        case 3:  return this.lockVariable;
        case 4:  return this.hiddenTComma;
        case 5:  return this.syncStatList;
        case 6:  return this.hiddenTRparen;
        case 7:  return this.hiddenTEos;
        default: return null;
        }
    }

    @Override protected void setASTField(int index, IASTNode value)
    {
        switch (index)
        {
        case 0:  this.label = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 1:  this.hiddenTUnlock = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 2:  this.hiddenTLparen = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 3:  this.lockVariable = (ASTNameNode)value; return;
        case 4:  this.hiddenTComma = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 5:  this.syncStatList = (IASTListNode<ASTSyncStatNode>)value; return;
        case 6:  this.hiddenTRparen = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 7:  this.hiddenTEos = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }
}

