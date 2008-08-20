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

public class ASTWhereConstructNode extends ASTNode implements IBodyConstruct, ICaseBodyConstruct, IExecutableConstruct, IExecutionPartConstruct, IForallBodyConstruct, IWhereBodyConstruct
{
    ASTWhereConstructStmtNode whereConstructStmt; // in ASTWhereConstructNode
    IASTListNode<IWhereBodyConstruct> whereBodyConstructBlock; // in ASTWhereConstructNode
    ASTMaskedElseWhereConstructNode maskedElseWhereConstruct; // in ASTWhereConstructNode
    ASTEndWhereStmtNode endWhereStmt; // in ASTWhereConstructNode
    ASTElseWhereConstructNode elseWhereConstruct; // in ASTWhereConstructNode

    public ASTWhereConstructStmtNode getWhereConstructStmt()
    {
        return this.whereConstructStmt;
    }

    public void setWhereConstructStmt(ASTWhereConstructStmtNode newValue)
    {
        this.whereConstructStmt = newValue;
    }


    public IASTListNode<IWhereBodyConstruct> getWhereBodyConstructBlock()
    {
        return this.whereBodyConstructBlock;
    }

    public void setWhereBodyConstructBlock(IASTListNode<IWhereBodyConstruct> newValue)
    {
        this.whereBodyConstructBlock = newValue;
    }


    public ASTMaskedElseWhereConstructNode getMaskedElseWhereConstruct()
    {
        return this.maskedElseWhereConstruct;
    }

    public void setMaskedElseWhereConstruct(ASTMaskedElseWhereConstructNode newValue)
    {
        this.maskedElseWhereConstruct = newValue;
    }


    public ASTEndWhereStmtNode getEndWhereStmt()
    {
        return this.endWhereStmt;
    }

    public void setEndWhereStmt(ASTEndWhereStmtNode newValue)
    {
        this.endWhereStmt = newValue;
    }


    public ASTElseWhereConstructNode getElseWhereConstruct()
    {
        return this.elseWhereConstruct;
    }

    public void setElseWhereConstruct(ASTElseWhereConstructNode newValue)
    {
        this.elseWhereConstruct = newValue;
    }


    public void accept(IASTVisitor visitor)
    {
        visitor.visitASTWhereConstructNode(this);
        visitor.visitIBodyConstruct(this);
        visitor.visitICaseBodyConstruct(this);
        visitor.visitIExecutableConstruct(this);
        visitor.visitIExecutionPartConstruct(this);
        visitor.visitIForallBodyConstruct(this);
        visitor.visitIWhereBodyConstruct(this);
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
        case 0:  return this.whereConstructStmt;
        case 1:  return this.whereBodyConstructBlock;
        case 2:  return this.maskedElseWhereConstruct;
        case 3:  return this.endWhereStmt;
        case 4:  return this.elseWhereConstruct;
        default: return null;
        }
    }

    @Override protected void setASTField(int index, IASTNode value)
    {
        switch (index)
        {
        case 0:  this.whereConstructStmt = (ASTWhereConstructStmtNode)value; return;
        case 1:  this.whereBodyConstructBlock = (IASTListNode<IWhereBodyConstruct>)value; return;
        case 2:  this.maskedElseWhereConstruct = (ASTMaskedElseWhereConstructNode)value; return;
        case 3:  this.endWhereStmt = (ASTEndWhereStmtNode)value; return;
        case 4:  this.elseWhereConstruct = (ASTElseWhereConstructNode)value; return;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }
}

