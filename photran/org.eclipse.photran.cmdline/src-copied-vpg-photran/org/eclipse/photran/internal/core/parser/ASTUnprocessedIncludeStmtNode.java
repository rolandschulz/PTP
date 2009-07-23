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

public class ASTUnprocessedIncludeStmtNode extends ASTNode implements ISpecificationStmt
{
    org.eclipse.photran.internal.core.lexer.Token label; // in ASTUnprocessedIncludeStmtNode
    org.eclipse.photran.internal.core.lexer.Token tIdent; // in ASTUnprocessedIncludeStmtNode
    org.eclipse.photran.internal.core.lexer.Token tScon; // in ASTUnprocessedIncludeStmtNode
    org.eclipse.photran.internal.core.lexer.Token tEos; // in ASTUnprocessedIncludeStmtNode

    public org.eclipse.photran.internal.core.lexer.Token getLabel()
    {
        return this.label;
    }

    public void setLabel(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.label = newValue;
    }


    public org.eclipse.photran.internal.core.lexer.Token getTIdent()
    {
        return this.tIdent;
    }

    public void setTIdent(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.tIdent = newValue;
    }


    public org.eclipse.photran.internal.core.lexer.Token getTScon()
    {
        return this.tScon;
    }

    public void setTScon(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.tScon = newValue;
    }


    public org.eclipse.photran.internal.core.lexer.Token getTEos()
    {
        return this.tEos;
    }

    public void setTEos(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.tEos = newValue;
    }


    public void accept(IASTVisitor visitor)
    {
        visitor.visitASTUnprocessedIncludeStmtNode(this);
        visitor.visitISpecificationStmt(this);
        visitor.visitASTNode(this);
    }

    @Override protected int getNumASTFields()
    {
        return 4;
    }

    @Override protected IASTNode getASTField(int index)
    {
        switch (index)
        {
        case 0:  return this.label;
        case 1:  return this.tIdent;
        case 2:  return this.tScon;
        case 3:  return this.tEos;
        default: return null;
        }
    }

    @Override protected void setASTField(int index, IASTNode value)
    {
        switch (index)
        {
        case 0:  this.label = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 1:  this.tIdent = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 2:  this.tScon = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 3:  this.tEos = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }
}

