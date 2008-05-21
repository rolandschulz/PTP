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

public class ASTEndfileStmtNode extends ASTNode implements IActionStmt
{
    org.eclipse.photran.internal.core.lexer.Token label; // in ASTEndfileStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTEnd; // in ASTEndfileStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTEndfile; // in ASTEndfileStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTFile; // in ASTEndfileStmtNode
    ASTUnitIdentifierNode unitIdentifier; // in ASTEndfileStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTLparen; // in ASTEndfileStmtNode
    IASTListNode<ASTPositionSpecListNode> positionSpecList; // in ASTEndfileStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTRparen; // in ASTEndfileStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTEos; // in ASTEndfileStmtNode

    public org.eclipse.photran.internal.core.lexer.Token getLabel()
    {
        return this.label;
    }

    public void setLabel(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.label = newValue;
    }


    public ASTUnitIdentifierNode getUnitIdentifier()
    {
        return this.unitIdentifier;
    }

    public void setUnitIdentifier(ASTUnitIdentifierNode newValue)
    {
        this.unitIdentifier = newValue;
    }


    public IASTListNode<ASTPositionSpecListNode> getPositionSpecList()
    {
        return this.positionSpecList;
    }

    public void setPositionSpecList(IASTListNode<ASTPositionSpecListNode> newValue)
    {
        this.positionSpecList = newValue;
    }


    public void accept(IASTVisitor visitor)
    {
        visitor.visitASTEndfileStmtNode(this);
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
        case 1:  return this.hiddenTEnd;
        case 2:  return this.hiddenTEndfile;
        case 3:  return this.hiddenTFile;
        case 4:  return this.unitIdentifier;
        case 5:  return this.hiddenTLparen;
        case 6:  return this.positionSpecList;
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
        case 1:  this.hiddenTEnd = (org.eclipse.photran.internal.core.lexer.Token)value;
        case 2:  this.hiddenTEndfile = (org.eclipse.photran.internal.core.lexer.Token)value;
        case 3:  this.hiddenTFile = (org.eclipse.photran.internal.core.lexer.Token)value;
        case 4:  this.unitIdentifier = (ASTUnitIdentifierNode)value;
        case 5:  this.hiddenTLparen = (org.eclipse.photran.internal.core.lexer.Token)value;
        case 6:  this.positionSpecList = (IASTListNode<ASTPositionSpecListNode>)value;
        case 7:  this.hiddenTRparen = (org.eclipse.photran.internal.core.lexer.Token)value;
        case 8:  this.hiddenTEos = (org.eclipse.photran.internal.core.lexer.Token)value;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }
}

