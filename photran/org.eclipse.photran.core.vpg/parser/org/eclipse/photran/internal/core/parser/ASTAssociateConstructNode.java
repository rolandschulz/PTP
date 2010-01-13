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

import org.eclipse.photran.internal.core.parser.Parser.ASTListNode;
import org.eclipse.photran.internal.core.parser.Parser.ASTNode;
import org.eclipse.photran.internal.core.parser.Parser.ASTNodeWithErrorRecoverySymbols;
import org.eclipse.photran.internal.core.parser.Parser.IASTListNode;
import org.eclipse.photran.internal.core.parser.Parser.IASTNode;
import org.eclipse.photran.internal.core.parser.Parser.IASTVisitor;
import org.eclipse.photran.internal.core.lexer.Token;

import org.eclipse.photran.internal.core.lexer.*;                   import org.eclipse.photran.internal.core.analysis.binding.ScopingNode;                   import org.eclipse.photran.internal.core.SyntaxException;                   import java.io.IOException;

@SuppressWarnings({ "unchecked", "unused" })
public class ASTAssociateConstructNode extends ASTNode implements IExecutableConstruct
{
    ASTAssociateStmtNode associateStmt; // in ASTAssociateConstructNode
    IASTListNode<IExecutionPartConstruct> associateBody; // in ASTAssociateConstructNode
    ASTEndAssociateStmtNode endAssociateStmt; // in ASTAssociateConstructNode

    public ASTAssociateStmtNode getAssociateStmt()
    {
        return this.associateStmt;
    }

    public void setAssociateStmt(ASTAssociateStmtNode newValue)
    {
        this.associateStmt = newValue;
    }


    public IASTListNode<IExecutionPartConstruct> getAssociateBody()
    {
        return this.associateBody;
    }

    public void setAssociateBody(IASTListNode<IExecutionPartConstruct> newValue)
    {
        this.associateBody = newValue;
    }


    public ASTEndAssociateStmtNode getEndAssociateStmt()
    {
        return this.endAssociateStmt;
    }

    public void setEndAssociateStmt(ASTEndAssociateStmtNode newValue)
    {
        this.endAssociateStmt = newValue;
    }


    public void accept(IASTVisitor visitor)
    {
        visitor.visitASTAssociateConstructNode(this);
        visitor.visitIExecutableConstruct(this);
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
        case 0:  return this.associateStmt;
        case 1:  return this.associateBody;
        case 2:  return this.endAssociateStmt;
        default: return null;
        }
    }

    @Override protected void setASTField(int index, IASTNode value)
    {
        switch (index)
        {
        case 0:  this.associateStmt = (ASTAssociateStmtNode)value; return;
        case 1:  this.associateBody = (IASTListNode<IExecutionPartConstruct>)value; return;
        case 2:  this.endAssociateStmt = (ASTEndAssociateStmtNode)value; return;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }
}

