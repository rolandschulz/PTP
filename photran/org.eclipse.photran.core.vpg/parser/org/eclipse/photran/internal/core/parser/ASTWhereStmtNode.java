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
public class ASTWhereStmtNode extends ASTNode implements IActionStmt, IBodyConstruct, ICaseBodyConstruct, IExecutableConstruct, IExecutionPartConstruct, IForallBodyConstruct, IWhereBodyConstruct
{
    org.eclipse.photran.internal.core.lexer.Token label; // in ASTWhereStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTWhere; // in ASTWhereStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTLparen; // in ASTWhereStmtNode
    ASTMaskExprNode maskExpr; // in ASTWhereStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTRparen; // in ASTWhereStmtNode
    ASTAssignmentStmtNode assignmentStmt; // in ASTWhereStmtNode

    public org.eclipse.photran.internal.core.lexer.Token getLabel()
    {
        return this.label;
    }

    public void setLabel(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.label = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public ASTMaskExprNode getMaskExpr()
    {
        return this.maskExpr;
    }

    public void setMaskExpr(ASTMaskExprNode newValue)
    {
        this.maskExpr = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public ASTAssignmentStmtNode getAssignmentStmt()
    {
        return this.assignmentStmt;
    }

    public void setAssignmentStmt(ASTAssignmentStmtNode newValue)
    {
        this.assignmentStmt = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public void accept(IASTVisitor visitor)
    {
        visitor.visitASTWhereStmtNode(this);
        visitor.visitIActionStmt(this);
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
        return 6;
    }

    @Override protected IASTNode getASTField(int index)
    {
        switch (index)
        {
        case 0:  return this.label;
        case 1:  return this.hiddenTWhere;
        case 2:  return this.hiddenTLparen;
        case 3:  return this.maskExpr;
        case 4:  return this.hiddenTRparen;
        case 5:  return this.assignmentStmt;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }

    @Override protected void setASTField(int index, IASTNode value)
    {
        switch (index)
        {
        case 0:  this.label = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 1:  this.hiddenTWhere = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 2:  this.hiddenTLparen = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 3:  this.maskExpr = (ASTMaskExprNode)value; if (value != null) value.setParent(this); return;
        case 4:  this.hiddenTRparen = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 5:  this.assignmentStmt = (ASTAssignmentStmtNode)value; if (value != null) value.setParent(this); return;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }
}

