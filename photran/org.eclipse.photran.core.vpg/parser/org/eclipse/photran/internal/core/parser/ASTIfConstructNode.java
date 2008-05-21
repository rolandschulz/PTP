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

public class ASTIfConstructNode extends ASTNode implements IExecutableConstruct
{
    ASTIfThenStmtNode ifThenStmt; // in ASTIfConstructNode
    IASTListNode<IExecutionPartConstruct> thenBody; // in ASTIfConstructNode
    IASTListNode<ASTElseIfConstructNode> elseIfParts; // in ASTIfConstructNode
    ASTElseStmtNode elseStmt; // in ASTIfConstructNode
    IASTListNode<IExecutionPartConstruct> elseBody; // in ASTIfConstructNode
    ASTEndIfStmtNode endIfStmt; // in ASTIfConstructNode

    public ASTIfThenStmtNode getIfThenStmt()
    {
        return this.ifThenStmt;
    }

    public void setIfThenStmt(ASTIfThenStmtNode newValue)
    {
        this.ifThenStmt = newValue;
    }


    public IASTListNode<IExecutionPartConstruct> getThenBody()
    {
        return this.thenBody;
    }

    public void setThenBody(IASTListNode<IExecutionPartConstruct> newValue)
    {
        this.thenBody = newValue;
    }


    public IASTListNode<ASTElseIfConstructNode> getElseIfParts()
    {
        return this.elseIfParts;
    }

    public void setElseIfParts(IASTListNode<ASTElseIfConstructNode> newValue)
    {
        this.elseIfParts = newValue;
    }


    public ASTElseStmtNode getElseStmt()
    {
        return this.elseStmt;
    }

    public void setElseStmt(ASTElseStmtNode newValue)
    {
        this.elseStmt = newValue;
    }


    public IASTListNode<IExecutionPartConstruct> getElseBody()
    {
        return this.elseBody;
    }

    public void setElseBody(IASTListNode<IExecutionPartConstruct> newValue)
    {
        this.elseBody = newValue;
    }


    public ASTEndIfStmtNode getEndIfStmt()
    {
        return this.endIfStmt;
    }

    public void setEndIfStmt(ASTEndIfStmtNode newValue)
    {
        this.endIfStmt = newValue;
    }


    public void accept(IASTVisitor visitor)
    {
        visitor.visitASTIfConstructNode(this);
        visitor.visitIExecutableConstruct(this);
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
        case 0:  return this.ifThenStmt;
        case 1:  return this.thenBody;
        case 2:  return this.elseIfParts;
        case 3:  return this.elseStmt;
        case 4:  return this.elseBody;
        case 5:  return this.endIfStmt;
        default: return null;
        }
    }

    @Override protected void setASTField(int index, IASTNode value)
    {
        switch (index)
        {
        case 0:  this.ifThenStmt = (ASTIfThenStmtNode)value;
        case 1:  this.thenBody = (IASTListNode<IExecutionPartConstruct>)value;
        case 2:  this.elseIfParts = (IASTListNode<ASTElseIfConstructNode>)value;
        case 3:  this.elseStmt = (ASTElseStmtNode)value;
        case 4:  this.elseBody = (IASTListNode<IExecutionPartConstruct>)value;
        case 5:  this.endIfStmt = (ASTEndIfStmtNode)value;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }
}

