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
    }


    public IASTListNode<IBodyConstruct> getBody()
    {
        return this.body;
    }

    public void setBody(IASTListNode<IBodyConstruct> newValue)
    {
        this.body = newValue;
    }


    public ASTContainsStmtNode getContainsStmt()
    {
        return this.containsStmt;
    }

    public void setContainsStmt(ASTContainsStmtNode newValue)
    {
        this.containsStmt = newValue;
    }


    public IASTListNode<IInternalSubprogram> getInternalSubprograms()
    {
        return this.internalSubprograms;
    }

    public void setInternalSubprograms(IASTListNode<IInternalSubprogram> newValue)
    {
        this.internalSubprograms = newValue;
    }


    public ASTEndFunctionStmtNode getEndFunctionStmt()
    {
        return this.endFunctionStmt;
    }

    public void setEndFunctionStmt(ASTEndFunctionStmtNode newValue)
    {
        this.endFunctionStmt = newValue;
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
        default: return null;
        }
    }

    @Override protected void setASTField(int index, IASTNode value)
    {
        switch (index)
        {
        case 0:  this.functionStmt = (ASTFunctionStmtNode)value;
        case 1:  this.body = (IASTListNode<IBodyConstruct>)value;
        case 2:  this.containsStmt = (ASTContainsStmtNode)value;
        case 3:  this.internalSubprograms = (IASTListNode<IInternalSubprogram>)value;
        case 4:  this.endFunctionStmt = (ASTEndFunctionStmtNode)value;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }
}

