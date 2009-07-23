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

public class ASTBlockDataSubprogramNode extends ScopingNode implements IProgramUnit
{
    ASTBlockDataStmtNode blockDataStmt; // in ASTBlockDataSubprogramNode
    IASTListNode<IBlockDataBodyConstruct> blockDataBody; // in ASTBlockDataSubprogramNode
    ASTEndBlockDataStmtNode endBlockDataStmt; // in ASTBlockDataSubprogramNode

    public ASTBlockDataStmtNode getBlockDataStmt()
    {
        return this.blockDataStmt;
    }

    public void setBlockDataStmt(ASTBlockDataStmtNode newValue)
    {
        this.blockDataStmt = newValue;
    }


    public IASTListNode<IBlockDataBodyConstruct> getBlockDataBody()
    {
        return this.blockDataBody;
    }

    public void setBlockDataBody(IASTListNode<IBlockDataBodyConstruct> newValue)
    {
        this.blockDataBody = newValue;
    }


    public ASTEndBlockDataStmtNode getEndBlockDataStmt()
    {
        return this.endBlockDataStmt;
    }

    public void setEndBlockDataStmt(ASTEndBlockDataStmtNode newValue)
    {
        this.endBlockDataStmt = newValue;
    }


    public void accept(IASTVisitor visitor)
    {
        visitor.visitASTBlockDataSubprogramNode(this);
        visitor.visitIProgramUnit(this);
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
        case 0:  return this.blockDataStmt;
        case 1:  return this.blockDataBody;
        case 2:  return this.endBlockDataStmt;
        default: return null;
        }
    }

    @Override protected void setASTField(int index, IASTNode value)
    {
        switch (index)
        {
        case 0:  this.blockDataStmt = (ASTBlockDataStmtNode)value; return;
        case 1:  this.blockDataBody = (IASTListNode<IBlockDataBodyConstruct>)value; return;
        case 2:  this.endBlockDataStmt = (ASTEndBlockDataStmtNode)value; return;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }
}

