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

public class ASTCloseStmtNode extends ASTNode implements IActionStmt
{
    org.eclipse.photran.internal.core.lexer.Token label; // in ASTCloseStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTClose; // in ASTCloseStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTLparen; // in ASTCloseStmtNode
    IASTListNode<ASTCloseSpecListNode> closeSpecList; // in ASTCloseStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTRparen; // in ASTCloseStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTEos; // in ASTCloseStmtNode

    public org.eclipse.photran.internal.core.lexer.Token getLabel()
    {
        return this.label;
    }

    public void setLabel(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.label = newValue;
    }


    public IASTListNode<ASTCloseSpecListNode> getCloseSpecList()
    {
        return this.closeSpecList;
    }

    public void setCloseSpecList(IASTListNode<ASTCloseSpecListNode> newValue)
    {
        this.closeSpecList = newValue;
    }


    public void accept(IASTVisitor visitor)
    {
        visitor.visitASTCloseStmtNode(this);
        visitor.visitIActionStmt(this);
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
        case 0:  return this.label;
        case 1:  return this.hiddenTClose;
        case 2:  return this.hiddenTLparen;
        case 3:  return this.closeSpecList;
        case 4:  return this.hiddenTRparen;
        case 5:  return this.hiddenTEos;
        default: return null;
        }
    }

    @Override protected void setASTField(int index, IASTNode value)
    {
        switch (index)
        {
        case 0:  this.label = (org.eclipse.photran.internal.core.lexer.Token)value;
        case 1:  this.hiddenTClose = (org.eclipse.photran.internal.core.lexer.Token)value;
        case 2:  this.hiddenTLparen = (org.eclipse.photran.internal.core.lexer.Token)value;
        case 3:  this.closeSpecList = (IASTListNode<ASTCloseSpecListNode>)value;
        case 4:  this.hiddenTRparen = (org.eclipse.photran.internal.core.lexer.Token)value;
        case 5:  this.hiddenTEos = (org.eclipse.photran.internal.core.lexer.Token)value;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }
}

