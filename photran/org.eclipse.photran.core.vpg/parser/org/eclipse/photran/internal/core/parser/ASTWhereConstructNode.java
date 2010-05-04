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
public class ASTWhereConstructNode extends ASTNode implements IBodyConstruct, ICaseBodyConstruct, IExecutableConstruct, IExecutionPartConstruct, IForallBodyConstruct, IWhereBodyConstruct
{
    ASTWhereConstructStmtNode whereConstructStmt; // in ASTWhereConstructNode
    IASTListNode<IWhereBodyConstruct> whereBodyConstructBlock; // in ASTWhereConstructNode
    ASTElseWhereConstructNode elseWhereConstruct; // in ASTWhereConstructNode
    ASTMaskedElseWhereConstructNode maskedElseWhereConstruct; // in ASTWhereConstructNode
    ASTEndWhereStmtNode endWhereStmt; // in ASTWhereConstructNode

    public ASTWhereConstructStmtNode getWhereConstructStmt()
    {
        return this.whereConstructStmt;
    }

    public void setWhereConstructStmt(ASTWhereConstructStmtNode newValue)
    {
        this.whereConstructStmt = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public IASTListNode<IWhereBodyConstruct> getWhereBodyConstructBlock()
    {
        return this.whereBodyConstructBlock;
    }

    public void setWhereBodyConstructBlock(IASTListNode<IWhereBodyConstruct> newValue)
    {
        this.whereBodyConstructBlock = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public ASTElseWhereConstructNode getElseWhereConstruct()
    {
        return this.elseWhereConstruct;
    }

    public void setElseWhereConstruct(ASTElseWhereConstructNode newValue)
    {
        this.elseWhereConstruct = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public ASTMaskedElseWhereConstructNode getMaskedElseWhereConstruct()
    {
        return this.maskedElseWhereConstruct;
    }

    public void setMaskedElseWhereConstruct(ASTMaskedElseWhereConstructNode newValue)
    {
        this.maskedElseWhereConstruct = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public ASTEndWhereStmtNode getEndWhereStmt()
    {
        return this.endWhereStmt;
    }

    public void setEndWhereStmt(ASTEndWhereStmtNode newValue)
    {
        this.endWhereStmt = newValue;
        if (newValue != null) newValue.setParent(this);
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
        case 2:  return this.elseWhereConstruct;
        case 3:  return this.maskedElseWhereConstruct;
        case 4:  return this.endWhereStmt;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }

    @Override protected void setASTField(int index, IASTNode value)
    {
        switch (index)
        {
        case 0:  this.whereConstructStmt = (ASTWhereConstructStmtNode)value; if (value != null) value.setParent(this); return;
        case 1:  this.whereBodyConstructBlock = (IASTListNode<IWhereBodyConstruct>)value; if (value != null) value.setParent(this); return;
        case 2:  this.elseWhereConstruct = (ASTElseWhereConstructNode)value; if (value != null) value.setParent(this); return;
        case 3:  this.maskedElseWhereConstruct = (ASTMaskedElseWhereConstructNode)value; if (value != null) value.setParent(this); return;
        case 4:  this.endWhereStmt = (ASTEndWhereStmtNode)value; if (value != null) value.setParent(this); return;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }
}

