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

public class ASTCallStmtNode extends ASTNodeWithErrorRecoverySymbols implements IActionStmt
{
    org.eclipse.photran.internal.core.lexer.Token label; // in ASTCallStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTCall; // in ASTCallStmtNode
    IASTListNode<ASTDataRefNode> dataRef; // in ASTCallStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTLparen; // in ASTCallStmtNode
    IASTListNode<ASTSubroutineArgNode> subroutineArgList; // in ASTCallStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTRparen; // in ASTCallStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTEos; // in ASTCallStmtNode

    public org.eclipse.photran.internal.core.lexer.Token getLabel()
    {
        return this.label;
    }

    public void setLabel(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.label = newValue;
    }


    public IASTListNode<ASTDataRefNode> getDataRef()
    {
        return this.dataRef;
    }

    public void setDataRef(IASTListNode<ASTDataRefNode> newValue)
    {
        this.dataRef = newValue;
    }


    public IASTListNode<ASTSubroutineArgNode> getSubroutineArgList()
    {
        return this.subroutineArgList;
    }

    public void setSubroutineArgList(IASTListNode<ASTSubroutineArgNode> newValue)
    {
        this.subroutineArgList = newValue;
    }


    public void accept(IASTVisitor visitor)
    {
        visitor.visitASTCallStmtNode(this);
        visitor.visitIActionStmt(this);
        visitor.visitASTNode(this);
    }

    @Override protected int getNumASTFields()
    {
        return 7;
    }

    @Override protected IASTNode getASTField(int index)
    {
        switch (index)
        {
        case 0:  return this.label;
        case 1:  return this.hiddenTCall;
        case 2:  return this.dataRef;
        case 3:  return this.hiddenTLparen;
        case 4:  return this.subroutineArgList;
        case 5:  return this.hiddenTRparen;
        case 6:  return this.hiddenTEos;
        default: return null;
        }
    }

    @Override protected void setASTField(int index, IASTNode value)
    {
        switch (index)
        {
        case 0:  this.label = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 1:  this.hiddenTCall = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 2:  this.dataRef = (IASTListNode<ASTDataRefNode>)value; return;
        case 3:  this.hiddenTLparen = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 4:  this.subroutineArgList = (IASTListNode<ASTSubroutineArgNode>)value; return;
        case 5:  this.hiddenTRparen = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 6:  this.hiddenTEos = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }
}

