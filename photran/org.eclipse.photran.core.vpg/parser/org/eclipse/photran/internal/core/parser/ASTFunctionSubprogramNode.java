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
public class ASTFunctionSubprogramNode extends ScopingNode implements IInternalSubprogram, IModuleBodyConstruct, IModuleSubprogram, IModuleSubprogramPartConstruct, IProgramUnit
{
    ASTFunctionStmtNode functionStmt; // in ASTFunctionSubprogramNode
    IASTListNode<IBodyConstruct> body; // in ASTFunctionSubprogramNode
    ASTContainsStmtNode containsStmt; // in ASTFunctionSubprogramNode
    IASTListNode<IInternalSubprogram> internalSubprograms; // in ASTFunctionSubprogramNode
    ASTEndFunctionStmtNode endFunctionStmt; // in ASTFunctionSubprogramNode

    public ASTFunctionStmtNode getFunctionStmt()
    {
        return this.functionStmt;
    }

    public void setFunctionStmt(ASTFunctionStmtNode newValue)
    {
        this.functionStmt = newValue;
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


    public ASTEndFunctionStmtNode getEndFunctionStmt()
    {
        return this.endFunctionStmt;
    }

    public void setEndFunctionStmt(ASTEndFunctionStmtNode newValue)
    {
        this.endFunctionStmt = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public void accept(IASTVisitor visitor)
    {
        visitor.visitASTFunctionSubprogramNode(this);
        visitor.visitIInternalSubprogram(this);
        visitor.visitIModuleBodyConstruct(this);
        visitor.visitIModuleSubprogram(this);
        visitor.visitIModuleSubprogramPartConstruct(this);
        visitor.visitIProgramUnit(this);
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
        case 0:  return this.functionStmt;
        case 1:  return this.body;
        case 2:  return this.containsStmt;
        case 3:  return this.internalSubprograms;
        case 4:  return this.endFunctionStmt;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }

    @Override protected void setASTField(int index, IASTNode value)
    {
        switch (index)
        {
        case 0:  this.functionStmt = (ASTFunctionStmtNode)value; if (value != null) value.setParent(this); return;
        case 1:  this.body = (IASTListNode<IBodyConstruct>)value; if (value != null) value.setParent(this); return;
        case 2:  this.containsStmt = (ASTContainsStmtNode)value; if (value != null) value.setParent(this); return;
        case 3:  this.internalSubprograms = (IASTListNode<IInternalSubprogram>)value; if (value != null) value.setParent(this); return;
        case 4:  this.endFunctionStmt = (ASTEndFunctionStmtNode)value; if (value != null) value.setParent(this); return;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }
}

