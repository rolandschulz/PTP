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

public class ASTEndTypeStmtNode extends ASTNode
{
    org.eclipse.photran.internal.core.lexer.Token label; // in ASTEndTypeStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTEndtype; // in ASTEndTypeStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTEnd; // in ASTEndTypeStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTType; // in ASTEndTypeStmtNode
    ASTTypeNameNode typeName; // in ASTEndTypeStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTEos; // in ASTEndTypeStmtNode

    public org.eclipse.photran.internal.core.lexer.Token getLabel()
    {
        return this.label;
    }

    public void setLabel(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.label = newValue;
    }


    public ASTTypeNameNode getTypeName()
    {
        return this.typeName;
    }

    public void setTypeName(ASTTypeNameNode newValue)
    {
        this.typeName = newValue;
    }


    public void accept(IASTVisitor visitor)
    {
        visitor.visitASTEndTypeStmtNode(this);
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
        case 1:  return this.hiddenTEndtype;
        case 2:  return this.hiddenTEnd;
        case 3:  return this.hiddenTType;
        case 4:  return this.typeName;
        case 5:  return this.hiddenTEos;
        default: return null;
        }
    }

    @Override protected void setASTField(int index, IASTNode value)
    {
        switch (index)
        {
        case 0:  this.label = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 1:  this.hiddenTEndtype = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 2:  this.hiddenTEnd = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 3:  this.hiddenTType = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 4:  this.typeName = (ASTTypeNameNode)value; return;
        case 5:  this.hiddenTEos = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }
}

