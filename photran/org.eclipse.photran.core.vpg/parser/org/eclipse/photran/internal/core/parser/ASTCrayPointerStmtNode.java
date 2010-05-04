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
public class ASTCrayPointerStmtNode extends ASTNode implements ISpecificationStmt
{
    org.eclipse.photran.internal.core.lexer.Token label; // in ASTCrayPointerStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTPointer; // in ASTCrayPointerStmtNode
    IASTListNode<ASTCrayPointerStmtObjectNode> crayPointerStmtObjectList; // in ASTCrayPointerStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTEos; // in ASTCrayPointerStmtNode

    public org.eclipse.photran.internal.core.lexer.Token getLabel()
    {
        return this.label;
    }

    public void setLabel(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.label = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public IASTListNode<ASTCrayPointerStmtObjectNode> getCrayPointerStmtObjectList()
    {
        return this.crayPointerStmtObjectList;
    }

    public void setCrayPointerStmtObjectList(IASTListNode<ASTCrayPointerStmtObjectNode> newValue)
    {
        this.crayPointerStmtObjectList = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public void accept(IASTVisitor visitor)
    {
        visitor.visitASTCrayPointerStmtNode(this);
        visitor.visitISpecificationStmt(this);
        visitor.visitASTNode(this);
    }

    @Override protected int getNumASTFields()
    {
        return 4;
    }

    @Override protected IASTNode getASTField(int index)
    {
        switch (index)
        {
        case 0:  return this.label;
        case 1:  return this.hiddenTPointer;
        case 2:  return this.crayPointerStmtObjectList;
        case 3:  return this.hiddenTEos;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }

    @Override protected void setASTField(int index, IASTNode value)
    {
        switch (index)
        {
        case 0:  this.label = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 1:  this.hiddenTPointer = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 2:  this.crayPointerStmtObjectList = (IASTListNode<ASTCrayPointerStmtObjectNode>)value; if (value != null) value.setParent(this); return;
        case 3:  this.hiddenTEos = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }
}

