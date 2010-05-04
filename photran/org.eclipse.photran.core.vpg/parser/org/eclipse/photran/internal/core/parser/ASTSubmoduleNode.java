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
public class ASTSubmoduleNode extends ScopingNode implements IProgramUnit
{
    ASTSubmoduleStmtNode submoduleStmt; // in ASTSubmoduleNode
    IASTListNode<IModuleBodyConstruct> moduleBody; // in ASTSubmoduleNode
    ASTEndSubmoduleStmtNode endSubmoduleStmt; // in ASTSubmoduleNode

    public ASTSubmoduleStmtNode getSubmoduleStmt()
    {
        return this.submoduleStmt;
    }

    public void setSubmoduleStmt(ASTSubmoduleStmtNode newValue)
    {
        this.submoduleStmt = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public IASTListNode<IModuleBodyConstruct> getModuleBody()
    {
        return this.moduleBody;
    }

    public void setModuleBody(IASTListNode<IModuleBodyConstruct> newValue)
    {
        this.moduleBody = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public ASTEndSubmoduleStmtNode getEndSubmoduleStmt()
    {
        return this.endSubmoduleStmt;
    }

    public void setEndSubmoduleStmt(ASTEndSubmoduleStmtNode newValue)
    {
        this.endSubmoduleStmt = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public void accept(IASTVisitor visitor)
    {
        visitor.visitASTSubmoduleNode(this);
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
        case 0:  return this.submoduleStmt;
        case 1:  return this.moduleBody;
        case 2:  return this.endSubmoduleStmt;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }

    @Override protected void setASTField(int index, IASTNode value)
    {
        switch (index)
        {
        case 0:  this.submoduleStmt = (ASTSubmoduleStmtNode)value; if (value != null) value.setParent(this); return;
        case 1:  this.moduleBody = (IASTListNode<IModuleBodyConstruct>)value; if (value != null) value.setParent(this); return;
        case 2:  this.endSubmoduleStmt = (ASTEndSubmoduleStmtNode)value; if (value != null) value.setParent(this); return;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }
}

