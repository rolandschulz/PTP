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
public class ASTElsePartNode extends ASTNode
{
    IASTListNode<IExecutionPartConstruct> conditionalBody; // in ASTElsePartNode
    ASTEndIfStmtNode endIfStmt; // in ASTElsePartNode

    public IASTListNode<IExecutionPartConstruct> getConditionalBody()
    {
        return this.conditionalBody;
    }

    public void setConditionalBody(IASTListNode<IExecutionPartConstruct> newValue)
    {
        this.conditionalBody = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public ASTEndIfStmtNode getEndIfStmt()
    {
        return this.endIfStmt;
    }

    public void setEndIfStmt(ASTEndIfStmtNode newValue)
    {
        this.endIfStmt = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public void accept(IASTVisitor visitor)
    {
        visitor.visitASTElsePartNode(this);
        visitor.visitASTNode(this);
    }

    @Override protected int getNumASTFields()
    {
        return 2;
    }

    @Override protected IASTNode getASTField(int index)
    {
        switch (index)
        {
        case 0:  return this.conditionalBody;
        case 1:  return this.endIfStmt;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }

    @Override protected void setASTField(int index, IASTNode value)
    {
        switch (index)
        {
        case 0:  this.conditionalBody = (IASTListNode<IExecutionPartConstruct>)value; if (value != null) value.setParent(this); return;
        case 1:  this.endIfStmt = (ASTEndIfStmtNode)value; if (value != null) value.setParent(this); return;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }
}

