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

public class ASTElseIfConstructNode extends ASTNode
{
    ASTElseIfStmtNode elseIfStmt; // in ASTElseIfConstructNode
    IASTListNode<IExecutionPartConstruct> conditionalBody; // in ASTElseIfConstructNode
    ASTEndIfStmtNode endIfStmt; // in ASTElseIfConstructNode
    ASTElseIfConstructNode elseIfConstruct; // in ASTElseIfConstructNode
    ASTElseConstructNode elseConstruct; // in ASTElseIfConstructNode

    public ASTElseIfStmtNode getElseIfStmt()
    {
        return this.elseIfStmt;
    }

    public void setElseIfStmt(ASTElseIfStmtNode newValue)
    {
        this.elseIfStmt = newValue;
    }


    public IASTListNode<IExecutionPartConstruct> getConditionalBody()
    {
        return this.conditionalBody;
    }

    public void setConditionalBody(IASTListNode<IExecutionPartConstruct> newValue)
    {
        this.conditionalBody = newValue;
    }


    public ASTEndIfStmtNode getEndIfStmt()
    {
        return this.endIfStmt;
    }

    public void setEndIfStmt(ASTEndIfStmtNode newValue)
    {
        this.endIfStmt = newValue;
    }


    public ASTElseIfConstructNode getElseIfConstruct()
    {
        return this.elseIfConstruct;
    }

    public void setElseIfConstruct(ASTElseIfConstructNode newValue)
    {
        this.elseIfConstruct = newValue;
    }


    public ASTElseConstructNode getElseConstruct()
    {
        return this.elseConstruct;
    }

    public void setElseConstruct(ASTElseConstructNode newValue)
    {
        this.elseConstruct = newValue;
    }


    public void accept(IASTVisitor visitor)
    {
        visitor.visitASTElseIfConstructNode(this);
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
        case 0:  return this.elseIfStmt;
        case 1:  return this.conditionalBody;
        case 2:  return this.endIfStmt;
        case 3:  return this.elseIfConstruct;
        case 4:  return this.elseConstruct;
        default: return null;
        }
    }

    @Override protected void setASTField(int index, IASTNode value)
    {
        switch (index)
        {
        case 0:  this.elseIfStmt = (ASTElseIfStmtNode)value; return;
        case 1:  this.conditionalBody = (IASTListNode<IExecutionPartConstruct>)value; return;
        case 2:  this.endIfStmt = (ASTEndIfStmtNode)value; return;
        case 3:  this.elseIfConstruct = (ASTElseIfConstructNode)value; return;
        case 4:  this.elseConstruct = (ASTElseConstructNode)value; return;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }
}

