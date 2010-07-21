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
public class ASTCaseStmtNode extends ASTNodeWithErrorRecoverySymbols implements IActionStmt, ICaseBodyConstruct
{
    org.eclipse.photran.internal.core.lexer.Token label; // in ASTCaseStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTCase; // in ASTCaseStmtNode
    org.eclipse.photran.internal.core.lexer.Token hasDefaultSelector; // in ASTCaseStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTLparen; // in ASTCaseStmtNode
    IASTListNode<ASTCaseValueRangeNode> caseValueRangeListSelector; // in ASTCaseStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTRparen; // in ASTCaseStmtNode
    ASTNameNode name; // in ASTCaseStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTEos; // in ASTCaseStmtNode

    public org.eclipse.photran.internal.core.lexer.Token getLabel()
    {
        return this.label;
    }

    public void setLabel(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.label = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public boolean hasDefaultSelector()
    {
        return this.hasDefaultSelector != null;
    }

    public void setHasDefaultSelector(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.hasDefaultSelector = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public IASTListNode<ASTCaseValueRangeNode> getCaseValueRangeListSelector()
    {
        return this.caseValueRangeListSelector;
    }

    public void setCaseValueRangeListSelector(IASTListNode<ASTCaseValueRangeNode> newValue)
    {
        this.caseValueRangeListSelector = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public ASTNameNode getName()
    {
        return this.name;
    }

    public void setName(ASTNameNode newValue)
    {
        this.name = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    @Override
    public void accept(IASTVisitor visitor)
    {
        visitor.visitASTCaseStmtNode(this);
        visitor.visitIActionStmt(this);
        visitor.visitICaseBodyConstruct(this);
        visitor.visitIActionStmt(this);
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
        case 1:  return this.hiddenTCase;
        case 2:  return this.hasDefaultSelector;
        case 3:  return this.hiddenTLparen;
        case 4:  return this.caseValueRangeListSelector;
        case 5:  return this.hiddenTRparen;
        case 6:  return this.name;
        case 7:  return this.hiddenTEos;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }

    @Override protected void setASTField(int index, IASTNode value)
    {
        switch (index)
        {
        case 0:  this.label = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 1:  this.hiddenTCase = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 2:  this.hasDefaultSelector = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 3:  this.hiddenTLparen = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 4:  this.caseValueRangeListSelector = (IASTListNode<ASTCaseValueRangeNode>)value; if (value != null) value.setParent(this); return;
        case 5:  this.hiddenTRparen = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 6:  this.name = (ASTNameNode)value; if (value != null) value.setParent(this); return;
        case 7:  this.hiddenTEos = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }
}

