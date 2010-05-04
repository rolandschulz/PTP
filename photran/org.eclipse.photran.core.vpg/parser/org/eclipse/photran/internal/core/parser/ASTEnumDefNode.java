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
public class ASTEnumDefNode extends ASTNode implements IDeclarationConstruct
{
    ASTEnumDefStmtNode enumDefStmt; // in ASTEnumDefNode
    IASTListNode<ASTEnumeratorDefStmtNode> enumeratorDefStmts; // in ASTEnumDefNode
    ASTEndEnumStmtNode endEnumStmt; // in ASTEnumDefNode

    public ASTEnumDefStmtNode getEnumDefStmt()
    {
        return this.enumDefStmt;
    }

    public void setEnumDefStmt(ASTEnumDefStmtNode newValue)
    {
        this.enumDefStmt = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public IASTListNode<ASTEnumeratorDefStmtNode> getEnumeratorDefStmts()
    {
        return this.enumeratorDefStmts;
    }

    public void setEnumeratorDefStmts(IASTListNode<ASTEnumeratorDefStmtNode> newValue)
    {
        this.enumeratorDefStmts = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public ASTEndEnumStmtNode getEndEnumStmt()
    {
        return this.endEnumStmt;
    }

    public void setEndEnumStmt(ASTEndEnumStmtNode newValue)
    {
        this.endEnumStmt = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public void accept(IASTVisitor visitor)
    {
        visitor.visitASTEnumDefNode(this);
        visitor.visitIDeclarationConstruct(this);
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
        case 0:  return this.enumDefStmt;
        case 1:  return this.enumeratorDefStmts;
        case 2:  return this.endEnumStmt;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }

    @Override protected void setASTField(int index, IASTNode value)
    {
        switch (index)
        {
        case 0:  this.enumDefStmt = (ASTEnumDefStmtNode)value; if (value != null) value.setParent(this); return;
        case 1:  this.enumeratorDefStmts = (IASTListNode<ASTEnumeratorDefStmtNode>)value; if (value != null) value.setParent(this); return;
        case 2:  this.endEnumStmt = (ASTEndEnumStmtNode)value; if (value != null) value.setParent(this); return;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }
}

