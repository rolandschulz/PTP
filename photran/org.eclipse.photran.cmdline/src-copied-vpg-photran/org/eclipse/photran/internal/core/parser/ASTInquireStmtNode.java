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

public class ASTInquireStmtNode extends ASTNode implements IActionStmt
{
    org.eclipse.photran.internal.core.lexer.Token label; // in ASTInquireStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTInquire; // in ASTInquireStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTLparen; // in ASTInquireStmtNode
    IASTListNode<ASTInquireSpecListNode> inquireSpecList; // in ASTInquireStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTIolengtheq; // in ASTInquireStmtNode
    ASTScalarVariableNode ioLengthVar; // in ASTInquireStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTRparen; // in ASTInquireStmtNode
    ASTOutputItemListNode outputItemList; // in ASTInquireStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTEos; // in ASTInquireStmtNode

    public org.eclipse.photran.internal.core.lexer.Token getLabel()
    {
        return this.label;
    }

    public void setLabel(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.label = newValue;
    }


    public IASTListNode<ASTInquireSpecListNode> getInquireSpecList()
    {
        return this.inquireSpecList;
    }

    public void setInquireSpecList(IASTListNode<ASTInquireSpecListNode> newValue)
    {
        this.inquireSpecList = newValue;
    }


    public ASTScalarVariableNode getIoLengthVar()
    {
        return this.ioLengthVar;
    }

    public void setIoLengthVar(ASTScalarVariableNode newValue)
    {
        this.ioLengthVar = newValue;
    }


    public ASTOutputItemListNode getOutputItemList()
    {
        return this.outputItemList;
    }

    public void setOutputItemList(ASTOutputItemListNode newValue)
    {
        this.outputItemList = newValue;
    }


    public void accept(IASTVisitor visitor)
    {
        visitor.visitASTInquireStmtNode(this);
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
        case 1:  return this.hiddenTInquire;
        case 2:  return this.hiddenTLparen;
        case 3:  return this.inquireSpecList;
        case 4:  return this.hiddenTIolengtheq;
        case 5:  return this.ioLengthVar;
        case 6:  return this.hiddenTRparen;
        case 7:  return this.outputItemList;
        case 8:  return this.hiddenTEos;
        default: return null;
        }
    }

    @Override protected void setASTField(int index, IASTNode value)
    {
        switch (index)
        {
        case 0:  this.label = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 1:  this.hiddenTInquire = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 2:  this.hiddenTLparen = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 3:  this.inquireSpecList = (IASTListNode<ASTInquireSpecListNode>)value; return;
        case 4:  this.hiddenTIolengtheq = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 5:  this.ioLengthVar = (ASTScalarVariableNode)value; return;
        case 6:  this.hiddenTRparen = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 7:  this.outputItemList = (ASTOutputItemListNode)value; return;
        case 8:  this.hiddenTEos = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }
}

