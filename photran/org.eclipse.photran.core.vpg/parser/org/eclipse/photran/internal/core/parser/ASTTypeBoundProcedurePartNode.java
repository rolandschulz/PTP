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
public class ASTTypeBoundProcedurePartNode extends ASTNode
{
    ASTContainsStmtNode containsStmt; // in ASTTypeBoundProcedurePartNode
    ASTBindingPrivateStmtNode bindingPrivateStmt; // in ASTTypeBoundProcedurePartNode
    IASTListNode<IProcBindingStmt> procBindingStmts; // in ASTTypeBoundProcedurePartNode

    public ASTContainsStmtNode getContainsStmt()
    {
        return this.containsStmt;
    }

    public void setContainsStmt(ASTContainsStmtNode newValue)
    {
        this.containsStmt = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public ASTBindingPrivateStmtNode getBindingPrivateStmt()
    {
        return this.bindingPrivateStmt;
    }

    public void setBindingPrivateStmt(ASTBindingPrivateStmtNode newValue)
    {
        this.bindingPrivateStmt = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public IASTListNode<IProcBindingStmt> getProcBindingStmts()
    {
        return this.procBindingStmts;
    }

    public void setProcBindingStmts(IASTListNode<IProcBindingStmt> newValue)
    {
        this.procBindingStmts = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public void accept(IASTVisitor visitor)
    {
        visitor.visitASTTypeBoundProcedurePartNode(this);
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
        case 0:  return this.containsStmt;
        case 1:  return this.bindingPrivateStmt;
        case 2:  return this.procBindingStmts;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }

    @Override protected void setASTField(int index, IASTNode value)
    {
        switch (index)
        {
        case 0:  this.containsStmt = (ASTContainsStmtNode)value; if (value != null) value.setParent(this); return;
        case 1:  this.bindingPrivateStmt = (ASTBindingPrivateStmtNode)value; if (value != null) value.setParent(this); return;
        case 2:  this.procBindingStmts = (IASTListNode<IProcBindingStmt>)value; if (value != null) value.setParent(this); return;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }
}

