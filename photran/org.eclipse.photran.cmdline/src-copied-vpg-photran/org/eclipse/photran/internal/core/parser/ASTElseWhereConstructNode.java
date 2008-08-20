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

public class ASTElseWhereConstructNode extends ASTNode
{
    ASTElseWhereStmtNode elseWhereStmt; // in ASTElseWhereConstructNode
    IASTListNode<IWhereBodyConstruct> whereBodyConstructBlock; // in ASTElseWhereConstructNode
    ASTEndWhereStmtNode endWhereStmt; // in ASTElseWhereConstructNode

    public ASTElseWhereStmtNode getElseWhereStmt()
    {
        return this.elseWhereStmt;
    }

    public void setElseWhereStmt(ASTElseWhereStmtNode newValue)
    {
        this.elseWhereStmt = newValue;
    }


    public IASTListNode<IWhereBodyConstruct> getWhereBodyConstructBlock()
    {
        return this.whereBodyConstructBlock;
    }

    public void setWhereBodyConstructBlock(IASTListNode<IWhereBodyConstruct> newValue)
    {
        this.whereBodyConstructBlock = newValue;
    }


    public ASTEndWhereStmtNode getEndWhereStmt()
    {
        return this.endWhereStmt;
    }

    public void setEndWhereStmt(ASTEndWhereStmtNode newValue)
    {
        this.endWhereStmt = newValue;
    }


    public void accept(IASTVisitor visitor)
    {
        visitor.visitASTElseWhereConstructNode(this);
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
        case 0:  return this.elseWhereStmt;
        case 1:  return this.whereBodyConstructBlock;
        case 2:  return this.endWhereStmt;
        default: return null;
        }
    }

    @Override protected void setASTField(int index, IASTNode value)
    {
        switch (index)
        {
        case 0:  this.elseWhereStmt = (ASTElseWhereStmtNode)value; return;
        case 1:  this.whereBodyConstructBlock = (IASTListNode<IWhereBodyConstruct>)value; return;
        case 2:  this.endWhereStmt = (ASTEndWhereStmtNode)value; return;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }
}

