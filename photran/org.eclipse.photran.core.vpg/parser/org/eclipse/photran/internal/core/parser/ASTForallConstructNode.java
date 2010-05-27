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
public class ASTForallConstructNode extends ASTNode implements IBodyConstruct, ICaseBodyConstruct, IExecutableConstruct, IExecutionPartConstruct, IForallBodyConstruct
{
    ASTForallConstructStmtNode forallConstructStmt; // in ASTForallConstructNode
    IASTListNode<IForallBodyConstruct> forallBody; // in ASTForallConstructNode
    ASTEndForallStmtNode endForallStmt; // in ASTForallConstructNode

    public ASTForallConstructStmtNode getForallConstructStmt()
    {
        return this.forallConstructStmt;
    }

    public void setForallConstructStmt(ASTForallConstructStmtNode newValue)
    {
        this.forallConstructStmt = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public IASTListNode<IForallBodyConstruct> getForallBody()
    {
        return this.forallBody;
    }

    public void setForallBody(IASTListNode<IForallBodyConstruct> newValue)
    {
        this.forallBody = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public ASTEndForallStmtNode getEndForallStmt()
    {
        return this.endForallStmt;
    }

    public void setEndForallStmt(ASTEndForallStmtNode newValue)
    {
        this.endForallStmt = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    @Override
    public void accept(IASTVisitor visitor)
    {
        visitor.visitASTForallConstructNode(this);
        visitor.visitIBodyConstruct(this);
        visitor.visitICaseBodyConstruct(this);
        visitor.visitIExecutableConstruct(this);
        visitor.visitIExecutionPartConstruct(this);
        visitor.visitIForallBodyConstruct(this);
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
        case 0:  return this.forallConstructStmt;
        case 1:  return this.forallBody;
        case 2:  return this.endForallStmt;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }

    @Override protected void setASTField(int index, IASTNode value)
    {
        switch (index)
        {
        case 0:  this.forallConstructStmt = (ASTForallConstructStmtNode)value; if (value != null) value.setParent(this); return;
        case 1:  this.forallBody = (IASTListNode<IForallBodyConstruct>)value; if (value != null) value.setParent(this); return;
        case 2:  this.endForallStmt = (ASTEndForallStmtNode)value; if (value != null) value.setParent(this); return;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }
}

