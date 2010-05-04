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
public class ASTReadStmtNode extends ASTNode implements IActionStmt
{
    org.eclipse.photran.internal.core.lexer.Token label; // in ASTReadStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTRead; // in ASTReadStmtNode
    ASTRdFmtIdNode rdFmtId; // in ASTReadStmtNode
    ASTRdCtlSpecNode rdCtlSpec; // in ASTReadStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTComma; // in ASTReadStmtNode
    IASTListNode<IInputItem> inputItemList; // in ASTReadStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTEos; // in ASTReadStmtNode

    public org.eclipse.photran.internal.core.lexer.Token getLabel()
    {
        return this.label;
    }

    public void setLabel(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.label = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public ASTRdFmtIdNode getRdFmtId()
    {
        return this.rdFmtId;
    }

    public void setRdFmtId(ASTRdFmtIdNode newValue)
    {
        this.rdFmtId = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public ASTRdCtlSpecNode getRdCtlSpec()
    {
        return this.rdCtlSpec;
    }

    public void setRdCtlSpec(ASTRdCtlSpecNode newValue)
    {
        this.rdCtlSpec = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public IASTListNode<IInputItem> getInputItemList()
    {
        return this.inputItemList;
    }

    public void setInputItemList(IASTListNode<IInputItem> newValue)
    {
        this.inputItemList = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public void accept(IASTVisitor visitor)
    {
        visitor.visitASTReadStmtNode(this);
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
        case 1:  return this.hiddenTRead;
        case 2:  return this.rdFmtId;
        case 3:  return this.rdCtlSpec;
        case 4:  return this.hiddenTComma;
        case 5:  return this.inputItemList;
        case 6:  return this.hiddenTEos;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }

    @Override protected void setASTField(int index, IASTNode value)
    {
        switch (index)
        {
        case 0:  this.label = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 1:  this.hiddenTRead = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 2:  this.rdFmtId = (ASTRdFmtIdNode)value; if (value != null) value.setParent(this); return;
        case 3:  this.rdCtlSpec = (ASTRdCtlSpecNode)value; if (value != null) value.setParent(this); return;
        case 4:  this.hiddenTComma = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 5:  this.inputItemList = (IASTListNode<IInputItem>)value; if (value != null) value.setParent(this); return;
        case 6:  this.hiddenTEos = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }
}

