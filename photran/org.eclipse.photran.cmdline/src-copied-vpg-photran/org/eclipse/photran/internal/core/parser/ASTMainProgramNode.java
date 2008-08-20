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

public class ASTMainProgramNode extends ScopingNode implements IProgramUnit
{
    ASTProgramStmtNode programStmt; // in ASTMainProgramNode
    IASTListNode<IBodyConstruct> body; // in ASTMainProgramNode
    ASTContainsStmtNode containsStmt; // in ASTMainProgramNode
    IASTListNode<IInternalSubprogram> internalSubprograms; // in ASTMainProgramNode
    ASTEndProgramStmtNode endProgramStmt; // in ASTMainProgramNode

    public ASTProgramStmtNode getProgramStmt()
    {
        return this.programStmt;
    }

    public void setProgramStmt(ASTProgramStmtNode newValue)
    {
        this.programStmt = newValue;
    }


    public IASTListNode<IBodyConstruct> getBody()
    {
        return this.body;
    }

    public void setBody(IASTListNode<IBodyConstruct> newValue)
    {
        this.body = newValue;
    }


    public ASTContainsStmtNode getContainsStmt()
    {
        return this.containsStmt;
    }

    public void setContainsStmt(ASTContainsStmtNode newValue)
    {
        this.containsStmt = newValue;
    }


    public IASTListNode<IInternalSubprogram> getInternalSubprograms()
    {
        return this.internalSubprograms;
    }

    public void setInternalSubprograms(IASTListNode<IInternalSubprogram> newValue)
    {
        this.internalSubprograms = newValue;
    }


    public ASTEndProgramStmtNode getEndProgramStmt()
    {
        return this.endProgramStmt;
    }

    public void setEndProgramStmt(ASTEndProgramStmtNode newValue)
    {
        this.endProgramStmt = newValue;
    }


    public void accept(IASTVisitor visitor)
    {
        visitor.visitASTMainProgramNode(this);
        visitor.visitIProgramUnit(this);
        visitor.visitASTNode(this);
    }

    @Override protected int getNumASTFields()
    {
        return 5;
    }

    @Override protected IASTNode getASTField(int index)
    {
        switch (index)
        {
        case 0:  return this.programStmt;
        case 1:  return this.body;
        case 2:  return this.containsStmt;
        case 3:  return this.internalSubprograms;
        case 4:  return this.endProgramStmt;
        default: return null;
        }
    }

    @Override protected void setASTField(int index, IASTNode value)
    {
        switch (index)
        {
        case 0:  this.programStmt = (ASTProgramStmtNode)value; return;
        case 1:  this.body = (IASTListNode<IBodyConstruct>)value; return;
        case 2:  this.containsStmt = (ASTContainsStmtNode)value; return;
        case 3:  this.internalSubprograms = (IASTListNode<IInternalSubprogram>)value; return;
        case 4:  this.endProgramStmt = (ASTEndProgramStmtNode)value; return;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }
}

