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

public class ASTElseConstructNode extends ASTNode
{
    ASTElseStmtNode elseStmt; // in ASTElseConstructNode
    IASTListNode<IExecutionPartConstruct> conditionalBody; // in ASTElseConstructNode
    ASTEndIfStmtNode endIfStmt; // in ASTElseConstructNode

    public ASTElseStmtNode getElseStmt()
    {
        return this.elseStmt;
    }

    public void setElseStmt(ASTElseStmtNode newValue)
    {
        this.elseStmt = newValue;
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


    public void accept(IASTVisitor visitor)
    {
        visitor.visitASTElseConstructNode(this);
        visitor.visitASTNode(this);
    }

    @Override protected int getNumASTFields()
    {
        return 3;
    }

    @Override protected IASTNode getASTField(int index)
    {
        switch (index)
        {
        case 0:  return this.elseStmt;
        case 1:  return this.conditionalBody;
        case 2:  return this.endIfStmt;
        default: return null;
        }
    }

    @Override protected void setASTField(int index, IASTNode value)
    {
        switch (index)
        {
        case 0:  this.elseStmt = (ASTElseStmtNode)value;
        case 1:  this.conditionalBody = (IASTListNode<IExecutionPartConstruct>)value;
        case 2:  this.endIfStmt = (ASTEndIfStmtNode)value;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }
}

