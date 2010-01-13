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
public class ASTCriticalConstructNode extends ASTNode implements IExecutableConstruct
{
    ASTCriticalStmtNode criticalStmt; // in ASTCriticalConstructNode
    IASTListNode<IBodyConstruct> body; // in ASTCriticalConstructNode
    ASTEndCriticalStmtNode endCriticalStmt; // in ASTCriticalConstructNode

    public ASTCriticalStmtNode getCriticalStmt()
    {
        return this.criticalStmt;
    }

    public void setCriticalStmt(ASTCriticalStmtNode newValue)
    {
        this.criticalStmt = newValue;
    }


    public IASTListNode<IBodyConstruct> getBody()
    {
        return this.body;
    }

    public void setBody(IASTListNode<IBodyConstruct> newValue)
    {
        this.body = newValue;
    }


    public ASTEndCriticalStmtNode getEndCriticalStmt()
    {
        return this.endCriticalStmt;
    }

    public void setEndCriticalStmt(ASTEndCriticalStmtNode newValue)
    {
        this.endCriticalStmt = newValue;
    }


    public void accept(IASTVisitor visitor)
    {
        visitor.visitASTCriticalConstructNode(this);
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
        case 0:  return this.criticalStmt;
        case 1:  return this.body;
        case 2:  return this.endCriticalStmt;
        default: return null;
        }
    }

    @Override protected void setASTField(int index, IASTNode value)
    {
        switch (index)
        {
        case 0:  this.criticalStmt = (ASTCriticalStmtNode)value; return;
        case 1:  this.body = (IASTListNode<IBodyConstruct>)value; return;
        case 2:  this.endCriticalStmt = (ASTEndCriticalStmtNode)value; return;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }
}

