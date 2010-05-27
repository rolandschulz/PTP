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
public class ASTForallStmtNode extends ASTNode implements IActionStmt, IBodyConstruct, ICaseBodyConstruct, IExecutableConstruct, IExecutionPartConstruct, IForallBodyConstruct
{
    org.eclipse.photran.internal.core.lexer.Token label; // in ASTForallStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTForall; // in ASTForallStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTLparen; // in ASTForallStmtNode
    ASTForallTripletSpecListNode forallTripletSpecList; // in ASTForallStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTComma; // in ASTForallStmtNode
    ASTScalarMaskExprNode scalarMaskExpr; // in ASTForallStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTRparen; // in ASTForallStmtNode
    ASTAssignmentStmtNode assignment; // in ASTForallStmtNode

    public org.eclipse.photran.internal.core.lexer.Token getLabel()
    {
        return this.label;
    }

    public void setLabel(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.label = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public ASTForallTripletSpecListNode getForallTripletSpecList()
    {
        return this.forallTripletSpecList;
    }

    public void setForallTripletSpecList(ASTForallTripletSpecListNode newValue)
    {
        this.forallTripletSpecList = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public ASTScalarMaskExprNode getScalarMaskExpr()
    {
        return this.scalarMaskExpr;
    }

    public void setScalarMaskExpr(ASTScalarMaskExprNode newValue)
    {
        this.scalarMaskExpr = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public ASTAssignmentStmtNode getAssignment()
    {
        return this.assignment;
    }

    public void setAssignment(ASTAssignmentStmtNode newValue)
    {
        this.assignment = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    @Override
    public void accept(IASTVisitor visitor)
    {
        visitor.visitASTForallStmtNode(this);
        visitor.visitIActionStmt(this);
        visitor.visitIBodyConstruct(this);
        visitor.visitICaseBodyConstruct(this);
        visitor.visitIExecutableConstruct(this);
        visitor.visitIExecutionPartConstruct(this);
        visitor.visitIForallBodyConstruct(this);
        visitor.visitASTNode(this);
    }

    @Override protected int getNumASTFields()
    {
        return 8;
    }

    @Override protected IASTNode getASTField(int index)
    {
        switch (index)
        {
        case 0:  return this.label;
        case 1:  return this.hiddenTForall;
        case 2:  return this.hiddenTLparen;
        case 3:  return this.forallTripletSpecList;
        case 4:  return this.hiddenTComma;
        case 5:  return this.scalarMaskExpr;
        case 6:  return this.hiddenTRparen;
        case 7:  return this.assignment;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }

    @Override protected void setASTField(int index, IASTNode value)
    {
        switch (index)
        {
        case 0:  this.label = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 1:  this.hiddenTForall = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 2:  this.hiddenTLparen = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 3:  this.forallTripletSpecList = (ASTForallTripletSpecListNode)value; if (value != null) value.setParent(this); return;
        case 4:  this.hiddenTComma = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 5:  this.scalarMaskExpr = (ASTScalarMaskExprNode)value; if (value != null) value.setParent(this); return;
        case 6:  this.hiddenTRparen = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 7:  this.assignment = (ASTAssignmentStmtNode)value; if (value != null) value.setParent(this); return;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }
}

