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

public class ASTModuleNode extends ScopingNode implements IProgramUnit
{
    ASTModuleStmtNode moduleStmt; // in ASTModuleNode
    IASTListNode<IModuleBodyConstruct> moduleBody; // in ASTModuleNode
    ASTEndModuleStmtNode endModuleStmt; // in ASTModuleNode

    public ASTModuleStmtNode getModuleStmt()
    {
        return this.moduleStmt;
    }

    public void setModuleStmt(ASTModuleStmtNode newValue)
    {
        this.moduleStmt = newValue;
    }


    public IASTListNode<IModuleBodyConstruct> getModuleBody()
    {
        return this.moduleBody;
    }

    public void setModuleBody(IASTListNode<IModuleBodyConstruct> newValue)
    {
        this.moduleBody = newValue;
    }


    public ASTEndModuleStmtNode getEndModuleStmt()
    {
        return this.endModuleStmt;
    }

    public void setEndModuleStmt(ASTEndModuleStmtNode newValue)
    {
        this.endModuleStmt = newValue;
    }


    public void accept(IASTVisitor visitor)
    {
        visitor.visitASTModuleNode(this);
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
        case 0:  return this.moduleStmt;
        case 1:  return this.moduleBody;
        case 2:  return this.endModuleStmt;
        default: return null;
        }
    }

    @Override protected void setASTField(int index, IASTNode value)
    {
        switch (index)
        {
        case 0:  this.moduleStmt = (ASTModuleStmtNode)value; return;
        case 1:  this.moduleBody = (IASTListNode<IModuleBodyConstruct>)value; return;
        case 2:  this.endModuleStmt = (ASTEndModuleStmtNode)value; return;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }
}

