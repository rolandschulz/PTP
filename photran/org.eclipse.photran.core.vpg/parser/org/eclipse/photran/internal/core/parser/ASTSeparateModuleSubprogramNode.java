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
public class ASTSeparateModuleSubprogramNode extends ScopingNode implements IModuleSubprogramPartConstruct
{
    ASTMpSubprogramStmtNode mpSubprogramStmt; // in ASTSeparateModuleSubprogramNode
    IASTListNode<IBodyConstruct> body; // in ASTSeparateModuleSubprogramNode
    ASTContainsStmtNode containsStmt; // in ASTSeparateModuleSubprogramNode
    IASTListNode<IInternalSubprogram> internalSubprograms; // in ASTSeparateModuleSubprogramNode
    ASTEndMpSubprogramStmtNode endMpSubprogramStmt; // in ASTSeparateModuleSubprogramNode

    public ASTMpSubprogramStmtNode getMpSubprogramStmt()
    {
        return this.mpSubprogramStmt;
    }

    public void setMpSubprogramStmt(ASTMpSubprogramStmtNode newValue)
    {
        this.mpSubprogramStmt = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public IASTListNode<IBodyConstruct> getBody()
    {
        return this.body;
    }

    public void setBody(IASTListNode<IBodyConstruct> newValue)
    {
        this.body = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public ASTContainsStmtNode getContainsStmt()
    {
        return this.containsStmt;
    }

    public void setContainsStmt(ASTContainsStmtNode newValue)
    {
        this.containsStmt = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public IASTListNode<IInternalSubprogram> getInternalSubprograms()
    {
        return this.internalSubprograms;
    }

    public void setInternalSubprograms(IASTListNode<IInternalSubprogram> newValue)
    {
        this.internalSubprograms = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public ASTEndMpSubprogramStmtNode getEndMpSubprogramStmt()
    {
        return this.endMpSubprogramStmt;
    }

    public void setEndMpSubprogramStmt(ASTEndMpSubprogramStmtNode newValue)
    {
        this.endMpSubprogramStmt = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public void accept(IASTVisitor visitor)
    {
        visitor.visitASTSeparateModuleSubprogramNode(this);
        visitor.visitIModuleSubprogramPartConstruct(this);
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
        case 0:  return this.mpSubprogramStmt;
        case 1:  return this.body;
        case 2:  return this.containsStmt;
        case 3:  return this.internalSubprograms;
        case 4:  return this.endMpSubprogramStmt;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }

    @Override protected void setASTField(int index, IASTNode value)
    {
        switch (index)
        {
        case 0:  this.mpSubprogramStmt = (ASTMpSubprogramStmtNode)value; if (value != null) value.setParent(this); return;
        case 1:  this.body = (IASTListNode<IBodyConstruct>)value; if (value != null) value.setParent(this); return;
        case 2:  this.containsStmt = (ASTContainsStmtNode)value; if (value != null) value.setParent(this); return;
        case 3:  this.internalSubprograms = (IASTListNode<IInternalSubprogram>)value; if (value != null) value.setParent(this); return;
        case 4:  this.endMpSubprogramStmt = (ASTEndMpSubprogramStmtNode)value; if (value != null) value.setParent(this); return;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }
}

