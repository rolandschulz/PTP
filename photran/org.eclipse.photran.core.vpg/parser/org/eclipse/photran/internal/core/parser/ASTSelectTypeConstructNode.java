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
public class ASTSelectTypeConstructNode extends ASTNode implements IExecutableConstruct
{
    ASTSelectTypeStmtNode selectTypeStmt; // in ASTSelectTypeConstructNode
    IASTListNode<ASTSelectTypeBodyNode> selectTypeBody; // in ASTSelectTypeConstructNode
    ASTEndSelectTypeStmtNode endSelectTypeStmt; // in ASTSelectTypeConstructNode

    public ASTSelectTypeStmtNode getSelectTypeStmt()
    {
        return this.selectTypeStmt;
    }

    public void setSelectTypeStmt(ASTSelectTypeStmtNode newValue)
    {
        this.selectTypeStmt = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public IASTListNode<ASTSelectTypeBodyNode> getSelectTypeBody()
    {
        return this.selectTypeBody;
    }

    public void setSelectTypeBody(IASTListNode<ASTSelectTypeBodyNode> newValue)
    {
        this.selectTypeBody = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public ASTEndSelectTypeStmtNode getEndSelectTypeStmt()
    {
        return this.endSelectTypeStmt;
    }

    public void setEndSelectTypeStmt(ASTEndSelectTypeStmtNode newValue)
    {
        this.endSelectTypeStmt = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public void accept(IASTVisitor visitor)
    {
        visitor.visitASTSelectTypeConstructNode(this);
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
        case 0:  return this.selectTypeStmt;
        case 1:  return this.selectTypeBody;
        case 2:  return this.endSelectTypeStmt;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }

    @Override protected void setASTField(int index, IASTNode value)
    {
        switch (index)
        {
        case 0:  this.selectTypeStmt = (ASTSelectTypeStmtNode)value; if (value != null) value.setParent(this); return;
        case 1:  this.selectTypeBody = (IASTListNode<ASTSelectTypeBodyNode>)value; if (value != null) value.setParent(this); return;
        case 2:  this.endSelectTypeStmt = (ASTEndSelectTypeStmtNode)value; if (value != null) value.setParent(this); return;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }
}

